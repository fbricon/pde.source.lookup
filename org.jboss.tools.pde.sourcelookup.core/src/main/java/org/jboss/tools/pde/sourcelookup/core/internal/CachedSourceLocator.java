/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.pde.sourcelookup.core.internal;

import static org.jboss.tools.pde.sourcelookup.core.internal.utils.BundleUtil.getLocalBundleSourcePathIfExists;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.pde.sourcelookup.core.internal.preferences.SourceLookupPreferences;
import org.jboss.tools.pde.sourcelookup.core.internal.utils.BundleUtil;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Locates artifact sources from local cache folders
 *
 * @author Fred Bricon
 */
public class CachedSourceLocator implements ISourceArtifactLocator {

  private Path M2_REPO = Paths.get(System.getProperty("user.home"), ".m2", "repository");
  private static final String SHA1_SEARCH_QUERY = "http://search.maven.org/solrsearch/select?q=1:%22{0}%22&rows=1&wt=json";

  private Map<String, GAV> SHA1_MAP = new ConcurrentHashMap<>();
  private static GAV UNKNOWN_ARTIFACT = new GAV("unknown", "unknown", "unknown");

  @Override
  public IPath findSources(File jar, IProgressMonitor monitor) {
    if (jar == null || !jar.isFile()) {
      return null;
    }
    IArtifactKey artifactKey = BundleUtil.getArtifactKey(jar);

    IPath sourcePath = findBundleSources(artifactKey, monitor);

    if (sourcePath == null) {
      sourcePath = findSourcesNextToFile(jar, monitor);
    }

    if (sourcePath == null) {
      sourcePath = findSourcesInMavenRepo(jar, monitor);
    }

    return sourcePath;
  }

  private GAV findGAVFromMavenCentral(File jar, IProgressMonitor monitor) {
    String sha1 = "";
    GAV key = null;
    try {
      sha1 = Files.asByteSource(jar).hash(Hashing.sha1()).toString();
      key = SHA1_MAP.get(sha1);
      if (UNKNOWN_ARTIFACT.equals(key)) {
        return null;
      } else if (key != null) {
        return key;
      }
      String searchUrl = NLS.bind(SHA1_SEARCH_QUERY, sha1);
      try (Reader reader = new InputStreamReader(new BufferedInputStream(new URL(searchUrl).openStream()),
          StandardCharsets.UTF_8)) {
        JsonObject json = new Gson().fromJson(reader, JsonObject.class).getAsJsonObject();
        key = parse(json);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (key == null) {
        key = UNKNOWN_ARTIFACT;
      }
    }
    SHA1_MAP.put(sha1, key);
    return UNKNOWN_ARTIFACT.equals(key) ? null : key;
  }

  private GAV parse(JsonObject json) {
    JsonObject response = json.getAsJsonObject("response");
    if (response != null) {
      int num = response.get("numFound").getAsInt();
      if (num == 1) {
        JsonObject docs = response.getAsJsonArray("docs").get(0).getAsJsonObject();
        if (docs.isJsonObject()) {
          String a = docs.get("a").getAsString();
          String g = docs.get("g").getAsString();
          String v = docs.get("v").getAsString();
          if (a != null && g != null && v != null) {
            return new GAV(g, a, v);
          }
        }
      }
    }
    return null;
  }
  /**
   * @param jar
   * @param monitor
   * @return
   */
  private IPath findSourcesNextToFile(File jar, IProgressMonitor monitor) {
    String name = jar.getName();
    if (name.endsWith(".jar")) {
      String sourceName = name.replace(".jar", "-sources.jar");
      File sourceFile = new File(jar.getParentFile(), sourceName);
      if (sourceFile.isFile()) {
        return new org.eclipse.core.runtime.Path(sourceFile.getAbsolutePath());
      }
    }
    return null;
  }

  public IPath findBundleSources(IArtifactKey artifactKey, IProgressMonitor monitor) {
    if (artifactKey == null) {
      return null;
    }
    if (monitor == null) {
      monitor = new NullProgressMonitor();
    }
    IArtifactKey sourceKey = BundleUtil.toSourceKey(artifactKey);

    for (Path cacheLocation : SourceLookupPreferences.getInstance().getCacheLocations()) {
      if (monitor.isCanceled()) {
        break;
      }
      IPath localCache = getLocalBundleSourcePathIfExists(cacheLocation, sourceKey);
      if (localCache != null) {
        return localCache;
      }
    }
    return null;
  }

  public IPath findSourcesInMavenRepo(File jar, IProgressMonitor monitor) {
    GAV gav = getGAV(jar);
    if (gav == null) {
      gav = findGAVFromMavenCentral(jar, monitor);
    }
    if (gav != null) {
      String fileName = gav.artifactId + "-" + gav.version + "-sources.jar";
      Path sourcePath = M2_REPO
          .resolve(Paths.get(gav.groupId.replace(".", "/"), gav.artifactId, gav.version, fileName));

      File sourceJar = sourcePath.toFile();
      if (sourceJar.exists()
          && (!jar.getName().endsWith("-SNAPSHOT.jar") || sourceJar.lastModified() >= jar.lastModified())) {
        IArtifactKey ak = BundleUtil.getArtifactKey(jar);
        if (ak != null) {
          // if snapshot bundle is found, ensure this is the exact version
          IArtifactKey sak = BundleUtil.getArtifactKey(sourceJar);
          if (sak != null && !Objects.equals(sak.getVersion(), ak.getVersion())) {
            return null;
          }
        }

        return new org.eclipse.core.runtime.Path(sourcePath.toString());
      }
    }
    return null;
  }

  private GAV getGAV(File jar) {
    if (jar == null || !jar.isFile() || !jar.canRead()) {
      return null;
    }
    GAV artifact = null;
    try (ZipFile zip = new ZipFile(jar)) {
      String mavenDir = "META-INF/maven";
      if (zip.getEntry(mavenDir) == null) {
        return null;
      }

      Enumeration<? extends ZipEntry> zipEntries = zip.entries();
      while (zipEntries.hasMoreElements()) {
        ZipEntry zipEntry = zipEntries.nextElement();
        if (zipEntry.getName().startsWith(mavenDir) && zipEntry.getName().endsWith("pom.properties")) {
          if (artifact != null) {
            // multiple values, which one to use?
            return null;
          }
          Properties props = new Properties();
          props.load(zip.getInputStream(zipEntry));
          String groupId = props.getProperty("groupId");
          String artifactId = props.getProperty("artifactId");
          String version = props.getProperty("version");
          if (groupId != null && artifactId != null && version != null) {
            artifact = new GAV(groupId, artifactId, version);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return artifact;
  }

  public static class GAV {

    private String groupId;
    private String artifactId;
    private String version;

    public GAV(String groupId, String artifactId, String version) {
      this.groupId = groupId;
      this.artifactId = artifactId;
      this.version = version;
    }

    @Override
    public String toString() {
      return groupId + ":" + artifactId + ":" + version;
    }

    public static GAV fromString(String gav) {
      if (gav != null) {
        if ("unknown:unknown:unknown".equals(gav)) {
          return UNKNOWN_ARTIFACT;
        }
        String[] coords = gav.split(":");
        if (coords.length > 2) {
          return new GAV(coords[0], coords[1], coords[2]);
        }
      }
      return null;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
      result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
      result = prime * result + ((version == null) ? 0 : version.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      GAV other = (GAV) obj;
      if (artifactId == null) {
        if (other.artifactId != null) {
          return false;
        }
      } else if (!artifactId.equals(other.artifactId)) {
        return false;
      }
      if (groupId == null) {
        if (other.groupId != null) {
          return false;
        }
      } else if (!groupId.equals(other.groupId)) {
        return false;
      }
      if (version == null) {
        if (other.version != null) {
          return false;
        }
      } else if (!version.equals(other.version)) {
        return false;
      }
      return true;
    }
  }
}

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

import static org.jboss.tools.pde.sourcelookup.core.internal.utils.BundleUtil.getLocalSourcePathIfExists;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.jboss.tools.pde.sourcelookup.core.internal.preferences.SourceLookupPreferences;
import org.jboss.tools.pde.sourcelookup.core.internal.utils.BundleUtil;

/**
 * Locates artifact sources from local cache folders
 *
 * @author Fred Bricon
 */
public class CachedSourceLocator implements ISourceFileLocator {

  private Path M2_REPO = Paths.get(System.getProperty("user.home"), ".m2", "repository");

  @Override
  public IPath findSources(IArtifactKey artifactKey, IProgressMonitor monitor) {
    if (artifactKey == null) {
      return null;
    }
    IArtifactKey sourceKey = BundleUtil.toSourceKey(artifactKey);

    for (Path cacheLocation : SourceLookupPreferences.getInstance().getCacheLocations()) {
      IPath localCache = getLocalSourcePathIfExists(cacheLocation, sourceKey);
      if (localCache != null) {
        return localCache;
      }
    }
    return null;
  }

  @Override
  public IPath findSources(File jar, IProgressMonitor monitor) {
    GAV gav = getGAV(jar);
    if (gav != null) {
      String fileName = gav.artifactId + "-" + gav.version + "-sources.jar";
      Path sourcePath = M2_REPO
          .resolve(Paths.get(gav.groupId.replace(".", "/"), gav.artifactId, gav.version, fileName));
      if (Files.exists(sourcePath)) {
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

  private static class GAV {
    private String groupId;
    private String artifactId;
    private String version;

    public GAV(String groupId, String artifactId, String version) {
      this.groupId = groupId;
      this.artifactId = artifactId;
      this.version = version;
    }
  }
}

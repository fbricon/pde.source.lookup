/*******************************************************************************
 * Copyright (c) 2018-2020 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/

package org.jboss.tools.pde.sourcelookup.core.internal.maven;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Objects;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.jboss.tools.pde.sourcelookup.core.internal.ISourceArtifactLocator;
import org.jboss.tools.pde.sourcelookup.core.internal.utils.BundleUtil;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * @author Fred Bricon
 *
 */
public class SimpleMavenSourceLocator implements ISourceArtifactLocator {

  private java.nio.file.Path M2_REPO = Paths.get(System.getProperty("user.home"), ".m2", "repository");

  private static final String SHA1_SEARCH_QUERY = "https://search.maven.org/solrsearch/select?wt=json&rows=1&q=1:";

  @Override
  public IPath findSources(File jar, IProgressMonitor monitor) {
    GAV gav = MavenUtils.getGAV(jar);
    if (gav == null) {
      gav = findGAVFromMavenCentral(jar, monitor);
    }
    if (gav != null) {
      return resolveSourcePath(jar, gav, monitor);
    }
    return null;
  }

  protected IPath resolveSourcePath(File jar, GAV gav, IProgressMonitor monitor) {
    String fileName = gav.getArtifactId() + "-" + gav.getVersion() + "-sources.jar";
    java.nio.file.Path sourcePath = M2_REPO
        .resolve(Paths.get(gav.getGroupId().replace(".", "/"), gav.getArtifactId(), gav.getVersion(), fileName));

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
    }
    return null;
  }

  protected GAV findGAVFromMavenCentral(File jar, IProgressMonitor monitor) {
    String sha1 = "";
    GAV key = null;
    try {
      sha1 = MavenUtils.getSha1(jar);
      key = MavenUtils.getGAVFromCache(sha1);
      if (key != null) {
        return GAV.UNKNOWN_ARTIFACT.equals(key) ? null : key;
      }
      try (
          Reader reader = new InputStreamReader(new BufferedInputStream(new URL(SHA1_SEARCH_QUERY + sha1).openStream()),
              StandardCharsets.UTF_8)) {
        JsonObject json = new Gson().fromJson(reader, JsonObject.class);
        key = parse(json);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (key == null) {
        key = GAV.UNKNOWN_ARTIFACT;
      }
    }
    MavenUtils.cacheGAV(sha1, key);
    return GAV.UNKNOWN_ARTIFACT.equals(key) ? null : key;
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

}

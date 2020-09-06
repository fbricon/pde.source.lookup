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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.Platform;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class MavenUtils {

  private static final boolean isM2eAvailable;

  private static Map<String, GAV> SHA1_MAP = new ConcurrentHashMap<>();

  static {
    isM2eAvailable = Platform.getBundle("org.eclipse.m2e.core") != null;
  }

  private MavenUtils() {

  }

  public static boolean isM2eAvailable() {
    return isM2eAvailable;
  }

  public static GAV getGAV(File jar) {
    String sha1;
    GAV key = null;
    try {
      sha1 = getSha1(jar);
      key = getGAVFromCache(sha1);
    } catch (IOException e) {
      // ignore
    }
    if (key != null) {
      return GAV.UNKNOWN_ARTIFACT.equals(key) ? null : key;
    }
    try {
      return findGAVInFile(jar);
    } catch (IOException e) {
      // ignore
    }
    return null;
  }

  public static GAV findGAVInFile(File jar) throws IOException {
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
          try (InputStream is = zip.getInputStream(zipEntry)) {
            props.load(is);
          }
          String groupId = props.getProperty("groupId");
          String artifactId = props.getProperty("artifactId");
          String version = props.getProperty("version");
          if (groupId != null && artifactId != null && version != null) {
            artifact = new GAV(groupId, artifactId, version);
          }
        }
      }
    }
    return artifact;
  }

  public static GAV getGAVFromCache(String sha1) {
    return SHA1_MAP.get(sha1);
  }

  public static void cacheGAV(String sha1, GAV gav) {
    SHA1_MAP.put(sha1, gav);
  }

  public static String getSha1(File jar) throws IOException {
    return Files.hash(jar, Hashing.sha1()).toString();
  }

}

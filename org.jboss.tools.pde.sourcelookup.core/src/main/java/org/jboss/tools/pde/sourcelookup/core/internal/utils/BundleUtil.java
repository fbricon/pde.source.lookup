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
package org.jboss.tools.pde.sourcelookup.core.internal.utils;

import java.io.File;
import java.nio.file.Files;
import java.util.Dictionary;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.publisher.eclipse.BundlesAction;
import org.eclipse.osgi.util.ManifestElement;

@SuppressWarnings("restriction")
public class BundleUtil {

  private BundleUtil() {
    //prevent instantiation
  }

  public static boolean isBundle(File file) {
    return getArtifactKey(file) != null;
  }

  public static IArtifactKey getArtifactKey(File file) {
    if (file == null || !file.exists()) {
      return null;
    }
    String id = null;
    String version = null;
    try {
      Dictionary<String, String> manifest = BundlesAction.loadManifest(file);
      if (manifest != null) {
        id = ManifestElement.parseHeader("Bundle-SymbolicName",
            manifest.get("Bundle-SymbolicName"))
            [0].getValue();
        version = manifest.get("Bundle-Version");
      }
    } catch (Exception e) {
      return null;
    }

    if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(version)) {
      return BundlesAction.createBundleArtifactKey(id, version);
    }
    return null;
  }

  public static IArtifactKey toSourceKey(IArtifactKey key) {
    if (key == null) {
      return null;
    }
    String name = key.getId();
    if (name.endsWith(".source")) {
      return key;
    }
    return BundlesAction.createBundleArtifactKey(name + ".source", key.getVersion().toString());
  }

  public static IPath getLocalSourcePathIfExists(java.nio.file.Path cacheFolder, IArtifactKey artifactKey) {
    java.nio.file.Path sourcePath = getLocalSourcePath(cacheFolder, artifactKey);
    return Files.exists(sourcePath) ? toIPath(sourcePath) : null;
  }

  public static java.nio.file.Path getLocalSourcePath(java.nio.file.Path cacheFolder, IArtifactKey artifactKey) {
    java.nio.file.Path sourcePath = cacheFolder.resolve(artifactKey.getId() + "_" + artifactKey.getVersion() + ".jar");
    return sourcePath;
  }

  private static IPath toIPath(java.nio.file.Path path) {
    return org.eclipse.core.runtime.Path.fromOSString(path.toAbsolutePath().toString());
  }
}

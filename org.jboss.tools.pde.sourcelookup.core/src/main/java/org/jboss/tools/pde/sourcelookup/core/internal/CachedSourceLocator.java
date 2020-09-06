/*******************************************************************************
 * Copyright (c) 2016-2020 Red Hat Inc. and others.
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

package org.jboss.tools.pde.sourcelookup.core.internal;

import static org.jboss.tools.pde.sourcelookup.core.internal.utils.BundleUtil.getLocalBundleSourcePathIfExists;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.jboss.tools.pde.sourcelookup.core.internal.preferences.SourceLookupPreferences;
import org.jboss.tools.pde.sourcelookup.core.internal.utils.BundleUtil;

/**
 * Locates artifact sources from local cache folders
 *
 * @author Fred Bricon
 */
public class CachedSourceLocator implements ISourceArtifactLocator {

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

    return sourcePath;
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

}

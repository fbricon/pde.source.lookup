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

import java.nio.file.Path;

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
public class CachedSourceLocator implements ISourceLocator {

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

}

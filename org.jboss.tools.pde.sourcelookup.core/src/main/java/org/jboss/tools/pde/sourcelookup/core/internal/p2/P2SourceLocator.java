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

package org.jboss.tools.pde.sourcelookup.core.internal.p2;

import static org.jboss.tools.pde.sourcelookup.core.internal.utils.BundleUtil.getLocalBundleSourcePathIfExists;
import static org.jboss.tools.pde.sourcelookup.core.internal.utils.BundleUtil.getLocalSourcePath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.operations.RepositoryTracker;
import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.jboss.tools.pde.sourcelookup.core.internal.CoreActivator;
import org.jboss.tools.pde.sourcelookup.core.internal.ISourceArtifactLocator;
import org.jboss.tools.pde.sourcelookup.core.internal.ProvisioningManager;
import org.jboss.tools.pde.sourcelookup.core.internal.preferences.SourceLookupPreferences;
import org.jboss.tools.pde.sourcelookup.core.internal.utils.BundleUtil;

/**
 * Locates artifact sources from P2 repositories.
 *
 * @author Fred Bricon
 */
public class P2SourceLocator implements ISourceArtifactLocator {

  private List<String> blackList = Arrays.asList("org.eclipse.swt");

  @Override
  public IPath findSources(File jar, IProgressMonitor monitor) {
    if (!BundleUtil.isBundle(jar)) {
      return null;
    }
    IArtifactKey artifactKey = BundleUtil.getArtifactKey(jar);
    if (artifactKey == null || blackList.contains(artifactKey.getId())) {
      return null;
    }
    IArtifactKey sourceKey = BundleUtil.toSourceKey(artifactKey);

    ProvisioningManager provisioningManager = CoreActivator.getInstance().getProvisioningManager();

    RepositoryTracker repositoryTracker = provisioningManager.getRepositoryTracker();
    if (repositoryTracker == null) {
      CoreActivator.log("Failed to load repositoryTracker");
      return null;
    }
    List<URI> uris = Arrays
        .asList(repositoryTracker.getKnownRepositories(provisioningManager.getSession()));
    Collections.sort(uris);// stupid trick to make eclipse.org repos being
    // searched almost first

    Path cacheFolder = SourceLookupPreferences.getInstance().getDownloadedSourcesDirectory();
    SubMonitor subMonitor = SubMonitor.convert(monitor, uris.size() * 100);
    for (URI repo : uris) {
      if (monitor.isCanceled()) {
        return null;
      }
      IArtifactRepository artifactRepo = null;
      try {
        artifactRepo = provisioningManager.loadArtifactRepository(repo, false, subMonitor.newChild(90));
      } catch (ProvisionException ignored) {
        CoreActivator.log("Failed to load artifact repository " + repo + ":" + ignored.getMessage());
        // local urls seem to fail
      }
      if (artifactRepo == null || !artifactRepo.contains(sourceKey)) {
        continue;
      }

      IArtifactDescriptor[] results = artifactRepo.getArtifactDescriptors(sourceKey);
      if (results.length > 0) {
        try {
          IPath artifact = saveArtifact(artifactRepo, results[0], cacheFolder, subMonitor.newChild(10));
          subMonitor.done();
          return artifact;
        } catch (Exception e) {
          CoreActivator.log("Failed to save artifact", e);
        }
      }
    }
    return null;
  }

  private IPath saveArtifact(IArtifactRepository artifactRepo, IArtifactDescriptor artifactDescriptor, Path cacheFolder,
      IProgressMonitor monitor) throws IOException {
    Files.createDirectories(cacheFolder);
    IArtifactKey artifactKey = artifactDescriptor.getArtifactKey();
    Path sourcePath = getLocalSourcePath(cacheFolder, artifactKey);
    boolean missing = !Files.exists(sourcePath);
    if (missing) {
      File sourceFile = Files.createFile(sourcePath).toFile();
      try (FileOutputStream stream = new FileOutputStream(sourceFile)) {
        artifactRepo.getArtifact(artifactDescriptor, stream, monitor);
      }
    }
    IPath localBundle = getLocalBundleSourcePathIfExists(cacheFolder, artifactKey);
    if (missing && localBundle != null) {
      CoreActivator.log("Saved " + localBundle);
    }
    return localBundle;
  }

}

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
package org.jboss.tools.pde.sourcelookup.ui.internal.jobs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathSupport;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.jboss.tools.pde.sourcelookup.core.internal.preferences.SourceLookupPreferences;
import org.jboss.tools.pde.sourcelookup.core.internal.utils.BundleUtil;

@SuppressWarnings("restriction")
public class P2SourceDownloadJob extends Job {

  private final Set<IPackageFragmentRoot> queue = new LinkedHashSet<>();

  public P2SourceDownloadJob() {
    super("Plugin Source Download");
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    if (monitor == null) {
      monitor = new NullProgressMonitor();
    }
    Collection<IPackageFragmentRoot> currentQueue;
    synchronized (this.queue) {
      currentQueue = new LinkedHashSet<>(this.queue);
      this.queue.clear();
    }

    for (IPackageFragmentRoot fragment : currentQueue) {
      if (monitor.isCanceled()) {
        break;
      }
      findAndAttachSources(fragment, monitor);
    }
    // schedule remaining requests that were added during this run
    synchronized (this.queue) {
      if (!queue.isEmpty()) {
        schedule();
      }
    }
    return Status.OK_STATUS;
  }

  private void findAndAttachSources(IPackageFragmentRoot fragment, IProgressMonitor monitor) {
    monitor.setTaskName("Searching sources for "+fragment.getElementName());
    // Check if fragment is a bundle, else bail
    if (!isValid(fragment)) {
      return;
    }
    try {
      IPath path = findSources(fragment, monitor);
      if (path != null) {
        // System.err.println("Attaching " + path);
        attachSource(fragment, path, monitor);
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

  private boolean isValid(IPackageFragmentRoot fragment) {
    return BundleUtil.isBundle(fragment.getPath().toFile());
  }

  private IPath findSources(IPackageFragmentRoot fragment, IProgressMonitor monitor)
      throws CoreException {
    if (!(fragment instanceof JarPackageFragmentRoot)) {
      return null;
    }
    monitor.setTaskName(fragment.getElementName());
    IArtifactKey artifactKey = BundleUtil.getArtifactKey(fragment.getPath().toFile());

    IArtifactKey sourceKey = BundleUtil.toSourceKey(artifactKey);

    Path cacheFolder = SourceLookupPreferences.getInstance().getDownloadedSourcesDirectory();
    IPath localCache = getLocalSourcePathIfExists(cacheFolder, sourceKey);
    if (localCache != null) {
      return localCache;
    }

    ProvisioningUI provisioningUI = ProvisioningUI.getDefaultUI();

    List<URI> uris = Arrays
        .asList(provisioningUI.getRepositoryTracker().getKnownRepositories(provisioningUI.getSession()));
    Collections.sort(uris);// stupid trick to make eclipse.org repos being
    // searched almost first

    // System.err.println("Searching sources for " +
    // fragment.getElementName());
    for (URI repo : uris) {
      if (monitor.isCanceled()) {
        return null;
      }
      try {
        IArtifactRepository artifactRepo = provisioningUI.loadArtifactRepository(repo, false, monitor);
        if (!artifactRepo.contains(sourceKey)) {
          continue;
        }
        IArtifactDescriptor[] results = artifactRepo.getArtifactDescriptors(sourceKey);
        if (results.length > 0) {
          return saveArtifact(artifactRepo, results[0], cacheFolder, monitor);
        }
      } catch (ProvisionException | OperationCanceledException | IOException e) {
        e.printStackTrace();
        return null;
      }
    }
    return null;
  }

  /**
   * @param cacheFolder
   * @param iArtifactDescriptor
   * @return
   * @throws IOException
   */
  private IPath saveArtifact(IArtifactRepository artifactRepo, IArtifactDescriptor artifactDescriptor,
      Path cacheFolder, IProgressMonitor monitor) throws IOException {
    Files.createDirectories(cacheFolder);
    IArtifactKey artifactKey = artifactDescriptor.getArtifactKey();
    Path sourcePath = getLocalSourcePath(cacheFolder, artifactKey);
    if (!Files.exists(sourcePath)) {
      File sourceFile = Files.createFile(sourcePath).toFile();
      try (FileOutputStream stream = new FileOutputStream(sourceFile)) {
        artifactRepo.getArtifact(artifactDescriptor, stream, monitor);
      }
    }
    return getLocalSourcePathIfExists(cacheFolder, artifactKey);
  }

  private Path getLocalSourcePath(Path cacheFolder, IArtifactKey artifactKey) {
    Path sourcePath = cacheFolder.resolve(artifactKey.getId() + "_" + artifactKey.getVersion() + ".jar");
    return sourcePath;
  }

  private IPath getLocalSourcePathIfExists(Path cacheFolder, IArtifactKey artifactKey) {
    Path sourcePath = getLocalSourcePath(cacheFolder, artifactKey);
    return Files.exists(sourcePath) ? toIPath(sourcePath) : null;
  }

  private IPath toIPath(Path path) {
    return org.eclipse.core.runtime.Path.fromOSString(path.toAbsolutePath().toString());
  }

  // Copied from JBoss Tools
  private void attachSource(final IPackageFragmentRoot fragment, final IPath newSourcePath,
      IProgressMonitor monitor) {
    try {
      if (fragment == null || fragment.getKind() != IPackageFragmentRoot.K_BINARY) {
        return;
      }

      //if (!Objects.equals(fragment.getSourceAttachmentPath(), newSourcePath)) {
      // would be so cool if it refreshed the UI
      // fragment.attachSource(newSourcePath, null, monitor);
      //}

      IPath containerPath = null;
      IJavaProject jproject = fragment.getJavaProject();
      IClasspathEntry entry = fragment.getRawClasspathEntry();
      if (entry != null && entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
        containerPath = entry.getPath();
        ClasspathContainerInitializer initializer = JavaCore
            .getClasspathContainerInitializer(containerPath.segment(0));
        IClasspathContainer container = JavaCore.getClasspathContainer(containerPath, jproject);
        if (initializer == null || container == null) {
          return;
        }
        IStatus status = initializer.getSourceAttachmentStatus(containerPath, jproject);
        if (status.getCode() == ClasspathContainerInitializer.ATTRIBUTE_NOT_SUPPORTED
            || status.getCode() == ClasspathContainerInitializer.ATTRIBUTE_READ_ONLY) {
          return;
        }
        entry = JavaModelUtil.findEntryInContainer(container, fragment.getPath());
        if (entry == null) {
          return;
        }
      }
      CPListElement elem = CPListElement.createFromExisting(entry, null);
      elem.setAttribute(CPListElement.SOURCEATTACHMENT, newSourcePath);
      IClasspathEntry entry1 = elem.getClasspathEntry();
      if (entry1.equals(entry)) {
        return;
      }
      IClasspathEntry newEntry = entry1;
      String[] changedAttributes = { CPListElement.SOURCEATTACHMENT };
      BuildPathSupport.modifyClasspathEntry(null, newEntry, changedAttributes, jproject, containerPath,
          newEntry.getReferencingEntry() != null, monitor);
    } catch (CoreException e) {
      // ignore
    }
  }

  public void queue(IPackageFragmentRoot... fragments) {
    if (fragments == null || fragments.length == 0) {
      return;
    }
    synchronized (queue) {
      queue.addAll(Arrays.asList(fragments));
    }
  }

}

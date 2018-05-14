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
package org.jboss.tools.pde.sourcelookup.core.internal.jobs;

import java.io.File;
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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.jboss.tools.pde.sourcelookup.core.internal.ISourceArtifactLocator;
import org.jboss.tools.pde.sourcelookup.core.internal.utils.ClasspathUtils;

@SuppressWarnings("restriction")
public abstract class AbstractSourceDownloadJob extends Job {

  private final Set<IPackageFragmentRoot> queue = new LinkedHashSet<>();

  private List<ISourceArtifactLocator> sourceLocators;

  public AbstractSourceDownloadJob(String name, ISourceArtifactLocator... sourceLocators) {
    super(name);
    this.sourceLocators = sourceLocators == null ? Collections.emptyList() : Arrays.asList(sourceLocators);
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
      if (!queue.isEmpty() && !monitor.isCanceled()) {
        schedule();
      }
    }
    return Status.OK_STATUS;
  }

  private void findAndAttachSources(IPackageFragmentRoot fragment, IProgressMonitor monitor) {
    monitor.setTaskName("Searching sources for "+fragment.getElementName());
    try {
      if (ClasspathUtils.hasSources(fragment)) {
        return;
      }
      IPath path = findSources(fragment, monitor);
      if (path != null) {
        ClasspathUtils.attachSource(fragment, path, monitor);
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

  protected IPath findSources(IPackageFragmentRoot fragment, IProgressMonitor monitor)
      throws CoreException {
    monitor.setTaskName(fragment.getElementName());
    IPath path = null;
    try {
      File jar = fragment.getResource() == null ? fragment.getPath().toFile()
          : fragment.getResource().getRawLocation().toFile();
      path = sourceLocators.stream().map(sl -> findSource(sl, jar, monitor)).filter(p -> p != null).findFirst()
          .orElse(null);
    } catch (Exception e) {
      System.err.println("can't find sources for " + fragment.getPath() + ": " + e.getMessage());
    }

    return path;
  }

  private IPath findSource(ISourceArtifactLocator locator, File jar, IProgressMonitor monitor) {
    IPath path = locator.findSources(jar, monitor);
    return path;
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

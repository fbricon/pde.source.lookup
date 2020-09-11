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

package org.jboss.tools.pde.sourcelookup.core.internal.jobs;

import static org.jboss.tools.pde.sourcelookup.core.internal.utils.ProjectUtils.isPluginProject;

import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.jboss.tools.pde.sourcelookup.core.internal.CachedSourceLocator;
import org.jboss.tools.pde.sourcelookup.core.internal.utils.ClasspathUtils;

/**
 * @author Fred Bricon
 *
 */
public class ReattachProjectBundleSourcesJob extends Job {

  private IPackageFragmentRoot[] pluginContainerEntries;

  public ReattachProjectBundleSourcesJob() {
    super("Collecting plugin projects");
    addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        if (event.getResult().isOK() && pluginContainerEntries != null && pluginContainerEntries.length > 0) {
          AbstractSourceDownloadJob sourceAttacherJob = new AbstractSourceDownloadJob(
              "Re-attaching project bundle sources", new CachedSourceLocator()) {
          };
          sourceAttacherJob.queue(pluginContainerEntries);
          sourceAttacherJob.schedule();
        }
      }
    });
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {

    IWorkspace workspace = null;
    try {
      workspace = ResourcesPlugin.getWorkspace();
    } catch (Throwable isClosing) {
      return Status.CANCEL_STATUS;
    }

    IProject[] projects = workspace.getRoot().getProjects();
    pluginContainerEntries = Stream.of(projects).filter(p -> isPluginProject(p))
        .map(p -> ClasspathUtils.getPluginContainerEntries(p)) //
        .flatMap(binaries -> Stream.of(binaries)) //
        .filter(pfr -> ClasspathUtils.isBinaryFragment(pfr)) //
        .toArray(s -> new IPackageFragmentRoot[s]); //
    return Status.OK_STATUS;
  }

}

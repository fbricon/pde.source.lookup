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

import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElementDelta;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * @author Fred Bricon
 *
 */
@SuppressWarnings("restriction")
public class ClasspathUtils {

  private ClasspathUtils() {
    // prevent instantiation
  }

  public static IClasspathEntry findEntryInContainer(IClasspathContainer container, IPath libPath) {
    if (container == null || libPath == null) {
      return null;
    }
    for (IClasspathEntry cpe : container.getClasspathEntries()) {
      IClasspathEntry resolved = JavaCore.getResolvedClasspathEntry(cpe);
      if (resolved != null && libPath.equals(resolved.getPath())) {
        return cpe;
      }
    }
    return null;
  }

  public static boolean isPluginContainer(IClasspathEntry cpe) {
    if (cpe != null && cpe.getPath() != null) {
      String path = cpe.getPath().toString();
      return "org.eclipse.pde.core.requiredPlugins".equals(path);
    }
    return false;
  }

  public static boolean isBinaryFragment(IPackageFragmentRoot pfr) {
    try {
      return pfr != null && pfr.getKind() == IPackageFragmentRoot.K_BINARY;
    } catch (JavaModelException e) {
      return false;
    }
  }

  public static void attachSource(final IPackageFragmentRoot fragment, final IPath newSourcePath,
      IProgressMonitor monitor) {
    try {
      if (fragment == null || fragment.getKind() != IPackageFragmentRoot.K_BINARY) {
        return;
      }
      if (!Objects.equals(fragment.getSourceAttachmentPath(), newSourcePath)) {
        // would be so cool if it refreshed the UI automatically
        fragment.attachSource(newSourcePath, null, monitor);
        // close the root so that source attachment cache is flushed. Else UI
        // won't update
        fragment.close();
        // we have to manually fire a delta to notify the UI about the source
        // attachment.
        JavaModelManager manager = JavaModelManager.getJavaModelManager();
        JavaElementDelta attachedSourceDelta = new JavaElementDelta(fragment.getJavaModel());
        attachedSourceDelta.sourceAttached(fragment);
        manager.getDeltaProcessor().fire(attachedSourceDelta, ElementChangedEvent.POST_CHANGE);
      }
    } catch (CoreException e) {
      // ignore
    }
  }

  public static IPackageFragmentRoot[] getPluginContainerEntries(IProject project) {
    if (!ProjectUtils.isPluginProject(project)) {
      return new IPackageFragmentRoot[0];
    }
    IJavaProject javaProject = JavaCore.create(project);

    IClasspathEntry pluginContainer = null;
    try {
      pluginContainer = Stream.of(javaProject.getRawClasspath()).filter(cpe -> isPluginContainer(cpe)).findFirst()
          .orElse(null);
    } catch (JavaModelException e) {
      e.printStackTrace();
    }
    if (pluginContainer == null) {
      return new IPackageFragmentRoot[0];
    }
    IPackageFragmentRoot[] pfr = javaProject.findPackageFragmentRoots(pluginContainer);
    return pfr;
  }
}

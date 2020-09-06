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

package org.jboss.tools.pde.sourcelookup.ui.internal.actions;

import static org.jboss.tools.pde.sourcelookup.ui.internal.utils.UIUtils.isPluginContainer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.jboss.tools.pde.sourcelookup.core.internal.CoreActivator;
import org.jboss.tools.pde.sourcelookup.core.internal.utils.ClasspathUtils;

/**
 * @author Fred Bricon
 */
public class DownloadSourcesHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    ISelection selection = HandlerUtil.getCurrentSelection(event);
    Collection<IPackageFragmentRoot> fragments = getSelectedClasspathEntries(selection);
    if (!fragments.isEmpty()) {
      CoreActivator.getInstance().getSourceLookupManager()
      .findSources(fragments.toArray(new IPackageFragmentRoot[fragments.size()]));
    }
    return null;
  }

  private Collection<IPackageFragmentRoot> getSelectedClasspathEntries(ISelection selection) {
    if (!(selection instanceof ITreeSelection)) {
      return Collections.emptyList();
    }
    ITreeSelection structuredSelection = (ITreeSelection) selection;
    Set<IPackageFragmentRoot> fragments = new LinkedHashSet<>(structuredSelection.size());
    for (Object o : structuredSelection.toList()) {
      if (o instanceof IPackageFragmentRoot) {
        IPackageFragmentRoot pfr = (IPackageFragmentRoot) o;
        if (ClasspathUtils.isBinaryFragment(pfr) && belongsToPluginContainer(structuredSelection, pfr)) {
          fragments.add(pfr);
        }
      } else if (isPluginContainer(o)) {
        IAdaptable adaptable = (IAdaptable) o;
        IWorkbenchAdapter wa = adaptable.getAdapter(IWorkbenchAdapter.class);
        if (wa != null) {
          Object[] children = wa.getChildren(o);
          if (children instanceof IAdaptable[]) {
            IAdaptable[] adaptables = (IAdaptable[]) children;
            fragments.addAll(filterPackageFragmentRoots(adaptables));
          }
        }
      }
    }
    return fragments;
  }

  /**
   * @param structuredSelection
   * @param pfr
   * @return
   */
  private boolean belongsToPluginContainer(ITreeSelection structuredSelection, IPackageFragmentRoot pfr) {
    TreePath[] paths = structuredSelection.getPathsFor(pfr);
    if (paths.length < 1) {
      return false;
    }
    TreePath path = paths[0];
    int length = path.getSegmentCount();
    if (length < 2) {
      return false;
    }
    Object parent = path.getSegment(length - 2);
    return isPluginContainer(parent);
  }

  /**
   * @param adaptables
   * @return
   */
  private Collection<IPackageFragmentRoot> filterPackageFragmentRoots(IAdaptable[] adaptables) {
    return Stream.of(adaptables).map(a -> a.getAdapter(IPackageFragmentRoot.class)).filter(pfr -> ClasspathUtils.isBinaryFragment(pfr))
        .collect(Collectors.toList());
  }


}

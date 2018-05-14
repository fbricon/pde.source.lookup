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

import static org.jboss.tools.pde.sourcelookup.core.internal.utils.ClasspathUtils.hasSources;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.jboss.tools.pde.sourcelookup.core.internal.CoreActivator;
import org.jboss.tools.pde.sourcelookup.core.internal.preferences.SourceLookupPreferences;
import org.jboss.tools.pde.sourcelookup.ui.internal.UIActivator;

@SuppressWarnings("restriction")
public class DownloadSourcesActionDelegate implements IEditorActionDelegate, IObjectActionDelegate {

  private IStructuredSelection selection;

  @Override
  public void run(IAction action) {
    if (selection == null || selection.isEmpty()) {
      return;
    }
    boolean isAutomaticDiscoveryEnabled = Platform.getPreferencesService().getBoolean(CoreActivator.PLUGIN_ID,
        SourceLookupPreferences.ENABLED_KEY, true, null);
    if (!isAutomaticDiscoveryEnabled) {
      return;
    }

    Set<IPackageFragmentRoot> queue = new LinkedHashSet<>();
    for(Iterator<?> it = selection.iterator(); it.hasNext();) {
      Object element = it.next();
      if(element instanceof IPackageFragmentRoot) {
        IPackageFragmentRoot fragment = (IPackageFragmentRoot) element;
        try {
          if (canProcess(fragment)) {
            queue.add(fragment);
          }
        } catch (CoreException e) {
          e.printStackTrace();
        }
      }
    }
    findSources(queue);
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof IStructuredSelection) {
      this.selection = (IStructuredSelection) selection;
    } else {
      this.selection = null;
    }
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart workbenchPart) {
    //Don't care
  }

  @Override
  public void setActiveEditor(IAction action, IEditorPart part) {
    if (part != null && part.getEditorInput() instanceof IClassFileEditorInput) {
      Set<IPackageFragmentRoot> queue = new LinkedHashSet<>();
      try {
        IClassFileEditorInput input = (IClassFileEditorInput) part.getEditorInput();
        IJavaElement element = input.getClassFile();
        while (element.getParent() != null) {
          element = element.getParent();
          if (element instanceof IPackageFragmentRoot) {
            IPackageFragmentRoot fragment = (IPackageFragmentRoot) element;
            if (canProcess(fragment)) {
              queue.add(fragment);
            }
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      findSources(queue);
    }
  }

  private void findSources(Collection<IPackageFragmentRoot> queue) {
    if (!queue.isEmpty()) {
      UIActivator.getInstance().getSourceLookupManager()
      .findSources(queue.toArray(new IPackageFragmentRoot[queue.size()]));
    }
  }

  private boolean canProcess(IPackageFragmentRoot fragment) throws CoreException {
    return isBinaryProject(fragment) && !hasSources(fragment);
  }

  private boolean isBinaryProject(IPackageFragmentRoot fragment) {
    try {
      return fragment.getKind() == IPackageFragmentRoot.K_BINARY;
    } catch (JavaModelException e) {
      return false;
    }
  }

}

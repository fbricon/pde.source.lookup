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

import java.util.Iterator;
import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.jboss.tools.pde.sourcelookup.core.internal.utils.ProjectUtils;
import org.jboss.tools.pde.sourcelookup.ui.internal.jobs.P2SourceDownloadJob;

public class DownloadSourcesActionDelegate implements IEditorActionDelegate, IObjectActionDelegate {

	private IStructuredSelection selection;

	@Override
	public void run(IAction action) {
		if (selection == null || selection.isEmpty()) {
			return;
		}
		for(Iterator<?> it = selection.iterator(); it.hasNext();) {
			Object element = it.next();
			if(element instanceof IPackageFragmentRoot) {
				scheduleDownload((IPackageFragmentRoot) element);
			}
		}
	}

	private void scheduleDownload(IPackageFragmentRoot fragment) {
		IProject project = fragment.getJavaProject().getProject();
		if (!ProjectUtils.isPluginProject(project)) {
			return;
		}
		if(hasSources(fragment)) {
			return;
		}
		new P2SourceDownloadJob(fragment).schedule();
	}

	private boolean hasSources(IPackageFragmentRoot fragment) {
		return false;
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

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.
	 * action.IAction, org.eclipse.ui.IEditorPart)
	 */
	@Override
	public void setActiveEditor(IAction action, IEditorPart part) {
		if (part != null && part.getEditorInput() instanceof IClassFileEditorInput) {
			try {
				IClassFileEditorInput input = (IClassFileEditorInput) part.getEditorInput();
				IJavaElement element = input.getClassFile();
				while (element.getParent() != null) {
					element = element.getParent();
					if (element instanceof IPackageFragmentRoot) {
						IPackageFragmentRoot fragment = (IPackageFragmentRoot) element;
						IPath filePath = fragment.getPath();
						IPath sourcePath = fragment.getSourceAttachmentPath();
						if (Objects.equals(sourcePath, filePath)) {
							//TODO check that sourcePath actualy contains the proper source file
							scheduleDownload(fragment);
						}

					}
				}
			} catch (Exception ex) {
			}
		}
	}

}

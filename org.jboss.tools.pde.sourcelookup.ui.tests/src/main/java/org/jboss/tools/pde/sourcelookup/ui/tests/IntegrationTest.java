/*******************************************************************************

 * Copyright (c) 2016 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.pde.sourcelookup.ui.tests;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.StructuredSelection;
import org.jboss.tools.pde.sourcelookup.core.internal.preferences.SourceLookupPreferences;
import org.jboss.tools.pde.sourcelookup.ui.internal.actions.DownloadSourcesActionDelegate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntegrationTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void basicSingleJarTest() throws Exception {
		final String id = "org.foo.bar";
		final String version = "1.0.0";
		final String jarName = id + "_" + version + ".jar";
		final String sourceJarName = id + ".source" + "_" + version + ".jar";
		final String resourceDir = "/resources/";
		final String sourceRepoRelPath = resourceDir + "source-repo";

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("integration-test");
		// Create the project as a Java project
		project.delete(true, null);
		project.create(null);
		project.open(null);
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
		project.setDescription(description, null);

		IJavaProject jProject = JavaCore.create(project);

		// binary jar file location
		String jarPath = FileLocator.toFileURL(getClass().getResource(resourceDir + jarName)).getPath();
		IPath binJarPath = Path.fromOSString(jarPath);

		// Add the binary jar to the classpath of the java project
		List<IClasspathEntry> newCPE = new ArrayList<>(Arrays.asList(jProject.getRawClasspath()));
		IClasspathEntry jarCPE = JavaCore.newLibraryEntry(binJarPath, null, null);
		newCPE.add(jarCPE);
		jProject.setRawClasspath(newCPE.toArray(new IClasspathEntry[0]), new NullProgressMonitor());

		// Set the default source attachment as the binary jar
		// This is necessary to trigger our PDE source lookup
		IPackageFragmentRoot pfr = jProject.getPackageFragmentRoot(jarPath);
		pfr.attachSource(binJarPath, null, new NullProgressMonitor());

		// source jar artifact repository location
		String sourcePath = FileLocator.toFileURL(getClass().getResource(sourceRepoRelPath)).getPath();
		URI sourceLoc = URI.create("file:" + sourcePath);

		// Inform the provisioning agent of the artifact repository containing
		// the source bundle
		ProvisioningUI pui = ProvisioningUI.getDefaultUI();
		pui.getRepositoryTracker().addRepository(sourceLoc, null, pui.getSession());

		// Simulate opening the classfile in the editor
		// TODO: Could we ever get JavaUI.openInEditor working with Tycho ?
//		IJavaElement jElement = jProject.findType("org.foo.bar.Main");
//		JavaUI.openInEditor(jElement);
		DownloadSourcesActionDelegate delegate = new DownloadSourcesActionDelegate();
		delegate.selectionChanged(null, new StructuredSelection(jProject.getPackageFragmentRoot(jarPath)));
		delegate.run(null);

		// Exepect to find the downloaded jar at this location
		File downloadedSourceJarFile = SourceLookupPreferences.getDownloadedSourcesDirectory()
				.resolve(sourceJarName).toFile();
		while (!downloadedSourceJarFile.exists()) {
			Thread.sleep(1000);
		}
	}

}

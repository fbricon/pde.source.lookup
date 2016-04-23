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
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.jboss.tools.pde.sourcelookup.core.internal.preferences.SourceLookupPreferences;
import org.jboss.tools.pde.sourcelookup.ui.internal.actions.DownloadSourcesActionDelegate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntegrationTest {

  private Path sourcesDirectory;

  @Before
  public void setUp() throws Exception {
    sourcesDirectory = Paths.get("target", "sources");
    SourceLookupPreferences.getInstance().setDownloadedSourcesDirectory(sourcesDirectory);
    deleteDirectory(sourcesDirectory);
  }

  private void deleteDirectory(java.nio.file.Path directory) throws IOException {
    if (!Files.exists(directory)) {
      return;
    }
    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }

    });
  }

  @After
  public void tearDown() throws Exception {
    //reset sources directory
    SourceLookupPreferences.getInstance().setDownloadedSourcesDirectory(null);
  }

  @Test
  public void basicSingleJarTest() throws Exception {
    final String id = "org.foo.bar";
    final String version = "1.0.0";
    final String jarName = id + "_" + version + ".jar";
    final String sourceJarName = id + ".source" + "_" + version + ".jar";
    final String resourceDir = "/resources/";
    final String sourceRepoRelPath = resourceDir + "valid-source-repo";

    IJavaProject jProject = createJavaProject("integration-test");

    // binary jar file location
    String jarPath = FileLocator.toFileURL(getClass().getResource(resourceDir + jarName)).getPath();
    IPath binJarPath = org.eclipse.core.runtime.Path.fromOSString(jarPath);

    // Add the binary jar to the classpath of the java project
    addBinaries(jProject, binJarPath);

    // Set the default source attachment as the binary jar
    // This is necessary to trigger our PDE source lookup
    IPackageFragmentRoot pfr = jProject.getPackageFragmentRoot(jarPath);
    pfr.attachSource(binJarPath, null, new NullProgressMonitor());

    // source jar artifact repository location
    URI sourceLoc = getSourceRepoURL(sourceRepoRelPath);

    // Inform the provisioning agent of the artifact repository containing
    // the source bundle
    addP2Repositories(getSourceRepoURL(resourceDir + "invalid-source-repo"), sourceLoc);

    // Simulate opening the classfile in the editor
    // TODO: Could we ever get JavaUI.openInEditor working with Tycho ?
    //		IJavaElement jElement = jProject.findType("org.foo.bar.Main");
    //		JavaUI.openInEditor(jElement);
    DownloadSourcesActionDelegate delegate = new DownloadSourcesActionDelegate();
    delegate.selectionChanged(null, new StructuredSelection(jProject.getPackageFragmentRoot(jarPath)));
    delegate.run(null);

    // Expect to find the downloaded jar at this location
    File downloadedSourceJarFile = sourcesDirectory.resolve(sourceJarName).toFile();
    while (!downloadedSourceJarFile.exists()) {
      Thread.sleep(1000);
    }
  }

  /**
   * @param resourceDir
   * @param sourceLoc
   * @throws IOException
   */
  private void addP2Repositories(URI... p2Repos) throws IOException {
    ProvisioningUI pui = ProvisioningUI.getDefaultUI();
    for (URI repo : p2Repos) {
      pui.getRepositoryTracker().addRepository(repo, null, pui.getSession());
    }
  }

  private IJavaProject createJavaProject(String name) throws CoreException {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = root.getProject(name);
    // Create the project as a Java project
    project.delete(true, null);
    project.create(null);
    project.open(null);
    IProjectDescription description = project.getDescription();
    description.setNatureIds(new String[] { JavaCore.NATURE_ID });
    project.setDescription(description, null);

    IJavaProject jProject = JavaCore.create(project);
    return jProject;
  }

  private void addBinaries(IJavaProject jProject, IPath... binJarPath) throws JavaModelException {
    List<IClasspathEntry> newCPE = new ArrayList<>(Arrays.asList(jProject.getRawClasspath()));
    for (IPath path : binJarPath) {
      IClasspathEntry jarCPE = JavaCore.newLibraryEntry(path, null, null);
      newCPE.add(jarCPE);
    }
    jProject.setRawClasspath(newCPE.toArray(new IClasspathEntry[0]), new NullProgressMonitor());
  }

  private URI getSourceRepoURL(final String relativePath) throws IOException {
    String sourcePath = FileLocator.toFileURL(getClass().getResource(relativePath)).getPath();
    return URI.create("file:" + sourcePath);
  }

}

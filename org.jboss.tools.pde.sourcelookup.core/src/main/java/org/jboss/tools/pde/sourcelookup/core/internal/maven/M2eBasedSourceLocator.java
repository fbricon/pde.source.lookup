/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.pde.sourcelookup.core.internal.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;

public class M2eBasedSourceLocator extends SimpleMavenSourceLocator {

  @Override
  protected IPath resolveSourcePath(File jar, GAV gav, IProgressMonitor monitor) {
    String groupId = gav.getGroupId();
    String artifactId = gav.getArtifactId();
    String version = gav.getVersion();

    IMaven maven = MavenPlugin.getMaven();
    try {
      List<ArtifactRepository> repositories = new ArrayList<>();
      repositories.addAll(maven.getArtifactRepositories());
      repositories.addAll(maven.getPluginArtifactRepositories());

      if (!maven.isUnavailable(groupId, artifactId, version, "jar", "sources", repositories)) {
        Artifact resolve = maven.resolve(groupId, artifactId, version, "jar", "sources", null, monitor);
        return new Path(resolve.getFile().getAbsolutePath());
      }
    } catch (CoreException e) {
      // TODO maybe log, ignore otherwise
    }
    return null;
  }

}

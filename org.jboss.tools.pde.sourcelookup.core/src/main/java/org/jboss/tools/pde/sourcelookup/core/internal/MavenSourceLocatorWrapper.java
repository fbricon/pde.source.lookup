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

package org.jboss.tools.pde.sourcelookup.core.internal;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.pde.sourcelookup.core.internal.maven.M2eBasedSourceLocator;
import org.jboss.tools.pde.sourcelookup.core.internal.maven.MavenUtils;
import org.jboss.tools.pde.sourcelookup.core.internal.maven.SimpleMavenSourceLocator;

public class MavenSourceLocatorWrapper implements ISourceArtifactLocator {

  private ISourceArtifactLocator delegateLocator = null;

  public MavenSourceLocatorWrapper() {
    if (MavenUtils.isM2eAvailable()) {
      delegateLocator = new M2eBasedSourceLocator();
    } else {
      delegateLocator = new SimpleMavenSourceLocator();
    }
  }

  @Override
  public IPath findSources(File jar, IProgressMonitor monitor) {
    return delegateLocator.findSources(jar, monitor);
  }

}

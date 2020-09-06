/*******************************************************************************
 * Copyright (c) 2018-2020 Red Hat Inc. and others.
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

package org.jboss.tools.pde.sourcelookup.core.internal.maven;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.pde.sourcelookup.core.internal.ISourceArtifactLocator;

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

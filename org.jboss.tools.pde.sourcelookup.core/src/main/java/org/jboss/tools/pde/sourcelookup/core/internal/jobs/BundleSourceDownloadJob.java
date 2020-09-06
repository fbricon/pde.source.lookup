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

import org.jboss.tools.pde.sourcelookup.core.internal.CachedSourceLocator;
import org.jboss.tools.pde.sourcelookup.core.internal.maven.MavenSourceLocatorWrapper;
import org.jboss.tools.pde.sourcelookup.core.internal.p2.P2SourceLocator;

public class BundleSourceDownloadJob extends AbstractSourceDownloadJob {

  public BundleSourceDownloadJob() {
    super("Bundle Sources Download", new CachedSourceLocator(),
        new MavenSourceLocatorWrapper(),
        new P2SourceLocator());
  }

}

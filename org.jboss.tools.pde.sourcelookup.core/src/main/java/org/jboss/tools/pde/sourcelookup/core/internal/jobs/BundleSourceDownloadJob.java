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

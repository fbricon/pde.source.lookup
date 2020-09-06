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

package org.jboss.tools.pde.sourcelookup.core.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.jboss.tools.pde.sourcelookup.core.internal.jobs.BundleSourceDownloadJob;

/**
 * @author Fred Bricon
 *
 */
public class BundleSourceLookupManager implements ISourceLookupManager {

  private BundleSourceDownloadJob sourceLookupJob;

  public BundleSourceLookupManager(BundleSourceDownloadJob sourceLookupJob) {
    Assert.isNotNull(sourceLookupJob, "Source lookup job can't be null");
    this.sourceLookupJob = sourceLookupJob;
  }

  @Override
  public void findSources(IPackageFragmentRoot...fragments) {
    sourceLookupJob.queue(fragments);
    sourceLookupJob.schedule();
  }

}

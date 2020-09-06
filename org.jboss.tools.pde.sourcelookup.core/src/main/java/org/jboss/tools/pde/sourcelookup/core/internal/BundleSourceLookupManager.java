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

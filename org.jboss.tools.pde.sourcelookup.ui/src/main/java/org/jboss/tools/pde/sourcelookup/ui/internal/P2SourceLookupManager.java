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

package org.jboss.tools.pde.sourcelookup.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.jboss.tools.pde.sourcelookup.ui.internal.jobs.P2SourceDownloadJob;

/**
 * @author Fred Bricon
 *
 */
public class P2SourceLookupManager implements ISourceLookupManager {

	private P2SourceDownloadJob sourceLookupJob;

	public P2SourceLookupManager(P2SourceDownloadJob sourceLookupJob) {
		Assert.isNotNull(sourceLookupJob, "source lookup job can't be null");
		this.sourceLookupJob = sourceLookupJob;
	}

	@Override
	public void findSources(IPackageFragmentRoot...fragments) {
		sourceLookupJob.queue(fragments);
		sourceLookupJob.schedule();
	}

}

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
package org.jboss.tools.pde.sourcelookup.ui.internal.jobs;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.jboss.tools.pde.sourcelookup.core.internal.model.BundleModel;
import org.jboss.tools.pde.sourcelookup.core.internal.utils.BundleUtil;

@SuppressWarnings("restriction")
public class P2SourceDownloadJob extends Job {

	private IPackageFragmentRoot fragment;

	public P2SourceDownloadJob(IPackageFragmentRoot fragment) {
		super("PDE Source Download");
		Assert.isNotNull(fragment, "Fragment can not be null");
		this.fragment = fragment;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.setTaskName("Searching sources for "+fragment.getElementName());

		// Check if fragment is a bundle, else bail
		if (!isValid(fragment)) {
			return Status.OK_STATUS;
		}
		System.err.println("Searching sources for " + fragment.getElementName());
		try {
			IInstallableUnit iu = findInstallableUnit(fragment, monitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		// Build IU from fragment

		// sourceIU = getCachedSourceIU(iu, cacheDirectory, loader)

		// if sourceIU == null

		// sourceIU = fetchP2SourceIU(iu, cacheDirectory) ////have p2 load all
		// IUs from all update sites, IU->SourceIU, that might take a while

		// if (sourceIU == null) bail

		// attach sourceIU location from cacheDirectory to CPE from fragment

		// done

		return Status.OK_STATUS;
	}

	/**
	 * @param fragment
	 * @return
	 */
	private boolean isValid(IPackageFragmentRoot fragment) {
		if (fragment instanceof JarPackageFragmentRoot) {
			try (ZipFile zip = ((JarPackageFragmentRoot) fragment).getJar()) {
				return BundleUtil.isBundle(zip);
			} catch (CoreException | IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @param monitor
	 * @param fragment2
	 * @return
	 * @throws CoreException
	 */
	private IInstallableUnit findInstallableUnit(IPackageFragmentRoot fragment, IProgressMonitor monitor)
			throws CoreException {

		ProvisioningUI provisioningUI = ProvisioningUI.getDefaultUI();

		List<URI> uris = Arrays
				.asList(provisioningUI.getRepositoryTracker().getKnownRepositories(provisioningUI.getSession()));
		Collections.sort(uris);

		if (!(fragment instanceof JarPackageFragmentRoot)) {
			return null;
		}
		BundleModel bundleModel = BundleUtil.getBundleModel(((JarPackageFragmentRoot)fragment).getJar());

		// IProvisioningAgentProvider provider =
		// PlatformUI.getWorkbench().getService(IProvisioningAgentProvider.class);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		// IMetadataRepositoryManager metadataManager =
		// (IMetadataRepositoryManager) agent
		// .getService(IMetadataRepositoryManager.SERVICE_NAME);
		// System.err.println(metadataManager);
		// URI[] knownRepos =
		// metadataManager.getKnownRepositories(IMetadataRepositoryManager.REPOSITORIES_ALL);
		Version version = Version.parseVersion(bundleModel.getVersion());
		IQuery<IInstallableUnit> query = QueryUtil.createIUQuery(bundleModel.getBundleName() + ".source", version);
		for (URI repo : uris) {
			if (monitor.isCanceled()) {
				return null;
			}
			try {
				System.err.println("Loading p2 repo at " + repo);
				IMetadataRepository metadataRepo = provisioningUI.loadMetadataRepository(repo, false, monitor);
				IQueryResult<IInstallableUnit> result = metadataRepo.query(query, monitor);
				if (!result.isEmpty()) {
					IInstallableUnit sourceIU = result.iterator().next();
					System.err.println("found " + sourceIU.getId() + " in " + repo);
					return sourceIU;
				}
			} catch (ProvisionException | OperationCanceledException e) {
				e.printStackTrace();
				return null;
			}
		}
		System.err.println("Nope");
		return null;
	}

}

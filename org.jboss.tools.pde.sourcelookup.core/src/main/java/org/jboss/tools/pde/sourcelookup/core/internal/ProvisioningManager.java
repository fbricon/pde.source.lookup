/*******************************************************************************
 * Copyright (c) 2009-2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation of org.eclipse.equinox.p2.ui.ProvisioningUI
 *     Sonatype, Inc. - ongoing development
 *     Red Hat Inc. - Bug 460967
 *     Red Hat Inc. - stripped down org.eclipse.equinox.p2.ui.ProvisioningUI to ProvisioningManager
 ******************************************************************************/

package org.jboss.tools.pde.sourcelookup.core.internal;

import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.RepositoryTracker;
import org.eclipse.equinox.p2.repository.IRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;

/**
 * Stripped down version of org.eclipse.equinox.p2.ui.ProvisioningUI
 *
 */
public class ProvisioningManager {

  private ProvisioningSession session;

  public ProvisioningManager(ProvisioningSession session) {
    this.session = session;
  }

  public RepositoryTracker getRepositoryTracker() {
    return session.getProvisioningAgent().getService(RepositoryTracker.class);
  }

  public ProvisioningSession getSession() {
    return session;
  }

  /**
   * Load the specified artifact repository, signaling a repository operation
   * start event before loading, and a repository operation complete event after
   * loading.
   *
   * @param location
   *          the location of the repository
   * @param update
   *          <code>true</code> if the UI should be updated as a result of the
   *          load, <code>false</code> if it should not
   * @param monitor
   *          the progress monitor to be used
   * @return the repository
   * @throws ProvisionException
   *           if the repository could not be loaded
   */
  public IArtifactRepository loadArtifactRepository(URI location, boolean update, IProgressMonitor monitor)
      throws ProvisionException {
    IArtifactRepository repo;
    IArtifactRepositoryManager manager = getArtifactRepositoryManager(getSession());
    repo = manager.loadRepository(location, monitor);

    // If there is no user nickname assigned to this repo but there is a
    // provider
    // name, then set the nickname.
    // This will keep the name in the manager even when the repo is not loaded
    String name = manager.getRepositoryProperty(location, IRepository.PROP_NICKNAME);
    if (name == null) {
      name = manager.getRepositoryProperty(location, IRepository.PROP_NAME);
      if (name != null) {
        manager.setRepositoryProperty(location, IRepository.PROP_NICKNAME, name);
      }
    }
    return repo;
  }

  /**
   * Return the artifact repository manager for the given session
   *
   * @return the repository manager
   */
  public IArtifactRepositoryManager getArtifactRepositoryManager(ProvisioningSession session) {
    return session.getProvisioningAgent().getService(IArtifactRepositoryManager.class);
  }

}

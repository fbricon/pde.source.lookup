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

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.jboss.tools.pde.sourcelookup.core.internal.jobs.BundleSourceDownloadJob;
import org.jboss.tools.pde.sourcelookup.core.internal.jobs.ReattachProjectBundleSourcesJob;
import org.jboss.tools.pde.sourcelookup.core.internal.preferences.SourceLookupPreferences;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

@SuppressWarnings("restriction")
public class CoreActivator implements BundleActivator {

  public static final String ROOT_PLUGIN_ID = "org.jboss.tools.pde.sourcelookup";

  public static final String PLUGIN_ID = ROOT_PLUGIN_ID+".core";

  private static final ILog LOGGER = Platform.getLog(CoreActivator.class);

  private static CoreActivator instance;

  private ProvisioningManager provisioningManager;

  private BundleContext context;

  private ISourceLookupManager sourceLookupManager;

  @Override
  public void start(BundleContext context) throws Exception {
    instance = this;
    this.context = context;
    sourceLookupManager = new BundleSourceLookupManager(new BundleSourceDownloadJob());
    boolean reattachSourcesOnStartup = Platform.getPreferencesService().getBoolean(PLUGIN_ID,
        SourceLookupPreferences.REATTACH_SOURCES_ON_STARTUP_KEY, true, null);
    if (reattachSourcesOnStartup) {
      new ReattachProjectBundleSourcesJob().schedule(2_000);
    }
  }

  public static void log(String msg) {
    log(msg, null);
  }

  public static void log(String msg, Exception e) {
    LOGGER.log(new Status((e == null ? Status.INFO : Status.ERROR), PLUGIN_ID, msg, e));
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    instance = null;
    sourceLookupManager = null;
    context = null;
  }

  public static CoreActivator getInstance() {
    return instance;
  }


  public ProvisioningManager getProvisioningManager() {
    if (provisioningManager == null) {
      IProvisioningAgent agent = ServiceHelper.getService(context, IProvisioningAgent.class);
      ProvisioningSession session = new ProvisioningSession(agent);
      provisioningManager = new ProvisioningManager(session);
    }
    return provisioningManager;
  }

  public ISourceLookupManager getSourceLookupManager() {
    return sourceLookupManager;
  }

}
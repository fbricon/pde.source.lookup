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

import org.eclipse.core.runtime.Platform;
import org.jboss.tools.pde.sourcelookup.core.internal.jobs.ReattachProjectBundleSourcesJob;
import org.jboss.tools.pde.sourcelookup.core.internal.preferences.SourceLookupPreferences;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class CoreActivator implements BundleActivator {

  public static final String ROOT_PLUGIN_ID = "org.jboss.tools.pde.sourcelookup";

  public static final String PLUGIN_ID = ROOT_PLUGIN_ID+".core";

  private static CoreActivator instance;

  @Override
  public void start(BundleContext context) throws Exception {
    instance = this;
    boolean reattachSourcesOnStartup = Platform.getPreferencesService().getBoolean(PLUGIN_ID,
        SourceLookupPreferences.REATTACH_SOURCES_ON_STARTUP_KEY, true, null);
    if (reattachSourcesOnStartup) {
      new ReattachProjectBundleSourcesJob().schedule(2_000);
    }
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    instance = null;
  }

  public static CoreActivator getInstance() {
    return instance;
  }
}
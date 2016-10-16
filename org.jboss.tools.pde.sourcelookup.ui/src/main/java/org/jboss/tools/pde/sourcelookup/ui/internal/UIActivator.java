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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.jboss.tools.pde.sourcelookup.core.internal.CoreActivator;
import org.jboss.tools.pde.sourcelookup.ui.internal.jobs.P2SourceDownloadJob;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class UIActivator implements BundleActivator {

  public static final String PLUGIN_ID =  CoreActivator.ROOT_PLUGIN_ID+".ui";
  private static UIActivator instance;
  private ISourceLookupManager sourceLookupManager;
  private ScopedPreferenceStore preferenceStore;

  @Override
  public void start(BundleContext context) throws Exception {
    instance = this;
    sourceLookupManager = new P2SourceLookupManager(new P2SourceDownloadJob());
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    sourceLookupManager = null;
    instance = null;
  }


  public static UIActivator getInstance() {
    return instance;
  }

  public ISourceLookupManager getSourceLookupManager() {
    return sourceLookupManager;
  }

  /**
   * @return
   */
  public IPreferenceStore getPreferenceStore() {
    // Create the preference store lazily.
    if (preferenceStore == null) {
      preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, CoreActivator.PLUGIN_ID);
    }
    return preferenceStore;
  }
}

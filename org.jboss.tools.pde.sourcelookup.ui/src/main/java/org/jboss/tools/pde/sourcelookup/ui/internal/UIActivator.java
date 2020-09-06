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
package org.jboss.tools.pde.sourcelookup.ui.internal;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.jboss.tools.pde.sourcelookup.core.internal.CoreActivator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class UIActivator implements BundleActivator {

  public static final String PLUGIN_ID =  CoreActivator.ROOT_PLUGIN_ID+".ui";
  private static UIActivator instance;
  private ScopedPreferenceStore preferenceStore;

  @Override
  public void start(BundleContext context) throws Exception {
    instance = this;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    instance = null;
  }


  public static UIActivator getInstance() {
    return instance;
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

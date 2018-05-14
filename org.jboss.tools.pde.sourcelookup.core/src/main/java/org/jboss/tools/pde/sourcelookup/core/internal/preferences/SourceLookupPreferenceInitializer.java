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

package org.jboss.tools.pde.sourcelookup.core.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.jboss.tools.pde.sourcelookup.core.internal.CoreActivator;

/**
 * @author Fred Bricon
 */
public class SourceLookupPreferenceInitializer extends AbstractPreferenceInitializer {

  @Override
  public void initializeDefaultPreferences() {
    IEclipsePreferences store = DefaultScope.INSTANCE.getNode(CoreActivator.PLUGIN_ID);
    store.putBoolean(SourceLookupPreferences.ENABLED_KEY, true);
    store.putBoolean(SourceLookupPreferences.REATTACH_SOURCES_ON_STARTUP_KEY, true);
    store.put(SourceLookupPreferences.DEFAULT_SOURCES_DIRECTORY_KEY, SourceLookupPreferences.DEFAULT_SOURCES_DIRECTORY);
  }

}

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

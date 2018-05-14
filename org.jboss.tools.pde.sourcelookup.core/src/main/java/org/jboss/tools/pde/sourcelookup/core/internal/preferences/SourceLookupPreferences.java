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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.jboss.tools.pde.sourcelookup.core.internal.CoreActivator;

public class SourceLookupPreferences {

  public static final String ENABLED_KEY = CoreActivator.PLUGIN_ID + ".enabled";

  public static final String REATTACH_SOURCES_ON_STARTUP_KEY = CoreActivator.PLUGIN_ID + ".reattachSourcesOnStartup";

  public static final String DEFAULT_SOURCES_DIRECTORY_KEY = CoreActivator.PLUGIN_ID + ".defaultSourceDirectory";

  public static final String DEFAULT_SOURCES_DIRECTORY = Paths
      .get(System.getProperty("user.home"), ".eclipse", CoreActivator.PLUGIN_ID, "sources").toString();

  private static final Path P2_POOL_DIRECTORY = Paths.get(System.getProperty("user.home"), ".p2", "pool", "plugins");

  private static final SourceLookupPreferences INSTANCE = new SourceLookupPreferences();

  private SourceLookupPreferences() {
  }

  public static SourceLookupPreferences getInstance() {
    return INSTANCE;
  }

  public Path getDownloadedSourcesDirectory() {
    String path = Platform.getPreferencesService().getString(CoreActivator.PLUGIN_ID,
        SourceLookupPreferences.DEFAULT_SOURCES_DIRECTORY_KEY, DEFAULT_SOURCES_DIRECTORY, null);
    return Paths.get(path);
  }

  // For testing purposes only
  public void setDownloadedSourcesDirectory(Path sourcesDirectory) {
    String downloadedSourcesDirectory = (sourcesDirectory == null) ? DEFAULT_SOURCES_DIRECTORY
        : sourcesDirectory.toString();
    getPreferences().put(DEFAULT_SOURCES_DIRECTORY_KEY, downloadedSourcesDirectory);
  }

  IEclipsePreferences getPreferences() {
    return InstanceScope.INSTANCE.getNode(CoreActivator.PLUGIN_ID);
  }

  public Collection<Path> getCacheLocations() {
    return Arrays.asList(P2_POOL_DIRECTORY, getDownloadedSourcesDirectory());
  }
}

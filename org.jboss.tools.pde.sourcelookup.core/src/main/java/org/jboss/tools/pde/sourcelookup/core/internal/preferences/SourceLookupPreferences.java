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

import org.jboss.tools.pde.sourcelookup.core.internal.CoreActivator;

public class SourceLookupPreferences {

  public static final String REATTACH_SOURCES_ON_STARTUP_KEY = CoreActivator.PLUGIN_ID + ".reattachSourcesOnStartup";

  private static final Path DEFAULT_SOURCES_DIRECTORY = Paths.get(System.getProperty("user.home"), ".eclipse",
      CoreActivator.PLUGIN_ID, "sources");

  private static final Path P2_POOL_DIRECTORY = Paths.get(System.getProperty("user.home"), ".p2", "pool", "plugins");

  private static final SourceLookupPreferences INSTANCE = new SourceLookupPreferences();

  private Path downloadedSourcesDirectory;

  private SourceLookupPreferences() {
    downloadedSourcesDirectory = DEFAULT_SOURCES_DIRECTORY;
  }

  public static SourceLookupPreferences getInstance() {
    return INSTANCE;
  }

  public Path getDownloadedSourcesDirectory() {
    return downloadedSourcesDirectory;
  }

  // For testing purposes only
  public void setDownloadedSourcesDirectory(Path sourcesDirectory) {
    downloadedSourcesDirectory = (sourcesDirectory == null) ? DEFAULT_SOURCES_DIRECTORY : sourcesDirectory;
  }

  public Collection<Path> getCacheLocations() {
    return Arrays.asList(P2_POOL_DIRECTORY, downloadedSourcesDirectory);
  }
}

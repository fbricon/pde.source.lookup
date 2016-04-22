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

import org.jboss.tools.pde.sourcelookup.core.internal.CoreActivator;

public class SourceLookupPreferences {

	private static final SourceLookupPreferences INSTANCE = new SourceLookupPreferences();

	private static final Path DEFAULT_SOURCES_DIRECTORY = Paths.get(System.getProperty("user.home"), ".eclipse",
			CoreActivator.PLUGIN_ID, "sources");

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
}

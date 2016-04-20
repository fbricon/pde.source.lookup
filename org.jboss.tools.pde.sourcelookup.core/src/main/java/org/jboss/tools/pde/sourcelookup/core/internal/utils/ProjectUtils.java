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

package org.jboss.tools.pde.sourcelookup.core.internal.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Fred Bricon
 *
 */
public class ProjectUtils {

	private ProjectUtils() {
	}

	public static boolean isPluginProject(IProject project) {
		return hasNature(project, "org.eclipse.pde.PluginNature");
	}

	public static boolean hasNature(IProject project, String natureId) {
		if (project == null) {
			return false;
		}
		try {
			return project.hasNature(natureId);
		} catch (CoreException O_o) {
		}
		return false;
	}
}

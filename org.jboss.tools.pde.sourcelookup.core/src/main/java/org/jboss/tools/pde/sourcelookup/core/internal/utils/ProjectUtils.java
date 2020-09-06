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
    return project != null && project.isAccessible() && hasNature(project, "org.eclipse.pde.PluginNature")
        && hasNature(project, "org.eclipse.jdt.core.javanature");
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

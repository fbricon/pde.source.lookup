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

package org.jboss.tools.pde.sourcelookup.ui.internal.utils;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.ui.packageview.ClassPathContainer;
import org.jboss.tools.pde.sourcelookup.core.internal.utils.ClasspathUtils;

/**
 * @author Fred Bricon
 */
@SuppressWarnings("restriction")
public class UIUtils {

  private UIUtils() {
    // prevent instantiation
  }

  public static boolean isPluginContainer(Object o) {
    IClasspathEntry cpe = null;
    if (o instanceof ClassPathContainer) {
      cpe = ((ClassPathContainer) o).getClasspathEntry();
    } else if (o instanceof IClasspathEntry) {
      cpe = (IClasspathEntry) o;
    }
    return ClasspathUtils.isPluginContainer(cpe);
  }
}

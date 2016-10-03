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

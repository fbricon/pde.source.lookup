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

package org.jboss.tools.pde.sourcelookup.ui.internal.actions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.packageview.ClassPathContainer;

/**
 * @author Fred Bricon
 *
 */
@SuppressWarnings("restriction")
public class PluginSourceLookupPropertyTester extends PropertyTester {

  public static final String IS_PLUGIN_CONTAINER = "isPluginContainer";

  public static final String IS_FROM_PLUGIN_CONTAINER = "isFromPluginContainer";

  public static boolean isBinaryFragment(IPackageFragmentRoot pfr) {
    try {
      return pfr != null && pfr.getKind() == IPackageFragmentRoot.K_BINARY;
    } catch (JavaModelException e) {
      return false;
    }
  }

  public static boolean isPluginContainer(Object o) {
    if (o instanceof ClassPathContainer) {
      ClassPathContainer cpc = (ClassPathContainer) o;
      IClasspathEntry cpe = cpc.getClasspathEntry();
      return cpe != null && "org.eclipse.pde.core.requiredPlugins".equals(cpe.getPath().toString());
    }
    return false;
  }

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    switch (property) {
    case IS_PLUGIN_CONTAINER:
      return isPluginContainer(receiver);
    case IS_FROM_PLUGIN_CONTAINER:
    }
    return false;
  }
}

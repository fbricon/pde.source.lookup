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

import static org.jboss.tools.pde.sourcelookup.ui.internal.utils.UIUtils.isPluginContainer;

import org.eclipse.core.expressions.PropertyTester;;

/**
 * @author Fred Bricon
 */
public class PluginSourceLookupPropertyTester extends PropertyTester {

  public static final String IS_PLUGIN_CONTAINER = "isPluginContainer";

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    boolean result = IS_PLUGIN_CONTAINER.equals(property) && isPluginContainer(receiver);
    return result;
  }
}

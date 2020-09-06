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
    return IS_PLUGIN_CONTAINER.equals(property) && isPluginContainer(receiver);
  }
}

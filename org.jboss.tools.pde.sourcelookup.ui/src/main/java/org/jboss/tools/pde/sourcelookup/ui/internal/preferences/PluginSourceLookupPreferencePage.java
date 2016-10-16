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

package org.jboss.tools.pde.sourcelookup.ui.internal.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jboss.tools.pde.sourcelookup.core.internal.preferences.SourceLookupPreferences;
import org.jboss.tools.pde.sourcelookup.ui.internal.UIActivator;

/**
 * @author Fred Bricon
 */
public class PluginSourceLookupPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  public PluginSourceLookupPreferencePage() {
    super();
    setPreferenceStore(UIActivator.getInstance().getPreferenceStore());
  }

  @Override
  public void init(IWorkbench workbench) {
  }

  @Override
  protected void createFieldEditors() {
    BooleanFieldEditor reattachSourceOnStartup = new BooleanFieldEditor(
        SourceLookupPreferences.REATTACH_SOURCES_ON_STARTUP_KEY, "Re-Attach bundle sources on workbench startup",
        getFieldEditorParent());
    addField(reattachSourceOnStartup);
  }

}

/*******************************************************************************
 * Copyright (c) 2010 Intel Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel - initial API and implementation
 *******************************************************************************/
package org.yocto.sdk.ide.actions;

import org.eclipse.cdt.internal.autotools.ui.actions.InvokeAction;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.yocto.sdk.ide.YoctoSDKPlugin;


@SuppressWarnings("restriction")
public class ReconfigYoctoAction extends InvokeAction {
	
	public void run(IAction action) {
		IContainer container = getSelectedContainer();
		if (container == null)
			return;

		IProject project = container.getProject();

		PreferenceDialog dialog =
				PreferencesUtil.createPropertyDialogOn(YoctoSDKPlugin.getActiveWorkbenchShell(), 
														project,
														"org.yocto.sdk.ide.page",
														null,
														null);
		dialog.open();
	}

	public void dispose() {

	}
}

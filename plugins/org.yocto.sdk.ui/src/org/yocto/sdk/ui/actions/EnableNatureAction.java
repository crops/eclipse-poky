/*******************************************************************************
 * Copyright (c) 2018 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.yocto.sdk.ui.actions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.yocto.sdk.core.YoctoProjectNature;
import org.yocto.sdk.core.preference.YoctoProjectProjectPreferences;

public class EnableNatureAction implements IObjectActionDelegate, IExecutableExtension {

	static final String YOCTO_PROJECT_PROPERTY_PAGE_ID = "org.yocto.sdk.docker.ui.YoctoProjectPropertyPage"; //$NON-NLS-1$

	private ISelection selection;

	public EnableNatureAction() {
		// Nothing to do
	}

	@Override
	public void run(IAction action) {

		if (selection instanceof IStructuredSelection) {

			IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			for (Iterator<?> it = structuredSelection.iterator(); it.hasNext();) {

				Object element = it.next();
				IProject project = null;

				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = ((IAdaptable) element).getAdapter(IProject.class);
				}

				if (project != null) {
					try {
						IProjectDescription description = project.getDescription();
						Set<String> updatedNatureIds = new HashSet<String>();

						for (String existingNatureId : description.getNatureIds()) {
							updatedNatureIds.add(existingNatureId);
						}

						updatedNatureIds.add(YoctoProjectNature.NATURE_ID);
						description.setNatureIds(updatedNatureIds.toArray(new String[] {}));
						project.setDescription(description, new NullProgressMonitor());

					} catch (CoreException e) {
						throw new RuntimeException(String.format(Messages.EnableNatureAction_EnableNatureFailed,
								YoctoProjectNature.NATURE_ID), e);
					}

					YoctoProjectProjectPreferences.createProjectPreferences(project);

					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(shell, project,
							YOCTO_PROJECT_PROPERTY_PAGE_ID, new String[] { YOCTO_PROJECT_PROPERTY_PAGE_ID }, null);

					dialog.open();

					// TODO: validate to make sure the profile preference is valid
				}
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		// TODO: convert/switch to Yocto toolchain or builder? This may require
		// per-project type EnableNatureAction

		// TODO: Initialize project preference to select default profile

	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// Nothing to do

	}

}

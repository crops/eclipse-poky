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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.yocto.sdk.core.YoctoProjectNature;

public class DisableNatureAction implements IObjectActionDelegate, IExecutableExtension {

	private ISelection selection;

	public DisableNatureAction() {
		// Nothing to do
	}

	@Override
	public void run(IAction action) {

		if(selection instanceof IStructuredSelection) {

			IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			for(Iterator<?> it = structuredSelection.iterator(); it.hasNext();) {

				Object element = it.next();
				IProject project = null;

				if(element instanceof IProject) {
					project = (IProject) element;
				} else if(element instanceof IAdaptable) {
					project = ((IAdaptable) element).getAdapter(IProject.class);
				}

				if(project != null) {
					try {
						IProjectDescription description = project.getDescription();
						Set<String> preservedNatureIds = new HashSet<String>();

						for (String existingNatureId : description.getNatureIds()) {

							if (!YoctoProjectNature.NATURE_ID.equals(existingNatureId)) {
								preservedNatureIds.add(existingNatureId);
							}

							description.setNatureIds(preservedNatureIds.toArray(new String[]{}));
							project.setDescription(description, new NullProgressMonitor());
						}

					} catch (CoreException e) {
						throw new RuntimeException(String.format(Messages.DisableNatureAction_DisableNatureFailed, YoctoProjectNature.NATURE_ID), e);
					}
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
		// Nothing to do

	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// Nothing to do

	}

}

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

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
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

@SuppressWarnings("restriction")
public class EnableNatureAction implements IObjectActionDelegate, IExecutableExtension {

	static final String YOCTO_PROJECT_PROPERTY_PAGE_ID = "org.yocto.sdk.docker.ui.YoctoProjectPropertyPage"; //$NON-NLS-1$

	private ISelection selection;

	private String toolchainId;

	public EnableNatureAction() {
		this(null);
	}

	public EnableNatureAction(String toolchainId) {
		this.toolchainId = toolchainId;
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
						updateProjectDescription(project);
						updateCProjectDescription(project);
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

	protected void updateProjectDescription(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		Set<String> updatedNatureIds = new HashSet<String>();

		for (String existingNatureId : description.getNatureIds()) {
			updatedNatureIds.add(existingNatureId);
		}

		updatedNatureIds.add(YoctoProjectNature.NATURE_ID);
		description.setNatureIds(updatedNatureIds.toArray(new String[] {}));
		project.setDescription(description, new NullProgressMonitor());
	}

	protected IToolChain getToolChain() {
		IToolChain[] toolChains = ManagedBuildManager.getRealToolChains();
		for (IToolChain toolChain : toolChains) {
			if (toolChain.isAbstract() || toolChain.isSystemObject())
				continue;
			if (toolChain.getId().equals(this.toolchainId))
				return toolChain;
		}

		return null;
	}

	protected void updateCProjectDescription(IProject project) throws CoreException {

		IToolChain toolchain = getToolChain();

		if (toolchain == null) {
			return;
		}

		ICProjectDescriptionManager cProjectDescriptionManager = CoreModel.getDefault().getProjectDescriptionManager();
		ICProjectDescription cProjectDescription = cProjectDescriptionManager.createProjectDescription(project, true);
		ManagedProject managedProject = new ManagedProject(cProjectDescription);

		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
		info.setManagedProject(managedProject);

		String toolchainId = (toolchain == null) ? "0" : toolchain.getId(); //$NON-NLS-1$

		// Create new configuration with the desired toolchain
		// TODO: ideally we want to be able to inherit/extend existing configuration if
		// exists...
		Configuration cfg = new Configuration(managedProject, (ToolChain) toolchain,
				ManagedBuildManager.calculateChildId(toolchainId, null), "Yocto"); //$NON-NLS-1$
		IBuilder builder = cfg.getEditableBuilder();

		// Turn on C/C++ Build -> Makefile generation -> Generate Makefiles
		// automatically. This is needed so that the cmake builder will be used to
		// generate Makefiles
		builder.setManagedBuildOn(true);

		CConfigurationData cCfgData = cfg.getConfigurationData();
		ICConfigurationDescription cCfgDescription = cProjectDescription
				.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, cCfgData);
		cProjectDescription.setActiveConfiguration(cCfgDescription);
		cProjectDescriptionManager.setProjectDescription(project, cProjectDescription);
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

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
package org.yocto.sdk.ide.preferences;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.yocto.sdk.ide.YoctoGeneralException;
import org.yocto.sdk.ide.YoctoProfileElement;
import org.yocto.sdk.ide.YoctoProfileSetting;
import org.yocto.sdk.ide.YoctoSDKMessages;
import org.yocto.sdk.ide.YoctoSDKPlugin;
import org.yocto.sdk.ide.YoctoSDKProjectNature;
import org.yocto.sdk.ide.YoctoSDKUtils;
import org.yocto.sdk.ide.YoctoSDKUtils.SDKCheckRequestFrom;
import org.yocto.sdk.ide.YoctoUIElement;
import org.yocto.sdk.ide.YoctoUISetting;

public class YoctoSDKPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String NEW_DIALOG_TITLE = "Preferences.Profile.New.Dialog.Title";
	private static final String NEW_DIALOG_MESSAGE = "Preferences.Profile.New.Dialog.Message";

	private YoctoProfileSetting yoctoProfileSetting;
	private YoctoUISetting yoctoUISetting;

	public YoctoSDKPreferencePage() {
		//super(GRID);
		IPreferenceStore defaultStore = YoctoSDKPlugin.getDefault().getPreferenceStore();
		String profiles = defaultStore.getString(PreferenceConstants.PROFILES);
		String selectedProfile = defaultStore.getString(PreferenceConstants.SELECTED_PROFILE);

		if (profiles.isEmpty()) {
			profiles = defaultStore.getDefaultString(PreferenceConstants.PROFILES);
			selectedProfile = defaultStore.getDefaultString(PreferenceConstants.SELECTED_PROFILE);
		}

		setPreferenceStore(YoctoSDKPlugin.getProfilePreferenceStore(selectedProfile));
		//setDescription(YoctoSDKMessages.getString(PREFERENCES_Yocto_CONFIG));
		YoctoUIElement elem = YoctoSDKUtils.getElemFromStore(getPreferenceStore());
		this.yoctoUISetting = new YoctoUISetting(elem);

		YoctoProfileElement profileElement = new YoctoProfileElement(profiles, selectedProfile);
		this.yoctoProfileSetting = new YoctoProfileSetting(profileElement, this);
	}

	/*
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);
		final Composite result= new Composite(parent, SWT.NONE);

		yoctoProfileSetting.createComposite(result);

		try {
			yoctoUISetting.createComposite(result);
			yoctoUISetting.validateInput(SDKCheckRequestFrom.Preferences, false);
			Dialog.applyDialogFont(result);
			return result;
		} catch (YoctoGeneralException e) {
			System.out.println("Have you ever set Yocto Project Reference before?");
			System.out.println(e.getMessage());
			return result;
		}
	}

	/*
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		try {
			yoctoUISetting.validateInput(SDKCheckRequestFrom.Preferences, true);

			YoctoUIElement elem = yoctoUISetting.getCurrentInput();
			YoctoSDKUtils.saveElemToStore(elem, getPreferenceStore());

			YoctoProfileElement profileElement = yoctoProfileSetting.getCurrentInput();
			YoctoSDKUtils.saveProfilesToDefaultStore(profileElement);

			updateAffectedProjects(profileElement.getSelectedProfile(), elem);

			return super.performOk();
		} catch (YoctoGeneralException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			return false;
		}
	}

	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		YoctoUIElement defaultElement = YoctoSDKUtils.getDefaultElemFromDefaultStore();
		yoctoUISetting.setCurrentInput(defaultElement);
		super.performDefaults();
	}

	public void performSaveAs() {
		YoctoProfileElement profileElement = yoctoProfileSetting.getCurrentInput();
		YoctoUIElement uiElement = yoctoUISetting.getCurrentInput();

		try {
			yoctoUISetting.validateInput(SDKCheckRequestFrom.Preferences, true);
		} catch (YoctoGeneralException e) {
			// just abort saving, validateInput will show an error dialog
			return;
		}

		InputDialog profileNameDialog =
							new InputDialog(null,
											YoctoSDKMessages.getString(NEW_DIALOG_TITLE),
											YoctoSDKMessages.getString(NEW_DIALOG_MESSAGE),
											null,
											new ProfileNameInputValidator(profileElement));

		int returnCode = profileNameDialog.open();
		if (returnCode == IDialogConstants.CANCEL_ID) {
			return;
		}

		profileElement.addProfile(profileNameDialog.getValue());
		yoctoProfileSetting.addProfile(profileNameDialog.getValue());

		yoctoUISetting.setCurrentInput(uiElement);
		performOk();
	}

	public void switchProfile(String selectedProfile) {
		setPreferenceStore(YoctoSDKPlugin.getProfilePreferenceStore(selectedProfile));
		YoctoUIElement profileElement = YoctoSDKUtils.getElemFromStore(getPreferenceStore());
		yoctoUISetting.setCurrentInput(profileElement);
	}

	public void renameProfile(String oldProfileName, String newProfileName) {
		YoctoUIElement oldProfileElement = YoctoSDKUtils.getElemFromStore(YoctoSDKPlugin.getProfilePreferenceStore(oldProfileName));
		YoctoSDKUtils.saveElemToStore(oldProfileElement, YoctoSDKPlugin.getProfilePreferenceStore(newProfileName));

		renameProfileInAffectedProjects(oldProfileName, newProfileName);
	}

	public void deleteProfile(String selectedProfile)
	{
		resetProfileInAffectedProjects(selectedProfile);
	}

	private void resetProfileInAffectedProjects(String usedProfile)
	{
		HashSet<IProject> yoctoProjects = getAffectedProjects(usedProfile);
		YoctoProfileElement profileElement = YoctoSDKUtils.getProfilesFromDefaultStore();
		profileElement.setSelectedProfile(PreferenceConstants.STANDARD_PROFILE_NAME);

		for (IProject project : yoctoProjects)
		{
			YoctoSDKUtils.saveProfilesToProjectPreferences(profileElement, project);
			YoctoUIElement elem = YoctoSDKUtils.getElemFromStore(
											YoctoSDKPlugin.getProfilePreferenceStore(PreferenceConstants.STANDARD_PROFILE_NAME));
			YoctoSDKUtils.saveElemToProjectEnv(elem, project);
		}
	}

	private void renameProfileInAffectedProjects(String oldProfileName, String newProfileName) {
		HashSet<IProject> yoctoProjects = getAffectedProjects(oldProfileName);
		YoctoProfileElement profileElement = YoctoSDKUtils.getProfilesFromDefaultStore();
		profileElement.setSelectedProfile(newProfileName);

		for (IProject project : yoctoProjects)
		{
			YoctoSDKUtils.saveProfilesToProjectPreferences(profileElement, project);
		}
	}

	private void updateAffectedProjects(String usedProfile, YoctoUIElement elem) {
		HashSet<IProject> yoctoProjects = getAffectedProjects(usedProfile);

		for (IProject project : yoctoProjects)
		{
			YoctoSDKUtils.saveElemToProjectEnv(elem, project);
		}
	}

	private HashSet<IProject> getAffectedProjects(String usedProfile)
	{
		HashSet<IProject> yoctoProjects = new HashSet<IProject>();

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		for (IProject project : projects)
		{
			try
			{
				if (!project.hasNature(YoctoSDKProjectNature.YoctoSDK_NATURE_ID)) {
					continue;
				}
			} catch (CoreException e)
			{
				// project is closed or does not exist so don't update
				continue;
			}

			if (!projectUsesProfile(project, usedProfile)) {
				continue;
			}

			yoctoProjects.add(project);
		}
		return yoctoProjects;
	}

	private boolean projectUsesProfile(IProject project, String profile)
	{
		YoctoProfileElement profileElement = YoctoSDKUtils.getProfilesFromProjectPreferences(project);

		if (!profileElement.getSelectedProfile().equals(profile)) {
			return false;
		}

		return true;
	}
}

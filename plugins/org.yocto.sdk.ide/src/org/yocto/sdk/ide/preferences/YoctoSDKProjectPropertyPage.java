/*******************************************************************************
 * Copyright (c) 2012 BMW Car IT GmbH.
 * Copyright (c) 2010 Intel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * BMW Car IT GmbH - initial implementation
 * Intel - initial API implementation (copied from YoctoSDKPreferencePage)
 *******************************************************************************/
package org.yocto.sdk.ide.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.yocto.sdk.ide.YoctoProfileElement;
import org.yocto.sdk.ide.YoctoProfileSetting;
import org.yocto.sdk.ide.YoctoProjectSpecificSetting;
import org.yocto.sdk.ide.YoctoSDKChecker;
import org.yocto.sdk.ide.YoctoSDKChecker.SDKCheckRequestFrom;
import org.yocto.sdk.ide.YoctoSDKChecker.SDKCheckResults;
import org.yocto.sdk.ide.YoctoSDKMessages;
import org.yocto.sdk.ide.YoctoSDKPlugin;
import org.yocto.sdk.ide.YoctoUIElement;
import org.yocto.sdk.ide.YoctoUISetting;
import org.yocto.sdk.ide.utils.ProjectPreferenceUtils;
import org.yocto.sdk.ide.utils.YoctoSDKUtils;

public class YoctoSDKProjectPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	private static final String REVALIDATION_MESSAGE = "Poky.SDK.Revalidation.Message";

	private YoctoProfileSetting yoctoProfileSetting;
	private YoctoProjectSpecificSetting yoctoProjectSpecificSetting;
	private YoctoUISetting yoctoUISetting;
	private IProject project = null;

	private Listener changeListener;

	public YoctoSDKProjectPropertyPage() {
		changeListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (getErrorMessage() != null) {
					setErrorMessage(null);
					setMessage(YoctoSDKMessages.getString(REVALIDATION_MESSAGE), INFORMATION);
				}
			}
		};
	}

	@Override
	protected Control createContents(Composite parent) {
		IProject project = getProject();

		YoctoProfileElement globalProfileElement= YoctoSDKUtils.getProfilesFromDefaultStore();
		YoctoProfileElement profileElement = ProjectPreferenceUtils.getProfiles(project);

		String selectedProfile = profileElement.getSelectedProfile();
		if (!globalProfileElement.contains(selectedProfile)) {
			selectedProfile = globalProfileElement.getSelectedProfile();
		}

		yoctoProfileSetting = new YoctoProfileSetting(
				new YoctoProfileElement(globalProfileElement.getProfilesAsString(), selectedProfile), this, false);
		boolean useProjectSpecificSetting = ProjectPreferenceUtils.getUseProjectSpecificOption(project);

		if (useProjectSpecificSetting) {
			yoctoUISetting = new YoctoUISetting(ProjectPreferenceUtils.getElem(project));
		} else {
			yoctoUISetting = new YoctoUISetting(YoctoSDKUtils.getElemFromStore(YoctoSDKPlugin.getProfilePreferenceStore(selectedProfile)));
		}

		yoctoProjectSpecificSetting = new YoctoProjectSpecificSetting(yoctoProfileSetting, yoctoUISetting, this);

		initializeDialogUnits(parent);
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		yoctoProfileSetting.createComposite(composite);
		yoctoProjectSpecificSetting.createComposite(composite);
		yoctoUISetting.createComposite(composite);

		if (useProjectSpecificSetting) {
			yoctoProfileSetting.setUIFormEnabledState(false);
			yoctoProjectSpecificSetting.setUseProjectSpecificSettings(true);
			yoctoUISetting.setUIFormEnabledState(true);

			SDKCheckResults result = yoctoUISetting.validateInput(SDKCheckRequestFrom.Preferences, false);
			if (result != SDKCheckResults.SDK_PASS) {
				setErrorMessage(result.getMessage());
			}
		} else {
			yoctoProfileSetting.setUIFormEnabledState(true);
			yoctoProjectSpecificSetting.setUseProjectSpecificSettings(false);
			yoctoUISetting.setUIFormEnabledState(false);
		}

		composite.addListener(SWT.Modify, changeListener);
		composite.addListener(SWT.Selection, changeListener);

		Dialog.applyDialogFont(composite);
		return composite;
	}

	private IProject getProject() {
		if (project != null) {
			return project;
		}

		IAdaptable adaptable = getElement();
		if (adaptable == null) {
			throw new IllegalStateException("Project can only be retrieved after properties page has been set up.");
		}

		project = (IProject) adaptable.getAdapter(IProject.class);
		return project;
	}

	/*
	 * @see PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		YoctoUIElement defaultElement = YoctoSDKUtils.getDefaultElemFromDefaultStore();
		yoctoUISetting.setCurrentInput(defaultElement);
		yoctoProjectSpecificSetting.setUseProjectSpecificSettings(true);
		super.performDefaults();
	}

	/*
	 * @see IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		clearMessages();

		IProject project = getProject();

		if (yoctoProjectSpecificSetting.isUsingProjectSpecificSettings()) {
			SDKCheckResults result = yoctoUISetting.validateInput(SDKCheckRequestFrom.Preferences, false);
			if (result != SDKCheckResults.SDK_PASS) {
				setErrorMessage(result.getMessage());
				return false;
			}

			ProjectPreferenceUtils.saveUseProjectSpecificOption(project, true);
			ProjectPreferenceUtils.saveProfiles(yoctoProfileSetting.getCurrentInput(), project);
			ProjectPreferenceUtils.saveElem(yoctoUISetting.getCurrentInput(), project);
		} else {
			ProjectPreferenceUtils.saveUseProjectSpecificOption(project, false);
			ProjectPreferenceUtils.saveProfiles(yoctoProfileSetting.getCurrentInput(), project);
		}

		ProjectPreferenceUtils.saveElemToProjectEnv(yoctoUISetting.getCurrentInput(), getProject());

		return super.performOk();
	}

	private void clearMessages() {
		setErrorMessage(null);
		setMessage(null);
		setTitle(getTitle());
	}

	public void switchProfile(String selectedProfile)
	{
		YoctoUIElement profileElement = YoctoSDKUtils.getElemFromStore(YoctoSDKPlugin.getProfilePreferenceStore(selectedProfile));
		yoctoUISetting.setCurrentInput(profileElement);
	}

	public void switchToProjectSpecificProfile()
	{
		YoctoUIElement profileElement = ProjectPreferenceUtils.getElem(getProject());
		SDKCheckResults result = YoctoSDKChecker.checkYoctoSDK(profileElement);

		if ((result != SDKCheckResults.SDK_PASS)) {
			/* Project specific profile has not yet been defined,
			 * leave settings from previously selected profile
			 */
			return;
		}

		yoctoUISetting.setCurrentInput(profileElement);
	}

	public void switchToSelectedProfile()
	{
		switchProfile(yoctoProfileSetting.getCurrentInput().getSelectedProfile());
	}
}

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
package org.yocto.sdk.docker.ui.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.yocto.sdk.core.preference.YoctoProjectProfilePreferences;
import org.yocto.sdk.core.preference.YoctoProjectProjectPreferences;
import org.yocto.sdk.core.preference.YoctoProjectWorkspacePreferences;
import org.yocto.sdk.docker.ui.editors.YoctoProjectProfileComposedEditor;
import org.yocto.sdk.ui.decorators.YoctoProjectProfileDecorator;
import org.yocto.ui.editors.BooleanFieldEditor2;
import org.yocto.ui.editors.ComboFieldEditor2;

/**
 * A per-project property page for configuring Yocto Project preferences.
 *
 * @author Intel Corporation
 *
 */
public class YoctoProjectPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

	Composite composite;
	BooleanFieldEditor2 useProjectSpecificBooleanFieldEditor;
	Composite profileComboComposite;

	ComboFieldEditor2 profileComboFieldEditor;

	YoctoProjectProfileComposedEditor composedEditor;

	public YoctoProjectPropertyPage() {

	}

	@Override
	protected Control createContents(Composite parent) {

		composite = createGridComposite(parent, 1);

		useProjectSpecificBooleanFieldEditor = new BooleanFieldEditor2(
				YoctoProjectProjectPreferences.USE_PROJECT_SPECIFIC_SETTINGS,
				Messages.YoctoProjectPropertyPage_UseProjectSpecificSettings, composite);
		useProjectSpecificBooleanFieldEditor.setPreferenceStore(getPreferenceStore());
		useProjectSpecificBooleanFieldEditor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (FieldEditor.VALUE.equals(event.getProperty())) {
					handleUseProjectSpecificSettingsButtonPressed();
					validate();
				}
			}
		});

		String[] profiles = YoctoProjectWorkspacePreferences.getWorkspaceProfiles();
		String[][] comboProfiles;

		if (profiles != null && profiles.length > 0) {
			comboProfiles = new String[profiles.length][2];

			for (int i = 0; i < profiles.length; i++) {
				comboProfiles[i][0] = profiles[i];
				comboProfiles[i][1] = profiles[i];
			}

		} else {
			comboProfiles = new String[][] {};
		}

		profileComboComposite = createGridComposite(composite, 2);
		profileComboFieldEditor = new ComboFieldEditor2(YoctoProjectProjectPreferences.PROJECT_PROFILE,
				Messages.YoctoProjectPropertyPage_CrossDevelopmentProfile, comboProfiles, profileComboComposite);
		profileComboFieldEditor.setPage(this);
		profileComboFieldEditor.setPreferenceStore(getPreferenceStore());
		profileComboFieldEditor.load();
		profileComboFieldEditor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (FieldEditor.VALUE.equals(event.getProperty())) {
					IPreferenceStore readOnlyProfilePreferenceStore = YoctoProjectProfilePreferences
							.createPreferenceStore(profileComboFieldEditor.getValue());
					composedEditor.load(readOnlyProfilePreferenceStore, false);
				}
			}
		});

		composedEditor = new YoctoProjectProfileComposedEditor(this, composite);
		composedEditor.addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				validate();
			}
		});

		useProjectSpecificBooleanFieldEditor.load();
		profileComboFieldEditor.load();

		handleUseProjectSpecificSettingsButtonPressed();

		return composite;
	}

//
//	private Group createGridGroup(Composite parent, String label, int columns) {
//		Group gridGroup = new Group(parent, SWT.NONE);
//		gridGroup.setText(label);
//		GridLayout layout = new GridLayout(columns, false);
//		layout.marginHeight = 0;
//		layout.marginWidth = 0;
//		gridGroup.setLayout(layout);
////		gridGroup.setBackground(new Color(Display.getDefault(), (int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
//		gridGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
//		return gridGroup;
//	}
//
	private Composite createGridComposite(Composite parent, int columns) {
		Composite gridComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(columns, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		gridComposite.setLayout(layout);
//		gridComposite.setBackground(new Color(Display.getDefault(), (int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
		gridComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		return gridComposite;
	}

	private void handleUseProjectSpecificSettingsButtonPressed() {

		boolean useProjectSpecificSettings = this.useProjectSpecificBooleanFieldEditor.getBooleanValue();

		IPreferenceStore projectPreferenceStore = getPreferenceStore();

		profileComboFieldEditor.setEnabled(!useProjectSpecificSettings, profileComboComposite);

		if (useProjectSpecificSettings) {
			composedEditor.reset();
			composedEditor.load(projectPreferenceStore, true);
		} else {
			profileComboFieldEditor.setPreferenceStore(YoctoProjectWorkspacePreferences.getWorkspacePreferenceStore());
			profileComboFieldEditor.load();
			profileComboFieldEditor.setPreferenceStore(getPreferenceStore());

			IPreferenceStore readOnlyProfilePreferenceStore = YoctoProjectProfilePreferences
					.createPreferenceStore(profileComboFieldEditor.getValue());
			composedEditor.reset();
			composedEditor.load(readOnlyProfilePreferenceStore, false);
		}
	}

	@Override
	protected void performDefaults() {

		super.performDefaults();

		useProjectSpecificBooleanFieldEditor.getCheckboxControl(composite).setSelection(false);
		handleUseProjectSpecificSettingsButtonPressed();
	}

	@Override
	public boolean performOk() {

		boolean status = super.performOk();

		useProjectSpecificBooleanFieldEditor.store();

		if (useProjectSpecificBooleanFieldEditor.getBooleanValue()) {
			composedEditor.store((IPersistentPreferenceStore) getPreferenceStore());
		} else {
			profileComboFieldEditor.store();
		}

		return status;
	}

	@Override
	public void setElement(IAdaptable element) {

		// Intercept setElement() calls so that the preference store can
		// be updated as soon as possible

		super.setElement(element);

		IPreferenceStore store = null;

		if (element != null) {
			IProject project = element.getAdapter(IProject.class);
			if (project != null) {
				store = YoctoProjectProjectPreferences.create(project).getPreferenceStore();
			}
		}

		setPreferenceStore(store);
	}

	@Override
	protected void performApply() {
		super.performApply();

		// Refresh label decorator just in case the information displayed needs to be
		// updated
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				PlatformUI.getWorkbench().getDecoratorManager().update(YoctoProjectProfileDecorator.ID);
			}
		});
	}

	String computeErrorMessage() {

		if (this.useProjectSpecificBooleanFieldEditor.getBooleanValue()) {
			if (composedEditor.isValid()) {
				return null;
			} else {
				return composedEditor.getErrorMessage();
			}
		} else {
			String profile = this.profileComboFieldEditor.getValue();

			if (profile == null || profile.length() == 0) {
				// TODO: Handle cases where profiles have been renamed but projects still
				// references the old names
				if (YoctoProjectWorkspacePreferences.getWorkspaceProfiles().length == 0) {
					return Messages.YoctoProjectPropertyPage_NoProfilesFound;
				} else {
					return Messages.YoctoProjectPropertyPage_NoProfileSelected;
				}
			} else {
				return null;
			}
		}
	}

	void validate() {

		String errorMessage = computeErrorMessage();

		if (errorMessage == null || errorMessage.length() == 0) {
			setValid(true);
			setMessage(null, IMessageProvider.NONE);
		} else {
			setValid(false);
			setMessage(errorMessage, IMessageProvider.ERROR);
		}
	}
}

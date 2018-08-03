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

import java.io.IOException;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.yocto.sdk.core.preference.YoctoProjectProfilePreferences;
import org.yocto.sdk.core.preference.YoctoProjectWorkspacePreferences;
import org.yocto.sdk.docker.ui.editors.YoctoProjectProfileComboFieldEditor;
import org.yocto.sdk.docker.ui.editors.YoctoProjectProfileComposedEditor;
import org.yocto.sdk.ui.decorators.YoctoProjectProfileDecorator;

/**
 * A workspace preference page which provides editors for creating/deleting/
 * renaming/customizing cross compilation profiles.
 *
 * Note that only the ProfileComboFieldEditor uses the workspace preference
 * store, whereas the composed editors (YoctoProjectComposedEditor) uses a
 * per-profile workspace-scoped preference store computed based on the choice of
 * profile in ProfileComboFieldEditor.
 *
 * @author Intel Corporation
 *
 */
public class YoctoProjectPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "org.yocto.sdk.docker.ui.YoctoProjectPreferencePage"; //$NON-NLS-1$

	IPersistentPreferenceStore profilePreferenceStore = null;
	YoctoProjectProfileComposedEditor composedEditor;

	YoctoProjectProfileComboFieldEditor profileComboFieldEditor;

	public YoctoProjectPreferencePage() {
		super();
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {

		Composite composite = createGridComposite(parent, 1);

		String[] profiles = YoctoProjectWorkspacePreferences.getWorkspaceProfiles();

		profileComboFieldEditor = new YoctoProjectProfileComboFieldEditor(YoctoProjectWorkspacePreferences.PROFILE,
				Messages.YoctoProjectPreferencePage_Profile, profiles,
				createGridGroup(composite, Messages.YoctoProjectPreferencePage_CrossDevelopmentProfile, 5));
		profileComboFieldEditor.setPreferenceStore(YoctoProjectWorkspacePreferences.getWorkspacePreferenceStore());
		profileComboFieldEditor.setPage(this);
		profileComboFieldEditor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {

				performApply();

				if (YoctoProjectProfileComboFieldEditor.NEW.equals(event.getProperty())) {

					// handle profile added
					String newProfile = (String) event.getNewValue();
					IPersistentPreferenceStore newProfilePreferenceStore = createProfilePreferenceStore(newProfile);
					composedEditor.reset();
					composedEditor.store(newProfilePreferenceStore);

					setProfilePreferenceStore(newProfilePreferenceStore);

				} else if (YoctoProjectProfileComboFieldEditor.REMOVE.equals(event.getProperty())) {

					// handle profile removed

					// TODO: disallow removing profiles in used? This can
					// prevented early by disabling the remove button,
					// however the reason the button is disabled might not
					// be obvious ...

					String removedProfile = (String) event.getOldValue();
					IPersistentPreferenceStore removedProfilePreferenceStore = createProfilePreferenceStore(
							removedProfile);
					// First we reset field editors to default values and store to
					// the profile, effectively erasing the preference store.
					composedEditor.reset();
					composedEditor.store(removedProfilePreferenceStore);

					IPersistentPreferenceStore selectedProfilePreferenceStore = createProfilePreferenceStore(
							YoctoProjectPreferencePage.this.profileComboFieldEditor.getSelectedProfile());
					composedEditor.load(selectedProfilePreferenceStore, true);

					setProfilePreferenceStore(selectedProfilePreferenceStore);

					// TODO: update projects using removed profile?

					refreshProfileDecorator();

				} else if (YoctoProjectProfileComboFieldEditor.RENAME.equals(event.getProperty())) {

					// handle profile renamed

					// TODO: disallow renaming profiles in used? This can be
					// prevented early by disabling the rename button,
					// however the reason the button is disabled might not
					// be obvious ...

					String oldProfile = (String) event.getOldValue();
					String newProfile = (String) event.getNewValue();

					IPersistentPreferenceStore oldProfilePreferenceStore = createProfilePreferenceStore(oldProfile);
					IPersistentPreferenceStore newProfilePreferenceStore = createProfilePreferenceStore(newProfile);

					// first we store field editors to new profile
					composedEditor.store(newProfilePreferenceStore);

					// then we reset field editors to default values and store to
					// the old profile, effectively erasing the preference store
					composedEditor.reset();
					composedEditor.store(oldProfilePreferenceStore);

//					// finally reload the editor with the new profile
//					this.profilePreferenceStore = addedProfilePreferenceStore;
					composedEditor.load(newProfilePreferenceStore, true);

					setProfilePreferenceStore(newProfilePreferenceStore);

					// TODO: update projects using renamed profile?

					refreshProfileDecorator();

				} else if (YoctoProjectProfileComboFieldEditor.SELECT.equals(event.getProperty())) {

					IPersistentPreferenceStore selectedProfilePreferenceStore = createProfilePreferenceStore(
							YoctoProjectPreferencePage.this.profileComboFieldEditor.getSelectedProfile());
					composedEditor.load(selectedProfilePreferenceStore, true);

					setProfilePreferenceStore(selectedProfilePreferenceStore);
				}

				profileComboFieldEditor.store();
				validate();
			}
		});

		composedEditor = new YoctoProjectProfileComposedEditor(this, composite);

		composedEditor.reset();
		profileComboFieldEditor.load();
		profilePreferenceStore = createProfilePreferenceStore(profileComboFieldEditor.getSelectedProfile());
		composedEditor.load(profilePreferenceStore, true);
		composedEditor.addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				validate();
			}
		});

		return composite;
	}

	private Group createGridGroup(Composite parent, String label, int columns) {
		Group gridGroup = new Group(parent, SWT.NONE);
		gridGroup.setText(label);
		GridLayout layout = new GridLayout(columns, false);
//		layout.marginHeight = 0;
//		layout.marginWidth = 0;
		gridGroup.setLayout(layout);
//		gridGroup.setBackground(new Color(Display.getDefault(), (int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
		gridGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		return gridGroup;
	}

	private Composite createGridComposite(Composite parent, int columns) {
		Composite gridComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(columns, false);
//		layout.marginHeight = 0;
//		layout.marginWidth = 0;
		gridComposite.setLayout(layout);
//		gridComposite.setBackground(new Color(Display.getDefault(), (int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
		gridComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		return gridComposite;
	}

	protected void setProfilePreferenceStore(IPersistentPreferenceStore profilePreferenceStore) {
		this.profilePreferenceStore = profilePreferenceStore;
	}

	@Override
	protected void performApply() {

		// Don't apply invalid preferences so that we don't have to deal with
		// invalid preferences when they are reused in projects

		if (isValid()) {

			super.performApply();

			// TODO: Store list of all profiles and the selected one using
			// ProfileComboFieldEditor?
			YoctoProjectWorkspacePreferences.setWorkspaceProfiles(this.profileComboFieldEditor.getProfiles());

			this.profileComboFieldEditor.store();

			composedEditor.store(this.profilePreferenceStore);

			if (this.profilePreferenceStore != null) {
				try {
					if (this.profilePreferenceStore.needsSaving())
						this.profilePreferenceStore.save();
				} catch (IOException e) {
					throw new RuntimeException(
							String.format(Messages.YoctoProjectPreferencePage_SavePreferenceStoreFailed,
									this.profilePreferenceStore.toString()),
							e);
				}
			}

			refreshProfileDecorator();
		}
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		composedEditor.reset();
		performApply();
	}

	IPersistentPreferenceStore createProfilePreferenceStore(String profile) {
		return YoctoProjectProfilePreferences.createPreferenceStore(profile);
	}

	String computeErrorMessage() {

		if (YoctoProjectWorkspacePreferences.getWorkspaceProfiles().length == 0)
			return Messages.YoctoProjectPreferencePage_NoProfilesFound;

		return composedEditor.getErrorMessage();
	}

	void validate() {

		String errorMessage = computeErrorMessage();

		if (errorMessage == null || errorMessage.length() == 0) {

			// This enables the apply button
			setValid(true);

			// Allow creating and renaming profiles when the preferences are
			// valid as we will silent do a performApply() to save the changes
			profileComboFieldEditor.setCanCreateNewProfile(true);
			profileComboFieldEditor.setCanRenameProfile(true);
			// TODO: Removing valid profile should always be allowed provided
			// it is not used by any projects
			profileComboFieldEditor.setCanDeleteProfile(true);

			setMessage(null, IMessageProvider.NONE);
		} else {

			// This disables the apply button
			setValid(false);

			// Don't allow creating or renaming profiles with invalid
			// preferences as the apply button has been disabled hence
			// unapplied changes could be lost
			profileComboFieldEditor.setCanCreateNewProfile(false);
			profileComboFieldEditor.setCanRenameProfile(false);
			// Removing invalid profile preference is always allowed
			// TODO: optionally sanity check to make sure it is not in use
			profileComboFieldEditor.setCanDeleteProfile(true);

			setMessage(errorMessage, IMessageProvider.ERROR);
		}
	}

	void refreshProfileDecorator() {
		// Refresh label decorator just in case the information displayed needs to be
		// updated
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				PlatformUI.getWorkbench().getDecoratorManager().update(YoctoProjectProfileDecorator.ID);
			}
		});
	}
}

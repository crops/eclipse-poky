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
package org.yocto.sdk.docker.ui.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.yocto.sdk.core.preference.YoctoProjectWorkspacePreferences;

public class YoctoProjectProfileComboFieldEditor extends FieldEditor {

	public static final String NEW = "add"; //$NON-NLS-1$
	public static final String REMOVE = "remove"; //$NON-NLS-1$
	public static final String RENAME = "rename"; //$NON-NLS-1$
	public static final String SELECT = "select"; //$NON-NLS-1$

	String[] profiles;

	Label label;
	Combo combo;
	Button newButton;
	Button removeButton;
	Button renameButton;
	int numControl;

	boolean canCreateNewProfile = true;
	boolean canRemoveProfile = true;
	boolean canRenameProfile = true;

	public YoctoProjectProfileComboFieldEditor(String fieldName, String labelText, String[] profiles, Composite parent) {
		this.profiles = profiles;
		init(fieldName, labelText);
		createControl(parent);
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {

		label = getLabelControl(parent);

		combo = new Combo(parent, SWT.READ_ONLY);
		combo.setItems(this.profiles);

		combo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				handleComboSelectionChange();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Nothing to do

			}
		});

		newButton = new Button(parent, SWT.NONE);
		newButton.setText("New..."); //$NON-NLS-1$
		newButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				handleNewButtonPressed();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Nothing to do

			}
		});
		removeButton = new Button(parent, SWT.NONE);
		removeButton.setText("Remove");	 //$NON-NLS-1$
		removeButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRemoveButtonPressed();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Nothing to do

			}
		});
		renameButton = new Button(parent, SWT.NONE);
		renameButton.setText("Rename..."); //$NON-NLS-1$
		renameButton.addSelectionListener(new SelectionListener( ) {

			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRenameButtonPressed();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Nothing to do

			}
		});


		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		combo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		newButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		removeButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		renameButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		refreshButtons();

	}

	@Override
	public int getNumberOfControls() {
		return 5;
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		// Unsupported

	}

	@Override
	protected void doLoad() {

		IPreferenceStore s = getPreferenceStore();

		if (s != null) {

			String profile = s.getString(getPreferenceName());

			int selectionIndex = -1;

			if (profile != null && profile.length() > 0) {
				for (int i = 0; i < this.combo.getItemCount(); i++) {
					if (this.combo.getItem(i).equals(profile)) {
						selectionIndex = i;
						break;
					}
				}

				if (selectionIndex != -1) {
					this.combo.select(selectionIndex);
				} else {
					this.combo.add(profile);
					this.combo.select(this.combo.getItemCount() - 1);
				}
			}

		}

		refreshButtons();
	}

	@Override
	protected void doLoadDefault() {
		// Nothing to do as the default values are no profile defined nor selected
	}

	@Override
	protected void doStore() {
		IPreferenceStore s = getPreferenceStore();

		if (s != null) {
			s.setValue(getPreferenceName(), this.combo.getText());
		}
	}

	void handleNewButtonPressed() {

		IInputValidator validator = new IInputValidator() {

			@Override
			public String isValid(String newText) {


				if (newText == null || newText.length() == 0)
					return Messages.YoctoProjectProfileComboFieldEditor_NeedNewProfileName;

				if (newText.contains(YoctoProjectWorkspacePreferences.PROFILE_SEPARATOR))
					return Messages.YoctoProjectProfileComboFieldEditor_NoIllegalCharacterInNewProfileName;

				for (int i = 0; i < YoctoProjectProfileComboFieldEditor.this.combo.getItemCount(); i++)
					if (YoctoProjectProfileComboFieldEditor.this.combo.getItem(i).equals(newText))
						return Messages.YoctoProjectProfileComboFieldEditor_NeedUniqueNewProfileName;

				return null;
			}
		};

		final String NO_INITIAL_VALUE = ""; //$NON-NLS-1$

		InputDialog dialog = new InputDialog(getPage().getShell(), Messages.YoctoProjectProfileComboFieldEditor_CreateNewProfile, Messages.YoctoProjectProfileComboFieldEditor_EnterNewProfileName, NO_INITIAL_VALUE, validator);

		if (Window.OK == dialog.open()) {

			String newProfile = dialog.getValue();

			List<String> updatedProfiles = new ArrayList<String>(Arrays.asList(this.combo.getItems()));
			updatedProfiles.add(newProfile);

			this.combo.setItems(updatedProfiles.toArray(new String[] {}));

			// the selection index will always be -1 immediately after
			// setItems() is invoked, but we don't have to fire a change event
			// using the selection index before setItems() is invoked as
			// there's nothing interesting about it
			int oldSelectionIndex = this.combo.getSelectionIndex();
			int newSelectionIndex = updatedProfiles.size() - 1;
			this.combo.select(newSelectionIndex);

			fireValueChanged(NEW, null, newProfile);
			fireValueChanged(SELECT, oldSelectionIndex, newSelectionIndex);

			refreshButtons();
		}
	}

	void handleRemoveButtonPressed() {

		String removedProfile = this.combo.getText();

		// Don't remove profile is it's in use by any projects
		List<String> projectNamesUsingProfile = YoctoProjectWorkspacePreferences.getProjectNamesByProfile(removedProfile);

		if (projectNamesUsingProfile.size() > 0) {
			MessageBox messageBox = new MessageBox(getPage().getShell(), SWT.ERROR | SWT.OK);
			messageBox.setMessage(String.format(Messages.YoctoProjectProfileComboFieldEditor_UnableToRemoveProfileInUse, removedProfile, String.join(System.lineSeparator(), projectNamesUsingProfile)));
			messageBox.open();
			return;
		}

		List<String> newValues = new ArrayList<String>(Arrays.asList(this.combo.getItems()));
		newValues.remove(this.combo.getSelectionIndex());

		this.combo.setItems(newValues.toArray(new String[] {}));

		// the selection index will always be -1 immediately after
		// setItems() is invoked, but we don't have to fire a change event
		// using the selection index before setItems() is invoked as
		// there's nothing interesting about it
		int oldSelectionIndex = this.combo.getSelectionIndex();
		int newSelectionIndex = newValues.size() > 0 ? 0 : -1;
		this.combo.select(newSelectionIndex);

		fireValueChanged(REMOVE, removedProfile, null);
		fireValueChanged(SELECT, oldSelectionIndex, newSelectionIndex);

		refreshButtons();
	}

	void handleRenameButtonPressed() {

		int selectionIndex = this.combo.getSelectionIndex();
		String oldProfile = this.combo.getText();

		List<String> projectNamesUsingProfile = YoctoProjectWorkspacePreferences.getProjectNamesByProfile(oldProfile);

		if (projectNamesUsingProfile.size() > 0) {
			MessageBox messageBox = new MessageBox(getPage().getShell(), SWT.ERROR | SWT.OK);
			messageBox.setMessage(String.format(Messages.YoctoProjectProfileComboFieldEditor_UnableToRenameProfileInUse, oldProfile, String.join(System.lineSeparator(), projectNamesUsingProfile)));
			messageBox.open();
			return;
		}

		IInputValidator validator = new IInputValidator() {

			@Override
			public String isValid(String newText) {
				if (newText == null || newText.length() == 0)
					return Messages.YoctoProjectProfileComboFieldEditor_NeedProfileNameForRenaming;

				if (newText.equals(oldProfile))
					return Messages.YoctoProjectProfileComboFieldEditor_NeedDifferentProfileNameForRenaming;

				for (int i = 0; i < YoctoProjectProfileComboFieldEditor.this.combo.getItemCount(); i++)
					if (YoctoProjectProfileComboFieldEditor.this.combo.getItem(i).equals(newText))
						return Messages.YoctoProjectProfileComboFieldEditor_NeedUniqueProfileNameForRenaming;

				return null;
			}
		};

		InputDialog renameProfileDialog = new InputDialog(getPage().getShell(), Messages.YoctoProjectProfileComboFieldEditor_RenameProfile, Messages.YoctoProjectProfileComboFieldEditor_EnterProfileNameForRenaming, oldProfile, validator);

		if (Window.OK == renameProfileDialog.open()) {

			String newProfile = renameProfileDialog.getValue();

			List<String> newValues = new ArrayList<String>(Arrays.asList(this.combo.getItems()));
			newValues.set(selectionIndex, newProfile);

			this.combo.setItems(newValues.toArray(new String[] {}));
			this.combo.select(selectionIndex);
			fireValueChanged(RENAME, oldProfile, newProfile);

			refreshButtons();
		}
	}

	void handleComboSelectionChange() {
		// Fire a bogus change event without modifying the value
		// TODO: figure out why SWT combos don't provide old and new values in change event
		fireValueChanged(SELECT, null, this.combo.getText());
		refreshButtons();
	}

	void refreshButtons() {
		this.newButton.setEnabled(this.canCreateNewProfile);
		this.removeButton.setEnabled(this.canRemoveProfile && this.combo.getSelectionIndex() != -1);
		this.renameButton.setEnabled(this.canRenameProfile && this.combo.getSelectionIndex() != -1);
	}

	@Override
	public void setEnabled(boolean enabled, Composite parent) {

		super.setEnabled(enabled, parent);

		this.label.setEnabled(enabled);
		this.combo.setEnabled(enabled);

		this.newButton.setEnabled(enabled && this.canCreateNewProfile);
		this.removeButton.setEnabled(enabled && this.canRemoveProfile && this.combo.getSelectionIndex() != -1);
		this.renameButton.setEnabled(enabled && this.canRenameProfile && this.combo.getSelectionIndex() != -1);
	}

	public String getSelectedProfile() {
		if (this.combo.getSelectionIndex() == -1)
			return null;

		return this.combo.getText();
	}

	public String[] getProfiles() {
		return this.combo.getItems();
	}

	public boolean isCanCreateNewProfile() {
		return canCreateNewProfile;
	}

	public void setCanCreateNewProfile(boolean canCreateNewProfile) {
		this.canCreateNewProfile = canCreateNewProfile;
		refreshButtons();
	}

	public boolean isCanDeleteProfile() {
		return canRemoveProfile;
	}

	public void setCanDeleteProfile(boolean canDeleteProfile) {
		this.canRemoveProfile = canDeleteProfile;
		refreshButtons();
	}

	public boolean isCanRenameProfile() {
		return canRenameProfile;
	}

	public void setCanRenameProfile(boolean canRenameProfile) {
		this.canRenameProfile = canRenameProfile;
		refreshButtons();
	}
}

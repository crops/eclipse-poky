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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.yocto.docker.ui.editors.ContainerDirectoryFieldEditor;
import org.yocto.docker.ui.editors.ContainerFileFieldEditor;
import org.yocto.sdk.core.YoctoProjectEnvironmentSetupScript;
import org.yocto.sdk.core.preference.YoctoProjectProfilePreferences;
import org.yocto.ui.editors.BooleanFieldEditor2;
import org.yocto.ui.editors.RadioFieldEditor;
import org.yocto.ui.editors.StringFieldEditor2;

/**
 * A composed editor specialized for editing Yocto Project profile preferences.
 *
 * Utility method for validating the editor values are also provided for
 * convenience.
 *
 * @author Intel Corporation
 *
 */
public class YoctoProjectProfileComposedEditor implements IPropertyChangeListener {

	IPersistentPreferenceStore profilePreferenceStore = null;

	boolean editable = false;

	// A map to remember the parent composite of a field editor,
	// as required by setEnabled(boolean, composite) methods
	Map<FieldEditor, Composite> editorCompositeMap = new HashMap<FieldEditor, Composite>();

	BooleanFieldEditor2 useContainerFieldEditor;

	RadioFieldEditor toolchainRadioFieldEditor;

	ContainerDirectoryFieldEditor sdkInstallationPathFieldEditor;
	ContainerDirectoryFieldEditor buildDirectoryPathFieldEditor;

	ContainerDirectoryFieldEditor sysrootPathFieldEditor;

	RadioFieldEditor targetRadioFieldEditor;
	ContainerFileFieldEditor qemubootconfFilePathFieldEditor;
	ContainerFileFieldEditor kernelImagePathFieldEditor;
	StringFieldEditor2 runqemuArgumentsStringFieldEditor;

	IPropertyChangeListener listener;

	public YoctoProjectProfileComposedEditor(DialogPage page, Composite parent) {

		Composite composite = createGridComposite(parent, 1);

		useContainerFieldEditor = new BooleanFieldEditor2(YoctoProjectProfilePreferences.USE_CONTAINER,
				Messages.YoctoProjectProfileComposedEditor_BuildAndLaunchWithContainer, composite);
		initializeEditor(useContainerFieldEditor, page, composite);
		useContainerFieldEditor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {

				YoctoProjectProfileComposedEditor.this.propertyChange(event);

				if (FieldEditor.VALUE.equals(event.getProperty())) {
					refreshEditorMode();

					// Reset the editors whenever container mode is toggled as
					// there is no guarantee that the paths will still be valid
					sdkInstallationPathFieldEditor.setValue(""); //$NON-NLS-1$
					buildDirectoryPathFieldEditor.setValue(""); //$NON-NLS-1$
					sysrootPathFieldEditor.setValue(""); //$NON-NLS-1$
					qemubootconfFilePathFieldEditor.setValue(""); //$NON-NLS-1$
					kernelImagePathFieldEditor.setValue(""); //$NON-NLS-1$
				}
			}
		});

		Composite sdkInstallationComposite = createGridComposite(composite, 3);
		sdkInstallationPathFieldEditor = new ContainerDirectoryFieldEditor(
				YoctoProjectProfilePreferences.SDK_INSTALLATION, "", sdkInstallationComposite); //$NON-NLS-1$
		initializeEditor(sdkInstallationPathFieldEditor, page, sdkInstallationComposite);
		sdkInstallationPathFieldEditor.setPropertyChangeListener(this);

		Composite buildDirectoryComposite = createGridComposite(composite, 3);
		buildDirectoryPathFieldEditor = new ContainerDirectoryFieldEditor(
				YoctoProjectProfilePreferences.BUILD_DIRECTORY, "", buildDirectoryComposite); //$NON-NLS-1$
		initializeEditor(buildDirectoryPathFieldEditor, page, buildDirectoryComposite);
		buildDirectoryPathFieldEditor.setPropertyChangeListener(this);

		Composite toolchainRadioComposite = createGridGroup(composite,
				Messages.YoctoProjectProfileComposedEditor_Toolchain, 1);
		String[][] toolchainRadioLabelValue = new String[][] {
				{ Messages.YoctoProjectProfileComposedEditor_SdkInstallation,
						YoctoProjectProfilePreferences.TOOLCHAIN_SDK_INSTALLATION },
				{ Messages.YoctoProjectProfileComposedEditor_BuildDirectory,
						YoctoProjectProfilePreferences.TOOLCHAIN_BUILD_DIRECTORY } };
		toolchainRadioFieldEditor = new RadioFieldEditor(YoctoProjectProfilePreferences.TOOLCHAIN, "", //$NON-NLS-1$
				toolchainRadioLabelValue, new Composite[] { sdkInstallationComposite, buildDirectoryComposite }, true,
				toolchainRadioComposite);
		initializeEditor(toolchainRadioFieldEditor, page, toolchainRadioComposite);
		toolchainRadioFieldEditor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {

				YoctoProjectProfileComposedEditor.this.propertyChange(event);

				if (RadioFieldEditor.PROPERTY_SELECTION_INDEX.equals(event.getProperty())) {
					refreshToolchainRadioGroupEditors();
				}
			}
		});

		Composite sysrootComposite = createGridComposite(composite, 3);
		sysrootPathFieldEditor = new ContainerDirectoryFieldEditor(YoctoProjectProfilePreferences.SYSROOT_LOCATION,
				Messages.YoctoProjectProfileComposedEditor_Sysroot, sysrootComposite);
		initializeEditor(sysrootPathFieldEditor, page, sysrootComposite);
		sysrootPathFieldEditor.setPropertyChangeListener(this);

		Composite qemuComposite = createGridComposite(composite, 1);
		final int indent = 25;

		Composite qemubootconfFileComposite = createGridComposite(qemuComposite, 3);
		qemubootconfFilePathFieldEditor = new ContainerFileFieldEditor(YoctoProjectProfilePreferences.QEMUBOOTCONF_FILE,
				Messages.YoctoProjectProfileComposedEditor_QemubootconfFile, qemubootconfFileComposite);
		initializeEditor(qemubootconfFilePathFieldEditor, page, qemubootconfFileComposite);
		qemubootconfFilePathFieldEditor.setPropertyChangeListener(this);
		GridDataFactory.fillDefaults().indent(indent, 0)
				.applyTo(qemubootconfFilePathFieldEditor.getLabelControl(qemubootconfFileComposite));

		Composite kernelImageComposite = createGridComposite(qemuComposite, 3);
		kernelImagePathFieldEditor = new ContainerFileFieldEditor(YoctoProjectProfilePreferences.KERNEL_IMAGE,
				Messages.YoctoProjectProfileComposedEditor_KernelImage, kernelImageComposite);
		initializeEditor(kernelImagePathFieldEditor, page, kernelImageComposite);
		kernelImagePathFieldEditor.setPropertyChangeListener(this);
		GridDataFactory.fillDefaults().indent(indent, 0)
				.applyTo(kernelImagePathFieldEditor.getLabelControl(kernelImageComposite));

		Composite runqemuArgumentsComposite = createGridComposite(qemuComposite, 2);
		runqemuArgumentsStringFieldEditor = new StringFieldEditor2(YoctoProjectProfilePreferences.RUNQEMU_ARGUMENTS,
				Messages.YoctoProjectProfileComposedEditor_RunqemuArguments, runqemuArgumentsComposite);
		initializeEditor(runqemuArgumentsStringFieldEditor, page, runqemuArgumentsComposite);
		runqemuArgumentsStringFieldEditor.setPropertyChangeListener(this);
		GridDataFactory.fillDefaults().indent(indent, 0)
				.applyTo(runqemuArgumentsStringFieldEditor.getLabelControl(runqemuArgumentsComposite));

		// external hardware composite is just an empty place holder
		Composite externalHardwareComposite = createGridComposite(composite, 3);

		String[][] targetRadioLabelValue = new String[][] {
				{ Messages.YoctoProjectProfileComposedEditor_QEMU, YoctoProjectProfilePreferences.TARGET_QEMU },
				{ Messages.YoctoProjectProfileComposedEditor_ExternalHardware,
						YoctoProjectProfilePreferences.TARGET_EXTERNAL_HARDWARE } };

		Composite targetRadioComposite = createGridGroup(composite, Messages.YoctoProjectProfileComposedEditor_Target,
				1);
		targetRadioFieldEditor = new RadioFieldEditor(YoctoProjectProfilePreferences.TARGET, "", targetRadioLabelValue, //$NON-NLS-1$
				new Composite[] { qemuComposite, externalHardwareComposite }, false, targetRadioComposite);
		initializeEditor(targetRadioFieldEditor, page, targetRadioComposite);
		targetRadioFieldEditor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {

				YoctoProjectProfileComposedEditor.this.propertyChange(event);

				if (RadioFieldEditor.PROPERTY_SELECTION_INDEX.equals(event.getProperty())) {
					refreshTargetRadioGroupEditors();
				}
			}
		});

		reset();

	}

	private Group createGridGroup(Composite parent, String label, int columns) {
		Group gridGroup = new Group(parent, SWT.NONE);
		gridGroup.setText(label);
		GridLayout layout = new GridLayout(columns, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		gridGroup.setLayout(layout);
//		gridGroup.setBackground(new Color(Display.getDefault(), (int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
		gridGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		return gridGroup;
	}

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

	private void initializeEditor(FieldEditor editor, DialogPage page, Composite composite) {
		// keep track of parent composite of editors as we'll need it
		// for enabling/disabling editor's controls via setEnabled(boolean, Composite)
		editorCompositeMap.put(editor, composite);
		editor.setPage(page);
		editor.setPreferenceStore(null);
		enableEditor(editor, false);
	}

	private void enableEditor(FieldEditor editor, boolean enabled) {
		boolean enableWithValidPreferenceStore = this.editable && enabled && editor.getPreferenceStore() != null;
		editor.setEnabled(enableWithValidPreferenceStore, editorCompositeMap.get(editor));
	}

	public void reset() {

		// TODO: implement using each editor's loadDefault() ?
		useContainerFieldEditor.getCheckboxControl(editorCompositeMap.get(useContainerFieldEditor)).setSelection(false);
		toolchainRadioFieldEditor.setSelectionIndex(0);
		sdkInstallationPathFieldEditor.setValue(""); //$NON-NLS-1$
		buildDirectoryPathFieldEditor.setValue(""); //$NON-NLS-1$
		sysrootPathFieldEditor.setValue(""); //$NON-NLS-1$
		targetRadioFieldEditor.setSelectionIndex(0);
		qemubootconfFilePathFieldEditor.setValue(""); //$NON-NLS-1$
		kernelImagePathFieldEditor.setValue(""); //$NON-NLS-1$
		runqemuArgumentsStringFieldEditor.setStringValue(""); //$NON-NLS-1$
	}

	public void load(IPreferenceStore preferenceStore, boolean editable) {

		this.editable = editable;

		for (FieldEditor fieldEditor : editorCompositeMap.keySet()) {
			fieldEditor.setPreferenceStore(preferenceStore);
			fieldEditor.load();
			enableEditor(fieldEditor, true);
		}

		refreshToolchainRadioGroupEditors();
		refreshTargetRadioGroupEditors();
		refreshEditorMode();
	}

	public void store(IPersistentPreferenceStore preferenceStore) {
		for (FieldEditor fieldEditor : editorCompositeMap.keySet()) {
			fieldEditor.setPreferenceStore(preferenceStore);
			fieldEditor.store();
		}
	}

	private void refreshToolchainRadioGroupEditors() {
		enableEditor(sdkInstallationPathFieldEditor, toolchainRadioFieldEditor.getSelectionIndex() == 0);
		enableEditor(buildDirectoryPathFieldEditor, toolchainRadioFieldEditor.getSelectionIndex() == 1);
	}

	private void refreshTargetRadioGroupEditors() {
		enableEditor(qemubootconfFilePathFieldEditor, targetRadioFieldEditor.getSelectionIndex() == 0);
		enableEditor(kernelImagePathFieldEditor, targetRadioFieldEditor.getSelectionIndex() == 0);
		enableEditor(runqemuArgumentsStringFieldEditor, targetRadioFieldEditor.getSelectionIndex() == 0);
	}

	private void refreshEditorMode() {
		boolean containerMode = useContainerFieldEditor.getBooleanValue();
		sdkInstallationPathFieldEditor.setContainerMode(containerMode);
		buildDirectoryPathFieldEditor.setContainerMode(containerMode);
		sysrootPathFieldEditor.setContainerMode(containerMode);
		qemubootconfFilePathFieldEditor.setContainerMode(containerMode);
		kernelImagePathFieldEditor.setContainerMode(containerMode);
	}

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		this.listener = listener;
	}

	public String getErrorMessage() {

		boolean useContainer = useContainerFieldEditor.getBooleanValue();

		String toolchain = (String) toolchainRadioFieldEditor.getData();

		if (YoctoProjectProfilePreferences.TOOLCHAIN_SDK_INSTALLATION.equals(toolchain)) {

			String sdkInstallationString = sdkInstallationPathFieldEditor.getValue();

			if (sdkInstallationString == null || sdkInstallationString.length() == 0)
				return Messages.YoctoProjectProfileComposedEditor_NeedSdkInstallation;

			if (!useContainer) {

				File sdkInstallation = new File(sdkInstallationString);

				if (YoctoProjectEnvironmentSetupScript.getEnvironmentSetupScript(sdkInstallation) == null)
					return Messages.YoctoProjectProfileComposedEditor_SdkInstallationMissingEnvSetupScript;
			}

		} else if (YoctoProjectProfilePreferences.TOOLCHAIN_BUILD_DIRECTORY.equals(toolchain)) {

			String buildDirectoryString = buildDirectoryPathFieldEditor.getValue();

			if (buildDirectoryString == null || buildDirectoryString.length() == 0)
				return Messages.YoctoProjectProfileComposedEditor_NeedBuildDirectory;

			if (!useContainer) {

				File buildDirectory = new File(buildDirectoryString);

				if (YoctoProjectEnvironmentSetupScript.getEnvironmentSetupScript(buildDirectory) == null)
					return Messages.YoctoProjectProfileComposedEditor_BuildDirectoryMissingEnvSetupScript;
			}

		} else {
			return Messages.YoctoProjectProfileComposedEditor_SelectToolchainMode;
		}

		String sysrootLocation = sysrootPathFieldEditor.getValue();

		if (sysrootLocation == null || sysrootLocation.length() == 0) {
			return Messages.YoctoProjectProfileComposedEditor_NeedSysroot;
		}

		if (!useContainer) {
			File sysrootDir = new File(sysrootLocation);

			if (!sysrootDir.exists())
				return Messages.YoctoProjectProfileComposedEditor_NoSuchSysroot;

			if (!sysrootDir.isDirectory())
				return Messages.YoctoProjectProfileComposedEditor_SysrootNotDirectory;
		}

		String target = (String) targetRadioFieldEditor.getData();

		if (YoctoProjectProfilePreferences.TARGET_QEMU.equals(target)) {

			String qemubootconf = qemubootconfFilePathFieldEditor.getValue();

			if (qemubootconf == null || qemubootconf.length() == 0) {
				return Messages.YoctoProjectProfileComposedEditor_NeedQemuconf;
			}

			if (!useContainer) {

				File qemubootconfFile = new File(qemubootconf);

				if (!qemubootconfFile.exists())
					return Messages.YoctoProjectProfileComposedEditor_NoSuchQemubootconf;

				if (!qemubootconfFile.isFile())
					return Messages.YoctoProjectProfileComposedEditor_QemubootconfNotFile;
			}

			String kernelImage = kernelImagePathFieldEditor.getValue();

			if (kernelImage == null || kernelImage.length() == 0) {
				return Messages.YoctoProjectProfileComposedEditor_NeedKernelImage;
			}

			if (!useContainer) {

				File kernelImageFile = new File(kernelImage);

				if (!kernelImageFile.exists())
					return Messages.YoctoProjectProfileComposedEditor_NoSuchKernelImage;

				if (!kernelImageFile.isFile())
					return Messages.YoctoProjectProfileComposedEditor_KernelImageNotFile;
			}

		} else if (YoctoProjectProfilePreferences.TARGET_EXTERNAL_HARDWARE.equals(target)) {
			// Nothing to validate
		} else {
			return Messages.YoctoProjectProfileComposedEditor_SelectTargetMode;
		}

		return null;
	}

	public boolean isValid() {
		String errorMessage = getErrorMessage();
		return (errorMessage == null || errorMessage.length() == 0);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {

		if (this.listener != null) {
			// TODO: this doesn't allow tracking which field editor triggered
			// the event, it's probably good enough for now ...
			this.listener.propertyChange(event);
		}
	}
}

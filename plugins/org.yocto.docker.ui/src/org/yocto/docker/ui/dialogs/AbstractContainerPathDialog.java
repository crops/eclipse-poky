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
package org.yocto.docker.ui.dialogs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.yocto.ui.editors.BooleanFieldEditor2;
import org.yocto.ui.editors.RadioFieldEditor;

public abstract class AbstractContainerPathDialog extends Dialog {

	public static final int NO_EXTERNAL_MOUNT = 0;
	public static final int BIND_MOUNT = 1;
	public static final int VOLUME_MOUNT = 2;

	String containerPath = ""; //$NON-NLS-1$
	int mountType = NO_EXTERNAL_MOUNT;
	boolean readOnly = false;

	Map<FieldEditor, Composite> fieldEditorCompositeMap = new HashMap<FieldEditor, Composite>();

	RadioFieldEditor mountTypeFieldEditor;
	FieldEditor pathFieldEditor;
	BooleanFieldEditor2 readOnlyFieldEditor;

	public AbstractContainerPathDialog(Shell parentShell) {
		super(parentShell);
	}

	abstract FieldEditor createEditor(Composite composite);

	@SuppressWarnings("nls")
	@Override
	protected Control createDialogArea(Composite parent) {

		super.createDialogArea(parent);

		Composite composite = createGridComposite(parent, 1);

		Composite containerPathComposite = createGridComposite(composite, 2);
		StringFieldEditor containerPathFieldEditor = new StringFieldEditor("containerPath", "Container path:",
				containerPathComposite);
		containerPathFieldEditor.setStringValue(containerPath);
		containerPathFieldEditor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (FieldEditor.VALUE.equals(event.getProperty())) {
					AbstractContainerPathDialog.this.containerPath = (String) event.getNewValue();
				}
			}
		});

		Group mountTypeComposite = createGridGroup(composite, "Mount type:", 1);

		Composite noneComposite = createGridComposite(composite, 1);

		Composite hostComposite = createGridComposite(composite, 1);

		pathFieldEditor = createEditor(hostComposite);

		readOnlyFieldEditor = new BooleanFieldEditor2("readOnly", "Read-only access", hostComposite);
		readOnlyFieldEditor.getCheckboxControl(hostComposite).setSelection(readOnly);
		GridDataFactory.fillDefaults().indent(25, 0).applyTo(readOnlyFieldEditor.getCheckboxControl(hostComposite));
		fieldEditorCompositeMap.put(readOnlyFieldEditor, hostComposite);
		readOnlyFieldEditor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (FieldEditor.VALUE.equals(event.getProperty())) {
					AbstractContainerPathDialog.this.readOnly = (boolean) event.getNewValue();
				}
			}
		});

		String[][] mountTypeLabelValue = new String[][] { { "No external mount", "none" },
				{ "Mount a host directory", "host" } };
		Composite[] mountTypeNestedComposite = new Composite[] { noneComposite, hostComposite };

		mountTypeFieldEditor = new RadioFieldEditor("mountType", "", mountTypeLabelValue, mountTypeNestedComposite,
				false, createGridComposite(mountTypeComposite, 1));
		mountTypeFieldEditor.setSelectionIndex(mountType);
		mountTypeFieldEditor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				AbstractContainerPathDialog.this.mountType = (int) event.getNewValue();
				refreshMountTypeRadioActivatedEditors();
			}
		});

		refreshMountTypeRadioActivatedEditors();

		return composite;
	}

	protected Composite createGridComposite(Composite parent, int columns) {
		Composite gridComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(columns, false);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		gridComposite.setLayout(layout);
//		gridComposite.setBackground(new Color(Display.getDefault(), (int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
		gridComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		return gridComposite;
	}

	protected Group createGridGroup(Composite parent, String label, int columns) {
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

	private void enableFieldEditor(FieldEditor editor, boolean enabled) {
		editor.setEnabled(enabled, fieldEditorCompositeMap.get(editor));
	}

	private void refreshMountTypeRadioActivatedEditors() {
		enableFieldEditor(pathFieldEditor, this.mountTypeFieldEditor.getSelectionIndex() == 1);
		enableFieldEditor(readOnlyFieldEditor, this.mountTypeFieldEditor.getSelectionIndex() == 1);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	public String getContainerPath() {
		return containerPath;
	}

	public void setContainerPath(String containerPath) {
		this.containerPath = containerPath;
	}

	public int getMountType() {
		return mountType;
	}

	public void setMountType(int mountType) {
		this.mountType = mountType;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

}

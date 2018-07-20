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
package org.yocto.ui.editors;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class RadioFieldEditor extends FieldEditor implements SelectionListener {

	public static final String PROPERTY_SELECTION_INDEX = "selectionIndex"; //$NON-NLS-1$

	String[][] labelValue;
	Button[] radioButtons;
	Composite[] controls;
	int selectionIndex = -1;

	public RadioFieldEditor(String name, String labelText, String[][] labelValue, Composite[] nestedComposites,
			boolean horizontal, Composite parent) {

		init(name, labelText);

		this.labelValue = labelValue;

		this.controls = nestedComposites;
		this.radioButtons = new Button[labelValue.length];

		for (int i = 0; i < labelValue.length; i++) {

			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout(horizontal ? 2 : 1, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
//			layout.marginLeft = 30;
			composite.setLayout(layout);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

			this.radioButtons[i] = new Button(composite, SWT.RADIO);
			this.radioButtons[i].setText(labelValue[i][0]);
			this.radioButtons[i].setData(labelValue[i][1]);
			this.radioButtons[i].addSelectionListener(this);

			if (this.controls[i] != null) {
				this.controls[i].setParent(composite);
			}
		}

		doFillIntoGrid(parent, 1);
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		// Unsupported

	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		for (int i = 0; i < this.radioButtons.length; i++) {
			this.radioButtons[i].setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true));
			if (this.controls[i] != null) {
				this.controls[i].setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
			}
		}
	}

	@Override
	protected void doLoad() {
		String selectedData = getPreferenceStore().getString(getPreferenceName());
		for (int i = 0; i < this.labelValue.length; i++) {
			boolean selected = selectedData.equals(this.radioButtons[i].getData());
			this.radioButtons[i].setSelection(selected);
			if (selected)
				this.selectionIndex = i;
		}
	}

	@Override
	protected void doLoadDefault() {
		this.radioButtons[0].setSelection(true);
		this.selectionIndex = 0;
	}

	@Override
	protected void doStore() {
		for (int i = 0; i < this.labelValue.length; i++) {
			if (this.radioButtons[i].getSelection()) {
				// There is an issue with writing string value to preference store will
				// always cause the preference store to be marked as dirty, so here we
				// workaround this by avoid writing the same string value.
				if (!getPreferenceStore().getString(getPreferenceName()).equals(this.radioButtons[i].getData()))
					getPreferenceStore().setValue(getPreferenceName(), (String) this.radioButtons[i].getData());
			}
		}
	}

	@Override
	public int getNumberOfControls() {
		return this.radioButtons.length;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {

		for (int i = 0; i < getNumberOfControls(); i++) {
			if (this.radioButtons[i] == e.getSource()) {
				int oldSelectionIndex = this.selectionIndex;
				this.selectionIndex = i;
				fireValueChanged(PROPERTY_SELECTION_INDEX, oldSelectionIndex, this.selectionIndex);
			} else {
				this.radioButtons[i].setSelection(false);
			}
		}
	}

	public int getSelectionIndex() {
		return this.selectionIndex;
	}

	public String getText() {
		if (this.selectionIndex == -1)
			return null;

		return this.radioButtons[this.selectionIndex].getText();
	}

	public Object getData() {
		if (this.selectionIndex == -1)
			return null;

		return this.radioButtons[this.selectionIndex].getData();
	}

	public void setSelectionIndex(int selectionIndex) {
		int oldSelectionIndex = this.selectionIndex;
		this.selectionIndex = selectionIndex;

		if (oldSelectionIndex != -1)
			this.radioButtons[oldSelectionIndex].setSelection(false);

		if (this.selectionIndex != -1)
			this.radioButtons[this.selectionIndex].setSelection(true);

		fireValueChanged(PROPERTY_SELECTION_INDEX, oldSelectionIndex, this.selectionIndex);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// Nothing to do

	}

	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		for (int i = 0; i < this.labelValue.length; i++) {
			this.radioButtons[i].setEnabled(enabled);
		}
	}

}

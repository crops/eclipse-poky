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
package org.yocto.docker.ui.editors;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public abstract class AbstractContainerPathFieldEditor extends FieldEditor {

	Label label;
	CLabel cLabel;
	Color cLabelDefaultForeground;
	Button editButton;
	boolean containerMode = false;

	final String CONTAINER_PATH_SEPARATOR = ":"; //$NON-NLS-1$
	final String CONTAINER_PATH_READONLY_FLAG = "ro"; //$NON-NLS-1$

	public AbstractContainerPathFieldEditor(String fieldName, String labelText, Composite parent) {
		super(fieldName, labelText, parent);
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		// Nothing to do

	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {

		label = getLabelControl(parent);

		cLabel = new CLabel(parent, SWT.BORDER);
		cLabelDefaultForeground = cLabel.getForeground();

		editButton = new Button(parent, SWT.NONE);
		editButton.setText("Edit..."); //$NON-NLS-1$
		editButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				handleEditButtonPressed();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Nothing to do

			}
		});

		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		cLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		editButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
	}

	@Override
	protected void doLoad() {
		cLabel.setText(getPreferenceStore().getString(getPreferenceName()));
	}

	@Override
	protected void doLoadDefault() {
		cLabel.setText(""); //$NON-NLS-1$
	}

	@Override
	protected void doStore() {
		getPreferenceStore().setValue(getPreferenceName(), cLabel.getText());
	}

	@Override
	public int getNumberOfControls() {
		return 3;
	}

	public String getValue() {
		return cLabel.getText();
	}

	public void setValue(String value) {
		String oldValue = cLabel.getText();

		if (!value.equals(oldValue)) {
			cLabel.setText(value);
			fireValueChanged(FieldEditor.VALUE, oldValue, value);
		}
	}

	public void setImage(Image image) {
		cLabel.setImage(image);
	}

	abstract void handleEditButtonPressed();

	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		label.setEnabled(enabled);
		cLabel.setEnabled(enabled);
		cLabel.setForeground(enabled ? cLabelDefaultForeground : Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		editButton.setEnabled(enabled);
	}

	public boolean isContainerMode() {
		return containerMode;
	}

	public void setContainerMode(boolean containerMode) {
		this.containerMode = containerMode;
	}

}

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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class ContainerFileDialog extends AbstractContainerPathDialog {

	String hostFile = ""; //$NON-NLS-1$

	public ContainerFileDialog(Shell parentShell) {
		super(parentShell);
	}

	@SuppressWarnings("nls")
	@Override
	FieldEditor createEditor(Composite hostComposite) {

		Composite fileComposite = createGridComposite(hostComposite, 3);
		FileFieldEditor fileFieldEditor = new FileFieldEditor("file", "File:", fileComposite);
		fileFieldEditor.setStringValue(hostFile);
		GridDataFactory.fillDefaults().indent(25, 0).applyTo(fileFieldEditor.getLabelControl(fileComposite));
		fieldEditorCompositeMap.put(fileFieldEditor, fileComposite);
		fileFieldEditor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (FieldEditor.VALUE.equals(event.getProperty())) {
					ContainerFileDialog.this.hostFile = (String) event.getNewValue();
				}
			}
		});

		return fileFieldEditor;
	}

	public String getHostDirectory() {
		return hostFile;
	}

	public void setHostDirectory(String hostDirectory) {
		this.hostFile = hostDirectory;
	}
}

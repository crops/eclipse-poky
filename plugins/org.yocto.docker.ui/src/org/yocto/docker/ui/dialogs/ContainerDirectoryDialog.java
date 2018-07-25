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
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class ContainerDirectoryDialog extends AbstractContainerPathDialog {

	String hostDirectory = ""; //$NON-NLS-1$

	public ContainerDirectoryDialog(Shell parentShell) {
		super(parentShell);
	}

	@SuppressWarnings("nls")
	@Override
	FieldEditor createEditor(Composite hostComposite) {

		Composite directoryComposite = createGridComposite(hostComposite, 3);
		DirectoryFieldEditor directoryFieldEditor = new DirectoryFieldEditor("directory", "Directory:", directoryComposite);
		directoryFieldEditor.setStringValue(hostDirectory);
		GridDataFactory.fillDefaults().indent(25, 0).applyTo(directoryFieldEditor.getLabelControl(directoryComposite));
		fieldEditorCompositeMap.put(directoryFieldEditor, directoryComposite);
		directoryFieldEditor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (FieldEditor.VALUE.equals(event.getProperty())) {
					ContainerDirectoryDialog.this.hostDirectory = (String) event.getNewValue();
				}
			}
		});

		return directoryFieldEditor;
	}

	public String getHostDirectory() {
		return hostDirectory;
	}

	public void setHostDirectory(String hostDirectory) {
		this.hostDirectory = hostDirectory;
	}

}

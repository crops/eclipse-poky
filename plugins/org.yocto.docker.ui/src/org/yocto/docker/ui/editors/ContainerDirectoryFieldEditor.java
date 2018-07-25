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

import java.io.File;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.yocto.docker.ui.dialogs.AbstractContainerPathDialog;
import org.yocto.docker.ui.dialogs.ContainerDirectoryDialog;

public class ContainerDirectoryFieldEditor extends AbstractContainerPathFieldEditor {

	public ContainerDirectoryFieldEditor(String fieldName, String labelText, Composite parent) {
		super(fieldName, labelText, parent);
	}

	@Override
	void handleEditButtonPressed() {

		if (containerMode) {

			ContainerDirectoryDialog dialog = new ContainerDirectoryDialog(getPage().getShell());

			String[] params = cLabel.getText().split(CONTAINER_PATH_SEPARATOR);

			if (params.length == 1) {
				dialog.setMountType(AbstractContainerPathDialog.NO_EXTERNAL_MOUNT);
				dialog.setContainerPath(params[0]);
			} else if (params.length == 2) {
				dialog.setMountType(AbstractContainerPathDialog.BIND_MOUNT);
				dialog.setHostDirectory(params[0]);
				dialog.setContainerPath(params[1]);
			} else if (params.length == 3) {
				dialog.setMountType(AbstractContainerPathDialog.BIND_MOUNT);
				dialog.setHostDirectory(params[0]);
				dialog.setContainerPath(params[1]);
				dialog.setReadOnly(CONTAINER_PATH_READONLY_FLAG.equals(params[2]));
			}

			if (dialog.open() == Window.OK) {
				// String oldValue = cLabel.getText();
				String newValue;

				switch (dialog.getMountType()) {
				case AbstractContainerPathDialog.NO_EXTERNAL_MOUNT:
					newValue = dialog.getContainerPath();
					break;
				case AbstractContainerPathDialog.BIND_MOUNT:
					newValue = dialog.getHostDirectory() + CONTAINER_PATH_SEPARATOR + dialog.getContainerPath()
							+ (dialog.isReadOnly() ? CONTAINER_PATH_SEPARATOR + CONTAINER_PATH_READONLY_FLAG : ""); //$NON-NLS-1$
					break;
				default:
					newValue = ""; //$NON-NLS-1$
					break;
				}

				setValue(newValue);
			}

		} else {

			DirectoryDialog dialog = new DirectoryDialog(getPage().getShell());

			// Be nice and try to resume browsing from where we left off
			if (getValue() != null && getValue().length() > 0) {

				File currentDir = new File(getValue());

				if (currentDir.exists()) {
					dialog.setFilterPath(currentDir.getParent());
				}
			}

			String dir = dialog.open();

			if (dir != null && dir.length() > 0) {
				setValue(dir);
			}

		}
	}

}

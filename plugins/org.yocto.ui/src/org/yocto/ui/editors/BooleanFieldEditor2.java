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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 *
 * An extended boolean field editor which exposes the checkbox control
 *
 * @author Intel Corporation
 *
 */
public class BooleanFieldEditor2 extends BooleanFieldEditor {

	public BooleanFieldEditor2(String name, String label, Composite parent) {
		super(name, label, parent);
	}

	public Button getCheckboxControl(Composite parent) {
		return super.getChangeControl(parent);
	}

}

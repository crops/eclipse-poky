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

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * An extended StringFieldEditor which writes to preference store only if required
 *
 * @author Intel Corporation
 *
 */
public class StringFieldEditor2 extends StringFieldEditor {

	public StringFieldEditor2(String name, String label, Composite parent) {
		super(name, label, parent);
	}

	@Override
	protected void doStore() {
		// There is an issue with writing string value to preference store will
		// always cause the preference store to be marked as dirty, so here we
		// workaround this by avoid writing the same string value.
		if (!getTextControl().getText().equals(getPreferenceStore().getString(getPreferenceName())))
			super.doStore();
	}

}

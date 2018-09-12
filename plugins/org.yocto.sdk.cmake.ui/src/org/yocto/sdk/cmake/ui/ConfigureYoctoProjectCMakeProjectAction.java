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
package org.yocto.sdk.cmake.ui;

import org.yocto.sdk.ui.actions.EnableNatureAction;

public class ConfigureYoctoProjectCMakeProjectAction extends EnableNatureAction {

	public ConfigureYoctoProjectCMakeProjectAction() {
		super("org.yocto.cmake.core.toolchain.yocto.base"); //$NON-NLS-1$
	}

}

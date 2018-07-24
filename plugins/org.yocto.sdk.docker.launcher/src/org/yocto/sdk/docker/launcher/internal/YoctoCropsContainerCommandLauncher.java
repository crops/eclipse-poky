/*******************************************************************************
 * Copyright (c) 2017, 2018 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.yocto.sdk.docker.launcher.internal;

import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.linuxtools.docker.ui.launch.IErrorMessageHolder;
import org.yocto.docker.launcher.ContainerCommandLauncher;
import org.yocto.docker.launcher.ContainerLauncher;

public class YoctoCropsContainerCommandLauncher
		extends ContainerCommandLauncher
		implements ICommandLauncher, IErrorMessageHolder {

	@Override
	protected ContainerLauncher getContainerLauncher() {
		return new YoctoCropsContainerLauncher();
	}

	@Override
	protected Integer getUid() {
		return null;
	}


}

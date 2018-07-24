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
import org.eclipse.cdt.core.ICommandLauncherFactory;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.yocto.docker.launcher.ContainerCommandLauncherFactory;
import org.yocto.sdk.core.YoctoProjectNature;

public class YoctoCropsContainerCommandLauncherFactory
		extends ContainerCommandLauncherFactory
		implements ICommandLauncherFactory {

	@Override
	protected ICommandLauncher createContainerCommandLauncher() {
		return new YoctoCropsContainerCommandLauncher();
	}

	@Override
	public ICommandLauncher getCommandLauncher(IProject project) {

		// only return extended container command launcher for projects with Yocto SDK nature
		try {
			if (project.getNature(YoctoProjectNature.NATURE_ID) == null)
				return null;
		} catch (CoreException e) {
			// Do nothing if project does not exist, or isn't open, or doesn't contain the Yocto SDK nature.
			return null;
		}

		return super.getCommandLauncher(project);
	}

	@Override
	public ICommandLauncher getCommandLauncher(
			ICConfigurationDescription cfgd) {

		// Add extra check to make sure project has Yocto SDK nature before resolving the command launcher
		try {
			if (cfgd.getProjectDescription().getProject().getNature(YoctoProjectNature.NATURE_ID) == null)
				return null;
		} catch (CoreException e) {
			// Do nothing if project does not exist, or isn't open, or doesn't contain the Yocto SDK nature.
			return null;
		}

		return super.getCommandLauncher(cfgd);
	}

}

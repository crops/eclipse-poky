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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.docker.ui.launch.IErrorMessageHolder;
import org.yocto.docker.launcher.ContainerLauncher;

public class YoctoCropsContainerLauncher extends ContainerLauncher {

	@Override
	public Process runCommand(String connectionName, String imageName, IProject project,
			IErrorMessageHolder errMsgHolder, String command, String commandDir, String workingDir,
			List<String> additionalDirs, Map<String, String> origEnv, Properties envMap, boolean supportStdin,
			boolean privilegedMode, HashMap<String, String> labels, boolean keepContainer, Integer uid) {

		String pokyEntryCommand = "/usr/bin/dumb-init -- /usr/bin/poky-entry.py --workdir " + workingDir + " --cmd '" + command + "'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return super.runCommand(connectionName, imageName, project, errMsgHolder, pokyEntryCommand, commandDir, workingDir,
				additionalDirs, origEnv, envMap, supportStdin, privilegedMode, labels, keepContainer, uid);
	}

}

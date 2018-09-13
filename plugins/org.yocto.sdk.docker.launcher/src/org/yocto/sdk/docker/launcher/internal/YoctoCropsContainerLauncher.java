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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;
import org.eclipse.linuxtools.docker.ui.launch.IErrorMessageHolder;
import org.eclipse.linuxtools.internal.docker.ui.wizards.DataVolumeModel;
import org.yocto.docker.launcher.ContainerLauncher;
import org.yocto.sdk.core.preference.YoctoProjectProfilePreferences;
import org.yocto.sdk.core.preference.YoctoProjectProjectPreferences;

@SuppressWarnings("restriction")
public class YoctoCropsContainerLauncher extends ContainerLauncher {

	@Override
	public Process runCommand(String connectionName, String imageName, IProject project,
			IErrorMessageHolder errMsgHolder, String command, String commandDir, String workingDir,
			List<String> additionalDirs, Map<String, String> origEnv, Properties envMap, boolean supportStdin,
			boolean privilegedMode, HashMap<String, String> labels, boolean keepContainer, Integer uid) {

		YoctoProjectProjectPreferences projectPreferences = YoctoProjectProjectPreferences
				.getProjectPreferences(project);
		YoctoProjectProfilePreferences profilePreferences = projectPreferences.getProfilePreferences();

		boolean isBuildDirectoryToolchain = YoctoProjectProfilePreferences.TOOLCHAIN_BUILD_DIRECTORY
				.equals(profilePreferences.getToolchain());
		String toolChainDir = isBuildDirectoryToolchain ? profilePreferences.getBuildDirectory()
				: profilePreferences.getSdkInstallation();

		String[] toolChainDirField = toolChainDir.split(":"); //$NON-NLS-1$

		String toolChainDirHostPath;
		String toolChainDirContainerPath;
		String toolChainDirReadOnly;

		if (toolChainDirField.length == 1) {
			toolChainDirHostPath = null;
			toolChainDirContainerPath = toolChainDirField[0];
			toolChainDirReadOnly = null;
		} else if (toolChainDirField.length == 2) {
			toolChainDirHostPath = toolChainDirField[0];
			toolChainDirContainerPath = toolChainDirField[1];
			toolChainDirReadOnly = null;
		} else {
			toolChainDirHostPath = toolChainDirField[0];
			toolChainDirContainerPath = toolChainDirField[1];
			toolChainDirReadOnly = toolChainDirField[2];
		}

		boolean toolChainDirReadOnlyValue = Boolean.valueOf(toolChainDirReadOnly);

		DataVolumeModel m;

		// For build directory toolchain to work within container, we'll need to bind
		// mount the poky git repo directory instead.
		if (isBuildDirectoryToolchain) {
			// TODO: do not assume the build directory toolchain is 2 level down from the
			// poky git repo
			String pokyDirHostPath = new File(toolChainDirHostPath).getParentFile().getParent();
			String pokyDirContainerPath = new File(toolChainDirContainerPath).getParentFile().getParent();
			m = new DataVolumeModel(pokyDirContainerPath, pokyDirHostPath, toolChainDirReadOnlyValue);
		} else {
			m = new DataVolumeModel(toolChainDirContainerPath, toolChainDirHostPath, toolChainDirReadOnlyValue);
		}

		additionalDirs.add(m.toString());

		List<String> entryPoint = getEntrypoint(connectionName, imageName);

		// TODO: use container entry point
		String pokyEntryCommand = String.join(" ", entryPoint) //$NON-NLS-1$
				+ " --toolchain " + toolChainDirContainerPath //$NON-NLS-1$
				+ " --workdir " + workingDir //$NON-NLS-1$
				+ " --cmd '" + command + "'"; //$NON-NLS-1$ //$NON-NLS-2$

		return super.runCommand(connectionName, imageName, project, errMsgHolder, pokyEntryCommand, commandDir,
				workingDir, additionalDirs, origEnv, envMap, supportStdin, privilegedMode, labels, keepContainer, uid);
	}

	List<String> getEntrypoint(String connectionName, String imageName) {

		IDockerConnection connection = DockerConnectionManager.getInstance().getConnectionByName(connectionName);

		if (connection != null) {
			IDockerImageInfo imageInfo = connection.getImageInfo(imageName);

			if (imageInfo != null) {
				return imageInfo.containerConfig().entrypoint();
			}
		}
		return new ArrayList<String>();
	}
}

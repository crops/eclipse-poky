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
package org.yocto.cmake.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator2;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.yocto.cmake.core.internal.Activator;

public class CMakeMakefileGenerator implements IManagedBuilderMakefileGenerator2 {

	private IProject project;
	private IConfiguration configuration;

	@Override
	public void generateDependencies() throws CoreException {
		regenerateDependencies(false);
	}

	private IPath getCommandAbsPath(String command, IEnvironmentVariable var) {
		// Try to resolve absolute path to executable

		if (var != null && var.getValue() != null) {
			for (String path : var.getValue().split(File.pathSeparator)) {
				File cmakeCommandAbsPath = new File(path, command);
				if (cmakeCommandAbsPath.exists()) {
					// return only the first path which matched
					return new Path(cmakeCommandAbsPath.getAbsolutePath());
				}
			}
		}

		return new Path(command);
	}

	@Override
	public MultiStatus generateMakefiles(IResourceDelta delta) throws CoreException {

		List<String> cmakeFlags = new ArrayList<String>();

		final String cmakeToolId = Activator.PLUGIN_ID + ".cmake"; //$NON-NLS-1$

		ITool[] cmakeTools = configuration.getToolsBySuperClassId(cmakeToolId);
		ITool cmakeTool = cmakeTools[0];
		String cmakeCommand = configuration.getToolCommand(cmakeTool);

		final String BUILD_DIR = "."; //$NON-NLS-1$
		final String SRC_DIR = ".."; //$NON-NLS-1$

		cmakeFlags.add(BUILD_DIR);

		cmakeFlags.add(SRC_DIR);

		String[] cmakeToolFlags;
		try {
			cmakeToolFlags = cmakeTool.getToolCommandFlags(project.getLocation(), project.getLocation());
			for (String cmakeFlag : cmakeToolFlags) {
				cmakeFlags.add(cmakeFlag);
			}
		} catch (BuildException e1) {
			return new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR,
					Messages.CMakeMakefileGenerator_GetCMakeToolCommandFlagsFailed, e1);
		}

		IFile toolchainFile = this.project.getFile("toolchain.cmake"); //$NON-NLS-1$

		if (toolchainFile.exists())
			cmakeFlags.add("-DCMAKE_TOOLCHAIN_FILE=" + toolchainFile.getLocation().toOSString()); //$NON-NLS-1$

//		cmakeFullCommand.add(this.project.getLocation().toOSString());

		try {
			File buildDir = getBuildWorkingDir().toFile();

			if (!buildDir.exists())
				if (!buildDir.mkdirs())
					throw new RuntimeException(
							String.format(Messages.CMakeMakefileGenerator_CreateDirectoryFailed, buildDir.toString()));

			ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(this.project);
			launcher.setProject(project);

			ICProjectDescription cpdesc = CoreModel.getDefault().getProjectDescription(project, true);
			ICConfigurationDescription ccdesc = cpdesc.getActiveConfiguration();
			IEnvironmentVariableManager manager = CCorePlugin.getDefault().getBuildEnvironmentManager();

			List<String> result = new ArrayList<String>();
			for (IEnvironmentVariable var : manager.getVariables(ccdesc, true)) {
				result.add(var.getName() + "=" + var.getValue()); //$NON-NLS-1$
			}

			IPath cmakeCommandPath = new Path(cmakeCommand);

			final String cmakeConsoleId = "org.yocto.cmake.ui.CMakeConsole"; //$NON-NLS-1$

			IConsole cmakeConsole = CCorePlugin.getDefault().getConsole(cmakeConsoleId);
			cmakeConsole.start(this.project);
			cmakeConsole.getInfoStream().write(String.format(Messages.CMakeMakefileGenerator_GeneratingBuildFiles,
					this.project.getName() + System.lineSeparator()));

			List<String> cmakeCommandAndFlags = new ArrayList<String>();
			cmakeCommandAndFlags.add(cmakeCommand);
			cmakeCommandAndFlags.addAll(cmakeFlags);
			cmakeConsole.getOutputStream().write(String.join(" ", cmakeCommandAndFlags) + System.lineSeparator()); //$NON-NLS-1$

			Process process = launcher.execute(cmakeCommandPath, cmakeFlags.toArray(new String[] {}),
					result.toArray(new String[] {}), new Path(buildDir.getAbsolutePath()), new NullProgressMonitor());

			if (process == null) {
				throw new CoreException(new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR,
						String.format(Messages.CMakeMakefileGenerator_LauncherCreateProcessFailed,
								String.join(" ", cmakeFlags)), //$NON-NLS-1$
						null));
			}

			int exitValue = launcher.waitAndRead(cmakeConsole.getOutputStream(), cmakeConsole.getErrorStream(),
					new NullProgressMonitor());

			if (exitValue != 0) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, String.format(
						Messages.CMakeMakefileGenerator_ProcessExitCodeNonZero, String.join(" ", cmakeFlags)), null)); //$NON-NLS-1$
			}

		} catch (IOException e) {
			throw new CoreException(new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR, String
					.format(Messages.CMakeMakefileGenerator_CMakeConsoleWriteFailed, String.join(" ", cmakeFlags)), e)); //$NON-NLS-1$
		}
		return new MultiStatus(Activator.PLUGIN_ID, IStatus.OK, null, null);
	}

	@Override
	public IPath getBuildWorkingDir() {
		return this.project.getFolder(this.configuration.getName()).getLocation();
	}

	@Override
	public String getMakefileName() {
		return "Makefile"; //$NON-NLS-1$
	}

	@Override
	public void initialize(IProject project, IManagedBuildInfo info, IProgressMonitor monitor) {
		this.project = project;
		this.configuration = info.getDefaultConfiguration();
	}

	@Override
	public boolean isGeneratedResource(IResource resource) {
		// TODO: figure out whether this check is useful or not
		return false;
	}

	@Override
	public void regenerateDependencies(boolean force) throws CoreException {
		// TODO: figure out whether this is required

	}

	@Override
	public MultiStatus regenerateMakefiles() throws CoreException {
		return generateMakefiles(null);
	}

	@Override
	public void initialize(int buildKind, IConfiguration cfg, IBuilder builder, IProgressMonitor monitor) {
		this.project = cfg.getOwner().getProject();
		this.configuration = cfg;
	}

	protected IProject getProject() {
		return this.project;
	}

	protected IConfiguration getConfiguration() {
		return this.configuration;
	}
}

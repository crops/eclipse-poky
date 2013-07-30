/*******************************************************************************
 * Copyright (c) 2013 BMW Car IT GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * BMW Car IT - initial API and implementation
 *******************************************************************************/
package org.yocto.cmake.managedbuilder.job;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.yocto.cmake.managedbuilder.Activator;
import org.yocto.cmake.managedbuilder.YoctoCMakeMessages;
import org.yocto.cmake.managedbuilder.util.ConsoleUtility;
import org.yocto.cmake.managedbuilder.util.SystemProcess;


public class ExecuteConfigureJob extends Job {
	private SystemProcess configureProcess;
	private LinkedList<String> configureCommand;
	private IProject project;
	private IConfiguration configuration;
	private IPath location;


	public ExecuteConfigureJob(String name,
			IProject project, IConfiguration configuration, IPath location) {
		super(name);
		this.project = project;
		this.configuration = configuration;
		this.location = location;
		createCommands();
		createProcesses();
	}

	protected void createCommands() {
		configureCommand = new LinkedList<String>();

		ITool[] configure = configuration
				.getToolsBySuperClassId("org.yocto.cmake.managedbuilder.cmakeconfigure.gnu.exe"); //$NON-NLS-1$

		addCommand(configure[0]);

		try {
			addFlags(configure[0]);
		} catch (BuildException e) {
			// ignore this exception
		}
	}

	private void addCommand(ITool configure) {
		String command = configuration.getToolCommand(configure);
		configureCommand.add(command);
	}

	private void addFlags(ITool configure) throws BuildException {
		String[] flags = configure.getToolCommandFlags(
				project.getLocation(), location);
		for (String flag : flags) {
			if (flag.contains(" ")) { //$NON-NLS-1$
				String[] separatedFlags = flag.trim().split(" "); //$NON-NLS-1$
				configureCommand.addAll(Arrays.asList(separatedFlags));
			} else {
				configureCommand.add(flag);
			}
		}
	}

	protected void createProcesses() {
		configureProcess =
				new SystemProcess(configureCommand, location.toFile(), getEnvVariablesAsMap(project));
	}

	private Map<String,String> getEnvVariablesAsMap (IProject project) {
		Map<String, String> result = new HashMap<String, String>();

		ICProjectDescription cpdesc = CoreModel.getDefault().getProjectDescription(project, true);
		ICConfigurationDescription ccdesc = cpdesc.getActiveConfiguration();
		IEnvironmentVariableManager manager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IContributedEnvironment env = manager.getContributedEnvironment();

		for(IEnvironmentVariable var : env.getVariables(ccdesc)) {
			result.put(var.getName(), var.getValue());
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(
				YoctoCMakeMessages.getString("ExecuteConfigureJob.runConfigure"), 20); //$NON-NLS-1$

		IOConsoleOutputStream cos =
				ConsoleUtility.getConsoleOutput(YoctoCMakeMessages.getFormattedString("ExecuteConfigureJob.consoleName", //$NON-NLS-1$
						project.getName()));
		monitor.worked(1);

		try {
			return buildProject(monitor, cos);
		} catch (IOException e) {
			return new Status(Status.ERROR,
					Activator.PLUGIN_ID, Status.OK,
					YoctoCMakeMessages.getString("ExecuteConfigureJob.error.couldNotStart"), e); //$NON-NLS-1$
		} catch (InterruptedException e) {
			return new Status(Status.WARNING,
					Activator.PLUGIN_ID,
					YoctoCMakeMessages.getString("ExecuteConfigureJob.warning.aborted")); //$NON-NLS-1$
		} finally {
			try {
				cos.close();
			} catch (IOException e) {
				cos = null;
			}
		}
	}

	private IStatus buildProject(IProgressMonitor monitor,
			IOConsoleOutputStream cos) throws IOException, InterruptedException {
		monitor.subTask(
				YoctoCMakeMessages.getString("ExecuteConfigureJob.buildingMakefile")); //$NON-NLS-1$
		configureProcess.start(cos);
		int exitValue = configureProcess.waitForResultAndStop();
		monitor.worked(15);

		if (exitValue != 0) {
			return new Status(Status.ERROR, Activator.PLUGIN_ID,
					YoctoCMakeMessages.getString("ExecuteConfigureJob.error.buildFailed") + " " + exitValue); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return Status.OK_STATUS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#canceling()
	 */
	/**
	 * Cancels the job and interrupts the system process.
	 * {@inheritDoc}
	 */
	@Override
	protected void canceling() {
		configureProcess.interrupt();
	}
}

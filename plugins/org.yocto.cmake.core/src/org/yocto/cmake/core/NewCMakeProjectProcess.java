package org.yocto.cmake.core;

import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.templateengine.ProjectCreatedActions;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class NewCMakeProjectProcess extends ProcessRunner {

	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
			throws ProcessFailureException {

		String projectName = args[0].getSimpleValue();
		String projectLocation = args[1].getSimpleValue();

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		try {

			if (!project.exists()) {

				IPath projectLocationPath = (projectLocation != null && projectLocation.length() > 0)
						? Path.fromPortableString(projectLocation)
						: null;

				List<?> configs = template.getTemplateInfo().getConfigurations();

				ProjectCreatedActions actions = new ProjectCreatedActions();
				actions.setProject(project);

				if (projectLocation != null) {
					actions.setProjectLocation(projectLocationPath);
				}
				actions.setConfigs(configs.toArray(new IConfiguration[configs.size()]));

				IManagedBuildInfo info = actions.createProject(monitor, CCorePlugin.DEFAULT_INDEXER, true);
				info.setValid(true);
				ManagedBuildManager.saveBuildInfo(project, true);
			}

			CMakeNature.addNature(project, monitor);

		} catch (CoreException | BuildException e) {
			throw new ProcessFailureException(Messages.NewCMakeProjectProcess_ProjectCreationFailed, e);
		}
	}

}

package org.yocto.cmake.ui;

import java.util.HashMap;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yocto.cmake.core.CMakeMakefileGenerator;
import org.yocto.cmake.ui.internal.Activator;

public class RegenerateHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Shell shell = HandlerUtil.getActiveShell(event);

		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			for (Object obj : ((IStructuredSelection) selection).toList()) {
				if (obj instanceof IAdaptable) {
					IProject project = ((IAdaptable) obj).getAdapter(IProject.class);

					if (project != null) {

						handleRegenerate(shell, project);

					}
				}
			}
		}

		return null;
	}

	private void handleRegenerate(Shell shell, IProject project) {

		IBuilder[] builders = ManagedBuildManager.createBuilders(project, new HashMap<String, String>());

		for (IBuilder builder : builders) {
			IManagedBuilderMakefileGenerator generator = builder.getBuildFileGenerator();

			if (generator instanceof CMakeMakefileGenerator) {

				IFolder buildDir = CMakeMakefileGenerator.getBuildDir(project);

				if (CMakeMakefileGenerator.isBuildSystemGenerated(project)) {
					MessageBox mbox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
					mbox.setText(Messages.RegenerateHandler_ConfirmRegenerationDialogTitle);
					String msg = String.format(Messages.RegenerateHandler_ConfirmRegenerate, buildDir.getName());
					mbox.setMessage(msg);

					if (mbox.open() != SWT.YES) {
						continue;
					}
				}

				Job deleteBuildDirJob = new Job(
						String.format(Messages.RegenerateHandler_DeletingBuildDir, project.getName(), buildDir.getName())) {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						// Delete the build directory before regenerating it
						try {
							return ResourcesPlugin.getWorkspace().delete(new IResource[] { buildDir }, true, monitor);
						} catch (CoreException e) {
							return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									String.format(Messages.RegenerateHandler_DeleteBuildDirFailed, project.getName(),
											buildDir.getName()),
									e);
						}
					}

				};

				ISchedulingRule deleteBuildDirRule = ResourcesPlugin.getWorkspace().getRuleFactory()
						.modifyRule(ResourcesPlugin.getWorkspace().getRoot());
				deleteBuildDirJob.setRule(deleteBuildDirRule);
				deleteBuildDirJob.schedule();

				Job regenerateBuildDirJob = new Job(
						String.format(Messages.RegenerateHandler_RegeneratingBuildDir, project.getName())) {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							generator.initialize(project, ManagedBuildManager.getBuildInfo(project), monitor);
							generator.regenerateMakefiles();
						} catch (CoreException e) {
							return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									String.format(Messages.RegenerateHandler_RegenerateBuildDirFailed, project.getName()), e);
						}

						return Status.OK_STATUS;
					}

				};

				ISchedulingRule regenerateBuildDirRule = ResourcesPlugin.getWorkspace().getRuleFactory()
						.modifyRule(buildDir);
				regenerateBuildDirJob.setRule(regenerateBuildDirRule);
				regenerateBuildDirJob.schedule();

				Job refreshBuildDirJob = new Job(
						String.format(Messages.RegenerateHandler_RefreshingBuildDir, project.getName(), buildDir.getName())) {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
						} catch (CoreException e) {
							return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									String.format(Messages.RegenerateHandler_RefreshBuildDirFailed, project.getName(),
											buildDir.getName()),
									e);
						}
						return Status.OK_STATUS;
					}

				};
				refreshBuildDirJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().refreshRule(project));
				refreshBuildDirJob.schedule();

			}

		}

	}
}

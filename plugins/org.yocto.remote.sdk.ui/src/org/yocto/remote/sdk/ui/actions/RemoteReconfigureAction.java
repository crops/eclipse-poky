package org.yocto.remote.sdk.ui.actions;

import org.eclipse.cdt.internal.autotools.ui.actions.InvokeAction;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.yocto.remote.sdk.core.RemoteAutotoolsNewMakeGenerator;

public class RemoteReconfigureAction extends InvokeAction {

	public void run(IAction action) {
		IContainer container = getSelectedContainer();
		if (container == null)
			return;

		// We need to use a workspace root scheduling rule because adding MakeTargets
		// may end up saving the project description which runs under a workspace root rule.
		final ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRoot();
		
		Job backgroundJob = new Job("Reconfigure Action"){  //$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				try {
					ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

						public void run(IProgressMonitor monitor) throws CoreException {
							IProject project = getSelectedContainer().getProject();
							RemoteAutotoolsNewMakeGenerator m = new RemoteAutotoolsNewMakeGenerator();
							IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
							CUIPlugin.getDefault().startGlobalConsole();
							m.initialize(project, info, monitor);
							try {
								m.reconfigure();
							} catch (CoreException e) {
								// do nothing for now
							}
						}
					}, rule, IWorkspace.AVOID_UPDATE, monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
				IStatus returnStatus = Status.OK_STATUS;
				return returnStatus;
			}
		};

		backgroundJob.setRule(rule);
		backgroundJob.schedule();
	}

	public void dispose() {

	}

}


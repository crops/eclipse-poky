package org.yocto.cmake.ui;

import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yocto.cmake.core.CMakeMakefileGenerator;

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

						try {
							PlatformUI.getWorkbench().getProgressService().run(false, true,
									new IRunnableWithProgress() {

										@Override
										public void run(IProgressMonitor monitor)
												throws InvocationTargetException, InterruptedException {
											try {
												handleRegenerate(shell, project, monitor);
											} catch (CoreException e) {
												throw new InvocationTargetException(e);
											}
										}
									});
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			}
		}

		return null;
	}

	private void handleRegenerate(Shell shell, IProject project, IProgressMonitor monitor) throws CoreException {

		IBuilder[] builders = ManagedBuildManager.createBuilders(project, new HashMap<String, String>());

		for (IBuilder builder : builders) {
			IManagedBuilderMakefileGenerator generator = builder.getBuildFileGenerator();

			if (generator instanceof CMakeMakefileGenerator) {

				if (CMakeMakefileGenerator.isBuildSystemGenerated(project)) {
					IFolder buildDir = CMakeMakefileGenerator.getBuildDir(project);
					MessageBox mbox = new MessageBox(shell, SWT.YES | SWT.NO);
					mbox.setText(Messages.RegenerateHandler_ConfirmRegenerationDialogTitle);
					String msg = String.format(Messages.RegenerateHandler_ConfirmRegenerate, buildDir.getName());
					mbox.setMessage(msg);

					if (mbox.open() == SWT.YES) {
						// Delete the build directory before regenerating it
						ResourcesPlugin.getWorkspace().delete(new IResource[] { buildDir }, true, monitor);
					}
				}
			}
			try {
				generator.initialize(project, ManagedBuildManager.getBuildInfo(project), monitor);
				generator.regenerateMakefiles();
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}

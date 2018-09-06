package org.yocto.cmake.ui.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.ui.wizards.CfgHolder;
import org.eclipse.cdt.managedbuilder.ui.wizards.NewMakeProjFromExisting;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.yocto.cmake.core.CMakeNature;

@SuppressWarnings("restriction")
public class CMakeImportWizard extends NewMakeProjFromExisting {

	CMakeImportWizardPage page;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Import existing code as CMake project");
	}

	@Override
	public void addPages() {
		page = new CMakeImportWizardPage();
		addPage(page);
	}

	/**
	 * This method override is mostly needed to set
	 * IBuilder.setManagedBuildOn(true), unfortunately there's quite alot of
	 * boilerplate code that needs to be duplicated ...
	 */
	@Override
	public boolean performFinish() {

		final String projectName = page.getProjectName();
		final Path projectLocation = new Path(page.getLocation());
		final boolean isC = page.isC();
		final boolean isCpp = page.isCPP();
		final IToolChain toolchain = page.getToolChain();

		IRunnableWithProgress runnable = new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor)
					throws CoreException, InvocationTargetException, InterruptedException {

				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IProject project = workspace.getRoot().getProject(projectName);
				IProjectDescription projectDescription = workspace.newProjectDescription(projectName);

				if (!projectLocation.isEmpty()) {
					if (projectDescription.getLocationURI() == null
							|| !projectLocation.equals(URIUtil.toPath(projectDescription.getLocationURI()))) {
						projectDescription.setLocation(projectLocation);
					}
				}

				CCorePlugin.getDefault().createCDTProject(projectDescription, project, monitor);

				if (isC) {
					CCProjectNature.addCNature(project, monitor);
				}

				if (isCpp) {
					CCProjectNature.addCCNature(project, monitor);
				}

				CMakeNature.addNature(project, monitor);

				ICProjectDescriptionManager cProjectDescriptionManager = CoreModel.getDefault()
						.getProjectDescriptionManager();
				ICProjectDescription cProjectDescription = cProjectDescriptionManager.createProjectDescription(project,
						true);
				ManagedProject managedProject = new ManagedProject(cProjectDescription);

				ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
				info.setManagedProject(managedProject);

				CfgHolder cfgHolder = new CfgHolder(toolchain, null);
				String toolchainId = (toolchain == null) ? "0" : toolchain.getId(); //$NON-NLS-1$
				Configuration cfg = new Configuration(managedProject, (ToolChain) toolchain,
						ManagedBuildManager.calculateChildId(toolchainId, null), cfgHolder.getName());
				IBuilder builder = cfg.getEditableBuilder();

				// Turn on C/C++ Build -> Makefile generation -> Generate Makefiles
				// automatically. This is needed so that the cmake builder will be used to
				// generate Makefiles
				builder.setManagedBuildOn(true);

				CConfigurationData data = cfg.getConfigurationData();
				cProjectDescription.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);

				cProjectDescriptionManager.setProjectDescription(project, cProjectDescription);
			}

		};

		try {
			getContainer().run(true, true, runnable);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

}

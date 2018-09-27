package org.yocto.sdk.autotools.ui;

import org.eclipse.cdt.internal.autotools.core.configure.AutotoolsConfigurationManager;
import org.eclipse.cdt.internal.autotools.core.configure.IAConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.yocto.sdk.ui.actions.EnableNatureAction;

@SuppressWarnings("restriction")
public class ConfigureYoctoProjectAutotoolsProjectAction extends EnableNatureAction {

	public ConfigureYoctoProjectAutotoolsProjectAction() {
		super("org.yocto.sdk.autotools.core.toolChain"); //$NON-NLS-1$
	}

	@Override
	protected void updateCProjectDescription(IProject project) throws CoreException {

		super.updateCProjectDescription(project);

		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IAConfiguration aCfg = AutotoolsConfigurationManager.getInstance().getConfiguration(project,
				info.getDefaultConfiguration().getId());
		// Make sure configure picks up --host, --build, --target etc.
		aCfg.setOption("configure", "configure ${CONFIGURE_FLAGS}"); //$NON-NLS-1$ //$NON-NLS-2$
		AutotoolsConfigurationManager.getInstance().addConfiguration(project, aCfg);
		AutotoolsConfigurationManager.getInstance().saveConfigs(project);

	}

}

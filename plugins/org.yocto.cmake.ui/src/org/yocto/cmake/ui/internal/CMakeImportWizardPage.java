package org.yocto.cmake.ui.internal;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.NewMakeProjFromExistingPage;
import org.eclipse.swt.widgets.Composite;

/**
 * Fork NewMakeProjFromExistingPage to support filtering by toolchain type, or
 * more specifically here we filter by CMake toolchain.
 *
 * @author Intel Corporation
 * @see org.eclipse.cdt.managedbuilder.ui.wizards.NewMakeProjFromExistingPage
 *
 */
public class CMakeImportWizardPage extends NewMakeProjFromExistingPage {

	@Override
	public void addToolchainSelector(Composite parent) {
		// Don't populate toolchain selector, instead always import the project using
		// the host cmake toolchain
	}

	@Override
	public IToolChain getToolChain() {

		final String CMAKE_TOOLCHAIN_BASE_ID = "org.yocto.cmake.core.toolchain.base"; //$NON-NLS-1$

		IToolChain[] toolChains = ManagedBuildManager.getRealToolChains();

		for (IToolChain toolChain : toolChains) {
			if (CMAKE_TOOLCHAIN_BASE_ID.equals(toolChain.getId())) {
				return toolChain;
			}
		}

		return null;
	}

}

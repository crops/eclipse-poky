package org.yocto.cmake.ui.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.NewMakeProjFromExistingPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.yocto.cmake.ui.Messages;

/**
 * Fork NewMakeProjFromExistingPage to support filtering by toolchain type, or
 * more specifically here we filter by CMake toolchain.
 *
 * @author Intel Corporation
 * @see org.eclipse.cdt.managedbuilder.ui.wizards.NewMakeProjFromExistingPage
 *
 */
public class CMakeImportWizardPage extends NewMakeProjFromExistingPage {

	List tcList;
	Map<String, IToolChain> tcMap = new HashMap<String, IToolChain>();

	@Override
	public void addToolchainSelector(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setText(Messages.CMakeImportWizardPage_ToolchainForIndexer);

		tcList = new List(group, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);

		// Base the List control size on the number of total toolchains, up to 15
		// entries, but allocate for no
		// less than five (small list boxes look strange). A vertical scrollbar will
		// appear as needed
		updateTcMap(false);
		gd.heightHint = tcList.getItemHeight() * (1 + Math.max(Math.min(tcMap.size(), 15), 5)); // +1 for <none>
		tcList.setLayoutData(gd);
		tcList.add(Messages.CMakeImportWizardPage_NoToolchain);

		final Button supportedOnly = new Button(group, SWT.CHECK);
		supportedOnly.setSelection(false);
		supportedOnly.setText(Messages.CMakeImportWizardPage_ShowSupportedToolchainOnly);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		supportedOnly.setLayoutData(gd);
		supportedOnly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTcWidget(supportedOnly.getSelection());
			}
		});

		supportedOnly.setSelection(true);
		updateTcWidget(true);
	}

	/**
	 * Load our map with the suitable toolchains.
	 *
	 * @param supportedOnly if true, add only toolchains that are available and
	 *                      which support the host platform
	 */
	private void updateTcMap(boolean supportedOnly) {
		tcMap.clear();
		IToolChain[] toolChains = ManagedBuildManager.getRealToolChains();
		for (IToolChain toolChain : toolChains) {
			if (toolChain.isAbstract() || toolChain.isSystemObject())
				continue;
			if (supportedOnly) {
				if (!toolChain.isSupported() || !ManagedBuildManager.isPlatformOk(toolChain)) {
					continue;
				}
			}

			// TODO: use proper cmake toolchain filtering
			if (isCompatibleCMakeToolchain(toolChain)) {
				tcMap.put(toolChain.getUniqueRealName(), toolChain);
			}
		}
	}

	private boolean isCompatibleCMakeToolchain(IToolChain toolChain) {

		// Here we expect compatible cmake toolchains all extends this cmake base
		// toolchain
		final String CMAKE_TOOLCHAIN_BASE_ID = "org.yocto.cmake.core.toolchain"; //$NON-NLS-1$

		if (toolChain.getId().equals(CMAKE_TOOLCHAIN_BASE_ID)) {
			return true;
		} else {
			if (toolChain.getSuperClass() != null) {
				return isCompatibleCMakeToolchain(toolChain.getSuperClass());
			} else {
				return false;
			}
		}
	}

	/**
	 * Load our map and with the suitable toolchains and then populate the List
	 * control
	 *
	 * @param supportedOnly if true, consider only supported toolchains
	 */
	private void updateTcWidget(boolean supportedOnly) {
		updateTcMap(supportedOnly);
		ArrayList<String> names = new ArrayList<String>(tcMap.keySet());
		Collections.sort(names);

		tcList.removeAll();
		tcList.add(Messages.CMakeImportWizardPage_NoToolchain);
		for (String name : names)
			tcList.add(name);

		tcList.setSelection(0); // select <none>
	}

	@Override
	public IToolChain getToolChain() {
		String[] selection = tcList.getSelection();
		return selection.length != 0 ? tcMap.get(selection[0]) : null;
	}

}

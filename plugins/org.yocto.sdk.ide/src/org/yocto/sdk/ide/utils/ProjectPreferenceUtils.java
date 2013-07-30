/*******************************************************************************
 * Copyright (c) 2013 BMW Car IT GmbH.
 * Copyright (c) 2010 Intel Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel - initial implementation
 * BMW Car IT - initial implementation and refactoring
 *******************************************************************************/
package org.yocto.sdk.ide.utils;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;
import org.yocto.sdk.ide.YoctoGeneralException;
import org.yocto.sdk.ide.YoctoProfileElement;
import org.yocto.sdk.ide.YoctoSDKMessages;
import org.yocto.sdk.ide.YoctoUIElement;
import org.yocto.sdk.ide.natures.YoctoSDKAutotoolsProjectNature;
import org.yocto.sdk.ide.natures.YoctoSDKCMakeProjectNature;
import org.yocto.sdk.ide.preferences.PreferenceConstants;

public class ProjectPreferenceUtils {
	private static final String CONSOLE_MESSAGE  = "Menu.SDK.Console.Configure.Message";

	/* Get POKY Preference settings from project's preference store */
	public static YoctoUIElement getElem(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(YoctoSDKUtilsConstants.PROJECT_SCOPE);
		if (projectNode == null) {
			return getElemFromProjectEnv(project);
		}

		YoctoUIElement elem = new YoctoUIElement();
		elem.setStrToolChainRoot(projectNode.get(PreferenceConstants.TOOLCHAIN_ROOT,""));
		elem.setStrTarget(projectNode.get(PreferenceConstants.TOOLCHAIN_TRIPLET,""));
		elem.setStrQemuKernelLoc(projectNode.get(PreferenceConstants.QEMU_KERNEL,""));
		elem.setStrSysrootLoc(projectNode.get(PreferenceConstants.SYSROOT,""));
		elem.setStrQemuOption(projectNode.get(PreferenceConstants.QEMU_OPTION,""));
		String sTemp = projectNode.get(PreferenceConstants.TARGET_ARCH_INDEX,"");
		if (!sTemp.isEmpty()) {
			elem.setIntTargetIndex(Integer.valueOf(sTemp).intValue());
		}

		if (projectNode.get(PreferenceConstants.SDK_MODE,"").equalsIgnoreCase(IPreferenceStore.TRUE)) {
			elem.setEnumPokyMode(YoctoUIElement.PokyMode.POKY_SDK_MODE);
		} else {
			elem.setEnumPokyMode(YoctoUIElement.PokyMode.POKY_TREE_MODE);
		}

		if(projectNode.get(PreferenceConstants.TARGET_MODE,"").equalsIgnoreCase(IPreferenceStore.TRUE)) {
			elem.setEnumDeviceMode(YoctoUIElement.DeviceMode.QEMU_MODE);
		} else {
			elem.setEnumDeviceMode(YoctoUIElement.DeviceMode.DEVICE_MODE);
		}
		return elem;
	}

	/* Get POKY Preference settings from project's environment */
	public static YoctoUIElement getElemFromProjectEnv(IProject project) {
		YoctoUIElement elem = new YoctoUIElement();
		elem.setStrToolChainRoot(YoctoSDKUtils.getEnvValue(project, PreferenceConstants.TOOLCHAIN_ROOT));
		elem.setStrTarget(YoctoSDKUtils.getEnvValue(project, PreferenceConstants.TOOLCHAIN_TRIPLET));
		elem.setStrQemuKernelLoc(YoctoSDKUtils.getEnvValue(project, PreferenceConstants.QEMU_KERNEL));
		elem.setStrSysrootLoc(YoctoSDKUtils.getEnvValue(project, PreferenceConstants.SYSROOT));
		elem.setStrQemuOption(YoctoSDKUtils.getEnvValue(project, PreferenceConstants.QEMU_OPTION));
		String sTemp = YoctoSDKUtils.getEnvValue(project, PreferenceConstants.TARGET_ARCH_INDEX);

		if (!sTemp.isEmpty()) {
			elem.setIntTargetIndex(Integer.valueOf(sTemp).intValue());
		}

		if (YoctoSDKUtils.getEnvValue(project, PreferenceConstants.SDK_MODE).equalsIgnoreCase(IPreferenceStore.TRUE)) {
			elem.setEnumPokyMode(YoctoUIElement.PokyMode.POKY_SDK_MODE);
		} else {
			elem.setEnumPokyMode(YoctoUIElement.PokyMode.POKY_TREE_MODE);
		}

		if(YoctoSDKUtils.getEnvValue(project, PreferenceConstants.TARGET_MODE).equalsIgnoreCase(IPreferenceStore.TRUE)) {
			elem.setEnumDeviceMode(YoctoUIElement.DeviceMode.QEMU_MODE);
		} else {
			elem.setEnumDeviceMode(YoctoUIElement.DeviceMode.DEVICE_MODE);
		}

		return elem;
	}

	/* Get profiles and selected profile from the project's preference store */
	public static YoctoProfileElement getProfiles(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(YoctoSDKUtilsConstants.PROJECT_SCOPE);

		if (projectNode == null) {
			return YoctoSDKUtils.getProfilesFromDefaultStore();
		}

		String profiles = projectNode.get(PreferenceConstants.PROFILES, "");
		String selectedProfile = projectNode.get(PreferenceConstants.SELECTED_PROFILE, "");

		return new YoctoProfileElement(profiles, selectedProfile);
	}

	public static boolean getUseProjectSpecificOption(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(YoctoSDKUtilsConstants.PROJECT_SCOPE);
		if (projectNode == null) {
			return false;
		}

		String useProjectSpecificSettingString = projectNode.get(PreferenceConstants.PROJECT_SPECIFIC_PROFILE,
																	IPreferenceStore.FALSE);

		if (useProjectSpecificSettingString.equals(IPreferenceStore.FALSE)) {
			return false;
		}

		return true;
	}

	/* Save POKY Preference settings to project's preference store */
	public static void saveElem(YoctoUIElement elem, IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(YoctoSDKUtilsConstants.PROJECT_SCOPE);
		if (projectNode == null) {
			return;
		}

		projectNode.putInt(PreferenceConstants.TARGET_ARCH_INDEX, elem.getIntTargetIndex());
		if (elem.getEnumPokyMode() == YoctoUIElement.PokyMode.POKY_SDK_MODE) {
			projectNode.put(PreferenceConstants.SDK_MODE, IPreferenceStore.TRUE);
		} else {
			projectNode.put(PreferenceConstants.SDK_MODE, IPreferenceStore.FALSE);
		}
		projectNode.put(PreferenceConstants.QEMU_KERNEL, elem.getStrQemuKernelLoc());
		projectNode.put(PreferenceConstants.QEMU_OPTION, elem.getStrQemuOption());
		projectNode.put(PreferenceConstants.SYSROOT, elem.getStrSysrootLoc());
		if (elem.getEnumDeviceMode() == YoctoUIElement.DeviceMode.QEMU_MODE) {
			projectNode.put(PreferenceConstants.TARGET_MODE, IPreferenceStore.TRUE);
		} else {
			projectNode.put(PreferenceConstants.TARGET_MODE, IPreferenceStore.FALSE);
		}
		projectNode.put(PreferenceConstants.TOOLCHAIN_ROOT, elem.getStrToolChainRoot());
		projectNode.put(PreferenceConstants.TOOLCHAIN_TRIPLET, elem.getStrTarget());

		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	/* Save POKY Preference settings to project's environment */
	public static void saveElemToProjectEnv(YoctoUIElement elem, IProject project) {
		ConsoleOutputStream consoleOutStream = null;

		try {
			YoctoSDKUtils.setEnvironmentVariables(project, elem);
			YoctoSDKUtils.createRemoteDebugAndQemuLaunchers(project, elem);

			if (project.hasNature(YoctoSDKAutotoolsProjectNature.YoctoSDK_AUTOTOOLS_NATURE_ID)) {
				YoctoSDKAutotoolsProjectNature.configureAutotoolsOptions(project);
			} else if (project.hasNature(YoctoSDKCMakeProjectNature.YoctoSDK_CMAKE_NATURE_ID)) {
				YoctoSDKCMakeProjectNature.extendProjectEnvironmentForCMake(project);
			}

			IConsole console = CCorePlugin.getDefault().getConsole("org.yocto.sdk.ide.YoctoConsole");
			console.start(project);
			consoleOutStream = console.getOutputStream();
			String messages = YoctoSDKMessages.getString(CONSOLE_MESSAGE);
			consoleOutStream.write(messages.getBytes());
		} catch (CoreException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (YoctoGeneralException e) {
			System.out.println(e.getMessage());
		} finally {
			if (consoleOutStream != null) {
				try {
					consoleOutStream.flush();
					consoleOutStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	/* Save profiles and selected profile to the project's preference store */
	public static void saveProfiles(YoctoProfileElement profileElement, IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectPreferences = projectScope.getNode(YoctoSDKUtilsConstants.PROJECT_SCOPE);

		if (projectPreferences == null) {
			return;
		}

		projectPreferences.put(PreferenceConstants.PROFILES, profileElement.getProfilesAsString());
		projectPreferences.put(PreferenceConstants.SELECTED_PROFILE, profileElement.getSelectedProfile());

		try {
			projectPreferences.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void saveUseProjectSpecificOption(IProject project, boolean useProjectSpecificSetting) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(YoctoSDKUtilsConstants.PROJECT_SCOPE);
		if (projectNode == null) {
			return;
		}

		if (useProjectSpecificSetting) {
			projectNode.put(PreferenceConstants.PROJECT_SPECIFIC_PROFILE, IPreferenceStore.TRUE);
		} else {
			projectNode.put(PreferenceConstants.PROJECT_SPECIFIC_PROFILE, IPreferenceStore.FALSE);
		}

		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
}

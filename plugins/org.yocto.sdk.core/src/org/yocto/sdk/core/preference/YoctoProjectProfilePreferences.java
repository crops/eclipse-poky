/*******************************************************************************
 * Copyright (c) 2018 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.yocto.sdk.core.preference;

import java.io.File;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.yocto.sdk.core.YoctoProjectEnvironmentSetupScript;
import org.yocto.sdk.core.internal.Activator;

/**
 * The primary data class which encapsulates all configurable preferences within
 * a profile, and backed by a persistent preference store with configurable
 * scope (e.g. workspace profile preference, or project-specific profile
 * preference) .
 *
 * Convenient methods are provided to convert preferences read from the store
 * into their respective data types.
 *
 * Additional utility methods which computes derived settings (e.g. environment
 * setup script and environment variables) based on the preferences can also be
 * found here.
 *
 * @author Intel Corporation
 *
 */
public class YoctoProjectProfilePreferences {

	public static final String USE_CONTAINER = "useContainer"; //$NON-NLS-1$
	public static final String TOOLCHAIN = "toolchainRadio"; //$NON-NLS-1$
	public static final String TOOLCHAIN_SDK_INSTALLATION = "sdkInstallation"; //$NON-NLS-1$
	public static final String TOOLCHAIN_BUILD_DIRECTORY = "buildDirectory"; //$NON-NLS-1$
	public static final String SDK_INSTALLATION = "sdkInstallation"; //$NON-NLS-1$
	public static final String BUILD_DIRECTORY = "buildDirectory"; //$NON-NLS-1$
//	public static final String SYSROOT_LOCATION = "sysrootLocation"; //$NON-NLS-1$
	public static final String TARGET = "targetRadio"; //$NON-NLS-1$
	public static final String TARGET_QEMU = "qemu"; //$NON-NLS-1$
	public static final String TARGET_EXTERNAL_HARDWARE = "externalHardware"; //$NON-NLS-1$
	public static final String QEMUBOOTCONF_FILE = "qemubootconfFile"; //$NON-NLS-1$
	public static final String KERNEL_IMAGE = "kernelImage"; //$NON-NLS-1$
	public static final String RUNQEMU_ARGUMENTS = "runqemuArguments"; //$NON-NLS-1$

	IPersistentPreferenceStore store;

	String profile;

	YoctoProjectEnvironmentSetupScript envSetupScript = null;

	// TODO: reuse in project preference
	public static void initializeDefaults(IPersistentPreferenceStore store) {
		store.setDefault(USE_CONTAINER, false);
		store.setDefault(TOOLCHAIN, ""); //$NON-NLS-1$
		store.setDefault(SDK_INSTALLATION, ""); //$NON-NLS-1$
		store.setDefault(BUILD_DIRECTORY, ""); //$NON-NLS-1$
//		store.setDefault(SYSROOT_LOCATION, ""); //$NON-NLS-1$
		store.setDefault(TARGET, ""); //$NON-NLS-1$
		store.setDefault(QEMUBOOTCONF_FILE, ""); //$NON-NLS-1$
		store.setDefault(KERNEL_IMAGE, ""); //$NON-NLS-1$
		store.setDefault(RUNQEMU_ARGUMENTS, ""); //$NON-NLS-1$
	}

	// TODO: reuse in project preference
	public static void initializeValues(IPersistentPreferenceStore store) {
		store.setValue(USE_CONTAINER, false);
		store.setValue(TOOLCHAIN, TOOLCHAIN_SDK_INSTALLATION);
		store.setValue(SDK_INSTALLATION, ""); //$NON-NLS-1$
		store.setValue(BUILD_DIRECTORY, ""); //$NON-NLS-1$
//		store.setValue(SYSROOT_LOCATION, ""); //$NON-NLS-1$
		store.setValue(TARGET, TARGET_EXTERNAL_HARDWARE);
		store.setValue(QEMUBOOTCONF_FILE, ""); //$NON-NLS-1$
		store.setValue(KERNEL_IMAGE, ""); //$NON-NLS-1$
		store.setValue(RUNQEMU_ARGUMENTS, ""); //$NON-NLS-1$
	}

	/**
	 * Return the matching preference store for the given profile name
	 *
	 * @param profile
	 * @return
	 */
	public static IPersistentPreferenceStore getPreferenceStore(String profile) {

		if (profile != null && profile.length() > 0) {
			String qualifier = Activator.PLUGIN_ID + "." + profile.hashCode(); //$NON-NLS-1$
			IPersistentPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, qualifier);
			initializeDefaults(store);
			return store;
		}

		return null;
	}

	/**
	 * Create a pristine preference store with defaults initialized
	 *
	 * @param profile
	 * @return
	 */
	public static IPersistentPreferenceStore createPreferenceStore(String profile) {

		IPersistentPreferenceStore store = getPreferenceStore(profile);

		if (store != null) {
			initializeValues(store);
		}

		return store;
	}

	public YoctoProjectProfilePreferences(String profile) {
		this.profile = profile;
		this.store = getPreferenceStore(profile);
	}

	public YoctoProjectProfilePreferences(IPersistentPreferenceStore store) {
		this.profile = null;
		this.store = store;
	}

	public String getProfile() {
		return this.profile;
	}

	public IPersistentPreferenceStore getPreferenceStore() {
		return this.store;
	}

	public boolean isUseContainer() {
		return getPreferenceStore().getBoolean(USE_CONTAINER);
	}

	public String getToolchain() {
		return getPreferenceStore().getString(TOOLCHAIN);
	}

	public String getSdkInstallation() {
		return getPreferenceStore().getString(SDK_INSTALLATION);
	}

	public String getBuildDirectory() {
		return getPreferenceStore().getString(BUILD_DIRECTORY);
	}

//	public String getSysrootLocation() {
//		return getPreferenceStore().getString(SYSROOT_LOCATION);
//	}

	public String getTarget() {
		return getPreferenceStore().getString(TARGET);
	}

	public String getQemubootconfFile() {
		return getPreferenceStore().getString(QEMUBOOTCONF_FILE);
	}

	public String getKernelImage() {
		return getPreferenceStore().getString(KERNEL_IMAGE);
	}

	public String getRunqemuArguments() {
		return getPreferenceStore().getString(RUNQEMU_ARGUMENTS);
	}

	/**
	 * Get environment setup script based on choice of toolchain
	 *
	 * @return environment setup script
	 */
	public YoctoProjectEnvironmentSetupScript getEnvironmentSetupScript() {

		if (envSetupScript == null) {
			File toolchainDir;

			if (TOOLCHAIN_SDK_INSTALLATION.equals(getToolchain())) {
				toolchainDir = new File(getSdkInstallation());
			} else {
				toolchainDir = new File(getBuildDirectory());
			}

			envSetupScript = YoctoProjectEnvironmentSetupScript.create(toolchainDir);
		}
		return envSetupScript;
	}

}

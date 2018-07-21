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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.yocto.sdk.core.internal.Activator;

/**
 * The primary data class which encapsulates all configurable preferences
 * within a profile, and backed by a persistent preference store with
 * configurable scope (e.g. workspace profile preference, or project-specific
 * profile preference) .
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
	public static final String SYSROOT_LOCATION = "sysrootLocation"; //$NON-NLS-1$
	public static final String TARGET = "targetRadio"; //$NON-NLS-1$
	public static final String TARGET_QEMU = "qemu"; //$NON-NLS-1$
	public static final String TARGET_EXTERNAL_HARDWARE = "externalHardware"; //$NON-NLS-1$
	public static final String QEMUBOOTCONF_FILE = "qemubootconfFile"; //$NON-NLS-1$
	public static final String KERNEL_IMAGE = "kernelImage"; //$NON-NLS-1$
	public static final String RUNQEMU_ARGUMENTS = "runqemuArguments";	 //$NON-NLS-1$

	IPersistentPreferenceStore store;

	String profile;

	public static IPersistentPreferenceStore createPreferenceStore(String profile) {

		if (profile != null && profile.length() > 0) {
			String qualifier = Activator.PLUGIN_ID + "." + profile.hashCode(); //$NON-NLS-1$
			return new ScopedPreferenceStore(InstanceScope.INSTANCE, qualifier);
		}

		return null;
	}

	public YoctoProjectProfilePreferences(String profile) {
		this.profile = profile;
		this.store = createPreferenceStore(profile);
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

	public String getSysrootLocation() {
		return getPreferenceStore().getString(SYSROOT_LOCATION);
	}

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

	static final String ENVIRONMENT_SETUP_SCRIPT_PREFIX = "environment-setup-"; //$NON-NLS-1$

	/**
	 * Convenient method for discovering environment setup script from a build directory
	 *
	 * @param toolchainDir
	 * @return environment setup script, or null if none found
	 */
	public static File getEnvironmentSetupScript(File toolchainDir) {

		if (toolchainDir == null || !toolchainDir.exists() || !toolchainDir.isDirectory())
			return null;

		for (File file : toolchainDir.listFiles()) {
			// Only return the first matching file
			if (file.getName().startsWith(ENVIRONMENT_SETUP_SCRIPT_PREFIX)) {
				return file;
			}
		}
		return null;
	}

	/**
	 * Get environment setup script based on choice of toolchain
	 *
	 * @return environment setup script
	 */
	public File getEnvironmentSetupScript() {

		File toolchainDir;

		if (TOOLCHAIN_SDK_INSTALLATION.equals(getToolchain())) {
			toolchainDir = new File(getSdkInstallation());
		} else {
			toolchainDir = new File(getBuildDirectory());
		}

		return getEnvironmentSetupScript(toolchainDir);
	}

	/**
	 *
	 * @return target prefix extracted from environment setup script filename
	 */
	public String getTargetPrefix() {

		File envScript = getEnvironmentSetupScript();

		if (envScript != null)
			return envScript.getName().replaceFirst(ENVIRONMENT_SETUP_SCRIPT_PREFIX, ""); //$NON-NLS-1$

		return null;
	}

	/**
	 *
	 * @return environment variables extracted from environment setup script
	 */
	@SuppressWarnings("nls")
	public HashMap<String, String> getEnvironmentVariables() {

		HashMap<String, String> envMap = new HashMap<String, String>();

		File environmentSetupScript = getEnvironmentSetupScript();

		if (environmentSetupScript == null || !environmentSetupScript.exists() || !environmentSetupScript.isFile())
			return envMap;

		try {

			BufferedReader input = new BufferedReader(new FileReader(environmentSetupScript));

			try {
				String line = null;

				while ((line = input.readLine()) != null) {
					if (!line.startsWith("export")) {
						continue;
					}
					String sKey = line.substring("export".length() + 1, line.indexOf('='));
					String sValue = line.substring(line.indexOf('=') + 1);
					if (sValue.startsWith("\"") && sValue.endsWith("\""))
						sValue = sValue.substring(sValue.indexOf('"') + 1, sValue.lastIndexOf('"'));
					/* If PATH ending with $PATH, we need to join with current system path */
					if (sKey.equalsIgnoreCase("PATH") && (sValue.lastIndexOf("$PATH") >= 0)) {
						if (envMap.containsKey(sKey)) {
							sValue = sValue.substring(0, sValue.lastIndexOf("$PATH")) + envMap.get(sKey);
						} else {
							sValue = sValue.substring(0, sValue.lastIndexOf("$PATH")) + System.getenv("PATH");
						}
					}

					if(sValue.toUpperCase().contains("$SDKTARGETSYSROOT")) {
						String rValue = sValue.replaceAll(Matcher.quoteReplacement("$SDKTARGETSYSROOT"), envMap.get("SDKTARGETSYSROOT"));
						envMap.put(sKey, rValue) ;
					} else {
						envMap.put(sKey, sValue);
					}

					// System.out.printf("get env key %s value %s\n", sKey, sValue);
				}
			} finally {
				input.close();
			}

		} catch (IOException e) {
			throw new RuntimeException("Unable to parse environment setup script: " + environmentSetupScript.getAbsolutePath(), e);
		}

		return envMap;

	}
}

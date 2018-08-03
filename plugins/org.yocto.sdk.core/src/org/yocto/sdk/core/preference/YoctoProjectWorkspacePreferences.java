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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.yocto.sdk.core.YoctoProjectSDKVersion;
import org.yocto.sdk.core.internal.Activator;

/**
 * A workspace-wide data class (backed by workspace preference store) which
 * keeps track of all available workspace profiles
 *
 * @author Intel Corporation
 *
 */
public class YoctoProjectWorkspacePreferences {

	public static final String PROFILE = "profile"; //$NON-NLS-1$

	public static final String PROFILES = "profiles"; //$NON-NLS-1$

	public static final String PROFILE_SEPARATOR = ","; //$NON-NLS-1$

	static String OS = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$

	public static final boolean OS_LINUX = OS.indexOf("nux") >= 0; //$NON-NLS-1$

	static boolean detectedWorkspaceProfiles = false;

	public YoctoProjectWorkspacePreferences create() {
		return new YoctoProjectWorkspacePreferences();
	}

	public static IPreferenceStore getWorkspacePreferenceStore() {
		return new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID);
	}

	public static String getWorkspaceProfile() {
		return getWorkspacePreferenceStore().getString(PROFILE);
	}

	public static String[] getWorkspaceProfiles() {
		String profiles = getWorkspacePreferenceStore().getString(PROFILES);

		if (profiles == null || profiles.length() == 0)
			return new String[] {};

		return profiles.split(PROFILE_SEPARATOR);
	}

	public static void detectWorkspaceProfiles() {

		// Only auto-detect once
		if (!detectedWorkspaceProfiles) {

			// Only auto-detect when there are no profiles defined

			// TODO: change to always detect when auto-detected profiles can be
			// managed separately from manually configured profiles

			if (true || getWorkspaceProfiles().length == 0 && OS_LINUX) {

				try {
					List<Path> environmentSetupScriptPaths = Files.find(Paths.get("/opt"), 4, //$NON-NLS-1$
							(filePath, fileAttr) -> (fileAttr.isRegularFile() && filePath.getFileName().toString()
									.startsWith(YoctoProjectProfilePreferences.ENVIRONMENT_SETUP_SCRIPT_PREFIX)))
							.collect(Collectors.toList());

					ArrayList<String> profileNames = new ArrayList<String>();

					for (Path environmentSetupScriptPath : environmentSetupScriptPaths) {

						String sdkPath = environmentSetupScriptPath.getParent().toString();

						YoctoProjectSDKVersion sdkVersion = YoctoProjectSDKVersion.create(new File(sdkPath));

						String profileName = "SDK - " + (sdkVersion == null ? sdkPath //$NON-NLS-1$
								: sdkVersion.getTargetPrefix() + " " + sdkVersion.toString()); //$NON-NLS-1$

						profileNames.add(profileName);

						YoctoProjectProfilePreferences profilePreference = new YoctoProjectProfilePreferences(
								profileName);
						IPersistentPreferenceStore store = profilePreference.getPreferenceStore();

						store.setValue(YoctoProjectProfilePreferences.TOOLCHAIN,
								YoctoProjectProfilePreferences.TOOLCHAIN_SDK_INSTALLATION);
						store.setValue(YoctoProjectProfilePreferences.SDK_INSTALLATION, sdkPath);

						String sysrootLocation = YoctoProjectProfilePreferences
								.getEnvironmentVariables(environmentSetupScriptPath.toFile())
								.get("OECORE_NATIVE_SYSROOT"); //$NON-NLS-1$

						store.setValue(YoctoProjectProfilePreferences.SYSROOT_LOCATION, sysrootLocation);
						store.setValue(YoctoProjectProfilePreferences.TARGET,
								YoctoProjectProfilePreferences.TARGET_EXTERNAL_HARDWARE);
						store.save();
					}

					setWorkspaceProfiles(profileNames.toArray(new String[] {}));

				} catch (IOException e) {
					// Ignore any errors during detection
				}
			}

			detectedWorkspaceProfiles = true;
		}
	}

	public static void setWorkspaceProfiles(String[] profiles) {
		final String NO_PROFILE = ""; //$NON-NLS-1$
		String profilesString = (profiles != null && profiles.length > 0) ? String.join(PROFILE_SEPARATOR, profiles)
				: NO_PROFILE;
		getWorkspacePreferenceStore().setValue(YoctoProjectWorkspacePreferences.PROFILES, profilesString);
	}

	public static YoctoProjectProfilePreferences getProfilePreference(String profile) {

		if (profile == null || profile.length() == 0)
			return null;

		return new YoctoProjectProfilePreferences(profile);
	}

	public static List<IProject> getProjectsByProfile(String profile) {

		List<IProject> projects = new ArrayList<IProject>();

		if (profile == null || profile.length() == 0)
			return projects;

		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (profile.equals(YoctoProjectProjectPreferences.create(project).getProfile())) {
				projects.add(project);
			}
		}

		return projects;
	}

	public static List<String> getProjectNamesByProfile(String profile) {

		List<String> projectNames = new ArrayList<String>();

		for (IProject project : getProjectsByProfile(profile)) {
			projectNames.add(project.getName());
		}

		return projectNames;
	}
}

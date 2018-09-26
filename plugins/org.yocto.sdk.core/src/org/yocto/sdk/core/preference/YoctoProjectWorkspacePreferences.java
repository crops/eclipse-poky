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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.yocto.sdk.core.YoctoProjectEnvironmentSetupScript;
import org.yocto.sdk.core.YoctoProjectQemubootConf;
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

	public static IPersistentPreferenceStore getWorkspacePreferenceStore() {
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

			// TODO: always detect the toolchain until there is a way to detect this
			// workspace is new and no auto-detection has been attempted?

			// TODO: alternatively, consider permanently enable auto-detection when
			// auto-detected profiles can be managed separately (as read-only) from manually
			// configured profiles

			if (true || OS_LINUX) {

				List<String> detectedProfiles = new ArrayList<String>();

				detectedProfiles.addAll(detectWorkspaceSdkProfiles());
				detectedProfiles.addAll(detectWorkspaceBuildDirProfiles());

				Set<String> allProfiles = new LinkedHashSet<String>(Arrays.asList(getWorkspaceProfiles()));
				allProfiles.addAll(detectedProfiles);
				setWorkspaceProfiles(allProfiles.toArray(new String[] {}));

				try {
					getWorkspacePreferenceStore().save();
				} catch (IOException e) {
					throw new RuntimeException("Problem detecting workspace profiles", e);
				}

			}

			detectedWorkspaceProfiles = true;
		}
	}

	static List<String> detectWorkspaceSdkProfiles() {

		ArrayList<String> profileNames = new ArrayList<String>();

		try {
			List<Path> environmentSetupScriptPaths = Files.find(Paths.get("/opt"), 4, //$NON-NLS-1$
					(filePath,
							fileAttr) -> (fileAttr.isRegularFile() && filePath.getFileName().toString()
									.startsWith(YoctoProjectEnvironmentSetupScript.ENVIRONMENT_SETUP_SCRIPT_PREFIX)))
					.collect(Collectors.toList());

			for (Path environmentSetupScriptPath : environmentSetupScriptPaths) {

				String sdkPath = environmentSetupScriptPath.getParent().toString();

				YoctoProjectEnvironmentSetupScript envSetupScript = YoctoProjectEnvironmentSetupScript
						.create(new File(sdkPath));
				YoctoProjectSDKVersion sdkVersion = YoctoProjectSDKVersion.create(new File(sdkPath));

				if (envSetupScript == null || sdkVersion == null)
					continue;

				String profileName = String.format("SDK %s %s", sdkVersion.getTargetPrefix(), sdkPath); //$NON-NLS-1$

				profileNames.add(profileName);

				IPersistentPreferenceStore store = YoctoProjectProfilePreferences.createPreferenceStore(profileName);

				store.setValue(YoctoProjectProfilePreferences.TOOLCHAIN,
						YoctoProjectProfilePreferences.TOOLCHAIN_SDK_INSTALLATION);
				store.setValue(YoctoProjectProfilePreferences.SDK_INSTALLATION, sdkPath);

				String sysrootLocation = envSetupScript.getEnvironmentVariables().get("OECORE_NATIVE_SYSROOT"); //$NON-NLS-1$

//				store.setValue(YoctoProjectProfilePreferences.SYSROOT_LOCATION, sysrootLocation);
				store.setValue(YoctoProjectProfilePreferences.TARGET,
						YoctoProjectProfilePreferences.TARGET_EXTERNAL_HARDWARE);
				store.save();
			}

		} catch (IOException e) {
			// Ignore any errors during detection
		}

		return profileNames;
	}

	static List<String> detectWorkspaceBuildDirProfiles() {

		ArrayList<String> profileNames = new ArrayList<String>();

		File userGitDir = new File(System.getProperty("user.home"), "git"); //$NON-NLS-1$//$NON-NLS-2$

		if (!userGitDir.exists())
			return profileNames;

		try {

			Set<Path> buildDirEnvScriptPaths = new HashSet<Path>();

			// Look for poky git repo with oe-init-build-env
			List<Path> oeInitBuildEnvPaths = Files
					.find(Paths.get(userGitDir.getAbsolutePath()), 2,
							(filePath,
									fileAttr) -> (fileAttr.isRegularFile()
											&& filePath.getFileName().toString().equals("oe-init-build-env"))) //$NON-NLS-1$
					.collect(Collectors.toList());

			// look 3 levels down from git repo's parent for anything that looks
			// like build/tmp/environment-setup-*

			// TODO: apparently this doesn't quite work as it chokes when recursing into
			// directories with permission issues.
//			List<Path> outOfBuildDirEnvScriptPaths = Files
//					.find(Paths.get(userGitDir.getParent()), 3,
//							(filePath, fileAttr) -> (fileAttr.isRegularFile() && filePath.getFileName().toString()
//									.startsWith(YoctoProjectProfilePreferences.ENVIRONMENT_SETUP_SCRIPT_PREFIX)))
//					.collect(Collectors.toList());
//
//			buildDirEnvScriptPaths.addAll(outOfBuildDirEnvScriptPaths);

			for (Path oeInitBuildEnvPath : oeInitBuildEnvPaths) {

				// look 3 levels down from git repo for anything that looks
				// like build/tmp/environment-setup-*
				List<Path> inBuildDirEnvScriptPaths = Files
						.find(oeInitBuildEnvPath.getParent(), 3, (filePath,
								fileAttr) -> (fileAttr.isRegularFile() && filePath.getFileName().toString().startsWith(
										YoctoProjectEnvironmentSetupScript.ENVIRONMENT_SETUP_SCRIPT_PREFIX)))
						.collect(Collectors.toList());

				buildDirEnvScriptPaths.addAll(inBuildDirEnvScriptPaths);
			}

			for (Path buildDirEnvScriptPath : buildDirEnvScriptPaths) {

				String buildDirPath = buildDirEnvScriptPath.getParent().toString();

				YoctoProjectEnvironmentSetupScript envSetupScript = YoctoProjectEnvironmentSetupScript
						.create(new File(buildDirPath));

				if (envSetupScript == null)
					continue;

				String profileName = String.format("Build %s %s", envSetupScript.getTargetPrefix(), buildDirPath); //$NON-NLS-1$

				profileNames.add(profileName);

				IPersistentPreferenceStore store = YoctoProjectProfilePreferences.createPreferenceStore(profileName);

				store.setValue(YoctoProjectProfilePreferences.TOOLCHAIN,
						YoctoProjectProfilePreferences.TOOLCHAIN_BUILD_DIRECTORY);
				store.setValue(YoctoProjectProfilePreferences.BUILD_DIRECTORY, buildDirPath);

				String sysrootLocation = envSetupScript.getEnvironmentVariables().get("OECORE_NATIVE_SYSROOT"); //$NON-NLS-1$

//				store.setValue(YoctoProjectProfilePreferences.SYSROOT_LOCATION, sysrootLocation);

				List<Path> qemubootConfPaths = Files
						.find(buildDirEnvScriptPath.getParent(), 4,
								(filePath,
										fileAttr) -> (fileAttr.isRegularFile() && filePath.getFileName().toString()
												.endsWith(YoctoProjectQemubootConf.QEMUBOOT_CONF_SUFFIX)))
						.collect(Collectors.toList());

				if (qemubootConfPaths.size() == 1) {

					YoctoProjectQemubootConf qemubootConf = YoctoProjectQemubootConf
							.create(qemubootConfPaths.get(0).getParent().toFile());

					if (qemubootConf != null && qemubootConf.getKernel() != null) {
						store.setValue(YoctoProjectProfilePreferences.TARGET,
								YoctoProjectProfilePreferences.TARGET_QEMU);

						store.setValue(YoctoProjectProfilePreferences.QEMUBOOTCONF_FILE,
								qemubootConf.toFile().getAbsolutePath());

						store.setValue(YoctoProjectProfilePreferences.KERNEL_IMAGE,
								qemubootConf.getKernel().getAbsolutePath());
					} else {
						store.setValue(YoctoProjectProfilePreferences.TARGET,
								YoctoProjectProfilePreferences.TARGET_EXTERNAL_HARDWARE);
					}

				} else {
					store.setValue(YoctoProjectProfilePreferences.TARGET,
							YoctoProjectProfilePreferences.TARGET_EXTERNAL_HARDWARE);
				}

				store.save();
			}

		} catch (IOException e) {
			// Ignore any errors during detection
		}

		return profileNames;
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
			if (profile.equals(YoctoProjectProjectPreferences.getProjectPreferences(project).getProfile())) {
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

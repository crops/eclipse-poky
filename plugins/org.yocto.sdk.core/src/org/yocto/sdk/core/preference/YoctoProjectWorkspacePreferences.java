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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
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
			return new String[]{};

		return profiles.split(PROFILE_SEPARATOR);
	}

	public static void setWorkspaceProfiles(String[] profiles) {
		final String NO_PROFILE = ""; //$NON-NLS-1$
		String profilesString = (profiles != null && profiles.length > 0) ? String.join(PROFILE_SEPARATOR, profiles) : NO_PROFILE;
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

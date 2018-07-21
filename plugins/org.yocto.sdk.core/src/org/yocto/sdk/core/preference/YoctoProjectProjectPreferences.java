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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.yocto.sdk.core.internal.Activator;

/**
 * A per-project data class which supports reusing workspace profile preference
 * or define project-specific profile preferences.
 *
 * This data class is backed by a project-scoped preference store which stores
 * project-specific profile preferences. When a workspace profile preference is
 * reused, the project-specific profile stored in project-scoped preference
 * store will not be accessed, instead the workspace profile preference store
 * will be used.
 *
 * @author Intel Corporation
 *
 */
public class YoctoProjectProjectPreferences {

	public static final String USE_PROJECT_SPECIFIC_SETTINGS = "useProjectSpecificSettings"; //$NON-NLS-1$

	public static final String PROJECT_PROFILE = "profile"; //$NON-NLS-1$

	IPersistentPreferenceStore projectPreferenceStore = null;

	public static YoctoProjectProjectPreferences create(IProject project) {
		return new YoctoProjectProjectPreferences(project);
	}

	public YoctoProjectProjectPreferences(IProject project) {

		if (project != null) {
			String qualifier = Activator.PLUGIN_ID + "." + project.getName(); //$NON-NLS-1$
			this.projectPreferenceStore = new ScopedPreferenceStore(new ProjectScope(project), qualifier);
		}
	}

	public IPersistentPreferenceStore getPreferenceStore() {
		return this.projectPreferenceStore;
	}

	public boolean isUseProjectSpecificSettings() {
		return getPreferenceStore().getBoolean(USE_PROJECT_SPECIFIC_SETTINGS);
	}

	public String getProfile() {
		return getPreferenceStore().getString(PROJECT_PROFILE);
	}

	public YoctoProjectProfilePreferences getProfilePreferences() {

		if (isUseProjectSpecificSettings()) {
			return new YoctoProjectProfilePreferences(getPreferenceStore());
		} else {
			String profile = getProfile();

			if (profile != null && profile.length() > 0)
				// TODO: make sure this is read-only?
				return new YoctoProjectProfilePreferences(getProfile());
			else
				return null;
		}
	}

}

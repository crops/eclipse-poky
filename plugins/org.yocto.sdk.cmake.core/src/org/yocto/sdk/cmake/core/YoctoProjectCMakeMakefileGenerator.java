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
package org.yocto.sdk.cmake.core;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.yocto.cmake.core.CMakeMakefileGenerator;
import org.yocto.sdk.cmake.core.internal.Activator;
import org.yocto.sdk.core.preference.YoctoProjectProjectPreferences;
import org.yocto.sdk.core.preference.YoctoProjectWorkspacePreferences;

public class YoctoProjectCMakeMakefileGenerator extends CMakeMakefileGenerator {

	@Override
	public MultiStatus generateMakefiles(IResourceDelta delta) throws CoreException {

		if (isValid()) {
			return super.generateMakefiles(delta);
		} else {
			throw new CoreException(new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR,
					Messages.YoctoProjectCMakeMakefileGenerator_CantGenerateMakefile,
					new Exception(computeErrorMessage())));
		}
	}

	String computeErrorMessage() {
		YoctoProjectProjectPreferences projectPreference = YoctoProjectProjectPreferences.create(getProject());

		if (projectPreference.isUseProjectSpecificSettings()) {
			// TODO: validate project specific settings
			return null;
		} else {
			String profile = projectPreference.getProfile();

			if (profile == null || profile.length() == 0) {
				if (YoctoProjectWorkspacePreferences.getWorkspaceProfiles().length == 0) {
					return Messages.YoctoProjectCMakeMakefileGenerator_CreateProfileBeforeUse;
				} else {
					return Messages.YoctoProjectCMakeMakefileGenerator_SelectProfileToUse;
				}
			} else {
				String[] workspaceProfiles = YoctoProjectWorkspacePreferences.getWorkspaceProfiles();

				if (workspaceProfiles.length == 0) {
					return String.format(Messages.YoctoProjectCMakeMakefileGenerator_NoWorkspaceProfilesToMatch,
							profile);
				} else {
					for (String workspaceProfile : workspaceProfiles) {
						if (profile.equals(workspaceProfile))
							return null;
					}

					return String.format(Messages.YoctoProjectCMakeMakefileGenerator_NoSuchSelectedProfile, profile);
				}
			}
		}
	}

	boolean isValid() {
		return (computeErrorMessage() == null || computeErrorMessage().length() == 0);
	}
}

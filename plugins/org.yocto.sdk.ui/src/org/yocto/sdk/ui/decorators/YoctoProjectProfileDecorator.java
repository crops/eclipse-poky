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
package org.yocto.sdk.ui.decorators;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.yocto.sdk.core.YoctoProjectNature;
import org.yocto.sdk.core.preference.YoctoProjectProfilePreferences;
import org.yocto.sdk.core.preference.YoctoProjectProjectPreferences;
import org.yocto.sdk.ui.internal.Activator;

/**
 * Yocto Project profile decorator for display visual cues such as target
 * architecture and selected cross compilation profile
 *
 * @author Intel Corporation
 *
 */
public class YoctoProjectProfileDecorator implements ILightweightLabelDecorator {

	public static final String ID = Activator.PLUGIN_ID + ".profileDecorator"; //$NON-NLS-1$

	@Override
	public void addListener(ILabelProviderListener listener) {
		// Unsupported

	}

	@Override
	public void dispose() {
		// Nothing to do

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// Unsupported

	}

	@Override
	public void decorate(Object element, IDecoration decoration) {

		if (element instanceof IProject) {
			IProject project = (IProject) element;

			try {

				if (!project.isOpen())
					return;

				if (!project.hasNature(YoctoProjectNature.NATURE_ID))
					return;

				YoctoProjectProjectPreferences projectPreference = YoctoProjectProjectPreferences.getProjectPreferences(project);

				if (projectPreference == null)
					return;

				YoctoProjectProfilePreferences profilePreferences = projectPreference.getProfilePreferences();

				if (profilePreferences == null)
					return;

				String profile = YoctoProjectProjectPreferences.getProjectPreferences(project).getProfile();
				decoration.addSuffix(" [" + profile + "]"); //$NON-NLS-1$ //$NON-NLS-2$

			} catch (CoreException e) {
				// Do nothing if project does not exist or project is closed
			}
		}
	}

}

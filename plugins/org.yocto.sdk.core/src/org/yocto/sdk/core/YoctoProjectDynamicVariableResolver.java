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
package org.yocto.sdk.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

public class YoctoProjectDynamicVariableResolver implements IDynamicVariableResolver {

	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {

//		if ("sysroot".equals(variable.getName())) {
//		if (argument != null && argument.length() > 0) {
//			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(argument);
//
//			if (!ProjectPreferenceUtils.getUseProjectSpecificOption(project)) {
//				YoctoProfileElement e = ProjectPreferenceUtils.getProfiles(project);
//				IPreferenceStore store = YoctoSDKPlugin.getProfilePreferenceStore(e.getSelectedProfile());
//				YoctoUIElement elem = YoctoSDKUtils.getElemFromStore(store);
//				String sysroot = elem.getStrSysrootLoc();
//				return sysroot;
//			} else {
//				// Unsupported
//			}
//
//		} else {
//			throw new CoreException(new Status(IStatus.ERROR, "org.yocto.sdk.ide", "Missing argument for variable: " + variable.getName()));
//		}
//	}
		return null;
	}

}

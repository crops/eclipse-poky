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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.yocto.sdk.cmake.core.messages"; //$NON-NLS-1$
	public static String YoctoProjectCMakeMakefileGenerator_CantGenerateMakefile;
	public static String YoctoProjectCMakeMakefileGenerator_CreateProfileBeforeUse;
	public static String YoctoProjectCMakeMakefileGenerator_NoSuchSelectedProfile;
	public static String YoctoProjectCMakeMakefileGenerator_NoWorkspaceProfilesToMatch;
	public static String YoctoProjectCMakeMakefileGenerator_SelectProfileToUse;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}

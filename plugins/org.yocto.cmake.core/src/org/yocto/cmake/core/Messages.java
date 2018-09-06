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
package org.yocto.cmake.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.yocto.cmake.core.messages"; //$NON-NLS-1$
	public static String CMakeMakefileGenerator_NotGeneratingBuildFiles;
	public static String CMakeMakefileGenerator_CreateDirectoryFailed;
	public static String CMakeMakefileGenerator_GeneratingBuildFiles;
	public static String CMakeMakefileGenerator_GetCMakeToolCommandFlagsFailed;
	public static String CMakeMakefileGenerator_LauncherCreateProcessFailed;
	public static String CMakeMakefileGenerator_ProcessExitCodeNonZero;
	public static String CMakeMakefileGenerator_CMakeConsoleWriteFailed;
	public static String NewCMakeProjectProcess_ProjectCreationFailed;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}

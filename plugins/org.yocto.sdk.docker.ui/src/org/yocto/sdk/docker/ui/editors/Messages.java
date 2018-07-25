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
package org.yocto.sdk.docker.ui.editors;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.yocto.sdk.ui.editors.messages"; //$NON-NLS-1$
	public static String YoctoProjectProfileComboFieldEditor_CreateNewProfile;
	public static String YoctoProjectProfileComboFieldEditor_EnterNewProfileName;
	public static String YoctoProjectProfileComboFieldEditor_EnterProfileNameForRenaming;
	public static String YoctoProjectProfileComboFieldEditor_NeedDifferentProfileNameForRenaming;
	public static String YoctoProjectProfileComboFieldEditor_NeedNewProfileName;
	public static String YoctoProjectProfileComboFieldEditor_NeedProfileNameForRenaming;
	public static String YoctoProjectProfileComboFieldEditor_NeedUniqueNewProfileName;
	public static String YoctoProjectProfileComboFieldEditor_NeedUniqueProfileNameForRenaming;
	public static String YoctoProjectProfileComboFieldEditor_NoIllegalCharacterInNewProfileName;
	public static String YoctoProjectProfileComboFieldEditor_RenameProfile;
	public static String YoctoProjectProfileComboFieldEditor_UnableToRemoveProfileInUse;
	public static String YoctoProjectProfileComboFieldEditor_UnableToRenameProfileInUse;
	public static String YoctoProjectProfileComposedEditor_BuildAndLaunchWithContainer;
	public static String YoctoProjectProfileComposedEditor_BuildDirectory;
	public static String YoctoProjectProfileComposedEditor_BuildDirectoryMissingEnvSetupScript;
	public static String YoctoProjectProfileComposedEditor_ExternalHardware;
	public static String YoctoProjectProfileComposedEditor_KernelImage;
	public static String YoctoProjectProfileComposedEditor_KernelImageNotFile;
	public static String YoctoProjectProfileComposedEditor_NeedBuildDirectory;
	public static String YoctoProjectProfileComposedEditor_NeedKernelImage;
	public static String YoctoProjectProfileComposedEditor_NeedQemuconf;
	public static String YoctoProjectProfileComposedEditor_NeedSdkInstallation;
	public static String YoctoProjectProfileComposedEditor_NeedSysroot;
	public static String YoctoProjectProfileComposedEditor_NoSuchKernelImage;
	public static String YoctoProjectProfileComposedEditor_NoSuchQemubootconf;
	public static String YoctoProjectProfileComposedEditor_NoSuchSysroot;
	public static String YoctoProjectProfileComposedEditor_QEMU;
	public static String YoctoProjectProfileComposedEditor_QemubootconfFile;
	public static String YoctoProjectProfileComposedEditor_QemubootconfNotFile;
	public static String YoctoProjectProfileComposedEditor_RunqemuArguments;
	public static String YoctoProjectProfileComposedEditor_SdkInstallation;
	public static String YoctoProjectProfileComposedEditor_SdkInstallationMissingEnvSetupScript;
	public static String YoctoProjectProfileComposedEditor_SelectTargetMode;
	public static String YoctoProjectProfileComposedEditor_SelectToolchainMode;
	public static String YoctoProjectProfileComposedEditor_Sysroot;
	public static String YoctoProjectProfileComposedEditor_SysrootNotDirectory;
	public static String YoctoProjectProfileComposedEditor_Target;
	public static String YoctoProjectProfileComposedEditor_Toolchain;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}

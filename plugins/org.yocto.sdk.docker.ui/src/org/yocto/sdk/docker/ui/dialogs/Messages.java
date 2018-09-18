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
package org.yocto.sdk.docker.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.yocto.sdk.docker.ui.dialogs.messages"; //$NON-NLS-1$
	public static String YoctoProjectPreferencePage_CrossDevelopmentProfile;
	public static String YoctoProjectPreferencePage_Profile;
	public static String YoctoProjectPreferencePage_NoProfilesFound;
	public static String YoctoProjectPreferencePage_SavePreferenceStoreFailed;
	public static String YoctoProjectPropertyPage_CrossDevelopmentProfile;
	public static String YoctoProjectPropertyPage_Profile;
	public static String YoctoProjectPropertyPage_ManageProfiles;
	public static String YoctoProjectPropertyPage_MustDisableProfileContainerBuild;
	public static String YoctoProjectPropertyPage_MustEnableProfileContainerBuild;
	public static String YoctoProjectPropertyPage_MustSelectProfileWithContainerBuild;
	public static String YoctoProjectPropertyPage_MustSelectProfileWithoutContainerBuild;
	public static String YoctoProjectPropertyPage_NoProfileSelected;
	public static String YoctoProjectPropertyPage_NoProfilesFound;
	public static String YoctoProjectPropertyPage_UseProjectSpecificSettings;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}

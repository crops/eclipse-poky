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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YoctoProjectSDKVersion {

	public static final String VERSION_FILE_PREFIX = "version-"; //$NON-NLS-1$

	public static final String PROPERTY_DISTRO = "Distro"; //$NON-NLS-1$

	public static final String PROPERTY_DISTRO_VERSION = "Distro Version"; //$NON-NLS-1$

	public static final String PROPERTY_METADATA_REVISION = "Metadata Revision"; //$NON-NLS-1$

	public static final String PROPERTY_TIMESTAMP = "Timestamp"; //$NON-NLS-1$

	public static File getVersionFile(File sdkDir) {

		if (sdkDir == null || !sdkDir.exists() || !sdkDir.isDirectory())
			return null;

		for (File file : sdkDir.listFiles()) {
			// Only return the first matching file
			if (file.getName().startsWith(VERSION_FILE_PREFIX)) {
				return file;
			}
		}
		return null;
	}

	public static YoctoProjectSDKVersion create(File sdkDir) {

		File file = getVersionFile(sdkDir);

		if (file != null)
			return new YoctoProjectSDKVersion(file);
		else
			return null;
	}

	private Map<String, String> propertiesMap = new HashMap<String, String>();

	private String targetPrefix;

	YoctoProjectSDKVersion(File versionFile) {

		targetPrefix = versionFile.getName().replaceFirst(VERSION_FILE_PREFIX, ""); //$NON-NLS-1$

		BufferedReader input;
		try {
			input = new BufferedReader(new FileReader(versionFile));

			String line = null;

			while ((line = input.readLine()) != null) {
				String[] segment = line.split(": "); //$NON-NLS-1$
				propertiesMap.put(segment[0], segment[1]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public String toString() {

		List<String> list = new ArrayList<String>();

//		if (propertiesMap.containsKey(PROPERTY_DISTRO))
//			list.add(propertiesMap.get(PROPERTY_DISTRO));

		if (propertiesMap.containsKey(PROPERTY_DISTRO_VERSION))
			list.add(propertiesMap.get(PROPERTY_DISTRO_VERSION));
//
//		if (propertiesMap.containsKey(PROPERTY_METADATA_REVISION))
//			list.add(propertiesMap.get(PROPERTY_METADATA_REVISION));
//
//		if (propertiesMap.containsKey(PROPERTY_TIMESTAMP))
//			list.add(propertiesMap.get(PROPERTY_TIMESTAMP));

		return String.join(" ", list); //$NON-NLS-1$
	}

	public String getTargetPrefix() {
		return targetPrefix;
	}

}

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class YoctoProjectQemubootConf {

	public static final String PROPERTY_QB_DEFAULT_KERNEL = "qb_default_kernel"; //$NON-NLS-1$

	public static final String QEMUBOOT_CONF_SUFFIX = ".qemuboot.conf"; //$NON-NLS-1$

	/**
	 * Find qemuboot.conf file within the specified directory
	 *
	 * @param deployDirImageDir directory as specified in DEPLOY_DIR_IMAGE (see
	 *                          https://www.yoctoproject.org/docs/latest/mega-manual/mega-manual.html#var-DEPLOY_DIR_IMAGE)
	 * @return
	 */
	public static File getQemubootConf(File deployDirImageDir) {

		if (deployDirImageDir == null || !deployDirImageDir.exists() || !deployDirImageDir.isDirectory())
			return null;

		for (File file : deployDirImageDir.listFiles()) {
			// Only return the first matching qemuboot symbolic link
			if (Files.isSymbolicLink(file.toPath()) && file.getName().endsWith(QEMUBOOT_CONF_SUFFIX)) {
				return file;
			}
		}

		for (File file : deployDirImageDir.listFiles()) {
			// Only return the first matching qemuboot file
			if (!Files.isSymbolicLink(file.toPath()) && file.getName().endsWith(QEMUBOOT_CONF_SUFFIX)) {
				return file;
			}
		}

		return null;
	}

	public static YoctoProjectQemubootConf create(File deployDirImageDir) {

		File file = getQemubootConf(deployDirImageDir);

		if (file != null)
			return new YoctoProjectQemubootConf(file);

		return null;
	}

	Map<String, String> propertiesMap = new HashMap<String, String>();

	File qemubootConfFile;

	YoctoProjectQemubootConf(File qemubootConfFile) {

		this.qemubootConfFile = qemubootConfFile;

		BufferedReader input;
		try {
			input = new BufferedReader(new FileReader(qemubootConfFile));

			String line = null;

			while ((line = input.readLine()) != null) {
				if (line.contains(" = ")) { //$NON-NLS-1$
					String[] segment = line.split(" = "); //$NON-NLS-1$
					this.propertiesMap.put(segment[0], segment[1]);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public File toFile() {
		return this.qemubootConfFile;
	}

	public File getKernel() {

		if (!this.propertiesMap.containsKey(PROPERTY_QB_DEFAULT_KERNEL))
			return null;

		// Get QB_DEFAULT_KERNEL path from qemuboot.conf
		File qbDefaultKernel = new File(this.propertiesMap.get(PROPERTY_QB_DEFAULT_KERNEL));

		// Convert QB_DEFAULT_KERNEL path to absolute
		if (!qbDefaultKernel.isAbsolute())
			qbDefaultKernel = new File(this.qemubootConfFile.getParent(), qbDefaultKernel.getPath());

		// Get the qemuboot.conf link which is pointing to QB_DEFAULT_KERNEL
		try {
			List<Path> symbolicLinks = Files
					.find(qbDefaultKernel.getParentFile().toPath(), 1,
							(filePath, fileAttr) -> (Files.isSymbolicLink(filePath))) // $NON-NLS-1$
					.collect(Collectors.toList());

			for (Path symbolicLink : symbolicLinks) {

				File symbolicLinkTarget = Files.readSymbolicLink(symbolicLink).toFile();

				if (!symbolicLinkTarget.isAbsolute())
					symbolicLinkTarget = new File(symbolicLink.getParent().toFile().getAbsolutePath(),
							symbolicLinkTarget.getPath());

				if (symbolicLinkTarget.equals(qbDefaultKernel.getCanonicalFile()))
					return symbolicLink.toFile();
			}
		} catch (IOException e) {
			// ignore errors
		}
		// just return QB_DEFAULT_KERNEL if we can't determine the symbolic link
		return qbDefaultKernel;
	}
}

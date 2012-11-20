/*****************************************************************************
 * Copyright (c) 2009 Ken Gilmer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ken Gilmer - initial API and implementation
 *******************************************************************************/
package org.yocto.bc.bitbake;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.yocto.bc.remote.utils.RemoteHelper;
import org.yocto.bc.ui.model.ProjectInfo;

/**
 * A helper class for ProjectInfo related tasks.
 * 
 * @author kgilmer
 * 
 */
public class ProjectInfoHelper {

	protected static final String DEFAULT_INIT_SCRIPT = "oe-init-build-env";
	/**
	 * @param path
	 * @return The path to bitbake init script
	 * @throws IOException
	 */
	public static String getInitScriptPath(URI uri) throws IOException {
		String val = uri.getPath() + "/" + DEFAULT_INIT_SCRIPT;
//		URI u;
//		try {
//			u = new URI(uri.getScheme(), uri.getHost(), uri.getPath() + ".eclipse-data", uri.getFragment());
//			File inFile = new File(u);
//			if(inFile.exists()) {
//				BufferedReader br = new BufferedReader(new FileReader(inFile));
//				val = br.readLine();
//				br.close();
//			}
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
		return val;
	}
	
//	public static String getInitScript(String path) throws IOException {
//		File inFile = new File(path);
//		BufferedReader br = new BufferedReader(new FileReader(inFile));
//		StringBuffer sb = new StringBuffer();
//		String line = null;
//		
//		while ((line = br.readLine()) != null) {
//			sb.append(line);
//		}
//		
//		br.close();
//
//		return sb.toString();
//	}

	public static String getProjectName(URI projectRoot) {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; ++i) {
			try {
				if (projects[i].getLocationURI().getPath().equals(projectRoot)) {
					return projects[i].getName();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * This method will store the path to the bitbake init script for future
	 * reference.
	 * 
	 * @param path
	 * @param projInfo
	 * @throws IOException
	 */
	public static void store(IHost connection, URI path, ProjectInfo projInfo, IProgressMonitor monitor) throws IOException {
		writeToFile(connection, path, projInfo.getInitScriptPath(), monitor);
	}

	private static void writeToFile(IHost connection, URI uri, String localInit, IProgressMonitor monitor) throws IOException {
		try {
//			URI u;
//			String remoteInit;
//			if (uri.getPath() != null) {
//				u = new URI(uri.getScheme(), uri.getPath() + ".eclipse-data", uri.getFragment());
//				remoteInit =  uri.getPath() + ".eclipse-data";
//			} else {
//				u = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), uri.getFragment() + ".eclipse-data");
//				remoteInit =  uri.getFragment() + ".eclipse-data";
//			}
//			System.out.println(u.toString());
//			RSEHelper.putRemoteFile(connection, localInit, remoteInit, monitor);
////			File outFile = new File(u);
//			FileOutputStream fos = new FileOutputStream(outFile);
//
//			fos.write(init.getBytes());
//
//			fos.flush();
//			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}

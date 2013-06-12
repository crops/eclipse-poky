/*****************************************************************************
 * Copyright (c) 2013 Ken Gilmer, Intel Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ken Gilmer - initial API and implementation
 *     Ioana Grigoropol (Intel) - adapt class for remote support
 *******************************************************************************/
package org.yocto.bc.ui;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import org.yocto.bc.bitbake.BBRecipe;
import org.yocto.bc.bitbake.BBSession;
import org.yocto.bc.bitbake.ProjectInfoHelper;
import org.yocto.bc.bitbake.ShellSession;
import org.yocto.bc.ui.model.ProjectInfo;
import org.yocto.bc.ui.wizards.newproject.CreateBBCProjectOperation;
import org.yocto.remote.utils.RemoteHelper;

public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.yocto.bc.ui";
	public static final String IMAGE_VARIABLE = "IMAGE_VARIABLE";
	public static final String IMAGE_FUNCTION = "IMAGE_FUNCTION";

	// The shared instance
	private static Activator plugin;
	private static Map shellMap;
	private static Map<URI, ProjectInfo> projInfoMap;
	private static Hashtable bbSessionMap;
	private static Hashtable bbRecipeMap;

	private IResourceChangeListener listener = new BCResourceChangeListener();

	public static BBRecipe getBBRecipe(BBSession session, URI fileURI) throws IOException {
		if (bbRecipeMap == null) {
			bbRecipeMap = new Hashtable();
		}

		URI key = session.getProjInfoRoot();
		BBRecipe recipe = (BBRecipe) bbRecipeMap.get(key);
		if (recipe == null) {
			recipe = new BBRecipe(session, fileURI);
			bbRecipeMap.put(key, recipe);
		}

		return recipe;
	}
	
	/**
	 * Get or create a BitBake session passing in ProjectInfo
	 * @param pinfo
	 * @return
	 * @throws IOException
	 */
	public static BBSession getBBSession(ProjectInfo projectInfo, Writer out, IProgressMonitor monitor) throws IOException {
		URI projectRoot = projectInfo.getOriginalURI();
		if (bbSessionMap == null) {
			bbSessionMap = new Hashtable();
		}
		
		BBSession bbs = (BBSession) bbSessionMap.get(projectRoot);
		
		if (bbs == null) {
			bbs = new BBSession(getShellSession(projectInfo, out, monitor), projectRoot);
			bbSessionMap.put(projectRoot, bbs);
		}
		
		return bbs;
	}
	
	/**
	 * Get or create a BitBake session passing in ProjectInfo
	 * @param pinfo
	 * @return
	 * @throws IOException
	 */
	public static BBSession getBBSession(ProjectInfo projectInfo, IProgressMonitor monitor) throws Exception {
		URI projectRoot = projectInfo.getOriginalURI();
		if (bbSessionMap == null) {
			bbSessionMap = new Hashtable();
		}
		
		BBSession bbs = (BBSession) bbSessionMap.get(projectRoot);
		
		if (bbs == null) {
			bbs = new BBSession(getShellSession(projectInfo, null, monitor), projectRoot);
			bbSessionMap.put(projectRoot, bbs);
		}
		
		return bbs;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static ProjectInfo getProjInfo(URI location) throws CoreException, InvocationTargetException, InterruptedException {
		if (projInfoMap == null) {
			projInfoMap = new Hashtable<URI, ProjectInfo>();
		}
		if (location != null) {
			ProjectInfo pi = projInfoMap.get(location);
			if (pi == null) {
				pi = new ProjectInfo();
				pi.setLocationURI(location);
				try {
					pi.setInitScriptPath(ProjectInfoHelper.getInitScriptPath(location));
				} catch (IOException e) {
					throw new InvocationTargetException(e);
				}

				projInfoMap.put(location, pi);
			}
			return pi;
		}
		return null;
	}

	public static void notifyAllBBSession(IResource[] added, IResource[] removed, IResource[] changed) {
		Iterator iter;
		if(bbRecipeMap != null) {
			iter = bbRecipeMap.values().iterator();
			while(iter.hasNext()) {
				BBRecipe p = (BBRecipe)iter.next();
				p.changeNotified(added, removed, changed);
			}
		}

		if(bbSessionMap != null) {
			iter= bbSessionMap.values().iterator();
			while(iter.hasNext()) {
				BBSession p = (BBSession)iter.next();
				p.changeNotified(added, removed, changed);
			}
		}
	}

	/**
	 * @param absolutePath
	 * @return a cached shell session for a given project root.
	 * @throws IOException 
	 */
	private static ShellSession getShellSession(ProjectInfo projInfo, Writer out, IProgressMonitor monitor) throws IOException {
		URI absolutePath = projInfo.getOriginalURI();
		if (shellMap == null) {
			shellMap = new Hashtable();
		}
		
		ShellSession ss = (ShellSession) shellMap.get(absolutePath);
		
		if (ss == null) {
			IHostFile remoteHostFile = RemoteHelper.getRemoteHostFile(projInfo.getConnection(), absolutePath.getPath(), monitor);
			ss = new ShellSession(projInfo, remoteHostFile, ProjectInfoHelper.getInitScriptPath(absolutePath));
		}
		
		return ss;
	}

	public static void putProjInfo(URI location, ProjectInfo pinfo) {
		if (projInfoMap == null) {
			projInfoMap = new Hashtable();
		}
		
		
		
		projInfoMap.put(location, pinfo);
	}
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
			      listener, IResourceChangeEvent.POST_CHANGE);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(
			      listener);
		plugin = null;
		super.stop(context);
	}

	/**
	 * Reset a configuration
	 * @param path
	 */
	public static void resetBBSession(String path) {
		shellMap.remove(path);
		bbSessionMap.remove(path);
	}

	protected void initializeImageRegistry(ImageRegistry reg) {
		reg.put(IMAGE_VARIABLE, Activator.getImageDescriptor("icons/variable.gif"));
		reg.put(IMAGE_FUNCTION, Activator.getImageDescriptor("icons/function.gif"));
    }
}

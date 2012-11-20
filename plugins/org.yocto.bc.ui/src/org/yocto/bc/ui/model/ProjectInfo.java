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
package org.yocto.bc.ui.model;

import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.files.IFileService;
import org.yocto.bc.remote.utils.RemoteHelper;


public class ProjectInfo implements IModelElement {
	private String name;
	private URI location;
	private String init;
	private IHost connection;
	private IRemoteServices remoteServices;
	
	public ProjectInfo() {
	}
	
	public String getInitScriptPath() {
		return init;
	}
	public String getProjectName() {
		return name;
	}
	public URI getURI() {
		return location;
	}
	public void initialize() throws Exception {
		name = new String();
		location = new URI("");
		init = new String();
	}

	public void setInitScriptPath(String init) {
		this.init = init;
	}

	public void setLocation(URI location) {
		this.location = location;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public IHost getConnection() {
		return connection;
	}

	public void setConnection(IHost connection) {
		this.connection = connection;
	}

	public IRemoteServices getRemoteServices() {
		return remoteServices;
	}

	public void setRemoteServices(IRemoteServices remoteServices) {
		this.remoteServices = remoteServices;
	}
	
	public IFileService getFileService(IProgressMonitor monitor){
		try {
			return (IFileService)RemoteHelper.getConnectedRemoteFileService(connection, monitor);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}

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
package org.yocto.bc.ui.wizards.newproject;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.yocto.bc.bitbake.BBSession;
import org.yocto.bc.bitbake.ProjectInfoHelper;
import org.yocto.bc.remote.utils.RemoteHelper;
import org.yocto.bc.ui.Activator;
import org.yocto.bc.ui.model.ProjectInfo;

public class BBConfigurationInitializeOperation implements IRunnableWithProgress {

	private final ProjectInfo pinfo;
	private final Writer writer;
	private boolean errorOccured = false;
	private String errorMessage = "";

	public boolean isErrorOccured() {
		return errorOccured;
	}

	public void setErrorOccured(boolean errorOccured) {
		this.errorOccured = errorOccured;
	}

	public BBConfigurationInitializeOperation(ProjectInfo pinfo) {
		this.pinfo = pinfo;
		writer = null;
	}

	public BBConfigurationInitializeOperation(ProjectInfo pinfo, Writer writer) {
		this.pinfo = pinfo;
		this.writer = writer;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		BBSession session;
		try {
			System.out.println("Initialize bitbake session ...");
			monitor.beginTask("Initialize bitbake session ...", RemoteHelper.TOTALWORKLOAD);
			ProjectInfoHelper.store(RemoteHelper.getRemoteConnectionByName(pinfo.getConnection().getName()), pinfo.getOriginalURI(), pinfo, monitor);
			session = Activator.getBBSession(pinfo, writer, monitor);
			session.initialize();
			monitor.worked(90);
			monitor.done();
			errorOccured = session.errorOccured();
			errorMessage = session.getErrorLines();
			if (!errorOccured) {
				System.out.println("Bitbake session initialized successfully.");
				errorMessage = "";
			} else 
				System.out.println("An error occured and Bitbake session was not initialized.");
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}

/*******************************************************************************
 * Copyright (c) 2012 Intel Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel - initial API and implementation
 *******************************************************************************/
package org.yocto.remote.sdk.ui.templates;

import java.net.URI;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.core.templateengine.process.processes.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.rse.core.model.IHost;
import org.yocto.remote.sdk.core.RemoteAutotoolsNewProjectNature;
import org.yocto.remote.utils.RemoteHelper;
import org.yocto.sdk.ide.YoctoSDKMessages;

public class NewRemoteAutotoolsYPPostProcess extends ProcessRunner {

	public static final String CHMOD_COMMAND = "chmod +x "; //$NON-NLS-1$
	public static final String AUTOGEN_SCRIPT_NAME = "autogen.sh"; //$NON-NLS-1$

	public NewRemoteAutotoolsYPPostProcess() {}

	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {

		String projectName = args[0].getSimpleValue();

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		try {
			if (!project.exists()) {
				throw new ProcessFailureException(Messages.getString("NewManagedProject.4") + projectName); //$NON-NLS-1$
			} else if (!project.hasNature(RemoteAutotoolsNewProjectNature.YoctoSDK_AUTOTOOLS_NATURE_ID)) {
				throw new ProcessFailureException(Messages.getString("NewManagedProject.3") + //$NON-NLS-1$
						YoctoSDKMessages.getFormattedString("AutotoolsProjectPostProcess.WrongProjectNature", //$NON-NLS-1$
								projectName));
			} else {
				URI uri = project.getLocationURI();
				IRemoteConnection remConn = RemoteHelper.getConnectionByURI(uri);
				IHost host = RemoteHelper.getRemoteConnectionByName(remConn.getName());
				RemoteHelper.remoteShellExec(host, "", "/bin/sh", "-c \"" + CHMOD_COMMAND  + uri.getPath() + "/" + AUTOGEN_SCRIPT_NAME + "\"", monitor);
			}
		} catch (Exception e) {
			throw new ProcessFailureException(Messages.getString("NewManagedProject.3") + e.getMessage(), e); //$NON-NLS-1$
		}
	}
}

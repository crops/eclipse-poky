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
package org.yocto.sdk.ide.wizard;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.core.templateengine.process.processes.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.IProgressMonitor;
import org.yocto.sdk.ide.YoctoSDKMessages;
import org.yocto.sdk.ide.natures.YoctoSDKAutotoolsProjectNature;

public class NewYoctoAutotoolsProjectPostProcess extends ProcessRunner {

	public static final String CHMOD_COMMAND = "chmod +x "; //$NON-NLS-1$
	public static final String AUTOGEN_SCRIPT_NAME = "autogen.sh"; //$NON-NLS-1$

	public NewYoctoAutotoolsProjectPostProcess() {}

	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {

		String projectName = args[0].getSimpleValue();

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		try {
			if (!project.exists()) {
				throw new ProcessFailureException(Messages.getString("NewManagedProject.4") + projectName); //$NON-NLS-1$
			} else if (!project.hasNature(YoctoSDKAutotoolsProjectNature.YoctoSDK_AUTOTOOLS_NATURE_ID)) {
				throw new ProcessFailureException(Messages.getString("NewManagedProject.3") + //$NON-NLS-1$
						YoctoSDKMessages.getFormattedString("AutotoolsProjectPostProcess.WrongProjectNature", //$NON-NLS-1$
								projectName));
			} else {
				URI path = project.getLocationURI();
				IFileStore fs = EFS.getStore(path);
				fs = fs.getFileStore(new Path(AUTOGEN_SCRIPT_NAME));
				IFileInfo fileinfo = EFS.createFileInfo();
				fileinfo.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, true);
				fs.putInfo(fileinfo,EFS.SET_ATTRIBUTES, null);
			}
		} catch (Exception e) {
			throw new ProcessFailureException(Messages.getString("NewManagedProject.3") + e.getMessage(), e); //$NON-NLS-1$
		}
	}
}

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
package org.yocto.cmake.ui.internal;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class CMakeConsole implements IConsole {

	private IConsole console;

	final String cMakeConsoleId = "CMakeConsole"; //$NON-NLS-1$

	@Override
	public void start(IProject project) {
		this.console = CUIPlugin.getDefault().getConsoleManager(Messages.CMakeConsole_Name, cMakeConsoleId).getConsole(project);
		this.console.start(project);
	}

	@Override
	public ConsoleOutputStream getOutputStream() throws CoreException {
		return this.console.getOutputStream();
	}

	@Override
	public ConsoleOutputStream getInfoStream() throws CoreException {
		return this.console.getInfoStream();
	}

	@Override
	public ConsoleOutputStream getErrorStream() throws CoreException {
		return this.console.getErrorStream();
	}

}

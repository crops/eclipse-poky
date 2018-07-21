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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.yocto.sdk.core.internal.Activator;

public class YoctoProjectNature implements IProjectNature {

	public static final String NATURE_ID = Activator.PLUGIN_ID + ".YoctoProjectNature"; //$NON-NLS-1$

	IProject project = null;

	@Override
	public void configure() throws CoreException {
		// TODO: initialize project preferences?

	}

	@Override
	public void deconfigure() throws CoreException {
		// TODO: clear project preferences?

	}

	@Override
	public IProject getProject() {
		return this.project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}

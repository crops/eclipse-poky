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
package org.yocto.bc.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.yocto.bc.bitbake.BBSession;
import org.yocto.bc.ui.Activator;
import org.yocto.bc.ui.builder.BitbakeCommanderNature;

class RecipeContentProvider implements IStructuredContentProvider {
	public void dispose() {
	}

	public Object[] getElements(Object parent) {
		List recipes = new ArrayList();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		IProjectNature nature = null;
		for (int i = 0; i < projects.length; ++i) {
			try {
				if (projects[i].isOpen() && projects[i].hasNature(BitbakeCommanderNature.NATURE_ID)) {
					recipes.addAll(getRecipesFromProject(projects[i]));
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return recipes.toArray();
	}

	private Collection getRecipesFromProject(IProject project) throws Exception {
		BBSession session = Activator.getBBSession(Activator.getProjInfo(project.getLocationURI()), null);
		return session.getRecipeFiles(project);
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}
}

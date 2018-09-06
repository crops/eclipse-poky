package org.yocto.cmake.core;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.yocto.cmake.core.internal.Activator;

public class CMakeNature implements IProjectNature {

	public static final String NATURE_ID = Activator.PLUGIN_ID + ".CMakeNature"; //$NON-NLS-1$

	public static void addNature(IProject project, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] existingNatureIds = description.getNatureIds();

		for (String existingNatureId : existingNatureIds) {
			if (NATURE_ID.equals(existingNatureId))
				return;
		}

		String[] updatedNatureIds = new String[existingNatureIds.length + 1];
		System.arraycopy(existingNatureIds, 0, updatedNatureIds, 0, existingNatureIds.length);
		updatedNatureIds[existingNatureIds.length] = NATURE_ID;
		description.setNatureIds(updatedNatureIds);
		project.setDescription(description, monitor);
	}

	public static void removeNature(IProject project, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		List<String> natureIds = Arrays.asList(description.getNatureIds());

		if (!natureIds.contains(NATURE_ID)) {
			return;
		}

		natureIds.remove(NATURE_ID);
		description.setNatureIds(natureIds.toArray(new String[natureIds.size()]));
		project.setDescription(description, monitor);
	}

	@Override
	public void configure() throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deconfigure() throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public IProject getProject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProject(IProject project) {
		// TODO Auto-generated method stub

	}

}

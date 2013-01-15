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
package org.yocto.bc.ui.filesystem;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.yocto.bc.ui.model.YoctoHostFile;

public class OEIgnoreFile implements IFileStore {

	private final YoctoHostFile file;

	public OEIgnoreFile(YoctoHostFile file) {
		this.file = file;
	}

	@Override
	public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException {

		return new IFileInfo[0];
	}

	@Override
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		return new String[0];
	}

	@Override
	public IFileStore[] childStores(int options, IProgressMonitor monitor) throws CoreException {

		return new IFileStore[0];
	}

	@Override
	public void copy(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
	}

	@Override
	public void delete(int options, IProgressMonitor monitor) throws CoreException {
	}

	@Override
	public IFileInfo fetchInfo() {
		return new FileInfo(file.getName());
	}

	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		return new FileInfo(file.getName());
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public IFileStore getChild(IPath path) {
		return null;
	}



	@Override
	public IFileStore getChild(String name) {
		return null;
	}

	@Override
	public IFileSystem getFileSystem() {
		return OEFileSystem.getInstance();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public IFileStore getParent() {
		return null;
	}

	@Override
	public boolean isParentOf(IFileStore other) {
		return false;
	}

	@Override
	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	@Override
	public void move(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
	}

	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	@Override
	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
	}


	@Override
	public File toLocalFile(int options, IProgressMonitor monitor) throws CoreException {
		return file.toLocalFile();
	}

	@Override
	public URI toURI() {
		return file.toURI();
	}

	@Override
	public IFileStore getFileStore(IPath path) {
		return null;
	}


}

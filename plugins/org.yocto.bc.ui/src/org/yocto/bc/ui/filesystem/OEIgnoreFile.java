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
import org.eclipse.rse.services.files.IHostFile;
import org.yocto.bc.ui.model.YoctoHostFile;

public class OEIgnoreFile implements IFileStore {

	private final YoctoHostFile file;

	public OEIgnoreFile(YoctoHostFile file) {
		this.file = file;
	}

	public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException {

		return new IFileInfo[0];
	}

	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		return new String[0];
	}

	public IFileStore[] childStores(int options, IProgressMonitor monitor) throws CoreException {

		return new IFileStore[0];
	}

	public void copy(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
	}

	public void delete(int options, IProgressMonitor monitor) throws CoreException {
	}
	
	public IFileInfo fetchInfo() {
		return new FileInfo(file.getName());
	}

	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		return new FileInfo(file.getName());
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public IFileStore getChild(IPath path) {
		return null;
	}



	public IFileStore getChild(String name) {
		return null;
	}

	public IFileSystem getFileSystem() {
		return OEFileSystem.getInstance();
	}

	public String getName() {
		return file.getName();
	}

	public IFileStore getParent() {
		return null;
	}

	public boolean isParentOf(IFileStore other) {
		return false;
	}

	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	public void move(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
	}

	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
	}

	
	public File toLocalFile(int options, IProgressMonitor monitor) throws CoreException {
		return file.toLocalFile();
	}

	public URI toURI() {
		return file.toURI();
	}

	public IFileStore getFileStore(IPath path) {
		return null;
	}
	
	
}

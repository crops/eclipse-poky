package org.yocto.bc.ui.model;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;

public class YoctoHostFile implements IHostFile{
	private IHostFile file;
	private URI fileURI;
	private ProjectInfo projectInfo;
	private IFileService fileService;
	
	public YoctoHostFile(ProjectInfo pInfo, URI fileURI, IProgressMonitor monitor) throws SystemMessageException {
		this.projectInfo = pInfo;
		this.fileURI = fileURI;
		String path = fileURI.getPath();
		int parentEnd = path.lastIndexOf("/");
		String parentPath = path.substring(0, parentEnd);
		String fileName = path.substring(parentEnd + 1);
		fileService = projectInfo.getFileService(monitor);
		fileService.getFile(parentPath, fileName, monitor);
	}
	
	public YoctoHostFile(ProjectInfo projectInfo, URI uri) {
		this.fileURI = uri;
		this.projectInfo = projectInfo;
	}
	
	public IHostFile getFile() {
		return file;
	}
	public void setFile(IHostFile file) {
		this.file = file;
	}
	public ProjectInfo getProjectInfo() {
		return projectInfo;
	}
	public void setProjectInfo(ProjectInfo projectInfo) {
		this.projectInfo = projectInfo;
	}
	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}
	public String getName() {
		return file.getName();
	}
	public URI getProjectLocationURI() {
		return projectInfo.getURI();
	}
	public URI getLocationURI() {
		projectInfo.getURI().getPath().indexOf(file.getAbsolutePath());
		return projectInfo.getURI();
	}
	public boolean isDirectory() {
		return file.isDirectory();
	}
	public String getParentPath() {
		return file.getParentPath();
	}
	public boolean copy(IFileStore destFileStore, IProgressMonitor monitor) {
		IHostFile destFile;
		try {
			destFile = fileService.getFile(destFileStore.toURI().getPath(), destFileStore.getName(), monitor);
			fileService.copy(file.getParentPath(), file.getName(), destFile.getParentPath(), destFile.getAbsolutePath(), monitor);
		} catch (SystemMessageException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean exists() {
		return file.exists();
	}
	@Override
	public boolean canRead() {
		return file.canRead();
	}
	@Override
	public boolean canWrite() {
		return file.canWrite();
	}
	@Override
	public long getModifiedDate() {
		return file.getModifiedDate();
	}
	@Override
	public long getSize() {
		return file.getSize();
	}
	@Override
	public boolean isArchive() {
		return file.isArchive();
	}
	@Override
	public boolean isFile() {
		return file.isFile();
	}
	@Override
	public boolean isHidden() {
		return file.isHidden();
	}
	@Override
	public boolean isRoot() {
		return file.isRoot();
	}
	@Override
	public void renameTo(String newName) {
		file.renameTo(newName);
	}
	public URI getParentFile() {
		if (file.getParentPath().isEmpty())
			return null;
		try {
			return new URI(fileURI.getScheme(), fileURI.getHost(), file.getParentPath(), fileURI.getFragment());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	public boolean delete(IProgressMonitor monitor) {
		try {
			fileService.delete(file.getParentPath(), file.getName(), monitor);
		} catch (SystemMessageException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public void mkdir() {
		
	}
	public String[] getChildNames(IProgressMonitor monitor) {
		if (file.isDirectory()) {
			IHostFile[] files;
			try {
				files = fileService.list(file.getAbsolutePath(), "*", IFileService.FILE_TYPE_FILES_AND_FOLDERS, monitor);
				ArrayList<String> names = new ArrayList<String>();
				
				for (IHostFile f : files) {
					names.add(f.getName());
				}
				return (String[])names.toArray();
			} catch (SystemMessageException e) {
				e.printStackTrace();
			}
		} 
		return  new String[]{};
	}
	public IHost getConnection() {
		return projectInfo.getConnection();
	}
	
	public URI getChildURI(String name) {
		try {
			return new URI(fileURI.getScheme(), fileURI.getHost(), fileService.getFile(file.getAbsolutePath(), name, null).getAbsolutePath(), fileURI.getFragment());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (SystemMessageException e) {
			e.printStackTrace();
		}
		return null;
	}
	public File toLocalFile() {
		//TODO
		//fileService.getFile(file.getParentPath(), file.getName(), null);
		return null;
	}
	public URI toURI() {
		return fileURI;
	}
	public YoctoHostFile getChildHostFile(String name) {
		try {
			return new YoctoHostFile(projectInfo, getChildURI(name), null);
		} catch (SystemMessageException e) {
			e.printStackTrace();
			return null;
		}
	}

	public URI getChildURIformPath(IPath path) {
		try {
			return new URI(fileURI.getScheme(), fileURI.getHost(), fileService.getFile(file.getAbsolutePath(), path.toPortableString(), null).getAbsolutePath(), fileURI.getFragment());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		} catch (SystemMessageException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void move(IFileStore destFile, IProgressMonitor monitor) {
		try {
			fileService.move(file.getParentPath(), file.getName(), destFile.getParent().toURI().getPath(), destFile.getName(), monitor);
		} catch (SystemMessageException e) {
			e.printStackTrace();
		}
	}

	public void getOutputStream(int options, IProgressMonitor monitor) {
		try {
			fileService.getOutputStream(file.getParentPath(), file.getName(), options, monitor);
		} catch (SystemMessageException e) {
			e.printStackTrace();
		}
	}

	public void getInputStream(int options, IProgressMonitor monitor) {
		try {
			fileService.getInputStream(file.getParentPath(), file.getName(), false, monitor);
		} catch (SystemMessageException e) {
			e.printStackTrace();
		}
	}

	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) {
		try {
			if ((options & EFS.SET_LAST_MODIFIED) != 0)
				fileService.setLastModified(file.getParentPath(), file.getName(), info.getLastModified(), monitor);
		} catch (SystemMessageException e) {
			e.printStackTrace();
		}
	}
}

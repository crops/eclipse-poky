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

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.yocto.bc.bitbake.BBSession;
import org.yocto.bc.ui.Activator;
import org.yocto.bc.ui.model.ProjectInfo;
import org.yocto.bc.ui.model.YoctoHostFile;

/**
 * A filesystem that ignores specific OE directories that contain derived information.
 * @author kgilmer
 *
 */
public class OEFileSystem extends FileSystem {

	private static IFileSystem ref;
	private ProjectInfo projInfo;

	public static IFileSystem getInstance() {
		return ref;
	}

	private Map<URI, OEFile> fileStoreCache;

	public OEFileSystem() {
		ref = this;
		fileStoreCache = new Hashtable<URI, OEFile>();
	}

	@Override
	public IFileStore getStore(URI uri) {

		OEFile uf = fileStoreCache.get(uri);
		setProjInfo(uri);

		if (uf == null) {
			BBSession config = null;
			try {
				config = Activator.getBBSession(projInfo, new NullProgressMonitor());
				config.initialize();
			} catch (Exception e) {
				e.printStackTrace();
				return new OEIgnoreFile(new YoctoHostFile(projInfo, uri));
			}

			if (config.get("TMPDIR") == null || config.get("DL_DIR") == null || config.get("SSTATE_DIR")== null) {
				throw new RuntimeException("Invalid local.conf: TMPDIR or DL_DIR or SSTATE_DIR undefined.");
			}

			List<Object> ignoreList = new ArrayList<Object>();

			//These directories are ignored because they contain too many files for Eclipse to handle efficiently.
			ignoreList.add(config.get("TMPDIR"));
			ignoreList.add(config.get("DL_DIR"));
			ignoreList.add(config.get("SSTATE_DIR"));

			//FIXME: add project info
			try {
				uf = new OEFile(uri, ignoreList, uri, projInfo, new NullProgressMonitor());
				fileStoreCache.put(uri, uf);
			} catch (SystemMessageException e) {
				e.printStackTrace();
			}
		}

		return uf;
	}

	private void setProjInfo(URI uri) {
			try {
				if(projInfo == null)
					projInfo = Activator.getProjInfo(uri);
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
}

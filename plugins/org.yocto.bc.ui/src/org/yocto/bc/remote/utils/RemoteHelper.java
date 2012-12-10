/********************************************************************************
 * Copyright (c) 2009, 2010 MontaVista Software, Inc and Others.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova (MontaVista) - initial API and implementation
 * Lianhao Lu (Intel)			- Modified to add other file operations.
 ********************************************************************************/
package org.yocto.bc.remote.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.ui.console.MessageConsole;
import org.yocto.bc.ui.Activator;
import org.yocto.bc.ui.wizards.install.Messages;

public class RemoteHelper {
	public static final String TERMINATOR = "234o987dsfkcqiuwey18837032843259d";
	public static final int TOTALWORKLOAD = 100;
	private static Map<IHost, RemoteMachine> machines;

	public static RemoteMachine getRemoteMachine(IHost connection){
		if (!getMachines().containsKey(connection))
			getMachines().put(connection, new RemoteMachine(connection));
		return getMachines().get(connection);
	}

	private static Map<IHost, RemoteMachine> getMachines() {
		if (machines == null)
			machines = new HashMap<IHost, RemoteMachine>();
		return machines;
	}

	public static MessageConsole getConsole(IHost connection) {
		return getRemoteMachine(connection).getConsole();
	}

	public static CommandResponseHandler getCommandHandler(IHost connection) {
		return getRemoteMachine(connection).getCmdHandler();
	}

	public static YoctoHostShellProcessAdapter getHostShellProcessAdapter(IHost connection) {
		return getRemoteMachine(connection).getHostShellProcessAdapter();
	}

	public static ProcessStreamBuffer getProcessBuffer(IHost connection) {
		return getRemoteMachine(connection).getProcessBuffer();
	}

	public static IHostShell getHostShell(IHost connection) {
		return getRemoteMachine(connection).getHostShell();
	}

	public static IHost getRemoteConnectionByName(String remoteConnection) {
		if (remoteConnection == null)
			return null;
		IHost[] connections = RSECorePlugin.getTheSystemRegistry().getHosts();
		for (int i = 0; i < connections.length; i++)
			if (connections[i].getAliasName().equals(remoteConnection))
				return connections[i];
		return null;
	}

	public static IHost getRemoteConnectionForURI(URI uri, IProgressMonitor monitor) {
		if (uri == null)
			return null;

		String host = uri.getHost();
		if (host == null) {
			// this is a local connection
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			return sr.getLocalHost();
		}
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		IHost[] connections = sr.getHosts();

		IHost unconnected = null;
		for (IHost conn : connections) {
			if (host.equalsIgnoreCase(conn.getHostName())) {
				IRemoteFileSubSystem fss = getRemoteFileSubSystem(conn);
				if (fss != null && fss.isConnected())
					return conn;
				unconnected = conn;
			}
		}

		return unconnected;
	}

	public static IRemoteFileSubSystem getRemoteFileSubSystem(IHost host) {
		IRemoteFileSubSystem candidate = null;
		IRemoteFileSubSystem otherServiceCandidate = null;
		IRemoteFileSubSystem[] subSystems = RemoteFileUtility.getFileSubSystems(host);

		for (IRemoteFileSubSystem subSystem : subSystems) {
			if (subSystem instanceof FileServiceSubSystem) {
				if (subSystem.isConnected())
					return subSystem;

				if (otherServiceCandidate == null)
					otherServiceCandidate = subSystem;

			} else if (candidate == null || (subSystem.isConnected() && !candidate.isConnected()))
				candidate = subSystem;

		}
		if (candidate != null && candidate.isConnected())
			return candidate;
		if (otherServiceCandidate != null)
			return otherServiceCandidate;
		return null;
	}

	public static String getRemoteHostName(String remoteConnection){
		final IHost host = getRemoteConnectionByName(remoteConnection);
		if(host == null)
			return null;
		else
			return host.getHostName();
	}

	public static IFileService getConnectedRemoteFileService(IHost connection, IProgressMonitor monitor) throws Exception {
		return getRemoteMachine(connection).getRemoteFileService(monitor);
	}

	public static IHostFile[] getRemoteDirContent(IHost connection, String remoteParent, String fileFilter, int fileType, IProgressMonitor monitor){

		try {
			IFileService fileServ = getConnectedRemoteFileService(connection, monitor);
			return fileServ.list(remoteParent, fileFilter, fileType, monitor);
		} catch (SystemMessageException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static IService getConnectedRemoteFileService(IRemoteConnection currentConnection, IProgressMonitor monitor) throws Exception {
		final ISubSystem subsystem = getFileSubsystem(getRemoteConnectionByName(currentConnection.getName()));

		if (subsystem == null)
			throw new Exception(Messages.ErrorNoSubsystem);

		try {
			subsystem.connect(monitor, false);
		} catch (CoreException e) {
			throw e;
		} catch (OperationCanceledException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}

		if (!subsystem.isConnected())
			throw new Exception(Messages.ErrorConnectSubsystem);

		return ((IFileServiceSubSystem) subsystem).getFileService();
	}

	public static ISubSystem getFileSubsystem(IHost connection) {
		return getRemoteMachine(connection).getFileSubsystem();
	}

	public static IService getConnectedShellService(IHost connection, IProgressMonitor monitor) throws Exception {
		return getRemoteMachine(connection).getShellService(monitor);
	}

	public static ISubSystem getCmdSubsystem(IHost host) {
		if (host == null)
			return null;
		ISubSystem[] subSystems = host.getSubSystems();
		for (int i = 0; i < subSystems.length; i++) {
			if (subSystems[i] instanceof IRemoteCmdSubSystem)
				return subSystems[i];
		}
		return null;
	}

	public static void getRemoteFile(IHost connection, String localExePath, String remoteExePath,
			IProgressMonitor monitor) throws Exception {

		assert(connection!=null);
		monitor.beginTask(Messages.InfoDownload, 100);

		IFileService fileService;
		try {
			fileService = getConnectedRemoteFileService(connection, new SubProgressMonitor(monitor, 10));
			File file = new File(localExePath);
			file.deleteOnExit();
			monitor.worked(5);
			Path remotePath = new Path(remoteExePath);
			fileService.download(remotePath.removeLastSegments(1).toString(),
					remotePath.lastSegment(),file,true, null,
					new SubProgressMonitor(monitor, 85));
		} finally {
			monitor.done();
		}
		return;
	}

	public static IHostFile getRemoteHostFile(IHost connection, String remoteFilePath, IProgressMonitor monitor){
		assert(connection != null);
		monitor.beginTask(Messages.InfoDownload, 100);

		try {
			IFileService fileService = getConnectedRemoteFileService(connection, new SubProgressMonitor(monitor, 10));
			Path remotePath = new Path(remoteFilePath);
			IHostFile remoteFile = fileService.getFile(remotePath.removeLastSegments(1).toString(), remotePath.lastSegment(), new SubProgressMonitor(monitor, 5));
			return remoteFile;
		} catch (Exception e) {
	    }finally {
			monitor.done();
		}
		return null;
	}

	public static boolean runCommandRemote(final IHost connection, final YoctoCommand cmd) throws Exception {
		final String remoteCommand = cmd.getCommand() + " " + cmd.getArguments();
		final boolean hasErrors = false;

		if (!cmd.getInitialDirectory().isEmpty()) {
			writeToShell(connection, "cd " + cmd.getInitialDirectory());
		}
		if (!hasErrors)
			writeToShell(connection, remoteCommand);

		return hasErrors;
	}

	public static boolean writeToShell(final IHost connection, final String remoteCommand){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					YoctoHostShellProcessAdapter adapter = getHostShellProcessAdapter(connection);
					String fullRemoteCommand = remoteCommand + "; echo " + TERMINATOR + ";";
					adapter.setLastCommand(fullRemoteCommand);
					getHostShell(connection).writeToShell(fullRemoteCommand);
					while (!adapter.isFinished())
						Thread.sleep(2);
//					return hostShellProcessAdapter.hasErrors();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).run();
		return true;
	}

	public static void runBatchRemote(IHost connection, List<YoctoCommand> cmds, boolean waitForOutput) throws CoreException {
		try {
			String remoteCommand = "";
			for (YoctoCommand cmd : cmds) {
				remoteCommand = cmd.getCommand() + " " + cmd.getArguments();
				if (!cmd.getInitialDirectory().isEmpty()) {
					writeToShell(connection, "cd " + cmd.getInitialDirectory());
				}
				writeToShell(connection, remoteCommand);
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 *
	 * @param message
	 *            the status message
	 * @param exception
	 *            lower level exception associated with the error, or
	 *            <code>null</code> if none
	 * @param code
	 *            error code
	 */
	public static void abort(String message, Throwable exception, int code) throws CoreException {
		IStatus status;
		if (exception != null) {
			MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, code, message, exception);
			multiStatus.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, code, exception.getLocalizedMessage(), exception));
			status= multiStatus;
		} else {
			status= new Status(IStatus.ERROR, Activator.PLUGIN_ID, code, message, null);
		}
		throw new CoreException(status);
	}

	public static URI createNewURI(URI oldURI, String name) {
		try {
			String sep = oldURI.getPath().endsWith("/") ? "" : "/";
			return new URI(oldURI.getScheme(), oldURI.getHost(), oldURI.getPath() + sep + name, oldURI.getFragment());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean fileExistsRemote(IHost conn, IProgressMonitor monitor, String path) {
		try {
			IFileService fs = getConnectedRemoteFileService(conn, monitor);
			int nameStart = path.lastIndexOf("/");
			String parentPath = path.substring(0, nameStart);
			String name = path.substring(nameStart + 1);
			IHostFile hostFile = fs.getFile(parentPath, name, monitor);

			return hostFile.exists();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void clearProcessBuffer(IHost connection) {
		getHostShellProcessAdapter(connection).clearProcessBuffer();
	}

}
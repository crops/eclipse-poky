package org.yocto.bc.remote.utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.shells.IHostShell;

public class YoctoRunnableWithProgress extends YoctoHostShellProcessAdapter
		implements IRunnableWithProgress {

	private String taskName;
	private IRemoteConnection remoteConnection;
	private IRemoteServices remoteServices;
	private String cmd;
	private String args;
	private IProgressMonitor monitor;
	
	public YoctoRunnableWithProgress(IHostShell hostShell,
			ProcessStreamBuffer processStreamBuffer,
			CommandResponseHandler commandResponseHandler) throws IOException {
		super(hostShell, processStreamBuffer, commandResponseHandler);
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		try {
			this.monitor = monitor;
			this.monitor.beginTask(taskName, RemoteHelper.TOTALWORKLOAD);
			
			if (!remoteConnection.isOpen()) {
				try {
					remoteConnection.open(monitor);
				} catch (RemoteConnectionException e1) {
					e1.printStackTrace();
				}
			}

			if (!remoteServices.isInitialized()) {
				remoteServices.initialize();
			}

			try {
				IHost connection = RemoteHelper.getRemoteConnectionByName(remoteConnection.getName());
                RemoteHelper.runCommandRemote(connection, new YoctoCommand(cmd, "", args));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				monitor.done();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public IProgressMonitor getOwnMonitor() {
		return monitor;
	}

	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}

	public void setRemoteConnection(IRemoteConnection remoteConnection) {
		this.remoteConnection = remoteConnection;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public IRemoteServices getRemoteServices() {
		return remoteServices;
	}

	public void setRemoteServices(IRemoteServices remoteServices) {
		this.remoteServices = remoteServices;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}
}

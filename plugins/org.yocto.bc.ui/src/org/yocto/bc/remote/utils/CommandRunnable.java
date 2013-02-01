package org.yocto.bc.remote.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.locks.Lock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.shells.HostShellProcessAdapter;
import org.eclipse.rse.services.shells.IHostShell;

public class CommandRunnable implements Runnable{
	private IHostShell hostShell;
	private IHost connection;
	private YoctoCommand cmd;
	private IProgressMonitor monitor;
	private CommandResponseHandler cmdHandler;

	CommandRunnable(IHost connection, YoctoCommand cmd, IProgressMonitor monitor){
		this.connection = connection;
		this.cmdHandler = RemoteHelper.getCommandHandler(connection);
		this.cmd = cmd;
		this.monitor = monitor;
		this.hostShell = null;
	}
	@Override
	public void run() {
		try {
			hostShell = RemoteHelper.runCommandRemote(connection, cmd, monitor);
			cmd.setProcessBuffer(RemoteHelper.processOutput(monitor, hostShell, cmdHandler, new char[]{'\n'}));
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

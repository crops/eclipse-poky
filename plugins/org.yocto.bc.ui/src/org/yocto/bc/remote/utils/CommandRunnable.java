package org.yocto.bc.remote.utils;

import java.io.BufferedReader;
import java.util.concurrent.locks.Lock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
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
			cmd.setProcessBuffer(processOutput());
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ProcessStreamBuffer processOutput() throws Exception {
		if (hostShell == null)
			throw new Exception("An error has occured while trying to run remote command!");

		Lock lock = hostShell.getStandardOutputReader().getReaderLock();
		lock.lock();
		ProcessStreamBuffer processBuffer = new ProcessStreamBuffer();
		BufferedReader inbr = hostShell.getStandardOutputReader().getReader();
		BufferedReader errbr = hostShell.getStandardErrorReader().getReader();
		boolean cancel = false;
		while (!cancel) {
			if(monitor.isCanceled()) {
				cancel = true;
				lock.unlock();
				throw new InterruptedException("User Cancelled");
			}
			StringBuffer buffer = new StringBuffer();
			int c;
			while ((c = errbr.read()) != -1) {
				char ch = (char) c;
				buffer.append(ch);
				if (ch == '\n'){
					String str = buffer.toString();
					processBuffer.addErrorLine(str);
					System.out.println(str);
					if (str.trim().equals(RemoteHelper.TERMINATOR)) {
						break;
					}
					cmdHandler.response(str, true);
					buffer.delete(0, buffer.length());
				}
			}

			while ((c = inbr.read()) != -1) {
				char ch = (char) c;
				buffer.append(ch);
				if (ch == '\n'){
					String str = buffer.toString();
					processBuffer.addOutputLine(str);
					System.out.println(str);
					if (str.trim().equals(RemoteHelper.TERMINATOR)) {
						break;
					}
					cmdHandler.response(str, false);
					buffer.delete(0, buffer.length());
				}
			}
			cancel = true;
		}
		lock.unlock();
		return processBuffer;
	}

}

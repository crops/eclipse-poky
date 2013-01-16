package org.yocto.bc.remote.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.swt.widgets.Display;

public class YoctoRunnableWithProgress implements IRunnableWithProgress {

	private String taskName;
	private IRemoteConnection remoteConnection;
	private IRemoteServices remoteServices;
	private IProgressMonitor monitor;
	private ICalculatePercentage calculator;
	private int reportedWorkload;

	private YoctoCommand command;

	public YoctoRunnableWithProgress(YoctoCommand command) throws IOException {
		this.command = command;
		this.calculator = new GitCalculatePercentage();
	}

	private interface ICalculatePercentage {
		public float calWorkloadDone(String info) throws IllegalArgumentException;
	}

	private class GitCalculatePercentage implements ICalculatePercentage {
		final Pattern pattern = Pattern.compile("^Receiving objects:\\s*(\\d+)%.*");
		@Override
		public float calWorkloadDone(String info) throws IllegalArgumentException {
			Matcher m = pattern.matcher(info.trim());
			if(m.matches()) {
				return new Float(m.group(1)) / 100;
			}else {
				throw new IllegalArgumentException();
			}
		}
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
				YoctoThread th = new YoctoThread(connection, command);
				th.run();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				monitor.done();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	class YoctoThread implements Runnable{
		private IHost connection;
		private YoctoCommand command;
		private CommandResponseHandler cmdHandler;
		private IHostShell hostShell;

		YoctoThread(IHost connection, YoctoCommand command){
			this.connection = connection;
			this.cmdHandler = RemoteHelper.getCommandHandler(connection);
			this.command = command;
		}

		@Override
		public void run() {
			try {
				hostShell = RemoteHelper.runCommandRemote(this.connection, command, monitor);
				command.setProcessBuffer(processOutput());
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		private ProcessStreamBuffer processOutput() throws Exception {
			if (hostShell == null)
				throw new Exception("An error has occured while trying to run remote command!");
			monitor.beginTask(taskName, RemoteHelper.TOTALWORKLOAD);
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
				if (errbr != null) {
					while ((c = errbr.read()) != -1) {
						char ch = (char) c;
						buffer.append(ch);
						if (ch == '\n' || ch == '\r'){
							String str = buffer.toString();
							processBuffer.addOutputLine(str);
							System.out.println(str);
							if (ch == '\r')
								reportProgress(str);
							if (str.trim().equals(RemoteHelper.TERMINATOR)) {
								break;
							}
							cmdHandler.response(str, false);
							buffer.delete(0, buffer.length());
						}
					}
				}
				if (inbr != null) {
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
				}
				cancel = true;
			}
			lock.unlock();
			return processBuffer;
		}
	}
	private void updateMonitor(final int work){

		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (monitor != null) {
					monitor.worked(work);
				}
			}

		});
	}

	private void doneMonitor(){
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				monitor.done();
			}
		});
	}

	public void reportProgress(String info) {
		if(calculator == null) {
			updateMonitor(1);
		} else {
			float percentage;
			try {
				percentage = calculator.calWorkloadDone(info);
			} catch (IllegalArgumentException e) {
				System.out.println(info);
				//can't get percentage
				return;
			}
			int delta = (int) (RemoteHelper.TOTALWORKLOAD * percentage - reportedWorkload);
			if( delta > 0 ) {
				updateMonitor(delta);
				reportedWorkload += delta;
			}

			if (reportedWorkload == RemoteHelper.TOTALWORKLOAD)
				doneMonitor();
		}
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
}

package org.yocto.bc.remote.utils;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.services.local.shells.LocalShellService;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;
import org.eclipse.ui.console.MessageConsole;
import org.yocto.bc.ui.wizards.install.Messages;

public class RemoteMachine {
	public static final String PROXY = "proxy";

	private Map<String, String> environment;
	private MessageConsole console;
	private CommandResponseHandler cmdHandler;
	private IHostShell hostShell;
	private IShellService shellService;
	private ProcessStreamBuffer processBuffer;
	private IHost connection;

	private ISubSystem fileSubSystem;
	private IFileService fileService;

	public RemoteMachine(IHost connection) {
		setConnection(connection);
	}
	private ProcessStreamBuffer processOutput(IHostShell shell, IProgressMonitor monitor) throws Exception {
		if (shell == null)
			throw new Exception("An error has occured while trying to run remote command!");
		ProcessStreamBuffer processBuffer = new ProcessStreamBuffer();
		
		Lock lock = shell.getStandardOutputReader().getReaderLock();
		lock.lock();
		BufferedReader inbr =  shell.getStandardOutputReader().getReader();
		BufferedReader errbr =  shell.getStandardErrorReader().getReader();
		
		boolean cancel = false;
		while (!cancel) {
			if(monitor.isCanceled()) {
				cancel = true;
				throw new InterruptedException("User Cancelled");
			}
			StringBuffer buffer = new StringBuffer();
			int c;
			if (errbr != null)
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
					buffer.delete(0, buffer.length());
				}
			}
			if (inbr != null)
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
					buffer.delete(0, buffer.length());
				}
			}
			cancel = true;
		}
		return processBuffer;
	}

	public String[] prepareEnvString(IProgressMonitor monitor){
		String[] env = null;
		try {
			if (shellService instanceof LocalShellService) {
				env  = shellService.getHostEnvironment();
			} else {
				List<String> envList = new ArrayList<String>();
				getRemoteEnvProxyVars(monitor);
				String value = "";
				for (String varName : environment.keySet()){
					value = varName + "=" + environment.get(varName);
					envList.add(value);
				}
				env = envList.toArray(new String[envList.size()]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return env;
	}
	public void getRemoteEnvProxyVars(IProgressMonitor monitor){
		try {
			if (environment != null && !environment.isEmpty())
				return;

			environment = new HashMap<String, String>();

			IShellService shellService = getShellService(new SubProgressMonitor(monitor, 7));

//			HostShellProcessAdapter p = null;
			ProcessStreamBuffer buffer = null;
			try {
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 3);
				IHostShell hostShell = shellService.runCommand("", "env" + " ; echo " + RemoteHelper.TERMINATOR + "; exit;", new String[]{}, subMonitor);
//				p = new HostShellProcessAdapter(hostShell);
				buffer = processOutput(hostShell, subMonitor);
				for(int i = 0; i < buffer.getOutputLines().size(); i++) {
					String out = buffer.getOutputLines().get(i);
					String[] tokens = out.split("=");
					if (tokens.length != 2)
						continue;
					String varName = tokens[0];
					String varValue = tokens[1];
					if (varName.contains(PROXY))
						environment.put(varName, varValue);
				}
			} catch (Exception e) {
//				if (p != null) {
//					p.destroy();
//				}
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, String> getEnvironment() {
		return environment;
	}
	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;
	}
	public MessageConsole getConsole() {
		if (console == null)
			console = ConsoleHelper.findConsole(ConsoleHelper.YOCTO_CONSOLE);

		ConsoleHelper.showConsole(console);
		return console;
	}
	public CommandResponseHandler getCmdHandler() {
		if (cmdHandler == null)
			cmdHandler = new CommandResponseHandler(getConsole());
		return cmdHandler;
	}
	public IHostShell getHostShell() {
		try {
			if (hostShell == null) {
				hostShell = getShellService(new NullProgressMonitor()).launchShell("", new String[]{}, new NullProgressMonitor());
				prepareEnvString(new NullProgressMonitor());
			}
		} catch (SystemMessageException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hostShell;
	}

	public IShellService getShellService(IProgressMonitor monitor) throws Exception {
		if (shellService != null)
			return shellService;

		final ISubSystem subsystem = getShellSubsystem();

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

		shellService = ((IShellServiceSubSystem) subsystem).getShellService();
		return shellService;
	}
	private ISubSystem getShellSubsystem() {
		if (connection == null)
			return null;
		ISubSystem[] subSystems = connection.getSubSystems();
		for (int i = 0; i < subSystems.length; i++) {
			if (subSystems[i] instanceof IShellServiceSubSystem)
				return subSystems[i];
		}
		return null;
	}

	public ProcessStreamBuffer getProcessBuffer() {
		if (processBuffer == null)
			processBuffer = new ProcessStreamBuffer();
		return processBuffer;
	}

	public IHost getConnection() {
		return connection;
	}
	public void setConnection(IHost connection) {
		this.connection = connection;
	}

	public IFileService getRemoteFileService(IProgressMonitor monitor) throws Exception {
		if (fileService == null) {

			while(getFileSubsystem() == null)
				Thread.sleep(2);
			try {
				getFileSubsystem().connect(monitor, false);
			} catch (CoreException e) {
				throw e;
			} catch (OperationCanceledException e) {
				throw new CoreException(Status.CANCEL_STATUS);
			}

			if (!getFileSubsystem().isConnected())
				throw new Exception(Messages.ErrorConnectSubsystem);

			fileService = ((IFileServiceSubSystem) getFileSubsystem()).getFileService();
		}
		return fileService;
	}

	public ISubSystem getFileSubsystem() {
		if (fileSubSystem == null) {
			if (connection == null)
				return null;
			ISubSystem[] subSystems = connection.getSubSystems();
			for (int i = 0; i < subSystems.length; i++) {
				if (subSystems[i] instanceof IFileServiceSubSystem) {
					fileSubSystem = subSystems[i];
					break;
				}
			}
		}
		return fileSubSystem;
	}

}

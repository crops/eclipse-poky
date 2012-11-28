package org.yocto.bc.remote.utils;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
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
	private YoctoHostShellProcessAdapter hostShellProcessAdapter;
	private IShellService shellService;
	private ProcessStreamBuffer processBuffer;
	private IHost connection;

	private ISubSystem fileSubSystem;
	private IFileService fileService;

	public RemoteMachine(IHost connection) {
		setConnection(connection);
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
			}
		} catch (SystemMessageException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hostShell;
	}
	
	public YoctoHostShellProcessAdapter getHostShellProcessAdapter() {
		try {
			if (hostShellProcessAdapter == null)
				hostShellProcessAdapter = new YoctoHostShellProcessAdapter(getHostShell(), getProcessBuffer(), getCmdHandler());
			return hostShellProcessAdapter;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
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
	
			if (getFileSubsystem() == null)
				throw new Exception(Messages.ErrorNoSubsystem);
	
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

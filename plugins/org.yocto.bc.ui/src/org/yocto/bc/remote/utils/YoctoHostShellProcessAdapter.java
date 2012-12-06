package org.yocto.bc.remote.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.services.shells.HostShellProcessAdapter;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputReader;
import org.eclipse.swt.widgets.Display;

public class YoctoHostShellProcessAdapter extends  HostShellProcessAdapter {
	private String commandPrompt = null;
	private static final String ROOT = "root";
	private static final String PROMPT_USER_CH = "$";
	private static final String PROMPT_ROOT_CH = "#";
	private ProcessStreamBuffer processStreamBuffer;
	private CommandResponseHandler commandResponseHandler;
	private boolean isFinished;
	private ICalculatePercentage calculator;
	private int reportedWorkload;
	private boolean isAlive;

	private String command;
	private Map<String, IProgressMonitor> commandMonitors;
	private String endChar = null;

	private Semaphore sem;
	
	public YoctoHostShellProcessAdapter(IHostShell hostShell, ProcessStreamBuffer processStreamBuffer, CommandResponseHandler commandResponseHandler) throws IOException {
		super(hostShell);
		this.processStreamBuffer = processStreamBuffer;
		this.commandResponseHandler = commandResponseHandler;
		this.calculator = new GitCalculatePercentage();
		this.sem = new Semaphore(1);
		this.command = "";
		this.commandMonitors = new HashMap<>();
	}

	public String getLastCommand() {
		return command;
	}

	public synchronized void setLastCommand(String lastCommand) {
		try {
			// there are still some processes that might take a long time and if we do not wait for them, 
			// then the semaphore will not be released, because an interrupted exception will occur
			Thread.sleep(2000);
			isFinished = false;
			sem.acquire();
			this.command = lastCommand.trim();
			System.out.println("last command " + lastCommand + getOwnMonitor());
			this.commandMonitors.put(command, getOwnMonitor());

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private interface ICalculatePercentage {
		public float calWorkloadDone(String info) throws IllegalArgumentException;
	}
	
	private class GitCalculatePercentage implements ICalculatePercentage {
		final Pattern pattern = Pattern.compile("^Receiving objects:\\s*(\\d+)%.*");
		public float calWorkloadDone(String info) throws IllegalArgumentException {
	Matcher m = pattern.matcher(info.trim());
			if(m.matches()) {
				return new Float(m.group(1)) / 100;
			}else {
				throw new IllegalArgumentException();
			}
		}
	}
	
	private IProgressMonitor getMonitor() {
		if (command == null) {
			return null;
		}
		return commandMonitors.get(command);
	}

	private void updateMonitor(final int work){
	
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (getMonitor() != null) {
					getMonitor().worked(work);
				}
			}

		});
	}

	private void doneMonitor(){
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				getMonitor().done();
			}
		});
	}

	private void reportProgress(String info) {
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

	@Override
	public void shellOutputChanged(IHostShellChangeEvent event) {
		IHostShellOutputReader reader = event.getReader();
		IHostOutput[] lines = event.getLines();
		if (reader.isErrorReader()) {
			for (IHostOutput line : lines) {
				String value = line.getString();
				if (value.isEmpty()) {
					continue;
				}
				setCommandPrompt(value);
				System.out.println(value);
				this.processStreamBuffer.addErrorLine(value);
				this.commandResponseHandler.response(value, false);
			}
		} else {
			for (IHostOutput line : lines) {
				String value = line.getString().trim();
				if (value.isEmpty()) {
					continue;
				}
				setCommandPrompt(value);
	
				if (commandPrompt != null && endChar != null && command != null && processStreamBuffer != null &&
						value.startsWith(commandPrompt) &&  value.endsWith(endChar) && 
						!value.endsWith(command) && processStreamBuffer.getLastOutputLineContaining(command) != null) {
					sem.release();
					isFinished = true;
				}

				reportProgress(value);
				System.out.println(value);
				this.processStreamBuffer.addOutputLine(value);
				this.commandResponseHandler.response(value, false);
			}
		}
		
	}
	private void setCommandPrompt(String value) {
		if (commandPrompt == null) {
			if (value.startsWith(ROOT) && value.indexOf(PROMPT_ROOT_CH) != -1) {
				int end = value.indexOf(PROMPT_ROOT_CH);
				commandPrompt = value.substring(0, end);
				endChar = PROMPT_ROOT_CH;
			} else if (value.indexOf(PROMPT_USER_CH) != -1) {
				int end = value.indexOf(PROMPT_USER_CH);
				commandPrompt = value.substring(0, end);
				endChar = PROMPT_USER_CH;
			}
				
		}
	}
	public boolean isFinished() {
		return isFinished;
	}
	public boolean hasErrors(){
		return this.processStreamBuffer.errorLines.size() != 0;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public void clearProcessBuffer() {
		this.processStreamBuffer.outputLines.clear();
		this.processStreamBuffer.errorLines.clear();
	}

	public IProgressMonitor getOwnMonitor() {
		return new NullProgressMonitor();
	}

}


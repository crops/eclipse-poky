package org.yocto.bc.remote.utils;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.eclipse.rse.services.shells.HostShellProcessAdapter;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputReader;

public class YoctoHostShellProcessAdapter extends  HostShellProcessAdapter{
	private String commandPrompt = null;
	private static final String ROOT = "root";
	private static final String PROMPT_USER_CH = "$";
	private static final String PROMPT_ROOT_CH = "#";
	
	private ProcessStreamBuffer processStreamBuffer;
	private CommandResponseHandler commandResponseHandler;
	private Semaphore sem;
	private String lastCommand;
	private boolean waitForOutput; 
	private String endChar = null;
	
	public String getLastCommand() {
		return lastCommand;
	}

	public void setLastCommand(String lastCommand) {
		if (waitForOutput) {
			try {
				// there are still some processed that might take a long time and if we do not wait for them, 
				// then the semaphore will not be released, because an interrupted exception will occur
				Thread.sleep(2000);
				sem.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.lastCommand = lastCommand.trim();
	}

	public YoctoHostShellProcessAdapter(IHostShell hostShell, ProcessStreamBuffer processStreamBuffer, CommandResponseHandler commandResponseHandler) throws IOException {
		super(hostShell);
		this.processStreamBuffer = processStreamBuffer;
		this.commandResponseHandler = commandResponseHandler;
		this.sem = new Semaphore(1);
		this.lastCommand = "";
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
				if (value.startsWith(commandPrompt) &&  value.endsWith(endChar) && 
						!value.endsWith(lastCommand) && processStreamBuffer.getLastOutputLineContaining(lastCommand) != null && 
						waitForOutput) {
					sem.release();
				}
				
				System.out.println(value);
				this.processStreamBuffer.addOutputLine(value);
				this.commandResponseHandler.response(value, false);
			}
		}
//		super.shellOutputChanged(event);
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

	public boolean isWaitForOutput() {
		return waitForOutput;
	}

	public void setWaitForOutput(boolean waitForOutput) {
		this.waitForOutput = waitForOutput;
	}
}

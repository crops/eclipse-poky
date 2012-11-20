package org.yocto.bc.remote.utils;


public class YoctoCommand {
	private String command;
	private String initialDirectory;
	private String arguments;
	private ProcessStreamBuffer processBuffer;
	
	public YoctoCommand(String command, String initialDirectory, String arguments){
		this.setCommand(command);
		this.setInitialDirectory(initialDirectory);
		this.setArguments(arguments);
		this.setProcessBuffer(new ProcessStreamBuffer());
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getInitialDirectory() {
		return initialDirectory;
	}

	public void setInitialDirectory(String initialDirectory) {
		this.initialDirectory = initialDirectory;
	}

	public String getArguments() {
		return arguments;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public ProcessStreamBuffer getProcessBuffer() {
		return processBuffer;
	}

	public void setProcessBuffer(ProcessStreamBuffer processBuffer) {
		this.processBuffer = processBuffer;
	}

	@Override
	public String toString() {
		return command + " " + arguments;
	}
}

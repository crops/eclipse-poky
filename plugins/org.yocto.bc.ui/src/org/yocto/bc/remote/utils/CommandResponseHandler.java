package org.yocto.bc.remote.utils;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.yocto.bc.bitbake.ICommandResponseHandler;

public class CommandResponseHandler implements ICommandResponseHandler {
	private MessageConsoleStream consoleStream;
	private Boolean errorOccured = false;

	public CommandResponseHandler(MessageConsole console) {
		try {
			this.consoleStream = console.newMessageStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Boolean hasError() {
		return errorOccured;
	}

	public void response(String line, boolean isError) {
		try {
			if (isError) {
				consoleStream.println(line);
				errorOccured = true;
			} else {
				consoleStream.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
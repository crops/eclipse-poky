package org.yocto.bc.remote.utils;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;

public class ConsoleRunnable implements Runnable{
	MessageConsole console;
	ConsoleRunnable (MessageConsole console){
		this.console = console;
	}
	@Override
	public void run() {
		IWorkbench wb = PlatformUI.getWorkbench();
		if (wb == null)
			return;
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		if (win == null)
			return;
		IWorkbenchPage page = win.getActivePage();
		if (page == null)
			return;
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		try {
			IConsoleView view = (IConsoleView) page.showView(id);
			if (view == null)
				return;
			view.display(console);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
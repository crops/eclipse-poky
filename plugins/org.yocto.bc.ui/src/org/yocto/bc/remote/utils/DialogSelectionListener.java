package org.yocto.bc.remote.utils;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;

class DialogSelectionListener implements SelectionListener{
	Combo comboChoices;
	Shell shell;
	int returnValue;
	
	DialogSelectionListener(Shell shell,Combo comboChoices){
		this.comboChoices = comboChoices;
		this.shell = shell;
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		this.returnValue = comboChoices.getSelectionIndex();
		shell.dispose();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		
	}
	
	public int getReturnValue(){
		return returnValue;
	}
}
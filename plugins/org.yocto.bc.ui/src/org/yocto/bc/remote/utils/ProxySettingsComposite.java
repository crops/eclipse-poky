package org.yocto.bc.remote.utils;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ProxySettingsComposite extends Composite {

	private CheckboxTableViewer viewer;
	private Button button;
	private Table table;
	
	public ProxySettingsComposite(Composite parent, int style) {
		super(parent, style);
		createContents();
	}

	public void createContents(){
		Group group = new Group(this, SWT.NONE);
		button = new Button(group, SWT.CHECK);
		button.setEnabled(true);
		button.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (button.getSelection()) {
					table.setEnabled(true);
				} else {
					table.setEnabled(false);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		table = new Table(group, SWT.NONE);
		viewer = new CheckboxTableViewer(table);
		IBaseLabelProvider labelProvider = new ProxyTableLabelProvider();
		viewer.setLabelProvider(labelProvider);
		createColumnHeaders();
		IContentProvider contentProvider = new ProxyTableContentProvider();
		viewer.setContentProvider(contentProvider);
	}

	private void createColumnHeaders() {
		String[] titles = {"", "Scheme", "Host", "Port"};
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableViewerColumn(viewer, SWT.NONE).getColumn();
			column.setText(titles[i]);
			column.setResizable(true);
		}
	}
}

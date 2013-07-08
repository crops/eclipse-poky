/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.yocto.remote.utils;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.RemoteUIServices;
import org.eclipse.ptp.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * Allows the user to select a provider of Remote Services for a
 * RemoteBuildServiceProvider.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * @author crecoskie
 * @see org.eclipse.ptp.rdt.ui.serviceproviders.RemoteBuildServiceProvider
 * @since 2.0
 */
public class RemoteConnectionBox {

	private IRemoteConnection fSelectedConnection;

	private RemoteConnectionWidget fRemoteConnectionWidget;

	/**
	 * @since 3.1
	 */
	public RemoteConnectionBox(Composite composite, IRemoteConnection conn) {
		createContents(composite);
		this.fSelectedConnection = conn;
		if (fSelectedConnection != null)
			fRemoteConnectionWidget.setConnection(fSelectedConnection);
	}

	/**
	 * Returns the name of the selected connection.
	 */
	public IRemoteConnection getConnection() {
		return fSelectedConnection;
	}

	public IRemoteConnection getRemoteConnection() {
		return fSelectedConnection;
	}

	public IRemoteServices getRemoteServices() {
		if (fSelectedConnection != null)
			return fSelectedConnection.getRemoteServices();
		return null;
	}

	/**
	 * Return whether or not we are currently showing the default location for
	 * the project.
	 * 
	 * @return boolean
	 */
	public boolean isDefault() {
		return false;
	}

	/**
	 * Attempt to open a connection.
	 */
	public void checkConnection() {
		IRemoteUIConnectionManager mgr = getUIConnectionManager();
		if (mgr != null) {
			mgr.openConnectionWithProgress(fRemoteConnectionWidget.getShell(), null, fSelectedConnection);
		}
	}

	/**
	 * @return
	 */
	private IRemoteUIConnectionManager getUIConnectionManager() {
		IRemoteUIConnectionManager connectionManager = RemoteUIServices.getRemoteUIServices(fSelectedConnection.getRemoteServices())
				.getUIConnectionManager();
		return connectionManager;
	}

	private void handleConnectionSelected() {
		fSelectedConnection = fRemoteConnectionWidget.getConnection();
	}

	protected Control createContents(Composite parent) {
		Group container = new Group(parent, SWT.SHADOW_ETCHED_IN);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		container.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		container.setLayoutData(gd);
		
		fRemoteConnectionWidget = new RemoteConnectionWidget(container, SWT.NONE, null, RemoteConnectionWidget.FLAG_FORCE_PROVIDER_SELECTION, null);
		gd = new GridData();
		gd.horizontalSpan = 3;
		fRemoteConnectionWidget.setLayoutData(gd);
		fRemoteConnectionWidget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleConnectionSelected();
			}
		});

		return container;
	}

}

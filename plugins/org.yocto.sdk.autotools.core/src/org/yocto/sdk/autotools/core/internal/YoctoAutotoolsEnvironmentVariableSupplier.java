/*******************************************************************************
 * Copyright (c) 2018 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.yocto.sdk.autotools.core.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.yocto.sdk.core.YoctoProjectEnvironmentVariableSupplier;

public class YoctoAutotoolsEnvironmentVariableSupplier extends YoctoProjectEnvironmentVariableSupplier implements IConfigurationEnvironmentVariableSupplier {

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration,
			IEnvironmentVariableProvider provider) {

		List<IBuildEnvironmentVariable> variables = new ArrayList<IBuildEnvironmentVariable>();

		// TODO: inherit variables from AutotoolsEnvironmentVariableSupplier?
		variables.add(new BuildEnvironmentVariable("V", "1")); //$NON-NLS-1$ //$NON-NLS-2$
		variables.addAll(Arrays.asList(super.getVariables(configuration, provider)));

		return variables.toArray(new IBuildEnvironmentVariable[]{});
	}

}

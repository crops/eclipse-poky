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
package org.yocto.sdk.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.resources.IProject;
import org.yocto.sdk.core.preference.YoctoProjectProfilePreferences;
import org.yocto.sdk.core.preference.YoctoProjectProjectPreferences;

/**
 *
 * An environment variable supplier which extracts environment variables from
 * environment setup script
 *
 * @author Intel Corporation
 *
 */
public class YoctoProjectEnvironmentVariableSupplier implements IConfigurationEnvironmentVariableSupplier {

	public class BuildEnvironmentVariable extends EnvironmentVariable implements IBuildEnvironmentVariable {

		public BuildEnvironmentVariable(String name, String value) {
			super(name, value);
		}
	}

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		for (IBuildEnvironmentVariable variable : getVariables(configuration, provider)) {
			if (variableName.equals(variable.getName()))
				return variable;
		}
		return null;
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration,
			IEnvironmentVariableProvider provider) {

		List<BuildEnvironmentVariable> variables = new ArrayList<BuildEnvironmentVariable>();

		IProject project = (IProject) configuration.getOwner();
		YoctoProjectProjectPreferences projectPreferences = YoctoProjectProjectPreferences.create(project);
		YoctoProjectProfilePreferences profilePreferences = projectPreferences.getProfilePreferences();

		// Don't load environment variables unless the profile preferences are valid
		if (profilePreferences != null) {
			Map<String, String> envVars = new HashMap<String, String>();

			if (profilePreferences.isUseContainer()) {
				// TODO: Do not load variables if we're building within containers?
			} else {

				YoctoProjectEnvironmentSetupScript envSetupScript = profilePreferences.getEnvironmentSetupScript();

				if (envSetupScript != null) {
					envVars = envSetupScript.getEnvironmentVariables();
				}
			}

			for (String envVarName : envVars.keySet()) {
				variables.add(new BuildEnvironmentVariable(envVarName, envVars.get(envVarName)));
			}
		}

		return variables.toArray(new IBuildEnvironmentVariable[] {});
	}
}

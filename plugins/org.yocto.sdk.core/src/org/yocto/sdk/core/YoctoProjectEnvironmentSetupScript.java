package org.yocto.sdk.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;

public class YoctoProjectEnvironmentSetupScript {

	public static final String ENVIRONMENT_SETUP_SCRIPT_PREFIX = "environment-setup-"; //$NON-NLS-1$

	/**
	 * Convenient method for discovering environment setup script from a build
	 * directory
	 *
	 * @param toolchainDir
	 * @return environment setup script, or null if none found
	 */
	public static File getEnvironmentSetupScript(File toolchainDir) {

		if (toolchainDir == null || !toolchainDir.exists() || !toolchainDir.isDirectory())
			return null;

		for (File file : toolchainDir.listFiles()) {
			// Only return the first matching file
			if (file.getName().startsWith(ENVIRONMENT_SETUP_SCRIPT_PREFIX)) {
				return file;
			}
		}
		return null;
	}

	public static YoctoProjectEnvironmentSetupScript create(File toolchainDir) {

		File envSetupScript = getEnvironmentSetupScript(toolchainDir);

		if (envSetupScript == null)
			return null;

		return new YoctoProjectEnvironmentSetupScript(envSetupScript);
	}

	HashMap<String, String> envMap = new HashMap<String, String>();

	String targetPrefix;

	YoctoProjectEnvironmentSetupScript(File envSetupScript) {
		loadEnvironmentVariables(envSetupScript);
		this.targetPrefix = envSetupScript.getName().replaceFirst(ENVIRONMENT_SETUP_SCRIPT_PREFIX, ""); //$NON-NLS-1$
	}

	/**
	 *
	 * @return target prefix extracted from environment setup script filename
	 */
	public String getTargetPrefix() {
		return this.targetPrefix;
	}

	/**
	 *
	 * @return environment variables extracted from environment setup script
	 */
	public HashMap<String, String> getEnvironmentVariables() {
		return this.envMap;
	}

	@SuppressWarnings("nls")
	void loadEnvironmentVariables(File environmentSetupScript) {

		try {

			BufferedReader input = new BufferedReader(new FileReader(environmentSetupScript));

			try {
				String line = null;

				while ((line = input.readLine()) != null) {
					if (!line.startsWith("export")) {
						continue;
					}
					String sKey = line.substring("export".length() + 1, line.indexOf('='));
					String sValue = line.substring(line.indexOf('=') + 1);
					if (sValue.startsWith("\"") && sValue.endsWith("\""))
						sValue = sValue.substring(sValue.indexOf('"') + 1, sValue.lastIndexOf('"'));
					/* If PATH ending with $PATH, we need to join with current system path */
					if (sKey.equalsIgnoreCase("PATH") && (sValue.lastIndexOf("$PATH") >= 0)) {
						if (this.envMap.containsKey(sKey)) {
							sValue = sValue.substring(0, sValue.lastIndexOf("$PATH")) + this.envMap.get(sKey);
						} else {
							sValue = sValue.substring(0, sValue.lastIndexOf("$PATH")) + System.getenv("PATH");
						}
					}

					if (sValue.toUpperCase().contains("$SDKTARGETSYSROOT")) {
						String rValue = sValue.replaceAll(Matcher.quoteReplacement("$SDKTARGETSYSROOT"),
								this.envMap.get("SDKTARGETSYSROOT"));
						this.envMap.put(sKey, rValue);
					} else {
						this.envMap.put(sKey, sValue);
					}

					// System.out.printf("get env key %s value %s\n", sKey, sValue);
				}
			} finally {
				input.close();
			}

		} catch (IOException e) {
			throw new RuntimeException(
					"Unable to parse environment setup script: " + environmentSetupScript.getAbsolutePath(), e);
		}

	}
}

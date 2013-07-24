package org.yocto.remote.sdk.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.autotools.core.AutotoolsNewMakeGenerator;
import org.eclipse.cdt.internal.autotools.core.ErrorParser;
import org.eclipse.cdt.internal.autotools.core.ErrorParserManager;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.rse.core.model.IHost;
import org.yocto.remote.utils.RemoteHelper;

public class RemoteAutotoolsNewMakeGenerator extends AutotoolsNewMakeGenerator {
	
	@Override
	protected int runScript(String commandPath, URI runPath, String[] args,
			String jobDescription, String errMsg, IConsole console,
			ArrayList<String> additionalEnvs, 
			boolean consoleStart) throws BuildException, CoreException,
			NullPointerException, IOException {

		int rc = IStatus.OK;
		boolean removePWD = false;
		
		removeAllMarkers(project);
		
		// We want to run the script via the shell command.  So, we add the command
		// script as the first argument and expect "sh" to be on the runtime path.
		// Any other arguments are placed after the script name.
		String[] configTargets = null;
		if (args == null)
			configTargets = new String[1];
		else {
			configTargets = new String[args.length+1];
			System.arraycopy(args, 0, configTargets, 1, args.length);
		}
        configTargets[0] = getPathString(commandPath);
        // Fix for bug #343879
        if (Platform.getOS().equals(Platform.OS_WIN32)
                || Platform.getOS().equals(Platform.OS_MACOSX))
        	removePWD = true;
        
        // Fix for bug #343731 and bug #371277
        // Always use sh -c for executing autotool scripts which should
        // work on all Linux POSIX compliant shells including bash, dash, as
        // well as Windows and Mac OSX.
		URI prjLoc = project.getLocationURI();
        String command = "cd " + prjLoc.getPath() +"; ";
        for (String arg : configTargets) {
        	// TODO check for spaces in args
        	if (command == null)
        		command = arg;
        	else
        		command += " " + arg;
        }
        configTargets = new String[] { "-c \"", command };
        
        for (int i = 0; i < configTargets.length; ++i) {
			// try to resolve the build macros in any argument
			try{
				String resolved =
					ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
							configTargets[i],
							"", //$NON-NLS-1$
							" ", //$NON-NLS-1$
							IBuildMacroProvider.CONTEXT_CONFIGURATION,
							cfg);
				// strip any env-var settings from options
				// fix for bug #356278
				if (resolved.length() > 0 && resolved.charAt(0) != '-')
					resolved = stripEnvVarsFromOption(resolved, additionalEnvs);
				configTargets[i] = fixEscapeChars(resolved);
			} catch (BuildMacroException e) {
			}
		}
		
		String[] msgs = new String[2];
		msgs[0] = commandPath.toString();
		msgs[1] = project.getName();
		monitor.subTask(AutotoolsPlugin.getFormattedString(
				"MakeGenerator.make.message", msgs)); //$NON-NLS-1$


		ConsoleOutputStream consoleOutStream = null;
//		ErrorParserManager epm = null;
		StringBuffer buf = new StringBuffer();

		// Launch command - main invocation
		if (consoleStart)
			console.start(project);
		
		try {
			consoleOutStream = console.getOutputStream();
			String[] consoleHeader = new String[3];

			consoleHeader[0] = jobDescription;
			consoleHeader[1] = toolsCfg.getId();
			consoleHeader[2] = project.getName();
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
			buf.append(jobDescription);
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$

			// Display command-line environment variables that have been stripped by us
			// because launch showCommand won't do this.
			if (additionalEnvs != null && additionalEnvs.size() > 0) {
				buf.append(AutotoolsPlugin
							.getResourceString("MakeGenerator.commandline.envvars"));
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
				buf.append("\t");
				for (int i = 0; i < additionalEnvs.size(); ++i) {
					String envvar = additionalEnvs.get(i);
					buf.append(envvar.replaceFirst("(\\w+=)(.*)"," $1\"$2\""));
				}
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
			}
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();

			// Get a launcher for the config command
//			CommandLauncher launcher = new CommandLauncher();
			// Set the environment
			IEnvironmentVariable variables[] = 
					CCorePlugin.getDefault().getBuildEnvironmentManager().getVariables(cdesc, true);
			String[] env = null;
			ArrayList<String> envList = new ArrayList<String>();
			if (variables != null) {
				for (int i = 0; i < variables.length; i++) {
					// For Windows/Mac, check for PWD environment variable being passed.
					// Remove it for now as it is causing errors in configuration.
					// Fix for bug #343879
					if (!removePWD || !variables[i].getName().equals("PWD")) { // $NON-NLS-1$
						String value = variables[i].getValue();
						// The following is a work-around for bug #407580.  Configure doesn't recognize
						// a directory with a trailing separator at the end is equivalent to the same
						// directory without that trailing separator.  This problem can cause
						// configure to try and link a file to itself (e.g. projects with a GnuMakefile) and
						// obliterate the contents.  Thus, we remove the trailing separator to be safe.
						if (variables[i].getName().equals("PWD")) { // $NON-NLS-1$
							if (value.charAt(value.length()-1) == IPath.SEPARATOR)
								value = value.substring(0, value.length() - 1);	
						}
						envList.add(variables[i].getName()
								+ "=" + value); //$NON-NLS-1$
					}
				}
				if (additionalEnvs != null)
					envList.addAll(additionalEnvs); // add any additional environment variables specified ahead of script
				env = (String[]) envList.toArray(new String[envList.size()]);
			}

			// Hook up an error parser manager
//			epm = new ErrorParserManager(project, runPath, this, new String[] {ErrorParser.ID});
//			epm.setOutputStream(consoleOutStream);
//			epm.addErrorParser(ErrorParser.ID, new ErrorParser(getSourcePath(), getBuildPath()));

//			OutputStream stdout = epm.getOutputStream();
//			OutputStream stderr = stdout;

			//launcher.showCommand(true);
			// Run the shell script via shell command.
//			RSEHelper.remoteShellExec();
			prjLoc = project.getLocationURI();
			IRemoteConnection remConn = RemoteHelper.getConnectionByURI(prjLoc);
			//FIXME: this should not be exclusively on RSE
			IHost host = RemoteHelper.getRemoteConnectionByName(remConn.getName());

			String configTargetsStr = "";
			for(String target: configTargets) {
				configTargetsStr += target + " ";
			}

			Process proc = RemoteHelper.remoteShellExec(host, "", "/bin/sh", configTargetsStr, env, new NullProgressMonitor());
			
			if (proc != null) {
				try {
					// Close the input of the process since we will never write to
					// it
					proc.getOutputStream().close();
				} catch (IOException e) {
				}

//				if (launcher.waitAndRead(stdout, stderr, new SubProgressMonitor(
//						monitor, IProgressMonitor.UNKNOWN)) != CommandLauncher.OK) {
//					errMsg = launcher.getErrorMessage();
//				}

				// Force a resync of the projects without allowing the user to
				// cancel.
				// This is probably unkind, but short of this there is no way to
				// ensure
				// the UI is up-to-date with the build results
				// monitor.subTask(ManagedMakeMessages
				// .getResourceString(REFRESH));
				monitor.subTask(AutotoolsPlugin.getResourceString("MakeGenerator.refresh")); //$NON-NLS-1$
				try {
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					monitor.subTask(AutotoolsPlugin
							.getResourceString("MakeGenerator.refresh.error")); //$NON-NLS-1$
				}
			} else {
//				errMsg = launcher.getErrorMessage();
			}
			InputStream is = proc.getInputStream();
			try {
				if (is.available() != 0) {
					BufferedReader in = new BufferedReader(new InputStreamReader(is));
					String line;
					buf = new StringBuffer();
					while ((line = in.readLine()) != null) {
						buf.append(line);
						buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
						consoleOutStream.write(buf.toString().getBytes());
						consoleOutStream.flush();
						buf.delete(0, buf.length());
					}
				}
			 }catch(IOException e1) {
				 //do nothing
			 }
			// Report either the success or failure of our mission
			buf = new StringBuffer();
			if (errMsg != null && errMsg.length() > 0) {
				String errorDesc = AutotoolsPlugin
						.getResourceString("MakeGenerator.generation.error"); //$NON-NLS-1$
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				buf.append(errorDesc);
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				buf.append("(").append(errMsg).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				rc = IStatus.ERROR;
			} else {
				int exitValue = Integer.MIN_VALUE;
				while (true) {
					try {
						exitValue = proc.exitValue();
						break;
					} catch (Exception e) {
						//just ignore this since it means the process is not finished yet
					}
				}
				if (exitValue >= 1 || exitValue < 0) {
					// We have an invalid return code from configuration.
					String[] errArg = new String[2];
					errArg[0] = Integer.toString(proc.exitValue());
					errArg[1] = commandPath.toString();
					errMsg = AutotoolsPlugin.getFormattedString(
							"MakeGenerator.config.error", errArg); //$NON-NLS-1$
					buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
					buf.append(AutotoolsPlugin.getResourceString("MakeGenerator.generation.error")); //$NON-NLS-1$
					buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
					if (proc.exitValue() == 1)
						rc = IStatus.WARNING;
					else
						rc = IStatus.ERROR;
				}else {
					// Report a successful build
					String successMsg = 
							AutotoolsPlugin.getResourceString("MakeGenerator.success"); //$NON-NLS-1$
					buf.append(successMsg);
					buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
					rc = IStatus.OK;
				} 
			} 

			// Write message on the console
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();

			// // Generate any error markers that the build has discovered
			// monitor.subTask(ManagedMakeMessages
			// .getResourceString(MARKERS));
			// epm.reportProblems();
		} finally {
			if (consoleOutStream != null)
				consoleOutStream.close();
//			if (epm != null)
//				epm.close();
		}
		
		// If we have an error and no specific error markers, use the default error marker.
		if (rc == IStatus.ERROR && !hasMarkers(project)) {
			addMarker(project, -1, errMsg, SEVERITY_ERROR_BUILD, null);
		}
		
		return rc;
	}
}

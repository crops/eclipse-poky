package org.yocto.bc.ui.wizards.install;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.console.MessageConsole;
import org.yocto.bc.bitbake.ICommandResponseHandler;
import org.yocto.bc.remote.utils.CommandResponseHandler;
import org.yocto.bc.remote.utils.ConsoleWriter;
import org.yocto.bc.remote.utils.RemoteHelper;
import org.yocto.bc.remote.utils.YoctoCommand;
import org.yocto.bc.ui.Activator;
import org.yocto.bc.ui.model.ProjectInfo;
import org.yocto.bc.ui.wizards.FiniteStateWizard;
import org.yocto.bc.ui.wizards.newproject.BBConfigurationInitializeOperation;
import org.yocto.bc.ui.wizards.newproject.CreateBBCProjectOperation;

/**
 * A wizard for installing a fresh copy of an OE system.
 * 
 * @author kgilmer
 * 
 * A Wizard for creating a fresh Yocto bitbake project and new poky build tree from git
 * 
 * @modified jzhang
 * 
 */
public class InstallWizard extends FiniteStateWizard implements IWorkbenchWizard {

	static final String KEY_PINFO = "KEY_PINFO";
	protected static final String OPTION_MAP = "OPTION_MAP";
	protected static final String INSTALL_SCRIPT = "INSTALL_SCRIPT";
	protected static final String INSTALL_DIRECTORY = "Install Directory";
	protected static final String INIT_SCRIPT = "Init Script";
	
	protected static final String SELECTED_CONNECTION = "SEL_CONNECTION";
	protected static final String SELECTED_REMOTE_SERVICE = "SEL_REMOTE_SERVICE";

	protected static final String PROJECT_NAME = "Project Name";
	protected static final String DEFAULT_INIT_SCRIPT = "oe-init-build-env";
	protected static final String DEFAULT_INSTALL_DIR = "~/yocto";
	
	protected static final String GIT_CLONE = "Git Clone";
	public static final String VALIDATION_FILE = DEFAULT_INIT_SCRIPT;

	private Map<String, Object> model;
	private MessageConsole console;

	public InstallWizard() {
		this.model = new Hashtable<String, Object>();
		model.put(INSTALL_DIRECTORY, DEFAULT_INSTALL_DIR);
		model.put(INIT_SCRIPT, DEFAULT_INIT_SCRIPT);
		
		setWindowTitle("Yocto Project BitBake Commander");
		setNeedsProgressMonitor(true);
		
	}


	public InstallWizard(IStructuredSelection selection) {
		model = new Hashtable<String, Object>();
	}

	/*
	 * @Override public IWizardPage getNextPage(IWizardPage page) { if (page
	 * instanceof WelcomePage) { if (model.containsKey(WelcomePage.ACTION_USE))
	 * { return bbcProjectPage; } } else if (page instanceof ProgressPage) {
	 * return bitbakePage; }
	 * 
	 * if (super.getNextPage(page) != null) { System.out.println("next page: " +
	 * super.getNextPage(page).getClass().getName()); } else {
	 * System.out.println("end page"); }
	 * 
	 * return super.getNextPage(page); }
	 * 
	 * @Override public boolean canFinish() { System.out.println("can finish: "
	 * + super.canFinish()); return super.canFinish(); }
	 */
	@Override
	public void addPages() {
		addPage(new OptionsPage(model));
	}

	@Override
	public Map<String, Object> getModel() {
		return model;
	}

	@Override
	public boolean performFinish() {
		WizardPage page = (WizardPage) getPage("Options");
		page.setPageComplete(true);
		Map<String, Object> options = model;
		

		try {
			URI uri = new URI("");
			if (options.containsKey(INSTALL_DIRECTORY)) {
				uri = (URI) options.get(INSTALL_DIRECTORY);
			}
				
			if (((Boolean)options.get(GIT_CLONE)).booleanValue()) {
				String[] cmd = {"/usr/bin/git clone --progress", "git://git.yoctoproject.org/poky.git", uri.getPath()};
				final Pattern pattern = Pattern.compile("^Receiving objects:\\s*(\\d+)%.*");
				LongtimeRunningTask runningTask = new LongtimeRunningTask("Checking out Yocto git repository", cmd, null, null,
						((IRemoteConnection)model.get(InstallWizard.SELECTED_CONNECTION)), 
						((IRemoteServices)model.get(InstallWizard.SELECTED_REMOTE_SERVICE)),
					new ICalculatePercentage() {
						public float calWorkloadDone(String info) throws IllegalArgumentException {
							Matcher m = pattern.matcher(info.trim());
							if(m.matches()) {
								return new Float(m.group(1)) / 100;
							}else {
								throw new IllegalArgumentException();
							}
						}
					}
				);
				this.getContainer().run(true,true, runningTask);
			}
			CommandResponseHandler cmdHandler = RemoteHelper.getCommandHandler(RemoteHelper.getRemoteConnectionByName(((IRemoteConnection)model.get(InstallWizard.SELECTED_CONNECTION)).getName()));
			if (!cmdHandler.hasError()) {
				String initPath = "";
				if (uri.getPath() != null) {
					 initPath = uri.getPath() + "/" + (String) options.get(INIT_SCRIPT);
				} else {
					initPath = uri.getFragment() + "/" + (String) options.get(INIT_SCRIPT);
				}
				String prjName = (String) options.get(PROJECT_NAME);
				ProjectInfo pinfo = new ProjectInfo();
				pinfo.setInitScriptPath(initPath);
				pinfo.setLocation(uri);
				pinfo.setName(prjName);
				IRemoteConnection remConn = (IRemoteConnection) model.get(InstallWizard.SELECTED_CONNECTION);
				IHost connection = RemoteHelper.getRemoteConnectionByName(remConn.getName());
				pinfo.setConnection(connection);
				pinfo.setRemoteServices((IRemoteServices) model.get(InstallWizard.SELECTED_REMOTE_SERVICE));
			
				ConsoleWriter cw = new ConsoleWriter();
				this.getContainer().run(false, false, new BBConfigurationInitializeOperation(pinfo, cw));
				console = RemoteHelper.getConsole(connection);
				console.newMessageStream().println(cw.getContents());

				model.put(InstallWizard.KEY_PINFO, pinfo);
				Activator.putProjInfo(pinfo.getURI(), pinfo);

				this.getContainer().run(false, false, new CreateBBCProjectOperation(pinfo));
				return true;
				
			}
		} catch (Exception e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					IStatus.ERROR, e.getMessage(), e));
			this.getContainer().getCurrentPage().setDescription(
							"Failed to create project: " + e.getMessage());
		}
		return false;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	private interface ICalculatePercentage {
		public float calWorkloadDone(String info) throws IllegalArgumentException;
	}

	private class LongtimeRunningTask implements IRunnableWithProgress {
		static public final int TOTALWORKLOAD = 100;
		
		private String []cmdArray;
		private Process process;
		private String taskName;
		private int reported_workload;
		private IRemoteConnection connection;
		private IRemoteServices remoteServices;
		
		ICalculatePercentage cal;

		public LongtimeRunningTask(String taskName, 
				String []cmdArray, String []envp, File dir, 
				IRemoteConnection connection, IRemoteServices remoteServices,
				ICalculatePercentage calculator) {
			this.taskName = taskName;
			this.cmdArray = cmdArray;
			this.process = null;
			this.cal = calculator;
			this.connection = connection;
//			this.handler = RemoteHelper.getCommandHandler(RemoteHelper.getRemoteConnectionByName(connection.getName()));
			this.remoteServices = remoteServices;
		}

//		private void reportProgress(IProgressMonitor monitor,String info) {
//			if(cal == null) {
//				monitor.worked(1);
//			}else {
//				float percentage;
//				try {
//					percentage=cal.calWorkloadDone(info);
//				} catch (IllegalArgumentException e) {
//					//can't get percentage
//					return;
//				}
//				int delta=(int) (TOTALWORKLOAD * percentage - reported_workload);
//				if( delta > 0 ) {
//					monitor.worked(delta);
//					reported_workload += delta;
//				}
//			}
//		}

		synchronized public void run(IProgressMonitor monitor) 
				throws InvocationTargetException, InterruptedException {

//			boolean cancel = false;
			reported_workload = 0;

			try {
				
				monitor.beginTask(taskName, TOTALWORKLOAD);
				
				if (!connection.isOpen()) {
					try {
						connection.open(monitor);
					} catch (RemoteConnectionException e1) {
						e1.printStackTrace();
					}
				}

				if (!remoteServices.isInitialized()) {
					remoteServices.initialize();
				}

				String args = "";
				for (int i = 1; i < cmdArray.length; i++)
					args += cmdArray[i] + " ";
				try {
					RemoteHelper.runCommandRemote(RemoteHelper.getRemoteConnectionByName(connection.getName()), new YoctoCommand(cmdArray[0], "", args), monitor, true);
					RemoteHelper.runCommandRemote(RemoteHelper.getRemoteConnectionByName(connection.getName()), new YoctoCommand("pwd", "", ""), monitor, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				monitor.done();
				if (process != null ) {
					process.destroy();
				}
			}
		}
	}


}

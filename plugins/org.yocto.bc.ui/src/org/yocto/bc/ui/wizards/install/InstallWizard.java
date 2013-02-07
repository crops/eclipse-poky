package org.yocto.bc.ui.wizards.install;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dstore.core.model.CommandHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.console.MessageConsole;
import org.yocto.bc.remote.utils.CommandResponseHandler;
import org.yocto.bc.remote.utils.ConsoleWriter;
import org.yocto.bc.remote.utils.RemoteHelper;
import org.yocto.bc.remote.utils.YoctoCommand;
import org.yocto.bc.remote.utils.YoctoRunnableWithProgress;
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
	private OptionsPage optionsPage;

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
		optionsPage = new OptionsPage(model);
		addPage(optionsPage);
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
			IRemoteConnection remoteConnection = ((IRemoteConnection)model.get(InstallWizard.SELECTED_CONNECTION));
			IRemoteServices remoteServices = ((IRemoteServices)model.get(InstallWizard.SELECTED_REMOTE_SERVICE));
			IHost connection = RemoteHelper.getRemoteConnectionByName(remoteConnection.getName());
			CommandResponseHandler cmdHandler = new CommandResponseHandler(RemoteHelper.getConsole(connection));
			IWizardContainer container = this.getContainer();
			if (((Boolean)options.get(GIT_CLONE)).booleanValue()) {
				String cmd = "/usr/bin/git clone --progress";
				String args = "git://git.yoctoproject.org/poky.git " + uri.getPath();
				String taskName = "Checking out Yocto git repository";

				YoctoRunnableWithProgress adapter = new YoctoRunnableWithProgress(new YoctoCommand(cmd, "", args));

				adapter.setRemoteConnection(remoteConnection);
				adapter.setRemoteServices(remoteServices);
				adapter.setTaskName(taskName);
				try {
					container.run(true, true, adapter);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
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
				pinfo.setLocationURI(uri);
				pinfo.setName(prjName);
				pinfo.setConnection(connection);
				pinfo.setRemoteServices(remoteServices);

				final ConsoleWriter cw = new ConsoleWriter();
				BBConfigurationInitializeOperation configInitOp = new BBConfigurationInitializeOperation(pinfo, null);
				container.run(false, false, configInitOp);
				console = RemoteHelper.getConsole(connection);
				console.newMessageStream().println(cw.getContents());
				
				if (configInitOp.isErrorOccured()) {
					optionsPage.setErrorMessage(configInitOp.getErrorMessage());
					return false;
				}
				
				model.put(InstallWizard.KEY_PINFO, pinfo);
				Activator.putProjInfo(pinfo.getOEFSURI(), pinfo);

				container.run(false, false, new CreateBBCProjectOperation(pinfo));
				return true;
			}
		} catch (Exception e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					IStatus.ERROR, e.getMessage(), e));
			this.getContainer().getCurrentPage().setDescription("Failed to create project: " + e.getMessage());
		}
		return false;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

}

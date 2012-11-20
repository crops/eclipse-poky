package org.yocto.bc.ui.wizards.install;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.rdt.ui.wizards.RemoteProjectContentsLocationArea;
import org.eclipse.ptp.rdt.ui.wizards.RemoteProjectContentsLocationArea.IErrorMessageReporter;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.rse.core.RSEConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.yocto.bc.ui.wizards.FiniteStateWizardPage;

/**
 * Select which flavor of OE is to be installed.
 * 
 * @author kgilmer
 * 
 * Setting up the parameters for creating the new Yocto Bitbake project
 * 
 * @modified jzhang
 */
public class OptionsPage extends FiniteStateWizardPage {

	public static final String URI_SEPARATOR = "/";
	public static final String LOCALHOST = "LOCALHOST";
	
	private Composite top;
	
	private ValidationListener validationListener;
	private Text txtProjectName;
	private Button btnGit;
	private Button btnValidate;
	
	private RemoteProjectContentsLocationArea locationArea;
	
	protected OptionsPage(Map<String, Object> model) {
		super("Options", model);
		setMessage("Enter these parameters to create new Yocto Project BitBake commander project");
	}

	@Override
	public void createControl(Composite parent) {
		top = new Composite(parent, SWT.None);
		top.setLayout(new GridLayout());
		top.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridData gdFillH = new GridData(GridData.FILL_HORIZONTAL);
		
		Composite projectNameComp = new Composite(top, SWT.NONE);
		GridData gdProjName = new GridData(GridData.FILL_HORIZONTAL);
		projectNameComp.setLayoutData(gdProjName);
		projectNameComp.setLayout(new GridLayout(2, false));
		Label lblProjectName = new Label(projectNameComp, SWT.NONE);
		lblProjectName.setText("Project N&ame:");

		txtProjectName = new Text(projectNameComp, SWT.BORDER);
		txtProjectName.setLayoutData(gdFillH);
		txtProjectName.setFocus();
		validationListener = new ValidationListener();
		
		txtProjectName.addModifyListener(validationListener);

		IErrorMessageReporter errorReporter = new IErrorMessageReporter() {
			
			@Override
			public void reportError(String errorMessage, boolean infoOnly) {
				setMessage(errorMessage);
				validatePage();
				updateModel();
			}
		};
		
		locationArea = new RemoteProjectContentsLocationArea(errorReporter, top, null);
		
		Group locationValidationGroup = new Group(top, SWT.NONE);
		locationValidationGroup.setText("Git repository");
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL);
		locationValidationGroup.setLayoutData(gd);
		GridLayout gl = new GridLayout(1, false);
		locationValidationGroup.setLayout(gl);
		
		SelectionListener lst = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (validateProjectName() && validateProjectLocation()) 
					setPageComplete(true);
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		
		
		btnGit = new Button(locationValidationGroup, SWT.RADIO);
		btnGit.setText("Clone from Yocto Project &Git Repository into new location");
		btnGit.setEnabled(true);
		btnGit.setSelection(true);
		btnGit.addSelectionListener(lst);
		
		
		btnValidate = new Button(locationValidationGroup, SWT.RADIO);
		btnValidate.setText("&Validate existing Git project location");
		btnValidate.setEnabled(true);
		btnValidate.setSelection(false);
		btnValidate.addSelectionListener(lst);
		
		setControl(top);
	}
	
	private boolean validateProjectName() {
		IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();

		IStatus validate = ResourcesPlugin.getWorkspace().validateName(txtProjectName.getText(), IResource.PROJECT);

		if (txtProjectName.getText().trim().isEmpty()) {
			setErrorMessage("Project name cannot be empty!");
			return false;
		}
		
		if (!validate.isOK() || !isValidProjectName(txtProjectName.getText())) {
			setErrorMessage("Invalid project name: " + txtProjectName.getText());
			return false;
		}

		IProject proj = wsroot.getProject(txtProjectName.getText());
		if (proj.exists()) {
			setErrorMessage("A project with the name " + txtProjectName.getText() + " already exists");
			return false;
		}
		return true;
	}
	
	public String getProjectName(){
		return txtProjectName.getText().trim();
	}
	
	protected boolean validateProjectLocation() {
		
		String projectLoc = locationArea.getProjectLocation().trim();
		
		File checkProject_dir = new File(projectLoc);
		if (!checkProject_dir.isDirectory()) {
			setErrorMessage("The project location directory " + projectLoc + " is not valid");
			return false;
		}
		projectLoc = convertToRealPath(projectLoc);
		String separator = projectLoc.endsWith(URI_SEPARATOR) ? "" : URI_SEPARATOR;
		String projectPath = projectLoc + separator + getProjectName();
		File gitDir = new File(projectPath);
		if(btnValidate.getSelection()) {
			if(!gitDir.isDirectory() || !gitDir.exists()) {
				setErrorMessage("Directory " + projectPath + " does not exist, please select git clone.");
				return false;
			}
			File[] filesMatched = gitDir.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File file, String pattern) {
					return file.getName().equals(".git");
				}
			});
			
			if (filesMatched.length != 1) {
				setErrorMessage("Directory " + projectPath + " does not contain a git repository, please select git clone.");
				return false;
			}
			
			if(!new File(projectLoc + separator + InstallWizard.VALIDATION_FILE).exists()) {
				setErrorMessage("Directory " + projectPath + " seems invalid, please use other directory or project name.");
				return false;
			}
		}
		
		try {
			IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
			IProject proj = wsroot.getProject(txtProjectName.getText());
			if (proj.exists()) {
				setErrorMessage("A project with the name " + txtProjectName.getText() + " already exists");
				return false;
			}
			URI location = new URI("file:" + URI_SEPARATOR + URI_SEPARATOR + convertToRealPath(projectLoc) + URI_SEPARATOR + txtProjectName.getText());
			
			IStatus status = ResourcesPlugin.getWorkspace().validateProjectLocationURI(proj, location);
			if (!status.isOK()) {
				setErrorMessage(status.getMessage());
				return false;
			}
		} catch (Exception e) {
			setErrorMessage("Run into error while trying to validate entries!");
			return false;
		}
		
		setErrorMessage(null);
		return true;
	}

	private String convertToRealPath(String path) {
	    String patternStr = File.separator + File.separator;
	    if (patternStr.equals(URI_SEPARATOR))
	    	return path;
	    String replaceStr = URI_SEPARATOR;
	    String convertedpath;

	    //Compile regular expression
	    Pattern pattern = Pattern.compile(patternStr); //pattern to look for

	    //replace all occurance of percentage character to file separator
	    Matcher matcher = pattern.matcher(path);
	    convertedpath = matcher.replaceAll(replaceStr);

	    return convertedpath;
	}

	
	@Override
	public void pageCleanup() {

	}

	@Override
	public void pageDisplay() {
	}

	@Override
	
	protected void updateModel() {
		try {
			URI uri = getProjectLocationURI();
			if (uri != null)
				model.put(InstallWizard.INSTALL_DIRECTORY, getProjectLocationURI());
		} catch (Exception e){
			e.printStackTrace();
		}
		model.put(InstallWizard.PROJECT_NAME, txtProjectName.getText());
		model.put(InstallWizard.GIT_CLONE, new Boolean(btnGit.getSelection()));
		model.put(InstallWizard.SELECTED_CONNECTION, locationArea.getRemoteConnection());
		model.put(InstallWizard.SELECTED_REMOTE_SERVICE, locationArea.getRemoteServices());
	}

	public URI getProjectLocationURI() throws URISyntaxException {
		URI uri = locationArea.getProjectLocationURI();
		
		if (uri != null) {
			String location = locationArea.getProjectLocation();
			if (!uri.getPath().isEmpty()) {
				String separator = uri.getPath().endsWith(URI_SEPARATOR) ? "" : URI_SEPARATOR;
				
				return new URI( uri.getScheme(),
								uri.getHost(),
								uri.getPath() + separator + txtProjectName.getText(),
								uri.getFragment());
			} else {
				return null;
//				String defaultPath = getDefaultPathDisplayString(locationArea.getRemoteConnection(), locationArea.getRemoteServices());
//				return new URI(uri.getScheme(), uri.getHost(), defaultPath, uri.getFragment());
			}
		} else {
			String location = locationArea.getProjectLocation();
			String separator = location.endsWith(URI_SEPARATOR) ? "" : URI_SEPARATOR;
			
			IRemoteConnection conn = locationArea.getConnection();
			if (conn instanceof RSEConnection) {
				RSEConnection rseConn = (RSEConnection)conn;
				return new URI("rse", rseConn.getHost().getHostName(), location);
			} else {
				return new URI( "file", location + separator + txtProjectName.getText(),"");
			}
		}
	}
	
	private String getDefaultPathDisplayString(IRemoteConnection connection, IRemoteServices remoteServices) {
		String projectName = getProjectName();
		if (projectName.isEmpty())
			projectName = "yocto";
		if (connection != null) {
			if (!connection.isOpen())
				try {
					connection.open(null);
				} catch (RemoteConnectionException e) {
					e.printStackTrace();
				}
			
			IRemoteFileManager fileMgr = remoteServices.getFileManager(connection);
			URI defaultURI = fileMgr.toURI(connection.getWorkingDirectory());

			// Handle files specially. Assume a file if there is no project to
			// query
			if (defaultURI != null && defaultURI.getScheme().equals("file")) {
				return Platform.getLocation().append(projectName).toOSString();
			}
			if (defaultURI == null) {
				return ""; //$NON-NLS-1$
			}
			return new Path(defaultURI.getPath()).append(projectName).toOSString();
		}
		return ""; //$NON-NLS-1$
	}
	
	private boolean isValidProjectName(String projectName) {
		if (projectName.indexOf('$') > -1) {
			return false;
		}

		return true;
	} 
	@Override
	protected boolean validatePage() {
		if  (!validateProjectName())
			return false;
		
		setErrorMessage(null);
		setMessage("All the entries are valid, press \"Finish\" to start the process, "+
				"this will take a while. Please don't interrupt till there's output in the Yocto Console window...");
		return true;
	}

}

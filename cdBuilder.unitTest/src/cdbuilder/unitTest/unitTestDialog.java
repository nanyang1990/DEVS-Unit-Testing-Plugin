/* This file is created by Nan Yang in 01/13/2014
 * 
 * This file is highly based on cdBuilder.buttons.RunSimulationDialog form 
 * the CDBuilderPluginDevelopmentProject_from_Matias project
 * 
 * Certain function have been added to accomplish unit test objective.
 */

package cdbuilder.unitTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import cdBuilder.CDBuilderPlugin;
import cdBuilder.buttons.OverwriteGui;
import cdBuilder.simulator.SimulatorPlugin;
import cdbuilder.unitTest.SimulationRunner.SimulationRunListener;
import cdbuilder.unitTest.fileDealer.fileExtracter;
import cdbuilder.unitTest.fileDealer.fileGenerater;
import cdbuilder.unitTest.views.UnitTestView;
import cdbuilder.unitTest.views.infoHolder;

public class unitTestDialog extends Dialog {

	static String absoluteProjectPath = null;

	// Dialog labels
	private static final String Header_FIELD_LABEL = "CPP file name (.cpp)";
	private static final String EVENT_FIELD_LABEL = "Event file name (.ev)";
	private static final String EXPECT_OUTPUT_FIELD_LABEL = "Expected output file name (.out) (Note: Please create your expected output file.\n Data shall be seperated by White Space. Like 00:05:00:000 outcash 200 , one output per line)";

	private static final String OUTPUT_FIELD_LABEL = "Output file name (.out)";
	private static final String LOG_FIELD_LABEL = "Log file name (.log)";
	private static final String STOP_TIME_FIELD_LABEL = "Simulation stop time (hh:mm:ss:ms)"
			+ "\n"
			+ "(NOTE: unchecked time option means 'infinity' as stop time)";
	private static final String PARAMERTERS_FIELD_LABEL = "Advanced Users Only. Enter desired parameters:";
	private static final String COMMENTS_FIELD_LABEL = "Comments";
	private static final String AUTOGENERATE_NAMES_LABEL = "Autogenerate the names for \"out\", \"log\" files:";
	private static final String DIALOG_TITLE = "Unit Test Simulate Project";

	// Default values
	private static final boolean DEFAULT_EVENT_FILE_ENABLED = false; // event
																		// file
																		// checkbox
																		// unchecked
																		// initially
	private static final boolean DEFAULT_TIME_FIELD_ENABLED = true;// time
																	// checkbox
																	// is
																	// selected
																	// initially
	private static boolean checkedOut = true;// out file checkbox is selected
												// initially
	private static boolean checkedLog = true;// log file checkbox is selected
												// initially

	private static boolean checkedAdv = false;// advanced checkbox unchecked
												// initially
	// private static boolean checkedComment = false;

	private static boolean checkedAutoLog = false; // automate name generation
													// for log files
	private static boolean checkedAutoOut = false; // automate name generation
													// for out files

	private SimulationRunner simulationRunner = null;

	private boolean OK2Close = true;
	private HashMap<String, Integer> maVersions;
	private Shell parent;

	private static String maName;

	private Button simuButton;
	private Button doneButton;
	private Button killButton;
	private Button loadButton;
	private Button saveButton;

	private Button checkBoxEv;
	private Button checkBoxOut;
	private Button checkBoxTime;
	private Button checkBoxLog;
	private Button checkBoxAdv;
	private Button checkBoxComment;

	private Button logFieldAutoGenerationCheckbox;
	private Button outFieldAutoGenerationCheckbox;

	private Button fileButtonEv;
	private Button fileButtonMa;
	private Button fileButtonLog;
	private Button fileButtonOut;
	private Button fileButtonExpectrfOut; // Nan

	private String MaFileName;

	private static Text textExpectedOutput;// Nan to save

	private Text textHeader;// Nan

	private Text textEv;
	private static Text textOut;
	private Text textLog;
	private Text textTime1, textTime2, textTime3, textTime4;
	private Text textAdv;
	private Text textComment;

	private static String outAutoName;
	private static String logAutoName;
	private static String stdOutText;
	private static String stdLogText;

	// private boolean simuKilled = false;

	// private IContainer container;
	private IWorkspaceRoot root;
	// private IPath projectPath;
	private IPath path;
	// private Process simuProcess;

	private Label errorMessageLabel;
	// private IInputValidator validator;

	// private boolean noErrors;

	// modifications by Sameen Rehman
	private Composite composite;
	private IFile defaultMaFile;
	private DecoratedLabel maFieldWarning;
	private static IContainer executionFolder;

	IViewPart sm;

	// unitTestResultView myview;

	/**
	 * constructor for SimuGui class
	 */
	public unitTestDialog(Shell parentShell, IContainer executionFolder) {

		super(parentShell);
		parent = parentShell;

		unitTestDialog.executionFolder = executionFolder;
		this.path = executionFolder.getLocation();
		this.root = (IWorkspaceRoot) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getInput();

		// this.projectPath = this.root.getLocation();
		// this.container = this.root.getContainerForLocation(this.projectPath);
		this.maVersions = new HashMap<String, Integer>();

		// Set simulation runner
		this.simulationRunner = new SimulationRunner(unitTestDialog.executionFolder);
		simulationRunner.addListener(new SimulationRunListener() {
			@Override
			public void simulationEnded(int status) {
				close();
			}
		});
	}

	/**
	 * constructor for SimuGui class
	 * 
	 * @throws CoreException
	 */
	public unitTestDialog(Shell parentShell, IFile maFile) throws CoreException {
		this(parentShell, maFile.getParent());
		this.defaultMaFile = maFile;
	}

	/**
	 * Determines if a text box is left empty and displays error message
	 * 
	 * @param parameter
	 *            :String
	 * @param textBox
	 *            :Text
	 * @return false of empty true otherwise
	 */
	private boolean isEmpty(String parameter, Text textBox) {
		String value = textBox.getText();
		if (value.isEmpty()) {
			MessageBox mbox = new MessageBox(parent, SWT.ICON_ERROR | SWT.OK
					| SWT.APPLICATION_MODAL);
			mbox.setText("Error");
			mbox.setMessage(parameter + " is not entered");
			mbox.open();

			textBox.setFocus();

			return true;
		}
		return false;
	}

	/**
	 * checks to see if the given string matches a file name found in the
	 * project directory
	 * 
	 * @param String
	 *            file, name of file
	 * @param String
	 *            errMsg, message to be displayed IF file was not found
	 */
	private boolean checkFileExistence(String file, String errMsg) {

		IWorkspaceRoot root = (IWorkspaceRoot) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getInput();
		IResource[] files;
		IFile fileHandle = null;

		try {
			files = root.getContainerForLocation(path).members();

			for (int i = 0; i < files.length; i++) {

				if (files[i] instanceof IFile
						&& files[i].getName().equalsIgnoreCase(file)) {
					fileHandle = (IFile) files[i];
					break;
				}
			}
			if (fileHandle == null) {

				MessageBox mbox = new MessageBox(parent, SWT.ICON_ERROR
						| SWT.OK | SWT.APPLICATION_MODAL);
				mbox.setText("Error");
				mbox.setMessage(errMsg);
				mbox.open();
				return false;
			}
			return true;

		} catch (CoreException e) {
			MessageBox mbox = new MessageBox(parent, SWT.ICON_ERROR | SWT.OK);
			mbox.setText("Error");
			mbox.setMessage("Error occured while accessing file");
			mbox.open();
			e.printStackTrace();
			// removed the try-catch for msg.wait()
			return false;
		}
	}

	/**
	 * This function will check to see if the file name has the specified
	 * extension and adds the extension otherwise. The final file name WITH
	 * extension is returned.
	 * 
	 * @param Text
	 *            name, the text box value containing the file name
	 * @param String
	 *            extention, the extention the name shoule have
	 * @return A string containing the file name with the proper extention
	 */
	public String checkExtention(Text name, String extention) {
		String fileName = name.getText().trim();

		if (!fileName.endsWith(extention)
				&& !fileName.endsWith(extention.toUpperCase()))
			return fileName.concat(extention);
		return fileName;
	}

	public String checkExtention(String name, String extention) {
		String fileName = name.trim();

		if (!fileName.endsWith(extention)
				&& !fileName.endsWith(extention.toUpperCase()))
			return fileName.concat(extention);
		return fileName;
	}

	/**
	 * This method defines the advanced field text when a BAT file is loaded by
	 * removing the unnecessary entries (ma, log, out, ev, time...)
	 * 
	 * @param command
	 *            :String - command line containing all parameters
	 * @param excludeFromAdvancedFieldText
	 *            :String[] - array of parameters that should not appear in
	 *            advanced field
	 * @return advField:String - string representation of text for advanced
	 *         field
	 * @author Chiril Chidisiuc
	 * @version 2006-07-11
	 */
	/*
	 * private String getAdvancedFieldText(String command,String []
	 * excludeFromAdvancedFieldText){ String advField = ""; int startParameter;
	 * int endParameter; int i=0; while (i<excludeFromAdvancedFieldText.length){
	 * if (excludeFromAdvancedFieldText[i]==null){ break; //no more entries } if
	 * (command.contains(excludeFromAdvancedFieldText[i])){ startParameter =
	 * command.indexOf(excludeFromAdvancedFieldText[i]); endParameter =
	 * startParameter + excludeFromAdvancedFieldText[i].length(); command =
	 * command.substring(0,startParameter) + command.substring(endParameter);
	 * //Chiril Chidisiuc: remove entry from command line } i++; } if
	 * (command.contains("exe")){ //Chiril Chidisiuc: remove C:/.../simu.exe
	 * (simuOrig.exe) from command line advField =
	 * command.substring(command.indexOf("exe")+4); } return advField.trim();
	 * //remove all spaces remaining after removing the entries }
	 */
	/**
	 * This method is used to obtain the type of the file that is being read.
	 * The types are as follows: '-1'=>name is not the same as MA file
	 * '0'==>name is the same as MA file, but doesn't contain version
	 * 'fileVersion'==>(fileVersion stands for any positive integer) name is the
	 * same as MA file and contains version X.
	 * 
	 * @param name
	 *            :String - name of the file being checked
	 * @param maName
	 *            :String - name of the MA file
	 * @return intger representation of the file version
	 * @author Chiril Chidisiuc
	 * @version 2006-06-24
	 */
	private int getTypeOfFileVersion(String name, String maName) {
		// String cleanName = "";
		boolean needToClean = false;
		int startIndex = 0;

		if (name.contains("LOG")) {
			startIndex = name.indexOf("LOG");
			needToClean = true;
		} else if (name.contains("OUT")) {
			startIndex = name.indexOf("OUT");
			needToClean = true;
		}

		if (needToClean == true) {
			int endIndex = startIndex + 3;
			name = name.substring(0, startIndex) + name.substring(endIndex);

			if (name.equals(maName)) {
				return 0; // name is the same as MA file
			}

			int startSequence = name.lastIndexOf("_");

			if (startSequence == -1 && name.equals(maName)) {
				return 0; // name is the same as MA file, but doesn't contain
							// version
			} else if (startSequence == -1 && !name.equals(maName)) {
				return -1; // name is not the same as MA file, and does not
							// contain version
			}

			String baseName = name.substring(0, startSequence);
			String sequence = name.substring(startSequence + 1);
			int fileVersion = this.getFileVersion(sequence);

			if (fileVersion != -1) { // an integer is returned
				if (baseName.equals(maName)) {
					return fileVersion; // name is the same as MA file, and
										// contains version
				} else {
					return -1; // name is not the same as MA file, and contains
								// version
				}
			}
		}
		return -1; // name not associated with any MA file
	}

	/**
	 * This method traverses the list of files provided and checks for the most
	 * recent version found...
	 * 
	 * @param list
	 *            :String[] - a string array containing the list of files
	 * @return none
	 * @author Chiril Chidisiuc
	 * 
	 * @version 2006-06-23
	 */
	private int traverseVersions(String[] list, String maName) {
		int versionToStore = ((Integer) this.maVersions.get(maName)).intValue();
		for (int i = 0; i < list.length; i++) {
			if (list[i] == null) { // no more files to check
				break;
			}
			int checkVersion = this.getTypeOfFileVersion(list[i], maName);
			if (checkVersion > versionToStore) {
				versionToStore = checkVersion;
			}
		}
		return versionToStore;
	}

	/**
	 * When this method is called, it checks for the latest version of the files
	 * and sets the appropriate version of simulation results. Example: if
	 * ATM_1.log or ATM_3.out already exist, the new set will be named ATM_4.log
	 * and ATM_4.out
	 * 
	 * @param none
	 * @return next version of simulation results (for names of *.log and *.out
	 *         files).
	 * @author Chiril Chidisiuc
	 * @version 2006-06-22
	 */
	private void setVersion(String maName) {
		this.maVersions.put(maName, new Integer(-1));
		String[] outFileList = this.getFileNames("out");
		String[] logFileList = this.getFileNames("log");
		// String[] batFileList = this.getFileNames("bat");
		// the folllowing three statements will check whichever file has the
		// latest version:

		// Search for latest version of "*.out" files
		int version1 = traverseVersions(outFileList, maName); // latest "out"
																// file
		// Search for latest version of "*.log" files
		int version2 = traverseVersions(logFileList, maName); // latest "log"
																// file
		int temp;
		if (version1 > version2) {
			temp = version1;
		} else if (version1 < version2) {
			temp = version2;
		} else {
			temp = version1;
		}
		temp++;
		this.maVersions.put(maName, new Integer(temp));
	}

	/**
	 * #getFileName(String extension):void - returns the list of all file names
	 * with such extension as a String array. extension must be entered as a
	 * parameter WITHOUT the ".". Examples of proper extensions: "exe", "ma",
	 * "ev", "doc" etc.
	 * 
	 * @param extension
	 *            :String - the file with this extension is checked
	 * @return fileName:String - name of the file is returned (without
	 *         extension!)
	 * @author Chiril Chidisiuc
	 * 
	 * @version 2006-06-07
	 */
	protected String[] getFileNames(String extension) {
		int count = 0;
		IResource[] projectFiles = null;

		try {
			projectFiles = this.root.getContainerForLocation(path).members();
		} catch (CoreException e) {
			SimulatorPlugin.getDefault().logError(
					"Problem in getFileName(extension:String):String[] - ", e);
		}

		// get files from working directory
		String[] fileNames = new String[projectFiles.length];
		for (int i = 0; i < projectFiles.length; i++) { // search files
			if (projectFiles[i] instanceof IFile
					&& (projectFiles[i].getName().toUpperCase().endsWith("."
							+ extension.toUpperCase()))) { // found the file
															// with specified
															// extension
				String fileNameWithExt = projectFiles[i].getName();
				int offset = fileNameWithExt.length() - 1 - extension.length();
				fileNames[count] = fileNameWithExt.substring(0, offset);
				count++;
			}
		}
		return fileNames;
	}

	/**
	 * This method collects all inputs of the dialog box and arranges them for
	 * further use as input for the command line. This method is called when
	 * either "Proceed" or "Save BAT file" is pressed.
	 * 
	 * ---modified by Chiril Chidisiuc 2006-06-12
	 * 
	 * This should be moved to the SimulationRunner
	 * 
	 * @param none
	 *            @return String containing the command line
	 * 
	 * @version 2006-06-13
	 */

	private String collectParameters() {// Nan modify TBD
		String[] param = new String[10];
		try {
			param[0] = "-m\"" + checkExtention(MaFileName, ".ma") + "\"";

			// event file input
			// Chiril Chidisiuc: NOTE - the event file is already dealt by
			// setEventFileName() in CreateDialogArea(Composite) method
			if (textEv.getEnabled() == true) {
				param[1] = "-e\"" + checkExtention(textEv, ".ev") + "\"";
			}

			// *.out file output
			if (textOut.getEnabled() == true) {
				param[2] = "-o\"" + checkExtention(textOut, ".out") + "\"";
			}

			// *.log file output
			if (textLog.getEnabled() == true) {
				param[3] = "-l\"" + checkExtention(textLog, ".log") + "\"";
			}

			// stop time parameter
			if (textTime1.getEnabled() == true) {
				String timeValue = textTime1.getText().trim() + ":"
						+ textTime2.getText().trim() + ":"
						+ textTime3.getText().trim() + ":"
						+ textTime4.getText().trim();
				param[4] = "-t\"" + timeValue + "\"";
			}

			// Advanced users text field input
			if (textAdv.getEnabled() == true) {
				param[5] = textAdv.getText().trim();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		int n = 0;
		String commandParameters = "";
		while (n < 6) {
			if (param[n] != null)
				commandParameters = commandParameters + " " + param[n];
			n++;
		}

		return commandParameters;
	}

	/**
	 * This method validates input parameters
	 */
	private boolean validateParameters() {
		try {
			// *.ma file input (user-selected)
			if (isEmpty("CPP file", textHeader)) // Nan
				return false;

			if (!checkFileExistence(checkExtention(textHeader, ".cpp"),
					"CPP file does not exist")) // Nan modify TBD
				return false;

			// *.out file expected (user-selected)
			if (isEmpty("Out file", textExpectedOutput)) // Nan
				return false;

			if (!checkFileExistence(checkExtention(textExpectedOutput, ".out"),
					"Expected output file does not exist")) // Nan modify TBD
				return false;

			// *.ev file
			if (textEv.getEnabled() == true) {
				if (isEmpty("Event", textEv))
					return false;

				if (!checkFileExistence(checkExtention(textEv, ".ev"),
						"Event file does not exist"))
					return false;
			}

			// *.out file output
			if (textOut.getEnabled() == true) {
				if (isEmpty("Output file", textOut))
					return false;
			}

			// *.log file output
			if (textLog.getEnabled() == true) {
				if (isEmpty("Log file", textLog))
					return false;
			}

			// stop time parameter
			if (textTime1.getEnabled() == true) {
				if (isEmpty("Hour value", textTime1)
						|| isEmpty("Minute value", textTime2)
						|| isEmpty("Second value", textTime3)
						|| isEmpty("Milli-second value", textTime3)) {

					return false;
				}
			}

			// Advanced users text field input
			if (textAdv.getEnabled() == true) {
				if (isEmpty("Advance parameter", textAdv))
					return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * This method is required to determine the version of the file (applied to
	 * *.out, *.log). This can later be used to determine the name of the next
	 * version (if such must be created) See collectParameters method for
	 * further use of this method. NOTE: this method has limits in max/min value
	 * that it can return...[-2147483648,2147483647]
	 * 
	 * @param sequence
	 *            :String - sequence which must be traversed and converted to
	 *            integer representation
	 * @return integer value of a string
	 * @throws NumberFormatException
	 * @author Chiril Chidisiuc
	 * 
	 * @version 2006-06-09
	 */
	protected int getFileVersion(String sequence) {
		try {
			return Integer.parseInt(sequence, 10);
		} catch (NumberFormatException e) {
			// EXAMPLE:
			// parseInt("2147483647", 10) returns 2147483647
			// parseInt("-2147483648", 10) returns -2147483648
			// parseInt("2147483648", 10) throws a NumberFormatException
			return -1;
		}
	}

	/**
	 * implementation of inherited method to define response to buttons
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 * @param buttonId
	 *            :int
	 * @return none
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.STOP_ID) { // Kill button
			this.simulationRunner.killSimulation();
			this.close();
		}
		if (buttonId == IDialogConstants.CLOSE_ID) { // Close button
			OK2Close = true;

			close();
		}
		if (buttonId == IDialogConstants.PROCEED_ID) { // Process button
			try {
				this.anlysisHeaderFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (!this.validateParameters()) {
				// Chiril Chidisiuc: to allow user correct the problem manually
				// and run the
				// simulation right after the modifications
				composite.setEnabled(true);
				simuButton.setEnabled(true);
				doneButton.setEnabled(true);
			} else {

				// run simulation
				try {
					runSimulation();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} // Nan

				try {
					
					executionFolder.refreshLocal(1, null);
					prepareUnitTestResultView();
					//showUnitTestResultView();
					// showUnitTestResult();
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		super.buttonPressed(buttonId);
	}

	private void runSimulation() throws InterruptedException {
		// get Parameters
		String parameters = collectParameters();

		// disable buttons
		killButton.setEnabled(true);
		simuButton.setEnabled(false);
		doneButton.setEnabled(false);

		System.out.println("command that will execute: " + parameters);
		simulationRunner.runSimulation(checkExtention(MaFileName, ".ma"),
				parameters);

	}

	/**
	 * sets up the Shell name
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 * @param shell
	 *            :Shell
	 * @return none
	 */
	protected void configureShell(Shell shell) {

		super.configureShell(shell);
		// the following line registers the shell of SimuGui to parent
		// which causes the file-dialog box modal to the SimuGui dialog
		// this fixes the problem associated with the file-dialog not being
		// modal to SimuGui
		parent = shell;
		if (DIALOG_TITLE != null)
			shell.setText(DIALOG_TITLE);
	}

	/**
	 * This method creates the dialog box when "Simulate" button is pressed. The
	 * location of fields (labels, text boxes, buttons) is defined here. This
	 * method also ensures that if an event file exists, it will be placed in
	 * the "Event file name (.ev)" text box (however, if there are more than one
	 * event file, please select the appropriate). Default event file name is
	 * first checked (event file with the same name as Makefile name). If no
	 * default event file found, then the first event file found in the
	 * directory is inserted. If no event file exists, the field remains
	 * inactive and user must locate event file himself, if desired. This method
	 * also responds to Makefile selection. When makefile is selceted and input
	 * into the field, the log and out file names are automatically generated
	 * and will be written over, if such names already exist.
	 * 
	 * --modified by Chiril Chidisiuc
	 * 
	 * @param parent
	 *            :Composite - the composite object is passed. Attributes of
	 *            dialog box are added to it.
	 * @return composite:Control - the object, which defines the structure and
	 *         behaviour of the dialog box
	 */
	protected Control createDialogArea(Composite parent) {
		// create composite
		composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(10, false)); // 11 column grid for
														// layout Nan

		createControls(parent);

		return composite;
	}

	/**
	 * creates button set
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 * @param parent
	 *            :Composite
	 * @return void
	 */

	protected void createButtonsForButtonBar(Composite parent) {
		// create OK, Cancel and Kill buttons
		simuButton = createButton(parent, IDialogConstants.PROCEED_ID,
				"Start Simulation", true);
		doneButton = createButton(parent, IDialogConstants.CLOSE_ID,
				IDialogConstants.CLOSE_LABEL, false);
		killButton = createButton(parent, IDialogConstants.STOP_ID,
				"Kill Simulation", false);

		addHandlers();

		setInitialControlsState();

		// do this here because setting the text will set enablement on the ok
		// button
		textHeader.setFocus();
	}

	/**
	 * #checkEventFileExistence():void - sets "checkedEv" to true if at least
	 * one event file is present in the project.
	 * 
	 * @param none
	 * @return none
	 * @author Chiril Chidisiuc
	 * 
	 * @version 2006-06-07
	 */
	private boolean eventFileExists() {
		String[] fileNamesTemp = getFileNames("ev");

		if (fileNamesTemp[0] == null) { // if first entry is null, then the rest
										// is null too
			return false;
		}
		// there are some event files (at least one entry is not "null")
		return true;
	}

	/**
	 * #setEventFileName():void - places name of the event file in dialog box,
	 * if the field is active (i.e. event file exists).
	 * 
	 * @param none
	 * @return none
	 * @author Chiril Chidisiuc
	 * 
	 * @version 2006-06-07
	 */
	private void setInitialEventFileName() {
		String[] eventFileNames = getFileNames("ev");
		String[] maFileNames = getFileNames("ma");

		if (eventFileNames[0] == null) {
			return;
		}
		for (int i = 0; i < eventFileNames.length; i++) {
			if (eventFileNames[i] == null) {
				break;
			}
			if (eventFileNames[i].equals(maFileNames[0])) {// set first default
															// name
															// (corresponding to
															// MA file name) if
															// such was found
				textEv.setText(maFileNames[0] + ".EV");
				return;
			}
		}
		textEv.setText(eventFileNames[0] + ".EV"); // set the first available
													// event file
		return;
	}

	private int getMaOffset(Text t) {
		int offset;
		if (textHeader.getText().length() >= 4
				&& textHeader.getText().toUpperCase().endsWith(".CPP")) { // Chiril
																			// Chidisiuc:
																			// set
																			// proper
																			// ending
			// of the MA file (offset)
			offset = textHeader.getText().length() - 4;
		} else {
			offset = textHeader.getText().length();
		}
		return offset;
	}

	private void setSimuFileNames(boolean out, boolean log) { // Chirill
																// Chidisiuc
		int offset = getMaOffset(textHeader);
		unitTestDialog.maName = textHeader.getText().substring(0, offset);
		// Chiril Chidisiuc: name is the same as
		// makefile, but without the ".MA" ending

		// String[] maFiles = getFileNames("ma");

		if (!maVersions.containsKey(maName)) {
			setVersion(maName);
		}
		int setThisVersion = ((Integer) maVersions.get(maName)).intValue();

		if (out) {
			if (checkedAutoOut) {
				if (textHeader.getText().length() != 0) {
					if (setThisVersion != 0) {
						// Chiril Chidisiuc: set appropriate name with index
						unitTestDialog.outAutoName = maName + "OUT_"
								+ setThisVersion + ".out";
						textOut.setText(unitTestDialog.outAutoName);
					} else {
						// Chiril Chidisiuc: set appropriate name without index
						unitTestDialog.outAutoName = maName + "OUT.out";
						textOut.setText(unitTestDialog.outAutoName);
					}
				}
			} else {
				// textOut.setText("previously launched OUT file...");
			}
		}
		if (log) {
			if (checkedAutoLog) {
				if (textHeader.getText().length() != 0) {
					if (setThisVersion != 0) {
						// Chiril Chidisiuc: set appropriate name with version
						unitTestDialog.logAutoName = maName + "LOG_"
								+ setThisVersion + ".log";
						textLog.setText(unitTestDialog.logAutoName);
					} else {
						// Chiril Chidisiuc: set appropriate name without
						// version (no previous records of name)
						unitTestDialog.logAutoName = maName + "LOG.log";
						textLog.setText(unitTestDialog.logAutoName);
					}
				}
			} else {
				// textLog.setText("previously launched LOG file...");
			}
		}
	}

	private void addHandlers() {
		// .ma field
		addTextMaChangedListener();

		// browse .ma
		fileButtonMa.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String fileName = NewFileGui("*.cpp");// modify by Nan
														// 01/26/2014

				if (fileName != null && !fileName.isEmpty())
					textHeader.setText(fileName);
			}
		});

		fileButtonExpectrfOut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String fileName = NewFileGui("*.out");// modify by Nan
														// 01/26/2014

				if (fileName != null && !fileName.isEmpty())
					textExpectedOutput.setText(fileName);
			}
		});

		// Check box for .ev
		checkBoxEv.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!checkBoxEv.getSelection()) {
					textEv.setEnabled(false);
					fileButtonEv.setEnabled(false);
				} else {
					textEv.setEnabled(true);
					fileButtonEv.setEnabled(true);
				}
			}
		});

		// Browse .ev
		fileButtonEv.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String fileName = NewFileGui("*.ev");

				if (!fileName.equals(""))
					textEv.setText(fileName);
			}
		});

		// auto generation for .out   // Nan 0212
		outFieldAutoGenerationCheckbox
				.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						boolean checked = ((Button) e.getSource())
								.getSelection(); // Chiril Chidisiuc
						checkedAutoOut = checked;
						if (checkedAutoOut) {
							setSimuFileNames(true, false);
						} else {
							textOut.setText(stdOutText);
						}
					}
				});

		// auto generation for .log
		logFieldAutoGenerationCheckbox
				.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						// TODO: selection LOG
						checkedAutoLog = !checkedAutoLog;
						if (checkedAutoLog) {
							setSimuFileNames(false, true);
						} else {
							textLog.setText(stdLogText); // TODO stdLogText
						}
					}
				});

		// .out checkbox
		checkBoxOut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!checkBoxOut.getSelection()) {
					textOut.setEnabled(false);
					outFieldAutoGenerationCheckbox.setEnabled(false); // Chiril
																		// Chidisiuc:
																		// disable
																		// name
																		// generation
																		// option
					outFieldAutoGenerationCheckbox.setSelection(false); // Chiril
																		// Chidisiuc:
																		// uncheck...
					fileButtonOut.setEnabled(false);
					checkedOut = false;
				} else {
					checkedOut = true;
					textOut.setEnabled(true);
					outFieldAutoGenerationCheckbox.setEnabled(true); // Chiril
																		// Chidisiuc:
																		// enable
																		// generation
																		// option
					outFieldAutoGenerationCheckbox.setSelection(checkedAutoOut); // Chiril
																					// Chidisiuc:
																					// this
																					// must
																					// selected
																					// as
																					// "last time"
					fileButtonOut.setEnabled(true);
				}
			}
		});

		// .out field
		textOut.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!outFieldAutoGenerationCheckbox.getSelection()) {
					stdOutText = textOut.getText();
				} else {
					if (!unitTestDialog.outAutoName.equals(textOut.getText())) {
						stdOutText = textOut.getText();
						checkedAutoOut = !checkedAutoOut;
						outFieldAutoGenerationCheckbox.setSelection(false);
					}
				}
			}
		});

		// .out browse
		fileButtonOut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String fileName = NewFileGui("*.out");

				if (!fileName.equals(""))
					textOut.setText(fileName);
			}
		});

		// .log checkbox
		checkBoxLog.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (textLog.getEnabled() == true) {
					textLog.setEnabled(false);
					logFieldAutoGenerationCheckbox.setEnabled(false); // Chiril
																		// Chidisiuc:
																		// disable
																		// name
																		// generation
																		// option
					logFieldAutoGenerationCheckbox.setSelection(false); // Chiril
																		// Chidisiuc:
																		// uncheck...
					fileButtonLog.setEnabled(false);
					checkedLog = false;
				} else {
					checkedLog = true;
					textLog.setEnabled(true);
					logFieldAutoGenerationCheckbox.setEnabled(true); // Chiril
																		// Chidisiuc:
																		// enable
																		// generation
																		// option
					logFieldAutoGenerationCheckbox.setSelection(checkedAutoLog); // Chiril
																					// Chidisiuc:
																					// this
																					// must
																					// selected
																					// as
																					// "last time"
					fileButtonLog.setEnabled(true);
				}
			}
		});

		// .log field
		textLog.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!logFieldAutoGenerationCheckbox.getSelection()) {
					stdLogText = textLog.getText();
				} else {
					if (!unitTestDialog.logAutoName.equals(textLog.getText())) {
						stdLogText = textLog.getText();
						checkedAutoLog = !checkedAutoLog;
						logFieldAutoGenerationCheckbox.setSelection(false);
					}
				}
			}
		});

		// .log browse
		fileButtonLog.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String fileName = NewFileGui("*.log");
				if (!fileName.equals(""))
					textLog.setText(fileName);
			}
		});

		// time checkbox
		checkBoxTime.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!checkBoxTime.getSelection()) {
					textTime1.setEnabled(false);
					textTime2.setEnabled(false);
					textTime3.setEnabled(false);
					textTime4.setEnabled(false);
				} else {
					textTime1.setEnabled(true);
					textTime2.setEnabled(true);
					textTime3.setEnabled(true);
					textTime4.setEnabled(true);
				}
			}
		});

		// time fields
		textTime1.addKeyListener(new NumberFieldKeyListener(textTime1));
		textTime2.addKeyListener(new NumberFieldKeyListener(textTime2));
		textTime3.addKeyListener(new NumberFieldKeyListener(textTime3));
		textTime4.addKeyListener(new NumberFieldKeyListener(textTime4));

		// advanced parameters
		checkBoxAdv.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (textAdv.getEnabled() == true) {
					textAdv.setEnabled(false);
					checkedAdv = false;
				} else {
					textAdv.setEnabled(true);
					checkedAdv = true;
				}
			}
		});

		// comments
		checkBoxComment.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (textComment.getEnabled() == true) {
					textComment.setEnabled(false);
					// checkedComment = false;
				} else {
					textComment.setEnabled(true);
					// checkedComment = true;
				}
			}
		});
	}

	private void setInitialControlsState() {
		// .ma warning
		this.maFieldWarning.setVisible(false);

		// .ma field
		if (this.defaultMaFile != null)
			textHeader.setText(this.defaultMaFile.getLocation().lastSegment());

		// Checkbox for .ev
		checkBoxEv.setSelection(DEFAULT_EVENT_FILE_ENABLED); // sets the initial
																// selection of
																// the
																// check-button
		if (this.getFileNames("ma")[0] != null && this.eventFileExists()) {// Chiril
																			// Chidisiuc:
																			// if
																			// MA
																			// &&
																			// ev
																			// file
																			// exists,
			this.checkBoxEv.setSelection(true); // if event file exists, set
												// checkedEv=true
		}

		// .ev field
		textEv.setEnabled(checkBoxEv.getSelection());
		if (checkBoxEv.getSelection()) {
			setInitialEventFileName(); // Chiril Chidisiuc: set first found
										// event file as default
		}

		// browse .ev
		fileButtonEv.setEnabled(checkBoxEv.getSelection());

		// Auto name for out
		outFieldAutoGenerationCheckbox.setSelection(checkedAutoOut); // Initially
																		// it
																		// should
																		// be
																		// "false"
																		// to
																		// let
																		// user
																		// specify
																		// own
																		// name
		outFieldAutoGenerationCheckbox.setEnabled(checkedOut); // Depends on
																// OUT, i.e.
																// disabled if
																// OUT name is
																// desabled
		stdOutText = ""; // Chiril Chidisiuc: MA file is not yet selected.

		// Auto name for .log
		logFieldAutoGenerationCheckbox.setSelection(checkedAutoLog); // Initially
																		// it
																		// should
																		// be
																		// "false"
																		// to
																		// let
																		// user
																		// specify
																		// own
																		// name
		logFieldAutoGenerationCheckbox.setEnabled(checkedLog); // Depends on
																// LOG, i.e.
																// disabled if
																// LOG name is
																// desabled
		stdLogText = ""; // Chiril Chidisiuc: MA file is not yet selected

		// .out field
		checkBoxOut.setSelection(checkedOut);
		textOut.setEnabled(checkedOut);

		// .out browse
		fileButtonOut.setEnabled(checkedOut);

		// .log checkbox
		checkBoxLog.setSelection(checkedLog);

		// .log field
		textLog.setEnabled(checkedLog);

		// .log browse
		fileButtonLog.setEnabled(checkedLog);

		// checkbox for time
		checkBoxTime.setSelection(DEFAULT_TIME_FIELD_ENABLED);

		// time fields
		textTime1.setEnabled(checkBoxTime.getSelection());
		textTime1.setText("00");
		textTime1.setTextLimit(2);
		textTime2.setEnabled(checkBoxTime.getSelection());
		textTime2.setText("00");
		textTime2.setTextLimit(2);
		textTime3.setEnabled(checkBoxTime.getSelection());
		textTime3.setText("00");
		textTime3.setTextLimit(2);
		textTime4.setEnabled(checkBoxTime.getSelection());
		textTime4.setText("000");
		textTime4.setTextLimit(3);

		// advanced parameters
		checkBoxAdv.setSelection(checkedAdv); // Chiril Chidisiuc: this is more
												// readable.
		// Can be changed once in declaration... (originally set to "false" by
		// default)
		textAdv.setEnabled(checkedAdv);

		// comments
		checkBoxComment.setSelection(false);
		textComment.setEnabled(false);

		// kill button
		killButton.setEnabled(false);
	}

	private void createControls(Composite parent) {
		{ // Row 1 - Should be text description of header file(.h) text box (Nan
			// renaming maybe done later TBD
			Label label1 = new Label(composite, SWT.NONE);
			label1.setText(Header_FIELD_LABEL);
			GridData gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.horizontalSpan = 10;
			label1.setLayoutData(gridData);
			label1.setFont(parent.getFont());
		}

		{ // Row 2 - Text Box for the ma file and file button
			textHeader = new Text(composite, SWT.SINGLE | SWT.BORDER);
			GridData gridData3 = new GridData();
			gridData3.horizontalSpan = 9;
			gridData3.horizontalAlignment = GridData.FILL;
			gridData3.grabExcessHorizontalSpace = true;
			gridData3.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			textHeader.setLayoutData(gridData3);

			this.fileButtonMa = new Button(composite, SWT.PUSH);
			fileButtonMa.setText("Browse");
			fileButtonMa.setToolTipText("Select CPP File");
			GridData gridDataFBMa = new GridData();
			gridDataFBMa.horizontalAlignment = GridData.BEGINNING;
			fileButtonMa.setLayoutData(gridDataFBMa);

			// warning message for .ma (used to alert if .ma is not un sync with
			// designer)
			final Image fieldDecorationWarningImage = PlatformUI.getWorkbench()
					.getSharedImages()
					.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
			this.maFieldWarning = new DecoratedLabel(composite,
					fieldDecorationWarningImage);
			this.maFieldWarning.setText("This will display the warning");
			this.maFieldWarning.setFont(new Font(Display.getDefault(),
					"Segoe UI", 8, SWT.BOLD | SWT.ITALIC));
			GridData gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.horizontalSpan = 9;
			this.maFieldWarning.setLayoutData(gridData);
		}

		{// Row 4.1- Text Description of expected output file text box
			Label label1 = new Label(composite, SWT.NONE);
			label1.setText(EXPECT_OUTPUT_FIELD_LABEL);
			GridData gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.horizontalSpan = 10;
			label1.setLayoutData(gridData);
			label1.setFont(parent.getFont());

		}

		{ // Row 4.2 - Text Box for the output file and file button
			unitTestDialog.textExpectedOutput = new Text(composite, SWT.SINGLE
					| SWT.BORDER);
			GridData gridData3 = new GridData();
			gridData3.horizontalSpan = 9;
			gridData3.horizontalAlignment = GridData.FILL;
			gridData3.grabExcessHorizontalSpace = true;
			gridData3.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			textExpectedOutput.setLayoutData(gridData3);

			this.fileButtonExpectrfOut = new Button(composite, SWT.PUSH);
			fileButtonExpectrfOut.setText("Browse");
			fileButtonExpectrfOut.setToolTipText("Select Expected output File");
			GridData gridDataFBMa = new GridData();
			gridDataFBMa.horizontalAlignment = GridData.BEGINNING;
			fileButtonExpectrfOut.setLayoutData(gridDataFBMa);

			/*
			 * // warning message for .ma (used to alert if .ma is not un sync
			 * with designer) final Image fieldDecorationWarningImage =
			 * PlatformUI
			 * .getWorkbench().getSharedImages().getImage(ISharedImages
			 * .IMG_OBJS_WARN_TSK); this.maFieldWarning = new
			 * DecoratedLabel(composite, fieldDecorationWarningImage);
			 * this.maFieldWarning.setText("This will display the warning");
			 * this.maFieldWarning.setFont(new Font(Display.getDefault(),
			 * "Segoe UI", 8, SWT.BOLD | SWT.ITALIC)); GridData gridData = new
			 * GridData(); gridData.horizontalAlignment = GridData.FILL;
			 * gridData.horizontalSpan = 9;
			 * this.maFieldWarning.setLayoutData(gridData);
			 */
		}

		{ // Row Three - Text Description of events text box
			Label label2 = new Label(composite, SWT.WRAP);

			label2.setText(EVENT_FIELD_LABEL);
			GridData gridData4 = new GridData();
			gridData4.horizontalAlignment = GridData.BEGINNING;
			gridData4.horizontalSpan = 10;
			label2.setLayoutData(gridData4);
			label2.setFont(parent.getFont());
		}

		{// Row Four - The Check Box then the Text Box for events
			checkBoxEv = new Button(composite, SWT.CHECK);
			GridData gridData5 = new GridData();
			gridData5.horizontalAlignment = GridData.BEGINNING;
			checkBoxEv.setLayoutData(gridData5);

			textEv = new Text(composite, SWT.SINGLE | SWT.BORDER);
			GridData gridData6 = new GridData();
			gridData6.horizontalAlignment = GridData.FILL;
			gridData6.horizontalSpan = 8;
			textEv.setLayoutData(gridData6);

			fileButtonEv = new Button(composite, SWT.PUSH);
			fileButtonEv.setText("Browse");
			fileButtonEv.setToolTipText("Select *.ev File");
			GridData gridDataFBEv = new GridData(SWT.WRAP);
			gridDataFBEv.horizontalAlignment = GridData.BEGINNING;
			fileButtonEv.setLayoutData(gridDataFBEv);
		}

		// ##################################### Kiril 06-07-11
		// ##############################################################
		// !!!!!!!!!!!!!!!!!!!!!!!!!11111!!!!!!!!!!!!!!!!!!!!!!!!!11111!!!!!!!!!!!!!!!!!!!!!!!!!11111!!!!!!!!!!!!!!!!!!!!!!!!!

		Label labelAuto = new Label(composite, SWT.WRAP);
		labelAuto.setText(AUTOGENERATE_NAMES_LABEL);
		GridData gridDataAuto = new GridData();
		gridDataAuto.horizontalAlignment = GridData.BEGINNING;
		gridDataAuto.horizontalSpan = 10;
		labelAuto.setLayoutData(gridDataAuto);
		labelAuto.setFont(parent.getFont());

		outFieldAutoGenerationCheckbox = new Button(composite, SWT.CHECK);
		outFieldAutoGenerationCheckbox.setText("Out");
		GridData gridDataAutoNameOut = new GridData();
		gridDataAutoNameOut.horizontalAlignment = GridData.BEGINNING;
		gridDataAutoNameOut.horizontalSpan = 1;
		outFieldAutoGenerationCheckbox.setLayoutData(gridDataAutoNameOut);

		logFieldAutoGenerationCheckbox = new Button(composite, SWT.CHECK);
		logFieldAutoGenerationCheckbox.setText("Log");
		GridData gridDataAutoNameLog = new GridData();
		gridDataAutoNameLog.horizontalAlignment = GridData.FILL;
		gridDataAutoNameLog.horizontalSpan = 1;
		logFieldAutoGenerationCheckbox.setLayoutData(gridDataAutoNameLog);

		// Row Five - Text Description of the Out text box
		Label label3 = new Label(composite, SWT.WRAP);
		label3.setText(OUTPUT_FIELD_LABEL);
		GridData gridData7 = new GridData();
		gridData7.horizontalAlignment = GridData.BEGINNING;
		gridData7.horizontalSpan = 10;
		label3.setLayoutData(gridData7);
		label3.setFont(parent.getFont());

		// Row Six - The check box and text box for out
		checkBoxOut = new Button(composite, SWT.CHECK);
		GridData gridData8 = new GridData();
		gridData8.horizontalAlignment = GridData.BEGINNING;
		checkBoxOut.setLayoutData(gridData8);

		textOut = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gridData9 = new GridData();
		gridData9.horizontalAlignment = GridData.FILL;
		gridData9.horizontalSpan = 8;
		textOut.setLayoutData(gridData9);

		fileButtonOut = new Button(composite, SWT.PUSH);
		fileButtonOut.setText("Browse");
		fileButtonOut.setToolTipText("Select *.out File");
		GridData gridDataFBOut = new GridData(SWT.WRAP);
		gridDataFBOut.horizontalAlignment = GridData.BEGINNING;
		fileButtonOut.setLayoutData(gridDataFBOut);

		// Row Seven - Text Description of the Log text box
		Label label4 = new Label(composite, SWT.WRAP);
		label4.setText(LOG_FIELD_LABEL);
		GridData gridData10 = new GridData();
		gridData10.horizontalAlignment = GridData.BEGINNING;
		gridData10.horizontalSpan = 10;
		label4.setLayoutData(gridData10);
		label4.setFont(parent.getFont());

		// Row Eight - The check box and text box for log
		checkBoxLog = new Button(composite, SWT.CHECK);
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.BEGINNING;
		checkBoxLog.setLayoutData(gridData11);

		textLog = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gridData12 = new GridData();
		gridData12.horizontalAlignment = GridData.FILL;
		gridData12.horizontalSpan = 8;
		textLog.setLayoutData(gridData12);

		fileButtonLog = new Button(composite, SWT.PUSH);
		fileButtonLog.setText("Browse");
		fileButtonLog.setToolTipText("Select Log File");
		GridData gridDataFBLog = new GridData(SWT.WRAP);
		gridDataFBLog.horizontalAlignment = GridData.BEGINNING;
		fileButtonLog.setLayoutData(gridDataFBLog);

		{// Row Nine - Text description of the Time text box
			Label label5 = new Label(composite, SWT.WRAP);
			label5.setText(STOP_TIME_FIELD_LABEL);
			GridData gridData13 = new GridData();
			gridData13.horizontalAlignment = GridData.BEGINNING;
			gridData13.horizontalSpan = 10;
			label5.setLayoutData(gridData13);
			label5.setFont(parent.getFont());
		}

		{// Row ten - The check box and text box for time
			checkBoxTime = new Button(composite, SWT.CHECK);
			GridData gridData14 = new GridData();
			gridData14.horizontalAlignment = GridData.BEGINNING;
			checkBoxTime.setLayoutData(gridData14);

			textTime1 = new Text(composite, SWT.SINGLE | SWT.BORDER);
			GridData gridData15 = new GridData();
			gridData15.widthHint = 15;
			gridData15.horizontalAlignment = GridData.END;
			textTime1.setLayoutData(gridData15);
			textTime1.setToolTipText("Hours");

			Label colon1 = new Label(composite, SWT.NONE);
			colon1.setText(":");

			textTime2 = new Text(composite, SWT.SINGLE | SWT.BORDER);
			gridData15 = new GridData();
			gridData15.widthHint = 15;
			textTime2.setLayoutData(gridData15);
			textTime2.setToolTipText("Minutes");

			Label colon2 = new Label(composite, SWT.NONE);
			colon2.setText(":");

			textTime3 = new Text(composite, SWT.SINGLE | SWT.BORDER);
			gridData15 = new GridData();
			gridData15.widthHint = 15;
			textTime3.setLayoutData(gridData15);
			textTime3.setToolTipText("Seconds");

			colon2 = new Label(composite, SWT.NONE);
			colon2.setText(":");

			textTime4 = new Text(composite, SWT.SINGLE | SWT.BORDER);
			gridData15 = new GridData();
			gridData15.widthHint = 20;
			textTime4.setLayoutData(gridData15);
			textTime4.setToolTipText("Milliseconds");
		}

		// Row Eleven - Text Discription of Advanced
		Label label6 = new Label(composite, SWT.WRAP);
		label6.setText(PARAMERTERS_FIELD_LABEL);
		GridData gridData16 = new GridData();
		gridData16.horizontalAlignment = GridData.BEGINNING;
		gridData16.horizontalSpan = 10;
		label6.setLayoutData(gridData16);
		label6.setFont(parent.getFont());

		// Row Tweleve - The check box and text box for advanced
		checkBoxAdv = new Button(composite, SWT.CHECK);
		GridData gridData17 = new GridData();
		gridData17.horizontalAlignment = GridData.BEGINNING;
		checkBoxAdv.setLayoutData(gridData17);
		checkBoxAdv
				.setToolTipText("This field will allow you to enter parameters in addition to command line");

		textAdv = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gridData18 = new GridData();
		gridData18.horizontalAlignment = GridData.FILL;
		gridData18.grabExcessHorizontalSpace = true;
		gridData18.horizontalSpan = 9;
		textAdv.setLayoutData(gridData18);
		String toolTipMsg = "Enter desired parameters manually. " + "\n"
				+ "Example: to add event file, type" + "\n"
				+ "     -e\"[your_file_name].ev\"" + "\n"
				+ "NOTE: Separate the entries by putting a space between them";
		textAdv.setToolTipText(toolTipMsg); // Chiril Chidisiuc: to help use the
											// field

		// Row Ten - Text description of the Comment text box
		Label label8 = new Label(composite, SWT.WRAP);
		label8.setText(COMMENTS_FIELD_LABEL);
		GridData gridData19 = new GridData();
		gridData19.horizontalAlignment = GridData.BEGINNING;
		gridData19.horizontalSpan = 10;
		label8.setLayoutData(gridData19);
		label8.setFont(parent.getFont());

		// Row ten - The check box and text box for comment
		checkBoxComment = new Button(composite, SWT.CHECK);
		GridData gridData20 = new GridData();
		gridData20.horizontalAlignment = GridData.BEGINNING;
		checkBoxComment.setLayoutData(gridData20);

		textComment = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gridData21 = new GridData();
		gridData21.horizontalSpan = 9;
		gridData21.horizontalAlignment = GridData.FILL;
		gridData21.grabExcessHorizontalSpace = true;
		textComment.setLayoutData(gridData21);
		textComment.setEnabled(false);

		// Row Sixteen - Save as Batch button
		saveButton = new Button(composite, SWT.PUSH);
		saveButton.setText("Save as .bat");
		saveButton.setToolTipText("Save Settings to batch file");
		GridData gridDataSBB = new GridData();
		gridDataSBB.horizontalAlignment = GridData.END;
		gridDataSBB.horizontalSpan = 9;
		saveButton.setLayoutData(gridDataSBB);
		saveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				composite.setEnabled(false);
				doneButton.setEnabled(false);
				killButton.setEnabled(false);
				simuButton.setEnabled(false);
				saveButton.setEnabled(false);
				loadButton.setEnabled(false);
				OK2Close = false;
				SaveBatchFileGui("*.bat");
			}
		});

		loadButton = new Button(composite, SWT.PUSH);
		loadButton.setText("Load .bat");
		loadButton.setToolTipText("Load Batch from file");
		GridData gridDataLBB = new GridData();
		gridDataLBB.horizontalAlignment = GridData.BEGINNING;
		loadButton.setLayoutData(gridDataLBB);
		loadButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					loadButton.setEnabled(false);
					OK2Close = false;

					BufferedReader batchReader = new BufferedReader(
							new FileReader(path.toString() + File.separatorChar
									+ NewFileGui("*.bat")));

					String command = batchReader.readLine();// Chiril Chidisiuc:
															// read first line
															// of BAT file
					System.out.println("command : " + command);
					while (!command.trim().contains("simu")) {// Chiril
																// Chidisiuc:
																// works for
																// DEVs/Cell-DEVs
						command = batchReader.readLine();
					}

					try {

						String comments = batchReader.readLine();
						for (int n = 1; !comments.trim().startsWith("rem"); n++) {// Chiril
																					// Chidisiuc:
																					// find
																					// comments
							n++;
							comments = batchReader.readLine();
						}

						if (comments.substring(4).length() != 0) { // Chiril
																	// Chidisiuc:
																	// comments
																	// exist...
							textComment.setText(comments.substring(4));
							checkBoxComment.setSelection(true);
							textComment.setEnabled(true);
						} else {// Chiril Chidisiuc: comments do not exist
							checkBoxComment.setSelection(false);
							textComment.setEnabled(false);
						}

						System.out.println("\n\n command : " + command);
						System.out.println("comments : " + comments);

					} catch (NullPointerException e1) { // some files may not
														// contain comments
						e1.printStackTrace(); // and null may be returned
					}
					String tempCommand = command.toLowerCase(); // Chiril
																// Chidisiuc:
																// avoid
																// case-sensetivity
					int indexStartMa = tempCommand.indexOf(" -m\"");// Chiril
																	// Chidisiuc:
																	// to avoid
																	// erroneous
																	// input
					int indexEndMa = tempCommand.indexOf(".ma\"");// Chiril
																	// Chidisiuc:
																	// to avoid
																	// erroneous
																	// input
					/*
					 * if (indexEndMa == -1) indexEndMa =
					 * command.indexOf(".MA"); //Chiril Chidisiuc: this can now
					 * be avoided...
					 */
					int indexStartEv = tempCommand.indexOf(" -e\""); // Chiril
																		// Chidisiuc:
																		// to
																		// avoid
																		// erroneous
																		// input
					int indexEndEv = tempCommand.indexOf(".ev\""); // Chiril
																	// Chidisiuc:
																	// to avoid
																	// erroneous
																	// input
					/*
					 * if (indexEndEv == -1) indexStartEv =
					 * command.indexOf(".EV"); //Chiril Chidisiuc: this can now
					 * be avoided...
					 */
					int indexStartOut = tempCommand.indexOf(" -o\"");// Chiril
																		// Chidisiuc:
																		// to
																		// avoid
																		// erroneous
																		// input
					int indexEndOut = tempCommand.indexOf(".out\"");// Chiril
																	// Chidisiuc:
																	// to avoid
																	// erroneous
																	// input

					int indexStartLog = tempCommand.indexOf(" -l\"");// Chiril
																		// Chidisiuc:
																		// to
																		// avoid
																		// erroneous
																		// input
					int indexEndLog = tempCommand.indexOf(".log\"");// Chiril
																	// Chidisiuc:
																	// to avoid
																	// erroneous
																	// input
					int hourIndexStart = tempCommand.indexOf(" -t\"");// Chiril
																		// Chidisiuc:
																		// to
																		// avoid
																		// erroneous
																		// input
					int hourIndexEnd = tempCommand.indexOf(":", hourIndexStart);
					int minuteIndexStart = tempCommand.indexOf(":",
							hourIndexStart);
					System.out.println("minute " + minuteIndexStart);
					int minuteIndexEnd = tempCommand.indexOf(":",
							minuteIndexStart + 1);

					int secondIndexStart = tempCommand.indexOf(":",
							minuteIndexEnd);
					int secondIndexEnd = tempCommand.indexOf(":",
							secondIndexStart + 1);
					int mSecondIndexStart = tempCommand.indexOf(":",
							secondIndexEnd);
					int mSecondIndexEnd = tempCommand.indexOf("\"",
							mSecondIndexStart);// Chiril Chidisiuc: originally
												// it was " "

					// index for Advanced field
					// int indexStartAdv = -1;
					if ((hourIndexStart != -1) && (mSecondIndexEnd != -1)) {
						// indexStartAdv =
						command.indexOf(" ", mSecondIndexStart);
					} else if (hourIndexStart == -1) {
						// indexStartAdv =
						command.indexOf(" ", indexEndLog);
					}

					String[] excludeFromAdvancedFieldText = new String[6];
					int excludeIndex = 0;

					if (indexStartMa != -1) {
						String maFieldText = command.substring(
								indexStartMa + 4, // Chiril Chidisiuc:
													// originally '+2', now 4 to
													// avoid '"' character
								indexEndMa + 3);
						textHeader.setText(maFieldText);
						excludeFromAdvancedFieldText[excludeIndex] = command
								.substring(indexStartMa, indexEndMa + 4);
						excludeIndex++;
					} else {
						textHeader.setText("");
					}

					if (indexStartEv != -1) {
						checkBoxEv.setSelection(true);
						textEv.setEnabled(true);
						fileButtonEv.setEnabled(true);
						textEv.setText(command.substring(indexStartEv + 4, // Chiril
																			// Chidisiuc:
																			// originally
																			// '+2',
																			// now
																			// 4
																			// to
																			// avoid
																			// '"'
																			// character
								indexEndEv + 3));
						excludeFromAdvancedFieldText[excludeIndex] = command
								.substring(indexStartEv, indexEndEv + 4);
						excludeIndex++;
						// 1 extra for the space at the end
					} else {
						checkBoxEv.setSelection(false);
						textEv.setEnabled(false);
						fileButtonEv.setEnabled(false);
						textEv.setText("");
					}

					if (indexStartOut != -1) {
						textOut.setText(command.substring(indexStartOut + 4, // Chiril
																				// Chidisiuc:
																				// originally
																				// '+2',
																				// now
																				// 4
																				// to
																				// avoid
																				// '"'
																				// character
								indexEndOut + 4));
						checkedOut = true;
						checkBoxOut.setSelection(checkedOut);
						textOut.setEnabled(checkedOut);
						fileButtonOut.setEnabled(checkedOut);
						excludeFromAdvancedFieldText[excludeIndex] = command
								.substring(indexStartOut, indexEndOut + 5);
						excludeIndex++;
						// 1 extra for the space at the end
					} else {
						textOut.setText("");
						checkedOut = false;
						checkBoxOut.setSelection(checkedOut);
						textOut.setEnabled(checkedOut);
						fileButtonOut.setEnabled(checkedOut);
					}

					if (indexStartLog != -1) {
						textLog.setText(command.substring(indexStartLog + 4, // Chiril
																				// Chidisiuc:
																				// originally
																				// '+2',
																				// now
																				// 4
																				// to
																				// avoid
																				// '"'
																				// character
								indexEndLog + 4));
						checkedLog = true;
						checkBoxLog.setSelection(checkedLog);
						textLog.setEnabled(checkedLog);
						fileButtonLog.setEnabled(checkedLog);
						excludeFromAdvancedFieldText[excludeIndex] = command
								.substring(indexStartLog, indexEndLog + 5);
						excludeIndex++;

					} else {
						textLog.setText("");
						checkedLog = false;
						checkBoxLog.setSelection(checkedLog);
						textLog.setEnabled(checkedLog);
						fileButtonLog.setEnabled(checkedLog);
					}

					if (hourIndexStart != -1) {
						textTime1.setText(command.substring(hourIndexStart + 4, // Chiril
																				// Chidisiuc:
																				// originally
																				// '+2',
																				// now
																				// 4
																				// to
																				// avoid
																				// '"'
																				// character
								hourIndexEnd));
						textTime2.setText(command.substring(
								minuteIndexStart + 1, minuteIndexEnd));
						textTime3.setText(command.substring(
								secondIndexStart + 1, secondIndexEnd));
						textTime4.setText(command.substring(
								mSecondIndexStart + 1, mSecondIndexEnd));
						checkBoxTime.setSelection(true);
						textTime1.setEnabled(true);
						textTime2.setEnabled(true);
						textTime3.setEnabled(true);
						textTime4.setEnabled(true);
						excludeFromAdvancedFieldText[excludeIndex] = command
								.substring(hourIndexStart, mSecondIndexEnd + 1);
						excludeIndex++;

					} else {

						textTime1.setText("00");
						textTime2.setText("00");
						textTime3.setText("00");
						textTime4.setText("000");
						checkBoxTime.setSelection(false);
						textTime1.setEnabled(false);
						textTime2.setEnabled(false);
						textTime3.setEnabled(false);
						textTime4.setEnabled(false);

					}

					// check if Advanced field is present
					String advancedFieldText = getAdvancedFieldText(command,
							excludeFromAdvancedFieldText);
					if (advancedFieldText.length() != 0) {
						// advanced present
						checkedAdv = true;
						checkBoxAdv.setSelection(checkedAdv);
						textAdv.setEnabled(checkedAdv);
						textAdv.setText(advancedFieldText.trim());
					} else {
						// advanced not present
						textAdv.setText("");
						checkedAdv = false;
						checkBoxAdv.setSelection(checkedAdv);
						textAdv.setEnabled(checkedAdv);
					}
				} catch (Exception error) {
					error.printStackTrace();
				}

				loadButton.setEnabled(true);
				OK2Close = true;

			}
		});

		// last row - show simulation flow check box
		// this.showSimulationFlowCheckBox = new Button(composite, SWT.CHECK);
		//
		// Label showSimulationLabel = new Label(composite, SWT.NONE);
		// showSimulationLabel.setText("Show Simulation Flow");
		// GridData gridDataSimulationFlowLabel = new GridData();
		// gridDataSimulationFlowLabel.horizontalAlignment = GridData.BEGINNING;
		// gridDataSimulationFlowLabel.horizontalSpan = 9;
		// showSimulationLabel.setLayoutData(gridDataSimulationFlowLabel);

		errorMessageLabel = new Label(composite, SWT.NONE);
		errorMessageLabel.setFont(parent.getFont());

	}

	private void addTextMaChangedListener() {
		textHeader.addModifyListener(new ModifyListener() {
			private IFile selectedFile;

			public void modifyText(ModifyEvent e) {
				if (!textHeader.getText().isEmpty()) {
					int offset = getMaOffset(textHeader);
					this.selectedFile = executionFolder.getFile(new Path(
							textHeader.getText()));

					this.setWarningField();

					// set Log file name
					if (textOut.getEnabled() == true) {
						setSimuFileNames(true, false);
					}

					// set Output file name
					if (textLog.getEnabled() == true) {
						setSimuFileNames(false, true);
					}

					// Chiril Chidisiuc: next "if" statement is needed to make
					// changes to textEv text field if
					// another default name must be set
					if (textEv.getEnabled() == true) { // find appropriate
														// default event file
														// name
						String name = textHeader.getText().substring(0, offset); // Chiril
																					// Chidisiuc:
																					// name
																					// is
																					// the
																					// same
						// as makefile without the ".MA" ending
						String[] eventFileNames = getFileNames("ev");
						// String [] maFileNames = getFileNames("ma");
						if (eventFileNames[0] != null) {
							for (int i = 0; i < eventFileNames.length; i++) {
								if (eventFileNames[i] != null) {// check the
																// event file
																// name
									if (eventFileNames[i].equals(name)) {// set
																			// default
																			// name
																			// if
																			// such
																			// was
																			// found
										textEv.setText(name + ".EV");
										break;
									}// end if
								}// end if "eventFileNames[i]"
							}// end for
						}// end if "eventFileNames[0]"
					}// end if "textEv.getEnabled()"

					// Chiril Chidisiuc:
					// If name is modified, then let all buttons become active.
					// It is to supress the deactivation of the "Proceed" and
					// "Cancel" buttons,
					// which happens if the specified MA file does not exist.
					simuButton.setEnabled(true);
					doneButton.setEnabled(true);
				}
			}

			private void setWarningField() {
				if (!this.selectedFile.exists()) {
					maFieldWarning
							.setText("The .h file is not in the selected execution folder: "
									+ this.selectedFile.getLocation());
					maFieldWarning.setVisible(true);
				} else if (this.isMaOutOfSync()) {
					maFieldWarning
							.setText("Diagram and Source files are not synchronized. The Source file will be used for simulations.");
					maFieldWarning.setVisible(true);
				} else {
					maFieldWarning.setVisible(false);
				}
			}

			private boolean isMaOutOfSync() {

				// modify required Nan TBD
				/*
				 * try { CDBuilderCoupledModelResource resource = new
				 * CDBuilderCoupledModelResource(this.selectedFile);
				 * if(resource.diagramFileExists() &&
				 * (resource.isDiagramFileNewer() ||
				 * resource.isSourceFileNewer())) return true;
				 * 
				 * } catch (InvalidParameterException e) {
				 * DEVSDiagramEditorPlugin.getInstance().logError(
				 * "Problems creating CDBuilder resource to check if source and diagram are in sync"
				 * , e); return true; } catch
				 * (DEVSModeler.util.InvalidParameterException e) { // TODO
				 * Auto-generated catch block e.printStackTrace(); }
				 */

				return false;
			}
		});
	}

	/**
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {

		if (OK2Close) {

			boolean returnValue = super.close();
			return returnValue;
		}

		return true;

	}

	/**
	 * This method takes the fileName and concatenates the digits of n [found
	 * through testString] to the end of the string [if required]. Edited: by
	 * Jing Cao
	 * 
	 * @param fileName
	 * @param integer
	 *            to be appended
	 * @return String tempFileName [new file name with the number at end]
	 */
	/*
	 * private String setDigit(String fileName, int n) { //beg setDigit String
	 * tempFileName = new String(fileName); String tempString; char c; int i;
	 * 
	 * //Note: the string array only extends from 0 -> (length of string-1) for
	 * (i = (fileName.length() - 1); i >= 0; i--) { //This loop will find the
	 * location where the number starts. c = tempFileName.charAt(i); if ((c <
	 * '0') || (c > '9')) //if the chracter is not a number then break out of
	 * the for loop break; // the value of 'i' will be the index where there is
	 * a character [not a number] }
	 * 
	 * tempFileName = tempFileName.trim(); //redundant, it gets rid of spaces
	 * infront or after the fileName tempString = new
	 * String(Integer.toString(n)); if (i < 0) //the fileName consists of all
	 * numbers return tempString; // then return the updated number as the file
	 * name! //else tempFileName = tempFileName.substring(0, i + 1); //get the
	 * character part of the string //note: the +1 is required because
	 * substring(beg,end) is "exclusive" of the end.
	 * 
	 * return tempFileName.concat(tempString); //adds the updated number to the
	 * end of the characters and returns the string } //end setDigit
	 */

	/**
	 * This method takes a file name (stripped off ".bat") and returns the
	 * digits (numbers) at the end as a string. Returns '0' if there's no number
	 * at the end of the file name. Will return the file name if the string is
	 * composed of only numbers.
	 * 
	 * @author Edited by Jing Cao
	 * @param fileName
	 * @return number [of the type string]
	 */
	/*
	 * private String testString(String fileName) { //beg testString()
	 * 
	 * //Variable Declarations String tempString = new String(fileName);
	 * //redunant but just to make shure that we don't make changes to
	 * testString() String number = new String(""); //this string stores the
	 * number int i; char c;
	 * 
	 * //Note: the string array only extends from 0 -> (length of string-1) for
	 * (i = (fileName.length() - 1); i >= 0; i--) { //This loop will find the
	 * location where the number starts. c = tempString.charAt(i); if ((c < '0')
	 * || (c > '9')) //if the chracter is not a number then break out of the for
	 * loop break; // the value of 'i' will be the index where there is a
	 * character [not a number] }
	 * 
	 * //if statements to set the number string properly if (i ==
	 * (fileName.length() - 1)) //sets number to 0 if no number found at end of
	 * the string. number = "0"; else if (i == -1) //means that the file name is
	 * a number! number = new String(fileName); //then set number to be the file
	 * name else number = tempString.substring(i + 1); //creates the number
	 * string by chopping off the letters
	 * 
	 * number = number.trim(); //removes any spaces at the beginning or the end
	 * of the string
	 * 
	 * return number; } //end testString()
	 */

	/**
	 * This method checks for latest BAT file and returns number of next version
	 * of the BAT file.
	 * 
	 * @param batName
	 *            :String - specific batch file name to be checked (without
	 *            extension!).
	 * @return latestVersion:int - number to be used in name of the next BAT
	 *         file with the root name as batName.
	 * 
	 * @author Chiril Chidisiuc
	 * @version 2006-06-25
	 */

	/*
	 * private int getNextBatVersion(String batName){ String [] batList =
	 * getFileNames("bat"); int latestVersion = 0; for(int i=0;
	 * i<batList.length; i++){ if(batList[i]==null){ break; //no more entries }
	 * int startSequence = batList[i].lastIndexOf("_"); String sequence =
	 * batList[i].substring(startSequence+1);//ending of file name int number =
	 * this.getFileVersion(sequence);//numerical representation of file name
	 * ending if (startSequence!=-1){//ending exists if(number!=-1){//name is a
	 * number String baseName = batList[i].substring(0,startSequence); //root
	 * name of bat file name if(baseName.equals(batName) &&
	 * (number>latestVersion)){//check which version is latest latestVersion =
	 * number; } }else{ continue;//file names with different root name are
	 * irrelevent } } } latestVersion++; //next file name must be 1 higher than
	 * currently latest return latestVersion; }
	 */

	/**
	 * this function is used by load.bat to load a new file.
	 * 
	 * @param filter
	 *            :String
	 * @return name of file
	 */
	private String NewFileGui(String filter) {
		FileDialog fileDialog = new FileDialog(parent);

		String[] extention = new String[1];
		extention[0] = filter;
		fileDialog.setFilterPath(path.toString());
		fileDialog.setFilterExtensions(extention);
		fileDialog.open();

		return fileDialog.getFileName();
	}

	/**
	 * Listener used by time input field to support pressing up & down keys.
	 * 
	 * @author Matiasb
	 */
	private class NumberFieldKeyListener implements KeyListener {
		private Text field;
		private NumberFormat formater = NumberFormat.getNumberInstance();

		public NumberFieldKeyListener(Text field) {
			this.field = field;
			this.formater.setMinimumIntegerDigits(field.getTextLimit());
		}

		private int getMaxValueForField() {
			return (int) Math.pow(10, field.getTextLimit()) - 1; // if using 3
																	// digits,
																	// 999 =>
																	// [10^(3)]
																	// - 1 =
																	// [1000 -
																	// 1] = 999
		}

		private void setFieldText(int value) {
			this.field.setText(formater.format(value));
		}

		public void keyPressed(KeyEvent e) {
			try {
				Integer time = new Integer(this.field.getText());
				if (e.keyCode == SWT.ARROW_UP) {
					time = new Integer(time.intValue() + 1);
					if (time.intValue() > this.getMaxValueForField()) {
						this.setFieldText(0);
					} else {
						this.setFieldText(time);
					}
				}

				if (e.keyCode == SWT.ARROW_DOWN) {
					time = new Integer(time.intValue() - 1);
					if (time.intValue() < 0) {
						this.setFieldText(this.getMaxValueForField());
					} else {
						this.setFieldText(time);
					}
				}
			} catch (NumberFormatException e2) {
				this.field.setText("");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		public void keyReleased(KeyEvent e) {
		}
	}

	private class DecoratedLabel extends Composite {
		private Label image;
		private Label label;

		public void setText(String text) {
			this.label.setText(text);
		}

		public void setFont(Font font) {
			this.label.setFont(font);
		}

		public DecoratedLabel(Composite parent, Image decoration) {
			super(parent, SWT.NONE);

			// set layout
			GridLayout layout = new GridLayout(10, false);
			layout.marginBottom = 0;
			layout.marginHeight = 0;
			layout.marginLeft = 0;
			layout.marginRight = 0;
			layout.marginTop = 0;
			layout.marginWidth = 0;
			layout.horizontalSpacing = 0;
			this.setLayout(layout);

			// create image
			this.image = new Label(this, SWT.NONE);
			this.image.setImage(decoration);

			// create label
			this.label = new Label(this, SWT.NONE);
			this.label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					true));
		}
	}

	/**
	 * this function is used by save.bat to save a file
	 * 
	 * @param filter
	 *            :String
	 * @return none
	 */
	// code modified by Sameen Rehman
	private void SaveBatchFileGui(String filter) {
		String[] param = new String[10];
		String command = collectParameters();

		if (command == null) {
			composite.setEnabled(true);
			doneButton.setEnabled(true);
			simuButton.setEnabled(true);
			saveButton.setEnabled(true);
			loadButton.setEnabled(true);
			OK2Close = true;
			return;
		}
		command = "simu " + command;
		composite.setEnabled(false);
		simuButton.setEnabled(false);
		doneButton.setEnabled(false);

		FileDialog fileDialog = new FileDialog(parent, SWT.SAVE);
		String[] extention = new String[1];
		extention[0] = filter;
		fileDialog.setFilterPath(path.toString());
		fileDialog.setFilterExtensions(extention);
		fileDialog.open();

		String fileName = fileDialog.getFileName();

		try {

			if (fileName.toUpperCase().endsWith(".BAT")) {
				fileName = fileName.substring(0, fileName.length() - 4);
			}
			// this code segment makes sure nothing is saved when u cancel the
			// save dialog box
			if (fileName == "") {
				fileDialog.setFilterPath("");
				fileDialog.setFilterExtensions(null);
				composite.setEnabled(true);
				simuButton.setEnabled(true);
				doneButton.setEnabled(true);
				saveButton.setEnabled(true);
				loadButton.setEnabled(true);

				OK2Close = true;
				return;
			}

			File existFile = new File(path.toString() + File.separatorChar
					+ fileName + ".bat");
			boolean cancelled = false;

			if (existFile.exists()) { // Chiril Chidisiuc: file is already
										// present in the directory
				IPreferenceStore store = CDBuilderPlugin.getDefault()
						.getPreferenceStore();
				if (store.getInt("batchChoice") == 3) { // Chiril Chidisiuc: an
														// existing file is
														// selected
					OverwriteGui ovG = new OverwriteGui(this.parent);// Chiril
																		// Chidisiuc:
																		// origianlly
																		// 'new
																		// Shell()'
					ovG.open();
					if (ovG.buttonPressed() == 0) { // Chiril Chidisiuc: cancel
													// the promt
						System.out.print("canceled saving bat");
						cancelled = true;
						composite.setEnabled(true);
						simuButton.setEnabled(true);
						doneButton.setEnabled(true);
						saveButton.setEnabled(true);
						loadButton.setEnabled(true);
						OK2Close = true;
					}
					// Chiril Chidisiuc: automatically generate the version of
					// the selected file
					if (ovG.buttonPressed() == 2) {
						int newVersion = getNextBatVersion(fileName); // Chiril
																		// Chidisiuc
						fileName = fileName + "_" + newVersion;
						MessageBox mbox = new MessageBox(this.parent,
								SWT.ICON_INFORMATION | SWT.OK);
						mbox.setText("Saving File");
						mbox.setMessage("The file has been saved as "
								+ fileName + File.separatorChar + ".bat");
						mbox.open();

						composite.setEnabled(true);
						simuButton.setEnabled(true);
						doneButton.setEnabled(true);
						saveButton.setEnabled(true);
						loadButton.setEnabled(true);

						if (ovG.noAskAgain())
							store.setValue("batchChoice", 2); // Chiril
																// Chidisiuc:
																// "Don't ask again"
																// selected to
																// always
																// automate
																// naming
					}
					if (ovG.buttonPressed() == 1) {// Chiril Chidisiuc:
													// overwrite the selected
													// file
						if (ovG.noAskAgain())
							store.setValue("batchChoice", 1);
					}

				} else if (store.getInt("batchChoice") == 2) {
					// store.setValue("batchChoice", 3); //Chiril Chidisiuc
					// [debug purposes]: run this once, or modify the
					// CDPreferencePage.java to restore settings
					int newVersion = getNextBatVersion(fileName);
					fileName = fileName + "_" + newVersion;
				}
			}
			if (!cancelled) { // Chiril Chidisiuc: saving file
				composite.setEnabled(false);
				simuButton.setEnabled(false);
				doneButton.setEnabled(false);
				OK2Close = false;
				FileWriter batchFile = new FileWriter(path.toString()
						+ File.separatorChar + fileName + ".bat");
				System.out.println(path.toString() + File.separatorChar
						+ fileName + ".bat");
				param[0] = "-m" + MaFileName;

				System.out.println(command);
				batchFile.write(command + "\n");
				batchFile.write("rem " + textComment.getText() + "\n");
				batchFile.write("pause");
				batchFile.close();
				composite.setEnabled(true);
				simuButton.setEnabled(true);
				doneButton.setEnabled(true);
				saveButton.setEnabled(true);
				loadButton.setEnabled(true);
				OK2Close = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method checks for latest BAT file and returns number of next version
	 * of the BAT file.
	 * 
	 * @param batName
	 *            :String - specific batch file name to be checked (without
	 *            extension!).
	 * @return latestVersion:int - number to be used in name of the next BAT
	 *         file with the root name as batName.
	 * 
	 * @author Chiril Chidisiuc
	 * @version 2006-06-25
	 */
	private int getNextBatVersion(String batName) {
		String[] batList = getFileNames("bat");
		int latestVersion = 0;
		for (int i = 0; i < batList.length; i++) {
			if (batList[i] == null) {
				break; // no more entries
			}
			int startSequence = batList[i].lastIndexOf("_");
			String sequence = batList[i].substring(startSequence + 1);// ending
																		// of
																		// file
																		// name
			int number = this.getFileVersion(sequence);// numerical
														// representation of
														// file name ending
			if (startSequence != -1) {// ending exists
				if (number != -1) {// name is a number
					String baseName = batList[i].substring(0, startSequence); // root
																				// name
																				// of
																				// bat
																				// file
																				// name
					if (baseName.equals(batName) && (number > latestVersion)) {// check
																				// which
																				// version
																				// is
																				// latest
						latestVersion = number;
					}
				} else {
					continue;// file names with different root name are
								// irrelevent
				}
			}
		}
		latestVersion++; // next file name must be 1 higher than currently
							// latest
		return latestVersion;
	}

	/**
	 * This method defines the advanced field text when a BAT file is loaded by
	 * removing the unnecessary entries (ma, log, out, ev, time...)
	 * 
	 * @param command
	 *            :String - command line containing all parameters
	 * @param excludeFromAdvancedFieldText
	 *            :String[] - array of parameters that should not appear in
	 *            advanced field
	 * @return advField:String - string representation of text for advanced
	 *         field
	 * @author Chiril Chidisiuc
	 * @version 2006-07-11
	 */
	private String getAdvancedFieldText(String command,
			String[] excludeFromAdvancedFieldText) {
		String advField = "";
		int startParameter;
		int endParameter;
		int i = 0;
		while (i < excludeFromAdvancedFieldText.length) {
			if (excludeFromAdvancedFieldText[i] == null) {
				break; // no more entries
			}
			if (command.contains(excludeFromAdvancedFieldText[i])) {
				startParameter = command
						.indexOf(excludeFromAdvancedFieldText[i]);
				endParameter = startParameter
						+ excludeFromAdvancedFieldText[i].length();
				command = command.substring(0, startParameter)
						+ command.substring(endParameter); // Chiril Chidisiuc:
															// remove entry from
															// command line
			}
			i++;
		}
		if (command.contains("exe")) { // Chiril Chidisiuc: remove
										// C:/.../simu.exe (simuOrig.exe) from
										// command line
			advField = command.substring(command.indexOf("exe") + 4);
		}
		return advField.trim(); // remove all spaces remaining after removing
								// the entries
	}

	
	/**
	 * This method create Ma file based on existing cpp file,
	 * and fresh the project folder 
	 * @author Nan Yang
	 * @version 2014-02-27
	 */
	private void anlysisHeaderFile() throws IOException, CoreException {

		absoluteProjectPath = path.toString().replaceAll("/", "//") + "//";
		String absolutePathName = path.toString().replaceAll("/", "//") + "//"
				+ unitTestDialog.maName + ".cpp";

		File tragetFile = new File(absolutePathName);
		//System.out.println("absolutePath=" + absolutePathName);
		//System.out.println("maName=" + maName);
		fileExtracter fe = new fileExtracter();
		fe.extractCppFile(tragetFile);

		String fileRootName = absolutePathName.replace(".cpp", "");
		fileGenerater FG = new fileGenerater(fe.getData(), fileRootName + ".ma");
		FG.generateMaFile();
		executionFolder.refreshLocal(1, null);
		MaFileName = maName + ".ma";
		//System.out.println("execute anlysisHeaderFile fileRootName="+ fileRootName);
	}


	/**
	 * This method prepare the unitTestResultView
	 * @author Nan Yang
	 * @version 2014-02-27
	 */
	private void prepareUnitTestResultView() throws PartInitException{
		
		File expectedOutputFile = new File(absoluteProjectPath
				+ textExpectedOutput.getText());
		File realOutputFile = new File(absoluteProjectPath + textOut.getText());
		
		infoHolder.setExpectedOutputFile(expectedOutputFile);
		infoHolder.setRealOutputFile(realOutputFile);
		
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		.getActivePage()
		.showView("cdbuilder.unitTest.views.unitTestResultView");
	}
	
	/**
	 * This method output and dispaly test result in the unitTestResultView
	 * @author Nan Yang
	 * @version 2014-02-27
	 */
	public static void showUnitTestResultView() throws IOException, CoreException, InterruptedException {
		
		
		ArrayList<ArrayList<String>> expectedData = null;// = new
															// ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> RealData = null;// = new
														// ArrayList<ArrayList<String>>();


		File expectedOutputFile = new File(absoluteProjectPath
				+ textExpectedOutput.getText());
		File realOutputFile = new File(absoluteProjectPath + textOut.getText());
		//System.out.println("expected=" + absoluteProjectPath+ textExpectedOutput.getText());
		//System.out.println("Real==" + absoluteProjectPath + textOut.getText());
		executionFolder.refreshLocal(1, null);

		
		if (expectedOutputFile.exists() && realOutputFile.exists()) {
			//System.out.println("The two files exists");

			fileExtracter fe = new fileExtracter();
			expectedData = fe.extractOutputFile(expectedOutputFile);
			//System.out.println("expectedData=" + expectedData);

			fileExtracter fe2 = new fileExtracter();
			RealData = fe2.extractOutputFile(realOutputFile);
			//System.out.println("RealData=" + RealData);

			String output = "";

			int realSize = RealData.size();
			int expectedSize = expectedData.size();
			int lineNumber;
			if(realSize>expectedSize){
				lineNumber=realSize;
			}else{
				lineNumber=expectedSize;
			}

			for (int t = 0; t < lineNumber; t++) {
				if(expectedData.size()>t){
					output += "Line " + t + " Expected output: "
					+ expectedData.get(t).get(0) + " "
					+ expectedData.get(t).get(1) + " "
					+ expectedData.get(t).get(2);
				}else{
					output += "Line " + t + " Expected output: null";
				}
				
				if(RealData.size()>t){
					output += " Real output: "
						+ RealData.get(t).get(0) + " " + RealData.get(t).get(1)
						+ " " + RealData.get(t).get(2) ;
				}else{
					output += " Real output: null \n";		
				}

				if(expectedData.size()>t &&RealData.size()>t){
					if(expectedData.get(t).get(0).equals(RealData.get(t).get(0)) && 
							expectedData.get(t).get(1).equals(RealData.get(t).get(1)) &&
							expectedData.get(t).get(2).equals(RealData.get(t).get(2))){
						
						output += " Line Pass \n";
					}else{
						
						output += " Line Fail \n";
					}				
				}				
			}
			infoHolder.setText(output);
			
			if (expectedData.toString().replaceAll(" ", "")
					.equals(RealData.toString().replaceAll(" ", ""))) {
				infoHolder.setState("Unit Test Pass");
				// sendInfoPass("Unit text Pass",output);
			} else {
				// sendInfo("Unit text Fail",output);
				infoHolder.setState("Unit Test Fail");
			}

		} else {

			infoHolder
					.setState("UnitTest Fail, Expected and/or Real output File do not exist");
			infoHolder.setText("");
		}

		
		
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage()
				.showView("cdbuilder.unitTest.views.unitTestResultView");

		UnitTestView.label.setText(infoHolder.getState() + "\n"
				+ infoHolder.getText());

	}

	

	public void sendInfo(String result, String Info) {
		MessageBox mbox = new MessageBox(parent, SWT.ICON_ERROR | SWT.OK
				| SWT.APPLICATION_MODAL);
		mbox.setText(result);
		mbox.setMessage(Info);
		mbox.open();

	}

	public void sendInfoPass(String result, String Info) {
		MessageBox mbox = new MessageBox(parent, SWT.ICON_WORKING | SWT.OK
				| SWT.APPLICATION_MODAL);
		mbox.setText(result);
		mbox.setMessage(Info);
		mbox.open();

	}

}
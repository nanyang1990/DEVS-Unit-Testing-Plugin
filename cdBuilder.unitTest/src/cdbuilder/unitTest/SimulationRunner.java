/* This file is created by Nan Yang in 01/19/2014
 * 
 * This file is highly based on cdBuilder.simulator.SimulationRunner form 
 * the CDBuilderPluginDevelopmentProject_from_Matias project
 * 
 * Certain function have been added to accomplish unit test objective.
 */




package cdbuilder.unitTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.EventListener;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
//import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;

import cdBuilder.console.ConsoleDocument;
import cdBuilder.console.StreamHandler;
import cdBuilder.simulator.SimulationHelper;
import cdBuilder.simulator.SimulatorPlugin;
import cdbuilder.unitTest.fileDealer.fileExtracter;
import cdbuilder.unitTest.views.UnitTestView;
import cdbuilder.unitTest.views.infoHolder;

public class SimulationRunner {

	private IContainer executionFolder;
	private Process simuProcess;
	private Thread simuThread;
	private ArrayList<SimulationRunListener> listeners = new ArrayList<SimulationRunListener>();

	// simulator exe to run
	public static final String INTERNAL_SIMULATOR = "simuOrig"; // original
																// simulator,
																// used when no
																// custom
																// compiling is
																// needed.
	public static final String INTERNAL_SIMULATOR_EXE = INTERNAL_SIMULATOR
			+ ".exe";
	public static final String GENERATED_SIMULATOR = "simu"; // generated
																// simulator,
																// used when
																// compiling
																// custom atomic
																// models.
																// Should be the
																// same as in
																// RunMakeFile
																// action.
																// Ideally the
																// constant
																// should be
																// declared only
																// there
	public static final String GENERATED_SIMULATOR_EXE = GENERATED_SIMULATOR
			+ ".exe";

	public SimulationRunner(IContainer executionFolder) {
		this.executionFolder = executionFolder;
	}

	public void runSimulation(String maFileName, final String parameters)
			throws InterruptedException {
		// resolve ma file
		IResource maFileResource = this.executionFolder.findMember(maFileName);
		final IFile maFile;
		if (maFileResource instanceof IFile)
			maFile = (IFile) maFileResource;
		else
			throw new InvalidParameterException("Invalid .ma file."
					+ this.executionFolder.getLocation().append(maFileName)
					+ " does not exist.");

		final IContainer currentFolder = this.executionFolder;
		// Start new thread to run simulations
		this.simuThread = new Thread() {
			@Override
			public void run() {
				try {
					IPath executionPath = executionFolder.getLocation();

					// create tempSimu.bat
					IPath tempFile = executionPath.append("tempSimu.bat");
					FileWriter tempFileWriter = new FileWriter(
							tempFile.toString());

					// write command to tempSimu.bat
					tempFileWriter.write(getCommand(maFile, parameters,
							currentFolder));
					tempFileWriter.close();

					// execute the .bat to run the simulation in a new process
					simuProcess = Runtime.getRuntime().exec(
							"cmd /c \"" + tempFile.toString() + "\"", null,
							new File(executionPath.toString()));

					// process listeners
					StreamHandler outputHandler = new StreamHandler(
							simuProcess.getInputStream(), "OUTPUT", true);
					outputHandler.start();

					StreamHandler errorHandler = new StreamHandler(
							simuProcess.getErrorStream(), "ERROR", true);
					errorHandler.start();

					// Wait for simulation to complete running
					int exitStatus = simuProcess.waitFor();

					// refresh folder to reflect new files in navigation
					// explorer
					executionFolder.refreshLocal(1, null);

					// Delete tempSimu.bat
					executionFolder.getFile(new Path("tempSimu.bat")).delete(
							true, null);

					// tell everyone simulation has ended
					fireSimulationEndedEvent(exitStatus);
				} catch (Throwable e) {
					SimulatorPlugin.getDefault().logError(
							"Problems running simulation", e);
				} finally {
					try {
						executionFolder.refreshLocal(1, null);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					

					try {
						showUnitTestView();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};

		this.simuThread.start();

	}

	public void killSimulation() {
		if (this.simuProcess != null && this.simuThread != null
				&& this.simuThread.isAlive()
				&& !this.simuThread.isInterrupted()) {
			try {

				// kill the process that run the .bat file
				simuProcess.destroy();

				// "ps" allow you to view the process table in linux/cygwin
				// ('-W' parameter allows you to view window processes as well!)
				Process lookProcess = Runtime.getRuntime().exec("ps");
				BufferedReader bf = new BufferedReader(new InputStreamReader(
						lookProcess.getInputStream()));
				String msg = null;
				while ((msg = bf.readLine()) != null) {
					// look for simu/simuOrig at the end of path portion of line
					// to find process
					if (msg.endsWith(INTERNAL_SIMULATOR)
							|| msg.endsWith(GENERATED_SIMULATOR)) {
						msg = msg.substring(2).trim();
						Runtime.getRuntime()
								.exec("kill -9 "
										+ msg.substring(0, msg.indexOf(" ")));
					}

				}

				// kill the thread that run the simulation
				this.simuThread.interrupt();
			} catch (IOException e) {
				SimulatorPlugin.getDefault().logError(
						"Problems trying to kill simulation process", e);
				e.printStackTrace();
			}

			// Inform user that simulation has been killed
			Display.getDefault().syncExec(
					new ConsoleWriter("***User has killed the simulation***"
							+ "\n"));

			fireSimulationEndedEvent(-1);
		}
	}

	public void addListener(SimulationRunListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(SimulationRunListener listener) {
		this.listeners.remove(listener);
	}

	private void fireSimulationEndedEvent(final int status) {
		// fire event in the display thread
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				for (SimulationRunListener listener : listeners)
					listener.simulationEnded(status);
			}
		});

	}

	// TODO: not static, do not receive execution folder by param.
	public static String getCommand(IFile maFile, String parameters,
			IContainer executionFolder) throws CoreException {
		String path = convertPathToCmd(executionFolder.getLocation().toString()); // is
																					// this
																					// necessary?
		IPath simulatorPath = getSimulatorFilePath(maFile, executionFolder);

		String command = "cd /D \"" + path + "\"\n";
		command += "\"" + simulatorPath + "\" " + parameters + " ";

		return command;
	}

	// TODO: not static, do not receive execution folder by param.
	private static IPath getSimulatorFilePath(IFile maFile,
			IContainer executionFolder) {
		// IPath internalPath = SimulatorPlugin.getInternalPluginFoler();

		// Check if there is a custom generated simulator
		if (SimulationHelper.containsGeneratedSimulator(executionFolder)) {
			// run the simu.exe that is generated when compiling custom .cpp
			// files, that should be located in the current folder.
			return SimulationHelper.getGeneratedSimulatorPath(executionFolder);
		}

		// this brunch should be removed, when simulator unified
		return SimulationHelper.getOriginalSimulatorPath();
	}

	/**
	 * This method converts the given path to command line format by replacing
	 * the path delimiters with Windows path delimiters. (each '/' is replaced
	 * with '\')
	 * 
	 * @param path
	 *            :String - the given current path (e.g. "C:/eclipse/workspace")
	 * @return converted path as a String - path with new delimiters (e.g.
	 *         "C:\eclipse\workspace")
	 * @author Chiril Chidisiuc
	 * 
	 * @version 2006-06-20
	 */
	protected static String convertPathToCmd(String path) {
		char c1 = 47; // character 47 is: '/'
		char c2 = 92; // character 92 is: '\'
		return path.replace(c1, c2); // replace(char oldChar, char newChar)
	}

	private class ConsoleWriter implements Runnable {
		String line;

		public ConsoleWriter(String line) {
			this.line = line;
		}

		public void run() {
			cdBuilder.console.ConsoleDocument.getCDOS().set(line);
			//ConsoleDocument.getCDOS().set(line);
		}
	}

	
	/**
	 * This method output and dispaly test result in the unitTestResultView
	 * @author Nan Yang
	 * @version 2014-02-27
	 */
	private void showUnitTestView() throws IOException {
		ArrayList<ArrayList<String>> expectedData = null;// = new
		// ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> RealData = null;// = new
		// ArrayList<ArrayList<String>>();

		File expectedOutputFile = infoHolder.getExpectedOutputFile();
		File realOutputFile = infoHolder.getRealOutputFile();
		//System.out.println("expected=" + expectedOutputFile.getAbsolutePath());
		//System.out.println("Real==" + realOutputFile.getAbsolutePath());
		//executionFolder.refreshLocal(1, null);

		if (expectedOutputFile.exists() && realOutputFile.exists()) {
			//System.out.println("The two files exists");

			fileExtracter fe = new fileExtracter();
			expectedData = fe.extractOutputFile(expectedOutputFile);
			//System.out.println("expectedData=" + expectedData);

			fileExtracter fe2 = new fileExtracter();
			RealData = fe2.extractOutputFile(realOutputFile);
			//System.out.println("RealData=" + RealData);

			
			String block="******************************************************************* \n";
			String output = block;
			String detail ="\n"+ block+"Overall Unit Test Description: \nThe Overall ";
			
			int failNum=0;

			int realSize = RealData.size();
			int expectedSize = expectedData.size();
			int lineNumber;
			if (realSize > expectedSize) {
				lineNumber = realSize;
			} else {
				lineNumber = expectedSize;
			}

			for (int t = 0; t < lineNumber; t++) {
				if (expectedData.size() > t) {
					output += "Line " + t + " Expected output: "
							+ expectedData.get(t).get(0) + " "
							+ expectedData.get(t).get(1) + " "
							+ expectedData.get(t).get(2);
				} else {
					output += "Line " + t + " Expected output: null";
				}

				if (RealData.size() > t) {
					output += "  Real output: " + RealData.get(t).get(0) + " "
							+ RealData.get(t).get(1) + " "
							+ RealData.get(t).get(2);
				} else {
					output += "  Real output: null ";
					failNum+=1;

				}

				if (expectedData.size() > t && RealData.size() > t) {
					if (expectedData.get(t).get(0)
							.equals(RealData.get(t).get(0))
							&& expectedData.get(t).get(1)
									.equals(RealData.get(t).get(1))
							&& expectedData.get(t).get(2)
									.equals(RealData.get(t).get(2))) {

						output += " Line Pass \n";
					} else {

						output += " Line Fail !!!!!!!!!! This Line Fail  !!!! \n";
						failNum+=1;
					}

				}else{
					output += " Line Fail !!!!!!!!!! This Line Fail  !!!! \n";
					failNum+=1;
					
				}

			}
			if (expectedData.toString().replaceAll(" ", "")
					.equals(RealData.toString().replaceAll(" ", ""))) {
				infoHolder.setState("Unit Test Pass");
				detail+="Unit Test Pass \n";
				// sendInfoPass("Unit text Pass",output);
			} else {
				// sendInfo("Unit text Fail",output);
				infoHolder.setState("Unit Test Fail");
				detail+="Unit Test Fail \n";
			}
			detail+="Expected output number: "+expectedSize+"\n";
			detail+="Real output number: "+realSize+"\n";
			if(expectedSize!=realSize){
				detail+="ERROR: Real output number and the Expected output number do not match \n";
			}
			detail+="Passed output number: "+(expectedSize-failNum)+"\n";
			detail+="Failed output number: "+failNum+"\n";
			detail+=block;
			infoHolder.setText(output+detail);

		} else {

			infoHolder
					.setState("UnitTest Fail, Expected and/or Real output File do not exist");
			infoHolder.setText("");
			
		}

		//UnitTestView.label.setText(infoHolder.getState() + "\n"+ infoHolder.getText());
		Display.getDefault().asyncExec(new Runnable(){
			public void run() {
				//label = new Label(parent, 0);
				UnitTestView.label.setText(infoHolder.getState()+"\n"+infoHolder.getText());
                //System.out.println("Timer out");
		    }
		});
	}

	public interface SimulationRunListener extends EventListener {
		public void simulationEnded(int status);
	}

}// end class runSimu
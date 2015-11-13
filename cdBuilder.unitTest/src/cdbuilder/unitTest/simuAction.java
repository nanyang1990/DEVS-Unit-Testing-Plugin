/* This file is created by Nan Yang in 01/17/2014
 * 
 * This file is highly based on cdBuilder.buttons.simuAction form 
 * the CDBuilderPluginDevelopmentProject_from_Matias project
 * 
 * Certain function have been added to accomplish unit test objective.
 */
package cdbuilder.unitTest;


import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import cdBuilder.CDBuilderPlugin;
import cdBuilder.simulator.SimulationHelper;
import cdbuilder.common.ResourceHelper;

/**
 * @author ssim
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class simuAction extends Action implements IWorkbenchWindowActionDelegate{

	public simuAction() {
		super();
		setText("Simulate");
		setDescription("This is a Simulator");
	}
	
	public void run() {
		// This method is not called
	}
	
	public void run(IAction action) {
		try {
			
			if(SimulationHelper.checkCurrentProjectReadyToSimulate())
			{
				IContainer selectedFolder = getSelectedFolder();
				//show console view
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("ConsoleView");
				// Open Dialog
				openRunSimulationDialog(selectedFolder, !SimulationHelper.containsCPPFiles(selectedFolder));
			}
		} catch (CoreException e) {
			CDBuilderPlugin.getDefault().logError("Problems running the simulation", e);
		}
	}

	private static Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	
	private static IContainer getSelectedFolder() {		
	    IResource selectedResource = ResourceHelper.getSelectedResource();
		if (selectedResource instanceof IContainer)
			return (IContainer) selectedResource;
	    
	    return selectedResource.getParent();
	}
	
	public void dispose() {
		// do nothing.
	}
	
	public void init(IWorkbenchWindow window) {
		// do nothing.
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing.
	}		
	
	private static void openRunSimulationDialog(IContainer containerFolder,	boolean isCellDEVS) throws CoreException {
		// Open Dialog		
		unitTestDialog dialog = new unitTestDialog(getShell(), containerFolder);
	//	SimuGui dialog = new SimuGui(getShell(), containerFolder.getLocation(), isCellDEVS , null);
		dialog.create();
		dialog.open();
		dialog.close();
		containerFolder.refreshLocal(1, null);
		
	}
}


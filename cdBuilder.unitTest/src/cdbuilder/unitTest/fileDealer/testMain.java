package cdbuilder.unitTest.fileDealer;

import java.io.File;
import java.io.IOException;
//import java.util.ArrayList;




public class testMain{
	//private static ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
	
	 public static void main(String[] args) throws IOException {
		 
		 
		 
		 
		 
		// IWorkspaceRoot root = (IWorkspaceRoot)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getInput();
		 //IPath projectPath;
		 //projectPath = root.getLocation();
		
		 File tragetFile=new File("D:\\study\\C_eclipse\\eclipse\\workspace\\TestExample\\House1Type.cpp");
		 //File tragetFile=new File("C:\\eclipse\\workspace\\ATM\\UserInterface.cpp");
		 //File tragetFile=new File("C:\\eclipse\\workspace\\Garage Door New\\countCode.cpp");
		 
		 fileExtracter fe=new fileExtracter();
		// System.out.println(tragetFile.getPath());
		 //System.out.println(tragetFile.getAbsolutePath());
		 
		
		 
		 fe.extractCppFile(tragetFile);
//		 fe.extractHeaderFile(tragetFile);
//		 fileGenerater FG=new fileGenerater (fe.getData(),"C:\\eclipse\\workspace\\ATM\\UserInterface2.ma");
//		 FG.generateMaFile();
//		 
//		 File expectedOutputFile=new File("C:\\eclipse\\workspace\\ATM\\ATMOUT_9.out");
//		 fe.extractOutputFile(expectedOutputFile);
//		 
//		 
	 }
	 
	 
	 
	 
	 
	
	
	
}
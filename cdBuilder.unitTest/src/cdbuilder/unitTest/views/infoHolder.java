/* This file is created by Nan Yang in 01/18/2014
 * This class is used to save the unit test result
 */

package cdbuilder.unitTest.views;

import java.io.File;

public class infoHolder{
	static String State="This is default State";


	static String text="This is default";
	
	static File expectedOutputFile;
	static File realOutputFile;
	
	public static String getText() {
		return text;
	}

	public static void setText(String text) {
		infoHolder.text = text;
	}

	
	public static String getState() {
		return State;
	}

	public static void setState(String state) {
		State = state;
	}
	
	public static File getExpectedOutputFile() {
		return expectedOutputFile;
	}

	public static void setExpectedOutputFile(File expectedOutputFile) {
		infoHolder.expectedOutputFile = expectedOutputFile;
	}

	public static File getRealOutputFile() {
		return realOutputFile;
	}

	public static void setRealOutputFile(File realOutputFile) {
		infoHolder.realOutputFile = realOutputFile;
	}

	public infoHolder(){
		
		
	}
	                    
	                    
	
	
}
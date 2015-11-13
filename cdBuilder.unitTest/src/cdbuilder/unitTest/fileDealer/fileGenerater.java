/* This file is created by Nan Yang in 01/15/2014
 * This class is used to generate ma file automatically
 * 
 */



package cdbuilder.unitTest.fileDealer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;





public class fileGenerater{
	
	public static final int component=0;
	public static final int inputPort=1;
	public static final int outputPort=2;
	public static final int timeName=3;
	public static final int timeValue=4;
	
	 private ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();;
	 private String fullpath;
	 FileWriter writer;
	
	public fileGenerater(ArrayList<ArrayList<String>> data, String fullpath){
		this.data=data;
		this.fullpath=fullpath;
	}
	
	public File generateMaFile() throws IOException{
		File outputFile=new File(fullpath);
		
		writer = new FileWriter(outputFile);
		
		writer.write("[top]\n");
		writer.write("components : ");
		
		writer.write("component"+"@"+data.get(component).get(0) + "\n");		
		
		
		
		writer.write("in : ");
		for(int t=0;t<data.get(inputPort).size();t++){
			writer.write(data.get(inputPort).get(t)+ " ");
			if(t==data.get(inputPort).size()-1){
				writer.write("\n");
			}
		}
		
		writer.write("out : ");
		for(int t=0;t<data.get(outputPort).size();t++){
			writer.write(data.get(outputPort).get(t)+ " ");
			if(t==data.get(outputPort).size()-1){
				writer.write("\n");
			}
		}		
		
		
		writer.write("%Link : \n");
		for(int t=0;t<data.get(inputPort).size();t++){
			String input=data.get(inputPort).get(t);
			writer.write("Link : "+input+" "+input+"@component\n" );
		}
		
		for(int t=0;t<data.get(outputPort).size();t++){
			String output=data.get(outputPort).get(t);
			writer.write("Link : "+" "+output+"@component "+output+"\n" );
		}
		
		writer.write("[component]\n");
		for(int t=0;t<data.get(timeName).size();t++){
			writer.write(data.get(timeName).get(t)+" : "+data.get(timeValue).get(t)+"\n");
			
			
		}

		writer.flush();
		writer.close();
		return outputFile;
	}
	
	
	
	
}
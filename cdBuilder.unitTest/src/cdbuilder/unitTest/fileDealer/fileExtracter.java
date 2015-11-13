/* This file is created by Nan Yang in 01/15/2014
 * This class is used to extract data from .cpp file for automatically building Ma file
 * This class is also used to  extract data from .expected output file
 * to compare with real output file
 */
package cdbuilder.unitTest.fileDealer;





import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class fileExtracter {
	private ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
	private ArrayList<ArrayList<String>> outputData = new ArrayList<ArrayList<String>>();
	private File tragetFile;
	
	public static final int component=0;
	public static final int inputPort=1;
	public static final int outputPort=2;
	public static final int timeName=3;
	public static final int timeValue=4;
	
	
	
	public fileExtracter(){
		data.add(new ArrayList<String>());
		data.add(new ArrayList<String>());
		data.add(new ArrayList<String>());
		data.add(new ArrayList<String>());
		data.add(new ArrayList<String>());	
	}
	

	/* This method is used to extract data from given cpp file
	 * 
	 */
	@SuppressWarnings("null")
	public boolean extractCppFile(File file) throws IOException{
		tragetFile=file;
		//String fileName=tragetFile.getName().replaceAll(".cpp", "");
		//data.get(component).add(fileName);
		BufferedReader  br = new BufferedReader(new FileReader(tragetFile));
		String [] dataArray = new String[30];
		
		
	        try {
	        	
	            String line = "//default line";
	            String startKey="::";
	            String endKey="{";
	            String dataString=null;
	            
	            while (line != null) {
	            	
	            	line=this.checkAndAvoidComment(br);
	            	
	            	
	            	if(line.contains(startKey)){
	            		dataString+=line;
	            		String className=line.substring(0,line.indexOf(startKey));
	            		data.get(component).add(className); //get class name
	            		//System.out.println("Class name is "+className);
	            		
	            		while(!line.contains(endKey)){
	            			line=checkAndAvoidComment(br);
	            			dataString+=line;
	            			
	            		}
	            		
	            		
	            	//),
	            		dataString=dataString.substring(dataString.indexOf(",")+1).replaceAll("\\s","");
	            		//System.out.println(dataString);
	            		//dataArray
	            		
	            		dataArray=dataString.split(Pattern.quote("),"));
	            		
	            		
	            		//System.out.println(dataArray.length);
	            		
	            		for(int t=0;t<dataArray.length;t++ ){
	            			//System.out.println(dataArray[t]);
	            			
	            			if (dataArray[t].contains("(addInputPort(")) {
	            				dataArray[t]=dataArray[t].substring(dataArray[t].indexOf("\"")+1);
	            				dataArray[t]=dataArray[t].substring(0,dataArray[t].indexOf("\""));
	            				//dataArray[t]=dataArray[t].replaceAll(Pattern.quote(""), "");
	    	                	//System.out.println(dataArray[t]);
	    	                	data.get(inputPort).add(dataArray[t]);
	    	                	
	    	                } else if (dataArray[t].contains("(addOutputPort")) {
	    	                	dataArray[t]=dataArray[t].substring(dataArray[t].indexOf("\"")+1);
	            				dataArray[t]=dataArray[t].substring(0,dataArray[t].indexOf("\""));
	    	                	//System.out.println(dataArray[t]);
	    	                	data.get(outputPort).add(dataArray[t]);
	    	                    // ...
	    	                	
	    	                }  else if (dataArray[t].matches("(.*)(\\d{1,2},\\d{1,2},\\d{1,2},\\d{1,2})(.*)")) {
	    	                	//System.out.println("dataArray[t== "+dataArray[t]);
	    	                	String name=dataArray[t].substring(0, dataArray[t].indexOf("("));
	    	                	//name=name.replaceAll(",", "");
	    	                	//System.out.println("name= "+name);
	    	                	
	    	                	String value=dataArray[t].replace(name, "").replaceAll("\\(", "").replaceAll("\\)\\{", "");
	    	                	//value=value.substring(value.indexOf(",")+2, value.length()-1).replaceAll(",", ":");
	    	                	//System.out.println("value= "+value);
	    	                	data.get(timeName).add(name);
	    	                	data.get(timeValue).add(value);
	    	                	
	    	                	//System.out.println(dataArray[t]);
	    	                }                    			
	            		}
	            		return true;
	            	}   
	            }       
	        } finally {
	            br.close() ;	            
	            System.out.println(data);
	        }
			return false;
	}
	
	
	/* This method is used to extract data from expected output file
	 * 
	 */
	public ArrayList<ArrayList<String>> extractOutputFile(File file) throws IOException{
		
		tragetFile=file;
		//BufferedReader  br = new BufferedReader(new FileReader(tragetFile));
		String[] RowData = null;
		
		InputStream input = new FileInputStream(tragetFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));

		try {// save file info to String array RowData[]
			String line;
			int t=0;
			while ((line = reader.readLine()) != null) {
				RowData = line.split(" ");
				
				//System.out.println(RowData[0]);
				//System.out.println(RowData[1]);
				//System.out.println(RowData[2]);
				outputData.add(new ArrayList<String>());
				outputData.get(t).add(RowData[0]);
				
				outputData.get(t).add(RowData[1]);
				outputData.get(t).add(RowData[2]);
				t++;
			}
		} catch (IOException ex) {
			// handle exception
		} finally {
			
				input.close();
				//System.out.println(outputData);
		}
		return outputData;
		
	}
	
	
	
	public String checkAndAvoidComment(BufferedReader br ) throws IOException{
		String inputString=br.readLine();
		boolean commentRemoved=false;
		while(commentRemoved==false&&inputString!=null){
			if(isLineAComment(inputString)){
				inputString=br.readLine().replaceAll("\\s","");
			}
			else if(isLineAMultiLineCommentStart(inputString)){
				while((!isLineAMultiLineCommentEnd(inputString))&& inputString!=null){
					inputString=br.readLine().replaceAll("\\s","");
				}
				inputString=br.readLine().replaceAll("\\s","");
			}
			else if(isLineContainsComment(inputString)){
				int beginIndex=inputString.indexOf("//");
				inputString=inputString.substring(0,beginIndex);
				commentRemoved=true;
			}
			else //if(!isLineContainsComment(inputString) && !isLineAComment(inputString)){
				commentRemoved=true;
			//}
			
		}
		return inputString;

	}
	
	
	public boolean isLineContainsComment(String line){
		if(line.contains("//")  )
			return true;

		return false;
	}
	
	
	
	public boolean isLineAComment(String line){
		if((line.startsWith("/*")&&line.endsWith("*/")) || line.startsWith("//")  )
			return true;

		return false;
	}
	
	public boolean isLineAMultiLineCommentStart(String line){
		if(line.startsWith("/*") )
			return true;	
		return false;
	}
	
	
	public boolean isLineAMultiLineCommentEnd (String line){
		if(line.endsWith("*/") ){
			return true;
		}
		else
		return false;
	}
	
	
	
    public  ArrayList<ArrayList<String>> getData() {
		return data;
	}

    

}
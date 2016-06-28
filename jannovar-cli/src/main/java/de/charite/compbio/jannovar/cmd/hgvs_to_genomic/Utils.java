package de.charite.compbio.jannovar.cmd.hgvs_to_genomic;
import htsjdk.variant.vcf.VCFHeader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.omg.CORBA.portable.InputStream;


public class Utils {

	public static void write(String filename,String string,boolean append){
		    BufferedWriter output = null;
	        try {
	            File file = new File(filename); 
	            output = new BufferedWriter(new FileWriter(file,append));
	            output.write(string);
	        } catch ( IOException e ) {
	            e.printStackTrace();
	        } finally {      
					try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			
	          
	        }
		
	}

	public static  void writeHeader(String vcf_file, boolean b) throws IOException {
		    BufferedWriter output = null;
		    try{
			    output = new BufferedWriter(new FileWriter(vcf_file,b));
			    String path=new File(".").getCanonicalPath();
				FileInputStream fstream =   new FileInputStream(new File(path+"/header.txt"));
//				System.out.println(path);
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
				String strLine;
				while ((strLine = br.readLine()) != null)   {
					output.write(strLine+"\n");
				}
				br.close();
		        output.close();
		    }catch(Exception e){
		    	e.printStackTrace();
		    }		
	          
	        
		
	}
}

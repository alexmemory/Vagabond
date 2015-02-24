package org.vagabond.performance.bitmarker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.csvreader.CsvWriter;

public class TestMarkerGenerator {
	static Logger log = Logger.getLogger(TestMarkerGenerator.class);
	
	public static void main(String[] args) {
		
		String outputFile = "resource/test/markers.txt";
		PropertyConfigurator.configure("resource/test/perfLog4jproperties.txt");
		
		// before we open the file check to see if it already exists
		boolean alreadyExists = new File(outputFile).exists();
		
		if (alreadyExists) {
			new File(outputFile).delete();
		}
		
		try {
			
			CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ' ');
			Random randomGenerator = new Random();
			/*
			// write out a few records
			for (int i = 1; i < 51; i++) {
				csvOutput.write("A(person,1|"+ i +"|"+ i +",name)");
				csvOutput.endRecord();
			}
			
			for (int i = 51; i < 101; i++) {
				csvOutput.write("A(person,1|"+ i +"|"+ i +",livesin)");
				csvOutput.endRecord();
			}
			
			
			// Employee Scenario
			for (int idx = 1; idx <= 3; ++idx){
				
				int randomInt = randomGenerator.nextInt(100);
				csvOutput.write("A(person,"+ randomInt +"|"+ randomInt +"|"+ randomInt +",name)");
				csvOutput.endRecord();
				
			}
			*/
			
			for (int idx = 1; idx <= 5; ++idx){
				
		      int randomInt = randomGenerator.nextInt(1000);
		      csvOutput.write("A(testcp0nl0ce0copy00,1|"+ randomInt +",nut_cp_0_nl0_ae0ke0)");
		      csvOutput.endRecord();
		    
			}			
			
			csvOutput.close();
			
			log.debug("------ 'markers' file has been created ------");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}

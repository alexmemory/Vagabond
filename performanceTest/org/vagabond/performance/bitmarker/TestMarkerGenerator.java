package org.vagabond.performance.bitmarker;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.vagabond.test.TestOptions;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.ResultSetUtil;

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
			for (int idx = 1; idx <= 100; ++idx){
				
				int randomInt = randomGenerator.nextInt(100000);
				csvOutput.write("A(person,1|" + randomInt +"|"+ randomInt +",name)");
				csvOutput.endRecord();
				
			}
			
			
			for (int i = 1; i <= 50; i++) {
				csvOutput.write("A(testcp1nl0ce0copy10,1|"+ i +",nut_cp_1_nl0_ae0ke0)");
				csvOutput.endRecord();
			}
			
			
			// Employee Scenario
			for (int idx = 1; idx <= 25; ++idx){
				
				int randomInt = randomGenerator.nextInt(100);
				csvOutput.write("A(person,"+ randomInt +"|"+ randomInt +"|"+ randomInt +",name)");
				csvOutput.endRecord();
				
			}	
			
			for (int idx = 1; idx <= 3; ++idx){
				
				int randomInt = randomGenerator.nextInt(100);
				csvOutput.write("A(organigram,"+ randomInt +"|"+ randomInt +"|"+ randomInt +",subordinate)");
				csvOutput.endRecord();
				
			}
			
			
			for (int idx = 1; idx <= 100; ++idx){
				
		      int randomInt = randomGenerator.nextInt(100000);
		      csvOutput.write("A(branchvp1nl0ce1,1|"+ randomInt +",measure_vp_1_nl0_ae2)");
		      csvOutput.endRecord();
		    
			}
			*/
			/*
			for (int idx = 1; idx <= 100; ++idx){
				
		      int randomInt = randomGenerator.nextInt(1000);
		      csvOutput.write("A(branchvp1nl0ce1,1|"+ randomInt +",measure_vp_1_nl0_ae2)");
		      csvOutput.endRecord();
		    
			}
			*/
			String[] cols;
			ResultSet rs;
						
			try {
				
				Connection con = TestOptions.getInstance().getConnection();
				rs = ConnectionManager.getInstance().execQuery(con,
						"SELECT tid FROM target.tarticle ORDER BY random() limit 100");
					
	            while (rs.next()) {
	                String em = rs.getString("tid");
	                cols = em.split("\n");
	                for (int i =0; i < cols.length; i++){
	                	csvOutput.write("A(tarticle," + cols[i] + ",articleid)");
	                }
	                csvOutput.endRecord();
	            }
	            
	            ConnectionManager.getInstance().closeRs(rs);
	            
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*
			for (int idx = 1; idx <= 5; ++idx){
				
			      int randomInt1 = randomGenerator.nextInt(200);
			      int randomInt2 = randomGenerator.nextInt(200);
			      int randomInt3 = randomGenerator.nextInt(200);
			      csvOutput.write("A(tarticle,"+ randomInt1 +"|"+ randomInt2 +"|"+ randomInt3 +",articleid)");
			      csvOutput.endRecord();
			    
			}
			*/
			csvOutput.close();
			
			log.debug("------ 'markers' file has been created ------");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}

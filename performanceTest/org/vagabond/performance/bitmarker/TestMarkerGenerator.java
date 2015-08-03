package org.vagabond.performance.bitmarker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema;
import org.vagabond.test.TestOptions;
import org.vagabond.util.ConnectionManager;
import org.vagabond.xmlmodel.MappingScenarioDocument.MappingScenario;

import com.csvreader.CsvWriter;


public class TestMarkerGenerator {
	
	static Logger log = Logger.getLogger(TestMarkerGenerator.class);
	public static String relName;
	public static int numRel;
	

	public static void main(String[] args) {
		
//		String outputFile = "resource/test/markers.txt";
		PropertyConfigurator.configure("resource/test/perfLog4jproperties.txt");
		String num = args[0];
		
		// before we open the file check to see if it already exists
//		boolean alreadyExists = new File(outputFile).exists();
				
//		if (alreadyExists) {
//			new File(outputFile).delete();
//		}
		
		try {
			
//			CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ' ');
	
			String[] cols;
			ResultSet rs;
						
			try {
	
				Connection con = TestOptions.getInstance().getConnection();
				
				// Count target tables
				rs = ConnectionManager.getInstance().execQuery(con, 
						"SELECT COUNT(table_name) AS numRel FROM information_schema.tables WHERE table_schema='target'");
				
				while (rs.next()) {
					numRel = Integer.parseInt(rs.getString("numRel"));
				}
				
				ConnectionManager.getInstance().closeRs(rs);

				
				// Generate markers       
				for (int k = 0; k < Integer.parseInt(num); k++) {
					
					// Randomly pick a table
					rs = ConnectionManager.getInstance().execQuery(con, 
							"SELECT table_name FROM (" +
									        "SELECT table_name FROM information_schema.tables WHERE table_schema='target') a " +
							"ORDER BY RANDOM() LIMIT 1");
					
					while (rs.next()) {
						relName = rs.getString("table_name");
					}
	
					ConnectionManager.getInstance().closeRs(rs);
					
				
					// Create markers
					rs = ConnectionManager.getInstance().execQuery(con, 
							"SELECT tid, attname " + 
							"FROM (SELECT tid FROM target."+ relName +" ORDER BY RANDOM() LIMIT 1) a, " +
								  "(SELECT attname FROM pg_catalog.pg_attribute WHERE attrelid = 'target."+ relName +"'::regclass " +
								  													"AND attnum > 0 AND NOT attisdropped AND attname <> 'tid' LIMIT 1) b");
	            	
			        while (rs.next()) {
			        	String em = rs.getString("tid");
			            cols = em.split("\n");
			            			            
			            for (int i = 0; i < cols.length; i++){
			            	System.out.println("A(" + relName + "," + cols[i] + "," + rs.getString("attname")+ ")");
			            }
//	                csvOutput.endRecord();
			        }
			            
			        ConnectionManager.getInstance().closeRs(rs);
			    	
				}
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			csvOutput.close();
			
//			log.debug("------ 'markers' file has been created ------");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}

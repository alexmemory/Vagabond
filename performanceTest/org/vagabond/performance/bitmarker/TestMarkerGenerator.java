package org.vagabond.performance.bitmarker;

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

import com.csvreader.CsvWriter;

import org.vagabond.explanation.marker.AttrValueMarker;
import org.vagabond.explanation.marker.ISingleMarker;

public class TestMarkerGenerator {
	static Logger log = Logger.getLogger(TestMarkerGenerator.class);
	
	public static void main(String[] args) {
		
//		String outputFile = "resource/test/markers.txt";
		PropertyConfigurator.configure("resource/test/perfLog4jproperties.txt");
		
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
				rs = ConnectionManager.getInstance().execQuery(con, 
						"SELECT tid, attname " + 
						"FROM (SELECT tid FROM target.testcp1nl0ce0copy1_0 ORDER BY tid LIMIT 5) a, " +
							  "(SELECT attname FROM pg_catalog.pg_attribute WHERE attrelid = 'target.testcp1nl0ce0copy1_0'::regclass " +
							  													"AND attnum > 0 AND NOT attisdropped AND attname <> 'tid' LIMIT 1) b");
				

	            while (rs.next()) {
	                String em = rs.getString("tid");
	                cols = em.split("\n");
	                for (int i =0; i < cols.length; i++){
	                	System.out.println("A(testcp1nl0ce0copy1_0," + cols[i] + "," + rs.getString("attname")+ ")");
	                }
//	                csvOutput.endRecord();
	            }
	            
	            ConnectionManager.getInstance().closeRs(rs);
	            
				rs = ConnectionManager.getInstance().execQuery(con, 
						"SELECT tid, attname " + 
						"FROM (SELECT tid FROM target.testcp1nl0ce0copy1_0 ORDER BY tid LIMIT 5) a, " +
							  "(SELECT attname FROM pg_catalog.pg_attribute WHERE attrelid = 'target.testcp1nl0ce0copy1_0'::regclass " +
							  													"AND attnum > 0 AND NOT attisdropped AND attname <> 'tid' LIMIT 1) b");
				

	            while (rs.next()) {
	                String em = rs.getString("tid");
	                cols = em.split("\n");
	                for (int i =0; i < cols.length; i++){
	                	System.out.println("A(testcp1nl0ce0copy1_0," + cols[i] + "," + rs.getString("attname")+ ")");
	                }
//	                csvOutput.endRecord();
	            }
	            
	            ConnectionManager.getInstance().closeRs(rs);
	            
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

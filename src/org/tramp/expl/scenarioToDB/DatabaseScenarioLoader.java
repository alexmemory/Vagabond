package org.tramp.expl.scenarioToDB;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.tramp.expl.model.MapScenarioHolder;
import org.tramp.xmlmodel.DataType;

public class DatabaseScenarioLoader {

	static Logger log = Logger.getLogger(DatabaseScenarioLoader.class);
	
	private static DatabaseScenarioLoader instance = new DatabaseScenarioLoader ();
	
	private DatabaseScenarioLoader () {
		
	}
	
	public static DatabaseScenarioLoader getInstance () {
		return instance;
	}
	
	public void loadScenario (Connection dbCon, MapScenarioHolder map) 
			throws SQLException {
		String ddl;
		
		ddl = SchemaCodeGenerator.getInstance().
				getSchemasCode(map.getScenario());
		executeDDL(dbCon, ddl);
		if (map.hasData())
			loadData (map.getScenario().getData());
	}
	
	private void executeDDL(Connection dbCon, String ddl) throws SQLException {
		Statement st;
		
		st = dbCon.createStatement();
		st.addBatch(ddl);
		st.executeBatch();
		st.close();
	}
	
	private void loadData (DataType data) {
		for (InstanceType inst: data.getInstanceArray()) {
			
		}
		
		for (InstanceFileType inst: data.getInstanceFileArray()) {
			
		}
		
	}
	
}

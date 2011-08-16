package org.vagabond.mapping.scenarioToDB;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger; import org.vagabond.util.LogProviderHolder;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.xmlmodel.DataType;
import org.vagabond.xmlmodel.RelInstanceFileType;
import org.vagabond.xmlmodel.RelInstanceType;
import org.vagabond.xmlmodel.RelInstanceType.Row;

public class DatabaseScenarioLoader {

	static Logger log = LogProviderHolder.getInstance().getLogger(DatabaseScenarioLoader.class);
	
	private static DatabaseScenarioLoader instance = new DatabaseScenarioLoader ();
	
	private DatabaseScenarioLoader () {
		
	}
	
	public static DatabaseScenarioLoader getInstance () {
		return instance;
	}
	
	public void loadScenario (Connection dbCon) throws SQLException {
		loadScenario (dbCon, MapScenarioHolder.getInstance());
	}
	
	public void loadScenarioNoData (Connection dbCon) throws SQLException {
		loadScenarioNoData(dbCon, MapScenarioHolder.getInstance());
	}
	
	public void loadScenarioNoData (Connection dbCon, MapScenarioHolder map) 
			throws SQLException {
		loadScenario (dbCon, map, true);
	}
	
	public void loadScenario (Connection dbCon, MapScenarioHolder map) 
			throws SQLException {
		loadScenario (dbCon, map, false);
	}
	
	private void loadScenario (Connection dbCon, MapScenarioHolder map, 
			boolean noData) 
			throws SQLException {
		String ddl;
		
		ddl = SchemaCodeGenerator.getInstance().
				getSchemaCodeNoFKeys(map.getScenario());
		log.debug("execute Schema DDL:\n" + ddl);
		executeDDL(dbCon, ddl);
		
		if (map.hasData() && !noData)
			loadData (dbCon, map.getScenario().getData());
		
		ddl = SchemaCodeGenerator.getInstance().getAllSourceForeignKeysCode
				(map.getScenario().getSchemas().getSourceSchema(), "source");
		log.debug("execute Foreign Key DDL:\n" + ddl);
		executeDDL(dbCon, ddl);
	}
	
	private void executeDDL(Connection dbCon, String ddl) throws SQLException {
		Statement st;
		
		st = dbCon.createStatement();
		st.execute(ddl);
		st.close();
	}
	
	private void loadData (Connection dbCon, DataType data) throws SQLException {
		Statement st;
		
		st = dbCon.createStatement();
		
		for (RelInstanceType inst: data.getInstanceArray()) {
			for(Row row: inst.getRowArray()) {
				st.execute(SchemaCodeGenerator.getInstance().
						getRowInsert("source", inst.getName(), row));
			}
		}
		
		for (RelInstanceFileType inst: data.getInstanceFileArray()) {
			st.execute(SchemaCodeGenerator.getInstance().
					getCopy("source", inst));
		}
		
		st.close();
	}
}

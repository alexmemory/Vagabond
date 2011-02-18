package org.tramp.expl.scenarioToDB;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.tramp.expl.model.MapScenarioHolder;
import org.tramp.xmlmodel.DataType;
import org.tramp.xmlmodel.RelInstanceFileType;
import org.tramp.xmlmodel.RelInstanceType;
import org.tramp.xmlmodel.RelInstanceType.Row;

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
				getSchemaCodeNoFKeys(map.getScenario());
		log.debug("execute Schema DDL:\n" + ddl);
		executeDDL(dbCon, ddl);
		
		if (map.hasData())
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

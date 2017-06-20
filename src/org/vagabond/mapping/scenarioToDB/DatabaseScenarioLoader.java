package org.vagabond.mapping.scenarioToDB;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.DataType;
import org.vagabond.xmlmodel.MappingScenarioDocument.MappingScenario;
import org.vagabond.xmlmodel.RelInstanceFileType;
import org.vagabond.xmlmodel.RelInstanceType;
import org.vagabond.xmlmodel.RelInstanceType.Row;

public class DatabaseScenarioLoader {

	public enum LoadMode {
		Conservative, // do not check but load everything from scratch
		Lazy // avoid creating schema element or load data if already avialable
	}
	
	static Logger log = LogProviderHolder.getInstance().getLogger(DatabaseScenarioLoader.class);
	
	private static DatabaseScenarioLoader instance = new DatabaseScenarioLoader ();
	
	private LoadMode operationalMode = LoadMode.Conservative;
	
	private DatabaseScenarioLoader () {
		
	}
	
	public static DatabaseScenarioLoader getInstance () {
		return instance;
	}
	
	public LoadMode getOperationalMode() {
		return operationalMode;
	}

	public void setOperationalMode(LoadMode operationalMode) {
		this.operationalMode = operationalMode;
	}

	public void loadScenario (Connection dbCon) throws Exception {
		loadScenario (dbCon, MapScenarioHolder.getInstance());
	}
	
	public void loadScenario (Connection dbCon, File csvPath) throws Exception {
		loadScenario (dbCon, MapScenarioHolder.getInstance(), csvPath);
	}
	
	public void loadScenarioNoData (Connection dbCon) throws Exception {
		loadScenarioNoData(dbCon, MapScenarioHolder.getInstance());
	}
	
	public void loadScenarioNoData (Connection dbCon, MapScenarioHolder map) 
			throws Exception {
		loadScenario (dbCon, map, true, null);
	}
	
	public void loadScenario (Connection dbCon, MapScenarioHolder map) 
			throws Exception {
		loadScenario (dbCon, map, false, null);
	}
	
	public void loadScenario (Connection dbCon, MapScenarioHolder map, File csvPath) 
			throws Exception {
		loadScenario (dbCon, map, false, csvPath);
	}
	
	private void loadScenario (Connection dbCon, MapScenarioHolder map, 
			boolean noData, File csvPath) 
			throws Exception {
		String ddl;
		
		// lazy operation checks whether scenario is already there
		if(operationalMode.equals(LoadMode.Lazy)
				&& schemaCreated(dbCon, map)) {
			if (!dataLoaded(dbCon, map) && !noData) {
				executeDDL(dbCon, SchemaCodeGenerator.getInstance()
						.getInstanceDelCode(map.getScenario()));
				loadData(dbCon, map.getScenario().getData(), csvPath, map.getScenario());
			}
			return;
		}
		
		ddl = SchemaCodeGenerator.getInstance().
				getSchemaCodeNoFKeys(map.getScenario());
		if (log.isDebugEnabled()) {log.debug("execute Schema DDL:\n" + ddl);};
		executeDDL(dbCon, ddl);
		
		if (map.hasData() && !noData)
			loadData (dbCon, map.getScenario().getData(), csvPath, map.getScenario());
		
		ddl = SchemaCodeGenerator.getInstance().getAllSourceForeignKeysCode
				(map.getScenario().getSchemas().getSourceSchema(), 
						SchemaCodeGenerator.SOURCE_SCHEMA_NAME);
		if (log.isDebugEnabled()) {log.debug("execute Foreign Key DDL:\n" + ddl);};
		executeDDL(dbCon, ddl);
	}

	private boolean schemaCreated (Connection dbCon, MapScenarioHolder map) throws Exception {
		String checkQuery = SchemaCodeGenerator.getInstance()
				.getCheckCode(map.getScenario());
		
		return executeBooleanQuery(dbCon, checkQuery);
	}
	
	private boolean dataLoaded (Connection dbCon, MapScenarioHolder map) throws Exception {
		String checkQuery = SchemaCodeGenerator.getInstance()
				.getInstanceCheckCode(map.getScenario());
		
		return executeBooleanQuery(dbCon, checkQuery);
	}
	
	private boolean executeBooleanQuery (Connection dbCon, String sql) throws Exception {
		Statement st;
		ResultSet rs;
		boolean result;
		
		if (log.isDebugEnabled()) {log.debug("run boolean query <" + sql + ">");};
		
		st = dbCon.createStatement();
		rs = st.executeQuery(sql);
		if (!rs.next())
			throw new Exception("one result row expected " +
					"for query <" + sql + ">");
		result = rs.getBoolean(1);
		rs.close();
		st.close();
		
		return result;
	}
	
	private void executeDDL(Connection dbCon, String ddl) throws SQLException {
		Statement st;
		
		st = dbCon.createStatement();
		st.execute(ddl);
		st.close();
	}
	
	private void loadData (Connection dbCon, DataType data, File csvPath, MappingScenario map) throws Exception {
		Statement st;
		st = dbCon.createStatement();
		boolean isTargetLoadAndExchange = SchemaCodeGenerator.getInstance().
				isExangeDataAndLoadTargetData(map);
		for (RelInstanceType inst: data.getInstanceArray()) {
			for(Row row: inst.getRowArray()) {
				String relName = isTargetLoadAndExchange ? 
						SchemaCodeGenerator.getInstance().
								getExtraTargetDataRelName(inst.getName()) : 
						inst.getName();
				st.execute(SchemaCodeGenerator.getInstance().
						getRowInsert(SchemaCodeGenerator.SOURCE_SCHEMA_NAME, 
								relName, row));
			}
		}
		
		for (RelInstanceFileType inst: data.getInstanceFileArray()) {
			
			st.execute(SchemaCodeGenerator.getInstance().
					getCopy(SchemaCodeGenerator.SOURCE_SCHEMA_NAME, inst, csvPath, map));
		}
		
		st.close();
	}
}

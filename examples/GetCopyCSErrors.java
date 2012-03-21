import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.PropertyConfigurator;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.test.TestOptions;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.ResultSetUtil;

public class GetCopyCSErrors {
	
	private static Connection con;

	public static void main(String args[]) throws Exception {
		try {
			con = TestOptions.getInstance().getConnection();

			// Without materializing
			createViews();
			GatherStats4Query.gatherStats("GetCopyCSErrors", con);
			cleanupViews();
			
			// Materializing
			materializeProvs();
			GatherStats4Query.gatherStats("GetCopyCSErrors", con);
			cleanupTables();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			con.close();
		}
		
	}
	
	private static void createViews() throws Exception {
		materializeProv("drop view if exists prov_name", "CreateViewProv4Name");
		materializeProv("drop view if exists prov_livesin", "CreateViewProv4Livesin");
	}

	private static void materializeProvs() throws Exception {
		materializeProv("drop table if exists prov_name", "MaterializeProv4Name");
		materializeProv("drop table if exists prov_livesin", "MaterializeProv4Livesin");
	}

	private static void materializeProv(String prev_query, String xmlName) throws Exception {
		try {
			GatherStats4Query.runDDLQuery(con, prev_query);
			String query = GatherStats4Query.getQuery(xmlName);
			GatherStats4Query.runDDLQuery(con, query);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		}

	}
	
	private static void cleanupViews() throws Exception {
		executeDDL("drop view prov_name");
		executeDDL("drop view prov_livesin");
	}
	
	private static void cleanupTables() throws Exception {
		executeDDL("drop table prov_name");
		executeDDL("drop table prov_livesin");
	}

	private static void executeDDL(String ddl) throws Exception {
		try {
			GatherStats4Query.runDDLQuery(con, ddl);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		}

	}

}

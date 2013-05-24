import java.sql.Connection;
import java.sql.SQLException;

public class RelWrapper {
	
	public static void createViews(Connection con) throws Exception {
		materializeProv(con, "drop view if exists prov_name", "CreateViewProv4Name");
		materializeProv(con, "drop view if exists prov_livesin", "CreateViewProv4Livesin");
	}

	public static void materializeProvs(Connection con) throws Exception {
		materializeProv(con, "drop table if exists prov_name", "MaterializeProv4Name");
		materializeProv(con, "drop table if exists prov_livesin", "MaterializeProv4Livesin");
	}

	private static void materializeProv(Connection con, String prev_query, String xmlName) throws Exception {
		try {
			GatherStats4Query.runDDLQuery(con, prev_query);
			String query = GatherStats4Query.getQuery(xmlName);
			GatherStats4Query.runDDLQuery(con, query);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		}

	}
	
	public static void cleanupViews(Connection con) throws Exception {
		executeDDL(con, "drop view prov_name");
		executeDDL(con, "drop view prov_livesin");
	}
	
	public static void cleanupTables(Connection con) throws Exception {
		executeDDL(con, "drop table prov_name");
		executeDDL(con, "drop table prov_livesin");
	}

	public static void executeDDL(Connection con, String ddl) throws Exception {
		try {
			GatherStats4Query.runDDLQuery(con, ddl);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		}

	}

}

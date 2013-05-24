import java.sql.Connection;
import java.sql.SQLException;

import org.vagabond.test.TestOptions;

public class GetSideeffects {

	private static Connection con;

	public static void main(String args[]) throws Exception {
		try {
			con = TestOptions.getInstance().getConnection();
			
			RelWrapper.createViews(con);
			generateView4CS();
			GatherStats4Query.gatherStats(con, "GetSideeffects");
			cleanupView4CS();		// csr depends on prov_name & prov_livesin
			RelWrapper.cleanupViews(con);
			
			RelWrapper.materializeProvs(con);
			generateView4CS();
			GatherStats4Query.gatherStats(con, "GetSideeffects");
			cleanupView4CS();
			RelWrapper.cleanupTables(con);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			con.close();
		}
		
	}
	
	private static void generateView4CS() throws Exception {
		try {
			RelWrapper.executeDDL(con, "drop view if exists csr");
			String query = GatherStats4Query.getQuery("CreateCSErrorsView");
			RelWrapper.executeDDL(con, query);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		}

	}
	
	private static void cleanupView4CS() throws Exception {
		RelWrapper.executeDDL(con, "drop view if exists csr");
	}


}

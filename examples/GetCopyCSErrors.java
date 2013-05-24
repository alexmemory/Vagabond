import java.sql.Connection;

import org.vagabond.test.TestOptions;

public class GetCopyCSErrors {
	
	private static Connection con;

	public static void main(String args[]) throws Exception {
		try {
			con = TestOptions.getInstance().getConnection();

			// Without materializing
			RelWrapper.createViews(con);
			GatherStats4Query.gatherStats(con, "GetCopyCSErrors");
			RelWrapper.cleanupViews(con);
			
			// Materializing
			RelWrapper.materializeProvs(con);
			GatherStats4Query.gatherStats(con, "GetCopyCSErrors");
			RelWrapper.cleanupTables(con);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			con.close();
		}
		
	}
	
}

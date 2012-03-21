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

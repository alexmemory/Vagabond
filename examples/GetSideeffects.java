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

public class GetSideeffects {

	public static void main(String args[]) throws Exception {
		generateView4CS();
		GatherStats4Query.gatherStats("GetSideeffects");

	}
	
	private static void generateView4CS() throws Exception {
		Connection con = TestOptions.getInstance().getConnection();
		
		try {
			GatherStats4Query.runDDLQuery(con, "drop view if exists csr");
			String query = GatherStats4Query.getQuery("CreateCSErrorsView");
			GatherStats4Query.runDDLQuery(con, query);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// con.close();
		}

	}


}

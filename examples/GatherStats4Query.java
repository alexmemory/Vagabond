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

public class GatherStats4Query {

	static {
		PropertyConfigurator.configure("resource/test/testLog4jproperties.txt");
	}
	
	public static void gatherStats(Connection con, String xmlQuery) throws Exception {

		// String numMarkers = args[0];
		int ROUNDS = 5;
		
		String query = getQuery(xmlQuery);
		ArrayList<Double> results = runQueryWithRounds(ROUNDS, con, query);

		// con.close();
		
		double finalAverage = getAverage(ROUNDS, results);
		double finalMedian = getMedian(ROUNDS, results);
		// System.out.println("Number of error markers: " + numMarkers);
		System.out.println("Running rounds: "+ ROUNDS);
		System.out.println("Average time spent: " + finalAverage + " ms");
		System.out.println("Median time spent: " + finalMedian + " ms");
	}

	public static String getQuery(String xmlQuery) throws FileNotFoundException,
			IOException {
		QueryHolder.getInstance().loadFromDir(new File ("examples"));
		String query = QueryHolder.getQuery(xmlQuery+".GetProv").parameterize();
		return query;
	}
	
	public static void runDDLQuery(Connection con, String query) throws SQLException, ClassNotFoundException {
		ConnectionManager.getInstance().execUpdate(con, query);
	}

	private static ArrayList<Double> runQueryWithRounds(int ROUNDS, 
			Connection con, String query) throws SQLException,
			ClassNotFoundException {
		ArrayList<Double> results = new ArrayList<Double>();
		Pattern pattern = Pattern.compile("Total runtime: (\\d+.\\d+) ms");
		ResultSet rs = null;
		String queryResults;
		for (int round=0; round<ROUNDS; round++) {
			rs = ConnectionManager.getInstance().execQuery(con,query);
			while (rs.next()) {
				queryResults = rs.getString(1);
				Matcher m = pattern.matcher(queryResults);
				if (m.find()) {
					double result = Double.valueOf(m.group(1));
					// System.out.println(result);
					results.add(result);
				}
			}
		}
		ConnectionManager.getInstance().closeRs(rs);
		return results;
	}

	private static double getAverage(int ROUNDS,
			ArrayList<Double> results) {
		double sumResult = 0.0;
		for (int round=0; round<ROUNDS; round++) {
			sumResult += results.get(round);
		}
		return sumResult/ROUNDS;
	}
	
	private static double getMedian(int ROUNDS,
			ArrayList<Double> results) {
		Collections.sort(results);
		return results.get((ROUNDS-1)/2);
	}

}

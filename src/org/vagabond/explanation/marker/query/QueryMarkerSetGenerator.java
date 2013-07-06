package org.vagabond.explanation.marker.query;

import static org.vagabond.util.LoggerUtil.logException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerSet;
import org.vagabond.explanation.marker.MarkerSummary;
import org.vagabond.test.TestOptions;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.ResultSetUtil;

public class QueryMarkerSetGenerator {

	static Logger log = Logger.getLogger(QueryMarkerSetGenerator.class);
	
	private static QueryMarkerSetGenerator instance;
	
	static {
		instance = new QueryMarkerSetGenerator();
	}
	
	public static QueryMarkerSetGenerator getInstance () {
		return instance;
	}
	
	public Connection getconnection (String name) {
		Connection con=null;
		String url;
		
		try {
			
			
			url = TestOptions.getInstance().getUrl();
			
			System.setProperty( 
					PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, 
					"org.postgresql.Driver");
	        System.setProperty( 
	        		PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, 
	        		url);
	        System.setProperty( 
	        		PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, 
	        		TestOptions.getInstance().getUser());
	        System.setProperty( 
	        		PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, 
	        		TestOptions.getInstance().getPassword());
			System.setProperty( 
					PropertiesBasedJdbcDatabaseTester.DBUNIT_SCHEMA, 
					TestOptions.getInstance().getSchemaName());
			
			con = TestOptions.getInstance().getConnection();
			
		}
		catch (Exception e) {
			System.err.println(e.toString());
			logException(e, log);
			System.exit(1);
		}
		return con;
	}

	public IMarkerSet genMSetFromQuery (String query) {
	 try {
		 ConnectionManager con = ConnectionManager.getInstance();
				 con.setConnection(getconnection("test"));
		ResultSet test=  con.execQuery(query);
		MarkerSet a = new MarkerSet();
		while(test.next())
		{
			//a.add(test.getInt(0), test.getInt(1),test.getInt(2));
			//String str0 = test.getString(0);
			
			int rel = 1;
			int attr = test.getInt(2);
			int tid = test.getInt(3);
			a.add(rel, attr, tid);
		};
		return a;
	
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		return null;
	}
	
}

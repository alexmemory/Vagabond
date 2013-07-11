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
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.test.TestOptions;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.ResultSetUtil;

public class QueryMarkerSetGenerator extends AbstractVagabondTest{

	static Logger log = Logger.getLogger(QueryMarkerSetGenerator.class);
	
	private static QueryMarkerSetGenerator instance;
	
	static {
		instance = new QueryMarkerSetGenerator();
	}
	
	public static QueryMarkerSetGenerator getInstance () {
		return instance;
	}
	
	

	public IMarkerSet genMSetFromQuery (String query) throws Exception {
	 try {
		 
		 ConnectionManager con = ConnectionManager.getInstance();
				 
		ResultSet test=  con.execQuery(query);
		MarkerSet a = new MarkerSet();
		while(test.next())
		{
			String rel = test.getString(1);
			String attr = test.getString(2);
			int tid = test.getInt(3);
			a.add(rel, attr, tid);
		};
		return a;
	} catch (SQLException e) {
		e.printStackTrace();
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	}
		return null;
	}
	
}

package org.tramp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * Singleton for conveniently creating database connections.
 * 
 * @author Boris Glavic
 *
 */
public class ConnectionManager {

	static Logger log = Logger.getLogger(ConnectionManager.class);
	
	private static ConnectionManager instance;
	
	private ConnectionManager () throws ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
	}
	
	public static ConnectionManager getInstance () throws ClassNotFoundException {
		if (instance == null) {
			instance = new ConnectionManager ();
		}
		
		return instance;
	}
	
	public Connection getConnection (String URL, String dbName, 
			String user, String password) throws SQLException {
		return DriverManager.getConnection(
				"jdbc:postgresql://" + URL + ":5432/" + dbName, user, password);
	}
	
}

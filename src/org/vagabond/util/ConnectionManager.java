package org.vagabond.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
	
	private Connection con;
	
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
		closeCon();
		con =  DriverManager.getConnection(
				"jdbc:postgresql://" + URL + ":5432/" + dbName, user, password);
		return con;
	}
	
	public Connection getConnection () {
		return con;
	}
	
	public void closeCon () throws SQLException {
		if (con != null)
			con.close();
	}
	
	public Statement getSt () throws SQLException {
		return con.createStatement();
	}
	
	public void closeRs (ResultSet rs) throws SQLException {
		rs.getStatement().close();
		rs.close();
	}
	
	public ResultSet execQuery (String query) throws SQLException {
		return execQuery(con, query);
	}
	
	public ResultSet execQuery (Connection userCon, String query) throws SQLException {
		ResultSet rs;
		Statement st;
		
		st = userCon.createStatement();
		
		rs = st.executeQuery(query);
		
		return rs;
	}
	
}

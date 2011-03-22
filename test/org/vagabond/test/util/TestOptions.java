package org.vagabond.test.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dbunit.database.AbstractDatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.vagabond.util.PropertyWrapper;
import static org.vagabond.util.LoggerUtil.*;

public class TestOptions extends AbstractDatabaseConnection {
	
	static Logger log = Logger.getLogger(TestOptions.class);
	
	private static TestOptions instance;
	
	private PropertyWrapper props;
	private IDatabaseConnection iCon;
	private Connection con;
	
	private TestOptions () throws FileNotFoundException, IOException, ClassNotFoundException {
		props = new PropertyWrapper(new File("resource/test/options.txt"), false);
		Class.forName("org.postgresql.Driver");
	}
	
	public static TestOptions getInstance() throws FileNotFoundException, IOException, ClassNotFoundException {
		if (instance == null)
			instance = new TestOptions();
		return instance;
	}
	
	public void close() throws SQLException {
		
	}

	public Connection getConnection() throws SQLException {
		try {
			if (con == null) {
				con = DriverManager.getConnection(getUrl(), getUser(), 
						getPassword());
			}
				
			return this.con;
		} 
		catch (Exception e) {
			logException(e, log);
			throw new SQLException (e.toString());
		}
	}

	public String getSchema() {
		return null;
	}
	
	public String getHost() {
		return props.getProperty("Host");
	}
	
	public String getDB () {
		return props.getProperty("DB");
	}
	
	public String getUser () {
		return props.getProperty("User");
	}
	
	public String getPassword () {
		return props.getProperty("Password");
	}
	
	public String getSchemaName () {
		return props.getProperty("Schema");
	}

	public String getPort() {
		return props.getProperty("Port");
	}
	
	public String getUrl() {
		return "jdbc:postgresql://" + this.getHost() 
		+ ":" + this.getPort() +  "/" 
		+ this.getDB();
	}
}
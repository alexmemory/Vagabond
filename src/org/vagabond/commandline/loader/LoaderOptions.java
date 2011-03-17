package org.vagabond.commandline.loader;

import java.io.File;

import org.kohsuke.args4j.Option;
import org.vagabond.xmlmodel.ConnectionInfoType;
import org.vagabond.xmlmodel.MappingScenarioDocument.MappingScenario;

public class LoaderOptions {

	@Option(name="-f", usage="xml mapping scenario document")
	private File xmlDoc;
	
	@Option(name="-u", usage="user name for connecting to the database")
	private String dbUser = "postgres";
	
	@Option(name="-p", usage="password for database user")
	private String dbPassword = "";
	
	@Option(name="-d", usage="name of the database to connect to")
	private String dbName = "tramptest";
	
	@Option(name="-h", usage="URL of the database to connect to")
	private String dbURL = "localhost";
	
	@Option(name="--no-data", usage="don't create instance data")
	private boolean noData = false;
	
	private boolean[] dbOptionsSet = { false, false, false, false };
	
	public LoaderOptions() {
		
	}
	
	public void setDBOptions (MappingScenario map) {
		ConnectionInfoType con = map.getConnectionInfo();
		
		if (con != null) {
			dbUser = con.getUser();
			dbPassword = con.getPassword();
			dbName = con.getDB();
			dbURL = con.getHost();
		}
	}

	public File getXmlDoc() {
		return xmlDoc;
	}

	public void setXmlDoc(File xmlDoc) {
		this.xmlDoc = xmlDoc;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbURL() {
		return dbURL;
	}

	public void setDbURL(String dbURL) {
		this.dbURL = dbURL;
	}

	public boolean isNoData() {
		return noData;
	}

	public void setNoData(boolean noData) {
		this.noData = noData;
	}
	
	
}

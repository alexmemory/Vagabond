package org.vagabond.commandline.explgen;

import java.io.File;

import org.kohsuke.args4j.Option;
import org.vagabond.xmlmodel.ConnectionInfoType;
import org.vagabond.xmlmodel.MappingScenarioDocument.MappingScenario;

public class ExplGenOptions {

	@Option(name="-u", usage="user name for connecting to the database")
	private String dbUser = "postgres";
	
	@Option(name="-p", usage="password for database user")
	private String dbPassword = "";
	
	@Option(name="-d", usage="name of the database to connect to")
	private String dbName = "tramptest";
	
	@Option(name="-h", usage="URL of the database to connect to")
	private String dbURL = "localhost";
	
	@Option(name="-x", usage="xml mapping scenario document")
	private File xmlDoc;
	
	@Option(name="-m", usage="File that stores markers")
	private File markerFile = null;
	
	@Option(name="-M", usage="List of error markers")
	private String markers;
	
	@Option(name="-loadScen", usage="Load the scenario to the database")
	private boolean loadScen = false;
	
	public ExplGenOptions () {
		
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

	public File getXmlDoc() {
		return xmlDoc;
	}

	public void setXmlDoc(File xmlDoc) {
		this.xmlDoc = xmlDoc;
	}

	public File getMarkerFile() {
		return markerFile;
	}

	public void setMarkerFile(File markerFile) {
		this.markerFile = markerFile;
	}

	public String getMarkers() {
		return markers;
	}

	public void setMarkers(String markers) {
		this.markers = markers;
	}

	public void setLoadScen(boolean loadScen) {
		this.loadScen = loadScen;
	}

	public boolean isLoadScen() {
		return loadScen;
	}
	
	
}

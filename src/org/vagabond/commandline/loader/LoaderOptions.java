package org.vagabond.commandline.loader;

import java.io.File;

import org.kohsuke.args4j.Option;
import org.vagabond.xmlmodel.ConnectionInfoType;
import org.vagabond.xmlmodel.MappingScenarioDocument.MappingScenario;

public class LoaderOptions {

	public enum OutputFormat {
		none,
		xml,
		map
	}
	
	@Option(name="-f", usage="xml mapping scenario document")
	private File xmlDoc;
	
	@Option(name="-u", usage="user name for connecting to the database")
	private String dbUser = "postgres";
	
	@Option(name="-p", usage="password for database user")
	private String dbPassword = "";

	@Option(name = "-P", usage = "port for database connection")
	private int port = 5432;
	
	@Option(name="-d", usage="name of the database to connect to")
	private String dbName = "tramptest";
	
	@Option(name="-h", usage="URL of the database to connect to")
	private String dbURL = "localhost";
	
	@Option(name="--no-data", usage="don't create instance data")
	private boolean noData = false;
	
	@Option(name="--validate-only", usage="only validate mapping XML file")
	private boolean onlyValidate = false;
	
	@Option(name="--boundranker", usage="choose generic boundary ranker")
	private boolean boundranker = false;
	
	@Option(name="--output", usage="output mapping in format (xml = Tramp XML mapping file, map = .map file)")
	private OutputFormat outForm = OutputFormat.none;
	
	@Option(name="-c", usage="data files (CSV) are load from this directory")
	private File csvLoadPath = null;
	
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

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isOnlyValidate() {
		return onlyValidate;
	}

	public void setOnlyValidate(boolean onlyValidate) {
		this.onlyValidate = onlyValidate;
	}
	
	public boolean isBoundaryRanker() {
		return boundranker;
	}

	public void setBoundaryRanker(boolean boundranker) {
		this.boundranker = boundranker;
	}

	public OutputFormat getOutForm() {
		return outForm;
	}

	public void setOutForm(OutputFormat outForm) {
		this.outForm = outForm;
	}

	public File getCsvLoadPath() {
		return csvLoadPath;
	}

	public void setCsvLoadPath(File csvLoadPath) {
		this.csvLoadPath = csvLoadPath;
	}
	
	
}

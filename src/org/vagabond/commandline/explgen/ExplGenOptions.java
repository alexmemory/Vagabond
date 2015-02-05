package org.vagabond.commandline.explgen;

import java.io.File;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;
import org.vagabond.explanation.ranking.RankerFactory;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.xmlmodel.ConnectionInfoType;
import org.vagabond.xmlmodel.MappingScenarioDocument.MappingScenario;

/**
 * @author lord_pretzel
 *
 */
public class ExplGenOptions {


	@Option(name = "-u", usage = "user name for connecting to the database")
	private String dbUser = "postgres";

	@Option(name = "-p", usage = "password for database user")
	private String dbPassword = "";

	@Option(name = "-P", usage = "port for database connection")
	private int port = 5432;
	
	@Option(name = "-d", usage = "name of the database to connect to")
	private String dbName = "tramptest";

	@Option(name = "-h", usage = "URL of the database to connect to")
	private String dbURL = "localhost";

	@Option(name = "-x", usage = "xml mapping scenario document")
	private File xmlDoc;

	@Option(name = "-m", usage = "File that stores markers")
	private File markerFile = null;

	@Option(name = "-M", usage = "List of error markers")
	private String markers = null;

	@Option(name = "-loadScen", usage = "Load the scenario to the database")
	private boolean loadScen = false;

	@Option(name = "-ranker", usage = "Select the type of ranker to use {}")
	private String rankerScheme = "Dummy";

	@Option(name = "-lazy", usage = "Use together with -loadScen. Check if " +
			"relations are already populated before loading data.")
	private boolean lazy = false;
	
	@Option(name = "-rankExpls", usage = "Rank the generated explanations")
	private boolean useRanker = false;

	@Option(name = "-funcweights", usage = "list of weight of scoring functions")
	private String funcweights = null;

	@Option(name = "-errweights", usage = "list of weight of error types")
	private String errweights = null;
	
	@Option(name = "-funcnames", usage = "list of scoring functions")
	private String funcnames = null;
	
	@Option(name = "-newfunc", usage = "1: WeightedCombined; 2: ErrorType; 3: ExplanationHomogenity;")
	private int newfunc= 0;

	@Option(name = "-boundranker", usage = "1:Use boundary ranker; 0: default ranker")
	private int boundranker = 0;
	
	@Option(name = "-rankSkyline", 
			usage = "Use Skyline ranker with this ranking schemes", 
			metaVar = "[scheme 1] [scheme 2] ...")
	private String[] skylineRankers = null;
		
	public ExplGenOptions() {
		CmdLineParser.registerHandler(String[].class, StringArrayOptionHandler.class);
	}

	public void setDBOptions(MappingScenario map) {
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

	public void setScoreFuncWeights(String scorefuncweights){
		this.funcweights = scorefuncweights;
	}

	public void setScoreFunctions(String scorefuncnames){
		this.funcnames = scorefuncnames;
	}
	
	public void setErrWeights(String errorweights){
		this.errweights = errorweights;
	}
	
	public boolean isLoadScen() {
		return loadScen;
	}

	public String getRankerScheme() {
			rankerScheme = RankerFactory.RankerSchemeConstructor(this.boundranker, this.newfunc, this.getScoreFuncNames(), this.getScoreFuncWeights(), this.getErrorWeights());
			
        return rankerScheme;
	}

	public void setRankerScheme(String rankerScheme) throws Exception {
		if (RankerFactory.getRankerSchemes().contains(rankerScheme))
			this.rankerScheme = rankerScheme;
		throw new Exception("Unknown ranker scheme <" + rankerScheme
				+ "> expected one of " + RankerFactory.getRankerSchemes());
	}

	public boolean isLazy() {
		return lazy;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}

	public boolean isUseRanker() {
		return useRanker;
	}

	public void setUseRanker(boolean useRanker) {
		this.useRanker = useRanker;
	}

	public String[] getSkylineRankers() {
		return skylineRankers;
	}
	
	public int useBoundRanker()
	{
		return boundranker;
	}
	
	public void setBoundRanker(int boundranker)
	{
		this.boundranker = boundranker;
	}
	
	
	public String[] getScoreFuncNames() {
		String[] mFuncNames;
		if (funcnames == null)
			return null;
		mFuncNames = funcnames.split(",");
		return mFuncNames;
	}

	public double[] getScoreFuncWeights() {
		if (funcweights == null)
			return null;
		double[] mFuncWeights = new double[funcweights.split(",").length];
		for (int i = 0; i < mFuncWeights.length; i++)
		{
			mFuncWeights[i] = Double.parseDouble(funcweights.split(",")[i]);	
		}
		return mFuncWeights;
	}

	public double[] getErrorWeights() {
		if (funcweights == null)
			return null;
		double[] mErrWeights = new double[funcweights.split(",").length];
		for (int i = 0; i < mErrWeights.length; i++)
		{
			mErrWeights[i] = Double.parseDouble(funcweights.split(",")[i]);	
		}
		return mErrWeights;
	}
	
	public void setSkylineRankers(String[] skylineRankers) {
		this.skylineRankers = skylineRankers;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	

}

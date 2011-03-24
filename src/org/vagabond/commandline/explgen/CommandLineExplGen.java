package org.vagabond.commandline.explgen;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.XmlException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.vagabond.explanation.marker.SchemaResolver;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LoggerUtil;

public class CommandLineExplGen {

	static Logger log = Logger.getLogger(CommandLineExplGen.class);
	
	private ExplGenOptions options;
	
	public CommandLineExplGen () {
		options = new ExplGenOptions();
	}
	
	public void setUpLogger () {
		PropertyConfigurator.configure("resource/log4jproperties.txt");
	}
	
	public void parseOptionsAndLoadScenario (String[] args) 
			throws Exception {
		parseOptions(args);
		
		if (options.getXmlDoc() == null)
			throw new CmdLineException("no mapping scenario XML document " +
					"given (-f option)");
		loadScenario (options.getXmlDoc());
		parseOptions(args);
		
		ConnectionManager.getInstance().getConnection(
				options.getDbURL(), options.getDbName(), options.getDbUser(),
				options.getDbPassword());
		
		if (options.isLoadScen())
			loadScenarioOnDB();
		
		loadMarkers();
		
		createExpls();
	}
	
	private void createExpls() {
		// TODO Auto-generated method stub
		
	}

	private void loadMarkers() {
		
		
	}

	private void loadScenarioOnDB() throws Exception {
		DatabaseScenarioLoader.getInstance().loadScenario(
				ConnectionManager.getInstance().getConnection());
	}

	private void loadScenario (File xmlDoc) 
			throws Exception {
		ModelLoader.getInstance().loadToInst(xmlDoc);
		SchemaResolver.getInstance().setSchemas();
		options.setDBOptions(MapScenarioHolder.getInstance().getScenario());
	}
	
	private void parseOptions (String[] args) throws CmdLineException {
		CmdLineParser parser;
		
		log.debug("Command line args are: <" + LoggerUtil.arrayToString(args) + ">");
		parser = new CmdLineParser(options);
		parser.parseArgument(args);
	}
		
	public void printUsage (PrintStream out) {
		CmdLineParser parser;
		
		parser = new CmdLineParser(options);
		parser.printUsage(out);
	}
	
	public boolean execute (String[] args) {
		try {
			setUpLogger();
		} catch (Exception e) {			
			e.printStackTrace();
			return false;
		}
		
		try {
			parseOptionsAndLoadScenario(args);
			//TODO
		} catch (CmdLineException e) {			
			LoggerUtil.logException(e, log);
			printUsage(System.err);
			return false;
		} catch (Exception e) {			
			LoggerUtil.logException(e, log);
			return false;
		}

		return true;
	}
	
	public static void main (String[] args) {
		CommandLineExplGen inst = new CommandLineExplGen();
		
		if (!inst.execute(args)) {
			System.exit(1);
		}
		System.exit(0);
	}
	
}

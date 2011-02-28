package org.tramp.commandline.loader;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.XmlException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.tramp.expl.model.MapScenarioHolder;
import org.tramp.expl.model.ModelLoader;
import org.tramp.expl.model.ValidationException;
import org.tramp.expl.scenarioToDB.DatabaseScenarioLoader;
import org.tramp.util.ConnectionManager;
import org.tramp.util.LoggerUtil;

public class CommandLineLoader {

	static Logger log = Logger.getLogger(CommandLineLoader.class);
	
	private LoaderOptions options;
	private MapScenarioHolder map;
	
	public CommandLineLoader () {
		options = new LoaderOptions ();
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
	}
	
	private void loadScenario (File xmlDoc) 
			throws XmlException, IOException, ValidationException {
		map = ModelLoader.getInstance().load(xmlDoc);
		options.setDBOptions(map.getScenario());
	}
	
	private void parseOptions (String[] args) throws CmdLineException {
		CmdLineParser parser;
		
		log.debug("Command line args are: <" + LoggerUtil.arrayToString(args) + ">");
		parser = new CmdLineParser(options);
		parser.parseArgument(args);
	}
	
	public void executeOnDB () throws SQLException, ClassNotFoundException {
		Connection dbCon;
		
		dbCon = ConnectionManager.getInstance().getConnection(
				options.getDbURL(), options.getDbName(), options.getDbUser(),
				options.getDbPassword());
		if (options.isNoData())
			DatabaseScenarioLoader.getInstance().loadScenarioNoData(dbCon, map);
		else
			DatabaseScenarioLoader.getInstance().loadScenario(dbCon, map);
		dbCon.close();
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
			executeOnDB();
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
		CommandLineLoader inst = new CommandLineLoader();
		
		if (!inst.execute(args)) {
			System.exit(1);
		}
		System.exit(0);
	}
	
}

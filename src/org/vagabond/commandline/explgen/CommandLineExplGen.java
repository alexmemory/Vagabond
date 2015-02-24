package org.vagabond.commandline.explgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.vagabond.explanation.generation.ExplanationSetGenerator;
import org.vagabond.explanation.generation.PartitionExplanationGenerator;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.explanation.model.ExplPartition;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.ranking.IExplanationRanker;
import org.vagabond.explanation.ranking.IPartitionRanker;
import org.vagabond.explanation.ranking.RankerFactory;
import org.vagabond.explanation.ranking.SkylineRanker;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader.LoadMode;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LoggerUtil;

public class CommandLineExplGen {

	static Logger log = Logger.getLogger(CommandLineExplGen.class);

	private ExplGenOptions options;
	private IMarkerSet markers;
	private ExplanationSetGenerator gen;

	public CommandLineExplGen() {
		options = new ExplGenOptions();
		gen = new ExplanationSetGenerator();
	}
	 	
	public void setUpLogger() {
		PropertyConfigurator.configure("resource/log4jproperties.txt");
	}

	public void parseOptionsAndLoadScenario(String[] args) throws Exception {
		parseOptions(args);

		if (options.getXmlDoc() == null)
			throw new Exception("no mapping scenario XML document "
					+ "given (-f option)");
		loadScenario(options.getXmlDoc());
		parseOptions(args);

		// setup DB connection
		ConnectionManager.getInstance().getConnection(options.getDbURL(),
				options.getDbName(), options.getDbUser(),
				options.getDbPassword(), options.getPort());

		if (options.isLoadScen())
			loadScenarioOnDB();
		ScenarioDictionary.getInstance().initFromScenario();
	}

	private void createExpls(PrintStream out) throws Exception {
		
		if (options.isUseRanker()) {
			Iterator<IExplanationSet> iter = null;

			// No Partitioning
			if (options.noUsePart()) {

				ExplanationSetGenerator noPartGen =	new ExplanationSetGenerator();			
				ExplanationCollection col2 = noPartGen.findExplanations(markers);
							
				if (options.getRankerScheme() != null){
					IExplanationRanker rank;

					if (log.isDebugEnabled()) {
						log.debug("Create ranker for scheme without partitioning " + options.getRankerScheme());
					};
					
					rank = RankerFactory.createInitializedRanker(options.getRankerScheme(), col2);
					iter = rank;
					
				}
			}
			else {
				
				PartitionExplanationGenerator partGen =	new PartitionExplanationGenerator();
				partGen.init();
	
				ExplPartition p = partGen.findExplanations(markers);
	
				if (options.getSkylineRankers() != null) {
					SkylineRanker rank;
					if (log.isDebugEnabled()) {log.debug("Create skyline ranker for scheme "
							+ Arrays.toString(options.getSkylineRankers()));};
					rank = RankerFactory.createSkylineRanker(
									options.getSkylineRankers(),
									options.getRankerScheme(), p);
					iter = rank;
				}
				else {
					IPartitionRanker rank;
	
					if (log.isDebugEnabled()) {log.debug("Create ranker for scheme "
							+ options.getRankerScheme());};
					rank = RankerFactory.createPartRanker(
									options.getRankerScheme(), p);
					iter = rank;
				}
			}
			
			boolean cont = true;
			int r = 0;
			BufferedReader in =
					new BufferedReader(new InputStreamReader(System.in));
			// use non-interactive ranking where we produced the top maxRank CES (or all if maxRank is -1)
			if (options.isRankNonInteractive()) {
				int i = 1;
				int max = options.getMaxRank();
				long beforeRank = System.nanoTime();
				while (iter.hasNext() && (max == -1 || i <= max)) {
					long lStartTime = System.nanoTime();
					
					IExplanationSet set = iter.next();
					
					if (!options.isNoShowSets()) {
						System.out.println("\n\n*********************************\n*" +
								"\t\t RANKED " 
								+ ++r 
								+ "\n*********************************\n");
						System.out.println(set.toString());
					}
										
					long lEndTime = System.nanoTime();
					long difference= (lEndTime - lStartTime);
					double secs = ((double) difference) / 1000000000.0;
					
					System.out.println(String.format("%d: %.8f secs", i, secs));
					i++;
				}
				long afterRank = System.nanoTime();
				double rankSecs = ((double) (afterRank - beforeRank)) / 1000000000.0;
				System.out.println(String.format("Ranking(%d): %.8f secs", i-1, rankSecs));
			}
			// use interactive ranking where the user is asked after each CES whether to continue or not
			else {
				while (cont && iter.hasNext()) {
					String read;
					IExplanationSet set = iter.next();
					
					if (!options.isNoShowSets()) {
						System.out.println("\n\n*********************************\n*" +
								"\t\t RANKED " 
								+ ++r 
								+ "\n*********************************\n");
						System.out.println(set.toString());
						System.out.println("\nContinue [y/n]?");
					}
					
					while (!in.ready())
						Thread.sleep(100);
					read = in.readLine().trim();
					
					if (log.isDebugEnabled()) {log.debug("user pressed " + read);};
					cont = !read.trim().startsWith("n");
				}
			}
		}
		else {
			ExplanationCollection col;

			col = gen.findExplanations(markers);
			out.println(col);
		}
	}

	private IMarkerSet loadMarkers() throws Exception {
		if (options.getMarkers() != null)
			markers = MarkerParser.getInstance().parseSet(options.getMarkers());
		else if (options.getMarkerFile() != null)
			markers =
					MarkerParser.getInstance().parseMarkers(
							new FileInputStream(options.getMarkerFile()));
		else
			throw new Exception("either marker file (-m) or markers (-M) have"
					+ "to be specified");

		if (log.isDebugEnabled()) {log.debug("Markers are :<" + markers + ">");};

		return markers;
	}

	private void loadScenarioOnDB() throws Exception {
		if (options.isLazy())
			DatabaseScenarioLoader.getInstance().setOperationalMode(
					LoadMode.Lazy);
		DatabaseScenarioLoader.getInstance().loadScenario(
				ConnectionManager.getInstance().getConnection());
	}

	private void loadScenario(File xmlDoc) throws Exception {
		ModelLoader.getInstance().loadToInst(xmlDoc);
		QueryHolder.getInstance().loadFromDir(new File("resource/queries"));
		options.setDBOptions(MapScenarioHolder.getInstance().getScenario());
	}

	private void parseOptions(String[] args) throws CmdLineException {
		CmdLineParser parser;

		if (log.isDebugEnabled()) {log.debug("Command line args are: <" + LoggerUtil.arrayToString(args)
				+ ">");};
		parser = new CmdLineParser(options);
		parser.parseArgument(args);
	}

	public void printUsage(PrintStream out) {
		CmdLineParser parser;

		parser = new CmdLineParser(options);
		parser.printUsage(out);
	}

	public boolean execute(String[] args) {
		try {
			setUpLogger();
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		try {
			parseOptionsAndLoadScenario(args);
			loadMarkers();
			createExpls(System.out);
		}
		catch (CmdLineException e) {
			LoggerUtil.logException(e, log);
			printUsage(System.err);
			return false;
		}
		catch (Throwable e) {
			LoggerUtil.logException(e, log);
			return false;
		}

		return true;
	}

	public static void main(String[] args) {
		CommandLineExplGen inst = new CommandLineExplGen();
		
		for (int i = 0; i < 1; i++) {
			
			long lStartTime = new Date().getTime();
		
			if (!inst.execute(args))
				System.exit(1);

			long lEndTime = new Date().getTime();
			long difference= (lEndTime - lStartTime);
			double secs = ((double) difference) / 1000.0;
			System.out.printf("Total: %.2f secs\n", secs);
		}
		
		System.exit(0);
		 
	}
}

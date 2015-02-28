package org.vagabond.commandline.explgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
import org.vagabond.explanation.metrics.RankingMetricPrecisionRecall;
import org.vagabond.explanation.model.ExplPartition;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.IBasicExplanation;
import org.vagabond.explanation.ranking.IExplanationRanker;
import org.vagabond.explanation.ranking.IPartitionRanker;
import org.vagabond.explanation.ranking.RankerFactory;
import org.vagabond.explanation.ranking.SkylineRanker;
import org.vagabond.explanation.ranking.scoring.IScoringFunction;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader.LoadMode;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.xmlbeans.ExplanationAndErrorXMLLoader;

public class CommandLineExplGen {

	static Logger log = Logger.getLogger(CommandLineExplGen.class);

	private ExplGenOptions options;
	private IMarkerSet markers;
	private ExplanationSetGenerator gen;
	
	static String rankSecs = "";
	static String secsRank = "";
	
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
			IExplanationRanker explRank = null;
			SkylineRanker skyRank  = null;
			IPartitionRanker partRank = null;
			IScoringFunction f = null;
			
			// No Partitioning
			if (options.noUsePart()) {
				ExplanationSetGenerator noPartGen =	new ExplanationSetGenerator();			
				ExplanationCollection col2;
				{
					long lStartTime = System.nanoTime();
				
					col2 = noPartGen.findExplanations(markers);
	
					long lEndTime = System.nanoTime();
					long difference= (lEndTime - lStartTime);
					double secs = ((double) difference) / 1000000000.0;
					System.out.printf("ExplGen: %.2f secs\n", secs);
				}
	
				
							
				if (options.getRankerScheme() != null){

					if (log.isDebugEnabled()) {
						log.debug("Create ranker for scheme without partitioning " + options.getRankerScheme());
					};
					
					explRank = RankerFactory.createInitializedRanker(options.getRankerScheme(), col2);
					iter = explRank;
					f = explRank.getScoreF();
				}
			}
			else {		
				PartitionExplanationGenerator partGen =	new PartitionExplanationGenerator();
				partGen.init();
				ExplPartition p;
				
				{
					long lStartTime = System.nanoTime();
				
					p = partGen.findExplanations(markers);
	
					long lEndTime = System.nanoTime();
					long difference= (lEndTime - lStartTime);
					double secs = ((double) difference) / 1000000000.0;
					System.out.printf("ExplGen: %.2f secs\n", secs);
				}
				
				if (options.getSkylineRankers() != null) {
					if (log.isDebugEnabled()) {log.debug("Create skyline ranker for scheme "
							+ Arrays.toString(options.getSkylineRankers()));};
					skyRank = RankerFactory.createSkylineRanker(
									options.getSkylineRankers(),
									options.getRankerScheme(), p);
					iter = skyRank;
				}
				else {
					if (log.isDebugEnabled()) {log.debug("Create ranker for scheme "
							+ options.getRankerScheme());};
					partRank = RankerFactory.createPartRanker(
									options.getRankerScheme(), p);
					iter = partRank;
					f = partRank.getScoreF();
				}
			}
			
			boolean cont = true;
			int r = 0;
			
			// if a gold standard is given then we just compute precision and recall metrics
			if (options.getGoldStandard() != null) {
				int max = options.getMaxRank();
				IExplanationSet pre = ExplanationAndErrorXMLLoader.getInstance().loadExplanations(options.getGoldStandard());
				IExplanationSet gold = ExplanationFactory.newExplanationSet();
				RankingMetricPrecisionRecall metric;
				double prec = 0.0, rec = 0.0;
				max = (max == -1) ? Integer.MAX_VALUE : max;
				int real = 0;
				// have to copy the set because computing real target SE changes the hash
				for(IBasicExplanation e: pre) {
					e.computeRealTargetSEAndExplains(markers);
					gold.add(e);
				}
				
				metric = new RankingMetricPrecisionRecall(gold);
				
				// gather solutions
				while(iter.hasNext() && real < max) {
					iter.next();
					real++;
				}
				
				for(int i = 0; i < real; i++) {
					IExplanationSet set;
					double score = 0.0;
					
					if (options.noUsePart()) {
						prec = metric.computePrecision(explRank, i);
						rec = metric.computeRecall(explRank, i);
						set = explRank.getRankedExpl(i);
						score = explRank.getScoreF().getScore(set);
					}
					else {
						prec = metric.computePrecision(partRank, i);
						rec = metric.computeRecall(partRank, i);
						set = partRank.getRankedExpl(i);
						score = partRank.getScoreF().getScore(set);
					}				
					
					if (!options.isNoShowSets()) {
						System.out.println("\n\n*********************************\n*" +
								"\t\t RANKED " 
								+ ++r + " with score " + score
								+ "\n*********************************\n");
						System.out.println(set.toString());
					}
					
					System.out.printf("top: %d, prec: %f, rec: %f\n", i+1, prec, rec);
					System.out.flush();
				}
			}
			// use non-interactive ranking where we produced the top maxRank CES (or all if maxRank is -1)
			else if (options.isRankNonInteractive()) {
				int i = 1;
				int max = options.getMaxRank();
				long beforeRank = System.nanoTime();
				//only 10mins running for ranking
				long start = System.currentTimeMillis();
				long end = options.getTimeLimit() == -1 ? -1 :  start + options.getTimeLimit()*1000;
				
				while ((max == -1 || i <= max) && (end < 0 || System.currentTimeMillis() < end)) {
					long lStartTime = System.nanoTime();				
					IExplanationSet set = iter.next();
					double score = -1.0;

					// do check inside timing, because ranking cost may be hidden in this check
					if (!iter.hasNext())
						break;
					
					if (f != null)
						score = f.getScore(set);
					
					if (!options.isNoShowSets()) {
						System.out.println("\n\n*********************************\n*" +
								"\t\t RANKED " 
								+ ++r + " with score " + score
								+ "\n*********************************\n");
						System.out.println(set.toString());
					}
										
					long lEndTime = System.nanoTime();
					long difference= (lEndTime - lStartTime);
					double secs = ((double) difference) / 1000000000.0;
					
					System.out.println(String.format("%d: %.8f secs", i, secs));
					System.out.flush();
					i++;
				}
				long afterRank = System.nanoTime();
				double rankSecs1 = ((double) (afterRank - beforeRank)) / 1000000000.0;
				System.out.println(String.format("Ranking(%d): %.8f secs", i-1, rankSecs1));
			}
			// use interactive ranking where the user is asked after each CES whether to continue or not
			else {
				BufferedReader in =
						new BufferedReader(new InputStreamReader(System.in));
				while (cont && iter.hasNext()) {
					String read;
					IExplanationSet set = iter.next();
					double score = -1.0;
					if (f != null)
						score = f.getScore(set);
					
					if (!options.isNoShowSets()) {
						System.out.println("\n\n*********************************\n*" +
								"\t\t RANKED " 
								+ ++r + " with score " + score
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
			if (!options.isNoShowSets())
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
				ConnectionManager.getInstance().getConnection(), 
				options.getCsvLoadPath());
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

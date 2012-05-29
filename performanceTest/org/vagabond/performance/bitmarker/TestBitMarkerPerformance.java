package org.vagabond.performance.bitmarker;

import java.sql.Connection;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.vagabond.explanation.marker.BitMarkerSet;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.test.TestOptions;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.GlobalResetter;

public class TestBitMarkerPerformance {
	static Logger log = Logger.getLogger(TestBitMarkerPerformance.class);
	
	private static IAttributeValueMarker attr;
	
	public static void main (String[] args) throws Exception {
		PropertyConfigurator.configure("resource/test/perfLog4jproperties.txt");
		//loadToDB("resource/exampleScenarios/homeless.xml");
		loadToDB("resource/exampleScenarios/performanceTest1.xml");
		IMarkerSet set1 = MarkerFactory.newBitMarkerSet();
		IMarkerSet set2 = MarkerFactory.newMarkerSet();
		System.out.println("------BitSet Adding Test------");
		AddingTest(set1);
		System.out.println("------BitSet Adding Test------");
		AddingTest(set2);
		
		
		
		
	}

	public static void  AddingTest(IMarkerSet set1) throws Exception{
		Random number = new Random();
		int maxRelid = 3, maxAttr = 3, maxTid = 99999;
		
		long before = System.currentTimeMillis();
		for(int i = 0; i< 10; i++){
			set1.add(randMarker(maxRelid, maxTid, maxAttr, number));
		}
		
		long breakpoint1 = System.currentTimeMillis();
		for(int i = 0; i< 100; i++){
			set1.add(randMarker(maxRelid, maxTid, maxAttr, number));
		}
		long breakpoint2 = System.currentTimeMillis();
		for(int i = 0; i< 890; i++){
			set1.add(randMarker(maxRelid, maxTid, maxAttr, number));
		}
		long end = System.currentTimeMillis();
		log.debug("Number of element added " + set1.getNumElem() );
		log.debug("Set Adding 10 elements time: " + (breakpoint1 - before));
		log.debug("Set Adding 100 elements time: " + (breakpoint2 - breakpoint1));
		log.debug("Set Adding 1000 elements time: " + (end - before));
		
	}
	
	private static IAttributeValueMarker randMarker (int maxRel, int maxTid, int maxAttr, Random number) throws Exception {
		int relid;
		int attrid;
		int tid;
		String tidString;
		
		relid = number.nextInt(3);
		tid = number.nextInt(99999);
		attrid = number.nextInt(3);
		tidString = ScenarioDictionary.getInstance().getTidString(tid, relid);
		return  MarkerFactory.newAttrMarker(relid,tidString,attrid);
	}

	public static void loadToDB (String fileName) throws Exception {
		Connection con = TestOptions.getInstance().getConnection();
		
		GlobalResetter.getInstance().reset();
		ModelLoader.getInstance().loadToInst(fileName);
		DatabaseScenarioLoader.getInstance().loadScenario(con);
		ConnectionManager.getInstance().setConnection(con);
		ScenarioDictionary.getInstance().initFromScenario();
	}

	
}
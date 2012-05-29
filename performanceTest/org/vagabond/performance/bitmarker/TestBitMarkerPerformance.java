package org.vagabond.performance.bitmarker;

import java.io.File;
import java.sql.Connection;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.marker.BitMarkerSet;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader.LoadMode;
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
		IMarkerSet bitset1 = MarkerFactory.newBitMarkerSet();
		IMarkerSet markerset1 = MarkerFactory.newMarkerSet();
		
		System.out.println("------BitSet Adding Test------");
		AddingTest(bitset1);
		System.out.println("------MarkerSet Adding Test------");
		AddingTest(markerset1);
		
		System.out.println("------BitSet Containing Test------");
		ContainingTest(bitset1);
		System.out.println("------MarkerSet Containing Test------");
		ContainingTest(markerset1);
		
		System.out.println("------Union Test------");
		UnionTest();
		System.out.println("------Intersect Test------");
		IntersectTest();
		System.out.println("------Difference Test------");
		DiffTest();
		System.out.println("------Clone Test------");
		CloneTest();
		
		
	}
	
	
	public static void  CloneTest() throws Exception{
		CloneTestSets(10, 200);
		CloneTestSets(100, 200);
		CloneTestSets(1000, 200);
	}
	
	
	public static void  UnionTest() throws Exception{
		unionSets(10, 200);
		unionSets(100, 200);
		unionSets(1000, 200);
	}
	
	public static void  IntersectTest() throws Exception{
		IntersectSets(10, 200);
		IntersectSets(100, 200);
		IntersectSets(1000, 200);
	}
	
	public static void  DiffTest() throws Exception{
		DiffSets(10, 200);
		DiffSets(100, 200);
		DiffSets(1000, 200);
	}
	
	
	public static void CloneTestSets (int size, int numSets) throws Exception {
		Random number = new Random();
		int maxRel = 3, maxAttr = 3, maxTid = 99999;
		IMarkerSet[] sets;
		IMarkerSet[] bitSets;
		
		sets = genSets(false, numSets, size, maxRel, maxTid, maxAttr, number);
		bitSets = genSets(true, numSets, size, maxRel, maxTid, maxAttr, number);
		
		long test1start = System.currentTimeMillis();
		for(int i = 0; i < sets.length; i+=2)
			sets[i] = sets[i+1].cloneSet();
		long test1end = System.currentTimeMillis();
		
		long test2start = System.currentTimeMillis();
		for(int i = 0; i < bitSets.length; i+=2)
			bitSets[i] = bitSets[i+1].cloneSet();
		long test2end = System.currentTimeMillis();
		
		log.debug("------ SIZE " + size + " ------");
		log.debug("MARKER: Cloning Between " + numSets + " Sets of " + size + " elements each time: " + (test1end - test1start));
		log.debug("BITMARKER: Cloning Between " + numSets + " Sets of " + size + " elements each time: " + (test2end - test2start));
	}
	
	public static void DiffSets (int size, int numSets) throws Exception {
		Random number = new Random();
		int maxRel = 3, maxAttr = 3, maxTid = 99999;
		IMarkerSet[] sets;
		IMarkerSet[] bitSets;
		
		sets = genSets(false, numSets, size, maxRel, maxTid, maxAttr, number);
		bitSets = genSets(true, numSets, size, maxRel, maxTid, maxAttr, number);
		
		long test1start = System.currentTimeMillis();
		for(int i = 0; i < sets.length; i+=2)
			sets[i].diff(sets[i+1]);
		long test1end = System.currentTimeMillis();
		
		long test2start = System.currentTimeMillis();
		for(int i = 0; i < bitSets.length; i+=2)
			bitSets[i].diff(bitSets[i+1]);
		long test2end = System.currentTimeMillis();
		
		log.debug("------ SIZE " + size + " ------");
		log.debug("MARKER: Difference Between " + numSets + " Sets of " + size + " elements each time: " + (test1end - test1start));
		log.debug("BITMARKER: Difference Between " + numSets + " Sets of " + size + " elements each time: " + (test2end - test2start));
	}
	
	public static void IntersectSets (int size, int numSets) throws Exception {
		Random number = new Random();
		int maxRel = 3, maxAttr = 3, maxTid = 99999;
		IMarkerSet[] sets;
		IMarkerSet[] bitSets;
		
		sets = genSets(false, numSets, size, maxRel, maxTid, maxAttr, number);
		bitSets = genSets(true, numSets, size, maxRel, maxTid, maxAttr, number);
		
		long test1start = System.currentTimeMillis();
		for(int i = 0; i < sets.length; i+=2)
			sets[i].intersect(sets[i+1]);
		long test1end = System.currentTimeMillis();
		
		long test2start = System.currentTimeMillis();
		for(int i = 0; i < bitSets.length; i+=2)
			bitSets[i].intersect(bitSets[i+1]);
		long test2end = System.currentTimeMillis();
		
		log.debug("------ SIZE " + size + " ------");
		log.debug("MARKER: Intersect Between " + numSets + " Sets of " + size + " elements each time: " + (test1end - test1start));
		log.debug("BITMARKER: Intersect Between " + numSets + " Sets of " + size + " elements each time: " + (test2end - test2start));
	}
	
	
	
	public static void unionSets (int size, int numSets) throws Exception {
		Random number = new Random();
		int maxRel = 3, maxAttr = 3, maxTid = 99999;
		IMarkerSet[] sets;
		IMarkerSet[] bitSets;
		
		sets = genSets(false, numSets, size, maxRel, maxTid, maxAttr, number);
		bitSets = genSets(true, numSets, size, maxRel, maxTid, maxAttr, number);
		
		long test1start = System.currentTimeMillis();
		for(int i = 0; i < sets.length; i+=2)
			sets[i].union(sets[i+1]);
		long test1end = System.currentTimeMillis();
		
		long test2start = System.currentTimeMillis();
		for(int i = 0; i < bitSets.length; i+=2)
			bitSets[i].union(bitSets[i+1]);
		long test2end = System.currentTimeMillis();
		
		log.debug("------ SIZE " + size + " ------");
		log.debug("MARKER: Union Between " + numSets + " Sets of " + size + " elements each time: " + (test1end - test1start));
		log.debug("BITMARKER: Union Between " + numSets + " Sets of " + size + " elements each time: " + (test2end - test2start));
	}
	
	
	public static IMarkerSet[] genSets (boolean bit, int numSets, int card, int maxRel, int maxTid, int maxAttr, Random number) throws Exception {
		IMarkerSet[] result = new IMarkerSet[numSets];
		
		for(int i = 0; i < numSets; i++) {
			if (bit)
				result[i] = MarkerFactory.newBitMarkerSet();
			else
				result[i] = MarkerFactory.newMarkerSet();

			setToElement(result[i], card, maxRel, maxTid, maxAttr, number);
		}
		
		return result;
	}
	
	public static void setToElement(IMarkerSet set1, int setNumber, int maxRel, int maxTid, int maxAttr, Random number) throws Exception{
		while(set1.getNumElem() < setNumber)
			set1.add(randMarker(maxRel, maxTid, maxAttr, number));
	}
	
	
	
	public static void  ContainingTest(IMarkerSet set1) throws Exception{
		Random number = new Random();
		int maxRelid = 3, maxAttr = 3, maxTid = 99999;
		
		long before = System.currentTimeMillis();
		for(int i = 0; i< 10; i++){
			set1.contains(randMarker(maxRelid, maxTid, maxAttr, number));
		}
		
		long breakpoint1 = System.currentTimeMillis();
		for(int i = 0; i< 100; i++){
			set1.contains(randMarker(maxRelid, maxTid, maxAttr, number));
		}
		long breakpoint2 = System.currentTimeMillis();
		for(int i = 0; i< 890; i++){
			set1.contains(randMarker(maxRelid, maxTid, maxAttr, number));
		}
		long end = System.currentTimeMillis();
		log.debug("Set Adding 10 elements time: " + (breakpoint1 - before));
		log.debug("Set Adding 100 elements time: " + (breakpoint2 - breakpoint1));
		log.debug("Set Adding 1000 elements time: " + (end - before));
		
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
		QueryHolder.getInstance().loadFromDir(new File("resource/queries"));
		ModelLoader.getInstance().loadToInst(fileName);
		DatabaseScenarioLoader.getInstance().setOperationalMode(LoadMode.Lazy);
		DatabaseScenarioLoader.getInstance().loadScenario(con);
		ConnectionManager.getInstance().setConnection(con);
		ScenarioDictionary.getInstance().initFromScenario();
	}

	
}
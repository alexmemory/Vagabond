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
	
	public enum SetOps {
		Union,
		Intersect,
		Diff,
		Clone
	}
	
	private static IAttributeValueMarker attr;

	private static final int NUM_SETS = 5;
	
	private static IMarkerSet[][][] testsets;
	private static int[][] sizesForSetTests = new int[][] {{10,500}, {100,500}, {1000,500}, {10000,500}};
	
	public static void main (String[] args) throws Exception {
		PropertyConfigurator.configure("resource/test/perfLog4jproperties.txt");
		//loadToDB("resource/exampleScenarios/homeless.xml");
		loadToDB("resource/exampleScenarios/performanceTest1.xml");
		genTestSets();
		
		IMarkerSet markerset1 = MarkerFactory.newMarkerSet();
		IMarkerSet bitset1 = MarkerFactory.newMarkerSet();
		System.out.println("------MarkerSet Adding Test------");
		AddingTest(markerset1);
		System.out.println("------BitSet Adding Test------");
		AddingTest(bitset1);

		System.out.println("------MarkerSet Containing Test------");
		ContainingTest(false);
		System.out.println("------BitSet Containing Test------");
		ContainingTest(true);
		
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
		for(int i = 0; i < sizesForSetTests.length; i++)
			execSetOp(SetOps.Clone, sizesForSetTests[i][0], sizesForSetTests[i][1]);
	}
	
	
	public static void  UnionTest() throws Exception{
		for(int i = 0; i < sizesForSetTests.length; i++)
			execSetOp(SetOps.Union, sizesForSetTests[i][0], sizesForSetTests[i][1]);
	}
	
	public static void  IntersectTest() throws Exception{
		for(int i = 0; i < sizesForSetTests.length; i++)
			execSetOp(SetOps.Intersect, sizesForSetTests[i][0], sizesForSetTests[i][1]);
	}
	
	public static void  DiffTest() throws Exception{
		for(int i = 0; i < sizesForSetTests.length; i++)
			execSetOp(SetOps.Diff, sizesForSetTests[i][0], sizesForSetTests[i][1]);
	}
	
	public static void execSetOp (SetOps oper, int size, int numIter) throws Exception {
		Random number = new Random();
		IMarkerSet set1, set2;
		int sizePos = getPosForSize(size);
		

		long sumM = 0;
		long testStart = System.currentTimeMillis();
		for(int i = 0; i < numIter; i+=2) {
			long beforeC = System.currentTimeMillis();
			set1 = testsets[sizePos][0][number.nextInt(NUM_SETS)].cloneSet();
			set2 = testsets[sizePos][0][number.nextInt(NUM_SETS)];
			long afterC = System.currentTimeMillis();
			sumM -= (afterC - beforeC);
			
			doSetOp(oper, set1, set2);
			
		}
		long testEnd = System.currentTimeMillis();
		sumM += (testEnd - testStart);
		
		long sumB = 0;
		testStart = System.currentTimeMillis();
		for(int i = 0; i < numIter; i+=2) {
			long beforeC = System.currentTimeMillis();
			set1 = testsets[sizePos][1][number.nextInt(NUM_SETS)].cloneSet();
			set2 = testsets[sizePos][1][number.nextInt(NUM_SETS)];
			long afterC = System.currentTimeMillis();
			sumB -= (afterC - beforeC);
			
			doSetOp(oper, set1, set2);	
		}
		testEnd = System.currentTimeMillis();
		sumB += (testEnd - testStart);
		
		log.debug("------ SIZE " + size + " ------");
		log.debug("MARKER: " + oper.toString() + " Between " + numIter
				+ " Sets of " + size + " elements each time: " + sumM);
		log.debug("BITMARKER: " + oper.toString() + " Between " + numIter 
				+ " Sets of " + size + " elements each time: " + sumB);
	}
	
	private static void doSetOp(SetOps oper, IMarkerSet set1, IMarkerSet set2) {
		switch(oper) {
		case Union:
			set1.union(set2);
			break;
		case Intersect:
			set1.intersect(set2);
			break;
		case Diff:
			set1.diff(set2);
			break;
		case Clone:
			set1.cloneSet();
			break;
		}
	}
	
	private static int getPosForSize (int size) {
		for(int i = 0; i < sizesForSetTests.length; i++) {
			if (sizesForSetTests[i][0] == size)
				return i;
		}
		return -1;
	}
	

	
	public static void genTestSets () throws Exception {
		Random rand = new Random(0);
		testsets = new IMarkerSet[sizesForSetTests.length][][];
		
		log.debug("-------- GENERATE TEST SETS ----------");
		
		for(int i = 0; i < sizesForSetTests.length; i++) {
			testsets[i] = new IMarkerSet[2][];
		
			log.debug("\t-- size: " + sizesForSetTests[i][0]);
			testsets[i][0] = genSets(false, NUM_SETS, sizesForSetTests[i][0], 
					3, 10000, 3, rand);
			testsets[i][1] = genSets(true, NUM_SETS, sizesForSetTests[i][0], 
					3, 10000, 3, rand);
		}
		log.debug("-------- DONE: GENERATE TEST SETS ----");
	}
	
	private static IMarkerSet[] genSets (boolean bit, int numSets, int card, int maxRel, int maxTid, int maxAttr, Random number) throws Exception {
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
	
	
	
	public static void  ContainingTest(boolean bit) throws Exception{
		Random number = new Random();
		int maxRelid = 3, maxAttr = 3, maxTid = 99999;
		int testsetOffset = bit ? 1 : 0;
		
		// for each size
		for(int i = 0; i < sizesForSetTests.length; i++) {
			int numRep = sizesForSetTests[i][1] * 1000;
			IMarkerSet set1 = testsets[i][testsetOffset][number.nextInt(NUM_SETS)];
			long before = System.currentTimeMillis();
			for(int j = 0; j < numRep; j++) {
				set1.contains(randMarker(maxRelid, maxTid, maxAttr, number));
			}
			long end = System.currentTimeMillis();
			log.debug("Repeated " + numRep + " times checking contains on set of size " + 
					sizesForSetTests[i][0] +  " in time: "
					+ (end - before));
		}
	}
	
	

	public static void  AddingTest(IMarkerSet set1) throws Exception{
		Random number = new Random();
		int maxRelid = 3, maxAttr = 3, maxTid = 99999;
		
		// for each size
		for(int i = 0; i < sizesForSetTests.length; i++) {
			int numAdd = sizesForSetTests[i][0];
			int numRep = sizesForSetTests[i][1];
			long before = System.currentTimeMillis();
			
			for(int j = 0; j < numRep; j++) {
				set1.clear();
				for(int k = 0; k < numAdd; k++)
					set1.add(randMarker(maxRelid, maxTid, maxAttr, number));
			}
			
			long end = System.currentTimeMillis();
			log.debug("Repeated " + numRep + " times adding " + 
					numAdd +  " elements to an empty set in time: "
					+ (end - before));
		}
		
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
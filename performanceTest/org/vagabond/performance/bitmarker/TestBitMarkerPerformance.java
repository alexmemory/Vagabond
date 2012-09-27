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
	
	private static IMarkerSet[][][] testsets;
	private static int[][] sizesForSetTests = new int[][] {{10,500, 50}, {100,500, 50}, 
		{1000,500, 10}, {10000,500,5}, {25000,50,2}};
	
	private static int maxRel = 3;
	private static int maxAttr = 3;
	private static int maxTid = 99999;
	
	public static void main (String[] args) throws Exception {
		PropertyConfigurator.configure("resource/test/perfLog4jproperties.txt");
		//loadToDB("resource/exampleScenarios/homeless.xml");
		loadToDB("resource/exampleScenarios/performanceTest1.xml");
		genTestSets();
		
		IMarkerSet markerset1 = MarkerFactory.newMarkerSet();
		IMarkerSet bitset1 = MarkerFactory.newMarkerSet();
		if (log.isDebugEnabled()) {log.debug("------ ADDING TEST ------");};
		if (log.isDebugEnabled()) {log.debug("\t-- MARKER SET --");};
		AddingTest(markerset1);
		if (log.isDebugEnabled()) {log.debug("\t-- BIT MARKER SET --");};
		AddingTest(bitset1);

		if (log.isDebugEnabled()) {log.debug("------ CONTAINMENT TEST ------");};
		if (log.isDebugEnabled()) {log.debug("\t-- MARKER SET --");};
		ContainingTest(false);
		if (log.isDebugEnabled()) {log.debug("\t-- BIT MARKER SET --");};
		ContainingTest(true);
		
		if (log.isDebugEnabled()) {log.debug("------ UNION TEST ------");};
		UnionTest();
		if (log.isDebugEnabled()) {log.debug("------ INTERSECT TEST ------");};
		IntersectTest();
		if (log.isDebugEnabled()) {log.debug("------ DIFF TEST ------");};
		DiffTest();
		if (log.isDebugEnabled()) {log.debug("------ CLONE TEST ------");};
		CloneTest();	
	}
	
	
	public static void  CloneTest() throws Exception{
		for(int i = 0; i < sizesForSetTests.length; i++)
			execSetOp(SetOps.Clone, sizesForSetTests[i][0], 
					sizesForSetTests[i][1] * 10);
	}
	
	
	public static void  UnionTest() throws Exception{
		for(int i = 0; i < sizesForSetTests.length; i++)
			execSetOp(SetOps.Union, sizesForSetTests[i][0], 
					sizesForSetTests[i][1] * 10);
	}
	
	public static void  IntersectTest() throws Exception{
		for(int i = 0; i < sizesForSetTests.length; i++)
			execSetOp(SetOps.Intersect, sizesForSetTests[i][0], 
					sizesForSetTests[i][1] * 10);
	}
	
	public static void  DiffTest() throws Exception{
		for(int i = 0; i < sizesForSetTests.length; i++)
			execSetOp(SetOps.Diff, sizesForSetTests[i][0], 
					sizesForSetTests[i][1] * 10);
	}
	
	public static void execSetOp (SetOps oper, int size, int numIter) throws Exception {
		Random number = new Random(0);
		IMarkerSet set1, set2;
		int sizePos = getPosForSize(size);
		int numSets = sizesForSetTests[sizePos][2];

		long sumM = 0;
		long testStart = System.currentTimeMillis();
		for(int i = 0; i < numIter; i+=2) {
			long beforeC = System.currentTimeMillis();
			set1 = testsets[sizePos][0][number.nextInt(numSets)].cloneSet();
			set2 = testsets[sizePos][0][number.nextInt(numSets)];
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
			set1 = testsets[sizePos][1][number.nextInt(numSets)].cloneSet();
			set2 = testsets[sizePos][1][number.nextInt(numSets)];
			long afterC = System.currentTimeMillis();
			sumB -= (afterC - beforeC);
			
			doSetOp(oper, set1, set2);	
		}
		testEnd = System.currentTimeMillis();
		sumB += (testEnd - testStart);
		
		if (log.isDebugEnabled()) {log.debug("------ SIZE " + size + " ------");};
		if (log.isDebugEnabled()) {log.debug("MARKER: " + oper.toString() + " Between " + numIter
				+ " Sets of " + size + " elements each time: " + sumM);};
		if (log.isDebugEnabled()) {log.debug("BITMARKER: " + oper.toString() + " Between " + numIter 
				+ " Sets of " + size + " elements each time: " + sumB);};
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
		
		if (log.isDebugEnabled()) {log.debug("-------- GENERATE TEST SETS ----------");};
		
		for(int i = 0; i < sizesForSetTests.length; i++) {
			int numSets = sizesForSetTests[i][2];
			testsets[i] = new IMarkerSet[2][];
		
			if (log.isDebugEnabled()) {log.debug("\t-- size: " + sizesForSetTests[i][0]);};
			testsets[i][0] = genSets(false, numSets, sizesForSetTests[i][0], 
					rand);
			testsets[i][1] = genSets(true, numSets, sizesForSetTests[i][0], 
					rand);
		}
		if (log.isDebugEnabled()) {log.debug("-------- DONE: GENERATE TEST SETS ----");};
	}
	
	private static IMarkerSet[] genSets (boolean bit, int numSets, int card, 
			Random number) throws Exception {
		IMarkerSet[] result = new IMarkerSet[numSets];
		
		for(int i = 0; i < numSets; i++) {
			if (bit)
				result[i] = MarkerFactory.newBitMarkerSet();
			else
				result[i] = MarkerFactory.newMarkerSet();

			populateSet(result[i], card, number);
		}
		
		return result;
	}
	
	public static void populateSet(IMarkerSet set1, int card, Random number) 
			throws Exception{
		for(int i = 0; i < card; i++)
			set1.add(randMarker(maxRel, maxTid, maxAttr, number));
	}
	
	
	
	public static void  ContainingTest(boolean bit) throws Exception{
		Random number = new Random(0);
		int testsetOffset = bit ? 1 : 0;
		
		// for each size
		for(int i = 0; i < sizesForSetTests.length; i++) {
			int numRep = sizesForSetTests[i][1] * 1000;
			int numSets = sizesForSetTests[i][2];
			IMarkerSet set1 = testsets[i][testsetOffset][number.nextInt(numSets)];
			
			long before = System.currentTimeMillis();
			for(int j = 0; j < numRep; j++) {
				set1.contains(randMarker(maxRel, maxTid, maxAttr, number));
			}
			long end = System.currentTimeMillis();
			if (log.isDebugEnabled()) {log.debug("Repeated " + numRep + " times checking contains on set of size " + 
					sizesForSetTests[i][0] +  " in time: "
					+ (end - before));};
		}
	}
	
	

	public static void  AddingTest(IMarkerSet set1) throws Exception{
		Random number = new Random(0);
		
		// for each size
		for(int i = 0; i < sizesForSetTests.length; i++) {
			int numAdd = sizesForSetTests[i][0];
			int numRep = sizesForSetTests[i][1];
			long before = System.currentTimeMillis();
			
			for(int j = 0; j < numRep; j++) {
				set1.clear();
				for(int k = 0; k < numAdd; k++)
					set1.add(randMarker(maxRel, maxTid, maxAttr, number));
			}
			
			long end = System.currentTimeMillis();
			if (log.isDebugEnabled()) {log.debug("Repeated " + numRep + " times adding " + 
					numAdd +  " elements to an empty set in time: "
					+ (end - before));};
		}
		
	}
	
	private static IAttributeValueMarker randMarker (int maxRel, int maxTid, 
			int maxAttr, Random number) throws Exception {
		int relid;
		int attrid;
		int tid;
		String tidString;
		
		relid = number.nextInt(maxRel);
		tid = number.nextInt(maxTid);
		attrid = number.nextInt(maxAttr);
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
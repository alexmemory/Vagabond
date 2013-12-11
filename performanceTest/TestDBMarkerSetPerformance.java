
import java.io.File;
import java.sql.Connection;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.marker.DBMarkerSet;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerSet;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader.LoadMode;
import org.vagabond.performance.bitmarker.TestBitMarkerPerformance;
import org.vagabond.test.TestOptions;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.Enums.Marker_Type;
import org.vagabond.util.GlobalResetter;



public class TestDBMarkerSetPerformance {
	static Logger log = Logger.getLogger(TestBitMarkerPerformance.class);

	public enum SetOps {
		Union,
		Intersect,
		Diff,
		Clone
	}

	private static int[] sizesForSetTests = new int[] {100,1000, 2000, 4000, 5000, 10000};
	private static int repeatCount = 2;

	public static void main (String[] args) throws Exception {
		
		PropertyConfigurator.configure("resource/test/perfLog4jproperties.txt");
		Logger.getRootLogger().removeAllAppenders();

		loadToDB("resource/exampleScenarios/homeless3.xml");
		
		//Prints out the avg taken taken for converting one type to another
		//generationTest();
				
		//SingleAdditionTest();
		
		unionTest();
		if (log.isDebugEnabled()) {log.debug("\t-- Generated --");};
	}

	private static void unionTest() throws Exception {
		int maxAttr = 15;
		int maxTid = 10000;

		//TABLE to JAVA REP
		System.out.print("\n\t--Union TABLE --");
		int loop;
		unionDBMarkerSet(maxAttr, maxTid, Marker_Type.TABLE_REP , Marker_Type.JAVA_REP,"");
		
		//JAVA to TABLE REP
		System.out.print("\n\t--Union JAVA to TABLE REP--");
		unionDBMarkerSet(maxAttr, maxTid, Marker_Type.JAVA_REP , Marker_Type.TABLE_REP,"");
		
		//INDEXED QUERY to JAVA REP
		System.out.print("\n\t--Union INDEXED QUERY --");
		unionDBMarkerSet(maxAttr, maxTid, Marker_Type.QUERY_REP , Marker_Type.JAVA_REP,"select 'table10000_15' :: text,  tid, '0100000000000000' ::bit varying  from source.table10000_15 where col1<");
		
		
		//NON INDEXED QUERY to JAVA REP
		System.out.print("\n\t--Union NON INDEXED  QUERY--");
		unionDBMarkerSet(maxAttr, maxTid, Marker_Type.QUERY_REP , Marker_Type.TABLE_REP,"select 'table10000_15' :: text,  tid, '0100000000000000' ::bit varying  from source.table10000_15 where tid<");
	}



	private static long[] unionDBMarkerSet(int maxAttr, int maxTid,Marker_Type source, Marker_Type dest, String query)
			throws Exception {
		int loop=0;
		long[] times = new long[sizesForSetTests.length];
		
		for(int i=0;i<sizesForSetTests.length;i++)
		{
		  times[i]=0;
		}
		
		//Repeat the following tests k times to get a good average time estimate. k =  repeatCount
		while(loop<repeatCount)
		{
			DBMarkerSet[] testset = genSets(source, "table10000_15",maxAttr, maxTid, query);
			DBMarkerSet[] testset2 = genSets(source, "table10000_15",maxAttr, maxTid, query);
			for(int i=0;i<sizesForSetTests.length;i++)
			{
				DBMarkerSet temp =testset[i];
				DBMarkerSet temp1 =testset[i];
				long before = System.currentTimeMillis();
				temp.union(temp1);
				long after = System.currentTimeMillis();
				times[i] += after - before; 
				//if (log.isDebugEnabled()) {log.debug("\t-- Size :" + sizesForSetTests[i] + " Time : " + times[i]);};
			}
			loop++;
		}
		
		
		for(int i=0;i<sizesForSetTests.length;i++)
		{
		  times[i]/=repeatCount;
		  System.out.print("\n\t-- Size :" + sizesForSetTests[i] + " Avg Time : " + times[i]);
		}
		return times;
	}


	private static void generationTest() throws Exception {
		int maxAttr = 15;
		int maxTid = 10000;

		//TABLE to JAVA REP
		System.out.print("\n\t--TABLE to JAVA REP--");
		int loop;
		convertDBMarkerSet(maxAttr, maxTid, Marker_Type.TABLE_REP , Marker_Type.JAVA_REP,"");
		
		//JAVA to TABLE REP
		System.out.print("\n\t--JAVA to TABLE REP--");
		convertDBMarkerSet(maxAttr, maxTid, Marker_Type.JAVA_REP , Marker_Type.TABLE_REP,"");
		
		//INDEXED QUERY to JAVA REP
		System.out.print("\n\t--INDEXED QUERY to JAVA REP--");
		convertDBMarkerSet(maxAttr, maxTid, Marker_Type.QUERY_REP , Marker_Type.JAVA_REP,"select 'table10000_15' :: text,  tid, '0100000000000000' ::bit varying  from source.table10000_15 where col1<");
		
		//NON INDEXED QUERY to JAVA REP
		System.out.print("\n\t--NON INDEXED QUERY to JAVA REP--");
		convertDBMarkerSet(maxAttr, maxTid, Marker_Type.QUERY_REP , Marker_Type.JAVA_REP,"select 'table10000_15' :: text,  tid, '0100000000000000' ::bit varying  from source.table10000_15 where tid<");
		
		//INDEXED QUERY to Table REP
		System.out.print("\n\t--INDEXED  QUERY to TABLE REP--");
		convertDBMarkerSet(maxAttr, maxTid, Marker_Type.QUERY_REP , Marker_Type.TABLE_REP,"select 'table10000_15' :: text,  tid, '0100000000000000' ::bit varying from source.table10000_15 where col1<");
		
		//NON INDEXED QUERY to JAVA REP
		System.out.print("\n\t--NON INDEXED  QUERY to TABLE REP--");
		convertDBMarkerSet(maxAttr, maxTid, Marker_Type.QUERY_REP , Marker_Type.TABLE_REP,"select 'table10000_15' :: text,  tid, '0100000000000000' ::bit varying  from source.table10000_15 where tid<");
	}



	private static long[] convertDBMarkerSet(int maxAttr, int maxTid,Marker_Type source, Marker_Type dest, String query)
			throws Exception {
		int loop=0;
		long[] times = new long[sizesForSetTests.length];
		
		for(int i=0;i<sizesForSetTests.length;i++)
		{
		  times[i]=0;
		}
		
		//Repeat the following tests k times to get a good average time estimate. k =  repeatCount
		while(loop<repeatCount)
		{
			DBMarkerSet[] testset = genSets(source, "table10000_15",maxAttr, maxTid, query);
			for(int i=0;i<sizesForSetTests.length;i++)
			{
				DBMarkerSet temp =testset[i];
				
				long before = System.currentTimeMillis();
				temp.GenerateRep(dest);
				long after = System.currentTimeMillis();
				times[i] += after - before; 
				//if (log.isDebugEnabled()) {log.debug("\t-- Size :" + sizesForSetTests[i] + " Time : " + times[i]);};
			}
			loop++;
		}
		
		
		for(int i=0;i<sizesForSetTests.length;i++)
		{
		  times[i]/=repeatCount;
		  System.out.print("\n\t-- Size :" + sizesForSetTests[i] + " Avg Time : " + times[i]);
		}
		return times;
	}


	private static void SingleAdditionTest() throws Exception {
		int maxAttr = 15;
		int maxTid = 10000;

		//TABLE
		System.out.print("\n\t--Addition to TABLE DBMarkerSet--");
		int loop;
		addToDBMarkerSet(maxAttr, maxTid, Marker_Type.TABLE_REP , Marker_Type.JAVA_REP,"");
		
		//JAVA 
		System.out.print("\n\t--Addition to JAVA  DBMarkerSet--");
		addToDBMarkerSet(maxAttr, maxTid, Marker_Type.JAVA_REP , Marker_Type.TABLE_REP,"");
		
		// INDEXED QUERY
		System.out.print("\n\t--Addition to  INDEXED QUERY  DBMarkerSet--");
		addToDBMarkerSet(maxAttr, maxTid, Marker_Type.QUERY_REP , Marker_Type.JAVA_REP,"select 'table10000_15' :: text,  tid, '0100000000000000' ::bit varying  from source.table10000_15 where col1<");
		
		//NON INDEXED QUERY to JAVA REP
		System.out.print("\n\t--Addition to NON INDEXED QUERY  DBMarkerSet--");
		addToDBMarkerSet(maxAttr, maxTid, Marker_Type.QUERY_REP , Marker_Type.JAVA_REP,"select 'table10000_15' :: text,  tid, '0100000000000000' ::bit varying  from source.table10000_15 where tid<");
		
		
	}
	
	private static long[] addToDBMarkerSet(int maxAttr, int maxTid,Marker_Type source, Marker_Type dest, String query)
			throws Exception {
		int loop=0;
		long[] times = new long[sizesForSetTests.length];
		
		for(int i=0;i<sizesForSetTests.length;i++)
		{
		  times[i]=0;
		}
		
		//Repeat the following tests k times to get a good average time estimate. k =  repeatCount
		while(loop<repeatCount)
		{
			DBMarkerSet[] testset = genSets(source, "table10000_15",maxAttr, maxTid, query);
			for(int i=0;i<sizesForSetTests.length;i++)
			{
				DBMarkerSet temp =testset[i];
				IAttributeValueMarker marker = randMarker("table10000_15", maxTid, maxAttr, new Random(0));
				long before = System.currentTimeMillis();
				
				temp.add(marker);
				long after = System.currentTimeMillis();
				times[i] += after - before; 
				//if (log.isDebugEnabled()) {log.debug("\t-- Size :" + sizesForSetTests[i] + " Time : " + times[i]);};
			}
			loop++;
		}
		
		
		for(int i=0;i<sizesForSetTests.length;i++)
		{
		  times[i]/=repeatCount;
		  System.out.print("\n\t-- Size :" + sizesForSetTests[i] + " Avg Time : " + times[i]);
		}
		return times;
	}
	
	
	private static DBMarkerSet[] genSets (Marker_Type type, String relName, int maxAttr, int maxTid, String query) throws Exception 
	{
		DBMarkerSet[] result = new DBMarkerSet[sizesForSetTests.length];
		Random number = new Random(0);


		for(int i = 0; i < sizesForSetTests.length; i++) {
			int card = sizesForSetTests[i];

			MarkerSet markers2 = new MarkerSet();
			markers2.add(randMarker(relName, maxTid, maxAttr, number));
			if(type!=Marker_Type.QUERY_REP)
			{
			    result[i] = new DBMarkerSet(markers2,false);
			  //Reset other types
				if(type != Marker_Type.JAVA_REP)
				{
					result[i].GenerateRep(type);
					result[i].ResetMarkerType(Marker_Type.JAVA_REP);

				}
				//Populate set with the specified number of records
				populateSet(result[i], card, number, relName, maxAttr, maxTid);
			}
			else
			{
				String fullquery = query + card;
				result[i] = new DBMarkerSet(fullquery,false);
			}

			

			
		}

		return result;
	}

	public static void populateSet(IMarkerSet set1, int card, Random number, String relName,int maxAttr, int maxTid) 
			throws Exception{
		for(int i = 0; i < card; i++)
			set1.add(randMarker(relName, maxTid, maxAttr, number));
	}

	private static IAttributeValueMarker randMarker (String relName, int maxTid, 
			int maxAttr, Random number) throws Exception {
		int relid;
		int attrid;
		int tid;
		String tidString;
		relid = ScenarioDictionary.getInstance().getRelId(relName);
		//relid = number.nextInt(maxRel);
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

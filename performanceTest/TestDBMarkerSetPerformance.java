
import java.io.File;
import java.sql.Connection;
import java.util.Random;

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
import org.vagabond.test.TestOptions;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.Enums.Marker_Type;
import org.vagabond.util.GlobalResetter;



public class TestDBMarkerSetPerformance {
	
	public enum TestType {
		TypeGeneration,
		AddRecord,
		Union
	}

	private static int[] sizesForSetTests = new int[] {100, 1000, 2000, 4000, 5000, 10000};
	private static int repeatCount = 4;
	
	static int maxAttr;
	static int maxTid = 10000;
	static String[] tableName={"table10000_5","table10000_10","table10000_15"};
	static String currentTableName="";
	static int rand = 400;
	static String indQuery ;
	static String nonIndQuery;
	

	public static void main (String[] args) throws Exception {

		//Load the homeless3 xml database. This also requires the table10000_15.csv file to be 
		//present in the resource/exampleScenarios/PerformanceTest folder
		loadToDB("resource/exampleScenarios/homeless3.xml");

		for(int i=0;i<tableName.length;i++)
		{
			currentTableName = tableName[i];
			maxAttr = (i+1)*5;
			indQuery = "select '" + currentTableName + "' :: text,  tid, '0100000000000000' ::bit varying  from source." + currentTableName + " where col1<";
			nonIndQuery = "select '" + currentTableName + "' :: text,  tid, '0100000000000000' ::bit varying  from source." + currentTableName + " where tid<";
			
			System.out.print("\n\nPerformance Test using table : " + currentTableName +" with column count : " + maxAttr + "--\n");
			
			//Run the DBMarkerSet single record addition performance test
			singleAdditionTest();

			//Run the DBMarkerSet type conversion performance test
			generationTest();

			//Run the DBMarkerSet union performance test
			unionTest();
		}
	}

	private static void unionTest() throws Exception {

		System.out.print("\n\nUnion Performance Test --\n");
		//Union TABLE 
		System.out.print("\n\t--Union TABLE --");
		testDBMarkerSet(TestType.Union, Marker_Type.TABLE_REP , null,"");

		//Union JAVA
		System.out.print("\n\n\t--Union JAVA --");
		testDBMarkerSet(TestType.Union, Marker_Type.JAVA_REP , null,"");

		//Union INDEXED QUERY
		System.out.print("\n\n\t--Union INDEXED QUERY --");
		testDBMarkerSet(TestType.Union, Marker_Type.QUERY_REP , null,indQuery);


		//Union NON INDEXED  QUERY
		System.out.print("\n\n\t--Union NON INDEXED  QUERY--");
		testDBMarkerSet(TestType.Union, Marker_Type.QUERY_REP , null,nonIndQuery);
	}

	private static void generationTest() throws Exception {

		System.out.print("\n\nType generation Performance Test --");

		//TABLE to JAVA REP
		System.out.print("\n\n\t--TABLE to JAVA REP--");
		testDBMarkerSet(TestType.TypeGeneration, Marker_Type.TABLE_REP , Marker_Type.JAVA_REP,"");

		//JAVA to TABLE REP
		System.out.print("\n\n\t--JAVA to TABLE REP--");
		testDBMarkerSet(TestType.TypeGeneration, Marker_Type.JAVA_REP , Marker_Type.TABLE_REP,"");

		//INDEXED QUERY to JAVA REP
		System.out.print("\n\n\t--INDEXED QUERY to JAVA REP--");
		testDBMarkerSet(TestType.TypeGeneration, Marker_Type.QUERY_REP , Marker_Type.JAVA_REP,indQuery);

		//NON INDEXED QUERY to JAVA REP
		System.out.print("\n\n\t--NON INDEXED QUERY to JAVA REP--");
		testDBMarkerSet(TestType.TypeGeneration, Marker_Type.QUERY_REP , Marker_Type.JAVA_REP,nonIndQuery);

		//INDEXED QUERY to Table REP
		System.out.print("\n\n\t--INDEXED  QUERY to TABLE REP--");
		testDBMarkerSet(TestType.TypeGeneration, Marker_Type.QUERY_REP , Marker_Type.TABLE_REP,indQuery);

		//NON INDEXED QUERY to Table REP
		System.out.print("\n\n\t--NON INDEXED  QUERY to TABLE REP--");
		testDBMarkerSet(TestType.TypeGeneration, Marker_Type.QUERY_REP , Marker_Type.TABLE_REP,nonIndQuery);
	}

	private static void singleAdditionTest() throws Exception {

		System.out.print("\n\nAdd Record Performance Test --");

		//TABLE
		System.out.print("\n\n\t--Addition to TABLE DBMarkerSet--");
		testDBMarkerSet(TestType.AddRecord, Marker_Type.TABLE_REP , null,"");

		//JAVA 
		System.out.print("\n\n\t--Addition to JAVA  DBMarkerSet--");
		testDBMarkerSet(TestType.AddRecord, Marker_Type.JAVA_REP , null,"");

		// INDEXED QUERY
		System.out.print("\n\n\t--Addition to  INDEXED QUERY  DBMarkerSet--");
		testDBMarkerSet(TestType.AddRecord, Marker_Type.QUERY_REP , null,indQuery);

		//NON INDEXED QUERY 
		System.out.print("\n\n\t--Addition to NON INDEXED QUERY  DBMarkerSet--");
		testDBMarkerSet(TestType.AddRecord, Marker_Type.QUERY_REP , null,nonIndQuery);


	}

	private static long[] testDBMarkerSet(TestType ttype,Marker_Type source, Marker_Type dest, String query)
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
			//Generate the DBMarkerSet to be used for testing
			DBMarkerSet[] testset = genSets(source, currentTableName,maxAttr, maxTid, query);
			DBMarkerSet[] testset2=null;

			//In case of union test, generate the second DBMarkerSet
			if(ttype == TestType.Union)
				testset2 = genSets(source, currentTableName,maxAttr, maxTid, query);
			for(int i=0;i<sizesForSetTests.length;i++)
			{
				long before, after;
				DBMarkerSet temp =testset[i];

				//Union Test
				if(ttype == TestType.Union)
				{
					DBMarkerSet temp1 = testset2[i];
					before = System.currentTimeMillis();
					temp.union(temp1);
					after = System.currentTimeMillis();
				}

				//Add record test
				else if(ttype == TestType.AddRecord)
				{
					rand +=200;
					IAttributeValueMarker marker = randMarker(currentTableName, maxTid, maxAttr, new Random(rand));
					before = System.currentTimeMillis();
					temp.add(marker);
					after = System.currentTimeMillis();
				}
				//DBMarker type conversion
				else 
				{
					before = System.currentTimeMillis();
					temp.GenerateRep(dest);
					after = System.currentTimeMillis();
				}
				times[i] += after - before; 
			}
			loop++;
		}

		//Calculate the average times
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
		rand +=200;
		Random number = new Random(rand);


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

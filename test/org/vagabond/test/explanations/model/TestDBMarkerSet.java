package org.vagabond.test.explanations.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.vagabond.explanation.marker.AttrValueMarker;
import org.vagabond.explanation.marker.BitMarkerSet;
import org.vagabond.explanation.marker.DBMarkerSet;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.explanation.marker.query.QueryMarkerSetGenerator;
import org.vagabond.mapping.scenarioToDB.MaterializedViewsBroker;
import org.vagabond.test.AbstractVagabondDBTest;
import org.vagabond.test.TestOptions;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.Enums.Marker_Type;
import org.vagabond.util.ewah.BitsetFactory;
import org.vagabond.util.ewah.IBitSet;
import org.vagabond.util.ewah.IBitSet.BitsetType;

public class TestDBMarkerSet extends AbstractVagabondDBTest {

	public TestDBMarkerSet(String name) {
		super(name);
	}
	private String query;
	private String relName;  // NULL if not materialized

	

	public void initialize () throws Exception {
		//loadToDB("resource/exampleScenarios/homeless.xml");
		loadToDB("resource/test/simpleTest2.xml");
		
		
		
	}
	

	@Test public void testGenerateRepQueryToTable() throws Exception
	{
		initialize ();
		//Set the connection
				
		//Test query to select the markers in the target
		query = "SELECT 'person' AS rel,  tid, '01' AS attr FROM source.person";
		
		//Generate the table representation
		DBMarkerSet test = new DBMarkerSet(query, false);
		
		
		//Get list of elements
		test.GenerateRep(Marker_Type.TABLE_REP);
		
		Set<ISingleMarker> generated_markers = test.getElems();
		for(ISingleMarker temp:generated_markers)
		{
		  System.out.println(temp.getRel() + " : " + temp.toUserString() +": " +temp.getTid());
		}
			
		//Get the expected table name, values etc from table directly?
		Set<ISingleMarker> expected_markers= new HashSet<ISingleMarker>(); 
		ISingleMarker m0 = new AttrValueMarker("person", "1", "address");
		ISingleMarker m2 = new AttrValueMarker("person", "2", "address");
		ISingleMarker m4 = new AttrValueMarker("person", "3", "address");
		ISingleMarker m6 = new AttrValueMarker("person", "4", "address");
		expected_markers.add(m4);
		expected_markers.add(m2);
		expected_markers.add(m0);
		expected_markers.add(m6);
		
		assertEquals(expected_markers, generated_markers);
		//Test if both are same.
		test.decompose();
		
	}
	

	@Test public void testGenerateRepTableToJavaObj() throws Exception
	{
		initialize ();
		//Set the connection
				
		//Test query to select the markers in the target
		query = "select * from (VALUES  ('person','4',B'010'::bit varying), " +
				"('person','1',B'010'::bit varying), " +
				"('person','3',B'010'::bit varying), " +
				"('person','2',B'010'::bit varying) ) as foo(relation,tid,attribute)";
		
		//Generate the table representation
		DBMarkerSet test = new DBMarkerSet(query, false);
		
		
		//Get list of elements
		test.GenerateRep(Marker_Type.TABLE_REP);
		
		
		//Reset the query representation
		test.ResetMarkerType(Marker_Type.QUERY_REP);
		
		//Generate the java representation
		test.GenerateRep(Marker_Type.JAVA_REP);
						
		//Get the expected table name, values etc from table directly?
		Set<ISingleMarker> expected_markers= new HashSet<ISingleMarker>(); 
		ISingleMarker m0 = new AttrValueMarker("person", "1", "address");
		ISingleMarker m2 = new AttrValueMarker("person", "2", "address");
		ISingleMarker m4 = new AttrValueMarker("person", "3", "address");
		ISingleMarker m6 = new AttrValueMarker("person", "4", "address");
		expected_markers.add(m4);
		expected_markers.add(m2);
		expected_markers.add(m0);
		expected_markers.add(m6);
		
		Set<ISingleMarker> generated_markers = test.getElems();
		assertEquals(expected_markers, generated_markers);
		//Test if both are same.
		test.decompose();
	}
	
	@Test 
	public void testGenerateRepTableToQuery() throws Exception
	{
		MaterializedViewsBroker instance = MaterializedViewsBroker.getInstance();
		if(instance !=null)
			instance.decompose();
		initialize ();
		//Get the expected table name, values etc from table directly?
		Set<ISingleMarker> insert_markers= new HashSet<ISingleMarker>(); 
		ISingleMarker m0 = new AttrValueMarker("person", "1", "address");
		ISingleMarker m2 = new AttrValueMarker("person", "2", "address");
		ISingleMarker m4 = new AttrValueMarker("person", "3", "address");
		ISingleMarker m6 = new AttrValueMarker("person", "4", "address");
		insert_markers.add(m4);
		insert_markers.add(m2);
		insert_markers.add(m0);
		insert_markers.add(m6);

		//Generate the table representation
		DBMarkerSet test = new DBMarkerSet(insert_markers, false);
		
		//Get table representation
		test.GenerateRep( Marker_Type.TABLE_REP);
		
			
		//Clear the Java rep
		test.ResetMarkerType(Marker_Type.JAVA_REP);
				

		//Generate query representation
		test.GenerateRep(Marker_Type.QUERY_REP);
		
		//get the generated query
		String genratedQuery = test.getQuery();
		
		assertEquals(insert_markers, test.getElems());

		test.decompose();
	}
	
	@Test
	public void testGenerateRepJavaObjToTable() throws Exception
	{
		MaterializedViewsBroker instance = MaterializedViewsBroker.getInstance();
		if(instance !=null)
			instance.decompose();
		initialize ();
		//Get the expected table name, values etc from table directly?
		Set<ISingleMarker> insert_markers= new HashSet<ISingleMarker>(); 
		ISingleMarker m0 = new AttrValueMarker("person", "1", "address");
		ISingleMarker m2 = new AttrValueMarker("person", "2", "address");
		ISingleMarker m4 = new AttrValueMarker("person", "3", "address");
		ISingleMarker m6 = new AttrValueMarker("person", "4", "address");
		insert_markers.add(m4);
		insert_markers.add(m2);
		insert_markers.add(m0);
		insert_markers.add(m6);

		//Generate the table representation
		DBMarkerSet test = new DBMarkerSet(insert_markers, false);
		
		//Get list of elements
		test.GenerateRep( Marker_Type.TABLE_REP);


		Set<ISingleMarker> generated_markers = test.getElems();
		assertEquals(insert_markers, generated_markers);
        assertEquals(4, generated_markers.size());
        test.decompose();
	}
	
	@Test 
	public void testGenerateRepJavaObjToQuery() throws Exception
	{
		MaterializedViewsBroker instance = MaterializedViewsBroker.getInstance();
		if(instance !=null)
			instance.decompose();
		initialize ();
		//Get the expected table name, values etc from table directly?
		Set<ISingleMarker> insert_markers= new HashSet<ISingleMarker>(); 
		ISingleMarker m0 = new AttrValueMarker("person", "1", "address");
		ISingleMarker m2 = new AttrValueMarker("person", "2", "address");
		ISingleMarker m4 = new AttrValueMarker("person", "3", "address");
		ISingleMarker m6 = new AttrValueMarker("person", "4", "address");
		insert_markers.add(m4);
		insert_markers.add(m2);
		insert_markers.add(m0);
		insert_markers.add(m6);

		//Generate the table representation
		DBMarkerSet test = new DBMarkerSet(insert_markers, false);
		
		//Get list of elements
		test.GenerateRep( Marker_Type.QUERY_REP);

		//Reset the javaobj type
		test.ResetMarkerType(Marker_Type.JAVA_REP);

		assertEquals(insert_markers, test.getElems());
		test.decompose();
		
	}
	
	
	
}

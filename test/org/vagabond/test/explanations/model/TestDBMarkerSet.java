package org.vagabond.test.explanations.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import org.vagabond.explanation.marker.ISchemaMarker;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerSet;
import org.vagabond.explanation.marker.MarkerSummary;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.explanation.marker.TupleMarker;
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
	private static MarkerSet markers;
	private static DBMarkerSet mv;

	public void initialize () throws Exception {
		//loadToDB("resource/exampleScenarios/homeless.xml");
		loadToDB("resource/test/simpleTest2.xml");
		
		
		
	}
	
	@Test
	public void testDBMarkerSetIntersectJavaJava () throws Exception {
		initialize();
		ISingleMarker m0 = new AttrValueMarker("employee", "1|1", "city");
		ISingleMarker m1 = new AttrValueMarker("employee", "2|2", "city");
		markers = new MarkerSet();
		markers.add(m0);
		markers.add(m1);
		mv = new DBMarkerSet(markers,false);
		//mv.ResetMarkerType(Marker_Type.QUERY_REP);
		
		ISingleMarker m2 = new AttrValueMarker("employee", "2|2", "city");
		ISingleMarker m3 = new AttrValueMarker("employee", "3|", "name");
		MarkerSet markers2 = new MarkerSet();
		markers2.add(m2);
		markers2.add(m3);
		
	    DBMarkerSet mv2 = new DBMarkerSet(markers2,false);
		
		
		DBMarkerSet mv3 = (DBMarkerSet)mv.intersect(mv2);
		assertTrue(mv3.getSize() == 1);
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
	
	
	@Test
	public void testDBMarkerSetQueryJavaMarkerUnion () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying FROM target.employee where tid in ('1|1','2|2')";
		mv = new DBMarkerSet(query,false);
		
		ISingleMarker m0 = new AttrValueMarker("employee", "2|2", "city");
		ISingleMarker m1 = new AttrValueMarker("employee", "3|", "name");
		markers = new MarkerSet();
		markers.add(m0);
		markers.add(m1);
		
		IMarkerSet mv1 = mv.union(markers);
		assertTrue(mv1.getSize() == 3);
	}
	
	@Test
	public void testDBMarkerSetQueryQueryUnion () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying FROM target.employee where tid in('1|1','2|2')";
		mv = new DBMarkerSet(query,false);
		
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying FROM target.employee where tid in ('1|1','3|','4|2')";
		DBMarkerSet mv2 = new DBMarkerSet(query,false);
		
		IMarkerSet mv3 = mv.union(mv2);
		assertTrue(mv3.getSize() == 4);
	}
	
	@Test
	public void testDBMarkerSetQueryTableUnion () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying FROM target.employee where tid in('1|1','2|2')";
		mv = new DBMarkerSet(query,false);
		
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying FROM target.employee where tid in ('1|1','3|','4|2')";
		DBMarkerSet mv2 = new DBMarkerSet(query,true);
		mv2.ResetMarkerType(Marker_Type.QUERY_REP);
		
		IMarkerSet mv3 = mv.union(mv2);
		assertTrue(mv3.getSize() == 4);
	}
	
	@Test
	public void testDBMarkerSetTableQueryUnion () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying FROM target.employee where tid in('1|1','2|2')";
		mv = new DBMarkerSet(query,false);
		mv.ResetMarkerType(Marker_Type.QUERY_REP);
		
		query = "SELECT 'employee':: text,  tid, '01' ::bit varying FROM target.employee where tid in ('1|1','3|','4|2')";
		DBMarkerSet mv2 = new DBMarkerSet(query,true);
	
		
		IMarkerSet mv3 = mv.union(mv2);
		assertTrue(mv3.getSize() == 4);
		
	}
	
	@Test
	public void testDBMarkerSetTableTableUnion () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying FROM target.employee where tid in('1|1','2|2')";
		mv = new DBMarkerSet(query,true);
		mv.ResetMarkerType(Marker_Type.QUERY_REP);
		
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying FROM target.employee where tid in ('1|1','3|','4|2')";
		DBMarkerSet mv2 = new DBMarkerSet(query,true);
		mv2.ResetMarkerType(Marker_Type.QUERY_REP);
		
		DBMarkerSet mv3 = (DBMarkerSet)mv.union(mv2);
		assertTrue(mv3.getSize() == 4);
		
	}
	
	@Test
	public void testDBMarkerSetTableJavaUnion () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying  FROM target.employee where tid in('1|1','2|2')";
		mv = new DBMarkerSet(query,true);
		mv.ResetMarkerType(Marker_Type.QUERY_REP);
		
		ISingleMarker m0 = new AttrValueMarker("employee", "2|2", "city");
		ISingleMarker m1 = new AttrValueMarker("employee", "3|", "name");
		markers = new MarkerSet();
		markers.add(m0);
		markers.add(m1);
		
	    DBMarkerSet mv2 = new DBMarkerSet(markers,false);
		
		
		DBMarkerSet mv3 = (DBMarkerSet)mv.union(mv2);
		assertTrue(mv3.getSize() == 3);
		
	}
	
	@Test
	public void testDBMarkerSetJavaJavaUnion () throws Exception {
		initialize();
		
		ISingleMarker m0 = new AttrValueMarker("employee", "1|1", "city");
		ISingleMarker m1 = new AttrValueMarker("employee", "3|", "name");
		markers = new MarkerSet();
		markers.add(m0);
		markers.add(m1);
		
		mv = new DBMarkerSet(markers,true);
		
		
		ISingleMarker m2 = new AttrValueMarker("employee", "2|2", "city");
		ISingleMarker m3 = new AttrValueMarker("employee", "3|", "name");
		markers = new MarkerSet();
		markers.add(m2);
		markers.add(m3);
		
	    DBMarkerSet mv2 = new DBMarkerSet(markers,false);
		
		
		DBMarkerSet mv3 = (DBMarkerSet)mv.union(mv2);
		assertTrue(mv3.getSize() == 3);
		
	}
	
	@Test
	public void testDBMarkerSetQueryJavaUnion () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying FROM target.employee where tid in ('1|1','2|2')";
		mv = new DBMarkerSet(query,false);
		
		ISingleMarker m0 = new AttrValueMarker("employee", "2|2", "city");
		ISingleMarker m1 = new AttrValueMarker("employee", "3|", "name");
		markers = new MarkerSet();
		markers.add(m0);
		markers.add(m1);
		
		DBMarkerSet mv2 = new DBMarkerSet(markers,false);
		
		DBMarkerSet mv3 = (DBMarkerSet)mv.union(mv2);
		assertTrue(mv3.getSize() == 3);
		
	}
	
	@Test
	public void testDBMarkerSetJavaQueryUnion () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying FROM target.employee where tid in ('1|1','2|2')";
		mv = new DBMarkerSet(query,false);
		
		ISingleMarker m0 = new AttrValueMarker("employee", "2|2", "city");
		ISingleMarker m1 = new AttrValueMarker("employee", "3|", "name");
		markers = new MarkerSet();
		markers.add(m0);
		markers.add(m1);
		
		DBMarkerSet mv2 = new DBMarkerSet(markers,false);
		
		DBMarkerSet mv3 = (DBMarkerSet)mv2.union(mv);
		assertTrue(mv3.getSize() == 3);
		
	}
	
	@Test
	public void testDBMarkerSetJavaTableUnion () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying  FROM target.employee where tid in('1|1','2|2')";
		mv = new DBMarkerSet(query,true);
		mv.ResetMarkerType(Marker_Type.QUERY_REP);
		
		ISingleMarker m0 = new AttrValueMarker("employee", "2|2", "city");
		ISingleMarker m1 = new AttrValueMarker("employee", "3|", "name");
		markers = new MarkerSet();
		markers.add(m0);
		markers.add(m1);
		
	    DBMarkerSet mv2 = new DBMarkerSet(markers,false);
		
		
		DBMarkerSet mv3 = (DBMarkerSet)mv2.union(mv);
		assertTrue(mv3.getSize() == 3);
		
	}
	
	@Test
	public void testDBMarkerSetIntersectQueryQueryView () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying  FROM target.employee where tid in('1|1','2|2')";
		mv = new DBMarkerSet(query,false);
		//mv.ResetMarkerType(Marker_Type.QUERY_REP);
		
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying  FROM target.employee where tid in('1|1','2|2','3|')";
		DBMarkerSet mv2 = new DBMarkerSet(query,false);
	    
		DBMarkerSet mv3 = (DBMarkerSet)mv2.intersect(mv);
		assertTrue(mv3.getSize() == 2);
	}
	
	@Test
	public void testDBMarkerSetIntersectQueryTable () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying  FROM target.employee where tid in('1|1','2|2')";
		mv = new DBMarkerSet(query,false);
		//
		
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying  FROM target.employee where tid in('1|1','2|2','3|')";
		DBMarkerSet mv2 = new DBMarkerSet(query,true);
		mv2.ResetMarkerType(Marker_Type.QUERY_REP);
		
		DBMarkerSet mv3 = (DBMarkerSet)mv2.intersect(mv);
		assertTrue(mv3.getSize() == 2);
	}
	
	@Test
	public void testDBMarkerSetIntersectTableQuery () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying  FROM target.employee where tid in('1|1','2|2')";
		mv = new DBMarkerSet(query,false);
		//
		
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying  FROM target.employee where tid in('1|1','2|2','3|')";
		DBMarkerSet mv2 = new DBMarkerSet(query,true);
		mv2.ResetMarkerType(Marker_Type.QUERY_REP);
		
		DBMarkerSet mv3 = (DBMarkerSet)mv2.intersect(mv);
		assertTrue(mv3.getSize() == 2);
	}
	
	@Test
	public void testDBMarkerSetIntersectTableTable () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying  FROM target.employee where tid in('1|1','2|2')";
		mv = new DBMarkerSet(query,true);
		mv.ResetMarkerType(Marker_Type.QUERY_REP);
		
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying  FROM target.employee where tid in('1|1','2|2','3|')";
		
	    DBMarkerSet mv2 = new DBMarkerSet(query,true);
	    mv2.ResetMarkerType(Marker_Type.QUERY_REP);
		
		
		DBMarkerSet mv3 = (DBMarkerSet)mv2.intersect(mv);
		assertTrue(mv3.getSize() == 2);
	}
	
	@Test
	public void testDBMarkerSetIntersectTableJava () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying  FROM target.employee where tid in('1|1','2|2')";
		mv = new DBMarkerSet(query,true);
		mv.ResetMarkerType(Marker_Type.QUERY_REP);
		
		ISingleMarker m0 = new AttrValueMarker("employee", "2|2", "city");
		ISingleMarker m1 = new AttrValueMarker("employee", "3|", "name");
		markers = new MarkerSet();
		markers.add(m0);
		markers.add(m1);
		
	    DBMarkerSet mv2 = new DBMarkerSet(markers,false);
		
		
		DBMarkerSet mv3 = (DBMarkerSet)mv2.intersect(mv);
		assertTrue(mv3.getSize() == 1);
	}
	
	@Test
	public void testDBMarkerSetIntersectJavaTable () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying  FROM target.employee where tid in('1|1','2|2')";
		mv = new DBMarkerSet(query,true);
		mv.ResetMarkerType(Marker_Type.QUERY_REP);
		
		ISingleMarker m0 = new AttrValueMarker("employee", "2|2", "city");
		ISingleMarker m1 = new AttrValueMarker("employee", "3|", "name");
		markers = new MarkerSet();
		markers.add(m0);
		markers.add(m1);
		
	    DBMarkerSet mv2 = new DBMarkerSet(markers,false);
		
		
		DBMarkerSet mv3 = (DBMarkerSet)mv.intersect(mv2);
		assertTrue(mv3.getSize() == 1);
	}
	
	@Test
	public void testDBMarkerSetIntersectQueryJava () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying  FROM target.employee where tid in('1|1','2|2')";
		mv = new DBMarkerSet(query,false);
		//mv.ResetMarkerType(Marker_Type.QUERY_REP);
		
		ISingleMarker m0 = new AttrValueMarker("employee", "2|2", "city");
		ISingleMarker m1 = new AttrValueMarker("employee", "3|", "name");
		markers = new MarkerSet();
		markers.add(m0);
		markers.add(m1);
		
	    DBMarkerSet mv2 = new DBMarkerSet(markers,false);
		
		
		DBMarkerSet mv3 = (DBMarkerSet)mv.intersect(mv2);
		assertTrue(mv3.getSize() == 1);
	}
	
	@Test
	public void testDBMarkerSetIntersectJavaQuery () throws Exception {
		initialize();
		query = "SELECT 'employee' :: text,  tid, '01' ::bit varying  FROM target.employee where tid in('1|1','2|2')";
		mv = new DBMarkerSet(query,false);
		//mv.ResetMarkerType(Marker_Type.QUERY_REP);
		
		ISingleMarker m0 = new AttrValueMarker("employee", "2|2", "city");
		ISingleMarker m1 = new AttrValueMarker("employee", "3|", "name");
		markers = new MarkerSet();
		markers.add(m0);
		markers.add(m1);
		
	    DBMarkerSet mv2 = new DBMarkerSet(markers,false);
		
		
		DBMarkerSet mv3 = (DBMarkerSet)mv2.intersect(mv);
		assertTrue(mv3.getSize() == 1);
	}
	
	
	
	
}

package org.vagabond.test.batchjobs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.vagabond.explanation.marker.AttrValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISchemaMarker;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerSet;
import org.vagabond.explanation.marker.MarkerSetFlattenedView;
import org.vagabond.explanation.marker.MarkerSummary;
import org.vagabond.explanation.marker.TupleMarker;
import org.vagabond.mapping.scenarioToDB.MaterializedViewsBroker;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.test.TestOptions;
import org.vagabond.util.ConnectionManager;


public class TestMarkerSetFlattenedView extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestMarkerSetFlattenedView.class);
	
	private static MarkerSet markers;
	private static MarkerSetFlattenedView mv;
	private static String query;
	
	@Before
	public void setUp () throws Exception {
		loadToDB("resource/test/simpleBatchTest.xml");
		query = 
			"SELECT 'person'::text AS rel, person.tid, B'10'::bit varying AS att " +
			"FROM target.person " +
			"WHERE person.livesin IS NULL " + 
			"UNION " + 
			"SELECT 'person'::text AS rel, person.tid, B'01'::bit varying AS att " + 
			"FROM target.person " + 
			"WHERE person.livesin IS NOT NULL";
		mv = new MarkerSetFlattenedView(query);
		markers = new MarkerSet();
	}
	
	@AfterClass
	public static void tearDown () throws Exception {
		MaterializedViewsBroker.getInstance().decompose();
		TestOptions.getInstance().close();
		ConnectionManager.getInstance().closeCon();
	}
	
	@Test
	public void testMarkerSetViewSize () throws Exception {
		assertTrue(mv.getSize() == 6);
	}
	
	@Test
	public void testMarkerSetViewNumElem () throws Exception {
		assertTrue(mv.getNumElem() == 6);
	}
	
	@Test
	public void testMarkerSetViewEqualsMarkerSet () throws Exception {
		ISingleMarker m0 = new AttrValueMarker("person", "1M", "name");
		ISingleMarker m2 = new AttrValueMarker("person", "2M", "name");
		ISingleMarker m4 = new AttrValueMarker("person", "3M", "name");
		ISingleMarker m1 = new AttrValueMarker("person", "1|3|2", "livesin");
		ISingleMarker m3 = new AttrValueMarker("person", "2|1|1", "livesin");
		ISingleMarker m5 = new AttrValueMarker("person", "3|3|2", "livesin");
		markers.add(m0);
		markers.add(m2);
		markers.add(m4);
		markers.add(m1);
		markers.add(m3);
		markers.add(m5);
		
		assertTrue(mv.equals(markers));
	}
	
	@Test
	public void testMarkerSetViewSizeNotEqualsMarkerSetView () throws Exception {
		String query1 = 
			"SELECT 'person'::text AS rel, person.tid, B'10'::bit varying AS att " +
			"FROM target.person " +
			"WHERE person.livesin IS NULL";
		MarkerSetFlattenedView mv1 = new MarkerSetFlattenedView(query1);
		
		assertFalse(mv.equals(mv1));
	}
	
	@Test
	public void testMarkerSetViewContentsNotEqualsMarkerSetView () throws Exception {
		String query1 = 
			"SELECT 'person'::text AS rel, person.tid, B'10'::bit varying AS att " + 
			"FROM target.person " + 
			"WHERE person.livesin IS NOT NULL " +
			"UNION " + 
			"SELECT 'person'::text AS rel, person.tid, B'01'::bit varying AS att " +
			"FROM target.person " +
			"WHERE person.livesin IS NULL and tid<>'1M' " +
			"UNION " +
			"SELECT 'person', '4M', B'10'::bit varying";
		MarkerSetFlattenedView mv1 = new MarkerSetFlattenedView(query1);
		
		assertFalse(mv.equals(mv1));
	}
	
	@Test
	public void testMarkerSetViewEqualsMarkerSetView () throws Exception {
		String query1 = 
			"SELECT 'person'::text AS rel, person.tid, B'01'::bit varying AS att " + 
			"FROM target.person " + 
			"WHERE person.livesin IS NOT NULL " +
			"UNION " + 
			"SELECT 'person'::text AS rel, person.tid, B'10'::bit varying AS att " +
			"FROM target.person " +
			"WHERE person.livesin IS NULL ";
		MarkerSetFlattenedView mv1 = new MarkerSetFlattenedView(query1);
		
		assertTrue(mv.equals(mv1));
	}
	
	@Test
	public void testMarkerSetViewUnion () throws Exception {
		ISingleMarker m0 = new AttrValueMarker("person", "4M", "name");
		ISingleMarker m1 = new AttrValueMarker("person", "5M", "livesin");
		markers.add(m0);
		markers.add(m1);
		
		IMarkerSet mv1 = mv.union(markers);
		assertTrue(mv1.getSize() == 8);
	}
	
	@Test
	public void testContains () throws Exception {
		ISingleMarker m0 = new AttrValueMarker("person", "1M", "name");
		ISingleMarker m1 = new AttrValueMarker("person", "8", "name");
		ISingleMarker m2 = new TupleMarker("person", "1M");
		
		assertTrue(mv.contains(m0));
		assertFalse(mv.contains(m1));
		assertFalse(mv.contains(m2));
	}
	
	@Test
	public void testMarkerSetViewContainsAllMarkerSet () throws Exception {
		ISingleMarker m0 = new AttrValueMarker("person", "1M", "name");
		ISingleMarker m2 = new AttrValueMarker("person", "2M", "name");
		markers.add(m0);
		markers.add(m2);
		
		assertTrue(mv.containsAll(markers));
	}
	
	@Test
	public void testMarkerSetViewContainsAllView () throws Exception {
		String query1 = 
			"SELECT 'person'::text AS rel, person.tid, B'10'::bit varying AS att " +
			"FROM target.person " +
			"WHERE person.livesin IS NULL";
		MarkerSetFlattenedView mv1 = new MarkerSetFlattenedView(query1);
		
		assertTrue(mv.containsAll(mv1));
	}
	
	@Test
	public void testMarkerSetViewAddSingleMarker () throws Exception {
		ISingleMarker m0 = new AttrValueMarker("person", "4M", "name");
		mv.materialize();
		mv.addSingleMarker(m0);
		
		assertTrue(mv.getSize() == 7);
	}
	
	@Test
	public void testMarkerSetViewAdd () throws Exception {
		ISingleMarker m0 = new AttrValueMarker("person", "1M", "name");
		ISingleMarker m1 = new AttrValueMarker("person", "1|3|2", "livesin");
		ISingleMarker m2 = new AttrValueMarker("person", "4M", "name");
		mv.materialize();
		assertFalse(mv.add(m0));
		assertFalse(mv.add(m1));
		assertTrue(mv.add(m2));
	}
	
	@Test
	public void testMarkerSetViewAddAll () throws Exception {
		ISingleMarker m0 = new AttrValueMarker("person", "1M", "name");
		ISingleMarker m1 = new AttrValueMarker("person", "4M", "name");
		markers.add(m0);
		markers.add(m1);
		mv.materialize();
		assertTrue(mv.addAll(markers));
		assertTrue(mv.getSize() == 7);
	}

	@Test
	public void testMarkerSetViewRemove () throws Exception {
		ISingleMarker m0 = new AttrValueMarker("person", "1M", "name");
		ISingleMarker m1 = new AttrValueMarker("person", "4M", "livesin");
		mv.materialize();
		assertTrue(mv.remove(m0));
		assertFalse(mv.remove(m1));
		assertTrue(mv.getSize() == 5);
		
		ISingleMarker m2 = new AttrValueMarker("person", "2M", "name");
		assertTrue(mv.remove(m2));
		assertTrue(mv.getSize() == 4);
	}
	
	@Test
	public void testMarkerSetViewRemoveAll () throws Exception {
		ISingleMarker m0 = new AttrValueMarker("person", "1M", "name");
		ISingleMarker m1 = new AttrValueMarker("person", "4M", "livesin");
		markers.add(m0);
		markers.add(m1);
		mv.materialize();

		assertTrue(mv.removeAll(markers));
		assertTrue(mv.getSize() == 5);
		
		String query1 = 
			"SELECT 'person'::text AS rel, person.tid, B'10'::bit varying AS att " +
			"FROM target.person " +
			"WHERE person.livesin IS NULL";
		MarkerSetFlattenedView mv1 = new MarkerSetFlattenedView(query1);
		assertTrue(mv.removeAll(mv1));
		assertTrue(mv.getSize() == 3);
	}
	
	@Test
	public void testMarkerSetViewRetainAll () throws Exception {
		ISingleMarker m0 = new AttrValueMarker("person", "1M", "name");
		ISingleMarker m1 = new AttrValueMarker("person", "2M", "name");
		ISingleMarker m2 = new AttrValueMarker("person", "4M", "name");
		markers.add(m0);
		markers.add(m1);
		markers.add(m2);
		mv.materialize();
		
		assertTrue(mv.retainAll(markers));
		assertTrue(mv.getSize() == 2);

		String query1 = 
			"SELECT 'person'::text AS rel, person.tid, B'10'::bit varying AS att " +
			"FROM target.person " +
			"WHERE person.livesin IS NULL";
		MarkerSetFlattenedView mv1 = new MarkerSetFlattenedView(query1);
		assertFalse(mv.retainAll(mv1));
		assertTrue(mv.getSize() == 2);
	}
	
	@Test
	public void testMarkerSetViewIntersectView () throws Exception {
		String query1 = 
			"SELECT 'person'::text AS rel, person.tid, B'10'::bit varying AS att " +
			"FROM target.person " +
			"WHERE person.livesin IS NULL";
		MarkerSetFlattenedView mv1 = new MarkerSetFlattenedView(query1);
		assertTrue(mv.intersect(mv1).getSize() == 3);
	}
	
	@Test
	public void testMarkerSetViewIntersectMarkerSet () throws Exception {
		ISingleMarker m0 = new AttrValueMarker("person", "1M", "name");
		ISingleMarker m1 = new AttrValueMarker("person", "4M", "name");
		ISingleMarker m3 = new AttrValueMarker("person", "2|1|1", "livesin");
		markers.add(m0);
		markers.add(m1);
		markers.add(m3);
		assertTrue(mv.intersect(markers).getSize() == 2);
	}
	
	@Test
	public void testMarkerSetViewGetSummary () throws Exception {
		MarkerSetFlattenedView fl = new MarkerSetFlattenedView("SELECT * FROM " +
				"(VALUES ('person','1M','B01'::bit varying), ('person','2M','B01'::bit varying)) AS m(rel,tid,att)");
		mv.materialize();
		MarkerSummary ms = mv.getSummary();
		
		assertTrue(ms.size() == 2);
		ISchemaMarker sm1 = MarkerFactory.newSchemaMarker("person", "name");
		ISchemaMarker sm2 = MarkerFactory.newSchemaMarker("person", "livesin");
		assertTrue(ms.contains(sm1));
		assertTrue(ms.contains(sm2));
		
		ms = fl.getSummary();
		assertTrue(ms.size() == 1);
		assertTrue(ms.contains(sm1));
	}
}

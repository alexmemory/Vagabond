package org.vagabond.test.batchjobs;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.generation.CopySourceExplanationGenerator;
import org.vagabond.explanation.generation.prov.SourceProvParser;
import org.vagabond.explanation.marker.AttrValueMarker;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.marker.MarkerQueryBatch;
import org.vagabond.explanation.marker.MarkerSet;
import org.vagabond.explanation.marker.MarkerSetView;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.prov.ProvWLRepresentation;
import org.vagabond.mapping.scenarioToDB.MaterializedViewsBroker;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.test.TestOptions;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.PropertyWrapper;


public class TestMarkerSetView extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestMarkerSetView.class);
	
	private static MarkerSet markers;
	private static MarkerSetView mv;
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
		mv = new MarkerSetView(query);
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
		MarkerSetView mv1 = new MarkerSetView(query1);
		
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
		MarkerSetView mv1 = new MarkerSetView(query1);
		
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
		MarkerSetView mv1 = new MarkerSetView(query1);
		
		assertTrue(mv.equals(mv1));
	}
	
	@Test
	public void testMarkerSetViewUnion () throws Exception {
		ISingleMarker m0 = new AttrValueMarker("person", "4M", "name");
		markers.add(m0);
		
		IMarkerSet mv1 = mv.union(markers);
		assertTrue(mv1.getSize() == 7);
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
		ISingleMarker m1 = new AttrValueMarker("person", "4M", "name");
		mv.materialize();
		assertFalse(mv.add(m0));
		assertTrue(mv.add(m1));
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
	}
	
	@Test
	public void testMarkerSetViewRetainAll () throws Exception {
		ISingleMarker m0 = new AttrValueMarker("person", "1M", "name");
		markers.add(m0);
		assertTrue(mv.retainAll(markers));
		assertTrue(mv.getSize() == 1);
	}
	
	@Test
	public void testMarkerSetViewIntersectView () throws Exception {
		String query1 = 
			"SELECT 'person'::text AS rel, person.tid, B'10'::bit varying AS att " +
			"FROM target.person " +
			"WHERE person.livesin IS NULL";
		MarkerSetView mv1 = new MarkerSetView(query1);
		assertTrue(mv.intersect(mv1).getSize() == 3);
	}
	
	@Test
	public void testMarkerSetViewIntersectMarkerSet () throws Exception {
		ISingleMarker m0 = new AttrValueMarker("person", "1M", "name");
		ISingleMarker m1 = new AttrValueMarker("person", "4M", "name");
		markers.add(m0);
		markers.add(m1);
		assertTrue(mv.intersect(markers).getSize() == 1);
	}
	
}

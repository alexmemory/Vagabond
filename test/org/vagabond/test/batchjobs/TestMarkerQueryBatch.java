package org.vagabond.test.batchjobs;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.marker.AttrValueMarker;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerQueryBatch;
import org.vagabond.explanation.marker.MarkerSet;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.ConnectionManager;


public class TestMarkerQueryBatch extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestMarkerQueryBatch.class);
	
	private static MarkerSet markers = new MarkerSet();
	private static MarkerQueryBatch mq;
	
	@BeforeClass
	public static void setUp () throws Exception {
		loadToDB("resource/test/simpleBatchTest.xml");
		
	}
	
	@Test
	public void testMarkerQueryBatchSetName () throws Exception {
		String relName = 
			"SELECT 'person'::text AS rel, person.tid, B'10'::bit varying AS att " +
			"FROM target.person " +
			"WHERE person.livesin IS NULL " + 
			"UNION " + 
			"SELECT 'person'::text AS rel, person.tid, B'01'::bit varying AS att " + 
			"FROM target.person " + 
			"WHERE person.livesin IS NOT NULL";
//		String relName = "errm";
		String predicate = "att & 'B10'::varbit != 'B00'::varbit";
		
		mq = new MarkerQueryBatch(relName, predicate);
		ISingleMarker m0 = new AttrValueMarker("person", "1M", "name");
		ISingleMarker m2 = new AttrValueMarker("person", "2M", "name");
		ISingleMarker m4 = new AttrValueMarker("person", "3M", "name");
		markers.add(m0);
		markers.add(m2);
		markers.add(m4);
		
		assert(mq.equals(markers));
		
	}
	
	@Test
	public void testMarkerQueryBatchSetLivesin () throws Exception {
		String relName = 
			"SELECT 'person'::text AS rel, person.tid, B'10'::bit varying AS att " +
			"FROM target.person " +
			"WHERE person.livesin IS NULL " + 
			"UNION " + 
			"SELECT 'person'::text AS rel, person.tid, B'01'::bit varying AS att " + 
			"FROM target.person " + 
			"WHERE person.livesin IS NOT NULL";
//		String relName = "errm";
		String predicate = "att & 'B01'::varbit != 'B00'::varbit";
		
		mq = new MarkerQueryBatch(relName, predicate);
		ISingleMarker m1 = new AttrValueMarker("person", "1|3|2", "livesin");
		ISingleMarker m3 = new AttrValueMarker("person", "2|1|1", "livesin");
		ISingleMarker m5 = new AttrValueMarker("person", "3|3|2", "livesin");
		markers.add(m1);
		markers.add(m3);
		markers.add(m5);
		
		assert(mq.equals(markers));
		
	}
	
	@Test
	public void testMarkerQueryBatchSetLivesinMaterializedView () throws Exception {
		String query = 
			"CREATE TABLE errm AS " +
			"SELECT 'person'::text AS rel, person.tid, B'10'::bit varying AS att " +
			"FROM target.person " +
			"WHERE person.livesin IS NULL " + 
			"UNION " + 
			"SELECT 'person'::text AS rel, person.tid, B'01'::bit varying AS att " + 
			"FROM target.person " + 
			"WHERE person.livesin IS NOT NULL";
		
		ConnectionManager.getInstance().execUpdate(query);
		
		String relName = "errm";
		String predicate = "att & 'B01'::varbit != 'B00'::varbit";
		
		mq = new MarkerQueryBatch(relName, predicate);
		ISingleMarker m1 = new AttrValueMarker("person", "1|3|2", "livesin");
		ISingleMarker m3 = new AttrValueMarker("person", "2|1|1", "livesin");
		ISingleMarker m5 = new AttrValueMarker("person", "3|3|2", "livesin");
		markers.add(m1);
		markers.add(m3);
		markers.add(m5);
		
		assert(mq.equals(markers));
		
		query = "DROP TABLE errm";
		ConnectionManager.getInstance().execUpdate(query);
		
	}
	
}

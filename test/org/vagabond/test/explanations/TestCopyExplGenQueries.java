package org.vagabond.test.explanations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.vagabond.explanation.generation.CopySourceExplanationGenerator;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.test.AbstractVagabondDBTest;

public class TestCopyExplGenQueries extends AbstractVagabondDBTest {

	private CopySourceExplanationGenerator gen;

	public TestCopyExplGenQueries(String name) throws Exception {
		super(name);
		loadToDB("resource/test/simpleTest.xml");
		
		gen = new CopySourceExplanationGenerator();
	}
	
	@Test
	public void testCopyCSQuery () throws Exception {
		String query = QueryHolder.getQuery("CopyCS.GetProv")
				.parameterize("target.employee","2|2","city");
		String result = 
			"\n   city   | prov_source_person_tid | prov_source_person_name " +
					"| prov_source_person_address | prov_source_address_tid | " +
					"prov_source_address_id | prov_source_address_city\n" + 
			"----------+------------------------+-------------------------+-" +
			"---------------------------+-------------------------+---------" +
			"---------------+--------------------------\n" +
					"Montreal |                        |                      " +
					"   |                            |                      " +
					" 2 |                      2 | Montreal";
		
		testSingleQuery(query, result);
	}
	
	@Test
	public void testSideEffectQuery () throws Exception {
		String query = QueryHolder.getQuery("CopyCS.GetSideEffect")
				.parameterize("target.employee", 
						" subprov.prov_source_address_tid IS DISTINCT FROM 2");
		String result =  "\ntid\n" + 
				"-----\n" +
				 "2$MID$2\n" +
				 "4$MID$2";
		
		testSingleQuery(query, result);
	}
	
	@Test
	public void testSideEffectGenQuery () throws Exception {
		Set<String> sourceRels;
		Map<String, IMarkerSet> sourceErr;
		IMarkerSet errSet, errSet2;
		String query;
		String result;

		errSet = MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("employee", "1")
				);
		errSet2 = MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2"),
				MarkerFactory.newTupleMarker("address", "3")
				);
		sourceErr = new HashMap<String, IMarkerSet> ();
		sourceErr.put("employee", errSet);
		sourceErr.put("address", errSet2);
		
		sourceRels = new HashSet<String> ();
		sourceRels.add("address");
		sourceRels.add("person");
		
		query = gen.getSideEffectQuery("employee", sourceRels, sourceErr).trim();
		
		result = "\n tid \n"+
				"-----\n" +
				" 2$MID$2\n" +
				" 4$MID$2";
		
		testSingleQuery(query, result);
	}

	@Test
	public void testSideEffectGenQuery2 () throws Exception {
		Set<String> sourceRels;
		Map<String, IMarkerSet> sourceErr;
		IMarkerSet errSet, errSet2;
		String query;
		String result;

		errSet = MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("person", "3")
				);
		errSet2 = MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2"),
				MarkerFactory.newTupleMarker("address", "3")
				);
		sourceErr = new HashMap<String, IMarkerSet> ();
		sourceErr.put("person", errSet);
		sourceErr.put("address", errSet2);
		
		sourceRels = new HashSet<String> ();
		sourceRels.add("address");
		sourceRels.add("person");
		
		query = gen.getSideEffectQuery("employee", sourceRels, sourceErr).trim();
		
		result = "\n tid \n"+
				"-----\n" +
				" 2$MID$2\n" +
				" 3$MID$\n" +
				" 4$MID$2";
		
		testSingleQuery(query, result);
	}
	
	@Test
	public void testGetProvQueryResultAttrs () throws Exception {
		String query;
		String result;
		
		query = QueryHolder.getQuery("CopyCS.GetProvQueryResultAttrs")
				.parameterize("source.person");
		
		result = "\n                                            attrs                                             \n" +
		"----------------------------------------------------------------------------------------------\n" +
		" {tid,name,address,prov_source_person_tid,prov_source_person_name,prov_source_person_address} \n";
		
		testSingleQuery (query, result);
		
		query = QueryHolder.getQuery("CopyCS.GetProvQueryResultAttrs")
				.parameterize("target.employee");
		
		result = "\n                                            attrs                                             \n" +
		"----------------------------------------------------------------------------------------------\n" +
		"  {tid,name,city,prov_source_person_tid,prov_source_person_name,prov_source_person_address,prov_source_address_tid,prov_source_address_id,prov_source_address_city} \n";
		
		testSingleQuery (query, result);
	}
	
	@Test
	public void testClose () throws Exception {
		AbstractVagabondDBTest.closeDown();
	}
}

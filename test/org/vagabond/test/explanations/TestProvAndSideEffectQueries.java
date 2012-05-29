package org.vagabond.test.explanations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.generation.prov.AlterSourceProvenanceSideEffectGenerator;
import org.vagabond.explanation.generation.prov.AttrGranularitySourceProvenanceSideEffectGenerator;
import org.vagabond.explanation.generation.prov.SideEffectGenerator;
import org.vagabond.explanation.generation.prov.SourceProvenanceSideEffectGenerator;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.test.AbstractVagabondDBTest;
import org.vagabond.test.TestOptions;
import org.vagabond.util.ConnectionManager;

public class TestProvAndSideEffectQueries extends AbstractVagabondDBTest {

	private SideEffectGenerator gen;
	private SourceProvenanceSideEffectGenerator standSEGen;
	private AlterSourceProvenanceSideEffectGenerator alterGen;
	private AttrGranularitySourceProvenanceSideEffectGenerator attrGen;
	
	public TestProvAndSideEffectQueries(String name) throws Exception {
		super(name);
		loadToDB("resource/test/simpleTest.xml");
		
		gen = SideEffectGenerator.getInstance();
		standSEGen = new SourceProvenanceSideEffectGenerator();
		alterGen = new AlterSourceProvenanceSideEffectGenerator();
		attrGen = new AttrGranularitySourceProvenanceSideEffectGenerator();
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
		
		query = standSEGen.getSideEffectQuery("employee", sourceRels, sourceErr).trim();
		
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
		
		query = standSEGen.getSideEffectQuery("employee", sourceRels, sourceErr).trim();
		
		result = "\n tid \n"+
				"-----\n" +
				" 2$MID$2\n" +
				" 3$MID$\n" +
				" 4$MID$2";
		
		testSingleQuery(query, result);
	}
	
	@Test
	public void testSideEffectQuery () throws Exception {
		String query = QueryHolder.getQuery("ProvSE.GetSideEffect")
				.parameterize("target.employee", 
						" subprov.prov_source_address_tid IS DISTINCT FROM 2");
		String result =  "\ntid\n" + 
				"-----\n" +
				 "2$MID$2\n" +
				 "4$MID$2";
		
		testSingleQuery(query, result);
	}

	
	@Test
	public void testAlterSideEffectGenQuery () throws Exception {
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
		
		query = alterGen.getSideEffectQuery
				("employee", sourceRels, sourceErr).trim();
		
		result = "\n tid \n"+
				"-----\n" +
				" 2$MID$2\n" +
				" 3$MID$\n" +
				" 4$MID$2";
		
		testSingleQuery(query, result);		
	}
	
	@Test
	public void testGetSideEffectAggQuery () throws Exception {
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
		
		query = alterGen.getSideEffectQuery("employee", sourceRels, sourceErr).trim();
		
		result = "\n tid \n"+
				"-----\n" +
				" 2$MID$2\n" +
				" 4$MID$2";
		
		testSingleQuery(query, result);
	}
	
	@Test
	public void testGetProvQueryResultAttrs () throws Exception {
		String query;
		String result;
		
		query = QueryHolder.getQuery("MetaQ.GetProvQueryResultAttrs")
				.parameterize("source.person");
		
		result = "\n                                            attrs                                             \n" +
		"----------------------------------------------------------------------------------------------\n" +
		" {tid,name,address,prov_source_person_tid,prov_source_person_name,prov_source_person_address} \n";
		
		testSingleQuery (query, result);
		
		query = QueryHolder.getQuery("MetaQ.GetProvQueryResultAttrs")
				.parameterize("target.employee");
		
		result = "\n                                            attrs                                             \n" +
		"----------------------------------------------------------------------------------------------\n" +
		"  {tid,name,city,prov_source_person_tid,prov_source_person_name,prov_source_person_address,prov_source_address_tid,prov_source_address_id,prov_source_address_city} \n";
		
		testSingleQuery (query, result);
	}
	
	@Test
	public void testGetMapsPerBaseRelAccess () throws Exception {
		String query;
		String result;
		
		query = QueryHolder.getQuery("MetaQ.GetMapsForBaseRelAccess")
				.parameterize("target.employee");
		
		result = "\n xslt_process \n"+
		"--------------\n" +
		" source.person:M1,M2$MID$source.address:M2\n";
		
		testSingleQuery(query, result);
	}
	
	@Test
	public void testGetMapProvQuery () throws Exception {
		String query = QueryHolder.getQuery("MapAndTransProv.GetMapProv")
				.parameterize("target.employee","2|2");
		String result =  "\ntrans_prov\n" + 
				"-----\n" +
				 "M2";

		testSingleQuery(query, result);
	}
	
	@Test
	public void testAttrGenSideEffectQuery () throws Exception {
		Set<String> sourceRels;
		Map<String, IMarkerSet> sourceErr;
		IMarkerSet errSet, errSet2;
		String query;
		String result;

		ConnectionManager.getInstance().getConnection(
				TestOptions.getInstance().getHost(),
				TestOptions.getInstance().getDB(),
				TestOptions.getInstance().getUser(), 
				TestOptions.getInstance().getPassword());
		
		errSet = MarkerParser.getInstance().parseSet("{T(employee,1)}");
		errSet2 = MarkerParser.getInstance().parseSet("{T(address,2),T(address,3)}");
		sourceErr = new HashMap<String, IMarkerSet> ();
		sourceErr.put("employee", errSet);
		sourceErr.put("address", errSet2);
		
		sourceRels = new HashSet<String> ();
		sourceRels.add("address");
		sourceRels.add("person");
		
		query = attrGen.getSideEffectQuery("employee", sourceRels, sourceErr).trim();
		
		result = "\n tid | prov_source_person_tid | prov_source_address_tid \n" +
			"-----+------------------------+-------------------------\n" +
			" 2$MID$2 |                      2 |                       2\n" +
			" 4$MID$2 |                      4 |                       2";
		
		testSingleQuery(query, result);
	}
	
	@Test
	public void testGetSideEffectAndMapQuery () throws Exception {
		String query = QueryHolder
				.getQuery("ProvSE.GetSideEffectUsingAggWithMap")
				.parameterize("target.employee",
						"(prov_source_address_tid = 2 " +
						"OR prov_source_address_tid = 3 )");
		String result = "\n tid | trans_prov\n" + 
						"-----+------------\n" +
						" 2$MID$2 | M2\n" +
						" 4$MID$2 | M2";
		
		
		testSingleQuery(query, result);
	}
	
	@Test
	public void testGetSideEffectAndProvQuery () throws Exception {
		String query = QueryHolder
				.getQuery("ProvSE.GetSideEffectUsingAggPlusCompleteProv")
				.parameterize("target.employee",
						"(prov_source_address_tid = 2 " +
						"OR prov_source_address_tid = 3 )",
						"prov.prov_source_person_tid, prov.prov_source_address_tid");
		String result = "\n tid | prov_source_person_tid | prov_source_address_tid \n" +
					"-----+------------------------+-------------------------\n" +
					" 2$MID$2 |                      2 |                       2\n" +
					" 4$MID$2 |                      4 |                       2";
		
		testSingleQuery(query, result);
	}
	
//	@Test
//	public void testClose () throws Exception {
//		AbstractVagabondDBTest.closeDown();
//	}
}

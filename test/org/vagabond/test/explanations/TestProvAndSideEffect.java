package org.vagabond.test.explanations;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.generation.prov.ProvenanceGenerator;
import org.vagabond.explanation.generation.prov.SourceProvenanceSideEffectGenerator;
import org.vagabond.explanation.marker.IMarkerSet;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.xmlmodel.MappingType;

public class TestProvAndSideEffect extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestProvAndSideEffect.class);
	
	private static ProvenanceGenerator pGen;
	private static SourceProvenanceSideEffectGenerator seGen;
	
	@BeforeClass
	public static void setUp () throws Exception {	
		loadToDB("resource/test/simpleTest.xml");
		QueryHolder.getInstance().loadFromDir(new File ("resource/queries"));
		
		pGen = ProvenanceGenerator.getInstance();
		seGen = new SourceProvenanceSideEffectGenerator();
	}
	
	@Test
	public void testMapProvQuery () throws Exception {
		Set<MappingType> result;
		Set<MappingType> exp;
		
		exp = new HashSet<MappingType>();
		exp.add(MapScenarioHolder.getInstance().getMapping("M2"));
		
		result = pGen.getMapProv(MarkerFactory.
				newAttrMarker("employee", "2|2", "city"));
		assertEquals(result, exp);
	}
	
	

	
	@Test
	public void testSideEffectQueryGen () throws Exception {
		Set<String> sourceRels;
		Map<String, IMarkerSet> sourceErr;
		IMarkerSet errSet, errSet2;
		String resultQuery;
		String query;

		query = "SELECT prov.tid\n" +
				"FROM\n" +
				"(SELECT *\n" +
				"FROM target.employee) AS prov\n" +
				"WHERE NOT EXISTS (SELECT subprov.tid\n" +
				"FROM (SELECT PROVENANCE * FROM target.employee) AS subprov\n" + 
				"WHERE prov.tid = subprov.tid " +
				"AND (prov_source_address_tid IS DISTINCT FROM 2 " +
				"AND prov_source_address_tid IS DISTINCT FROM 3 ))";
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
		
		resultQuery = seGen.getSideEffectQuery
				("employee", sourceRels, sourceErr).trim();
		log.debug(resultQuery);
		
		assertEquals(query, resultQuery);
	}
	
}

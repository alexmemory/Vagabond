package org.vagabond.test.explanations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.vagabond.explanation.generation.CopySourceExplanationGenerator;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.generation.prov.AlterSourceProvenanceSideEffectGenerator;
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
	public void testClose () throws Exception {
		AbstractVagabondDBTest.closeDown();
	}

	

		

}

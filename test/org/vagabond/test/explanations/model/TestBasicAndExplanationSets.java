package org.vagabond.test.explanations.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashSet;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.explanation.marker.SchemaResolver;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.basic.CorrespondenceError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;

public class TestBasicAndExplanationSets extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestBasicAndExplanationSets.class);
	
	@BeforeClass
	public static void setUp () throws Exception {
		ModelLoader.getInstance().loadToInst(
				"resource/exampleScenarios/homelessDebugged.xml");
		SchemaResolver.getInstance().setSchemas();
	}
	
	@Test
	public void testBasicExplanations () throws Exception {
		CopySourceError c1, c2;
		CorrespondenceError r1,r2;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		
		// copy error
		c1 = new CopySourceError();
		c1.setExplains(MarkerParser.getInstance().parseMarker("A(person,2,name)"));
		c1.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,1)}"));
		c1.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
				
		c2 = new CopySourceError();
		c2.setExplains(MarkerParser.getInstance().parseMarker("A(person,2,name)"));
		c2.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,1)}"));
		c2.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
				
		assertEquals(c1,c2);
		
		c1.setTargetSE(MarkerParser.getInstance().parseSet("{T(person,2|1|1)}"));
		
		assertFalse(c1.equals(c2));
		assertFalse(c2.equals(c1));
		
		c1.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
		c1.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,2)}"));
		
		assertFalse(c1.equals(c2));
		assertFalse(c2.equals(c1));
		
		c1.setSourceSE(MarkerParser.getInstance().parseSet("{T(socialworker,1)}"));
		c1.setExplains(MarkerParser.getInstance().parseMarker("A(person,1,name)"));
		
		assertFalse(c1.equals(c2));
		assertFalse(c2.equals(c1));
		
		// correspondence error
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c2"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		
		r1 = new CorrespondenceError();
		r1.setExplains(MarkerParser.getInstance().parseMarker("A(person,1,name)"));
		r1.setCorrespondences(corrs);
		r1.setMapSE(maps);
		r1.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
		
		r2 = new CorrespondenceError();
		r2.setExplains(MarkerParser.getInstance().parseMarker("A(person,1,name)"));
		r2.setCorrespondences(corrs);
		r2.setMapSE(maps);
		r2.setTargetSE(MarkerParser.getInstance().parseSet("{}"));
		
		assertEquals(r1,r2);
	}
}

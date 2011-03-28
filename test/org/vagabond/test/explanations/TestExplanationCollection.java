package org.vagabond.test.explanations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.SchemaResolver;
import org.vagabond.explanation.model.ExplanationCollection;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.CopySourceError;
import org.vagabond.explanation.model.basic.CorrespondenceError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;

public class TestExplanationCollection extends AbstractVagabondTest {

	@BeforeClass
	public static void setUp () throws Exception {
		ModelLoader.getInstance().loadToInst("resource/test/simpleTest.xml");
		SchemaResolver.getInstance().setSchemas();
	}
	
	@Test
	public void testSingleSetCol () throws Exception {
		CopySourceError e1;
		CorrespondenceError e2;
		IExplanationSet set;
		IExplanationSet resultSet;
		IAttributeValueMarker error = 
				MarkerFactory.newAttrMarker("employee", "2|2", "city");
		ExplanationCollection col;
		HashSet<CorrespondenceType> corrs;
		HashSet<MappingType> maps;
		
		
		e1 = new CopySourceError();
		e1.setExplains(error);
		e1.setSourceSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("address", "2")
				));
		e1.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newTupleMarker("employee", "4|2")
				));
		
		corrs = new HashSet<CorrespondenceType> ();
		corrs.add(MapScenarioHolder.getInstance().getCorr("c2"));
		maps = new HashSet<MappingType> ();
		maps.add(MapScenarioHolder.getInstance().getMapping("M2"));
		e2 = new CorrespondenceError();
		e2.setExplains(error);
		e2.setCorrespondences(corrs);
		e2.setMapSE(maps);
		e2.setTargetSE(MarkerFactory.newMarkerSet(
				MarkerFactory.newAttrMarker("employee", "1|1", "city"),
				MarkerFactory.newAttrMarker("employee", "4|2", "city")
		));
		
		set = ExplanationFactory.newExplanationSet(e1,e2);
		
		col = new ExplanationCollection();
		col.addExplSet(error, set);
		
		assertEquals(col.getNumCombinations(),2); 
		assertEquals(col.getDimensions().size(),1);
		assertEquals(col.getDimensions().get(0),new Integer(2));
		col.resetIter();
		while(col.hasNext()) {
			resultSet = col.next();
			assertTrue(resultSet.getExplains().contains(error));
			assertTrue(resultSet.getExplanationsSet().contains(e1)
					|| resultSet.getExplanationsSet().contains(e2));
		}
	}
	
	@Test
	public void testMultiSetCol () {
		
	}
	
}

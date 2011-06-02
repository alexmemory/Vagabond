package org.vagabond.test.mapping.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.ITupleMarker;
import org.vagabond.explanation.marker.MarkerParser;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.MappingGraph;
import org.vagabond.mapping.model.MappingGraph.MappingGraphRel;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.Pair;

public class TestMappingGraph extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestMappingGraph.class);
	
	@Before
	public void setUp () throws Exception {
		loadToDB("resource/test/simpleTest.xml");
	}
	
	@Test
	public void testSimpleGraph () throws Exception {
		MappingGraph g; 
		MappingGraphRel node;
		
		g = MapScenarioHolder.getInstance().getGraphForMapping("M2");
		log.debug(g);
		
		node = g.getForeachAtomsForVar("a").iterator().next();
		assertEquals(0, node.getPos());
		assertEquals("person", node.getRelName());
		assertEquals(CollectionUtils.makeVec("a","b"), node.getVars());
		
		assertEquals(2, g.getForeachAtomsForVar("b").size());
		assertEquals(1, g.getExistsAtomsForVar("c").size());
		
		g.getJoinVarsAndAtoms("c");
	}
	
	@Test
	public void testGetJoinAttrs () throws Exception {
		Set<Pair<Integer,String>> result;
		MappingGraph g; 

		g = MapScenarioHolder.getInstance().getGraphForMapping("M2");
		result = g.getJoinVarsAndAtoms("c");
		
		log.debug(result);
		
		assertEquals(CollectionUtils.makeSet(new Pair<Integer,String>(0, "b")),
				result);
		
		result = g.getJoinVarsAndAtoms("b");
		assertEquals(CollectionUtils.makeSet(), result);
	}
	
}

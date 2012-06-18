package org.vagabond.test.util;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.DirectedGraph;
import org.vagabond.util.Graph;

public class TestGraph {

	static Logger log = Logger.getLogger(TestGraph.class);
	
	@BeforeClass
	public static void setUp () {
		PropertyConfigurator.configure("resource/test/testLog4jproperties.txt");
	}
	
	@Test
	public void testGraph () throws Exception {
		Graph<String> g = new Graph<String> (false);
		
		// create nodes
		g.addNode("A");
		g.addNode("B");
		g.addNode("C");
		g.addNode("D");
		
		assertTrue(g.hasNode("A"));
		assertTrue(g.hasNode("B"));
		assertTrue(g.hasNode("C"));
		assertTrue(g.hasNode("D"));
		assertFalse(g.hasNode("E"));
		
		// create edges
		g.addEdge("A", "B");
		g.addEdge("A", "C");
		g.addEdge("C", "D");
		
		log.debug(g.toString());
		
		assertTrue(g.hasEdge("A", "B"));
		assertTrue(g.hasEdge("B", "A"));
		
		assertTrue(g.hasEdge("A", "C"));
		assertTrue(g.hasEdge("C", "A"));
		
		assertTrue(g.hasEdge("C", "D"));
		assertTrue(g.hasEdge("D", "C"));
		
		assertFalse(g.hasEdge("A", "D"));
		
		assertTrue(g.hasEdge("A"));
		assertTrue(g.hasEdge("B"));
		assertTrue(g.hasEdge("C"));
		assertTrue(g.hasEdge("D"));
		
		assertEquals(g.getNumNodes(), 4); 
	}
	
	
	@Test
	public void testDirectedGraph () throws Exception {
		DirectedGraph<String> g = new DirectedGraph<String> (false);
		
		// create nodes
		g.addNode("A");
		g.addNode("B");
		g.addNode("C");
		g.addNode("D");
		
		assertTrue(g.hasNode("A"));
		assertTrue(g.hasNode("B"));
		assertTrue(g.hasNode("C"));
		assertTrue(g.hasNode("D"));
		assertFalse(g.hasNode("E"));
		
		// create edges
		g.addEdge("A", "B");
		g.addEdge("A", "C");
		g.addEdge("C", "D");
		
		log.debug(g.toString());
		
		assertTrue(g.hasEdge("A", "B"));
		assertFalse(g.hasEdge("B", "A"));
		
		assertTrue(g.hasEdge("A", "C"));
		assertFalse(g.hasEdge("C", "A"));
		
		assertTrue(g.hasEdge("C", "D"));
		assertFalse(g.hasEdge("D", "C"));
		
		assertFalse(g.hasEdge("A", "D"));
		
		assertTrue(g.hasEdge("A"));
		assertTrue(g.hasEdge("B"));
		assertTrue(g.hasEdge("C"));
		assertTrue(g.hasEdge("D"));
		
		assertFalse(g.hasIncomingEdge("A"));
		assertTrue(g.hasIncomingEdge("B"));
		assertTrue(g.hasIncomingEdge("C"));
		assertTrue(g.hasIncomingEdge("D"));

		assertTrue(g.hasOutgoingEdge("A"));
		assertFalse(g.hasOutgoingEdge("B"));
		assertTrue(g.hasOutgoingEdge("C"));
		assertFalse(g.hasOutgoingEdge("D"));
		
		assertEquals(g.getNumNodes(), 4);
	}
	
	@Test
	public void testTopologicalOrder() throws Exception {
		DirectedGraph<String> g = new DirectedGraph<String> (false);
		
		// create nodes
		g.addNode("A");
		g.addNode("B");
		g.addNode("C");
		g.addNode("D");
		
		// create edges
		g.addEdge("A", "B");
		g.addEdge("A", "D");
		g.addEdge("A", "C");
		g.addEdge("B", "C");
		g.addEdge("B", "D");
		g.addEdge("C", "D");
		
		log.debug(g.toString());
		
		List<String> sort = g.topologicalSort();
		List<String> ex = CollectionUtils.makeList("A","B","C","D");
		assertEquals(ex, sort);
	}
}

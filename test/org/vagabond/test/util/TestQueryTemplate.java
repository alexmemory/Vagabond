package org.vagabond.test.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.vagabond.util.QueryTemplate;

public class TestQueryTemplate {

	@Test
	public void testTemplate () {
		QueryTemplate temp = new QueryTemplate("SELECT ${1} FROM ${2} WHERE ${1} = 3;");
		
		assertEquals(temp.parameterize("a", "R"), "SELECT a FROM R WHERE a = 3;");
	}
	
}

package org.vagabond.test.util;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.test.AbstractVagabondTest;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.ResultSetUtil;

public class TestResultSetUtil extends AbstractVagabondTest {

	@BeforeClass
	public static void setUp () throws SQLException, ClassNotFoundException {
		Connection con = ConnectionManager.getInstance().getConnection("localhost", 
				"tramptest", "postgres", "");
	}
	
	@Test
	public void testProvAttrSplitters () {
		String[] names = {"prov_public_r_a", 
						"prov_public_r_1_b", 
						"prov_target_rel__with__underscore_name"};
		assertEquals(ResultSetUtil.getAttrFromProvName(names[0]),"a");
		assertEquals(ResultSetUtil.getAttrFromProvName(names[1]),"b");
		assertEquals(ResultSetUtil.getAttrFromProvName(names[2]),"name");
		
		assertEquals(ResultSetUtil.getRelFromProvName(names[0]),"r");
		assertEquals(ResultSetUtil.getRelFromProvName(names[1]),"r");
		assertEquals(ResultSetUtil.getRelFromProvName(names[2]),"rel_with_underscore");
		
		assertTrue(ResultSetUtil.isProvAttr(names[0]));
		assertTrue(ResultSetUtil.isProvAttr(names[1]));
		assertTrue(ResultSetUtil.isProvAttr(names[2]));
	}
	
	@Test
	public void test () throws SQLException, ClassNotFoundException {
		String[] cols;
		ResultSet rs;
		
		rs = ConnectionManager.getInstance().execQuery(
				"SELECT * FROM source.person");
		cols = ResultSetUtil.getResultColumns(rs);
		assertArrayEquals(cols, new String[] {"tid","name","address"});
		
		rs = ConnectionManager.getInstance().execQuery(
				"SELECT PROVENANCE tid FROM source.person");
		cols = ResultSetUtil.getResultColumns(rs);
		assertArrayEquals(cols, new String[] {"tid","prov_source_person_tid",
				"prov_source_person_name","prov_source_person_address"});
	}
	
}

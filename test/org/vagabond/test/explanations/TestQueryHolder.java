package org.vagabond.test.explanations;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.test.AbstractVagabondTest;

public class TestQueryHolder extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestQueryHolder.class);
	
	@Test
	public void testLoad () throws FileNotFoundException, IOException {
		QueryHolder.getInstance().loadFromDir(new File("resource/queries/"));
		
		log.debug(QueryHolder.getInstance().getQueries().stringPropertyNames());
		assertTrue(QueryHolder.hasQuery("CopyCS.GetProv"));
	}
	
}

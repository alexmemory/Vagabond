package org.vagabond.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.XmlException;
import org.junit.BeforeClass;
import org.vagabond.explanation.generation.QueryHolder;
import org.vagabond.explanation.marker.SchemaResolver;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.util.PropertyWrapper;

public abstract class AbstractVagabondTest {

	@BeforeClass
	public static void setUpLogger () throws FileNotFoundException, IOException, XmlException, ValidationException {
		PropertyConfigurator.configure("resource/test/testLog4jproperties.txt");
		QueryHolder.getInstance().setQueries(new PropertyWrapper(
				"resource/queries/CopyCS.xml"));
	}
	
	protected static void setSchemas (String fileName) throws XmlException, IOException, ValidationException {
		MapScenarioHolder holder;
		
		holder = ModelLoader.getInstance().load(new File(fileName));
		SchemaResolver.getInstance().setSchemas(
				holder.getScenario().getSchemas().getSourceSchema(),
				holder.getScenario().getSchemas().getTargetSchema());
	}
	
}

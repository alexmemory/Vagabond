package org.vagabond.test.explanations.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.vagabond.test.explanations.model.TestExplanationCollection;
import org.vagabond.test.explanations.model.TestMarkerParser;
import org.vagabond.test.explanations.model.TestMarkers;


@RunWith(Suite.class)
@Suite.SuiteClasses({
		TestBasicAndExplanationSets.class,
		TestExplanationCollection.class,
		TestMarkerParser.class,
		TestMarkers.class
        })
public class AllTestExplanationsModel {

}

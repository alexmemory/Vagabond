package org.vagabond.test.explanations.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
		TestBasicAndExplanationSets.class,
		TestExplanationCollection.class,
		TestMarkerParser.class,
		TestMarkers.class,
		TestErrorPartitionGraph.class,
		TestScenarioDictionary.class
        })
public class AllTestExplanationsModel {

}

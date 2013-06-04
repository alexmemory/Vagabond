package org.vagabond.test.explanations.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.vagabond.test.explanations.model.query.AllTestExplanationsModelQuery;


@RunWith(Suite.class)
@Suite.SuiteClasses({
		TestBasicAndExplanationSets.class,
		TestExplanationCollection.class,
		TestMarkerParser.class,
		TestMarkers.class,
		TestBitMarkerSet.class,
		TestErrorPartitionGraph.class,
		TestScenarioDictionary.class,
		TestErrorPartitioning.class,
		AllTestExplanationsModelQuery.class
        })
public class AllTestExplanationsModel {

}

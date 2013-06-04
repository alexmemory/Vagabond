package org.vagabond.test.explanations.model.query;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.vagabond.test.explanations.model.TestBasicAndExplanationSets;
import org.vagabond.test.explanations.model.TestBitMarkerSet;
import org.vagabond.test.explanations.model.TestErrorPartitionGraph;
import org.vagabond.test.explanations.model.TestErrorPartitioning;
import org.vagabond.test.explanations.model.TestExplanationCollection;
import org.vagabond.test.explanations.model.TestMarkerParser;
import org.vagabond.test.explanations.model.TestMarkers;
import org.vagabond.test.explanations.model.TestScenarioDictionary;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		TestQueryMarkerSetGeneration.class
        })
public class AllTestExplanationsModelQuery {

}

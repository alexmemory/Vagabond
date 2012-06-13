package org.vagabond.test.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
		TestResultSetUtil.class,
		TestQueryTemplate.class,
		TestBitMatrixAndBitset.class,
		TestBloomFilter.class,
		TestFNVHash.class,
		TestGraph.class
        })
public class AllTestsUtil {
}

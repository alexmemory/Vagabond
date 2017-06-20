package org.vagabond.test.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
		TestResultSetUtil.class,
		TestQueryTemplate.class,
		TestBitMatrixAndBitset.class,
		TestNewBitMatrixAndBitset.class,
		TestBloomFilter.class,
		TestFNVHash.class,
		TestGraph.class,
		TestPropertyWrapper.class
        })
public class AllTestsUtil {
}

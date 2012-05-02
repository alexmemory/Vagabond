package org.vagabond.test.util;

import static org.junit.Assert.*;

import java.util.Random;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.explanation.ranking.PartitionRanker;
import org.vagabond.explanation.ranking.PartitionRanker.FullExplSummary;
import org.vagabond.explanation.ranking.scoring.SideEffectSizeScore;
import org.vagabond.test.AbstractVagabondTest;

import com.skjegstad.utils.BloomFilter;
import org.vagabond.util.ewah.IBitSet.BitsetType;

public class TestBloomFilter extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestBloomFilter.class);
	
	private static final String[] testStrings = new String[] {"a","b","c","cde"};
	private static final String[] notIn = new String[] {"a","b","c","cde"};
	
	private static FullExplSummary[] testExpl;
	private static FullExplSummary[] notExpl;
	private static final PartitionRanker r;
	
	static {
		r = new PartitionRanker(SideEffectSizeScore.inst);
		testExpl = new FullExplSummary[5];
		notExpl = new FullExplSummary[3];
		
		testExpl[0] = r.new FullExplSummary(3,0);
		testExpl[1] = r.new FullExplSummary(3,1);
		testExpl[2] = r.new FullExplSummary(3,2);
		testExpl[3] = r.new FullExplSummary(3,3);
		testExpl[4] = r.new FullExplSummary(3,4);
		
		notExpl[0] = r.new FullExplSummary(testExpl[0], 0);
		notExpl[1] = r.new FullExplSummary(testExpl[0], 1);
		notExpl[2] = r.new FullExplSummary(testExpl[0], 2);
		Logger.getRootLogger().setLevel(Level.INFO);
	}
	
	@Test
	public void testBloomFilterJava () {
		BloomFilter<String> b = new BloomFilter<String>(20, 10, BitsetType.JavaBitSet);
		
		for(String test: testStrings)
			b.add(test);
		
		for(String test: testStrings)
			assertTrue(test, b.contains(test));
		
		for(String test: notIn) {
			boolean result = b.contains(test); 
			if (result)
				log.info("was in " + test);
		}
	}

	@Test
	public void testBloomFilterEWAH () {
		BloomFilter<String> b = new BloomFilter<String>(20, 10, BitsetType.EWAHBitSet);
		
		for(String test: testStrings)
			b.add(test);
		
		for(String test: testStrings)
			assertTrue(test, b.contains(test));
		
		for(String test: notIn) {
			boolean result = b.contains(test); 
			if (result)
				log.info("was in " + test);
		}
	}
	
	@Test
	public void testBloomFilterJavaFullExplSummary () {
		
		BloomFilter<FullExplSummary> b = new BloomFilter<FullExplSummary>(20, 10, BitsetType.JavaBitSet);
		
		for(FullExplSummary ed: testExpl) {
			b.add(ed);
			log.info(b.toString());
		}
		
		for(FullExplSummary test: testExpl)
			assertTrue("" + test + "\n\n" + b, b.contains(test));
		
		for(FullExplSummary test: notExpl) {
			boolean result = b.contains(test); 
			if (result)
				log.info("was in " + test);
		}
	}
	
	@Test
	public void testBloomFilterEWAHFullExplSummary () {
		
		BloomFilter<FullExplSummary> b = new BloomFilter<FullExplSummary>(20, 10, BitsetType.EWAHBitSet);
		
		for(FullExplSummary ed: testExpl) {
			b.add(ed);
			log.info(b.toString());
		}
		
		for(FullExplSummary test: testExpl)
			assertTrue("" + test + "\n\n" + b, b.contains(test));
		
		for(FullExplSummary test: notExpl) {
			boolean result = b.contains(test); 
			if (result)
				log.info("was in " + test);
		}
	}
	
	@Test
	public void testBloomFilterSpeed () {
		testBloomSpeed(1000, 1000000, 1000, 0.01);
		testBloomSpeed(1000, 1000000, 1000, 0.0001);
//		testBloomSpeed(1000, 1000000, 1000000, 0.01);
	}
	
	private void testBloomSpeed (int dataLen, int testLen, int exLen, double falsePos) {
		BloomFilter<Long> bEwah = new BloomFilter<Long>(falsePos,exLen, BitsetType.EWAHBitSet);
		BloomFilter<Long> bJava = new BloomFilter<Long>(falsePos,exLen, BitsetType.JavaBitSet);
		long[] data = new long[dataLen];
		long[] test = new long[testLen];
		Random rand = new Random(0);
		
		log.info("-------------  " + dataLen + "," + testLen + "," + exLen + "," + falsePos);
		for(int i = 0; i < data.length; i++)
			data[i] = rand.nextLong();
		for(int i = 0; i < test.length; i++) 
			test[i] = rand.nextLong();
		
		long start, end;
		
		start = System.currentTimeMillis();
		for(int i = 0; i < data.length; i++)
			bEwah.add(data[i]);
		end = System.currentTimeMillis();
		log.info("insert EWAH: " + (end - start));
		
		start = System.currentTimeMillis();
		for(int i = 0; i < test.length; i++)
			bEwah.contains(test[i]);
		end = System.currentTimeMillis();
		log.info("test EWAH: " + (end - start));
		log.info("EWAH size " + bEwah.getBitSet().getByteSize());
		
		start = System.currentTimeMillis();
		for(int i = 0; i < data.length; i++)
			bJava.add(data[i]);
		end = System.currentTimeMillis();
		log.info("insert Java: " + (end - start));
		
		start = System.currentTimeMillis();
		for(int i = 0; i < test.length; i++)
			bJava.contains(test[i]);
		end = System.currentTimeMillis();
		log.info("test Java: " + (end - start));
		log.info("Java size " + bJava.getBitSet().getByteSize());
	}
	
}

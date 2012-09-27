package org.vagabond.test.util;

import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.vagabond.test.AbstractVagabondTest;

import static org.vagabond.util.HashFNV.*;

public class TestFNVHash extends AbstractVagabondTest {

	static Logger log = Logger.getLogger(TestFNVHash.class);
	
	private int[] genRandArray (int size, int seed) {
		int[] result = new int[size];
		Random rand = new Random(seed);
		
		for(int i = 0; i < size; i++)
			result[i] = rand.nextInt();
		return result;
	}
	
	@Test
	public void testHash () {
		int[] a1 = genRandArray(10, 0);
		int[] a2 = new int[a1.length];
		System.arraycopy(a1, 0, a2, 0, a1.length);
		a2[3]++;
		
		if (log.isDebugEnabled()) {log.debug(Integer.toBinaryString(fnv(a1)));};
		if (log.isDebugEnabled()) {log.debug(Integer.toBinaryString(fnv(a2)));};
	}
	
}

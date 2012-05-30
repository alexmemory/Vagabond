package org.vagabond.test.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.util.BitMatrix;
import org.vagabond.util.ewah.Bitmap;
import org.vagabond.util.ewah.EWAHCompressedBitmap;
import org.vagabond.util.ewah.BitsetView;
import org.vagabond.util.ewah.IBitSet;
import org.vagabond.util.ewah.IntIterator;
import org.vagabond.util.ewah.JavaUtilBitSet;

public class TestBitMatrixAndBitset {

	static Logger log = Logger.getLogger(TestBitMatrixAndBitset.class);

	public static final String largevalue = "01110001 00000101 01100001 10000011 01000000 10000111 " +
			"00000000 00010000 01100001 00100001 01000001 00000011 00000001" +
			" 01000000 00000000 01000000 00000000 10000000 00000000 10000000 " +
			"00000000 01000000 00000000 01000000 00010000 01000000 0000011";
	
	static final int[] setBits1 = {1, 3, 5, 7, 128, 140, 150};
	static final int[] setBits1a = {150, 140, 128, 7, 5, 3, 1};
	static final int[] setBits1b = {128, 1, 3, 5,  140, 150, 7};
	static final int[] setBits1c = {3, 1, 5, 7, 140, 128, 150};
	static final int[] setBits2 = {1,5,8,15,16};
	static final int[] setBits3 = {1024, 1026, 1800, 3000};
	
	@BeforeClass
	public static void setUp () {
		PropertyConfigurator.configure("resource/test/testLog4jproperties.txt");
	}
	
	public static void setBits(int[] bits, IBitSet map) {
		for(int i = 0; i < bits.length; i++) {
			map.set(bits[i]);
		}
	}
	
	private void randSetBits(int[] bits, IBitSet map) {
		Random rand = new Random();
		int temp, left, right;
		final int[] mix = new int[bits.length];
		
		System.arraycopy(bits, 0, mix, 0, bits.length);
		for(int i = 0; i < bits.length * 10; i++) {
			left = rand.nextInt(bits.length);
			right = rand.nextInt(bits.length);
			temp = bits[left];
			bits[left] = bits[right];
			bits[right] = temp;
		}
		setBits(bits, map);
	}
	 
	private void checkSet(int[] bits, EWAHCompressedBitmap set) {
		for(int i = 0; i < bits.length; i++) {
			assertTrue("i: " + bits[i] + "\n" + set.bufferToString(), set.intersects(EWAHCompressedBitmap.getSingleton(bits[i])));
			assertTrue("get i: " + bits[i] + "\n" + set.bufferToString(), set.get(bits[i]));
		}
	}
	
	private void checkRows (String[] rows, BitMatrix m) {
		for(int i = 0; i < rows.length; i++) {
			assertEquals("Row " + i + " " + rows[i] + "\n\n" +m.getReadonlyRow(i), 
					rows[i], m.getReadonlyRow(i).toBitsString());
		}
	}
	
	private void checkIter (Bitmap set, int[] bits) {
		int[] sorted = new int[bits.length];
		IntIterator iter;
		
		System.arraycopy(bits, 0, sorted, 0, bits.length);
		Arrays.sort(sorted);
		iter = set.intIterator();
		
		for(int i = 0; i < sorted.length; i++) {
			assertTrue(iter.hasNext());
			assertEquals("" + sorted[i], sorted[i], iter.next());
		}
		assertFalse(iter.hasNext());
	}
	
	private void checkIter (IBitSet set, int[] bits, int start, int end) {
		int[] sorted = new int[bits.length];
		IntIterator iter;
		
		System.arraycopy(bits, 0, sorted, 0, bits.length);
		Arrays.sort(sorted);
		iter = set.intIterator(start, end);
		for(int i = 0; i < sorted.length; i++) {
			if (sorted[i] >= start && sorted[i] < end) {
				assertTrue(iter.hasNext());
				assertEquals("" + sorted[i], sorted[i], iter.next());
			}
		}
		assertFalse(iter.hasNext());
	}
	
	private void randCheckIter (IBitSet set, int[] bit) {
		Random rand = new Random();
		int start, end;
		
		start = rand.nextInt(bit.length);
		end = start + 1 + ( (start == bit.length - 1) ? 0 : rand.nextInt(bit.length - 1 - start));
		checkIter(set, bit, start, end);
	}
	
	private void assertEqualsBitset (EWAHCompressedBitmap set1, EWAHCompressedBitmap set2, String name) {
		assertEquals("variant the bits <" + name + ">:\n\n" + set1.bufferToString() 
				+ "\n\n" + set2.bufferToString(), set1.toBitsString(), set2.toBitsString());
		assertEquals("variant <" + name + ">:\n\n" + set1.bufferToString() 
				+ "\n\n" + set2.bufferToString(), set1, set2);
	}
	
	@Test
	public void testBitsetGet () {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap(), bitset2;
		setBits(setBits1, bitset);
		
		checkSet(setBits1, bitset);
		
		for(int i = 0; i < 100; i++) {
			bitset2 = new EWAHCompressedBitmap();
			randSetBits(setBits1, bitset2);
			checkSet(setBits1, bitset2);
		}
	}
	
	@Test
	public void testBitSetToBitString () {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		setBits(setBits2, bitset);
		
		checkSet(setBits2, bitset);
		assertEquals("toBitString", "01000100 10000001 1", bitset.toBitsString());
	}
	
	@Test
	public void testBitSetFromString () {
		String value = "01000100 001";
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap(value);
		assertEquals(value, bitset.toBitsString());
		
		value = "01110001 00000101 01100001 10000011 01000000 10000111 00000000 00010000 01100001 00100001 01000001 00000011 00000001 01000000 00000000 01000000 00000000 10000000 00000000 10000000 00000000 01000000 00000000 01000000 00010000 01000000 0000011";
		bitset = new EWAHCompressedBitmap(value);
		assertEquals(value, bitset.toBitsString());
	}
	
	@Test
	public void testBitsetSaveSet () {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		EWAHCompressedBitmap bitset2 = new EWAHCompressedBitmap();
		setBits(setBits1, bitset);
		
		bitset2 = new EWAHCompressedBitmap();
		setBits(setBits1a, bitset2);
		assertEqualsBitset(bitset,bitset2,"a");
		
		bitset2 = new EWAHCompressedBitmap();
		setBits(setBits1b, bitset2);
		assertEqualsBitset(bitset,bitset2,"b");
		
		bitset2 = new EWAHCompressedBitmap();
		setBits(setBits1c, bitset2);
		assertEqualsBitset(bitset,bitset2,"c");
		
		for(int i = 0; i < 100; i++) {
			bitset2 = new EWAHCompressedBitmap();
			randSetBits(setBits1, bitset2);
			assertEqualsBitset(bitset,bitset2,"iteration " + i);
		}
	}
	
	@Test
	public void testBitsetSaveSet2 () {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		EWAHCompressedBitmap bitset2 = new EWAHCompressedBitmap();
		
		setBits(new int[] {121,0}, bitset);
		setBits(new int[] {0,121}, bitset2);
		
		assertEqualsBitset(bitset2,bitset,"0-set");
	}
	
	@Test 
	public void testBitsetClone () {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		EWAHCompressedBitmap bitset2;
		
		setBits(new int[] {121,0}, bitset);
		bitset2 = (EWAHCompressedBitmap) bitset.clone();
		assertEquals(bitset, bitset2);
		
		bitset2.set(1000);
		assertFalse(bitset.equals(bitset2));
	}
	
	@Test
	public void testLogicalOperations () {
		EWAHCompressedBitmap b1,b2, ex, res;
		
		// OR
		b1 = new EWAHCompressedBitmap("100");
		b2 = new EWAHCompressedBitmap("001");
		ex = new EWAHCompressedBitmap("101");
		res = b1.or(b2);
		assertEquals(ex.toBitsString() + " - " + res.toBitsString(), ex, res);
		
		// AND
		b1 = new EWAHCompressedBitmap("101");
		b2 = new EWAHCompressedBitmap("011");
		ex = new EWAHCompressedBitmap("001");
		res = b1.and(b2);
		assertEquals(ex.toBitsString() + " - " + res.toBitsString(), ex, res);
		
		// AND NOT
		b1 = new EWAHCompressedBitmap("110");
		b2 = new EWAHCompressedBitmap("011");
		ex = new EWAHCompressedBitmap("100");
		ex.setSizeInBits(3);
		res = b1.andNot(b2);
		assertEquals(ex.toBitsString() + " - " + res.toBitsString(), ex, res);
		
		// XOR
		b1 = new EWAHCompressedBitmap("110");
		b2 = new EWAHCompressedBitmap("011");
		ex = new EWAHCompressedBitmap("101");
		res = b1.xor(b2);
		assertEquals(ex.toBitsString() + " - " + res.toBitsString(), ex, res);
		
		// NOT
		b1 = new EWAHCompressedBitmap("101");
		b1.not();
		ex = new EWAHCompressedBitmap("010");
		ex.setSizeInBits(3);
		assertEquals(ex.toBitsString() + " - " + b1.toBitsString(), ex, b1);
	}
	
	@Test
	public void testManyInserts () {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		Random rand = new Random(0);
		
		for(int i = 0; i < 10000; i++) {
			int val = rand.nextInt(1000);
//			log.debug("------------- before " + i + " = " + val);
			assertTrue("before : " + i + " = " + val, bitset.checkInvariants());
//			log.debug(bitset.toDebugString());
			
			bitset.set(val);
					
//			log.debug("after " + i + " = " + val);	
		}
		
	}
	
	@Test
	public void testIter () {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		setBits(setBits1, bitset);
		
		checkIter(bitset, setBits1);
		
		JavaUtilBitSet b2 = new JavaUtilBitSet();
		setBits(setBits1, b2);
		
		checkIter(b2, setBits1);
	}
	
	@Test
	public void testRangeIter () {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		setBits(setBits1, bitset);
		
		checkIter(bitset, setBits1, 6,141);
		
		for(int i = 0; i < 20; i++)
			randCheckIter(bitset, setBits1);
		
		JavaUtilBitSet bitset2 = new JavaUtilBitSet();
		setBits(setBits1, bitset2);
		
		checkIter(bitset2, setBits1, 6,141);
		
		for(int i = 0; i < 20; i++)
			randCheckIter(bitset2, setBits1);
	}
	
	@Test
	public void testEWAHViews () {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		setBits(setBits1, bitset);
		BitsetView view = new BitsetView(bitset, 5, 10);
		BitsetView view2 = new BitsetView(bitset, 5, 7);
		BitsetView view3 = new BitsetView(view, 2, 5);
		
		checkIter(view, new int[] {0,2});
		checkIter(view2, new int[] {0});
		checkIter(view3, new int[] {0});
		
		assertEquals(view.toBitsString(), "10100", view.toBitsString());
		assertEquals(view2.toBitsString(), "10", view2.toBitsString());
		assertEquals(view3.toBitsString(), "100", view3.toBitsString());
	}
	
	@Test
	public void testJavaBitsetViews () {
		JavaUtilBitSet bitset = new JavaUtilBitSet();
		setBits(setBits1, bitset);
		BitsetView view = new BitsetView(bitset, 5, 10);
		BitsetView view2 = new BitsetView(bitset, 5, 7);
		BitsetView view3 = new BitsetView(view, 2, 5);
		
		checkIter(view, new int[] {0,2});
		checkIter(view2, new int[] {0});
		checkIter(view3, new int[] {0});
		
		assertEquals(view.toBitsString(), "10100", view.toBitsString());
		assertEquals(view2.toBitsString(), "10", view2.toBitsString());
		assertEquals(view3.toBitsString(), "100", view3.toBitsString());
	}
	
	@Test
	public void testMatrix () {
		BitMatrix m = new BitMatrix(3, 2);
		
		log.debug(m.toString() + "\n\n");
		
		m.set(2, 1);
		m.set(1, 0);
		m.set(1, 1);
		
		log.debug(m.toString());
		
		assertTrue("2,1", m.get(2, 1));
		assertTrue("1,0", m.get(1, 0));
		assertTrue("1,1", m.get(1, 1));
		assertFalse("0,0", m.get(0, 0));
		assertFalse("0,1", m.get(0, 1));
		
		checkRows(new String[] {"00","11","01"}, m);
		
		assertEquals("first one in col 0", 1, m.firstOneInCol(0));
		assertEquals("first one in col 1", 1, m.firstOneInCol(1));
		
		assertEquals("first one in row 0", -1, m.firstOneInRow(0));
		assertEquals("first one in row 1", 0, m.firstOneInRow(1));
		assertEquals("first one in row 2", 1, m.firstOneInRow(2));
				
		m = new BitMatrix(15, 15, largevalue);
		assertEquals("01110001 0000010", m.getReadonlyRow(0).toBitsString());
		assertEquals("01000000 0000000", m.getReadonlyRow(9).toBitsString());
		assertEquals("00011000 0000000", m.getReadonlyRow(14).toBitsString());
		
		IntIterator iter = m.getRowIntIter(14);
		char[] row = "000110000000000".toCharArray();
		while(iter.hasNext()) {
			int val = iter.next();
			assertTrue("" + val, row[val] == '1');
		}
	}
	
	@Test
	public void testCloning () {
		JavaUtilBitSet b = new JavaUtilBitSet();
		setBits(setBits1, b);
		JavaUtilBitSet b2 = (JavaUtilBitSet) b.clone();
		
		assertEquals(b,b2);
		assertFalse(b == b2);
		
		checkIter(b, setBits1);
		checkIter(b2, setBits1);
		b.flip(1);
		checkIter(b2, setBits1);
	}
	
}

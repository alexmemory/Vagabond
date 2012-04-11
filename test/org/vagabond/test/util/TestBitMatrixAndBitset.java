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
import org.vagabond.util.ewah.EWAHView;
import org.vagabond.util.ewah.IntIterator;

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
	
	public static void setBits(int[] bits, EWAHCompressedBitmap map) {
		for(int i = 0; i < bits.length; i++) {
			map.set(bits[i]);
		}
	}
	
	private void randSetBits(int[] bits, EWAHCompressedBitmap map) {
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
	
	private void checkIter (EWAHCompressedBitmap set, int[] bits, int start, int end) {
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
	
	private void randCheckIter (EWAHCompressedBitmap set, int[] bit) {
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
	public void testIter () {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		setBits(setBits1, bitset);
		
		checkIter(bitset, setBits1);
	}
	
	@Test
	public void testRangeIter () {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		setBits(setBits1, bitset);
		
		checkIter(bitset, setBits1, 6,141);
		
		for(int i = 0; i < 20; i++)
			randCheckIter(bitset, setBits1);
	}
	
	@Test
	public void testEWAHViews () {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		setBits(setBits1, bitset);
		EWAHView view = new EWAHView(bitset, 5, 10);
		EWAHView view2 = new EWAHView(bitset, 5, 7);
		EWAHView view3 = new EWAHView(view, 2, 5);
		
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
	}
	
	
	
}

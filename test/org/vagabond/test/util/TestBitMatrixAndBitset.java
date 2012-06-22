package org.vagabond.test.util;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.util.BitMatrix;
import org.vagabond.util.DynamicBitMatrix;
import org.vagabond.util.ewah.Bitmap;
import org.vagabond.util.ewah.EWAHCompressedBitmap;
import org.vagabond.util.ewah.BitsetView;
import org.vagabond.util.ewah.IBitSet;
import org.vagabond.util.ewah.IntIterator;
import org.vagabond.util.ewah.JavaUtilBitSet;
import org.vagabond.util.ewah.NewEWAHBitmap;
import org.vagabond.util.ewah.RunningLengthWord;

public class TestBitMatrixAndBitset {

	static Logger log = Logger.getLogger(TestBitMatrixAndBitset.class);

	public static final String largevalue =
			"01110001 00000101 01100001 10000011 01000000 10000111 "
					+ "00000000 00010000 01100001 00100001 01000001 00000011 00000001"
					+ " 01000000 00000000 01000000 00000000 10000000 00000000 10000000 "
					+ "00000000 01000000 00000000 01000000 00010000 01000000 0000011";

	static final int[] setBits1 = { 1, 3, 5, 7, 128, 140, 150 };
	static final int[] setBits1a = { 150, 140, 128, 7, 5, 3, 1 };
	static final int[] setBits1b = { 128, 1, 3, 5, 140, 150, 7 };
	static final int[] setBits1c = { 3, 1, 5, 7, 140, 128, 150 };
	static final int[] setBits2 = { 1, 5, 8, 15, 16 };
	static final int[] setBits3 = { 1024, 1026, 1800, 3000 };

	public static String allOnes64;
	public static String allZeros64;
	public static String allOnesExcept12;
	public static String allOnesExcept63;
	public static String oneZeroSeq;
	public static String onlyFirst3Set;

	public static final long maxLiteralOrig =
			RunningLengthWord.largestliteralcount;
	public static final long maxRunLengthOrig =
			RunningLengthWord.largestrunninglengthcount;

	@BeforeClass
	public static void setUp() {
		PropertyConfigurator.configure("resource/test/testLog4jproperties.txt");

		StringBuffer onesB, zerosB, allOnesExcept12B, oneZeroSeqB, onlyFirst3SetB, allOnesExcept63B;
		onesB = new StringBuffer();
		zerosB = new StringBuffer();
		allOnesExcept12B = new StringBuffer();
		oneZeroSeqB = new StringBuffer();
		onlyFirst3SetB = new StringBuffer();
		allOnesExcept63B = new StringBuffer();

		for (int i = 0; i < 64; i++) {
			onesB.append('1');
			zerosB.append('0');
			
			if (i == 12)
				allOnesExcept12B.append('0');
			else
				allOnesExcept12B.append('1');
			
			if (i % 2 == 0)
				oneZeroSeqB.append('1');
			else
				oneZeroSeqB.append('0');

			if (i < 3)
				onlyFirst3SetB.append('1');
			else
				onlyFirst3SetB.append('0');

			if (i == 63)
				allOnesExcept63B.append('0');
			else
				allOnesExcept63B.append('1');
			
			if (i % 8 == 0 && i > 0) {
				onesB.append(' ');
				zerosB.append(' ');
				allOnesExcept12B.append(' ');
				oneZeroSeqB.append(' ');
				allOnesExcept63B.append(' ');
			}
		}

		allZeros64 = zerosB.toString();
		allOnes64 = onesB.toString();
		allOnesExcept12 = allOnesExcept12B.toString();
		oneZeroSeq = oneZeroSeqB.toString();
		onlyFirst3Set = onlyFirst3SetB.toString();
		allOnesExcept63 = allOnesExcept63B.toString();
	}

	public static void setBits(int[] bits, IBitSet map) {
		for (int i = 0; i < bits.length; i++) {
			map.set(bits[i]);
		}
	}

	private void randSetBits(int[] bits, IBitSet map) {
		Random rand = new Random();
		int temp, left, right;
		final int[] mix = new int[bits.length];

		System.arraycopy(bits, 0, mix, 0, bits.length);
		for (int i = 0; i < bits.length * 10; i++) {
			left = rand.nextInt(bits.length);
			right = rand.nextInt(bits.length);
			temp = bits[left];
			bits[left] = bits[right];
			bits[right] = temp;
		}
		setBits(bits, map);
	}

	private void checkSet(int[] bits, EWAHCompressedBitmap set) {
		for (int i = 0; i < bits.length; i++) {
			assertTrue("i: " + bits[i] + "\n" + set.bufferToString(),
					set.intersects(EWAHCompressedBitmap.getSingleton(bits[i])));
			assertTrue("get i: " + bits[i] + "\n" + set.bufferToString(),
					set.get(bits[i]));
		}
	}

	private void checkRows(String[] rows, BitMatrix m) {
		for (int i = 0; i < rows.length; i++) {
			assertEquals(
					"Row " + i + ": " + rows[i] + "\n\n" + m.getReadonlyRow(i),
					rows[i], m.getReadonlyRow(i).toBitsString());
		}
	}

	private void checkIter(Bitmap set, int[] bits) {
		int[] sorted = new int[bits.length];
		IntIterator iter;

		System.arraycopy(bits, 0, sorted, 0, bits.length);
		Arrays.sort(sorted);
		iter = set.intIterator();

		for (int i = 0; i < sorted.length; i++) {
			assertTrue(iter.hasNext());
			assertEquals("" + sorted[i], sorted[i], iter.next());
		}
		assertFalse(iter.hasNext());
	}

	private void checkIter(IBitSet set, int[] bits, int start, int end) {
		int[] sorted = new int[bits.length];
		IntIterator iter;

		System.arraycopy(bits, 0, sorted, 0, bits.length);
		Arrays.sort(sorted);
		iter = set.intIterator(start, end);
		for (int i = 0; i < sorted.length; i++) {
			if (sorted[i] >= start && sorted[i] < end) {
				assertTrue(iter.hasNext());
				assertEquals("" + sorted[i], sorted[i], iter.next());
			}
		}
		assertFalse(iter.hasNext());
	}

	private void randCheckIter(IBitSet set, int[] bit) {
		Random rand = new Random();
		int start, end;

		start = rand.nextInt(bit.length);
		end =
				start
						+ 1
						+ ((start == bit.length - 1) ? 0 : rand
								.nextInt(bit.length - 1 - start));
		checkIter(set, bit, start, end);
	}

	private void assertEqualsBitset(EWAHCompressedBitmap set1,
			EWAHCompressedBitmap set2, String name) {
		assertEquals(
				"variant the bits <" + name + ">:\n\n" + set1.bufferToString()
						+ "\n\n" + set2.bufferToString(), set1.toBitsString(),
				set2.toBitsString());
		assertEquals("variant <" + name + ">:\n\n" + set1.bufferToString()
				+ "\n\n" + set2.bufferToString(), set1, set2);
	}

	@Test
	public void testBitsetGet() {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap(), bitset2;
		setBits(setBits1, bitset);

		checkSet(setBits1, bitset);

		for (int i = 0; i < 100; i++) {
			bitset2 = new EWAHCompressedBitmap();
			randSetBits(setBits1, bitset2);
			checkSet(setBits1, bitset2);
		}
	}

	@Test
	public void testBitSetToBitString() {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		setBits(setBits2, bitset);

		checkSet(setBits2, bitset);
		assertEquals("toBitString", "01000100 10000001 1",
				bitset.toBitsString());
	}

	@Test
	public void testBitSetFromString() {
		String value = "01000100 001";
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap(value);
		assertEquals(value, bitset.toBitsString());

		value =
				"01110001 00000101 01100001 10000011 01000000 10000111 00000000 00010000 01100001 00100001 01000001 00000011 00000001 01000000 00000000 01000000 00000000 10000000 00000000 10000000 00000000 01000000 00000000 01000000 00010000 01000000 0000011";
		bitset = new EWAHCompressedBitmap(value);
		assertEquals(value, bitset.toBitsString());
	}

	@Test
	public void testBitsetSaveSet() {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		EWAHCompressedBitmap bitset2 = new EWAHCompressedBitmap();
		setBits(setBits1, bitset);

		bitset2 = new EWAHCompressedBitmap();
		setBits(setBits1a, bitset2);
		assertEqualsBitset(bitset, bitset2, "a");

		bitset2 = new EWAHCompressedBitmap();
		setBits(setBits1b, bitset2);
		assertEqualsBitset(bitset, bitset2, "b");

		bitset2 = new EWAHCompressedBitmap();
		setBits(setBits1c, bitset2);
		assertEqualsBitset(bitset, bitset2, "c");

		for (int i = 0; i < 100; i++) {
			bitset2 = new EWAHCompressedBitmap();
			randSetBits(setBits1, bitset2);
			assertEqualsBitset(bitset, bitset2, "iteration " + i);
		}
	}

	@Test
	public void testBitsetSaveSet2() {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		EWAHCompressedBitmap bitset2 = new EWAHCompressedBitmap();

		setBits(new int[] { 121, 0 }, bitset);
		setBits(new int[] { 0, 121 }, bitset2);

		assertEqualsBitset(bitset2, bitset, "0-set");
	}

	@Test
	public void testBitsetClone() {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		EWAHCompressedBitmap bitset2;

		setBits(new int[] { 121, 0 }, bitset);
		bitset2 = (EWAHCompressedBitmap) bitset.clone();
		assertEquals(bitset, bitset2);

		bitset2.set(1000);
		assertFalse(bitset.equals(bitset2));
	}

	@Test
	public void testLogicalOperations() {
		EWAHCompressedBitmap b1, b2, ex, res;

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
	public void testManyInserts() {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		Random rand = new Random(0);

		for (int i = 0; i < 10000; i++) {
			int val = rand.nextInt(1000);
			assertTrue("before : " + i + " = " + val, bitset.checkInvariants());
			bitset.set(val);
		}
	}

	@Test
	public void testSetMethodBranches() throws SecurityException,
			IllegalArgumentException, NoSuchFieldException,
			IllegalAccessException {
		EWAHCompressedBitmap b;
		changeMaxLiteralAndRunLength(5, 5);
		// TODO how to create full headers without running into mem problems?
		// modify the max number of literals?

		// ***************************************
		// CASE 1 larger than sizeinbits
		// ***************************************
		// a) finish literal 343 in last literal
		testOneCase("1a",
				replicate(allZeros64, 3, ' ') + replicate(oneZeroSeq, 2, ' ')
						+ onlyFirst3Set, 343);
		
		// b) add new literal to existing header 150 is literal following
		// current one
		testOneCase("1b", oneZeroSeq + onlyFirst3Set, 150);

		// c) add new literal to existing header, but header is full, add new
		// header
		testOneCase("1c",
				replicate(oneZeroSeq, 4, ' ') + onlyFirst3Set, 360);
		
		// d) add new header with 0's
		testOneCase("1d", oneZeroSeq + onlyFirst3Set, 400);

		// e) add more than one header with 0's
		testOneCase("1e", oneZeroSeq, 4000);
		
		// f) turned last literal into 1's sequence
		testOneCase("1f", allOnesExcept63, 63);
		
		// ***************************************
		// CASE 2 inside 1 sequence
		// ***************************************
		// a) one case return without modification
		testOneCase("2", replicate(allOnes64, 3, ' ') + onlyFirst3Set, 90);

		// ***************************************
		// CASE 3 inside literal word
		// ***************************************
		// *********
		// a) just set bit
		testOneCase("3a", replicate(onlyFirst3Set, 3, ' '), 90);

		// *********
		// b) after setting the bit the literal only contains 1's
		// b) 1) was first literal and running bit is 1
		// b) 1) a) current header still has literal and space to increase run
		// length
		testOneCase("3b1a", allOnes64 + allOnesExcept12 + oneZeroSeq, 76);
		// b) 1) b) current header is full
		b = testOneCase("3b1b", replicate(allOnes64, 5, ' ') + allOnesExcept12 + allOnes64, 332);
		log.error(b.toDebugString());
		// b) 1) c) current header still space to increase run length but no
		// literals left
		testOneCase("3b1c", allOnes64 + allOnesExcept12 + allOnes64, 76);

		// *********
		// b) 2) was last literal and following running bit is 1
		// b) 2) a) current header still has literal and next one has space to
		// increase run length
		testOneCase("3b2a", oneZeroSeq + allOnesExcept12 + allOnes64, 76);
		
		// b) 2) b) next header is full
		testOneCase("3b2b", oneZeroSeq + allOnesExcept12 
				+ replicate(allOnes64, 5, ' '), 
				76);
		// b) 2) c) current header has no literals left and next one has space
		// to increase run length
		testOneCase("3b2c", allOnesExcept12 + oneZeroSeq + 
				replicate(allOnes64, 4, ' '), 12);

		// b) 2) d) current header has no literals left but combined run length
		// of current and next header exceeds maximum
		testOneCase("3b2c", replicate(allOnes64, 3, ' ') + allOnesExcept12 + oneZeroSeq + 
				replicate(allOnes64, 4, ' '), 204);
		
		// b) 3) no merging possible because in the middle of literal sequence
		testOneCase("3b3", oneZeroSeq + allOnesExcept12 + oneZeroSeq, 76);

		// ***************************************
		// CASE 4 inside 0 sequence
		// ***************************************
		// **********
		// a) header has no more running length try to merge with previous
		// a) 1) preceding has enough space to take over the literals
		testOneCase("4a1", oneZeroSeq + allZeros64 + allOnesExcept12, 70);
		
		// a) 2) preceding is full, keep this one
		testOneCase("4a2", replicate(oneZeroSeq, 5, ' ') + allZeros64 
				+ allOnesExcept12, 
				350);

		// **********
		// b) header still has running length, we changed the first running length word
		// b) 1) previous has enough space to take over new literal
		testOneCase("4b1", oneZeroSeq + replicate(allZeros64,2,' ') 
				+ allOnesExcept12, 
				70);
		
		// b) 2) previous is full, create new header
		testOneCase("4b2", replicate(oneZeroSeq, 5, ' ') 
				+ replicate(allZeros64,2,' ') + allOnesExcept12, 
				350);
		
		// **********		
		// c) we set bit in last running length word, try to add literal as new
		// first literal of currrent header
		// c) 1)  current header has enough space
		testOneCase("4c1", replicate(allZeros64,2,' ') + allOnesExcept12, 
				70);
		
		// c) 2)  current header is full create new header
		testOneCase("4c2", replicate(allZeros64,2,' ') 
				+ replicate(allOnesExcept12,5,' '), 
				70);
		
		// **********
		// d) current header is full and next one has running length 0 and is not full
		testOneCase("4d", replicate(allZeros64,2,' ') 
				+ replicate(allOnesExcept12,5,' ') + oneZeroSeq, 
				70);
		
		// **********
		// e) no merging or extension of other header possible have to create new
		// header and split 0 sequence
		// e) 1) header is full and the bit to set is in last word of 0 sequence
		// add max literal count to new word (before old one)
		testOneCase("4c2", replicate(allZeros64,2,' ') 
				+ replicate(allOnesExcept12,5,' '), 
				70);		
		
		// e) 2) simple splitting
		testOneCase("4c2", replicate(allZeros64,3,' ') 
				+ allOnesExcept12, 
				70);
		
		changeMaxLiteralAndRunLength(-1, -1);
	}

	private EWAHCompressedBitmap
			testOneCase(String name, String in, int setBit) {
		EWAHCompressedBitmap b;

		JavaUtilBitSet ex;

		b = new EWAHCompressedBitmap(in);
		b2 = new NewEWAHBitmap(in);
		ex = new JavaUtilBitSet(in);
		ex.set(setBit);
		b.set(setBit);
		assertTrue(b.checkInvariants());
		assertEquals(name + "\n\n" + b.toDebugString() 
				+ "\n\n" + ex.toBitsString() 
				+ "\n\n" + b.toBitsString(),
				ex, b);
		log.debug("CASE <" + name + "> sucessful:\n" + b.toDebugString());
		return b;
	}

	private String replicate(String in, int count, char delim) {
		StringBuffer result = new StringBuffer();

		while (count-- > 0)
			result.append(in);

		return result.toString();
	}

	private void changeMaxLiteralAndRunLength(int maxLiteral, int maxRunLen)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		final Field maxLitField =
				getUnfinalizedField(RunningLengthWord.class,
						"largestliteralcount");
		final Field maxRunField =
				getUnfinalizedField(RunningLengthWord.class,
						"largestrunninglengthcount");

		// reset to original values
		if (maxLiteral == -1 && maxRunLen == -1) {
			maxLitField.setLong(null, maxLiteralOrig);
			maxRunField.setLong(null, maxRunLengthOrig);
		}
		// set to new values
		else {
			maxLitField.setLong(null, maxLiteral);
			maxRunField.setLong(null, maxRunLen);
			assert (RunningLengthWord.largestliteralcount == maxLiteral);
			assert (RunningLengthWord.largestrunninglengthcount == maxRunLen);
			log.debug("new values are: "
					+ RunningLengthWord.largestliteralcount + " and "
					+ RunningLengthWord.largestrunninglengthcount);
		}
	}

	private Field getUnfinalizedField(Class clazz, String fieldName)
			throws IllegalArgumentException, IllegalAccessException,
			SecurityException, NoSuchFieldException {
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		Field field = clazz.getDeclaredField(fieldName);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		return field;
	}

	@Test
	public void testIter() {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		setBits(setBits1, bitset);

		checkIter(bitset, setBits1);

		JavaUtilBitSet b2 = new JavaUtilBitSet();
		setBits(setBits1, b2);

		checkIter(b2, setBits1);
	}

	@Test
	public void testRangeIter() {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		setBits(setBits1, bitset);

		checkIter(bitset, setBits1, 6, 141);

		for (int i = 0; i < 20; i++)
			randCheckIter(bitset, setBits1);

		JavaUtilBitSet bitset2 = new JavaUtilBitSet();
		setBits(setBits1, bitset2);

		checkIter(bitset2, setBits1, 6, 141);

		for (int i = 0; i < 20; i++)
			randCheckIter(bitset2, setBits1);
	}

	@Test
	public void testEWAHViews() {
		EWAHCompressedBitmap bitset = new EWAHCompressedBitmap();
		setBits(setBits1, bitset);
		BitsetView view = new BitsetView(bitset, 5, 10);
		BitsetView view2 = new BitsetView(bitset, 5, 7);
		BitsetView view3 = new BitsetView(view, 2, 5);

		checkIter(view, new int[] { 0, 2 });
		checkIter(view2, new int[] { 0 });
		checkIter(view3, new int[] { 0 });

		assertEquals(view.toBitsString(), "10100", view.toBitsString());
		assertEquals(view2.toBitsString(), "10", view2.toBitsString());
		assertEquals(view3.toBitsString(), "100", view3.toBitsString());
	}

	@Test
	public void testJavaBitsetViews() {
		JavaUtilBitSet bitset = new JavaUtilBitSet();
		setBits(setBits1, bitset);
		BitsetView view = new BitsetView(bitset, 5, 10);
		BitsetView view2 = new BitsetView(bitset, 5, 7);
		BitsetView view3 = new BitsetView(view, 2, 5);

		checkIter(view, new int[] { 0, 2 });
		checkIter(view2, new int[] { 0 });
		checkIter(view3, new int[] { 0 });

		assertEquals(view.toBitsString(), "10100", view.toBitsString());
		assertEquals(view2.toBitsString(), "10", view2.toBitsString());
		assertEquals(view3.toBitsString(), "100", view3.toBitsString());
	}

	@Test
	public void testMatrix() {
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

		checkRows(new String[] { "00", "11", "01" }, m);

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
		while (iter.hasNext()) {
			int val = iter.next();
			assertTrue("" + val, row[val] == '1');
		}
	}
	
	@Test
	public void testDynamicBitmatrix () {
		DynamicBitMatrix m = new DynamicBitMatrix();

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

		log.debug(m.getBitmap().toBitsString());
		
		checkRows(new String[] { "00", "11", "01" }, m);

		assertEquals("first one in col 0", 1, m.firstOneInCol(0));
		assertEquals("first one in col 1", 1, m.firstOneInCol(1));

		assertEquals("first one in row 0", -1, m.firstOneInRow(0));
		assertEquals("first one in row 1", 0, m.firstOneInRow(1));
		assertEquals("first one in row 2", 1, m.firstOneInRow(2));

		m = new DynamicBitMatrix(15, 15, largevalue);
		assertEquals("01110001 0000010", m.getReadonlyRow(0).toBitsString());
		assertEquals("10110000 1100000", m.getReadonlyRow(1).toBitsString());
		assertEquals("01000000 0000000", m.getReadonlyRow(9).toBitsString());
		assertEquals("00011000 0000000", m.getReadonlyRow(14).toBitsString());

		assertEquals(1, m.firstOneInRow(0));
		assertEquals(1, m.firstOneInRow(9));
		assertEquals(3, m.firstOneInRow(14));
		
		assertEquals(0, m.firstOneInCol(1));
		assertEquals(1, m.firstOneInCol(8));
		
		IntIterator iter = m.getRowIntIter(14);
		char[] row = "000110000000000".toCharArray();
		while (iter.hasNext()) {
			int val = iter.next();
			assertTrue("" + val, row[val] == '1');
		}		
	}

	@Test
	public void testCloning() {
		JavaUtilBitSet b = new JavaUtilBitSet();
		setBits(setBits1, b);
		JavaUtilBitSet b2 = (JavaUtilBitSet) b.clone();

		assertEquals(b, b2);
		assertFalse(b == b2);

		checkIter(b, setBits1);
		checkIter(b2, setBits1);
		b.flip(1);
		checkIter(b2, setBits1);
	}

}

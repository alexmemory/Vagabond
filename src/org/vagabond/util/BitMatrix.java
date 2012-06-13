package org.vagabond.util;

import java.util.Iterator;

import org.vagabond.util.ewah.Bitmap;
import org.vagabond.util.ewah.BitsetFactory;
import org.vagabond.util.ewah.EWAHCompressedBitmap;
import org.vagabond.util.ewah.BitsetView;
import org.vagabond.util.ewah.IBitSet;
import org.vagabond.util.ewah.IBitSet.BitsetType;
import org.vagabond.util.ewah.IntIterator;


public class BitMatrix {

	private static final int DEFAULT_BUFFER = 0;
	protected static final BitsetType DEFAULT_BITSET_TYPE = BitsetType.JavaBitSet;
	protected IBitSet bitmap;
	protected int rows;
	protected int cols;
	
	protected BitMatrix () {
		bitmap = BitsetFactory.newBitset(DEFAULT_BITSET_TYPE);
	}
	
	protected BitMatrix (BitsetType type) {
		bitmap = BitsetFactory.newBitset(type);
	}
	
	public BitMatrix (int rows, int cols) {
		this(rows,cols,DEFAULT_BUFFER, DEFAULT_BITSET_TYPE);		
	}
	
	public BitMatrix (final int rows, final int cols, final BitsetType bitType) { 
		this(rows, cols, DEFAULT_BUFFER, bitType);
	}
	
	public BitMatrix (final int rows, final int cols, final String values) {
		this (rows, cols, values, DEFAULT_BITSET_TYPE);
	}
	
	public BitMatrix (final int rows, final int cols, final String values, final BitsetType bitType) {
		this.rows = rows;
		this.cols = cols;
		bitmap = BitsetFactory.newBitset(bitType, values);
	}
	
	public BitMatrix (final int rows, final int cols, final int bytesInBuf, final BitsetType bitType) {
		this.rows = rows;
		this.cols = cols;
		
		if (bytesInBuf == 0)
			bitmap = BitsetFactory.newBitset(bitType);
		else
			bitmap = BitsetFactory.newBitset(bitType, bytesInBuf);
	}
	
	public boolean get (int row, int col) {
		return bitmap.get(row *cols + col);
	}
	
	
	
	public void setSym (int row, int col) {
		set(row, col);
		set(col, row);
	}
	
	public void set (int row, int col) {
		bitmap.set(row * cols + col);
	}
	
	public int numOnesInRow (int row) {
		IntIterator iter = getReadonlyRow(row).intIterator();
		int count = 0;
		
		for(;iter.hasNext(); iter.next())
			return count++;
		return count;
	}
	
	public int numOnes () {
		return bitmap.cardinality();
	}
	
	public int numOnesInCol (int col) {
		int count = 0;
		for(int i = 0; i < rows; i++) {
			if (bitmap.get(i * cols + col))
				count++;
		}
		return count;
	}
	
	public int firstOneInCol (int col) {
		for(int i = 0; i < rows; i++) {
			if (bitmap.get(i * cols + col))
				return i;
		}
		return -1;
	}
	
	public int firstOneInRow (int row) {
		IntIterator iter = getReadonlyRow(row).intIterator();
		
		if (iter.hasNext())
			return iter.next();
		return -1;
	}
	
	public Bitmap getReadonlyRow (int rowNum) {
		return new BitsetView(bitmap, rowNum * cols, (rowNum + 1) * cols);
	}
	
	public Iterator<Integer> getRowIter (int rowNum) {
		return getReadonlyRow(rowNum).iterator();
	}
	
	public IntIterator getRowIntIter (int rowNum) {
		return getReadonlyRow(rowNum).intIterator();
	}

	@Override
	public String toString () {
		StringBuffer buf = new StringBuffer();
		
		buf.append("Bitmap [" + rows + "," + cols + "]\n");
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (get(i,j))
					buf.append('1');
				else
					buf.append('0');
			}
			buf.append("\n");
		}
		
		return buf.toString();
	}
	
	@Override
	public boolean equals (Object o) {
		if (o == null)
			return false;
		
		if (o == this)
			return true;
		
		if (o instanceof BitMatrix) {
			BitMatrix other = (BitMatrix) o;
			return rows == other.rows && cols == other.cols 
					&& this.bitmap.equals(other.bitmap);
		}
		
		return false;
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

	public IBitSet getBitmap() {
		return bitmap;
	}
}

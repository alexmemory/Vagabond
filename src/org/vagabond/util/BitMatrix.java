package org.vagabond.util;

import java.util.Iterator;

import org.vagabond.util.ewah.Bitmap;
import org.vagabond.util.ewah.EWAHCompressedBitmap;
import org.vagabond.util.ewah.EWAHView;
import org.vagabond.util.ewah.IntIterator;


public class BitMatrix {

	private static final int DEFAULT_BUFFER = 0;
	
	private EWAHCompressedBitmap bitmap;
	private int rows, cols;
	
	public BitMatrix (int rows, int cols) {
		this(rows,cols,DEFAULT_BUFFER);		
	}
	
	public BitMatrix (int rows, int cols, String values) {
		this.rows = rows;
		this.cols = cols;
		bitmap = new EWAHCompressedBitmap(values);
	}
	
	public BitMatrix (final int rows, final int cols, final int bitBuf) {
		this.rows = rows;
		this.cols = cols;
		
		if (bitBuf == 0)
			bitmap = new EWAHCompressedBitmap();
		else
			bitmap = new EWAHCompressedBitmap(bitBuf / 64);
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
		return new EWAHView(bitmap, rowNum * cols, (rowNum + 1) * cols);
	}
	
	public Iterator<Integer> getRowIter (int rowNum) {
		return bitmap.iterator(rowNum * cols, (rowNum + 1) * cols);
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

	public EWAHCompressedBitmap getBitmap() {
		return bitmap;
	}
}

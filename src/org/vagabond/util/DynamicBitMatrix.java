package org.vagabond.util;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.log4j.Logger;
import org.vagabond.util.ewah.IBitSet.BitsetType;
import org.vagabond.util.ewah.Bitmap;
import org.vagabond.util.ewah.IntIterator;
import org.vagabond.util.ewah.PositionTranslator;
import org.vagabond.util.ewah.TranslatedBitsetView;

public class DynamicBitMatrix extends BitMatrix {

	static Logger log = Logger.getLogger(DynamicBitMatrix.class);
	
	private IntList elemOffset;
	
	public DynamicBitMatrix() {
		this(DEFAULT_BITSET_TYPE);
	}
	
	public DynamicBitMatrix(BitsetType type) {
		super(type);
		elemOffset = new ArrayIntList();
		elemOffset.add(0);
		rows = 0;
		cols = 0;
	}
	
	public DynamicBitMatrix(int rows, int cols) {
		this(DEFAULT_BITSET_TYPE);
		this.rows = rows;
		this.cols = cols;
	}
	
	public DynamicBitMatrix(int rows, int cols, String values) {
		this();
		BitMatrix b = new BitMatrix(rows, cols, values);
		
		for(int i = 0; i < rows; i++) {
			IntIterator iter = b.getRowIntIter(i);
			while(iter.hasNext()) {
				this.set(i, iter.next());
			}
		}
	}
	
	@Override
	public boolean get (int row, int col) {
		return bitmap.get(translate(row, col));
	}
	
	@Override
	public int firstOneInCol (int col) {
		for(int i = 0; i < rows; i++) {
			if (get(i, col))
				return i;
		}
		return -1;
	}
	
	@Override
	public int numOnesInCol (int col) {
		int count = 0;
		for(int i = 0; i < rows; i++) {
			if (get(i, col))
				count++;
		}
		return count;
	}

	@Override
	public Bitmap getReadonlyRow (int rowNum) {
		return new TranslatedBitsetView(bitmap, rowNum * cols, (rowNum + 1) * cols, 
				new PositionTranslator() {
	
			@Override
			public int translateToBitpos(int in) {
				int row = in / cols;
				int col = in % cols;
				if (log.isDebugEnabled()) {log.debug("from in <" + in + "> to r:<" + row + ">, c:<" + col + ">");};
				return translate(row, col);
			}
			
		});
	}

	
	@Override
	public void set (int row, int col) {
		int max = (row > col) ? row : col;
		if (rows <= row)
			rows = row + 1;
		if (cols <= col)
			cols = col + 1;
		if (log.isDebugEnabled()) {log.debug("new dim <" + rows + "," + cols + ">");};
		bitmap.set(translate(row, col));
	}

	private int translate (int row, int col) {
		int max = (row > col) ? row : col;
		ensureOffsetFor(max);
		int offset = elemOffset.get(max);
		
		if (row >= col)
			offset += col;
		else
			offset += col + row + 1;
		if (log.isDebugEnabled()) {log.debug("translated r:<" + row + "> c:<" + col + "> to <" + offset + ">");};
		
		return offset;
	}

	private void ensureOffsetFor(int max) {
		while(elemOffset.size() <= max) {
			int curMaxEl = elemOffset.size() - 1;
			int newOffset = elemOffset.get(curMaxEl) + (2 * curMaxEl) + 1;
			if (log.isDebugEnabled()) {log.debug("add new offset for <" + (curMaxEl + 1) + "> is <" + newOffset + ">");};
			elemOffset.add(newOffset);
		}
	}
	
}

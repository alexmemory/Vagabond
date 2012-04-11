package org.vagabond.util.ewah;

public class OffsetRangeEWAHintIterator extends BufferedRangeEWAHintIterator {

	private int offset; 
	
	public OffsetRangeEWAHintIterator(EWAHCompressedBitmap map, int start,
			int end, int offset) {
		super(map, start, end);
		this.offset = offset;
	}

	@Override
	public int next() {
		return super.next() - offset;
	}
}

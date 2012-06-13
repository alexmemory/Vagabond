package org.vagabond.util.ewah;

import java.util.NoSuchElementException;

public class TranslatedBitsetView extends BitsetView {

	private PositionTranslator trans;
	
	public TranslatedBitsetView (IBitSet map, int start, int end, PositionTranslator trans) {
		super(map, start, end);
		this.trans = trans;
	}
	
	public TranslatedBitsetView (BitsetView v, int start, int end, PositionTranslator trans) {
		super(v, start, end);
		this.trans = trans;
	}
	
	
	@Override
	public boolean get(int bitpos) {
		return map.get(trans.translateToBitpos(start + bitpos));
	}

	@Override
	public IntIterator intIterator() {
		return new IntIterator () {

			private int cur = start; 
			private int[] buf = new int[1024];
			private int bufSize = 0;
			private int bufPos = 0;
			
			@Override
			public int next() {
				if (hasNext())
					return buf[bufPos++];
				return -1;
			}

			@Override
			public boolean hasNext() {
				if (bufPos >= bufSize)
					fillBuffer();
				return bufPos < bufSize;
			}
			
			private void fillBuffer() {
				while(cur < end && bufSize < buf.length - 1) {
					int transCur = trans.translateToBitpos(cur++);
					if(map.get(transCur))
						buf[bufSize++] = cur - start - 1;
				}
				if (bufPos == buf.length - 1 && bufSize != 0)
					bufPos = 0;
			}
			
		};
	}

	
	
}

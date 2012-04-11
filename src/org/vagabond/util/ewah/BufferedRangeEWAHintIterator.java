package org.vagabond.util.ewah;

public class BufferedRangeEWAHintIterator extends BufferedEWAHintIterator {

	private final int start;
	private final int end;
	
	public BufferedRangeEWAHintIterator(EWAHCompressedBitmap map, final int start, final int end) {
		i = new EWAHIterator(map.buffer, map.actualsizeinwords);
		this.start = start;
		this.end = end;
		loadBufferFirst();
		this.status = queryStatus();
	}
	
	private void loadBufferFirst() {
		// skip until we found RLW with start bit
		loadNextRLE();
		while(pos + localrlw.size() * 64 < start && loadNextRLE())
			pos += localrlw.size() * 64;
		
		// start to add ones from run length?
		if (this.localrlw.getRunningLength() + pos > start && this.localrlw.getRunningBit()) {
			int addones = (int) (start - (this.localrlw.getRunningLength() + pos));
			pos = start;
			for(int i = 0; i < addones; i++)
				add(this.pos++);
		}
		else {
			this.pos += this.localrlw.getRunningLength();
		}
		
		// add literals
		for (int j = 0; j < this.localrlw.getNumberOfLiteralWords(); ++j) {
			final long data = i.buffer()[i.dirtyWords() + j];
			// words that are completely after start are added fully
			if (pos > start) {
				for (long c = 0; c < EWAHCompressedBitmap.wordinbits; ++c) {
					if (((1l << c) & data) != 0)
						add(this.pos);
					++this.pos;
				}
			} 
			// start position is inside current word
			else if (! (pos + EWAHCompressedBitmap.wordinbits < start)) {
				pos = start;
				for (long c = start % 64; c < EWAHCompressedBitmap.wordinbits; ++c) {
					if (((1l << c) & data) != 0)
						add(this.pos);
					++this.pos;
				}
			}
			// completely before start, skip word
			else
				this.pos += EWAHCompressedBitmap.wordinbits;
		}
	}
	
	@Override
	protected boolean queryStatus() {
		return super.queryStatus() && this.localbuffer[this.bufferpos] < end;
	}
	
	@Override
	public int next() {
		final int answer = super.next();
		
		if (this.localbuffer[this.bufferpos] >= end)
			this.status = queryStatus();
		
		return answer;
	}

}

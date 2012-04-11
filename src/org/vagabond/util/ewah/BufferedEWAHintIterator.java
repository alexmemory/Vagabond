package org.vagabond.util.ewah;

public class BufferedEWAHintIterator implements IntIterator {

		int pos = 0;
		RunningLengthWord localrlw = null;
		final static int initcapacity = 512;
		int[] localbuffer = new int[initcapacity];
		int localbuffersize = 0;
		int bufferpos = 0;
		boolean status;
		EWAHIterator i;
		
		protected BufferedEWAHintIterator () {
		}
		
		public BufferedEWAHintIterator (EWAHCompressedBitmap map) {
			i = new EWAHIterator(map.buffer, map.actualsizeinwords);
			status = queryStatus();
		}
		
		@Override
		public boolean hasNext() {
			return this.status;
		}

		protected boolean queryStatus() {
			while (this.localbuffersize == 0) {
				if (!loadNextRLE())
					return false;
				loadBuffer();
			}
			return true;
		}

		protected boolean loadNextRLE() {
			while (i.hasNext()) {
				this.localrlw = i.next();
				return true;
			}
			return false;
		}

		protected void add(final int val) {
			++this.localbuffersize;
			while (this.localbuffersize > this.localbuffer.length) {
				int[] oldbuffer = this.localbuffer;
				this.localbuffer = new int[this.localbuffer.length * 2];
				System.arraycopy(oldbuffer, 0, this.localbuffer, 0, oldbuffer.length);
			}
			this.localbuffer[this.localbuffersize - 1] = val;
		}

		private void loadBuffer() {
			this.bufferpos = 0;
			this.localbuffersize = 0;
			if (this.localrlw.getRunningBit()) {
				for (int j = 0; j < this.localrlw.getRunningLength(); ++j) {
					for (int c = 0; c < EWAHCompressedBitmap.wordinbits; ++c) {
						add(this.pos++);
					}
				}
			} else {
				this.pos += EWAHCompressedBitmap.wordinbits * this.localrlw.getRunningLength();
			}
			for (int j = 0; j < this.localrlw.getNumberOfLiteralWords(); ++j) {
				final long data = i.buffer()[i.dirtyWords() + j];
				for (long c = 0; c < EWAHCompressedBitmap.wordinbits; ++c) {
					if (((1l << c) & data) != 0) {
						add(this.pos);
					}
					++this.pos;
				}
			}
		}

		@Override
		public int next() {
			final int answer = this.localbuffer[this.bufferpos++];
			if (this.localbuffersize == this.bufferpos) {
				this.localbuffersize = 0;
				this.status = queryStatus();
			}
			return answer;
		}
}

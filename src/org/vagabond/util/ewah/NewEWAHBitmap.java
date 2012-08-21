package org.vagabond.util.ewah;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.vagabond.util.LoggerUtil;

public class NewEWAHBitmap extends EWAHCompressedBitmap {

	static Logger log = Logger.getLogger(NewEWAHBitmap.class);

	private long[] CompressPosRep;
	private long[] UnCompressPosRep;
	private int CompressUsedWord;
	private int UnCompressUsedWords;

	public NewEWAHBitmap() {
		super();
		Init();
	}

	public NewEWAHBitmap(String in) {
		this.buffer = new long[defaultbuffersize];
		this.rlw = new RunningLengthWord(this.buffer, 0);
		Init();
		readFromBitsString(in);
	}

	public void Init() {
		CompressPosRep = new long[buffer.length];
		UnCompressPosRep = new long[buffer.length];
		CompressPosRep[0] = 0;
		UnCompressPosRep[0] = 0;
		CompressUsedWord = 1;
		UnCompressUsedWords = 1;
	}

	public long[] getCompressRep() {
		return CompressPosRep;
	}

	public long[] getUnCompressRep() {
		return UnCompressPosRep;
	}

	@Override
	protected void xor(EWAHCompressedBitmap a, BitmapStorage container) {
		super.xor(a, container);
		((NewEWAHBitmap) container).createHeaderMapping();

	}

	@Override
	public EWAHCompressedBitmap xor(final EWAHCompressedBitmap a) {
		final NewEWAHBitmap container = new NewEWAHBitmap();
		container.reserve(this.actualsizeinwords + a.actualsizeinwords);
		xor(a, container);
		return container;
	}

	@Override
	protected void and(EWAHCompressedBitmap a, BitmapStorage container) {
		super.and(a, container);
		((NewEWAHBitmap) container).createHeaderMapping();
	}

	@Override
	public EWAHCompressedBitmap and(final EWAHCompressedBitmap a) {
		final NewEWAHBitmap container = new NewEWAHBitmap();
		container
				.reserve(this.actualsizeinwords > a.actualsizeinwords ? this.actualsizeinwords
						: a.actualsizeinwords);
		and(a, container);
		return container;
	}

	@Override
	protected void andNot(EWAHCompressedBitmap a, BitmapStorage container) {
		super.andNot(a, container);
		((NewEWAHBitmap) container).createHeaderMapping();
	}

	public EWAHCompressedBitmap andNot(final EWAHCompressedBitmap a) {
		final NewEWAHBitmap container = new NewEWAHBitmap();
		container
				.reserve(this.actualsizeinwords > a.actualsizeinwords ? this.actualsizeinwords
						: a.actualsizeinwords);
		andNot(a, container);
		return container;
	}

	@Override
	public void not() {
		super.not();
		((NewEWAHBitmap) this).createHeaderMapping();
	}

	@Override
	protected void or(EWAHCompressedBitmap a, BitmapStorage container) {
		super.or(a, container);
		((NewEWAHBitmap) container).createHeaderMapping();
	}

	@Override
	public EWAHCompressedBitmap or(final EWAHCompressedBitmap a) {
		final NewEWAHBitmap container = new NewEWAHBitmap();
		container.reserve(this.actualsizeinwords + a.actualsizeinwords);
		or(a, container);
		return container;
	}

	// TODO the classes called these function will handle the array problem
	// @Override
	// protected void push_back(final long data) {
	// int beforePos = rlw.position;
	// super.push_back(data);
	// if (rlw.position != beforePos) {
	// checkArraySize();
	// CompressPosRep[usedwords] = rlw.position;
	// UnCompressPosRep[usedwords] = createWordRepOffset(usedwords);
	// usedwords++;
	// }
	// }
	// @Override
	// protected void push_back(final long[] data, final int start,
	// final int number) {
	// super.push_back(data, start, number);
	// ((NewEWAHBitmap) this).createHeaderMapping();
	// }
	//
	// @Override
	// protected void negative_push_back(final long[] data, final int start,
	// final int number) {
	// while (this.actualsizeinwords + number >= this.buffer.length) {
	// final long oldbuffer[] = this.buffer;
	// this.buffer = new long[oldbuffer.length * 2];
	// System.arraycopy(oldbuffer, 0, this.buffer, 0, oldbuffer.length);
	// this.rlw.array = this.buffer;
	// }
	// int beforePos = rlw.position;
	// for (int k = 0; k < number; ++k){
	// this.buffer[this.actualsizeinwords + k] = ~data[start + k];
	// if (rlw.position != beforePos) {
	// checkArraySize();
	// CompressPosRep[usedwords] = rlw.position;
	// UnCompressPosRep[usedwords] = createWordRepOffset(usedwords);
	// usedwords++;
	// beforePos = rlw.position;
	// }
	// }
	// this.actualsizeinwords += number;
	// }

	// @Override
	// public void shiftCompressedWordsLeft(final int startWord, final int
	// shift) {
	// super.shiftCompressedWordsLeft(startWord, shift);
	// ((NewEWAHBitmap) this).createHeaderMapping();
	// }
	//
	// @Override
	// public void shiftCompressedWordsRight(final int startWord, final int
	// shift) {
	// super.shiftCompressedWordsRight(startWord, shift);
	// ((NewEWAHBitmap) this).createHeaderMapping();
	// }

	// @Override
	// public int addStreamOfEmptyWords(boolean v, long number) {
	// return super.addStreamOfEmptyWords(v, number);
	// }
	//
	// @Override
	// public long addStreamOfNegatedDirtyWords(long[] data, long start,
	// long number) {
	// return super.addStreamOfNegatedDirtyWords(data, start, number);
	// }
	//
	// @Override
	// public long addStreamOfDirtyWords(long[] data, long start, long number) {
	// return super.addStreamOfDirtyWords(data, start, number);
	// }
	// @Override
	// public int add(final long newdata, final int bitsthatmatter) {
	// return super.add(newdata, bitsthatmatter);
	// }

	@Override
	public boolean fastSet(final int i) {
		if (!super.fastSet(i))
			return false;
		
		RunningLengthWord rlw = new RunningLengthWord(this.buffer,
				(int) CompressPosRep[CompressUsedWord - 1]);
		long currentsize = UnCompressPosRep[UnCompressUsedWords - 1];
		rlw.next();

		// while current header position does not includes i.
		while (rlw.position < actualsizeinwords && rlw.position + rlw.size() < actualsizeinwords) {
			checkArraySize();
			currentsize += rlw.size();
			CompressPosRep[CompressUsedWord] = rlw.position;
			UnCompressPosRep[UnCompressUsedWords] = currentsize;
			CompressUsedWord++;
			UnCompressUsedWords++;
			rlw.next();
		}
		
		return true;
	}

	@Override
	public boolean get(int bitpos) {
		if (bitpos >= sizeinbits)
			return false;
		int arrayBitPos = getHeaderIndex(bitpos);
		int bufferIndex = (int) CompressPosRep[arrayBitPos];
		RunningLengthWord rlw = new RunningLengthWord(this.buffer, bufferIndex);

		int wordpos = 1;
		int currentWordBitPos = bitpos - (int) UnCompressPosRep[arrayBitPos];

		if (currentWordBitPos < rlw.getRunningLength() * 64)
			return rlw.getRunningBit();

		currentWordBitPos -= rlw.getRunningLength() * 64;
		wordpos += currentWordBitPos / 64;
		long bit = 1L << (currentWordBitPos % 64);
		return (this.buffer[rlw.position + wordpos] & bit) != 0L;

	}

	@Override
	public void clear() {
		super.clear();
		CompressPosRep[0] = 0;
		UnCompressPosRep[0] = 0;
		CompressUsedWord = 1;
		UnCompressUsedWords = 1;

	}

	@Override
	public EWAHCompressedBitmap slice(final int start, final int length) {
		EWAHCompressedBitmap rawResult = super.slice(start, length);
		NewEWAHBitmap result = new NewEWAHBitmap();
		result.buffer = rawResult.buffer;
		result.rlw = rawResult.rlw;
		result.createHeaderMapping();
		return (EWAHCompressedBitmap) result;
	}

	@Override
	public void setRange(final boolean value, int start, int end) {
		super.setRange(value, start, end);
		((NewEWAHBitmap) this).createHeaderMapping();
	}

	@Override
	public boolean setSizeInBits(int size, boolean defaultvalue) {
		boolean result = super.setSizeInBits(size, defaultvalue);
		((NewEWAHBitmap) this).createHeaderMapping();
		return result;
	}

	@Override
	public void set(final int i) {
		int wordPos;
		int bitPos = 0;
		int offset;

		assert (i >= 0);

		// try simple set (append style)
		if (fastSet(i))
			return;

		// simple set failed. Find compressed word where to set the bit
		RunningLengthWord rlw = null, prev = null, next = null;

		EWAHIterator iter = new EWAHIterator(this.buffer, actualsizeinwords);
		while (iter.hasNext() && (bitPos < i || bitPos == 0)) {
			if (rlw != null)
				prev = new RunningLengthWord(rlw);
			rlw = iter.next();
			bitPos += rlw.size() * wordinbits;
		}
		if (iter.hasNext())
			next = rlw.getNext();

		bitPos -= rlw.size() * 64;
		offset = i - bitPos;
		wordPos = rlw.position;

		// the bit to set is in a literal word. Try to set bit directly
		if (offset >= rlw.getRunningLength() * 64) {
			offset -= rlw.getRunningLength() * 64;
			int literalPos = (offset / 64);
			wordPos += literalPos + 1;
			final long newdata = 1l << (offset % 64);
			this.buffer[wordPos] = this.buffer[wordPos] | newdata;
			// if all bits of literal set, then either merge with run length
			// or create new RLW.
			if (this.buffer[wordPos] == oneMask) {
				// first literal of current RLW and running bit is set
				// increase count by one (unless maximal count reached)
				if (literalPos == 0
						&& (rlw.getRunningBit() || rlw.getRunningLength() == 0)
						&& rlw.getRunningLength() < RunningLengthWord.largestrunninglengthcount) {
					int bufferdeletepos = -1;
					if (next != null)
						bufferdeletepos = next.position;
					rlw.setRunningBit(true);
					rlw.setRunningLength(rlw.getRunningLength() + 1);
					rlw.setNumberOfLiteralWords(rlw.getNumberOfLiteralWords() - 1);
					// case 4: HLL -> HL
					// comp: [0, 3...] -> [0, 2...]
					// uncomp: [0, 4..] -> [0, 4...]
					
					if (bufferdeletepos != -1) {
						int deletepos = findCompresIndexByBufferIndex(bufferdeletepos);
						shiftCompIndexToLeft(deletepos, 0, -1);
					}

					// current RLW has no literals left, try to merge with
					// following RLW
					if (rlw.getNumberOfLiteralWords() == 0
							&& next != null
							&& next.getRunningBit()
							&& next.getRunningLength() + rlw.getRunningLength() < RunningLengthWord.largestrunninglengthcount) {
						bufferdeletepos = rlw.position;
						int nextBufferDeletePos = next.position;
						// if (rlw.equals(this.rlw))
						// this.rlw.position = next.position;
						next.setRunningLength(next.getRunningLength()
								+ rlw.getRunningLength());
						shiftCompressedWordsLeft(rlw.position + 2, 2);
						// case 6 literal merge to front: HLHL -> HL
						// comp: [0, 2, 4...] -> [0, 2...]
						// uncomp: [0, 3, 6..] -> [0, 6...]
						
						int deletepos = findCompresIndexByBufferIndex(bufferdeletepos);
						int uncompDeletepos = findCompresIndexByBufferIndex(nextBufferDeletePos);
						shiftCompIndexToLeft(deletepos, 1, -2);
						shiftUnCompIndexToLeft(uncompDeletepos, 1, 0);
					}
					// else just reduce length of rlw by 1
					else
						shiftCompressedWordsLeft(rlw.position + 2, 1);

				}
				// if last word increase following running length count if
				// possible
				else if (next != null
						&& next.getRunningLength() < RunningLengthWord.largestrunninglengthcount
						&& (next.getRunningLength() == 0 || next
								.getRunningBit())
						&& literalPos == rlw.getNumberOfLiteralWords() - 1) {
					int nextBufferDeletePos = next.position;
					next.setRunningBit(true);
					next.setRunningLength(next.getRunningLength() + 1);
					rlw.setNumberOfLiteralWords(rlw.getNumberOfLiteralWords() - 1);
					// case 5: HLLH -> HLH
					// comp: [0, 3, 5...] -> [0, 2, 4...]
					// uncomp: [0, 4, 7..] -> [0, 3, 7...]
					int deletepos = findCompresIndexByBufferIndex(nextBufferDeletePos);
					shiftCompIndexToLeft(deletepos, 0, -1);
					UnCompressPosRep[deletepos] -= 1;
					
					// current RLW has no literals left, try to merge with
					// following
					if (rlw.getNumberOfLiteralWords() == 0
							&& next != null
							&& next.getRunningBit()
							&& next.getRunningLength() < RunningLengthWord.largestrunninglengthcount) {
						if (rlw.equals(this.rlw))
							this.rlw.position = next.position;
						int bufferdeletepos = rlw.position;
						nextBufferDeletePos = next.position;
						next.setRunningLength(next.getRunningLength()
								+ rlw.getRunningLength());
						shiftCompressedWordsLeft(rlw.position + 2, 2);
						// case 6 leteral merge to back: HLHL -> HL
						// comp: [0, 2, 4...] -> [0, 2...]
						// uncomp: [0, 3, 6..] -> [0, 6...]
						deletepos = findCompresIndexByBufferIndex(bufferdeletepos);
						int uncompDeletepos = findCompresIndexByBufferIndex(nextBufferDeletePos);
						shiftCompIndexToLeft(deletepos, 1, -2);
						shiftUnCompIndexToLeft(uncompDeletepos, 1, 0);

					} else
						shiftCompressedWordsLeft(
								rlw.position + rlw.getNumberOfLiteralWords()
										+ 2, 1);

				}
				// cannot merge, have to create new RLW and adapt literal count
				// of current RLW
				else {
					int nextBufferdeletepos = -1;
					if (next != null)
						nextBufferdeletepos = next.position;
					
					
					int beforeLit = literalPos;
					int afterLit = rlw.getNumberOfLiteralWords() - literalPos
							- 1;

					log.debug("split into " + beforeLit + " and " + afterLit);

					RunningLengthWord newRlw = new RunningLengthWord(rlw);
					newRlw.position += literalPos + 1;
					newRlw.setRunningBit(true);
					newRlw.setRunningLength(1L);
					newRlw.setNumberOfLiteralWords(afterLit);

					rlw.setNumberOfLiteralWords(beforeLit);
					// case 7: HLLLH -> HLHLH
					// comp: [0, 4...] -> [0, 2, 4...]
					// uncomp: [0, 5..] -> [0, 3, 5...]
					
					if (nextBufferdeletepos != -1){
						int movingPos = findCompresIndexByBufferIndex(nextBufferdeletepos);
						shiftCompIndexToRight(movingPos, 1, 0);
						long newComp = (movingPos == 0) ? 0 : CompressPosRep[movingPos - 1]; 
						CompressPosRep[movingPos] = newComp + literalPos;
						shiftUncompIndexToRight(movingPos, 1, 0);
						UnCompressPosRep[movingPos] = rlw.getRunningLength() + literalPos;
					}

					// if next one is full running length 1's we have to switch
					// running lengths
					if (next != null
							&& next.getRunningBit()
							&& next.getRunningLength() == RunningLengthWord.largestrunninglengthcount) {
						next.setRunningLength(1L);
						newRlw.setRunningLength(RunningLengthWord.largestrunninglengthcount);
					}
					// we split the last word, adapt it
					if (rlw.position == this.rlw.position)
						this.rlw.position = newRlw.position;
					log.debug("split because new 1's run");
				}
			}
		}
		// bit is in a clean word, if it is a '1' sequence we are fine
		else if (rlw.getRunningBit()) {
			return;
		}
		// bit to set is in '0' clean word. We have to split this clean word and
		// shift following words in the buffer.
		// We do this by adding a new RLW y after the RLW x which has to be
		// split. This new RWL y takes over all the
		// literal words of x and encodes the part of the 0 sequence that
		// follows the bit we want to set. RWL x's
		// run length is reduced and we add the new bit as the new single
		// literal word for x.
		else {
			long zeroRunLen = rlw.getRunningLength();
			long newRunLen = offset / 64;
			long afterRunLen = ((zeroRunLen * 64 - 63) / 64) - newRunLen;
			long newNumLiterals = rlw.getNumberOfLiteralWords() + 1;
			final long newdata = 1l << (offset % 64);

			// no preceeding and following run length.
			// CASE 1) Try to merge with preceeding or following RLW.
			if (newRunLen == 0 && afterRunLen == 0) {
				// merge with previous if exists and possible
				if (prev != null
						&& prev.getNumberOfLiteralWords() + newNumLiterals <= RunningLengthWord.largestliteralcount) {
					int bufferdeletepos = rlw.position;
					prev.setNumberOfLiteralWords(prev.getNumberOfLiteralWords()
							+ newNumLiterals);
					this.buffer[rlw.position] = newdata;

					if (this.rlw.equals(rlw))
						this.rlw = prev;

					// case 10: HLHLL -> HLLLL
					// comp: [0, 2, 5...] -> [0, 5...]
					// uncomp: [0, 3, 6..] -> [0, 6...]
					int deletepos = findCompresIndexByBufferIndex(bufferdeletepos);
					shiftCompIndexToLeft(deletepos, 1, 0);
					shiftUnCompIndexToLeft(deletepos, 1, 0);

					return;
				}
			}
			// No merging possible!
			// CASE 2) if previous run length = 0 then add new literal into
			// previous if previous still has space
			if (newRunLen == 0
					&& prev != null
					&& prev.getNumberOfLiteralWords() < RunningLengthWord.largestliteralcount) {
				int bufferdeletepos = rlw.position;
				prev.setNumberOfLiteralWords(prev.getNumberOfLiteralWords() + 1);
				rlw.setRunningLength(afterRunLen);
				shiftCompressedWordsRight(rlw.position, 1);
				this.buffer[prev.position + prev.getNumberOfLiteralWords()] = newdata;

				// case 11: HLHL -> HLLHL
				// comp: [0, 2, 4...] -> [0, 3, 5...]
				// uncomp: [0, 3, 6..] -> [0, 4, 6...]
				int deletepos = findCompresIndexByBufferIndex(bufferdeletepos);
				shiftCompIndexToLeft(deletepos, 0, 1);
				UnCompressPosRep[deletepos] += 1;

				return;
			}

			// CASE 3) No merging possible, if following run length = 0, then
			// try to extend R with the new literal.
			if (afterRunLen == 0
					&& rlw.getNumberOfLiteralWords() < RunningLengthWord.largestliteralcount) {
				int bufferdeletepos = -1;
				if (next != null)
					bufferdeletepos = next.position;
				rlw.setNumberOfLiteralWords(rlw.getNumberOfLiteralWords() + 1);
				rlw.setRunningLength(newRunLen);
				shiftCompressedWordsRight(rlw.position + 1, 1);
				this.buffer[rlw.position + 1] = newdata;

				// case 12: HLL -> HLLL
				// comp: [0, 3...] -> [0, 4...]
				// uncomp: [0, 4..] -> [0, 5...]
				if (bufferdeletepos != -1) {
					int deletepos = findCompresIndexByBufferIndex(bufferdeletepos);
					shiftCompIndexToLeft(deletepos, 0, 1);
					shiftUnCompIndexToLeft(deletepos, 0, 1);
				}
				return;
			}

			// TODO rlw is full (literals) and next one has no running length
			// and is not full
			if (afterRunLen == 0
					& next != null
					&& rlw.getNumberOfLiteralWords() == RunningLengthWord.largestliteralcount
					&& next.getNumberOfLiteralWords() < RunningLengthWord.largestliteralcount) {
				rlw.setRunningLength(rlw.getRunningLength() - 1);
				shiftCompressedWordsRight(rlw.position + 1, 1);
				rlw.array = this.buffer;
				next.array = this.buffer;

				this.buffer[rlw.position + 1] = newdata;

				// switch next header with last literal
				long temp = this.buffer[next.position + 1];
				this.buffer[next.position + 1] = this.buffer[next.position];
				this.buffer[next.position] = temp;
				next.setNumberOfLiteralWords(next.getNumberOfLiteralWords() + 1);

				if (next.position + 1 == this.rlw.position)
					this.rlw.position = next.position;

				return;
			}

			// CASE 4) no extension possible. Have to SPLIT the zero sequence
			// and create new RLW
			// new RLW only gets a single literal
			if (afterRunLen == 0) {
				assert (rlw.getNumberOfLiteralWords() == RunningLengthWord.largestliteralcount);

				int bufferdeletepos = -1;
				if (next != null)
					bufferdeletepos = next.position;
				shiftCompressedWordsRight(rlw.position + 1, 1);
				shiftCompressedWordsRight(
						rlw.position + rlw.getNumberOfLiteralWords() + 1, 1);
				RunningLengthWord newRlw = new RunningLengthWord(this.buffer,
						rlw.position + rlw.getNumberOfLiteralWords() + 1);
				rlw.array = this.buffer;

				newRlw.setNumberOfLiteralWords(1L);
				newRlw.setRunningLength(0);
				newRlw.setRunningBit(false);

				rlw.setRunningLength(newRunLen);

				this.buffer[rlw.position + 1] = newdata;

				if (newRlw.position > this.rlw.position)
					this.rlw.position = newRlw.position;

				// case 13: HLL -> HLHLL
				// comp: [0, 3...] -> [0, 2, 5...]
				// uncomp: [0, 5..] -> [0, 2, 5...]
				if (bufferdeletepos != -1) {
					int movingpos = findCompresIndexByBufferIndex(bufferdeletepos);
					shiftCompIndexToRight(movingpos, 1, 2);
					CompressPosRep[movingpos] = CompressPosRep[movingpos - 1] + 2;
					shiftUncompIndexToRight(movingpos, 1, 0);
					UnCompressPosRep[movingpos] = UnCompressPosRep[movingpos - 1] + 2;
				}

			}
			// new RLW also gets run length
			else {
				shiftCompressedWordsRight(wordPos + 1, 2);
				RunningLengthWord newRlw = new RunningLengthWord(this.buffer,
						wordPos + 2);
				rlw.array = this.buffer;

				newRlw.setNumberOfLiteralWords(rlw.getNumberOfLiteralWords());
				newRlw.setRunningLength(afterRunLen);
				newRlw.setRunningBit(false);

				rlw.setRunningLength(newRunLen);
				rlw.setNumberOfLiteralWords(1);

				this.buffer[wordPos + 1] = newdata;

				if (newRlw.position > this.rlw.position)
					this.rlw.position = newRlw.position;
			}
		}
	}

	private void shiftCompIndexToLeft(int deletepos, int shift, int add) {
		if (deletepos + shift == CompressUsedWord)
			return;
		System.arraycopy(CompressPosRep, deletepos + shift, CompressPosRep,
				deletepos, CompressUsedWord - shift - deletepos);
		if (add != 0) {
			for (int i = deletepos; i < CompressUsedWord; i++)
				CompressPosRep[i] += add;
		}

		CompressUsedWord -= shift;
	}

	private void shiftUnCompIndexToLeft(int deletepos, int shift, int add) {
		if (deletepos + shift == UnCompressUsedWords)
			return;
		System.arraycopy(UnCompressPosRep, deletepos + shift, UnCompressPosRep,
				deletepos, UnCompressUsedWords - shift - deletepos);
		if (add != 0) {
			for (int i = deletepos; i < UnCompressUsedWords; i++)
				UnCompressPosRep[i] += add;
		}

		UnCompressUsedWords -= shift;
	}

	private void shiftCompIndexToRight(int movingpos, int shift, int add) {
		if (add != 0) {
			for (int i = movingpos; i < CompressUsedWord; i++)
				CompressPosRep[i] += add;
		}

		long[] result = new long[CompressUsedWord + shift];
		System.arraycopy(CompressPosRep, 0, result, 0, movingpos);
		System.arraycopy(CompressPosRep, movingpos, result, movingpos + shift,
				CompressUsedWord - movingpos);

		CompressPosRep = result;
		CompressUsedWord += shift;
	}

	private void shiftUncompIndexToRight(int movingpos, int shift, int add) {
		if (add != 0) {
			for (int i = movingpos; i < UnCompressUsedWords; i++)
				UnCompressPosRep[i] += add;
		}

		long[] result = new long[UnCompressUsedWords + shift];
		System.arraycopy(UnCompressPosRep, 0, result, 0, movingpos);
		System.arraycopy(UnCompressPosRep, movingpos, result,
				movingpos + shift, UnCompressUsedWords - movingpos);

		UnCompressPosRep = result;
		UnCompressUsedWords += shift;
	}

	private int findCompresIndexByBufferIndex(int bufferdeletepos) {
		int midpoint = (CompressUsedWord - 1) / 2;
		int leftpoint = 0;
		int rightpoint = CompressUsedWord - 1;

		while (true) {
			if (bufferdeletepos > CompressPosRep[midpoint]) {
				leftpoint = midpoint;
				midpoint = leftpoint + (rightpoint - leftpoint) / 2;
				if (CompressPosRep[CompressUsedWord - 1] <= bufferdeletepos)
					return rightpoint;
				else if (midpoint == leftpoint)
					return leftpoint;

			} else if (bufferdeletepos < CompressPosRep[midpoint]) {
				rightpoint = midpoint;
				midpoint = leftpoint + (rightpoint - leftpoint) / 2;
				if (midpoint == leftpoint)
					return leftpoint;

			} else
				return leftpoint;
		}
	}

	private void checkArraySize() {
		if (CompressUsedWord == CompressPosRep.length) {
			final long oldLiteralRep[] = CompressPosRep;
			final long oldWordRep[] = UnCompressPosRep;
			CompressPosRep = new long[oldLiteralRep.length * 2];
			UnCompressPosRep = new long[oldWordRep.length * 2];
			System.arraycopy(oldLiteralRep, 0, CompressPosRep, 0,
					oldLiteralRep.length);
			System.arraycopy(oldWordRep, 0, UnCompressPosRep, 0,
					oldWordRep.length);
		}
	}

	// return the index to uncompressposrep that contains the bitpos.
	public int getHeaderIndex(int bitpos) {
		int midpoint = (UnCompressUsedWords - 1) / 2;
		int leftpoint = 0;
		int rightpoint = UnCompressUsedWords - 1;
		int bitWordIndex = bitpos / 64;

		while (true) {
			if (bitWordIndex > UnCompressPosRep[midpoint]) {
				leftpoint = midpoint;
				midpoint = leftpoint + (rightpoint - leftpoint) / 2;
				if (UnCompressPosRep[UnCompressUsedWords - 1] <= bitWordIndex)
					return rightpoint;
				else if (midpoint == leftpoint)
					return leftpoint;

			} else if (bitWordIndex < UnCompressPosRep[midpoint]) {
				rightpoint = midpoint;
				midpoint = leftpoint + (rightpoint - leftpoint) / 2;
				if (midpoint == leftpoint)
					return leftpoint;

			} else
				return leftpoint;
		}
	}

	public void createHeaderMapping() {
		EWAHIterator iter = new EWAHIterator(this.buffer, actualsizeinwords);
		int i = 1;
		CompressPosRep[0] = 0;
		UnCompressPosRep[0] = 0;
		RunningLengthWord rlw = iter.next();
		long currentsize = rlw.size();
		while (iter.hasNext()) {
			rlw = iter.next();
			CompressPosRep[i] = rlw.position;
			UnCompressPosRep[i] = currentsize;
			currentsize += rlw.size();
			i++;
		}
	}

	public RunningLengthWord getRLW(int headerIndex) {
		return new RunningLengthWord(this.buffer, headerIndex);
	}

}

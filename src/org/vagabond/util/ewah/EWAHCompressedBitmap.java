package org.vagabond.util.ewah;

/*
 * Copyright 2009-2012, Daniel Lemire, Cliff Moon, David McIntosh and Robert
 * Becho Licensed under APL 2.0.
 */

import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;
import org.vagabond.util.LoggerUtil;

/**
 * <p>
 * This implements the patent-free(1) EWAH scheme. Roughly speaking, it is a
 * 64-bit variant of the BBC compression scheme used by Oracle for its bitmap
 * indexes.
 * </p>
 * <p>
 * The objective of this compression type is to provide some compression, while
 * reducing as much as possible the CPU cycle usage.
 * </p>
 * <p>
 * This implementation being 64-bit, it assumes a 64-bit CPU together with a
 * 64-bit Java Virtual Machine. This same code on a 32-bit machine may not be as
 * fast.
 * <p>
 * <p>
 * For more details, see the following paper:
 * </p>
 * <ul>
 * <li>Daniel Lemire, Owen Kaser, Kamel Aouiche, Sorting improves word-aligned
 * bitmap indexes. Data & Knowledge Engineering 69 (1), pages 3-28, 2010.
 * http://arxiv.org/abs/0901.3751</li>
 * </ul>
 * <p>
 * A 32-bit version of the compressed format was described by Wu et al. and
 * named WBC:
 * </p>
 * <ul>
 * <li>K. Wu, E. J. Otoo, A. Shoshani, H. Nordberg, Notes on design and
 * implementation of compressed bit vectors, Tech. Rep. LBNL/PUB-3161, Lawrence
 * Berkeley National Laboratory, available from http://crd.lbl.
 * gov/~kewu/ps/PUB-3161.html (2001).</li>
 * </ul>
 * <p>
 * Probably, the best prior art is the Oracle bitmap compression scheme (BBC):
 * </p>
 * <ul>
 * <li>G. Antoshenkov, Byte-Aligned Bitmap Compression, DCC'95, 1995.</li>
 * </ul>
 * <p>
 * 1- The authors do not know of any patent infringed by the following
 * implementation. However, similar schemes, like WAH are covered by patents.
 * </p>
 * 
 * @since 0.1.0
 */
public class EWAHCompressedBitmap implements Cloneable, Externalizable,
		Iterable<Integer>, BitmapStorage, WritableBitmap, IBitSet { // TODO add
																	// bloom
																	// filter
																	// for more
																	// efficient
																	// get

	static Logger log = Logger.getLogger(EWAHCompressedBitmap.class);

	/**
	 * Creates an empty bitmap (no bit set to true).
	 */
	public EWAHCompressedBitmap() {
		this.buffer = new long[defaultbuffersize];
		this.rlw = new RunningLengthWord(this.buffer, 0);
	}

	/**
	 * Sets explicitly the buffer size (in 64-bit words). The initial memory
	 * usage will be "buffersize * 64". For large poorly compressible bitmaps,
	 * using large values may improve performance.
	 * 
	 * @param buffersize
	 *            number of 64-bit words reserved when the object is created)
	 */
	public EWAHCompressedBitmap(final int buffersize) {
		this.buffer = new long[buffersize];
		this.rlw = new RunningLengthWord(this.buffer, 0);
	}

	/**
	 * Create a new bitmap and set values from a String of 0's and 1's.
	 * 
	 * @param values
	 */
	public EWAHCompressedBitmap(final String values) {
		this.buffer = new long[defaultbuffersize];
		this.rlw = new RunningLengthWord(this.buffer, 0);
		readFromBitsString(values);
	}

	/**
	 * Gets an EWAHIterator over the data. This is a customized iterator which
	 * iterates over run length word. For experts only.
	 * 
	 * @return the EWAHIterator
	 */
	private EWAHIterator getEWAHIterator() {
		return new EWAHIterator(this.buffer, this.actualsizeinwords);
	}

	/**
	 * Returns a new compressed bitmap containing the bitwise XOR values of the
	 * current bitmap with some other bitmap. The running time is proportional
	 * to the sum of the compressed sizes (as reported by sizeInBytes()).
	 * 
	 * @param a
	 *            the other bitmap
	 * @return the EWAH compressed bitmap
	 */
	public EWAHCompressedBitmap xor(final EWAHCompressedBitmap a) {
		final EWAHCompressedBitmap container = new EWAHCompressedBitmap();
		container.reserve(this.actualsizeinwords + a.actualsizeinwords);
		xor(a, container);
		return container;
	}

	/**
	 * Computes a new compressed bitmap containing the bitwise XOR values of the
	 * current bitmap with some other bitmap. The running time is proportional
	 * to the sum of the compressed sizes (as reported by sizeInBytes()).
	 * 
	 * @since 0.4.0
	 * @param a
	 *            the other bitmap
	 * @param container
	 *            where we store the result
	 */
	protected void xor(final EWAHCompressedBitmap a,
			final BitmapStorage container) {
		final EWAHIterator i = a.getEWAHIterator();
		final EWAHIterator j = getEWAHIterator();
		if (!(i.hasNext() && j.hasNext())) {// this never happens...
			container.setSizeInBits(sizeInBits());
		}
		// at this point, this is safe:
		BufferedRunningLengthWord rlwi =
				new BufferedRunningLengthWord(i.next());
		BufferedRunningLengthWord rlwj =
				new BufferedRunningLengthWord(j.next());
		while (true) {
			final boolean i_is_prey = rlwi.size() < rlwj.size();
			final BufferedRunningLengthWord prey = i_is_prey ? rlwi : rlwj;
			final BufferedRunningLengthWord predator = i_is_prey ? rlwj : rlwi;
			if (prey.getRunningBit() == false) {
				final long predatorrl = predator.getRunningLength();
				final long preyrl = prey.getRunningLength();
				final long tobediscarded =
						(predatorrl >= preyrl) ? preyrl : predatorrl;
				container.addStreamOfEmptyWords(predator.getRunningBit(),
						tobediscarded);
				final long dw_predator =
						predator.dirtywordoffset
								+ (i_is_prey ? j.dirtyWords() : i.dirtyWords());
				container.addStreamOfDirtyWords(
						i_is_prey ? j.buffer() : i.buffer(), dw_predator,
						preyrl - tobediscarded);
				predator.discardFirstWords(preyrl);
				prey.discardFirstWords(preyrl);
			}
			else {
				// we have a stream of 1x11
				final long predatorrl = predator.getRunningLength();
				final long preyrl = prey.getRunningLength();
				final long tobediscarded =
						(predatorrl >= preyrl) ? preyrl : predatorrl;
				container.addStreamOfEmptyWords(!predator.getRunningBit(),
						tobediscarded);
				final int dw_predator =
						predator.dirtywordoffset
								+ (i_is_prey ? j.dirtyWords() : i.dirtyWords());
				final long[] buf = i_is_prey ? j.buffer() : i.buffer();
				for (int k = 0; k < preyrl - tobediscarded; ++k)
					container.add(~buf[k + dw_predator]);
				predator.discardFirstWords(preyrl);
				prey.discardFirstWords(preyrl);
			}
			final long predatorrl = predator.getRunningLength();
			if (predatorrl > 0) {
				if (predator.getRunningBit() == false) {
					final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
					final long tobediscarded =
							(predatorrl >= nbre_dirty_prey) ? nbre_dirty_prey
									: predatorrl;
					final long dw_prey =
							prey.dirtywordoffset
									+ (i_is_prey ? i.dirtyWords() : j
											.dirtyWords());
					predator.discardFirstWords(tobediscarded);
					prey.discardFirstWords(tobediscarded);
					container.addStreamOfDirtyWords(
							i_is_prey ? i.buffer() : j.buffer(), dw_prey,
							tobediscarded);
				}
				else {
					final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
					final long tobediscarded =
							(predatorrl >= nbre_dirty_prey) ? nbre_dirty_prey
									: predatorrl;
					final int dw_prey =
							prey.dirtywordoffset
									+ (i_is_prey ? i.dirtyWords() : j
											.dirtyWords());
					predator.discardFirstWords(tobediscarded);
					prey.discardFirstWords(tobediscarded);
					final long[] buf = i_is_prey ? i.buffer() : j.buffer();
					for (int k = 0; k < tobediscarded; ++k)
						container.add(~buf[k + dw_prey]);
				}
			}
			// all that is left to do now is to AND the dirty words
			final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
			if (nbre_dirty_prey > 0) {
				for (int k = 0; k < nbre_dirty_prey; ++k) {
					if (i_is_prey)
						container.add(i.buffer()[prey.dirtywordoffset
								+ i.dirtyWords() + k]
								^ j.buffer()[predator.dirtywordoffset
										+ j.dirtyWords() + k]);
					else
						container.add(i.buffer()[predator.dirtywordoffset
								+ i.dirtyWords() + k]
								^ j.buffer()[prey.dirtywordoffset
										+ j.dirtyWords() + k]);
				}
				predator.discardFirstWords(nbre_dirty_prey);
			}
			if (i_is_prey) {
				if (!i.hasNext()) {
					rlwi = null;
					break;
				}
				rlwi.reset(i.next());
			}
			else {
				if (!j.hasNext()) {
					rlwj = null;
					break;
				}
				rlwj.reset(j.next());
			}
		}
		if (rlwi != null)
			discharge(rlwi, i, container);
		if (rlwj != null)
			discharge(rlwj, j, container);
		container.setSizeInBits(Math.max(sizeInBits(), a.sizeInBits()));
	}

	/**
	 * Returns a new compressed bitmap containing the bitwise AND values of the
	 * current bitmap with some other bitmap. The running time is proportional
	 * to the sum of the compressed sizes (as reported by sizeInBytes()).
	 * 
	 * @param a
	 *            the other bitmap
	 * @return the EWAH compressed bitmap
	 */
	public EWAHCompressedBitmap and(final EWAHCompressedBitmap a) {
		final EWAHCompressedBitmap container = new EWAHCompressedBitmap();
		container.reserve(this.actualsizeinwords > a.actualsizeinwords
				? this.actualsizeinwords : a.actualsizeinwords);
		and(a, container);
		return container;
	}

	/**
	 * Computes new compressed bitmap containing the bitwise AND values of the
	 * current bitmap with some other bitmap. The running time is proportional
	 * to the sum of the compressed sizes (as reported by sizeInBytes()).
	 * 
	 * @since 0.4.0
	 * @param a
	 *            the other bitmap
	 * @param container
	 *            where we store the result
	 */
	protected void and(final EWAHCompressedBitmap a,
			final BitmapStorage container) {
		final EWAHIterator i = a.getEWAHIterator();
		final EWAHIterator j = getEWAHIterator();
		if (!(i.hasNext() && j.hasNext())) {// this never happens...
			container.setSizeInBits(sizeInBits());
		}
		// at this point, this is safe:
		BufferedRunningLengthWord rlwi =
				new BufferedRunningLengthWord(i.next());
		BufferedRunningLengthWord rlwj =
				new BufferedRunningLengthWord(j.next());
		while (true) {
			final boolean i_is_prey = rlwi.size() < rlwj.size();
			final BufferedRunningLengthWord prey = i_is_prey ? rlwi : rlwj;
			final BufferedRunningLengthWord predator = i_is_prey ? rlwj : rlwi;
			if (prey.getRunningBit() == false) {
				container.addStreamOfEmptyWords(false, prey.RunningLength);
				predator.discardFirstWords(prey.RunningLength);
				prey.RunningLength = 0;
			}
			else {
				// we have a stream of 1x11
				final long predatorrl = predator.getRunningLength();
				final long preyrl = prey.getRunningLength();
				final long tobediscarded =
						(predatorrl >= preyrl) ? preyrl : predatorrl;
				container.addStreamOfEmptyWords(predator.getRunningBit(),
						tobediscarded);
				final int dw_predator =
						predator.dirtywordoffset
								+ (i_is_prey ? j.dirtyWords() : i.dirtyWords());
				container.addStreamOfDirtyWords(
						i_is_prey ? j.buffer() : i.buffer(), dw_predator,
						preyrl - tobediscarded);
				predator.discardFirstWords(preyrl);
				prey.RunningLength = 0;
			}
			final long predatorrl = predator.getRunningLength();
			if (predatorrl > 0) {
				if (predator.getRunningBit() == false) {
					final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
					final long tobediscarded =
							(predatorrl >= nbre_dirty_prey) ? nbre_dirty_prey
									: predatorrl;
					predator.discardFirstWords(tobediscarded);
					prey.discardFirstWords(tobediscarded);
					container.addStreamOfEmptyWords(false, tobediscarded);
				}
				else {
					final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
					final int dw_prey =
							prey.dirtywordoffset
									+ (i_is_prey ? i.dirtyWords() : j
											.dirtyWords());
					final long tobediscarded =
							(predatorrl >= nbre_dirty_prey) ? nbre_dirty_prey
									: predatorrl;
					container.addStreamOfDirtyWords(
							i_is_prey ? i.buffer() : j.buffer(), dw_prey,
							tobediscarded);
					predator.discardFirstWords(tobediscarded);
					prey.discardFirstWords(tobediscarded);
				}
			}
			// all that is left to do now is to AND the dirty words
			final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
			if (nbre_dirty_prey > 0) {
				for (int k = 0; k < nbre_dirty_prey; ++k) {
					if (i_is_prey)
						container.add(i.buffer()[prey.dirtywordoffset
								+ i.dirtyWords() + k]
								& j.buffer()[predator.dirtywordoffset
										+ j.dirtyWords() + k]);
					else
						container.add(i.buffer()[predator.dirtywordoffset
								+ i.dirtyWords() + k]
								& j.buffer()[prey.dirtywordoffset
										+ j.dirtyWords() + k]);
				}
				predator.discardFirstWords(nbre_dirty_prey);
			}
			if (i_is_prey) {
				if (!i.hasNext()) {
					rlwi = null;
					break;
				}
				rlwi.reset(i.next());
			}
			else {
				if (!j.hasNext()) {
					rlwj = null;
					break;
				}
				rlwj.reset(j.next());
			}
		}
		if (rlwi != null)
			dischargeAsEmpty(rlwi, i, container);
		if (rlwj != null)
			dischargeAsEmpty(rlwj, j, container);
		container.setSizeInBits(Math.max(sizeInBits(), a.sizeInBits()));
	}

	/**
	 * Return true if the two EWAHCompressedBitmap have both at least one true
	 * bit in the same position. Equivalently, you could call "and" and check
	 * whether there is a set bit, but intersects will run faster if you don't
	 * need the result of the "and" operation.
	 * 
	 * @since 0.3.2
	 * @param a
	 *            the other bitmap
	 * @return whether they intersect
	 */
	public boolean intersects(final EWAHCompressedBitmap a) {
		final EWAHIterator i = a.getEWAHIterator();
		final EWAHIterator j = getEWAHIterator();
		if ((!i.hasNext()) || (!j.hasNext())) {
			return false;
		}
		BufferedRunningLengthWord rlwi =
				new BufferedRunningLengthWord(i.next());
		BufferedRunningLengthWord rlwj =
				new BufferedRunningLengthWord(j.next());
		while (true) {
			final boolean i_is_prey = rlwi.size() < rlwj.size();
			final BufferedRunningLengthWord prey = i_is_prey ? rlwi : rlwj;
			final BufferedRunningLengthWord predator = i_is_prey ? rlwj : rlwi;
			if (prey.getRunningBit() == false) {
				predator.discardFirstWords(prey.RunningLength);
				prey.RunningLength = 0;
			}
			else {
				// we have a stream of 1x11
				final long predatorrl = predator.getRunningLength();
				final long preyrl = prey.getRunningLength();
				final long tobediscarded =
						(predatorrl >= preyrl) ? preyrl : predatorrl;
				if (predator.getRunningBit())
					return true;
				if (preyrl > tobediscarded)
					return true;
				predator.discardFirstWords(preyrl);
				prey.RunningLength = 0;
			}
			final long predatorrl = predator.getRunningLength();
			if (predatorrl > 0) {
				if (predator.getRunningBit() == false) {
					final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
					final long tobediscarded =
							(predatorrl >= nbre_dirty_prey) ? nbre_dirty_prey
									: predatorrl;
					predator.discardFirstWords(tobediscarded);
					prey.discardFirstWords(tobediscarded);
				}
				else {
					final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
					final long tobediscarded =
							(predatorrl >= nbre_dirty_prey) ? nbre_dirty_prey
									: predatorrl;
					if (tobediscarded > 0)
						return true;
					predator.discardFirstWords(tobediscarded);
					prey.discardFirstWords(tobediscarded);
				}
			}
			final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
			if (nbre_dirty_prey > 0) {
				for (int k = 0; k < nbre_dirty_prey; ++k) {
					if (i_is_prey) {
						if ((i.buffer()[prey.dirtywordoffset + i.dirtyWords()
								+ k] & j.buffer()[predator.dirtywordoffset
								+ j.dirtyWords() + k]) != 0)
							return true;
					}
					else {
						if ((i.buffer()[predator.dirtywordoffset
								+ i.dirtyWords() + k] & j.buffer()[prey.dirtywordoffset
								+ j.dirtyWords() + k]) != 0)
							return true;
					}
				}
			}
			if (i_is_prey) {
				if (!i.hasNext()) {
					rlwi = null;
					break;
				}
				rlwi.reset(i.next());
			}
			else {
				if (!j.hasNext()) {
					rlwj = null;
					break;
				}
				rlwj.reset(j.next());
			}
		}
		return false;
	}

	/**
	 * Returns a new compressed bitmap containing the bitwise AND NOT values of
	 * the current bitmap with some other bitmap. The running time is
	 * proportional to the sum of the compressed sizes (as reported by
	 * sizeInBytes()).
	 * 
	 * @param a
	 *            the other bitmap
	 * @return the EWAH compressed bitmap
	 */
	public EWAHCompressedBitmap andNot(final EWAHCompressedBitmap a) {
		final EWAHCompressedBitmap container = new EWAHCompressedBitmap();
		container.reserve(this.actualsizeinwords > a.actualsizeinwords
				? this.actualsizeinwords : a.actualsizeinwords);
		andNot(a, container);
		return container;
	}

	/**
	 * Returns a new compressed bitmap containing the bitwise AND NOT values of
	 * the current bitmap with some other bitmap. The running time is
	 * proportional to the sum of the compressed sizes (as reported by
	 * sizeInBytes()).
	 * 
	 * @since 0.4.0
	 * @param a
	 *            the other bitmap
	 * @return the EWAH compressed bitmap
	 */
	protected void andNot(final EWAHCompressedBitmap a,
			final BitmapStorage container) {
		final EWAHIterator i = a.getEWAHIterator();
		final EWAHIterator j = getEWAHIterator();
		if (!(i.hasNext() && j.hasNext())) {// this never happens...
			container.setSizeInBits(sizeInBits());
		}
		// at this point, this is safe:
		BufferedRunningLengthWord rlwi =
				new BufferedRunningLengthWord(i.next());
		rlwi.setRunningBit(!rlwi.getRunningBit());
		BufferedRunningLengthWord rlwj =
				new BufferedRunningLengthWord(j.next());
		while (true) {
			final boolean i_is_prey = rlwi.size() < rlwj.size();
			final BufferedRunningLengthWord prey = i_is_prey ? rlwi : rlwj;
			final BufferedRunningLengthWord predator = i_is_prey ? rlwj : rlwi;

			if (prey.getRunningBit() == false) {
				container.addStreamOfEmptyWords(false, prey.RunningLength);
				predator.discardFirstWords(prey.RunningLength);
				prey.RunningLength = 0;
			}
			else {
				// we have a stream of 1x11
				final long predatorrl = predator.getRunningLength();
				final long preyrl = prey.getRunningLength();
				final long tobediscarded =
						(predatorrl >= preyrl) ? preyrl : predatorrl;
				container.addStreamOfEmptyWords(predator.getRunningBit(),
						tobediscarded);
				final int dw_predator =
						predator.dirtywordoffset
								+ (i_is_prey ? j.dirtyWords() : i.dirtyWords());
				if (i_is_prey)
					container.addStreamOfDirtyWords(j.buffer(), dw_predator,
							preyrl - tobediscarded);
				else
					container.addStreamOfNegatedDirtyWords(i.buffer(),
							dw_predator, preyrl - tobediscarded);
				predator.discardFirstWords(preyrl);
				prey.RunningLength = 0;
			}
			final long predatorrl = predator.getRunningLength();
			if (predatorrl > 0) {
				if (predator.getRunningBit() == false) {
					final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
					final long tobediscarded =
							(predatorrl >= nbre_dirty_prey) ? nbre_dirty_prey
									: predatorrl;
					predator.discardFirstWords(tobediscarded);
					prey.discardFirstWords(tobediscarded);
					container.addStreamOfEmptyWords(false, tobediscarded);
				}
				else {
					final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
					final int dw_prey =
							prey.dirtywordoffset
									+ (i_is_prey ? i.dirtyWords() : j
											.dirtyWords());
					final long tobediscarded =
							(predatorrl >= nbre_dirty_prey) ? nbre_dirty_prey
									: predatorrl;
					if (i_is_prey)
						container.addStreamOfNegatedDirtyWords(i.buffer(),
								dw_prey, tobediscarded);
					else
						container.addStreamOfDirtyWords(j.buffer(), dw_prey,
								tobediscarded);
					predator.discardFirstWords(tobediscarded);
					prey.discardFirstWords(tobediscarded);
				}
			}
			// all that is left to do now is to AND the dirty words
			final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
			if (nbre_dirty_prey > 0) {
				for (int k = 0; k < nbre_dirty_prey; ++k) {
					if (i_is_prey)
						container.add((~i.buffer()[prey.dirtywordoffset
								+ i.dirtyWords() + k])
								& j.buffer()[predator.dirtywordoffset
										+ j.dirtyWords() + k]);
					else
						container.add((~i.buffer()[predator.dirtywordoffset
								+ i.dirtyWords() + k])
								& j.buffer()[prey.dirtywordoffset
										+ j.dirtyWords() + k]);
				}
				predator.discardFirstWords(nbre_dirty_prey);
			}
			if (i_is_prey) {
				if (!i.hasNext()) {
					rlwi = null;
					break;
				}
				rlwi.reset(i.next());
				rlwi.setRunningBit(!rlwi.getRunningBit());
			}
			else {
				if (!j.hasNext()) {
					rlwj = null;
					break;
				}
				rlwj.reset(j.next());
			}
		}
		if (rlwi != null)
			dischargeAsEmpty(rlwi, i, container);
		if (rlwj != null)
			discharge(rlwj, j, container);
		container.setSizeInBits(Math.max(sizeInBits(), a.sizeInBits()));
	}

	/**
	 * Negate (bitwise) the current bitmap. To get a negated copy, do
	 * ((EWAHCompressedBitmap) mybitmap.clone()).not(); The running time is
	 * proportional to the compressed size (as reported by sizeInBytes()).
	 */
	public void not() {
		final EWAHIterator i =
				new EWAHIterator(this.buffer, this.actualsizeinwords);
		if (!i.hasNext())
			return;
		while (true) {
			final RunningLengthWord rlw1 = i.next();
			if (rlw1.getRunningLength() > 0)
				rlw1.setRunningBit(!rlw1.getRunningBit());
			for (int j = 0; j < rlw1.getNumberOfLiteralWords(); ++j) {
				i.buffer()[i.dirtyWords() + j] =
						~i.buffer()[i.dirtyWords() + j];
			}
			if (!i.hasNext()) {// must potentially adjust the last dirty word
				if (rlw1.getNumberOfLiteralWords() == 0)
					return;
				int usedbitsinlast = this.sizeinbits % wordinbits;
				if (usedbitsinlast == 0)
					return;
				i.buffer()[i.dirtyWords() + rlw1.getNumberOfLiteralWords() - 1] &=
						((oneMask) >>> (wordinbits - usedbitsinlast));
				return;
			}
		}
	}

	/**
	 * Returns a new compressed bitmap containing the bitwise OR values of the
	 * current bitmap with some other bitmap. The running time is proportional
	 * to the sum of the compressed sizes (as reported by sizeInBytes()).
	 * 
	 * @param a
	 *            the other bitmap
	 * @return the EWAH compressed bitmap
	 */
	public EWAHCompressedBitmap or(final EWAHCompressedBitmap a) {
		final EWAHCompressedBitmap container = new EWAHCompressedBitmap();
		container.reserve(this.actualsizeinwords + a.actualsizeinwords);
		or(a, container);
		return container;
	}

	/**
	 * Returns the cardinality of the result of a bitwise OR of the values of
	 * the current bitmap with some other bitmap. Avoids needing to allocate an
	 * intermediate bitmap to hold the result of the OR.
	 * 
	 * @since 0.4.0
	 * @param a
	 *            the other bitmap
	 * @return the cardinality
	 */
	public int orCardinality(final EWAHCompressedBitmap a) {
		final BitCounter counter = new BitCounter();
		or(a, counter);
		return counter.getCount();
	}

	/**
	 * Returns the cardinality of the result of a bitwise AND of the values of
	 * the current bitmap with some other bitmap. Avoids needing to allocate an
	 * intermediate bitmap to hold the result of the OR.
	 * 
	 * @since 0.4.0
	 * @param a
	 *            the other bitmap
	 * @return the cardinality
	 */
	public int andCardinality(final EWAHCompressedBitmap a) {
		final BitCounter counter = new BitCounter();
		and(a, counter);
		return counter.getCount();
	}

	/**
	 * Returns the cardinality of the result of a bitwise AND NOT of the values
	 * of the current bitmap with some other bitmap. Avoids needing to allocate
	 * an intermediate bitmap to hold the result of the OR.
	 * 
	 * @since 0.4.0
	 * @param a
	 *            the other bitmap
	 * @return the cardinality
	 */
	public int andNotCardinality(final EWAHCompressedBitmap a) {
		final BitCounter counter = new BitCounter();
		andNot(a, counter);
		return counter.getCount();
	}

	/**
	 * Returns the cardinality of the result of a bitwise XOR of the values of
	 * the current bitmap with some other bitmap. Avoids needing to allocate an
	 * intermediate bitmap to hold the result of the OR.
	 * 
	 * @since 0.4.0
	 * @param a
	 *            the other bitmap
	 * @return the cardinality
	 */
	public int xorCardinality(final EWAHCompressedBitmap a) {
		final BitCounter counter = new BitCounter();
		xor(a, counter);
		return counter.getCount();
	}

	/**
	 * Computes the bitwise or between the current bitmap and the bitmap "a".
	 * Stores the result in the container.
	 * 
	 * @since 0.4.0
	 * @param a
	 *            the other bitmap
	 * @param container
	 *            where we store the result
	 */
	protected void or(final EWAHCompressedBitmap a,
			final BitmapStorage container) {
		final EWAHIterator i = a.getEWAHIterator();
		final EWAHIterator j = getEWAHIterator();
		if (!(i.hasNext() && j.hasNext())) {// this never happens...
			container.setSizeInBits(sizeInBits());
			return;
		}
		// at this point, this is safe:
		BufferedRunningLengthWord rlwi =
				new BufferedRunningLengthWord(i.next());
		BufferedRunningLengthWord rlwj =
				new BufferedRunningLengthWord(j.next());
		// RunningLength;
		while (true) {
			final boolean i_is_prey = rlwi.size() < rlwj.size();
			final BufferedRunningLengthWord prey = i_is_prey ? rlwi : rlwj;
			final BufferedRunningLengthWord predator = i_is_prey ? rlwj : rlwi;
			if (prey.getRunningBit() == false) {
				final long predatorrl = predator.getRunningLength();
				final long preyrl = prey.getRunningLength();
				final long tobediscarded =
						(predatorrl >= preyrl) ? preyrl : predatorrl;
				container.addStreamOfEmptyWords(predator.getRunningBit(),
						tobediscarded);
				final long dw_predator =
						predator.dirtywordoffset
								+ (i_is_prey ? j.dirtyWords() : i.dirtyWords());
				container.addStreamOfDirtyWords(
						i_is_prey ? j.buffer() : i.buffer(), dw_predator,
						preyrl - tobediscarded);
				predator.discardFirstWords(preyrl);
				prey.discardFirstWords(preyrl);
				prey.RunningLength = 0;
			}
			else {
				// we have a stream of 1x11
				container.addStreamOfEmptyWords(true, prey.RunningLength);
				predator.discardFirstWords(prey.RunningLength);
				prey.RunningLength = 0;
			}
			long predatorrl = predator.getRunningLength();
			if (predatorrl > 0) {
				if (predator.getRunningBit() == false) {
					final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
					final long tobediscarded =
							(predatorrl >= nbre_dirty_prey) ? nbre_dirty_prey
									: predatorrl;
					final long dw_prey =
							prey.dirtywordoffset
									+ (i_is_prey ? i.dirtyWords() : j
											.dirtyWords());
					predator.discardFirstWords(tobediscarded);
					prey.discardFirstWords(tobediscarded);
					container.addStreamOfDirtyWords(
							i_is_prey ? i.buffer() : j.buffer(), dw_prey,
							tobediscarded);
				}
				else {
					final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
					final long tobediscarded =
							(predatorrl >= nbre_dirty_prey) ? nbre_dirty_prey
									: predatorrl;
					container.addStreamOfEmptyWords(true, tobediscarded);
					predator.discardFirstWords(tobediscarded);
					prey.discardFirstWords(tobediscarded);
				}
			}
			// all that is left to do now is to OR the dirty words
			final long nbre_dirty_prey = prey.getNumberOfLiteralWords();
			if (nbre_dirty_prey > 0) {
				for (int k = 0; k < nbre_dirty_prey; ++k) {
					if (i_is_prey)
						container.add(i.buffer()[prey.dirtywordoffset
								+ i.dirtyWords() + k]
								| j.buffer()[predator.dirtywordoffset
										+ j.dirtyWords() + k]);
					else
						container.add(i.buffer()[predator.dirtywordoffset
								+ i.dirtyWords() + k]
								| j.buffer()[prey.dirtywordoffset
										+ j.dirtyWords() + k]);
				}
				predator.discardFirstWords(nbre_dirty_prey);
			}
			if (i_is_prey) {
				if (!i.hasNext()) {
					rlwi = null;
					break;
				}
				rlwi.reset(i.next());// = new
				// BufferedRunningLengthWord(i.next());
			}
			else {
				if (!j.hasNext()) {
					rlwj = null;
					break;
				}
				rlwj.reset(j.next());// = new
				// BufferedRunningLengthWord(
				// j.next());
			}
		}
		if (rlwi != null)
			discharge(rlwi, i, container);
		if (rlwj != null)
			discharge(rlwj, j, container);
		container.setSizeInBits(Math.max(sizeInBits(), a.sizeInBits()));
	}

	/**
	 * Returns a new compressed bitmap containing the bitwise OR values of the
	 * provided bitmaps.
	 * 
	 * @since 0.4.0
	 * @param bitmaps
	 *            bitmaps to OR together
	 * @return result of the OR
	 */
	public static EWAHCompressedBitmap
			or(final EWAHCompressedBitmap... bitmaps) {
		final EWAHCompressedBitmap container = new EWAHCompressedBitmap();
		int largestSize = 0;
		for (EWAHCompressedBitmap bitmap : bitmaps) {
			largestSize = Math.max(bitmap.actualsizeinwords, largestSize);
		}
		container.reserve((int) (largestSize * 1.5));
		or(container, bitmaps);
		return container;
	}

	/**
	 * Returns the cardinality of the result of a bitwise OR of the values of
	 * the provided bitmaps. Avoids needing to allocate an intermediate bitmap
	 * to hold the result of the OR.
	 * 
	 * @since 0.4.0
	 * @param bitmaps
	 *            bitmaps to OR
	 * @return the cardinality
	 */
	public static int orCardinality(final EWAHCompressedBitmap... bitmaps) {
		final BitCounter counter = new BitCounter();
		or(counter, bitmaps);
		return counter.getCount();
	}

	/**
	 * For internal use. Computes the bitwise or of the provided bitmaps and
	 * stores the result in the container.
	 * 
	 * @since 0.4.0
	 */
	private static void or(final BitmapStorage container,
			final EWAHCompressedBitmap... bitmaps) {
		if (bitmaps.length == 2) {
			// should be more efficient
			bitmaps[0].or(bitmaps[1], container);
			return;
		}

		// Sort the bitmaps in descending order by sizeinbits. We will exhaust
		// the sorted bitmaps from right to left.
		final EWAHCompressedBitmap[] sortedBitmaps =
				new EWAHCompressedBitmap[bitmaps.length];
		System.arraycopy(bitmaps, 0, sortedBitmaps, 0, bitmaps.length);
		Arrays.sort(sortedBitmaps, new Comparator<EWAHCompressedBitmap>() {
			public int compare(EWAHCompressedBitmap a, EWAHCompressedBitmap b) {
				return a.sizeinbits < b.sizeinbits ? 1
						: a.sizeinbits == b.sizeinbits ? 0 : -1;
			}
		});

		final IteratingBufferedRunningLengthWord[] rlws =
				new IteratingBufferedRunningLengthWord[bitmaps.length];
		int maxAvailablePos = 0;
		for (EWAHCompressedBitmap bitmap : sortedBitmaps) {
			EWAHIterator iterator = bitmap.getEWAHIterator();
			if (iterator.hasNext()) {
				rlws[maxAvailablePos++] =
						new IteratingBufferedRunningLengthWord(iterator);
			}
		}

		if (maxAvailablePos == 0) { // this never happens...
			container.setSizeInBits(0);
			return;
		}

		int maxSize = sortedBitmaps[0].sizeinbits;

		while (true) {
			long maxOneRl = 0;
			long minZeroRl = Long.MAX_VALUE;
			long minSize = Long.MAX_VALUE;
			int numEmptyRl = 0;
			for (int i = 0; i < maxAvailablePos; i++) {
				IteratingBufferedRunningLengthWord rlw = rlws[i];
				long size = rlw.size();
				if (size == 0) {
					maxAvailablePos = i;
					break;
				}
				minSize = Math.min(minSize, size);

				if (rlw.getRunningBit()) {
					long rl = rlw.getRunningLength();
					maxOneRl = Math.max(maxOneRl, rl);
					minZeroRl = 0;
					if (rl == 0 && size > 0) {
						numEmptyRl++;
					}
				}
				else {
					long rl = rlw.getRunningLength();
					minZeroRl = Math.min(minZeroRl, rl);
					if (rl == 0 && size > 0) {
						numEmptyRl++;
					}
				}
			}

			if (maxAvailablePos == 0) {
				break;
			}
			else if (maxAvailablePos == 1) {
				// only one bitmap is left so just write the rest of it out
				rlws[0].discharge(container);
				break;
			}

			if (maxOneRl > 0) {
				container.addStreamOfEmptyWords(true, maxOneRl);
				for (int i = 0; i < maxAvailablePos; i++) {
					IteratingBufferedRunningLengthWord rlw = rlws[i];
					rlw.discardFirstWords(maxOneRl);
				}
			}
			else if (minZeroRl > 0) {
				container.addStreamOfEmptyWords(false, minZeroRl);
				for (int i = 0; i < maxAvailablePos; i++) {
					IteratingBufferedRunningLengthWord rlw = rlws[i];
					rlw.discardFirstWords(minZeroRl);
				}
			}
			else {
				int index = 0;

				if (numEmptyRl == 1) {
					// if one rlw has dirty words to process and the rest have a
					// run of 0's we can write them out here
					IteratingBufferedRunningLengthWord emptyRl = null;
					long minNonEmptyRl = Long.MAX_VALUE;
					for (int i = 0; i < maxAvailablePos; i++) {
						IteratingBufferedRunningLengthWord rlw = rlws[i];
						long rl = rlw.getRunningLength();
						if (rl == 0) {
							assert emptyRl == null;
							emptyRl = rlw;
						}
						else {
							minNonEmptyRl = Math.min(minNonEmptyRl, rl);
						}
					}
					long wordsToWrite =
							minNonEmptyRl > minSize ? minSize : minNonEmptyRl;
					if (emptyRl != null)
						emptyRl.writeDirtyWords((int) wordsToWrite, container);
					index += wordsToWrite;
				}

				while (index < minSize) {
					long word = 0;
					for (int i = 0; i < maxAvailablePos; i++) {
						IteratingBufferedRunningLengthWord rlw = rlws[i];
						if (rlw.getRunningLength() <= index) {
							word |=
									rlw.getDirtyWordAt(index
											- (int) rlw.getRunningLength());
						}
					}
					container.add(word);
					index++;
				}
				for (int i = 0; i < maxAvailablePos; i++) {
					IteratingBufferedRunningLengthWord rlw = rlws[i];
					rlw.discardFirstWords(minSize);
				}
			}
		}
		container.setSizeInBits(maxSize);
	}

	/**
	 * For internal use.
	 * 
	 * @param initialWord
	 *            the initial word
	 * @param iterator
	 *            the iterator
	 * @param container
	 *            the container
	 */
	protected static void discharge(
			final BufferedRunningLengthWord initialWord,
			final EWAHIterator iterator, final BitmapStorage container) {
		BufferedRunningLengthWord runningLengthWord = initialWord;
		for (;;) {
			final long runningLength = runningLengthWord.getRunningLength();
			container.addStreamOfEmptyWords(runningLengthWord.getRunningBit(),
					runningLength);
			container.addStreamOfDirtyWords(iterator.buffer(),
					iterator.dirtyWords() + runningLengthWord.dirtywordoffset,
					runningLengthWord.getNumberOfLiteralWords());
			if (!iterator.hasNext())
				break;
			runningLengthWord = new BufferedRunningLengthWord(iterator.next());
		}
	}

	/**
	 * For internal use.
	 * 
	 * @param initialWord
	 *            the initial word
	 * @param iterator
	 *            the iterator
	 * @param container
	 *            the container
	 */
	private static void dischargeAsEmpty(
			final BufferedRunningLengthWord initialWord,
			final EWAHIterator iterator, final BitmapStorage container) {
		BufferedRunningLengthWord runningLengthWord = initialWord;
		for (;;) {
			final long runningLength = runningLengthWord.getRunningLength();
			container.addStreamOfEmptyWords(false, runningLength
					+ runningLengthWord.getNumberOfLiteralWords());
			if (!iterator.hasNext())
				break;
			runningLengthWord = new BufferedRunningLengthWord(iterator.next());
		}
	}

	/**
	 * set the bit at position i to true, the bits must be set in increasing
	 * order. For example, set(15) and then set(7) will fail. You must do set(7)
	 * and then set(15).
	 * 
	 * @param i
	 *            the index
	 * @return true if the value was set (always true when i>= sizeInBits()).
	 */
	public boolean fastSet(final int i) {
		if (i < this.sizeinbits)
			return false;
		// must I complete a word?
		if ((this.sizeinbits % 64) != 0) {
			final int possiblesizeinbits = (this.sizeinbits / 64) * 64 + 64;
			if (possiblesizeinbits < i + 1) {
				this.sizeinbits = possiblesizeinbits;
			}
		}
		addStreamOfEmptyWords(false, (i / 64) - this.sizeinbits / 64);
		final int bittoflip = i - (this.sizeinbits / 64 * 64);
		// next, we set the bit
		if ((this.rlw.getNumberOfLiteralWords() == 0)
				|| ((this.sizeinbits - 1) / 64 < i / 64)) {
			final long newdata = 1l << bittoflip;
			addLiteralWord(newdata);
		}
		else {
			this.buffer[this.actualsizeinwords - 1] |= 1l << bittoflip;
			// check if we just completed a stream of 1s
			if (this.buffer[this.actualsizeinwords - 1] == oneMask) {
				// we remove the last dirty word
				this.buffer[this.actualsizeinwords - 1] = 0;
				--this.actualsizeinwords;
				this.rlw.setNumberOfLiteralWords(this.rlw
						.getNumberOfLiteralWords() - 1);
				// next we add one clean word
				addEmptyWord(true);
			}
		}
		this.sizeinbits = i + 1;
		return true;
	}

	/**
	 * Same as set, except there are no restrictions on the order of setting
	 * bits.
	 * 
	 * @param i
	 */
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
					rlw.setRunningBit(true);
					rlw.setRunningLength(rlw.getRunningLength() + 1);
					rlw.setNumberOfLiteralWords(rlw.getNumberOfLiteralWords() - 1);
					// current RLW has no literals left, try to merge with
					// following RLW
					if (rlw.getNumberOfLiteralWords() == 0
							&& next != null
							&& next.getRunningBit()
							&& next.getRunningLength() + rlw.getRunningLength() < RunningLengthWord.largestrunninglengthcount) {
						if (rlw.equals(this.rlw))
							this.rlw.position = next.position;
						next.setRunningLength(next.getRunningLength()
								+ rlw.getRunningLength());
						shiftCompressedWordsLeft(rlw.position + 2, 2);
					}
					// merging doesn't work because of size
					// else just reduce length of rlw by 1
					else
						shiftCompressedWordsLeft(rlw.position + 2, 1);
				}
				// if last word increase following running length count if
				// possible
				else if (next != null
						&& next.getRunningLength()  
								< RunningLengthWord.largestrunninglengthcount
						&& (next.getRunningLength() == 0 || next
								.getRunningBit())
						&& literalPos == rlw.getNumberOfLiteralWords() - 1) {
					next.setRunningBit(true);
					next.setRunningLength(next.getRunningLength() + 1);
					rlw.setNumberOfLiteralWords(rlw.getNumberOfLiteralWords() - 1);
					// current RLW has no literals left, try to merge with
					// following
					if (rlw.getNumberOfLiteralWords() == 0
							&& next != null
							&& next.getRunningBit()) {
						long totalRunLen = next.getRunningLength() + rlw.getRunningLength(); 
						
						// merging into next possible?
						if (totalRunLen < RunningLengthWord.largestrunninglengthcount) {
							next.setRunningLength(next.getRunningLength()
									+ rlw.getRunningLength());
							shiftCompressedWordsLeft(rlw.position + 2, 2);
						}
						// run length to large, move run length from next to current
						// and shift next one to the left
						else {
							rlw.setRunningLength(RunningLengthWord.
									largestrunninglengthcount);
							next.setRunningLength(totalRunLen - RunningLengthWord
									.largestrunninglengthcount);
							shiftCompressedWordsLeft(rlw.position + 2, 1);
						}
					}
					// remove RLW
					else
						shiftCompressedWordsLeft(
								rlw.position + rlw.getNumberOfLiteralWords()
										+ 2, 1);
				}
				// cannot merge, have to create new RLW and adapt literal count
				// of current RLW
				else {
					int beforeLit = literalPos;
					int afterLit =
							rlw.getNumberOfLiteralWords() - literalPos - 1;

					if (log.isDebugEnabled()) {log.debug("split into " + beforeLit + " and " + afterLit);};

					RunningLengthWord newRlw = new RunningLengthWord(rlw);
					newRlw.position += literalPos + 1;
					newRlw.setRunningBit(true);
					newRlw.setRunningLength(1L);
					newRlw.setNumberOfLiteralWords(afterLit);

					rlw.setNumberOfLiteralWords(beforeLit);

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
					if (log.isDebugEnabled()) {log.debug("split because new 1's run");};
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
					prev.setNumberOfLiteralWords(prev.getNumberOfLiteralWords()
							+ newNumLiterals);
					this.buffer[rlw.position] = newdata;

					if (this.rlw.equals(rlw))
						this.rlw = prev;

					return;
				}
			}
			// No merging possible!
			// CASE 2) if previous run length = 0 then add new literal into
			// previous if previous still has space
			if (newRunLen == 0
					&& prev != null
					&& prev.getNumberOfLiteralWords() < RunningLengthWord.largestliteralcount) {
				prev.setNumberOfLiteralWords(prev.getNumberOfLiteralWords() + 1);
				rlw.setRunningLength(afterRunLen);
				shiftCompressedWordsRight(rlw.position, 1);
				this.buffer[prev.position + prev.getNumberOfLiteralWords()] =
						newdata;

				return;
			}

			// CASE 3) No merging possible, if following run length = 0, then
			// try to extend R with the new literal.
			if (afterRunLen == 0
					&& rlw.getNumberOfLiteralWords() < RunningLengthWord.largestliteralcount) {
				rlw.setNumberOfLiteralWords(rlw.getNumberOfLiteralWords() + 1);
				rlw.setRunningLength(newRunLen);
				shiftCompressedWordsRight(rlw.position + 1, 1);
				this.buffer[rlw.position + 1] = newdata;

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

				shiftCompressedWordsRight(rlw.position + 1, 1);
				shiftCompressedWordsRight(
						rlw.position + rlw.getNumberOfLiteralWords() + 1, 1);
				RunningLengthWord newRlw =
						new RunningLengthWord(this.buffer, rlw.position
								+ rlw.getNumberOfLiteralWords() + 1);
				rlw.array = this.buffer;

				newRlw.setNumberOfLiteralWords(1L);
				newRlw.setRunningLength(0);
				newRlw.setRunningBit(false);

				rlw.setRunningLength(newRunLen);

				this.buffer[rlw.position + 1] = newdata;

				if (newRlw.position > this.rlw.position)
					this.rlw.position = newRlw.position;
			}
			// new RLW also gets run length
			else {
				shiftCompressedWordsRight(wordPos + 1, 2);
				RunningLengthWord newRlw =
						new RunningLengthWord(this.buffer, wordPos + 2);
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

	/**
	 * Check some invariants in the encoding. The following situations are
	 * errors Two consecutive words with 1) both run length 0 and less than
	 * MAX_DIRTY_WORDS dirty words 2) both no dirty words, the same running bit,
	 * and less then MAX_RUNLENGTH run length encoded words
	 * 
	 * @return false if a violation occured
	 */
	public boolean checkInvariants() {
		RunningLengthWord rlw, prev;

		try {
			EWAHIterator i =
					new EWAHIterator(this.buffer, this.actualsizeinwords);
			// test that actualsizeinwords complies with info in headers and
			// test that literal number is not > max
			while (i.hasNext()) {
				RunningLengthWord w = i.next();
				if (i.dirtyWords() > actualsizeinwords) {
					log.error(i.dirtyWords() + " larger than actual "
							+ actualsizeinwords);
					log.error(toDebugString());
					return false;
				}

				if (i.pointer > actualsizeinwords) {
					log.error("pointer " + i.pointer + " larger than actual "
							+ actualsizeinwords);
					log.error(toDebugString());
					return false;
				}

				if (w.getNumberOfLiteralWords() > RunningLengthWord.largestrunninglengthcount) {
					log.error("larger than max literals"
							+ w.getNumberOfLiteralWords());
					log.error(toDebugString());
					return false;
				}

				if (w.getRunningLength() > RunningLengthWord.largestrunninglengthcount) {
					log.error("larger than max running length "
							+ w.getRunningLength());
					log.error(toDebugString());
					return false;
				}
			}

			// check adjacent words for errors
			rlw = new RunningLengthWord(buffer, 0);
			prev = rlw;
			rlw = rlw.getNext();

			while (rlw.position < actualsizeinwords
					&& rlw.position + rlw.getNumberOfLiteralWords() < actualsizeinwords) {
				// case 1) second word has no running length -> first one should
				// have max literal count
				if (rlw.getRunningLength() == 0
						&& prev.getNumberOfLiteralWords() < RunningLengthWord.largestliteralcount) {
					log.error(prev.getNumberOfLiteralWords()
							+ " dirty words followed by "
							+ rlw.getNumberOfLiteralWords()
							+ " number of dirty words " + "\n\n"
							+ toDebugString());
					return false;
				}
				// case 2) both have running length for same bit and first one
				// has
				// no literals -> first one should have max running length
				if (prev.getRunningLength() > 0
						&& rlw.getRunningLength() > 0
						&& prev.getNumberOfLiteralWords() == 0
						&& prev.getRunningBit() == rlw.getRunningBit()
						&& prev.getRunningLength() < RunningLengthWord.largestrunninglengthcount) {
					log.error("Two running length for same bit of length "
							+ prev.getRunningLength() + " and "
							+ rlw.getRunningLength() + "\n\n" + toDebugString());
					return false;
				}
				prev = rlw;
				rlw = rlw.getNext();
			}

			if (!prev.equals(this.rlw)) {
				log.error("Last word should have been " + prev.toString()
						+ " but was " + this.rlw.toString());
				return false;
			}

			// the largest bit set == sizeinbits
			IntIterator it = intIterator();
			int greatest = -1;
			while (it.hasNext()) {
				greatest = it.next();
			}
			if (this.sizeinbits != greatest + 1) {
				log.error("sizein bits " + sizeinbits
						+ " but largest value is " + greatest + "\n\n"
						+ toDebugString());
				return false;
			}
		}
		catch (Exception e) {
			LoggerUtil.logException(e, log);
			log.error(bufferToString());
		}

		return true;
	}

	public void setRange(final boolean value, int start, int end) {
		// TODO
	}

	/**
	 * Shift all word following and including startWord to shift positions to
	 * the left ( effectively overwriting previous content).
	 * 
	 * @param startWord
	 * @param shift
	 */
	public void shiftCompressedWordsLeft(final int startWord, final int shift) {
		System.arraycopy(this.buffer, startWord, this.buffer,
				startWord - shift, actualsizeinwords - startWord);

		// zero remaining words
		for (int i = actualsizeinwords - 1; i >= actualsizeinwords - shift; i--)
			this.buffer[i] = 0;

		actualsizeinwords -= shift;
		if (startWord <= rlw.position)
			rlw.position -= shift;
	}

	/**
	 * Shift the compressed words of this bitset to the right in the buffer to
	 * make space for new RLWs.
	 * 
	 * @param startWord
	 *            start shifting from here
	 * @param shift
	 *            shift this many positions
	 */
	public void shiftCompressedWordsRight(final int startWord, final int shift) {
		// need to enlarge buffer?
		while (buffer.length < actualsizeinwords + shift) {
			final long oldbuffer[] = this.buffer;
			this.buffer = new long[oldbuffer.length * 2];
			System.arraycopy(oldbuffer, 0, this.buffer, 0, actualsizeinwords);
			this.rlw.array = this.buffer;
		}
		System.arraycopy(this.buffer, startWord, this.buffer,
				startWord + shift, actualsizeinwords - startWord);
		for (int i = 0; i < shift; i++)
			this.buffer[startWord + i] = 0;

		actualsizeinwords += shift;
		if (log.isDebugEnabled())
			if (log.isDebugEnabled()) {log.debug(this.rlw.getNumberOfLiteralWords());};

		// adapt position of last RLW unless we shifted literal words from the
		// last RLW
		if (this.rlw.position >= startWord)
			this.rlw.position += shift;
	}

	/**
	 * Returns true if the bit at position bitpos is set. False otherwise.
	 * 
	 * @param bitpos
	 * @return Returns true if the bit at position bitpos is set. False
	 *         otherwise.
	 */
	public boolean get(int bitpos) {
		if (bitpos >= sizeinbits)
			return false;

		EWAHIterator iter = new EWAHIterator(this.buffer, actualsizeinwords);
		RunningLengthWord rlw;
		while (iter.hasNext()) {
			rlw = iter.next();
			// found the run length word
			if (rlw.size() * 64 >= bitpos) {
				int wordpos = 1;

				if (bitpos < rlw.getRunningLength() * 64)
					return rlw.getRunningBit();

				bitpos -= rlw.getRunningLength() * 64;
				wordpos += bitpos / 64;
				long bit = 1L << (bitpos % 64);
				return (this.buffer[rlw.position + wordpos] & bit) != 0L;
			}
			bitpos -= rlw.size() * 64;
		}

		// keep compiler quit
		return false;
	}

	public EWAHCompressedBitmap slice(final int start, final int length) {
		EWAHIterator iter = new EWAHIterator(this.buffer, actualsizeinwords);
		EWAHCompressedBitmap result = new EWAHCompressedBitmap();
		RunningLengthWord startWord = null, endWord, rlw;
		int bitpos = 0, startOffset, endSkip;

		// find start position in compressed bitset
		while (iter.hasNext() && bitpos < start) {
			startWord = iter.next();
			bitpos += startWord.size() * 64;
		}
		endWord = new RunningLengthWord(startWord);
		startOffset = bitpos - start;

		// find end position
		while (iter.hasNext() && bitpos < start + length) {
			endWord = iter.next();
			bitpos += endWord.size() * 64;
		}
		endSkip = bitpos - (start + length);

		// copy RLWs
		int copyLen =
				endWord.position + endWord.getNumberOfLiteralWords()
						- startWord.position;
		result.buffer = new long[copyLen + 2];
		result.actualsizeinwords = copyLen;
		result.rlw =
				new RunningLengthWord(result.buffer, endWord.position
						- startWord.position);
		System.arraycopy(this.buffer, startWord.position, result.buffer, 0,
				copyLen);

		// adapt words according to offset and skip
		rlw = new RunningLengthWord(result.buffer, 0);
		while (bitpos < length) {
			int wordOffset = startOffset / 64;
			int offset = startOffset % 64;

			// TODO

			rlw.next();
		}

		return result;
	}

	/**
	 * Adding words directly to the bitmap (for expert use). This is normally
	 * how you add data to the array. So you add bits in streams of 8*8 bits.
	 * 
	 * @param newdata
	 *            the word
	 * @return the number of words added to the buffer
	 */
	public int add(final long newdata) {
		return add(newdata, wordinbits);
	}

	/**
	 * For experts: You want to add many zeroes or ones? This is the method you
	 * use.
	 * 
	 * @param v
	 *            the boolean value
	 * @param number
	 *            the number
	 * @return the number of words added to the buffer
	 */
	public int addStreamOfEmptyWords(final boolean v, final long number) {
		if (number == 0)
			return 0;
		final boolean noliteralword = (this.rlw.getNumberOfLiteralWords() == 0);
		final long runlen = this.rlw.getRunningLength();
		if ((noliteralword) && (runlen == 0)) {
			this.rlw.setRunningBit(v);
		}
		int wordsadded = 0;
		if ((noliteralword) && (this.rlw.getRunningBit() == v)
				&& (runlen < RunningLengthWord.largestrunninglengthcount)) {
			long whatwecanadd =
					number < RunningLengthWord.largestrunninglengthcount
							- runlen ? number
							: RunningLengthWord.largestrunninglengthcount
									- runlen;
			this.rlw.setRunningLength(runlen + whatwecanadd);
			this.sizeinbits += whatwecanadd * wordinbits;
			if (number - whatwecanadd > 0)
				wordsadded += addStreamOfEmptyWords(v, number - whatwecanadd);
		}
		else {
			push_back(0);
			++wordsadded;
			this.rlw.position = this.actualsizeinwords - 1;
			final long whatwecanadd =
					number < RunningLengthWord.largestrunninglengthcount
							? number
							: RunningLengthWord.largestrunninglengthcount;
			this.rlw.setRunningBit(v);
			this.rlw.setRunningLength(whatwecanadd);
			this.sizeinbits += whatwecanadd * wordinbits;
			if (number - whatwecanadd > 0)
				wordsadded += addStreamOfEmptyWords(v, number - whatwecanadd);
		}
		return wordsadded;
	}

	/**
	 * Same as addStreamOfDirtyWords, but the words are negated.
	 * 
	 * @param data
	 *            the dirty words
	 * @param start
	 *            the starting point in the array
	 * @param number
	 *            the number of dirty words to add
	 * @return how many (compressed) words were added to the bitmap
	 */
	public long addStreamOfNegatedDirtyWords(final long[] data,
			final long start, final long number) {
		if (number == 0)
			return 0;
		final long NumberOfLiteralWords = this.rlw.getNumberOfLiteralWords();
		final long whatwecanadd =
				number < RunningLengthWord.largestliteralcount
						- NumberOfLiteralWords ? number
						: RunningLengthWord.largestliteralcount
								- NumberOfLiteralWords;
		this.rlw.setNumberOfLiteralWords(NumberOfLiteralWords + whatwecanadd);
		final long leftovernumber = number - whatwecanadd;
		negative_push_back(data, (int) start, (int) whatwecanadd);
		this.sizeinbits += whatwecanadd * wordinbits;
		long wordsadded = whatwecanadd;
		if (leftovernumber > 0) {
			push_back(0);
			this.rlw.position = this.actualsizeinwords - 1;
			++wordsadded;
			wordsadded +=
					addStreamOfDirtyWords(data, start + whatwecanadd,
							leftovernumber);
		}
		return wordsadded;
	}

	/**
	 * if you have several dirty words to copy over, this might be faster.
	 * 
	 * @param data
	 *            the dirty words
	 * @param start
	 *            the starting point in the array
	 * @param number
	 *            the number of dirty words to add
	 * @return how many (compressed) words were added to the bitmap
	 */
	public long addStreamOfDirtyWords(final long[] data, final long start,
			final long number) {
		if (number == 0)
			return 0;
		final long NumberOfLiteralWords = this.rlw.getNumberOfLiteralWords();
		final long whatwecanadd =
				number < RunningLengthWord.largestliteralcount
						- NumberOfLiteralWords ? number
						: RunningLengthWord.largestliteralcount
								- NumberOfLiteralWords;
		this.rlw.setNumberOfLiteralWords(NumberOfLiteralWords + whatwecanadd);
		final long leftovernumber = number - whatwecanadd;
		push_back(data, (int) start, (int) whatwecanadd);
		this.sizeinbits += whatwecanadd * wordinbits;
		long wordsadded = whatwecanadd;
		if (leftovernumber > 0) {
			push_back(0);
			this.rlw.position = this.actualsizeinwords - 1;
			++wordsadded;
			wordsadded +=
					addStreamOfDirtyWords(data, start + whatwecanadd,
							leftovernumber);
		}
		return wordsadded;
	}

	/**
	 * Adding words directly to the bitmap (for expert use).
	 * 
	 * @param newdata
	 *            the word
	 * @param bitsthatmatter
	 *            the number of significant bits (by default it should be 64)
	 * @return the number of words added to the buffer
	 */
	public int add(final long newdata, final int bitsthatmatter) {
		this.sizeinbits += bitsthatmatter;
		if (newdata == 0) {
			return addEmptyWord(false);
		}
		else if (newdata == oneMask) {
			return addEmptyWord(true);
		}
		else {
			return addLiteralWord(newdata);
		}
	}

	/**
	 * Returns the size in bits of the *uncompressed* bitmap represented by this
	 * compressed bitmap. Initially, the sizeInBits is zero. It is extended
	 * automatically when you set bits to true.
	 * 
	 * @return the size in bits
	 */
	public int sizeInBits() {
		return this.sizeinbits;
	}

	/**
	 * set the size in bits
	 * 
	 * @since 0.4.0
	 */
	public void setSizeInBits(final int size) {
		this.sizeinbits = size;
	}

	/**
	 * Change the reported size in bits of the *uncompressed* bitmap represented
	 * by this compressed bitmap. It is not possible to reduce the sizeInBits,
	 * but it can be extended. The new bits are set to false or true depending
	 * on the value of defaultvalue.
	 * 
	 * @param size
	 *            the size in bits
	 * @param defaultvalue
	 *            the default boolean value
	 * @return true if the update was possible
	 */
	public boolean setSizeInBits(final int size, final boolean defaultvalue) {
		if (size < this.sizeinbits)
			return false;
		// next loop could be optimized further
		if (defaultvalue)
			while (((this.sizeinbits % 64) != 0) && (this.sizeinbits < size)) {
				this.fastSet(this.sizeinbits);
			}
		final int leftover = size % 64;
		if (defaultvalue == false)
			this.addStreamOfEmptyWords(defaultvalue, (size / 64)
					- this.sizeinbits / 64 + (leftover != 0 ? 1 : 0));
		else {
			this.addStreamOfEmptyWords(defaultvalue, (size / 64)
					- this.sizeinbits / 64);
			final long newdata = (1l << leftover) + ((1l << leftover) - 1);
			this.addLiteralWord(newdata);
		}
		this.sizeinbits = size;
		return true;
	}

	/**
	 * Report the *compressed* size of the bitmap (equivalent to memory usage,
	 * after accounting for some overhead).
	 * 
	 * @return the size in bytes
	 */
	public int sizeInBytes() {
		return this.actualsizeinwords * 8;
	}

	/**
	 * For internal use (trading off memory for speed).
	 * 
	 * @param size
	 *            the number of words to allocate
	 * @return True if the operation was a success.
	 */
	protected boolean reserve(final int size) {
		if (size > this.buffer.length) {
			final long oldbuffer[] = this.buffer;
			this.buffer = new long[size];
			System.arraycopy(oldbuffer, 0, this.buffer, 0, oldbuffer.length);
			this.rlw.array = this.buffer;
			return true;
		}
		return false;
	}

	/**
	 * For internal use.
	 * 
	 * @param data
	 *            the word to be added
	 */
	private void push_back(final long data) {
		if (this.actualsizeinwords == this.buffer.length) {
			final long oldbuffer[] = this.buffer;
			this.buffer = new long[oldbuffer.length * 2];
			System.arraycopy(oldbuffer, 0, this.buffer, 0, oldbuffer.length);
			this.rlw.array = this.buffer;
		}
		this.buffer[this.actualsizeinwords++] = data;
	}

	/**
	 * For internal use.
	 * 
	 * @param data
	 *            the array of words to be added
	 * @param start
	 *            the starting point
	 * @param number
	 *            the number of words to add
	 */
	private void
			push_back(final long[] data, final int start, final int number) {
		while (this.actualsizeinwords + number >= this.buffer.length) {
			final long oldbuffer[] = this.buffer;
			this.buffer = new long[oldbuffer.length * 2];
			System.arraycopy(oldbuffer, 0, this.buffer, 0, oldbuffer.length);
			this.rlw.array = this.buffer;
		}
		System.arraycopy(data, start, this.buffer, this.actualsizeinwords,
				number);
		this.actualsizeinwords += number;
	}

	/**
	 * For internal use.
	 * 
	 * @param data
	 *            the array of words to be added
	 * @param start
	 *            the starting point
	 * @param number
	 *            the number of words to add
	 */
	private void negative_push_back(final long[] data, final int start,
			final int number) {
		while (this.actualsizeinwords + number >= this.buffer.length) {
			final long oldbuffer[] = this.buffer;
			this.buffer = new long[oldbuffer.length * 2];
			System.arraycopy(oldbuffer, 0, this.buffer, 0, oldbuffer.length);
			this.rlw.array = this.buffer;
		}
		for (int k = 0; k < number; ++k)
			this.buffer[this.actualsizeinwords + k] = ~data[start + k];
		this.actualsizeinwords += number;
	}

	/**
	 * For internal use.
	 * 
	 * @param v
	 *            the boolean value
	 * @return the storage cost of the addition
	 */
	private int addEmptyWord(final boolean v) {
		final boolean noliteralword = (this.rlw.getNumberOfLiteralWords() == 0);
		final long runlen = this.rlw.getRunningLength();
		if ((noliteralword) && (runlen == 0)) {
			this.rlw.setRunningBit(v);
		}
		if ((noliteralword) && (this.rlw.getRunningBit() == v)
				&& (runlen < RunningLengthWord.largestrunninglengthcount)) {
			this.rlw.setRunningLength(runlen + 1);
			return 0;
		}
		push_back(0);
		this.rlw.position = this.actualsizeinwords - 1;
		this.rlw.setRunningBit(v);
		this.rlw.setRunningLength(1);
		return 1;
	}

	/**
	 * For internal use.
	 * 
	 * @param newdata
	 *            the dirty word
	 * @return the storage cost of the addition
	 */
	private int addLiteralWord(final long newdata) {
		final long numbersofar = this.rlw.getNumberOfLiteralWords();
		if (numbersofar >= RunningLengthWord.largestliteralcount) {
			push_back(0);
			this.rlw.position = this.actualsizeinwords - 1;
			this.rlw.setNumberOfLiteralWords(1);
			push_back(newdata);
			return 2;
		}
		this.rlw.setNumberOfLiteralWords(numbersofar + 1);
		push_back(newdata);
		return 1;
	}

	/**
	 * reports the number of bits set to true. Running time is proportional to
	 * compressed size (as reported by sizeInBytes).
	 * 
	 * @return the number of bits set to true
	 */
	public int cardinality() {
		int counter = 0;
		final EWAHIterator i =
				new EWAHIterator(this.buffer, this.actualsizeinwords);
		while (i.hasNext()) {
			RunningLengthWord localrlw = i.next();
			if (localrlw.getRunningBit()) {
				counter += wordinbits * localrlw.getRunningLength();
			}
			for (int j = 0; j < localrlw.getNumberOfLiteralWords(); ++j) {
				counter += Long.bitCount(i.buffer()[i.dirtyWords() + j]);
			}
		}
		return counter;
	}

	/**
	 * A string describing the bitmap.
	 * 
	 * @return the string
	 */
	@Override
	public String toString() {
		String ans =
				" EWAHCompressedBitmap, size in bits = " + this.sizeinbits
						+ " size in words = " + this.actualsizeinwords + "\n";
		final EWAHIterator i =
				new EWAHIterator(this.buffer, this.actualsizeinwords);
		while (i.hasNext()) {
			RunningLengthWord localrlw = i.next();
			if (localrlw.getRunningBit()) {
				ans += localrlw.getRunningLength() + " 1x11\n";
			}
			else {
				ans += localrlw.getRunningLength() + " 0x00\n";
			}
			ans += localrlw.getNumberOfLiteralWords() + " dirties\n";
		}
		return ans;
	}

	/**
	 * Return the bitset as a String of 0's and 1's.
	 */
	public String toBitsString() {
		StringBuffer buf = new StringBuffer(actualsizeinwords * wordinbits);
		int bitPos = 0;
		final EWAHIterator iter =
				new EWAHIterator(this.buffer, actualsizeinwords);

		while (iter.hasNext()) {
			RunningLengthWord rlw = iter.next();
			if (rlw.getRunningLength() > 0) {
				char bit;
				if (rlw.getRunningBit())
					bit = '1';
				else
					bit = '0';
				for (int i = 0; i < rlw.getRunningLength() * 64; i++) {
					buf.append(bit);
					if (i != 0 && i % 8 == 0)
						buf.append(' ');
				}
				bitPos += rlw.getRunningLength() * 64;
				buf.append(' ');
			}
			for (int i = 1; i <= rlw.getNumberOfLiteralWords(); i++) {
				bitPos += 64;
				if (bitPos > sizeinbits)
					buf.append(longToBitString(this.buffer[rlw.position + i],
							sizeinbits % 64));
				else {
					buf.append(longToBitString(this.buffer[rlw.position + i]));
					buf.append(' ');
				}
			}
		}

		return buf.toString();
	}

	/**
	 * Returns a bitwise representation of the compressed long array buffer
	 */
	public String bufferToString() {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < actualsizeinwords; i++) {
			buf.append("" + i + ": ");
			buf.append(longToBitString(this.buffer[i]));
			buf.append('\n');
		}

		return buf.toString();
	}

	public String longToBitString(long data) {
		return longToBitString(data, 64);
	}

	public String longToBitString(long data, int numBits) {
		long mask = 1L;
		StringBuffer buf = new StringBuffer();

		for (int j = 0; j < numBits; j++) {
			if (j != 0 && j % 8 == 0)
				buf.append(' ');
			if ((mask & data) != 0L)
				buf.append('1');
			else
				buf.append('0');
			mask <<= 1;
		}

		return buf.toString();
	}

	/**
	 * A more detailed string describing the bitmap (useful for debugging).
	 * 
	 * @return the string
	 */
	public String toDebugString() {
		String ans =
				" EWAHCompressedBitmap, size in bits = " + this.sizeinbits
						+ " size in words = " + this.actualsizeinwords
						+ " with last RLW " + this.rlw.toString() + "\n";
		final EWAHIterator i =
				new EWAHIterator(this.buffer, this.actualsizeinwords);
		while (i.hasNext()) {
			RunningLengthWord localrlw = i.next();
			ans += localrlw.position + ": ";
			if (localrlw.getRunningBit()) {
				ans += localrlw.getRunningLength() + " 1x11\n";
			}
			else {
				ans += localrlw.getRunningLength() + " 0x00\n";
			}
			long header = i.buffer()[i.rlw.position];
			ans += "\t" + longToBitString(header) + "\n";
			ans += localrlw.getNumberOfLiteralWords() + " dirties\n";
			for (int j = 0; j < localrlw.getNumberOfLiteralWords(); ++j) {
				long data = i.buffer()[i.dirtyWords() + j];
				ans += "\t" + longToBitString(data) + "\n";
			}
		}
		return ans;
	}

	/**
	 * Iterator over the set bits (this is what most people will want to use to
	 * browse the content). The location of the set bits is returned, in
	 * increasing order.
	 * 
	 * @return the int iterator
	 */
	public IntIterator intIterator() {
		return new BufferedEWAHintIterator(this);
	}

	/**
	 * Iterator over the set bits (this is what most people will want to use to
	 * browse the content). The location of the set bits is returned, in
	 * increasing order starting from bits after position start until position
	 * end.
	 * 
	 * @param start
	 *            skip this many bits from the front
	 * @param end
	 *            do not proceed after these bits
	 * @return the iterator
	 */
	public IntIterator intIterator(final int start, final int end) {
		return new BufferedRangeEWAHintIterator(this, start, end);
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public Iterator<Integer> iterator(final int start, final int end) {
		return new WrappedIntIterator(intIterator(start, end));
	}

	/**
	 * iterate over the positions of the true values. This is similar to
	 * intIterator(), but it uses Java generics.
	 * 
	 * @return the iterator
	 */
	public Iterator<Integer> iterator() {
		return new WrappedIntIterator(intIterator());
	}

	/**
	 * get the locations of the true values as one vector. (may use more memory
	 * than iterator())
	 * 
	 * @return the positions
	 */
	public List<Integer> getPositions() {
		final ArrayList<Integer> v = new ArrayList<Integer>();
		final EWAHIterator i =
				new EWAHIterator(this.buffer, this.actualsizeinwords);
		int pos = 0;
		while (i.hasNext()) {
			RunningLengthWord localrlw = i.next();
			if (localrlw.getRunningBit()) {
				for (int j = 0; j < localrlw.getRunningLength(); ++j) {
					for (int c = 0; c < wordinbits; ++c)
						v.add(new Integer(pos++));
				}
			}
			else {
				pos += wordinbits * localrlw.getRunningLength();
			}
			for (int j = 0; j < localrlw.getNumberOfLiteralWords(); ++j) {
				final long data = i.buffer()[i.dirtyWords() + j];
				for (long c = 0; c < wordinbits; ++c) {
					if (((1l << c) & data) != 0) {
						v.add(new Integer(pos));
					}
					++pos;
				}
			}
		}
		while ((v.size() > 0)
				&& (v.get(v.size() - 1).intValue() >= this.sizeinbits))
			v.remove(v.size() - 1);
		return v;
	}

	/**
	 * Check to see whether the two compressed bitmaps contain the same data.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o instanceof EWAHCompressedBitmap) {
			EWAHCompressedBitmap other = (EWAHCompressedBitmap) o;
			if (this.sizeinbits == other.sizeinbits
					&& this.actualsizeinwords == other.actualsizeinwords
					&& this.rlw.position == other.rlw.position) {
				for (int k = 0; k < this.actualsizeinwords; ++k)
					if (this.buffer[k] != other.buffer[k])
						return false;
				return true;
			}
		}
		if (o instanceof IBitSet) {
			IBitSet b = (IBitSet) o;
			if (this.sizeinbits != b.sizeInBits())
				return false;
			IntIterator i = intIterator();
			IntIterator j = b.intIterator();

			while (i.hasNext())
				if (i.next() != j.next())
					return false;
			return true;
		}

		return false;
	}

	/**
	 * Returns a customized hash code (based on Karp-Rabin). Naturally, if the
	 * bitmaps are equal, they will hash to the same value.
	 */
	@Override
	public int hashCode() {
		int karprabin = 0;
		final int B = 31;
		for (int k = 0; k < this.actualsizeinwords; ++k) {
			karprabin += B * karprabin + (this.buffer[k] & ((1l << 32) - 1));
			karprabin += B * karprabin + (this.buffer[k] >>> 32);
		}
		return this.sizeinbits ^ karprabin;
	}

	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		EWAHCompressedBitmap clone = null;
		try {
			clone = (EWAHCompressedBitmap) super.clone();
		}
		catch (CloneNotSupportedException e) {
			LoggerUtil.logException(e, log);
		}

		clone.buffer = this.buffer.clone();
		clone.actualsizeinwords = this.actualsizeinwords;
		clone.sizeinbits = this.sizeinbits;
		clone.rlw = new RunningLengthWord(clone.buffer, rlw.position);
		return clone;
	}

	/*
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException {
		deserialize(in);
	}

	/**
	 * Deserialize.
	 * 
	 * @param in
	 *            the DataInput stream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void deserialize(DataInput in) throws IOException {
		this.sizeinbits = in.readInt();
		this.actualsizeinwords = in.readInt();
		if (this.buffer.length < this.actualsizeinwords) {
			this.buffer = new long[this.actualsizeinwords];
		}
		for (int k = 0; k < this.actualsizeinwords; ++k)
			this.buffer[k] = in.readLong();
		this.rlw = new RunningLengthWord(this.buffer, in.readInt());
	}

	/*
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		serialize(out);
	}

	/**
	 * Serialize.
	 * 
	 * @param out
	 *            the DataOutput stream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void serialize(DataOutput out) throws IOException {
		out.writeInt(this.sizeinbits);
		out.writeInt(this.actualsizeinwords);
		for (int k = 0; k < this.actualsizeinwords; ++k)
			out.writeLong(this.buffer[k]);
		out.writeInt(this.rlw.position);
	}

	/**
	 * Report the size required to serialize this bitmap
	 * 
	 * @return the size in bytes
	 */
	public int serializedSizeInBytes() {
		return this.sizeInBytes() + 3 * 4;
	}

	/**
	 * Clear any set bits and set size in bits back to 0
	 */
	public void clear() {
		this.sizeinbits = 0;
		this.actualsizeinwords = 1;
		this.rlw.position = 0;
		// buffer is not fully cleared but any new set operations should
		// overwrite stale data
		this.buffer[0] = 0;
	}

	public static EWAHCompressedBitmap getSingleton(int i) {
		final EWAHCompressedBitmap setBit = new EWAHCompressedBitmap(1);
		setBit.fastSet(i);

		return setBit;
	}

	/**
	 * The Constant defaultbuffersize: default memory allocation when the object
	 * is constructed.
	 */
	static final int defaultbuffersize = 4;

	/** The buffer (array of 64-bit words) */
	long buffer[] = null;

	/** The actual size in words. */
	int actualsizeinwords = 1;

	/** sizeinbits: number of bits in the (uncompressed) bitmap. */
	int sizeinbits = 0;

	/** The current (last) running length word. */
	RunningLengthWord rlw = null;

	/** The Constant wordinbits represents the number of bits in a long. */
	public static final int wordinbits = 64;

	/** 1 bitmask **/
	public static final long oneMask = ~0l;

	@Override
	public boolean intersects(IBitSet other) {
		if (!(other instanceof EWAHCompressedBitmap))
			throw new ClassCastException();
		return intersects((EWAHCompressedBitmap) other);
	}

	@Override
	public IBitSet andNot(IBitSet other) {
		if (!(other instanceof EWAHCompressedBitmap))
			throw new ClassCastException();
		return andNot((EWAHCompressedBitmap) other);
	}

	@Override
	public IBitSet and(IBitSet other) {
		if (!(other instanceof EWAHCompressedBitmap))
			throw new ClassCastException();
		return and((EWAHCompressedBitmap) other);
	}

	@Override
	public IBitSet or(IBitSet other) {
		if (!(other instanceof EWAHCompressedBitmap))
			throw new ClassCastException();
		return or((EWAHCompressedBitmap) other);
	}

	@Override
	public int getByteSize() {
		return buffer.length * Long.SIZE / 8;
	}

	@Override
	public void readFromBitsString(String values) {
		int pos = 0;

		this.clear();
		for (char c : values.toCharArray()) {
			switch (c) {
			case '0':
				pos++;
				break;
			case '1':
				set(pos++);
				break;
			default:
				break;
			}
		}
	}
}

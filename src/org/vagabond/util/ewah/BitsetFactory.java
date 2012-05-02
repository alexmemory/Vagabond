package org.vagabond.util.ewah;

import org.vagabond.util.ewah.IBitSet.BitsetType;

public class BitsetFactory {

	public static IBitSet newBitset (BitsetType type) {
		switch(type) {
		case JavaBitSet:
			return new JavaUtilBitSet ();
		case EWAHBitSet:
			return new EWAHCompressedBitmap();
		default:
			return null;
		}
	}
	
	public static IBitSet newBitset (BitsetType type, String values) {
		IBitSet bitset;
		
		bitset = newBitset(type);
		bitset.readFromBitsString(values);
		
		return bitset;
	}
	
	public static IBitSet newBitset (BitsetType type, int bufSizeInBits) {
		switch(type) {
		case JavaBitSet:
			return new JavaUtilBitSet (bufSizeInBits);
		case EWAHBitSet:
			return new EWAHCompressedBitmap(bufSizeInBits / EWAHCompressedBitmap.wordinbits);
		default:
			return null;
		}		
	}
	
	public static Bitmap newBitsetView (IBitSet set, int start, int end) {
		return new BitsetView(set, start, end);
	}
}

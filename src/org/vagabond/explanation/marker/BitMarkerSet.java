package org.vagabond.explanation.marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.ewah.BitsetFactory;
import org.vagabond.util.ewah.IBitSet;
import org.vagabond.util.ewah.IBitSet.BitsetType;
import org.vagabond.util.ewah.IntIterator;

public class BitMarkerSet implements IMarkerSet {

	static Logger log = LogProviderHolder.getInstance().getLogger(
			BitMarkerSet.class);

	private MarkerSummary sum;
	private int hash = -1;
	private IBitSet markers;

	public BitMarkerSet() {
		init();
	}

	private void init() {
		markers = BitsetFactory.newBitset(BitsetType.EWAHBitSet);
		sum = null;
	}

	private void resetLazyFields() {
		sum = null;
		hash = -1;
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;

		if (other == this)
			return true;

		if (other instanceof BitMarkerSet) {
			BitMarkerSet oMarker = (BitMarkerSet) other;

			if (getSize() != oMarker.getSize())
				return false;

			IntIterator i, j;

			i = this.markers.intIterator();
			j = oMarker.markers.intIterator();

			while (i.hasNext()) {
				if (i.next() != j.next())
					return false;
			}

			return true;
		}

		return false;
	}

	public int hashCode() {
		if (hash == -1) {
			hash = markers.hashCode();
		}
		return hash;
	}

	public int getSize() {
		return markers.cardinality();
	}

	public int getNumElem() {
		return markers.cardinality();
	}

	public Set<ISingleMarker> getElems() {
		Set<ISingleMarker> result = new HashSet<ISingleMarker>();
		IntIterator intiterator = markers.intIterator();
		while (intiterator.hasNext()) {
			int temp_index = intiterator.next();
			try {
				ISingleMarker tempMarker =
						ScenarioDictionary.getInstance()
								.getAttrValueMarkerByIBitSet(temp_index);
				result.add(tempMarker);
			}
			catch (Exception e) {
				LoggerUtil.logException(e, log);
			}
		}
		return result;
	}

	public IBitSet getIBitSetElems() {
		return markers;
	}

	public List<ISingleMarker> getElemList() {
		return new ArrayList<ISingleMarker>(getElems());
	}

	@Override
	public IMarkerSet union(IMarkerSet other) {
		if (other instanceof BitMarkerSet) {
			markers = markers.or(((BitMarkerSet) other).markers);
		}
		else {
			for (ISingleMarker m : other)
				this.add(m);
		}
		resetLazyFields();
		return this;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		if (arg0 instanceof BitMarkerSet)
			return intersect((BitMarkerSet) arg0);

		Iterator<ISingleMarker> it = iterator();
		while (it.hasNext()) {
			it.next();
		}

		return false;
	}

	private boolean intersect(BitMarkerSet other) {
		IBitSet oldMarkers = markers;
		markers = markers.and(((BitMarkerSet) other).markers);
		return markers.equals(oldMarkers);
	}

	@Override
	public IMarkerSet intersect(IMarkerSet other) {
		if (other instanceof BitMarkerSet)
			markers = markers.and(((BitMarkerSet) other).markers);
		else {
			BitMarkerSet newresult = new BitMarkerSet();
			for (ISingleMarker m : other) {
				try {
					if (this.contains(m))
						newresult.add(m);
				}
				catch (Exception e) {
					LoggerUtil.logException(e, log);
				}
			}
			markers = newresult.markers;
		}
		resetLazyFields();
		return this;
	}

	public boolean contains(ISingleMarker marker) throws Exception {
		if (marker instanceof IAttributeValueMarker) {
			int bitPos =
					ScenarioDictionary.getInstance().attrMarkerToBitPos(
							(IAttributeValueMarker) marker);
			return markers.get(bitPos);
		}
		boolean hasSet = false;
		if (marker instanceof TupleMarker) {
			TupleMarker t = (TupleMarker) marker;
			int numAttr =
					ScenarioDictionary.getInstance().getTupleSize(t.getRelId());

			for (int i = 0; i < numAttr; i++) {
				int bitPos =
						ScenarioDictionary.getInstance().getOffset(
								t.getRelId(), i, t.getTid());
				if (!markers.get(bitPos)) {
					hasSet = true;
				}
			}

		}
		return hasSet;
	}

	public IMarkerSet diff(IMarkerSet other) {
		if (other instanceof BitMarkerSet)
			this.markers = this.markers.andNot(((BitMarkerSet) other).markers);
		else {
			for (ISingleMarker m : other)
				this.remove(m);
		}
		resetLazyFields();
		return this;
	}

	public boolean contains(String relName, String tid) throws Exception {
		return this.contains(MarkerFactory.newTupleMarker(relName, tid));
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("MarkerSet: {");

		IntIterator iterator = markers.intIterator();
		int tempIndex;
		while (iterator.hasNext()) {
			tempIndex = iterator.next();
			AttrValueMarker tempMarker;
			try {
				tempMarker =
						ScenarioDictionary.getInstance()
								.getAttrValueMarkerByIBitSet(tempIndex);
				String tempString =
						"('" + tempMarker.getRel() + "'("
								+ tempMarker.getRelId() + "),"
								+ tempMarker.getTid() + ",'"
								+ tempMarker.getAttrName() + "'("
								+ tempMarker.getAttrId() + ")),";
				result.append(tempString);

			}
			catch (Exception e) {
				LoggerUtil.logException(e, log);
			}
		}

		result.deleteCharAt(result.length() - 1);

		result.append("}");

		return result.toString();
	}

	public String toUserString() {
		StringBuffer result = new StringBuffer();

		Map<String, IMarkerSet> markerPerRel =
				new HashMap<String, IMarkerSet>();

		IntIterator iterator = markers.intIterator();
		int tempIndex;
		while (iterator.hasNext()) {
			tempIndex = iterator.next();
			AttrValueMarker tempMarker;
			try {
				tempMarker =
						ScenarioDictionary.getInstance()
								.getAttrValueMarkerByIBitSet(tempIndex);
				String rel = tempMarker.getRel();
				if (!markerPerRel.containsKey(rel)) {
					markerPerRel.put(rel, MarkerFactory.newMarkerSet());
				}
				markerPerRel.get(rel).add(tempMarker);
			}
			catch (Exception e) {
				LoggerUtil.logException(e, log);
			}
		}

		for (String rel : markerPerRel.keySet()) {
			result.append(" relation " + rel + " (");
			for (ISingleMarker marker : markerPerRel.get(rel)) {
				result.append(marker.toUserStringNoRel());
				result.append(", ");
			}
			result.delete(result.length() - 2, result.length());
			result.append(')');
		}

		return result.toString();
	}

	public IMarkerSet cloneSet() {
		BitMarkerSet cloneSet = new BitMarkerSet();
		cloneSet.markers = (IBitSet) this.markers.clone();
		cloneSet.resetLazyFields();

		return cloneSet;
	}

	public MarkerSummary getSummary() {
		if (sum == null)
			this.sum = MarkerFactory.newMarkerSummary(this);
		return sum;
	}

	public IMarkerSet subset(MarkerSummary sum) {
		BitMarkerSet result = new BitMarkerSet();
		result.resetLazyFields();
		IntIterator iterator = markers.intIterator();
		int tempIndex;
		while (iterator.hasNext()) {
			tempIndex = iterator.next();
			AttrValueMarker tempMarker;
			try {
				tempMarker =
						ScenarioDictionary.getInstance()
								.getAttrValueMarkerByIBitSet(tempIndex);
				if (sum.hasAttr(tempMarker))
					result.add(tempMarker);
			}
			catch (Exception e) {
				LoggerUtil.logException(e, log);
			}
		}
		return result;
	}

	@Override
	public boolean addAll(Collection<? extends ISingleMarker> arg0) {
		resetLazyFields();
		Iterator<? extends ISingleMarker> iterator = arg0.iterator();
		boolean result = true;
		while (iterator.hasNext()) {
			if (!this.add((ISingleMarker) iterator.next()))
				result = false;
		}
		return result;
	}

	@Override
	public void clear() {
		sum = null;
		IBitSet empty = BitsetFactory.newBitset(BitsetType.EWAHBitSet);
		markers = empty;
	}

	@Override
	public boolean contains(Object arg0) {
		if (arg0 instanceof ISingleMarker)
			try {
				return this.contains((ISingleMarker) arg0);
			}
			catch (Exception e) {
				LoggerUtil.logException(e, log);
			}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		boolean result = true;
		Iterator<?> iterator = arg0.iterator();
		while (iterator.hasNext())
			if (!this.contains(iterator.next()))
				result = false;
		return result;
	}

	@Override
	public boolean isEmpty() {
		boolean result = true;
		IntIterator iterator = markers.intIterator();
		while (iterator.hasNext())
			result = false;
		return result;
	}

	@Override
	public Iterator<ISingleMarker> iterator() {
		return new Iterator<ISingleMarker>() {

			private IntIterator in = markers.intIterator();

			@Override
			public boolean hasNext() {
				return in.hasNext();
			}

			@Override
			public ISingleMarker next() {
				try {
					return ScenarioDictionary.getInstance()
							.getAttrValueMarkerByIBitSet(in.next());
				}
				catch (Exception e) {
					LoggerUtil.logException(e, log);
				}
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public boolean remove(Object arg0) {
		resetLazyFields();
		boolean result = false;

		if ((ISingleMarker) arg0 instanceof IAttributeValueMarker) {
			int bitPos =
					ScenarioDictionary.getInstance().attrMarkerToBitPos(
							(IAttributeValueMarker) arg0);
			if (markers.get(bitPos)) {
				this.removeSingleBit(bitPos);
				result = true;
			}
		}

		if ((ISingleMarker) arg0 instanceof TupleMarker) {
			TupleMarker t = (TupleMarker) arg0;
			int numAttr =
					ScenarioDictionary.getInstance().getTupleSize(t.getRelId());
			for (int i = 0; i < numAttr; i++) {
				int bitPos;
				try {
					bitPos =
							ScenarioDictionary.getInstance().getOffset(
									t.getRelId(), i, t.getTid());
					if (markers.get(bitPos)) {
						this.removeSingleBit(bitPos);
						result = true;
					}
				}
				catch (Exception e) {
					LoggerUtil.logException(e, log);
				}
			}
		}

		return result;
	}

	public void removeSingleBit(int bitpos) {
		resetLazyFields();
		IntIterator iteratorStart = this.markers.intIterator(0, bitpos);
		IntIterator iteratorEnd =
				this.markers.intIterator(bitpos + 1,
						this.markers.getByteSize() * 8);
		IBitSet new_markers = BitsetFactory.newBitset(BitsetType.EWAHBitSet);
		while (iteratorStart.hasNext())
			new_markers.set(iteratorStart.next());
		while (iteratorEnd.hasNext())
			new_markers.set(iteratorEnd.next());
		markers = new_markers;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		resetLazyFields();
		Iterator<?> iterator = arg0.iterator();
		while (iterator.hasNext()) {
			if (!this.remove(iterator.next()))
				return false;
		}
		return true;
	}

	@Override
	public int size() {
		return markers.cardinality();
	}

	//
	@Override
	public Object[] toArray() {
		return toArray(new Object[0]);
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		if (arg0.length < size()) {
			List<T> list = new ArrayList<T>(size());
			IntIterator iterator = markers.intIterator();
			while (iterator.hasNext())
				try {
					list.add((T) ScenarioDictionary.getInstance()
							.getAttrValueMarkerByIBitSet(iterator.next()));
				}
				catch (Exception e) {
					LoggerUtil.logException(e, log);
				}
			return (T[]) list.toArray();
		}
		else {
			IntIterator iterator = markers.intIterator();
			int counter = 0;
			while (iterator.hasNext())
				try {
					arg0[counter++] =
							(T) ScenarioDictionary.getInstance()
									.getAttrValueMarkerByIBitSet(
											iterator.next());
				}
				catch (Exception e) {
					LoggerUtil.logException(e, log);
				}
			return arg0;
		}
	}

	@Override
	public boolean add(ISingleMarker marker) {
		hash = -1;
		if (marker instanceof IAttributeValueMarker) {
			int bitPos =
					ScenarioDictionary.getInstance().attrMarkerToBitPos(
							(IAttributeValueMarker) marker);
			if (markers.get(bitPos))
				return false;
			markers.set(bitPos);
			return true;
		}
		if (marker instanceof TupleMarker) {
			TupleMarker t = (TupleMarker) marker;
			int numAttr =
					ScenarioDictionary.getInstance().getTupleSize(t.getRelId());
			boolean hasSet = true;

			for (int i = 0; i < numAttr; i++) {
				int bitPos;
				try {
					bitPos =
							ScenarioDictionary.getInstance().getOffset(
									t.getRelId(), i, t.getTid());
					if (markers.get(bitPos))
						hasSet = false;
					markers.set(bitPos);
				}
				catch (Exception e) {
					LoggerUtil.logException(e, log);
				}
			}
			return hasSet;
		}
		return false;
	}

	@Override
	public boolean add(int relId, int attrId, int tidId) {
		int bitPos = ScenarioDictionary.getInstance().getOffset(relId, attrId, tidId);
		
		if (markers.get(bitPos))
			return false;
		markers.set(bitPos);
		return true;
	}

}

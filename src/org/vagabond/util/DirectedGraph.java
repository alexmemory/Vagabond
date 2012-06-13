package org.vagabond.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.vagabond.util.ewah.Bitmap;
import org.vagabond.util.ewah.IntIterator;

public class DirectedGraph<T> extends Graph<T> {

	public DirectedGraph(boolean isSparse) {
		super(isSparse);
	}
	

	
	public void addEdge(T node, T other) {
		edges.set(nodeIds.getId(node), nodeIds.getId(other));
	}

	public void addEdge(int nodeId, int otherId) {
		assert (nodeId >= 0 && nodeId < getNumNodes() 
				&& otherId >= 0 && otherId < getNumNodes());
		edges.set(nodeId, otherId);
	}

	@Override
	public boolean hasEdge(int nodeId) {
		return hasIncomingEdge(nodeId) || hasOutgoingEdge(nodeId);
	}
	
	public boolean hasIncomingEdge(int nodeId) {
		return edges.firstOneInCol(nodeId) != -1;
	}
	
	public boolean hasOutgoingEdge(int nodeId) {
		return edges.firstOneInRow(nodeId) != -1;
	}
	
	
	
	public IntList getChildNodes (int nodeId) {
		Bitmap v = edges.getReadonlyRow(nodeId);
		IntIterator i = v.intIterator();
		IntList result = new ArrayIntList();
		
		while(i.hasNext())
			result.add(i.next());
		return result;
	}
	
	public IntList topologicalSortIds () {
		return null;
	}
	
	public List<T> topologicalSort () throws Exception {
		List<T> result = new ArrayList<T> (getNumNodes());
		IntList todo = new ArrayIntList ();
		DynamicBitMatrix edgeMarker = new DynamicBitMatrix(edges.getRows(), edges.getCols());
		
		for(int i = 0; i < nodeIds.size(); i++) {
			if (!hasIncomingEdge(i))
				todo.add(i);
		}
		
		while(todo.size() > 0) {
			int node = todo.removeElementAt(0);
			result.add(nodeIds.get(node));
			
			IntList children = getChildNodes(node);
			for(int i = 0; i < children.size(); i++) {
				int child = children.get(i);
				edgeMarker.set(node, child);
				// all edges deleted?
				if (edgeMarker.numOnesInCol(child) == edges.numOnesInCol(child))
					todo.add(child);
			}
		}
		
		if (edgeMarker.numOnes() != edges.numOnes())
			throw new Exception("Cannot produce topological order, because of " +
					"cycles in the graph:\n\n" + toString());
		
		return result;
	}



	public boolean hasIncomingEdge(T node) {
		return hasIncomingEdge(nodeIds.getId(node));
	}
	
	public boolean hasOutgoingEdge(T node) {
		return hasOutgoingEdge(nodeIds.getId(node));
	}
}

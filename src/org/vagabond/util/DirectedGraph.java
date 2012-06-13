package org.vagabond.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.adapters.ListIntList;

public class DirectedGraph<T> extends Graph<T> {

	public DirectedGraph(boolean isSparse) {
		super(isSparse);
	}
	
	public void addNodesAndEdge (T node, T other) {
		if (!nodeIds.containsVal(node))
			nodeIds.put(node);
		if (!nodeIds.containsVal(other))
			nodeIds.put(other);
		addEdge(node, other);
	}
	
	public void addEdge(T node, T other) {
		edges.set(nodeIds.getId(node), nodeIds.getId(other));
	}

	public void addEdge(int nodeId, int otherId) {
		assert (nodeId >= 0 && nodeId < getNumNodes() 
				&& otherId >= 0 && otherId < getNumNodes());
		edges.set(nodeId, otherId);
	}

	public boolean hasIncomingEdge(int nodeId) {
		return edges.firstOneInCol(nodeId) == -1;
	}
	
	public boolean hasOutgoingEdge(int nodeId) {
		return edges.firstOneInRow(nodeId) == -1;
	}
	
	public IntList topologicalSortIds () {
		return null;
	}
	
	public List<T> topologicalSort () {
		List<T> result = new ArrayList<T> (getNumNodes());
		IntList todo = new ArrayIntList ();
		DynamicBitMatrix edgeMarker = new DynamicBitMatrix(edges.getRows(), edges.getCols());
		
		for(int i = 0; i < nodeIds.size(); i++) {
			if (!hasIncomingEdge(i))
				todo.add(i);
		}
		
		while(todo.size() > 0) {
			
		}
		
		return result;
	}
}

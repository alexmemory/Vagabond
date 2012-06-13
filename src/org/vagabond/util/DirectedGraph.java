package org.vagabond.util;

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
		assert (nodeId >= 0 && nodeId < numNodes 
				&& otherId >= 0 && otherId < numNodes);
		edges.set(nodeId, otherId);
	}

	public IntList topologicalSort () {
		
	}
	
}

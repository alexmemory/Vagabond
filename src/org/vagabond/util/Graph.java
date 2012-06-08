package org.vagabond.util;

import org.vagabond.util.ewah.IBitSet;
import org.vagabond.util.ewah.IBitSet.BitsetType;

public class Graph<T> {

	private IdMap<T> nodeIds;
	private BitMatrix edges;
	private int numNodes;

	public Graph(int numNodes, boolean isSparse) {
		IBitSet.BitsetType type =
				isSparse ? BitsetType.EWAHBitSet : BitsetType.JavaBitSet;
		nodeIds = new IdMap<T>();
		this.numNodes = numNodes;
		edges = new BitMatrix(numNodes, numNodes, type);
	}

	public Graph(int numNodes, boolean isSparse, T[] nodes) throws Exception {
		this(numNodes, isSparse);
		for (T node : nodes)
			addNode(node);
	}

	public void addNode(T node) throws Exception {
		nodeIds.put(node);
		if (nodeIds.size() > numNodes)
			throw new Exception("Graph size was predetermined to " + numNodes);
	}

	public void addEdge(T node, T other) {
		edges.setSym(nodeIds.getId(node), nodeIds.getId(other));
	}

	public void addEdge(int nodeId, int otherId) {
		assert (nodeId >= 0 && nodeId < numNodes 
				&& otherId >= 0 && otherId < numNodes);
		edges.setSym(nodeId, otherId);
	}
	
	public int getNodeId (T node) {
		return nodeIds.getId(node);
	}

	public void hasEdge(T node, T other) {
		edges.get(nodeIds.getId(node), nodeIds.getId(other));
	}
	
	public void hasEdge(int nodeId, int otherId) {
		assert (nodeId >= 0 && nodeId < numNodes 
				&& otherId >= 0 && otherId < numNodes);
		edges.get(nodeId, otherId);
	}
}

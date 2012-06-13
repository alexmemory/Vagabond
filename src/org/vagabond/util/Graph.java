package org.vagabond.util;

import org.vagabond.util.ewah.IBitSet;
import org.vagabond.util.ewah.IBitSet.BitsetType;

public class Graph<T> {

	protected IdMap<T> nodeIds;
	protected DynamicBitMatrix edges;
	protected int numNodes;

	public Graph(boolean isSparse) {
		IBitSet.BitsetType type =
				isSparse ? BitsetType.EWAHBitSet : BitsetType.JavaBitSet;
		nodeIds = new IdMap<T>();
		this.numNodes = 0;
		edges = new DynamicBitMatrix(type);
	}

	public Graph(boolean isSparse, T[] nodes) throws Exception {
		this(isSparse);
		for (T node : nodes)
			addNode(node);
	}

	public void addNode(T node) throws Exception {
		nodeIds.put(node);
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
	
	public void deleteEdge(int nodeId, int otherId) {
		assert (nodeId >= 0 && nodeId < numNodes 
				&& otherId >= 0 && otherId < numNodes);
		//TODO
	}
}

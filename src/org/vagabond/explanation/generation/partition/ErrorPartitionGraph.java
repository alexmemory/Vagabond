package org.vagabond.explanation.generation.partition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.vagabond.explanation.marker.ISchemaMarker;
import org.vagabond.explanation.marker.MarkerSummary;
import org.vagabond.explanation.marker.ScenarioDictionary;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.MappingGraph;
import org.vagabond.mapping.model.MappingGraph.MappingGraphRel;
import org.vagabond.util.BitMatrix;
import org.vagabond.util.IdMap;
import org.vagabond.util.ewah.Bitmap;
import org.vagabond.util.ewah.IntIterator;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;

public class ErrorPartitionGraph {

	public enum ErrorGraphNodeType {
		SourceAttr,
		TargetAttr,
		MappingVar
	}
	
	public class ErrorNode {
		
		ErrorGraphNodeType type;
		int mapOrRel;
		int attrOrVar;
		int hashValue = -1;
		String asString;
		
		public ErrorNode (ErrorGraphNodeType type, int mapOrRel, int attrOrVar) {
			this.type = type;
			this.mapOrRel = mapOrRel;
			this.attrOrVar = attrOrVar;
		}
		
		public ErrorNode (ErrorGraphNodeType type, String mapOrRelName, String attrOrVarName) throws Exception {
			this.type = type;
			switch (type) {
			case SourceAttr:
			case TargetAttr:
				this.mapOrRel = ScenarioDictionary.getInstance().getMapId(mapOrRelName);
				this.attrOrVar = ScenarioDictionary.getInstance().getVarId(mapOrRelName, attrOrVarName);
				break;
			case MappingVar:
				this.mapOrRel = ScenarioDictionary.getInstance().getRelId(mapOrRelName);
				this.attrOrVar = ScenarioDictionary.getInstance().getAttrId(mapOrRelName, attrOrVarName);
				break;
			}
		}
		
		@Override
		public int hashCode () {
			if (hashValue == -1) {
				hashValue = mapOrRel * 13;
				hashValue = (13 * hashValue) + attrOrVar;
				hashValue = (13 * hashValue) + type.hashCode();
			}
			
			return hashValue;
		}
		
		public String toString () {
			String result = null;
			
			switch(type) {
			case SourceAttr:
				result = "S:";
				break;
			case TargetAttr:
				result = "T:";
				break;
			case MappingVar:
				result = "M:";
				break;
			}
			result = result + mapOrRel + "." + attrOrVar;
			
			return result; 
		}
	}
	
	private IdMap<ErrorNode> nodes;
	private int[][] nodeIndex; 
	private int sourceOffset;
	private int targetOffset;
	private BitMatrix edges;
	private BitMatrix components = null; 
	
	public ErrorPartitionGraph () throws Exception {
		this(MapScenarioHolder.getInstance());
	}
	
	public ErrorPartitionGraph (MapScenarioHolder doc) throws Exception {
		int numNodes = ScenarioDictionary.getInstance().getTotalAttrCount() 
				+ ScenarioDictionary.getInstance().getTotalVarCount();
		int numMapPlusRel = ScenarioDictionary.getInstance().getMapCount() + 
				ScenarioDictionary.getInstance().getRelCount();
		
		sourceOffset = ScenarioDictionary.getInstance().getMapCount();
		targetOffset = ScenarioDictionary.getInstance().getSchemaRelCount(true) + sourceOffset;
		
		edges = new BitMatrix (numNodes, numNodes);
		nodes = new IdMap<ErrorNode> ();
		nodeIndex = new int[numMapPlusRel][];
		
		constructFromScenario(doc);
	}
	
	private void constructFromScenario (MapScenarioHolder doc) throws Exception {
		int id, numElem, row = 0;
		MappingGraph mG;
		
		// create nodes that are addressable by arithmetics over the ids of attrs and vars
		// use this fact to directly set the edges in the bit matrix
		for(MappingType m: doc.getDocument().getMappingScenario().getMappings().getMappingArray()) {
			id = ScenarioDictionary.getInstance().getMapId(m.getId());
			numElem = ScenarioDictionary.getInstance().getNumVars(id);
			nodeIndex[id] = new int[numElem];
			
			for(int i = 0; i < numElem; i++) {
				nodes.put(new ErrorNode(ErrorGraphNodeType.MappingVar, id, i));
				nodeIndex[id][i] = row;
				
				row++;
			}
		}
		
		for(RelationType r: doc.getDocument().getMappingScenario().getSchemas().getSourceSchema().getRelationArray()) {
			id = ScenarioDictionary.getInstance().getRelId(r.getName());
			numElem = ScenarioDictionary.getInstance().getTupleSize(id);
			nodeIndex[id + sourceOffset] = new int[numElem];
			
			for(int i = 0; i < numElem; i++) {
				nodes.put(new ErrorNode(ErrorGraphNodeType.SourceAttr, id, i));
				nodeIndex[id + sourceOffset][i] = row++;
			}
		}
		
		for(RelationType r: doc.getDocument().getMappingScenario().getSchemas().getTargetSchema().getRelationArray()) {
			id = ScenarioDictionary.getInstance().getRelId(r.getName());
			numElem = ScenarioDictionary.getInstance().getTupleSize(id);
			nodeIndex[id + sourceOffset] = new int[numElem];
			
			for(int i = 0; i < numElem; i++) {
				nodes.put(new ErrorNode(ErrorGraphNodeType.TargetAttr, id, i));
				nodeIndex[id + sourceOffset][i] = row++;
			}
		}

		// create edges
		for(MappingType m: doc.getDocument().getMappingScenario().getMappings().getMappingArray()) {
			id = ScenarioDictionary.getInstance().getMapId(m.getId());
			numElem = ScenarioDictionary.getInstance().getNumVars(id);
			mG = doc.getGraphForMapping(m);
			
			for(int i = 0; i < numElem; i++) {
				String varName = ScenarioDictionary.getInstance().getVarName(id, i);
				row = nodeIndex[id][i];
				
				createEdgesForAtoms(row, mG.getForeachAtomsForVar(varName), varName, false);
				createEdgesForAtoms(row, mG.getExistsAtomsForVar(varName), varName, true);
				
				for(int j = i + 1; j < numElem; j++)
					edges.setSym(row, nodeIndex[id][j]);
			}
		}
	}
	
	private void createEdgesForAtoms (int row, Set<MappingGraphRel> atoms, String varName, boolean target) throws Exception {
		int col;
		int relId;
		
		for(MappingGraphRel atom: atoms) {
			relId = ScenarioDictionary.getInstance().getRelId(atom.getRelName()); 
			
			for(int i = 0; i < atom.getVars().size(); i++) {
				if (atom.getVars().get(i).equals(varName)) {
					col = nodeIndex[relId + sourceOffset][i]; 
					edges.setSym(row, col);
				}
			}
		}
	}
	
	public List<MarkerSummary> paritionAttrs (MarkerSummary summary) throws Exception {
		List<MarkerSummary> temp = new ArrayList<MarkerSummary> ();
		List<MarkerSummary> result = new ArrayList<MarkerSummary> ();
		
		getComponents();
		for(int i = 0; i < components.getRows(); i++) {
			temp.add(new MarkerSummary());
		}
		
		for(ISchemaMarker m: summary)
			temp.get(getPartitionForAttr(m)).add(m);
		
		while(temp.contains(null))
			temp.remove(null);
		
		
		for(MarkerSummary m: temp) {
			if (!m.isEmpty())
				result.add(m);
		}
		
		return result;
	}
	
	private int getComponent (int node) throws Exception {
		getComponents();
		return components.firstOneInCol(node);
	}
	
	public int getPartitionForAttr (ISchemaMarker m) throws Exception {
		int node = nodeIndex[m.getRelId() + sourceOffset][m.getAttrId()];
		return getComponent(node);
	}
	
	public ErrorNode getNode (ISchemaMarker m) {
		return nodes.get(nodeIndex[m.getRelId() + sourceOffset][m.getAttrId()]);
	}
	
	public List<ErrorNode> getNodesForComponents (ErrorNode node) {
		List<ErrorNode> result = new ArrayList<ErrorNode> (nodes.size() 
				/ components.getRows());
		int id = nodes.getId(node);
		Bitmap comp = components.getReadonlyRow(components.firstOneInCol(id));
		IntIterator iter = comp.intIterator();
		
		while(iter.hasNext())
			result.add(nodes.get(iter.next()));
		
		return result;
	}
	
	public int getNumComponents () throws Exception {
		return getComponents().getRows();
	}
	
	public BitMatrix getComponents () throws Exception {
		Stack<Integer> conNodes = new Stack<Integer> (); 
		int[] comps = new int[nodes.size()];
		int curComp = -1; 
		
		if(components != null)
			return components;
		
		for(int i = 0; i < nodes.getSize(); i++)
			comps[i] = -1;
			
		for(int i = 0; i < comps.length; i++) {
			// node is not part of component?
			if (comps[i] == -1) {
				comps[i] = ++curComp;
				conNodes.push(i);
				
				// find all nodes in component
				while(!conNodes.isEmpty()) {
					int node = conNodes.pop();
					
					for(IntIterator it = edges.getReadonlyRow(node).intIterator(); it.hasNext(); ) {
						int j = it.next();
						if(comps[j] == -1) {
							comps[j] = curComp;
							conNodes.push(j);
						}
					}
				}
			}
		}
		
		// create bitmaps for connected components
		components = new BitMatrix (curComp + 1, nodes.size());
		for(int i = 0; i < nodes.size(); i++)
			components.set(comps[i], i);
		
		return components;
	}
	
	public ErrorNode getNode(ErrorGraphNodeType type, String name, String elem) throws Exception {
		int mapOrRelId;
		int varOrAttrId;
		
		switch(type) {
		case MappingVar:
			mapOrRelId = ScenarioDictionary.getInstance().getMapId(name);
			varOrAttrId = ScenarioDictionary.getInstance().getVarId(name, elem);
			break;
		default:
			mapOrRelId = ScenarioDictionary.getInstance().getRelId(name);
			varOrAttrId = ScenarioDictionary.getInstance().getAttrId(mapOrRelId, elem);
			mapOrRelId += sourceOffset;
			break;
		}
		
		return nodes.get(nodeIndex[mapOrRelId][varOrAttrId]);
	}
	
	public ErrorNode getNode(ErrorGraphNodeType type, int id, int elemId) {
		if (!type.equals(ErrorGraphNodeType.MappingVar))
			id += sourceOffset;
		return nodes.get(nodeIndex[id][elemId]);
	}
	
	public boolean hasEdge (ErrorNode in, ErrorNode out) {
		return edges.get(nodes.getId(in), nodes.getId(out));
	}
	
	
	
	@Override
	public String toString () {
		StringBuffer result = new StringBuffer();
		
		for(int i = 0; i < nodes.size(); i++) {
			result.append("<" + nodes.get(i).toString() + "> ");
			result.append(edges.getReadonlyRow(i).toBitsString() + "\n");
		}
		
		return result.toString();
	}

	public IdMap<ErrorNode> getNodes() {
		return nodes;
	}
	
	public int[][] getNodeIndex () {
		return nodeIndex;
	}

	public int getSourceOffset() {
		return nodeIndex[sourceOffset][0];
	}

	public BitMatrix getEdges() {
		return edges;
	}
	
}

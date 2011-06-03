package org.vagabond.mapping.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.Pair;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;

public class MappingGraph {

	public class MappingGraphRel {
		private int pos;
		private boolean foreach;
		private String relName;
		private Vector<String> vars;
		
		public MappingGraphRel (int pos, boolean foreach, String relName, 
				Vector<String> vars) {
			this.pos = pos;
			this.foreach = foreach;
			this.relName = relName;
			this.vars = vars;
		}
		
		
		public int getPos() {
			return pos;
		}
		public void setPos(int pos) {
			this.pos = pos;
		}
		public boolean isForeach() {
			return foreach;
		}
		public void setForeach(boolean foreach) {
			this.foreach = foreach;
		}
		public String getRelName() {
			return relName;
		}
		public void setRelName(String relName) {
			this.relName = relName;
		}
		public Vector<String> getVars() {
			return vars;
		}
		public void setVars(Vector<String> vars) {
			this.vars = vars;
		}
		
		@Override
		public String toString () {
			return "Node(" + pos + "," + foreach + "," + relName + "," 
					+ vars + ")";
		}
	}

	static Logger log = Logger.getLogger(MappingGraph.class);
	
	private Vector<MappingGraphRel> foreachNodes;
	private Vector<MappingGraphRel> existsNodes;
	private Map<String, Set<MappingGraphRel>> varToNodesMap;
	
	
	public MappingGraph () {
		init();
	}
	
	public MappingGraph (MappingType map) {
		init();
		
		for(int i = 0; i < map.getForeach().sizeOfAtomArray(); i++) {
			MappingGraphRel node;
			RelAtomType atom;
			
			atom = map.getForeach().getAtomArray(i);
			node = new MappingGraphRel(i, true, atom.getTableref(), 
					CollectionUtils.makeVecFromArray(atom.getVarArray()));
			foreachNodes.add(node);
			
			// update var to node map
			updateVarToNodeMap(node);
		}
		
		for(int i = 0; i < map.getExists().sizeOfAtomArray(); i++) {
			MappingGraphRel node;
			RelAtomType atom;
			
			atom = map.getExists().getAtomArray(i);
			node = new MappingGraphRel(i, false, atom.getTableref(), 
					CollectionUtils.makeVecFromArray(atom.getVarArray()));
			existsNodes.add(node);
			
			// update var to node map
			updateVarToNodeMap(node);
		}
		
	}
	
	private void updateVarToNodeMap (MappingGraphRel node) {
		for (String var: node.vars) {
			if (!varToNodesMap.containsKey(var))
				varToNodesMap.put(var, new HashSet<MappingGraphRel> ());
			varToNodesMap.get(var).add(node);
		}
	}
	
	private void init () {
		foreachNodes = new Vector<MappingGraphRel> ();
		existsNodes = new  Vector<MappingGraphRel> ();
		varToNodesMap = new HashMap<String, Set<MappingGraphRel>> ();
	}
	
	public Set<MappingGraphRel> getForeachAtomsForVar (String var) {
		return getAtoms (true, var);
	}
	
	public Set<MappingGraphRel> getExistsAtomsForVar (String var) {
		return getAtoms (false, var);
	}
	
	private Set<MappingGraphRel> getAtoms (boolean foreach, String var) {
		Set<MappingGraphRel> result;
		
		result = new HashSet<MappingGraphRel> ();
		
		for(MappingGraphRel node: varToNodesMap.get(var)) {
			if (node.foreach == foreach)
				result.add(node);
		}
		
		return result;
	}
	
	public Set<String> getAllVars () {
		return varToNodesMap.keySet();
	}
	
	public Set<Pair<Integer,String>> getJoinVarsAndAtoms (String var) {
		Set<MappingGraphRel> sourceNodes;
		Set<Pair<Integer, String>> joinAttrs;
		Set<MappingGraphRel> visited;
		Stack<MappingGraphRel> todo;
		
		sourceNodes = getForeachAtomsForVar(var);
		visited = new HashSet<MappingGraphRel> (sourceNodes);
		todo = CollectionUtils.makeStack(sourceNodes);
		joinAttrs = new HashSet<Pair<Integer,String>> ();
		
		// traverse graph starting from the source atoms for var and
		// keeping track of visited nodes
		while(!todo.empty()) {
			MappingGraphRel node = todo.pop();
				
			visited.add(node);
			
			// for each var
			for(String curVar: node.vars) {
				if (!curVar.equals(var)) {
					// get neighbors that share this var
					for(MappingGraphRel neighbor
							: varToNodesMap.get(curVar)) {
						if (neighbor.foreach && !sourceNodes.contains(neighbor) 
								&& node != neighbor) {
							joinAttrs.add(new Pair<Integer,String>
									(neighbor.pos, curVar));
							if (!visited.contains(neighbor))
								todo.push(neighbor);
						}
					}
				}
			}
		}
		
		return joinAttrs;
	}

	public int[][][] getAtomPosToTargetPosMap (String targetRel) 
			throws Exception {
		for(MappingGraphRel node: existsNodes) {
			if (node.relName.equals(targetRel))
				return getAtomPosToTargetPosMap(node.pos);
		}
		
		throw new Exception ("Mapping does not map to relation <" 
				+ targetRel + ">");
	}
	
	public int[][][] getAtomPosToTargetPosMap (int existsPos) {
		int[][][] result = new int[foreachNodes.size()][][];
		MappingGraphRel node;
		
		for(int i = 0; i < result.length; i++) {
			node = foreachNodes.get(i);
			result[i] = new int[node.vars.size()][];
			
			for(int j = 0; j < node.vars.size(); j++) {
				String var = node.vars.get(j);
				Set<MappingGraphRel> existsNodes;
				existsNodes = getExistsAtomsForVar(var);
				
				for(MappingGraphRel exists: existsNodes) {
					if (exists.pos == existsPos) {
						Vector<Integer> varPositions = new Vector<Integer>();
						
						for(int k = 0; k < exists.vars.size(); k++) {
							if (exists.vars.get(k).equals(var))
								varPositions.add(k);
						}
						
						result[i][j] = new int[varPositions.size()];
						for(int k = 0; k < result[i][j].length; k++) {
							result[i][j][k] = varPositions.get(k);
						}
					}
				}
				
				if (result[i][j] == null)
					result[i][j] = new int[0];
			}
		}
		
		return result;
	}
	
	@Override
	public String toString () {
		StringBuffer result;
		
		result = new StringBuffer();
		
		result.append("Foreach Nodes:\n");
		result.append(foreachNodes);
		result.append("\nExists Nodes:\n");
		result.append(existsNodes);
		result.append("\nvarMap:\n");
		result.append(varToNodesMap);
		
		return result.toString();
	}
}

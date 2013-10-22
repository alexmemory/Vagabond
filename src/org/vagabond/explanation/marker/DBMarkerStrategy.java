package org.vagabond.explanation.marker;

import java.util.HashSet;
import java.util.Set;

import org.vagabond.util.Enums.Marker_Type;



public class DBMarkerStrategy implements IDBMarkerStrategy 
{
	CreateStrategy currentStrategy = CreateStrategy.CreateOnTarget;
	
int[][] output_Matrix = {
		{1,5,3,3,1,3,3},
		{5,4,3,2,5,6,5},
		{3,3,3,3,3,3,3},
		{3,2,3,2,3,6,3},
		{1,1,3,3,5,4,5},
		{3,5,3,2,3,6,6},
		{3,5,3,3,3,6,7}};
//We use a 7 by 7 matrix with values 1,2,4 indicating QUERY, TABLE or JAVA obj respectively
//In case of more than one, it can be represented as a sum of these numbers

// Q - Query  J - Java T - Table
// Default Strategy : Use can update the matrix to one of his choice

/*
 *           |  Q   |  J  |Q & T | T  |  Q & J | J & T | Q & J & T |
       |  Q  |  Q   | Q&J | Q&T  |Q&T |    Q   |  Q&T  |    Q&T    |   
       |  J  | Q&J  |  J  | Q&T  | T  |   Q&J  |  J&T  |    Q&J    |
     | Q & T | Q&T  | Q&T | Q&T  |Q&T |   Q&T  |  Q&T  |    Q&T    |
       |  T  | Q&T  |  T  | Q&T  | T  |   Q&T  |  J&T  |    Q&T    |
     | Q & J | Q    |  Q  | Q&T  |Q&T |   Q&J  |   J   |    Q&J    |
     | J & T | Q&T  | J&T | Q&T  | T  |   Q&T  |  J&T  |    J&T    |
  | Q & J & T| Q&T  | Q&J | Q&T  |Q&T |   Q&T  |  J&T  |   Q&J&T   |
*/

	//Function to get the preferred out types for the result of a SET operation on DBMarkerSet like
	//union or intersect
	@Override
	public Set<Marker_Type> getBinaryOperationOutput(Set<Marker_Type> leftTypes, Set<Marker_Type> rightTypes) 
	{
		Set<Marker_Type> retType = new HashSet<Marker_Type>();
		int left_index =-1, right_index=-1, out_value=-1;
		
		//Get the left index of the matrix table
		//Query 1
		//Table 2
		//Java  4 etc
		if(leftTypes.contains(Marker_Type.QUERY_REP))
			left_index++;
		if(leftTypes.contains(Marker_Type.JAVA_REP))
			left_index = left_index + 2;
		if(leftTypes.contains(Marker_Type.TABLE_REP))
			left_index = left_index + 4;

		//Get the right index of the matrix table
		if(rightTypes.contains(Marker_Type.QUERY_REP))
			right_index ++;
		if(rightTypes.contains(Marker_Type.JAVA_REP))
			right_index = right_index + 2;
		if(rightTypes.contains(Marker_Type.TABLE_REP))
			right_index = right_index + 4;
		
		//Get the output type value from the indicator matrix
		out_value = output_Matrix[left_index][right_index];
		
		//Convert the output value to set
		//We get the bit value, each of which indicates the presence of a Marker Type
		if((((byte)out_value) & (0x01 << 0))==1)
			retType.add(Marker_Type.QUERY_REP);
		if((((byte)out_value) & (0x01 << 1))==1)
			retType.add(Marker_Type.TABLE_REP);
		if((((byte)out_value) & (0x01 << 2))==1)
			retType.add(Marker_Type.JAVA_REP);
		  
		return retType;
	}

	//Helps user to set the output type of a SET operation on DBMarkerSet
		@Override
		public void setBinaryOperationOutput(Set<Marker_Type> leftTypes,
				Set<Marker_Type> rightTypes, Set<Marker_Type> outTypes) {
			int left_index =-1, right_index=-1, out_value=0;
			
			//Get the left index of the matrix table
			//Query 1
			//Table 2
			//Java  4 etc
			if(leftTypes.contains(Marker_Type.QUERY_REP))
				left_index++;
			if(leftTypes.contains(Marker_Type.JAVA_REP))
				left_index = left_index + 2;
			if(leftTypes.contains(Marker_Type.TABLE_REP))
				left_index = left_index + 4;

			//Get the right index of the matrix table
			if(rightTypes.contains(Marker_Type.QUERY_REP))
				right_index ++;
			if(rightTypes.contains(Marker_Type.JAVA_REP))
				right_index = right_index + 2;
			if(rightTypes.contains(Marker_Type.TABLE_REP))
				right_index = right_index + 4;
			
			//Get the output type value from the parameter
			//We get the bit value, each of which indicates the presence of a Marker Type
			if(outTypes.contains(Marker_Type.QUERY_REP))
				out_value = ((byte)out_value) | (0x01 << 0);
			if(outTypes.contains(Marker_Type.JAVA_REP))
				out_value = ((byte)out_value) | (0x01 << 1);
			if(outTypes.contains(Marker_Type.TABLE_REP))
				out_value = ((byte)out_value) | (0x01 << 2);
			
			//Set the output type value from the indicator matrix
			output_Matrix[left_index][right_index] = out_value;
		}
		
	//Returns the current strategy for generating the output objects at source or target of the SET operation
	@Override
	public CreateStrategy getCreateStrategy() {
		return currentStrategy;

	}

	//Sets the strategy for generating the output objects at source or target of the SET operation
	@Override
	public void setCreateStrategy(CreateStrategy strat) {
		currentStrategy = strat;
	}

}

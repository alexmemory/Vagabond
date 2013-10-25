package org.vagabond.explanation.marker;

import java.util.HashSet;
import java.util.Set;

import org.vagabond.util.Enums.Marker_Type;



public class DBMarkerStrategy implements IDBMarkerStrategy 
{
	CreateStrategy currentStrategy = CreateStrategy.CreateOnTarget;
	
int[][] output_Matrix = {
		{1,3,3,5,1,3,3},
		{3,2,3,2,3,6,3},
		{3,3,3,3,3,3,3},
		{5,2,3,4,5,6,5},
		{1,3,3,1,5,4,5},
		{3,2,3,6,3,6,6},
		{3,3,3,5,3,6,7}};
//We use a 7 by 7 matrix with values 1,2,4 indicating QUERY, TABLE or JAVA obj respectively
//In case of more than one, it can be represented as a sum of these numbers

// Q - Query  J - Java T - Table
// Default Strategy : Use can update the matrix to one of his choice

/*
 *           |  Q    |  T   | Q & T  | J   |Q & J  | J & T  | Q & J & T |
       |  Q  |  Q 1  | Q&T3 |  Q&T3  |Q&J5 |  Q 1  |  Q&T 3 |    Q&T3    |   
       |  T  | Q&T3  |  T 2 |  Q&T3  | T 2 | Q&T3  |  J&T 6 |    Q&T3    |
     | Q & T | Q&T3  | Q&T3 |  Q&T3  |Q&T3 | Q&T 3 |  Q&T 3 |    Q&T3    |
       |  J  | Q&J5  |  T 2 |  Q&T3  | J 4 | Q&J 5 |  J&T 6 |    Q&J5    |
     | Q & J | Q  1  | Q&T3 |  Q&T3  | Q 1 | Q&J 5 |   J  4 |    Q&J5    |
     | J & T | Q&T3  |  T 2 |  Q&T3  |J&T6 | Q&T 3 |  J&T 6 |    J&T6    |
  | Q & J & T| Q&T3  | Q&T3 |  Q&T3  |Q&J5 | Q&T 3 |  J&T 6 |   Q&J&T7   |
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
			left_index = left_index + 4;
		if(leftTypes.contains(Marker_Type.TABLE_REP))
			left_index = left_index + 2;

		//Get the right index of the matrix table
		if(rightTypes.contains(Marker_Type.QUERY_REP))
			right_index ++;
		if(rightTypes.contains(Marker_Type.JAVA_REP))
			right_index = right_index + 4;
		if(rightTypes.contains(Marker_Type.TABLE_REP))
			right_index = right_index + 2;
		
		//Get the output type value from the indicator matrix
		out_value = output_Matrix[left_index][right_index];
		
		//Convert the output value to set
		//We get the bit value, each of which indicates the presence of a Marker Type
		if((((byte)out_value) & (0x001 << 0))==1)
			retType.add(Marker_Type.QUERY_REP);
		if((((byte)out_value) & (0x001 << 1))==2)
			retType.add(Marker_Type.TABLE_REP);
		if((((byte)out_value) & (0x001 << 2))==4)
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
				left_index = left_index + 4;
			if(leftTypes.contains(Marker_Type.TABLE_REP))
				left_index = left_index + 2;

			//Get the right index of the matrix table
			if(rightTypes.contains(Marker_Type.QUERY_REP))
				right_index ++;
			if(rightTypes.contains(Marker_Type.JAVA_REP))
				right_index = right_index + 4;
			if(rightTypes.contains(Marker_Type.TABLE_REP))
				right_index = right_index + 2;
			
			
			//Get the output type value from the parameter
			//We get the bit value, each of which indicates the presence of a Marker Type
			if(outTypes.contains(Marker_Type.QUERY_REP))
				out_value = ((byte)out_value) | (0x01 << 0);
			if(outTypes.contains(Marker_Type.JAVA_REP))
				out_value = ((byte)out_value) | (0x01 << 2);
			if(outTypes.contains(Marker_Type.TABLE_REP))
				out_value = ((byte)out_value) | (0x01 << 1);
			
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

package kdom.core;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Touple
 * @author Swapnil
 *
 */
public class Tuple {

	
	/**
	 * Tuple id
	 */
	private long id;

	/**
	 * Indexed data 
	 */
	private Map<String, Double> data;

	/**
	 * Use only for jackson json conversion
	 */
	public Tuple(){

	};
	/**
	 * 
	 */
	public Tuple(long id, Map<String, Double> data) {
		this.id = id;
		this.data  = data;
	}

	public long getId() {
		return id;
	}

	public Map<String, Double> getData() {
		return data;
	}

	/**
	 * Check whether this tuple dominates argument tuple
	 * Tuple A dominates other tuple B,	 *  
	 * if for all dimensions d, A[d] <= B[d] 
	 * and for at least one dimension d, A[d] < B[d]
	 *  
	 * So return false for equal tuples.
	 * 
	 * @param tuple
	 * @return
	 */
	public boolean isFullDominate(Tuple tuple,Set<String> queryDimensionSet){
		boolean isDominate = false;	

		//loop will compare all dimension values
		Map<String, Double> otherTupleData = tuple.getData();
		for (String dimension : queryDimensionSet) {

			Double localDimensionValue = this.data.get(dimension);
			Double otherDimensionValue = otherTupleData.get(dimension);
			if(localDimensionValue > otherDimensionValue) {
				return false;
			} else if(localDimensionValue < otherDimensionValue){				
				isDominate = true;
			} 
		}

		return isDominate;
	}


	/**
	 * Check whether this tuple dominated by argument tupleData
	 * Tuple A dominates other tuple B,	 *  
	 * if for all dimensions d, A[d] <= B[d] 
	 * and for at least one dimension d, A[d] < B[d]
	 *  
	 * So return false for equal tuples.
	 * 
	 * @param tuple
	 * @return
	 */
	public boolean isFullDominatedBy(Map<String, Double> otherData, Set<String> queryDimensionSet){
		boolean isDominate = false;	

		if(otherData != null){
			//loop will compare all dimension values
			for (String dimension : queryDimensionSet) {

				Double localDimensionValue = this.data.get(dimension);
				Double otherDimensionValue = otherData.get(dimension);
				if(localDimensionValue < otherDimensionValue) {
					return false;
				} else if(localDimensionValue > otherDimensionValue){				
					isDominate = true;
				} 
			}
		}
		return isDominate;
	}
	
	/**
	 * Computes volume of  dominating region
	 * @param queryDimensionSet
	 * @return
	 */
	public double getVDR(Set<String> queryDimensionSet){
		double vdr = 1;
		for (String dimension : queryDimensionSet) {
			vdr *= 10000 - this.data.get(dimension);
		}
		return vdr;
	}
	
	public static Tuple getMaxVDRTuple(Set<String> queryDimensionSet, Collection<Tuple> tuples){
		double maxVDR = Double.NEGATIVE_INFINITY;
		Tuple maxVDRTuple = null;
		for (Tuple tuple : tuples) {
			if(maxVDR < tuple.getVDR(queryDimensionSet))
				maxVDRTuple = tuple;
		}
		return maxVDRTuple;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tuple other = (Tuple) obj;
		if (id != other.id)
			return false;
		return true;
	}
	/**
	 * Check whether this tuple k dominates argument tuple
	 * Tuple A dominates other tuple B,	 *  
	 * if for some K dimensions d, A[d] <= B[d] 
	 * and for at least one dimension d, A[d] < B[d]
	 *  
	 * So return false for equal tuples.
	 * 
	 * @param tuple
	 * @return
	 */
	public boolean isKDominate(Tuple tuple, int k, Set<String> queryDimensionSet){
		boolean isStrictDominateInAtLeastOneDimension = false;	
		boolean isKDominate = false;
		int dominatingDimensionCount = 0;
		//loop will compare all dimension values
		Map<String, Double> otherTupleData = tuple.getData();
		for (String dimension : queryDimensionSet) {

			Double localDimensionValue = this.data.get(dimension);
			Double otherDimensionValue = otherTupleData.get(dimension);
			if(localDimensionValue <= otherDimensionValue) {
				if(localDimensionValue < otherDimensionValue){
					isStrictDominateInAtLeastOneDimension = true;
				} 
				dominatingDimensionCount++;				
				//exactly k dimension dominance
				if(isStrictDominateInAtLeastOneDimension && dominatingDimensionCount == k){
					isKDominate =  true;
					break;
				}
			}

		}
		return isKDominate;
	}

	@Override
	public String toString(){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(this.id);
		for(Map.Entry<String, Double> dimensionValueEntry : this.data.entrySet()){
			stringBuilder.append(" ");
			stringBuilder.append(dimensionValueEntry.getValue());
		}
		return stringBuilder.toString();
	}

	/**
	 * Factory method for tuple creation from string
	 * containing tuple id followed by space separated attribute values
	 * Assume string is well defined to have constant number of dimension values
	 * @param tupleString
	 * @return
	 */
	public static Tuple createTuple(String tupleString){
		Scanner lineScanner = new Scanner(tupleString);

		//scan id
		long id = 0;
		if(lineScanner.hasNext())
			id = lineScanner.nextLong();

		//scan data
		Map<String, Double> tupleData = new LinkedHashMap<String, Double>();
		int counter = 1;
		while(lineScanner.hasNext()){
			Double dimensionValue = lineScanner.nextDouble();
			tupleData.put(Integer.toString(counter), dimensionValue);
			counter++;
		}
		lineScanner.close();

		//generate tuple
		Tuple tuple = new Tuple(id , tupleData);
		return tuple;

	}

}

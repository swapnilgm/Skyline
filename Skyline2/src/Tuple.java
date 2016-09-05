import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

/**
 * Touple
 * @author Swapnil
 *
 */
public class Tuple extends MinDistSequenceElement {

	private static Set<Integer> dimensionSet;
		
	private static long dimensionSize;
	/**
	 * Tuple id
	 */
	private long id;
	
	/**
	 * Indexed data 
	 */
	private ArrayList<Double> data;
	
	/**
	 * 
	 */
	public Tuple(long id, ArrayList<Double> data) {
		this.id = id;
		this.data  = data;
		computeMinDist();
	}

	private void computeMinDist(){
		double minDist = 0;
		for (Double value : this.data) {
			minDist = minDist + value;
		}
		this.setMinDist(minDist);
	}
	
	public long getId() {
		return id;
	}

	public ArrayList<Double> getData() {
		return data;
	}
	

	public static long getDimensionSize() {
		return dimensionSize;
	}

	public static void setDimensionSize(long dimensionSize) {
		Tuple.dimensionSize = dimensionSize;
	}

	public static Set<Integer> getDimensionSet() {
		return dimensionSet;
	}

	public static void setDimensionSet(Set<Integer> dimensionSet) {
		Tuple.dimensionSet = dimensionSet;
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
	public boolean isDominate(Tuple tuple){
		boolean isDominate = false;
		ArrayList<Double> otherTupleData = tuple.getData();
		isDominate = isDominate(otherTupleData);
		return isDominate;
	}
	
	public boolean isDominate(ArrayList<Double> otherTupleData){
		boolean isDominate = false;	
		
		//since both tuple data are arrayList with same size
		//loop will compare all dimension values
		for (Integer dimenison : dimensionSet) {
			Double localDimensionValue = this.data.get(dimenison);
			Double otherDimensionValue = otherTupleData.get(dimenison);
			if(localDimensionValue > otherDimensionValue) {
				return false;
			} else if(localDimensionValue < otherDimensionValue){				
				isDominate = true;
			} 
		}
		
		return isDominate;
	}
	
	@Override
	public String toString(){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(this.id);
		for(Double dimensionValue : this.data){
			stringBuilder.append(" ");
			stringBuilder.append(dimensionValue);
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
	protected static Tuple createTuple(String tupleString){
		Scanner lineScanner = new Scanner(tupleString);

		//scan id
		long id = 0;
		if(lineScanner.hasNext())
			id = lineScanner.nextLong();

		//scan data
		ArrayList<Double> tupleData = new ArrayList<Double>();
		while(lineScanner.hasNext()){
			Double dimensionValue = lineScanner.nextDouble();
			tupleData.add(dimensionValue);			
		}
		
		lineScanner.close();
		//generate tuple
		Tuple tuple = new Tuple(id , tupleData);
		return tuple;

	}
	
	/**
	 * Computes entropy value in L norm form i.e. sum of dimensions
	 * @return
	 */
	protected double getEntropyValue(){
		double entrophyValue = 0;
		for (Integer dimension : dimensionSet) {
			Double dimensionValue = this.data.get(dimension-1);
			entrophyValue = entrophyValue + dimensionValue;			
		}
		return entrophyValue;
	}

}

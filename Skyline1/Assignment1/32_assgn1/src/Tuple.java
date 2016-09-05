import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Swapnil
 *
 */
public class Tuple {

	private static Set<Integer> dimensionSet;
	/**
	 * Tuple id
	 */
	private long id;
	
	/**
	 *Tuple logical timestamp
	 */
	private long timeStamp;
	
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
	}

	public long getId() {
		return id;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public ArrayList<Double> getData() {
		return data;
	}
	
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
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
		
		//since both tuple data are arrayList with same size
		//loop will compare all dimension values
		for (Integer dimenison : dimensionSet) {
			Double localDimensionValue = this.data.get(dimenison - 1);
			Double otherDimensionValue = otherTupleData.get(dimenison - 1);
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
		
		//generate tuple
		Tuple tuple = new Tuple(id , tupleData);
		return tuple;

	}
	
	protected double getEntropyValue(){
		double entrophyValue = 0;
		for (Integer dimension : dimensionSet) {
			Double dimensionValue = this.data.get(dimension-1);
			entrophyValue = entrophyValue + Math.log(dimensionValue + 1);			
		}
		return entrophyValue;
	}
}

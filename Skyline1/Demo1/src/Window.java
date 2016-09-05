import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Swapnil
 *
 */
public class Window {

	/**
	 * Window buffer size
	 */
	private long size;

	/**
	 * Set of tuple added to window
	 */
	private List<Tuple> tupleLists;

	/**
	 * 
	 */
	private Set<Integer> dimensionSet;

	private long tupleComparisons = 0;
	
	private long timeStampComparison = 0 ;
	/**
	 * 
	 */
	public Window(long size, Set<Integer> dimensionSet) {
		this.size = size;
		this.tupleLists = new ArrayList<Tuple>();
		this.dimensionSet = dimensionSet;
	}

	/**
	 * Adds the tuple to window
	 * @param tuple
	 */
	protected void addTuple(Tuple tuple){
		this.tupleLists.add(tuple);
	}


	public List<Tuple> getTupleLists() {
		return tupleLists;
	}

	/**
	 * It iterates over the all tuples in windows and removes the tuples which are 
	 * dominated by argument tuple
	 * @param newTuple
	 */
	protected boolean pruneDominatedTuples(Tuple newTuple){
		boolean isPruned = false;
		Iterator<Tuple> tupleIterator = this.tupleLists.iterator();		
		while (tupleIterator.hasNext()) {
			Tuple tuple = (Tuple) tupleIterator.next();
			this.tupleComparisons++;
			//Compare tuples
			if(newTuple.isDominate(tuple))
				tupleIterator.remove();
			else if(tuple.isDominate(newTuple)){
				isPruned = true;
				break;
			}
		}
		return isPruned;
	}

	/**
	 * It iterates over the all tuples in windows and removes the tuples which are 
	 * dominated by argument tuple
	 * @param newTuple
	 */
	protected List<Tuple> removeAndGetOldertuples(long timeStamp){
		List<Tuple> olderTupleList = new ArrayList<Tuple>();
		Iterator<Tuple> tupleIterator = this.tupleLists.iterator();		
		while (tupleIterator.hasNext()) {
			Tuple tuple = (Tuple) tupleIterator.next();
			long tupletimeStamp = tuple.getTimeStamp();
			if(tupletimeStamp < timeStamp) {
				olderTupleList.add(tuple);
				tupleIterator.remove();
			}
		}
					
		return olderTupleList;
	}
	
	/**
	 * Check whether window is filled completly
	 * @return
	 */
	protected boolean isFull(){		
		return (this.tupleLists.size() == this.size);
	}

	public long getTupleComparisons() {
		return tupleComparisons;
	}

	public void setTupleComparisons(long tupleComparisons) {
		this.tupleComparisons = tupleComparisons;
	}

	public long getTimeStampComparison() {
		return timeStampComparison;
	}

	public void setTimeStampComparison(long timeStampComparison) {
		this.timeStampComparison = timeStampComparison;
	}

	public void clear(){
		this.timeStampComparison = 0;
		this.tupleComparisons = 0;
		this.tupleLists.clear();
	}
}

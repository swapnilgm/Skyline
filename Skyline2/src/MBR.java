import java.util.ArrayList;

/**
 * 
 */

/**
 * @author Swapnil
 *
 */
public class MBR extends MinDistSequenceElement{

	private ArrayList<Double> lowerBound;
	
	private ArrayList<Double> upperBound;
	
	/**
	 * 
	 */
	public MBR(ArrayList<Double> lowerBound, ArrayList<Double> upperBound) {
		setLowerBound(lowerBound);;
		this.upperBound = upperBound;
	}

	public MBR(Node node) {
		MBR nodeMbr = node.getMbr();
		setLowerBound(new ArrayList<Double>(nodeMbr.getLowerBound()));
		this.upperBound = new ArrayList<Double>(nodeMbr.getUpperBound());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (Double bound : lowerBound) {
			stringBuilder.append(bound);
			stringBuilder.append(" ");			
		}
		stringBuilder.append("\n");
		for (Double bound : upperBound) {
			stringBuilder.append(bound);
			stringBuilder.append(" ");			
		}
		stringBuilder.append("\n");
		return stringBuilder.toString();
	}

	public MBR(Tuple tuple) {
		this(tuple.getData());		
	}
	
	public MBR(ArrayList<Double> tupleData) {
		
		setLowerBound(new ArrayList<Double>(tupleData));
		this.upperBound = new ArrayList<Double>(tupleData);
	}

	private void computeMinDist(){
		
		double minDist = 0;
		for (Double value : this.lowerBound) {
			minDist = minDist + value;
		}
		this.setMinDist(minDist);
	}
	
		
	/**
	 * @return the lowerBound
	 */
	protected ArrayList<Double> getLowerBound() {
		return lowerBound;
	}

	/**
	 * @param lowerBound the lowerBound to set
	 */
	protected void setLowerBound(ArrayList<Double> lowerBound) {
		this.lowerBound = lowerBound;
		computeMinDist();
	}

	/**
	 * @return the upperBound
	 */
	protected ArrayList<Double> getUpperBound() {
		return upperBound;
	}

	/**
	 * @param upperBound the upperBound to set
	 */
	protected void setUpperBound(ArrayList<Double> upperBound) {
		this.upperBound = upperBound;
	}

	/**
	 * Check whether this tuple dominates argument MBR
	 * 
	 * @param tuple
	 * @return
	 */
	public boolean isDominatedBy(Tuple tuple){
		boolean isDominate = false;
		isDominate = tuple.isDominate(this.lowerBound);		
		return isDominate;
	}


	/**
	 * Adjust  MBR
	 * 
	 * @param tuple
	 * @return
	 */
	public void adjustMBR(Tuple tuple){
		ArrayList<Double> tupleData = tuple.getData();
		adjustMBR(tupleData);
	}

	public void adjustMBR(Node node){
		MBR mbr = node.getMbr();
		adjustMBR(mbr);
	}
	
	public void adjustMBR(MBR mbr){
		int key =0;
		for (Double value : mbr.getLowerBound()) {
			Double lowerBoundValue = this.lowerBound.get(key);
			this.lowerBound.set(key, Math.min(lowerBoundValue,value));
			key++;
		}
		
		key=0;
		for (Double value : mbr.getUpperBound()) {
			Double upperBoundValue = this.upperBound.get(key);
			this.upperBound.set(key, Math.max(upperBoundValue, value));
			key++;
		}
		computeMinDist();
	}

	public void adjustMBR(ArrayList<Double> tupleData){
		
		int key =0;
		for (Double value : tupleData) {
			Double lowerBoundValue = this.lowerBound.get(key);
			this.lowerBound.set(key, Math.min(lowerBoundValue,value));
			Double upperBoundValue = this.upperBound.get(key);
			this.upperBound.set(key, Math.max(upperBoundValue, value));
			key++;
		}
		computeMinDist();
	}

	
}

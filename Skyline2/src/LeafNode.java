import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * 
 */

/**
 * @author Swapnil
 *
 */
public class LeafNode extends Node {

	private Set<Tuple> tuples;

	/**
	 * @param mbr
	 */
	public LeafNode(MBR mbr) {
		super(mbr);
		this.tuples = new HashSet<Tuple>();
	}

	/**
	 * @param mbr
	 */
	public LeafNode(MBR mbr, int nodeId) {
		super(mbr, nodeId);
		this.tuples = new HashSet<Tuple>();
	}

	/**
	 * @return the tuples
	 */
	protected Set<Tuple> getTuples() {
		return tuples;
	}


	/**
	 * @param tuples the tuples to set
	 */
	protected void setTuples(Set<Tuple> tuples) {
		this.tuples = tuples;
	}


	@Override
	public Node insertTuple(Tuple tuple) {
		Node newNode = null;
		this.tuples.add(tuple);
		this.getMbr().adjustMBR(tuple);
		if(tuples.size() > Node.getMaximumChildren()){
			newNode = splitNode();			
		}
		return newNode;		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LeafNode [tuplesCount=" + tuples.size() +  ", nodeID = "+getNodeId()+"]";
	}


	@Override
	public void searchTuple(Tuple tuple) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public Node splitNode() {
		
		Pair<Tuple> tuplePair = pickSeeds();
		Set<Tuple> tuplesL = new HashSet<Tuple>();
		Set<Tuple> tuplesR = new HashSet<Tuple>();
		
		Tuple tupleLow = tuplePair.getLow();
		Tuple tupleHigh = tuplePair.getHigh();
		MBR mbrl = new MBR(tupleLow);
		MBR mbrh = new MBR(tupleHigh);
		tuplesL.add(tupleLow);
		tuplesR.add(tupleHigh);
		Set<Tuple> tupleSet = new HashSet<Tuple>(tuples);
		tupleSet.remove(tupleLow);
		tupleSet.remove(tupleHigh);
		Iterator<Tuple> tupleIterator = tupleSet.iterator();
		int remainingElementCount = tupleSet.size();
		while (tupleIterator.hasNext()) {
			//fill up minimum capacity of smaller node based on remaining tuple count
			//redundunt code modularize latter
			if(remainingElementCount + tuplesL.size() == Node.getMinimumChildren()){
				while (tupleIterator.hasNext()) {
					Tuple tuple = (Tuple) tupleIterator.next();
					tuplesL.add(tuple);
					mbrl.adjustMBR(tuple);
				}
			} else if(remainingElementCount + tuplesR.size() == Node.getMinimumChildren()){
				while (tupleIterator.hasNext()) {
					Tuple tuple = (Tuple) tupleIterator.next();
					tuplesR.add(tuple);
					mbrh.adjustMBR(tuple);
				}
			} else {
				Tuple tuple = (Tuple) tupleIterator.next();
				boolean next = pickNext(tuple, mbrl, mbrh, tuplesL.size(), tuplesR.size());
				if(next == true ){
					tuplesL.add(tuple);
					mbrl.adjustMBR(tuple);
				} else {
					tuplesR.add(tuple);
					mbrh.adjustMBR(tuple);
				}				
			}
			remainingElementCount--;
		}
		
		this.setMbr(mbrl);
		this.setTuples(tuplesL);

		LeafNode newNode = new LeafNode(mbrh);
		newNode.setTuples(tuplesR);
		
		return newNode;
	}



	public Pair<Tuple> pickSeeds() {
		//maximum distance along all dimension 
		Tuple seedHigh = null;
		Tuple seedLow = null;
		

		//TODO could have created some data structure
		double maxDist = Double.NEGATIVE_INFINITY;
		
		for(int dimensionIndex : Tuple.getDimensionSet()){
			double higestLowerBound = Double.NEGATIVE_INFINITY;
			double lowestUpperBound = Double.POSITIVE_INFINITY;
			Tuple tupleLow =null;
			Tuple tupleHigh = null;
			for(Tuple tuple : tuples){
				ArrayList<Double> tupleData = tuple.getData();
				Double dimensionValue = tupleData.get(dimensionIndex);
				if(dimensionValue > higestLowerBound) {
					higestLowerBound = dimensionValue;
					tupleLow = tuple;
				}
				if(dimensionValue < lowestUpperBound) {
					lowestUpperBound = dimensionValue;
					tupleHigh = tuple;
				}
			}
			
			if(maxDist < Math.abs(lowestUpperBound - higestLowerBound)){
				maxDist = Math.abs(lowestUpperBound - higestLowerBound);
				seedHigh = tupleHigh;
				seedLow = tupleLow;
			}
		}
		return new Pair<Tuple>(seedLow, seedHigh);
	}

	/**
	 * Find out  appropriate group/node for nect tuple
	 */
	public boolean pickNext(Tuple tuple, MBR mbrl, MBR mbrh, int sizeL, int sizeR) {
		boolean next = true;
		double enlargmentH = computeEnlargement(tuple, mbrh);
		double enlargmentl = computeEnlargement(tuple, mbrl);
		if(enlargmentH < enlargmentl){
			next = false;
		}
		else if(enlargmentH > enlargmentl){
			next = true;
		}
		else if(sizeL > sizeR){
			next = false;
		}
		else {
			next = true;
		}
		return next;
	}


	@Override
	public String getSeriealizedContent() {
		StringBuilder serieslisedContent = new StringBuilder();
		for (Tuple tuple : this.tuples) {
			serieslisedContent.append(tuple.toString() + "\n");
		}
		return serieslisedContent.toString();
	}

	public static Node createNodeFromFile(File nodeDataDile){
		Node node = null;
		return node;
	};
	
	public static Node createNodeFromStream(Scanner scanner, MBR mbr){
		LeafNode node = null;
		
		//read nodeID
		String dataLine = (String) scanner.nextLine();
		Scanner lineScanner = new Scanner(dataLine);
		int nodeID = lineScanner.nextInt();
		lineScanner.close();

		node = new LeafNode(mbr, nodeID);
		
		//node
		Set<Tuple> tuples = new HashSet<Tuple>();
		while (scanner.hasNext()) {
			dataLine = (String) scanner.nextLine();
			tuples.add(Tuple.createTuple(dataLine));
		}
		node.setTuples(tuples);
		lineScanner.close();
		
		return node;
	};

}

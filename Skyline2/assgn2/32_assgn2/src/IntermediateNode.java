import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * 
 */

/**
 * @author Swapnil
 *
 */
public class IntermediateNode extends Node {

	/**
	 * Used only in case of file based implementation
	 */
	private Map<Integer, MBR> childNodeIDSet;

	private Set<Node> childNodes;

	/**
	 * @param mbr
	 */
	public IntermediateNode(MBR mbr) {
		super(mbr);
	}

	/**
	 * @param mbr
	 */
	public IntermediateNode(MBR mbr, int nodeId) {
		super(mbr,nodeId);
	}

	/**
	 * @return the childNodeIDSet
	 */
	protected Map<Integer, MBR> getChildNodeIDSet() {
		return childNodeIDSet;
	}

	/**
	 * @param childNodeIDSet the childNodeIDSet to set
	 */
	protected void setChildNodeIDSet(Map<Integer, MBR> childNodeIDSet) {
		this.childNodeIDSet = childNodeIDSet;
	}

	/**
	 * @return the childNodes
	 */
	protected Set<Node> getChildNodes() {
		return childNodes;
	}


	/**
	 * @param childNodes the childNodes to set
	 */
	protected void setChildNodes(Set<Node> childNodes) {
		this.childNodes = childNodes;
	}


	private Node chooseChildForInsertion(Tuple tuple){
		double minEnlargement = Double.MAX_VALUE;
		Node choosenNode = null;
		for(Node node : this.childNodes){
			double childEnlargement = computeEnlargement(tuple, node.getMbr());
			if(childEnlargement < minEnlargement){
				minEnlargement = childEnlargement;
				choosenNode = node;
			}
		}
		return choosenNode;
	}


	@Override
	public Node insertTuple(Tuple tuple) {

		Node insertNode = this.chooseChildForInsertion(tuple);
		Node newNode = insertNode.insertTuple(tuple);
		if(newNode!=null) {
			this.childNodes.add(newNode);
			if(this.childNodes.size() > Node.getMaximumChildren()){
				newNode = splitNode();			
			}
			else newNode = null;
		} else {
			this.getMbr().adjustMBR(tuple);			
		}
		return newNode;		
	}

	@Override
	public void searchTuple(Tuple tuple) {
		// TODO Auto-generated method stub

	}

	@Override
	public Node splitNode() {

		Pair<Node> nodePair = pickSeeds();
		Set<Node> nodeL = new HashSet<Node>();
		Set<Node> nodeR = new HashSet<Node>();

		Node nodeLow = nodePair.getLow();
		Node nodeHigh = nodePair.getHigh();
		nodeL.add(nodeLow);
		nodeR.add(nodeHigh);
		MBR mbrl = new MBR(nodeLow);
		MBR mbrh = new MBR(nodePair.getHigh());
		Set<Node> childNodeSet = new HashSet<Node>(this.childNodes);
		childNodeSet.remove(nodeHigh);
		childNodeSet.remove(nodeLow);
		Iterator<Node> childNodeIterator = childNodeSet.iterator();
		int remainingElementCount = childNodeSet.size();

		//TODO change thoughtless logic but worth
		while (childNodeIterator.hasNext()) {

			//fill up minimum capacity of smaller node based on remaining tuple count
			//redundunt code modularize latter
			if(remainingElementCount + nodeL.size() == Node.getMinimumChildren()){
				while (childNodeIterator.hasNext()) {
					Node node = (Node) childNodeIterator.next();
					nodeL.add(node);
					mbrl.adjustMBR(node);
				}
			} else if(remainingElementCount + nodeR.size() == Node.getMinimumChildren()){
				while (childNodeIterator.hasNext()) {
					Node node = (Node) childNodeIterator.next();
					nodeR.add(node);
					mbrh.adjustMBR(node);
				}
			} else {
				Node node = (Node) childNodeIterator.next();
				boolean next = pickNext(node, mbrl, mbrh, nodeL.size(), nodeR.size());
				if(next == true){
					nodeL.add(node);
					mbrl.adjustMBR(node);
				} else {
					nodeR.add(node);
					mbrh.adjustMBR(node);
				}				
			}
			remainingElementCount--;
		}

		this.setMbr(mbrl);
		this.setChildNodes(nodeL);

		IntermediateNode newNode = new IntermediateNode(mbrh);
		newNode.setChildNodes(nodeR);

		return newNode;
	}


	public Pair<Node> pickSeeds() {
		//maximum distance along all dimension 
		Node seedHigh = null;
		Node seedLow = null;


		//TODO could have created some data structure

		double maxDist = Double.NEGATIVE_INFINITY;
		for(int dimensionIndex : Tuple.getDimensionSet()){
			double highestLowerBound = Double.NEGATIVE_INFINITY;
			double lowestUpperBound = Double.POSITIVE_INFINITY;
			double highestUpperBound = Double.NEGATIVE_INFINITY;
			double lowestLowerBound = Double.POSITIVE_INFINITY;
			Node nodeLow =null;
			Node nodeHigh = null;
			for(Node node : this.childNodes){
				MBR mbr = node.getMbr();
				ArrayList<Double> lowerBound = mbr.getLowerBound();
				Double dimensionLowerValue = lowerBound.get(dimensionIndex);
				//find highest lower bound for separation
				if(dimensionLowerValue > highestLowerBound) {
					highestLowerBound = dimensionLowerValue;
					nodeLow = node;
				}

				//find lowest lower bound for width
				if(dimensionLowerValue < lowestLowerBound) {
					lowestLowerBound = dimensionLowerValue;					
				}

				ArrayList<Double> upperBound = mbr.getUpperBound();
				Double dimensionUpperValue = upperBound.get(dimensionIndex);
				//find highest upper bound for width
				if(dimensionUpperValue > highestUpperBound) {
					highestUpperBound = dimensionUpperValue;
				}
				//find lowest upper bound for separation 
				if(dimensionUpperValue < lowestUpperBound) {
					lowestUpperBound = dimensionUpperValue;					
					nodeHigh = node;
				}
			}

			//compute normalization or current dimension
			if((lowestLowerBound == highestUpperBound) && maxDist == Double.MIN_VALUE) {
				seedHigh = nodeHigh;
				seedLow = nodeLow;
			} else {
				double normalization = (highestLowerBound - lowestUpperBound )/(highestUpperBound - lowestLowerBound);
				if(maxDist < normalization){
					maxDist = normalization;
					seedHigh = nodeHigh;
					seedLow = nodeLow;
				}
			}
		}
		return new Pair<Node>(seedLow, seedHigh);
	}


	public boolean pickNext(Node node, MBR mbrl, MBR mbrh, int sizeL, int sizeR) {
		boolean next = true;
		double enlargmentH = computeEnlargement(node, mbrh);
		double enlargmentl = computeEnlargement(node, mbrl);
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
	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IntermediateNode [ChildCount=" + childNodes.size() + ", nodeID = "+getNodeId()+"]";
	}


	@Override
	public String getSeriealizedContent() {
		StringBuilder serieslisedContent = new StringBuilder();
		for (Node node : this.childNodes) {
			serieslisedContent.append(node.getNodeId() + "\n" );
			serieslisedContent.append(node.getMbr().toString());
			
		}
		return serieslisedContent.toString();
	}

	@Override
	public void writeToFile() throws IOException{
		super.writeToFile();
		for (Node node : this.childNodes) {
			node.writeToFile();
		}
	}



	public static Node createNodeFromStream(Scanner scanner, MBR noswMbr){
		IntermediateNode node = null;

		//read nodeID
		String dataLine = (String) scanner.nextLine();
		Scanner lineScanner = new Scanner(dataLine);
		int nodeID = lineScanner.nextInt();
		lineScanner.close();

		node = new IntermediateNode(noswMbr, nodeID);

		//node
		Map<Integer, MBR> childIDSet = new HashMap<Integer, MBR>();
		while(scanner.hasNext()){
			dataLine = (String) scanner.nextLine();		
			lineScanner = new Scanner(dataLine);
			int nodeId = lineScanner.nextInt();
			lineScanner.close();

			//read lowerbound
			dataLine = (String) scanner.nextLine();
			lineScanner = new Scanner(dataLine);
			ArrayList<Double> mbrlowerBound = new ArrayList<Double>();
			while (lineScanner.hasNextDouble()) {
				mbrlowerBound.add(lineScanner.nextDouble());
			}
			lineScanner.close();

			//read upperbound
			dataLine = (String) scanner.nextLine();
			lineScanner = new Scanner(dataLine);
			ArrayList<Double> mbrupperBound = new ArrayList<Double>();
			while (lineScanner.hasNextDouble()) {
				mbrupperBound.add(lineScanner.nextDouble());
			}
			lineScanner.close();
			MBR mbr = new MBR(mbrlowerBound, mbrupperBound);
			childIDSet.put(nodeId, mbr);

		}
		node.setChildNodeIDSet(childIDSet);
		return node;
	};
}

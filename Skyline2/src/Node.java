import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;


/**
 * 
 */

/**
 * Abstract R-tree node
 * @author Swapnil
 *
 */
public abstract class Node extends MinDistSequenceElement {

	private static int nodeIdGenrator = 0;
	
	private int nodeId;
	
	private File file;
	
	private MBR mbr;
	
	private static long minimumChildren;
	
	private static long maximumChildren;
	
	/**
	 * 
	 */
	public Node(MBR mbr) {
		this.mbr = mbr;
		this.nodeId = Node.getNextNodeID();
	}
	
	/**
	 * 
	 */
	public Node(MBR mbr, int nodeID) {
		this.mbr = mbr;
		this.nodeId = nodeID;
	}

	/**
	 * @return the nodeId
	 */
	protected int getNodeId() {
		return nodeId;
	}

	/**
	 * @param nodeId the nodeId to set
	 */
	protected void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	private static int getNextNodeID(){
		return nodeIdGenrator++;
	}
	
	public static long getMinimumChildren() {
		return minimumChildren;
	}

	public static void setMinimumChildren(long minimumChildren) {
		Node.minimumChildren = minimumChildren;
	}

	public static long getMaximumChildren() {
		return maximumChildren;
	}

	public static void setMaximumChildren(long maximumChildren) {
		Node.maximumChildren = maximumChildren;
	}

	public MBR getMbr() {
		return mbr;
	}

	public void setMbr(MBR mbr) {
		this.mbr = mbr;
	}

	/**
	 * @return the file
	 */
	protected File getFile() {
		return file;
	}
	
	/**
	 * @param file the file to set
	 */
	protected void setFile(File file) {
		this.file = file;
	}

	/**
	 * Check whether tuple dominates node
	 * 
	 * @param tuple
	 * @return
	 */
	public boolean isDominatedBy(Tuple tuple){
		boolean isDominate = false;
		isDominate = this.mbr.isDominatedBy(tuple);		
		return isDominate;
	}
	
	public abstract void searchTuple(Tuple tuple);
	
	public abstract Node insertTuple(Tuple tuple);

	public abstract Node splitNode();

	/**
	 * Calculates the enlargement due tuple provided mbr
	 * @param tuple
	 * @param mbr
	 * @return
	 */
	public double computeEnlargement(Tuple tuple, MBR mbr){
		double enlargement = 0;
		double newVolume = 0;
		double originalVolume = 0;

		ArrayList<Double> upperBoundData = new ArrayList<Double>();
		ArrayList<Double> lowerBoundData = new ArrayList<Double>();
		ArrayList<Double> tupleData = tuple.getData();
		ArrayList<Double> mbrUpperBoundData = mbr.getUpperBound();
		ArrayList<Double> mbrLowerBoundData = mbr.getLowerBound();
		originalVolume = computeVolume(mbrLowerBoundData, mbrUpperBoundData);
		for(int dimIndex : Tuple.getDimensionSet()){
			Double dimValue = tupleData.get(dimIndex);
			upperBoundData.add(Math.max(mbrUpperBoundData.get(dimIndex), dimValue));
			lowerBoundData.add(Math.min(mbrLowerBoundData.get(dimIndex), dimValue));			
		}
		
		newVolume = computeVolume(lowerBoundData, upperBoundData);
		enlargement = newVolume - originalVolume;
		return enlargement;
	}
	
	protected double computeEnlargement(Node node, MBR mbr){
		MBR nodeMbr = node.getMbr();		
		double enlargement = computeEnlargement(nodeMbr.getLowerBound(), nodeMbr.getUpperBound(), mbr);
		return enlargement;
	}

	protected double computeEnlargement(ArrayList<Double> lowerBoundData, ArrayList<Double> upperBoundData, MBR mbr){
		double enlargement = 0;
		double newVolume = 0;
		double originalVolume = 0;

		ArrayList<Double> newUpperBoundData = new ArrayList<Double>();
		ArrayList<Double> newLowerBoundData = new ArrayList<Double>();
		
		ArrayList<Double> mbrUpperBoundData = mbr.getUpperBound();
		ArrayList<Double> mbrLowerBoundData = mbr.getLowerBound();
		
		originalVolume = computeVolume(mbrLowerBoundData, mbrUpperBoundData);
		
		for(int dimIndex : Tuple.getDimensionSet()){
			
			newUpperBoundData.add(Math.max(mbrUpperBoundData.get(dimIndex), lowerBoundData.get(dimIndex)));
			newLowerBoundData.add(Math.min(mbrLowerBoundData.get(dimIndex), upperBoundData.get(dimIndex)));			
		}
		
		newVolume = computeVolume(newLowerBoundData, newUpperBoundData);
		enlargement = newVolume - originalVolume;
		return enlargement;
	}

	private double computeVolume(ArrayList<Double> lowerBoundData, ArrayList<Double> upperBoundData){
		double volume = 1;
		//ArrayList<Double> upperBoundData = upperBound.getData();
		//ArrayList<Double> lowerBoundData = lowerBound.getData();
		for (int dimIndex =0 ; dimIndex< lowerBoundData.size() ; dimIndex++) {
			double value = upperBoundData.get(dimIndex) - lowerBoundData.get(dimIndex);
			volume = volume * value;
		}
		return volume;
	}

	/**
	 * Finds whether node is leaf or not
	 * @return
	 */
	public abstract boolean isLeaf();
	
	@Override
	protected double getMinDist(){
		return this.getMbr().getMinDist();
	}

	public void  writeToFile() throws IOException{
		createFile();
	};
	
	
	/**
	 * Creates the file for node
	 * @param nodeId
	 * @throws IOException
	 */
	public void createFile() throws IOException{
		
		File file = new File("node"+ this.nodeId +".txt");
		FileOutputStream fos = new FileOutputStream(file);
		
		PrintWriter pw = new PrintWriter(fos);
		pw.println(""+isLeaf() +" "+ this.nodeId);
		pw.println(this.getSeriealizedContent());
		pw.close();
		fos.close();
	}
	
	public abstract String getSeriealizedContent();
}

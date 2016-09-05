import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


/**
 * @author Swapnil
 *
 */
public class RTree {

	private Node root;
	
	private int rootNodeId;

	private final long minimumChildren;

	private final long maximumChildren;

	/**
	 * 
	 */
	public RTree(long diskPageSize, long pointerSize, long keySize, long noOfDimension) {
		this.root = null;
		this.maximumChildren = computeMaximumChildrenBound(diskPageSize, pointerSize, keySize, noOfDimension);
		this.minimumChildren = this.maximumChildren/2-1;
		Node.setMaximumChildren(maximumChildren);
		Node.setMinimumChildren(minimumChildren);
	}

	/**
	 * @return the root
	 */
	protected Node getRoot() {
		return root;
	}
	
	/**
	 * @param root the root to set
	 */
	protected void setRoot(Node root) {
		this.root = root;
	}
	
	/**
	 * @return the rootNodeId
	 */
	protected int getRootNodeId() {
		return rootNodeId;
	}

	/**
	 * @param rootNodeId the rootNodeId to set
	 */
	protected void setRootNodeId(int rootNodeId) {
		this.rootNodeId = rootNodeId;
	}

	/**
	 * @return the root
	 */
	protected Node getRootNodeFromFile(Scanner scanner) {
		return root;
	}

	private long computeMaximumChildrenBound(long diskPageSize, long pointerSize, long keySize, long noOfDimension){
		long maxmimumChildren = diskPageSize/(2 * noOfDimension * keySize + pointerSize);
		return maxmimumChildren;		
	}

	/**
	 * Insters tuple in rtree
	 * @param tuple
	 */
	public void insertTuple(Tuple tuple){
		if(root == null){
			root = new LeafNode(new MBR(tuple));
			this.rootNodeId = root.getNodeId();
		} 
		Node newNode = root.insertTuple(tuple);
		if(newNode != null) {
			MBR mbr = new MBR(newNode);
			mbr.adjustMBR(root);
			IntermediateNode newRoot = new IntermediateNode(mbr);
			Set<Node> childNodes = new HashSet<Node>();
			childNodes.add(newNode);
			childNodes.add(root);
			newRoot.setChildNodes(childNodes);
			this.root = newRoot;
			this.rootNodeId = newRoot.getNodeId();
		}

	};

}

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Swapnil
 *
 */
public class SkylineFinder {


	private RTree rTree;

	public SkylineFinder(boolean printSkylineTuple){		
		this.rTree = null;
	}


	/**
	 * Reads the query file. capture the set of dimension in skyline query
	 * Get the window size 
	 * @param queryFilePath
	 * @return
	 */
	public void readQuery(String queryFilePath, PrintWriter outputWriter){

		outputWriter.println("\n********** Reading Query **********\n");
		long startTimeForQuery = System.currentTimeMillis();

		//Load query
		File queryFile = new File(queryFilePath);
		InputStream queryFileStream = null;
		Set<Integer> dimensionSet = new HashSet<Integer>();
		Long diskPageSize = null;
		Long pointerSize = null;
		Long keySize = null;

		try {
			queryFileStream = new FileInputStream(queryFile);
			Scanner scanner = null;
			scanner = new Scanner(queryFileStream);

			//read dimension ser
			if(scanner.hasNextLine()){
				String fileLine = scanner.nextLine();
				Scanner lineScanner = new Scanner(fileLine);
				while(lineScanner.hasNext()){
					Integer dimension = lineScanner.nextInt();
					dimensionSet.add(dimension-1);			
				}
				lineScanner.close();
			}
			diskPageSize = scanner.nextLong();
			pointerSize = scanner.nextLong();
			keySize = scanner.nextLong();
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("Query file with path \""+queryFilePath +"\"not found ");
		}finally {
			try {
				if(queryFileStream  != null)
					queryFileStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		long stopTimeForQuery = System.currentTimeMillis();
		outputWriter.println("Query read time :: " + (stopTimeForQuery - startTimeForQuery) + "ms");
		Tuple.setDimensionSet(dimensionSet);
		outputWriter.println("dimension set :: " + dimensionSet);
		outputWriter.println("dimension set length :: " + dimensionSet.size());
		outputWriter.println("Disk page size ::" + diskPageSize);
		outputWriter.println("Pointer size ::" + pointerSize);
		outputWriter.println("Key size ::" + keySize);
		rTree = new RTree(diskPageSize, pointerSize, keySize, dimensionSet.size());
		outputWriter.println("\n********** Query File loaded successfully **********\n");
	}


	/**
	 * Reads and load tuples from file into rTree
	 * @param inputDataSource
	 * @param rTree
	 */
	public void loadDataIntoRTree(File inputDataSource){

		InputStream inputDataSetStream = null;
		try {
			inputDataSetStream = new FileInputStream(inputDataSource);
			Scanner scanner = new Scanner(inputDataSetStream);

			while(scanner.hasNextLine()){
				String fileLine = scanner.nextLine();
				Tuple tuple = Tuple.createTuple(fileLine);
				//insert tuple into rtree

				rTree.insertTuple(tuple);
			}

			scanner.close();
			inputDataSetStream.close();

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Actual method to find out skyline using BBS algorithm
	 * 
	 * @param outputWriter
	 * @param printSkyline
	 * @return no of skylines
	 */
	public int runBBSAlgo(PrintWriter outputWriter, boolean printSkyline){
		int comparison = 0;
		PriorityQueue<MinDistSequenceElement> heap = new PriorityQueue<MinDistSequenceElement>();
		Node rootNode = rTree.getRoot();
		Set<Tuple> skylines = new HashSet<Tuple>();
		heap.add(rootNode);

		while (!heap.isEmpty()) {

			MinDistSequenceElement minDistSequenceElement = heap.remove();

			//Case :: heap top ie tuple
			if(minDistSequenceElement instanceof Tuple) {
				boolean isDominated = false;
				Tuple nextTuple = (Tuple)minDistSequenceElement;

				//check if dominated by skylines till moment
				for (Tuple tuple : skylines) {
					comparison++;
					if(tuple.isDominate(nextTuple)){
						isDominated = true;
						break;
					}
				}
				if(!isDominated){
					//declare it skyline
					skylines.add(nextTuple);
					if(printSkyline) {
						outputWriter.println(nextTuple.toString());
					}

					//prune hepa elements dominated by this skyline
					Iterator<MinDistSequenceElement> heapIterator = heap.iterator();
					while (heapIterator.hasNext()) {
						comparison++;
						MinDistSequenceElement heapElement = (MinDistSequenceElement) heapIterator
								.next();
						if(heapElement instanceof Tuple ){
							if(nextTuple.isDominate((Tuple)heapElement))
								heapIterator.remove();
						} 
						else {
							Node node = (Node)heapElement;
							MBR mbr = node.getMbr();
							if(mbr.isDominatedBy(nextTuple)){
								heapIterator.remove();
							}
						} 
					}
				}
			}
			//case heap top is intermediate node
			else if(minDistSequenceElement instanceof IntermediateNode ){
				IntermediateNode intermediateNode = (IntermediateNode)minDistSequenceElement;
				//add childNode to heap if not dominated by any skyline
				Set<Node> childNodes = intermediateNode.getChildNodes();
				for (Node node : childNodes) {
					boolean isNodeDominated = false;
					MBR mbr = node.getMbr();
					for (Tuple tuple : skylines) {
						comparison++;
						if(mbr.isDominatedBy(tuple)){
							isNodeDominated = true;
							break;
						}				
					}
					if (!isNodeDominated) {
						heap.add(node);
					}			
				}
			} else {
				//case :: leafnode
				LeafNode leafNode = (LeafNode)minDistSequenceElement;
				Set<Tuple> dataTuples = leafNode.getTuples();
				//add tuples to heap if not by skylines
				for (Tuple dataTuple : dataTuples) {
					boolean isTupleDominated = false;
					for (Tuple skylineTuple : skylines) {
						comparison++;					
						if(skylineTuple.isDominate(dataTuple)){
							isTupleDominated = true;
							break;
						}				
					}
					if (!isTupleDominated) {
						heap.add(dataTuple);
					}			
				}
			}			
		}

		outputWriter.println("Total number of object to object comparison :: " + comparison);

		return skylines.size();
	}

	/**
	 * Writes node to the disk
	 */
	public void writeNodesToDisk(){
		Node root = this.rTree.getRoot();
		try {
			root.writeToFile();
		} catch (IOException e) {
			System.out.println("Exception while creating files for node ::\n"+ e.getMessage());
		}
	}

	/**
	 * Actual method to find out skyline using BBS algorithm
	 * 
	 * @param outputWriter
	 * @param printSkyline
	 * @return no of skylines
	 * @throws FileNotFoundException 
	 */
	public int runBBSAlgoNodeInFile(PrintWriter outputWriter, boolean printSkyline) throws FileNotFoundException{
		int comparison = 0;
		PriorityQueue<MinDistSequenceElement> heap = new PriorityQueue<MinDistSequenceElement>();
		Node rootNode = readNodeFromFile("node"+rTree.getRootNodeId()+".txt", null);
		//rTree.setRoot(rootNode);
		Set<Tuple> skylines = new HashSet<Tuple>();
		heap.add(rootNode);

		while (!heap.isEmpty()) {

			MinDistSequenceElement minDistSequenceElement = heap.remove();

			//Case :: heap top is tuple
			if(minDistSequenceElement instanceof Tuple) {
				boolean isDominated = false;
				Tuple nextTuple = (Tuple)minDistSequenceElement;

				//check if dominated by skylines till moment
				for (Tuple tuple : skylines) {
					comparison++;
					if(tuple.isDominate(nextTuple)){
						isDominated = true;
						break;
					}
				}
				if(!isDominated){
					//declare it skyline
					skylines.add(nextTuple);
					if(printSkyline) {
						outputWriter.println(nextTuple.toString());
					}

					//prune heap elements dominated by this skyline
					Iterator<MinDistSequenceElement> heapIterator = heap.iterator();
					while (heapIterator.hasNext()) {
						comparison++;
						MinDistSequenceElement heapElement = (MinDistSequenceElement) heapIterator
								.next();
						if(heapElement instanceof Tuple ){
							if(nextTuple.isDominate((Tuple)heapElement))
								heapIterator.remove();
						} 
						else {
							Node node = (Node)heapElement;
							MBR mbr = node.getMbr();
							if(mbr.isDominatedBy(nextTuple)){
								heapIterator.remove();
							}
						} 
					}
				}
			}
			//case heap top is intermediate node
			else if(minDistSequenceElement instanceof IntermediateNode ){
				IntermediateNode intermediateNode = (IntermediateNode)minDistSequenceElement;
				//add childNode to heap if not dominated by any skyline
				Map<Integer, MBR> childNodes = intermediateNode.getChildNodeIDSet();
				for (Map.Entry<Integer, MBR> nodeEntry : childNodes.entrySet()) {
					boolean isNodeDominated = false;
					MBR mbr = nodeEntry.getValue();
					for (Tuple tuple : skylines) {
						comparison++;
						if(mbr.isDominatedBy(tuple)){
							isNodeDominated = true;
							break;
						}				
					}
					if (!isNodeDominated) {
						Node node = readNodeFromFile("node"+nodeEntry.getKey() + ".txt", mbr);
						heap.add(node);
					}			
				}
			} else {
				//case :: leafnode
				LeafNode leafNode = (LeafNode)minDistSequenceElement;
				Set<Tuple> dataTuples = leafNode.getTuples();
				//add tuples to heap if not by skylines
				for (Tuple dataTuple : dataTuples) {
					boolean isTupleDominated = false;
					for (Tuple skylineTuple : skylines) {
						comparison++;					
						if(skylineTuple.isDominate(dataTuple)){
							isTupleDominated = true;
							break;
						}				
					}
					if (!isTupleDominated) {
						heap.add(dataTuple);
					}			
				}
			}			
		}

		outputWriter.println("Total number of object to object comparison :: " + comparison);

		return skylines.size();
	}

	private Node readNodeFromFile(String nodeDataFileName, MBR mbr) throws FileNotFoundException{
		File nodeDataFile =new File(nodeDataFileName);
		Scanner scanner = new Scanner(nodeDataFile);
		boolean isLeaf = scanner.nextBoolean();
		Node node = null;
		if(isLeaf){
			node = LeafNode.createNodeFromStream(scanner, mbr);
		} else {
			node = IntermediateNode.createNodeFromStream(scanner, mbr);
		}
		scanner.close();
		nodeDataFile.delete();	
		return node;
	}

}

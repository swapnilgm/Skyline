import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * 
 */

/**
 * @author Swapnil
 *
 */
public class SkylineFinderUI {

	/**
	 * First argument is data source file path
	 * Second argument is query file path
	 * third argument is boolean for printing skyline tuples
	 * @param args
	 */
	public static void main(String[] args) {

		String inputDataSetFilePath = null;
		String queryFilePath = null;
		boolean printTuple = false;
		if(args.length < 2){
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter the data source file path :: ");
			try {
				inputDataSetFilePath = br.readLine();
				System.out.println("\nEnter the query file path :: ");
				queryFilePath = br.readLine();
				
				System.out.println("\nDo you want to print skyline point?(Y/N) :: ");
				String choice = br.readLine().toLowerCase();
				if(choice.equals("y") || choice.equals("yes"))
					printTuple = true;
				else {
					printTuple = false;
				}
			} catch (IOException e) {
				System.out.println("Error : Exception while reading input" + e.getMessage());	
			}
		} else {
			inputDataSetFilePath = args[0];
			queryFilePath = args[1];
			if(args.length > 2){
				String choice = args[2].toLowerCase();
				if(choice.equals("y") || choice.equals("yes"))
					printTuple = true;
				else {
					printTuple = false;
				} 
			}
		}
		
		File inputDataSource = new File(inputDataSetFilePath);
		String outputFilePath =  inputDataSource.getPath() + ".out";
		File outputFile = new File(outputFilePath);		
		Writer fw = null;
		try {
			fw = new FileWriter(outputFile);
			System.out.println("Please check ouput at file :: " + outputFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PrintWriter outputWriter = new PrintWriter(fw);
		
		SkylineFinder skylineFinder = new SkylineFinder(printTuple);
		skylineFinder.readQuery(queryFilePath, outputWriter);
		
		outputWriter.println("**** Loading data into rtree *****");
		long startTimeForRtree = System.currentTimeMillis();
		skylineFinder.loadDataIntoRTree(inputDataSource);
		long stopTimeForRtree = System.currentTimeMillis();
		long rTreeRunTime = (stopTimeForRtree - startTimeForRtree);
		outputWriter.println("Running time for Rtree:: " + rTreeRunTime + "ms");
		outputWriter.println("**** Loaded data into rtree *****");
		
		outputWriter.println("\n********** Start of BBS algorithm In-Memory implementation **********\n");
		outputWriter.println("Generating skyline set with BBS algorithm");
		long startTimeForBBS = System.currentTimeMillis();
		int skylineSetSize = skylineFinder.runBBSAlgo( outputWriter, printTuple);
		long stopTimeForBBS = System.currentTimeMillis();
		long BBSRunTime = (stopTimeForBBS - startTimeForBBS);
		outputWriter.println("Size of skyline set :: "+ skylineSetSize);
		outputWriter.println("Running time for BBS:: " + BBSRunTime + "ms");
		outputWriter.println("Total Running time for Rtree+ BBS:: " + (rTreeRunTime+BBSRunTime) + "ms");
		outputWriter.flush();
		outputWriter.println("\n********** END of BBS algorithm implementation **********\n");
		
	/*	
	 *Modified ::: removed disk based implementation call
	 * 
	 * outputWriter.println("\n********** Start of BBS algorithm Disk based implementation **********\n");
	 * 
	 * 
		skylineFinder.writeNodesToDisk();
		outputWriter.println("Generating skyline set with BBS algorithm");
		startTimeForBBS = System.currentTimeMillis();
		try {
			skylineSetSize = skylineFinder.runBBSAlgoNodeInFile( outputWriter, printTuple);
			stopTimeForBBS = System.currentTimeMillis();
			BBSRunTime = (stopTimeForBBS - startTimeForBBS);
			outputWriter.println("Size of skyline set :: "+ skylineSetSize);
			outputWriter.println("Running time for BBS:: " + BBSRunTime + "ms");
			outputWriter.println("Total Running time for Rtree+ BBS:: " + (rTreeRunTime+BBSRunTime) + "ms");
			outputWriter.flush();
			outputWriter.println("\n********** END of BBS algorithm implementation **********\n");
			
			outputWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	*/	try {
			fw.close();
		} catch (IOException e1) {
			System.out.println("Error while closing output file writer.");
		}
		
	}

}

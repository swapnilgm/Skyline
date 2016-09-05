import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Swapnil
 *
 */
public class SkylineFinder {

	
	boolean printSkylineTuple;
	
	public SkylineFinder(boolean printSkylineTuple){
		this.printSkylineTuple = printSkylineTuple;
	}
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
		
		SkylineFinder skylineFinder = new SkylineFinder(printTuple);
		Window window = skylineFinder.readQuery(queryFilePath);

		if(window == null) {
			return;
		}
		
		File inputDataSource = new File(inputDataSetFilePath);
		String outputFilePath =  inputDataSource.getPath() + ".out";
		File outputFile = new File(outputFilePath);
		Writer fw = null;
		try {
			fw = new FileWriter(outputFile);
			System.out.println("Please check ouput at file :: " + outputFilePath);
		} catch (IOException e) {
			fw = new PrintWriter(System.out);
		}
		
		PrintWriter outputWriter = new PrintWriter(fw);
		outputWriter.println("\n********** Start of BNL algorithm implementation **********\n");
		outputWriter.println("Generating skyline set with BNL algorithm");
		long startTimeForBNL = System.currentTimeMillis();
		int skylineSetSize = skylineFinder.BNLAlgorithm(inputDataSource, window, outputWriter);
		long stopTimeForBNL = System.currentTimeMillis();
		outputWriter.println("Total running time :: " + (stopTimeForBNL - startTimeForBNL) + "ms");
		outputWriter.println("Total tuple comparisons :: " + window.getTupleComparisons());
		outputWriter.println("Size of skyline set :: "+ skylineSetSize);

		outputWriter.println("\n********** END of BNL algorithm implementation **********\n");
		
		window.clear();

		outputWriter.println("\n**********Start of SFS algorithm implementation **********\n");
		outputWriter.println("Generating skyline set with SFS algorithm");
		inputDataSource = new File(inputDataSetFilePath);		
		long startTimeForSFS = System.currentTimeMillis();
		skylineSetSize = skylineFinder.SFSAlgorithm(inputDataSource, window, outputWriter);
		long stopTimeForSFS = System.currentTimeMillis();
		outputWriter.println("Total running time :: " + (stopTimeForSFS - startTimeForSFS) + "ms");
		outputWriter.println("Total tuple comparisons :: " + window.getTupleComparisons());
		outputWriter.println("Size of skyline set :: "+ skylineSetSize);

		outputWriter.println("********** END of SFS algorithm implementation **********");
		window.clear();
		outputWriter.flush();
		outputWriter.close();
		try {
			fw.close();
		} catch (IOException e1) {
			System.out.println("Error while closing output file writer.");
		}
		
	}



	/**
	 * Reads the query file. capture the set of dimension in skyline query
	 * Get the window size 
	 * @param queryFilePath
	 * @return
	 */
	private Window readQuery(String queryFilePath){

		System.out.println("\n********** Reading Query **********\n");
		long startTimeForQuery = System.currentTimeMillis();

		//Load query
		File queryFile = new File(queryFilePath);
		InputStream queryFileStream = null;
		Set<Integer> dimensionSet = new HashSet<Integer>();
		Long windowSize = null;
		try {
			queryFileStream = new FileInputStream(queryFile);
			Scanner scanner = null;
			scanner = new Scanner(queryFileStream);

			if(scanner.hasNextLine()){
				String fileLine = scanner.nextLine();
				Scanner lineScanner = new Scanner(fileLine);
				while(lineScanner.hasNext()){
					Integer dimension = lineScanner.nextInt();
					dimensionSet.add(dimension);			
				}
			}
			if(scanner.hasNextLine()){
				windowSize = scanner.nextLong();
			}
		} catch (FileNotFoundException e) {
			System.out.println("Query file with path \""+queryFilePath +"\"not found ");
			return null;
		}finally {
			try {
				if(queryFileStream  != null)
					queryFileStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		long stopTimeForQuery = System.currentTimeMillis();
		System.out.println("Query read time :: " + (stopTimeForQuery - startTimeForQuery) + "ms");
		Tuple.setDimensionSet(dimensionSet);
		Window window = new Window(windowSize, dimensionSet);
		System.out.println("Window size ::" + windowSize);
		System.out.println("dimension set :: " + dimensionSet);
		System.out.println("dimension set length :: " + dimensionSet.size());
		System.out.println("\n********** Query File loaded successfully **********\n");
		return window;
	}

	/**
	 * Implements block nested loop algorithm to find skyline points 
	 * in input data source
	 * @param inputDataSource
	 * @param window
	 */
	private int BNLAlgorithm(File inputDataSource, Window window, PrintWriter outputWriter){
		//BNL starts here
		//load dataSet 
		
		long inTimeStamp = 0;
		long outTimeStamp = 0;
		InputStream inputDataSetStream = null;
		int tempFileIndex = 0;

		File tempFile = null;
		int skylineSetSize = 0;
		//int pass =0;
		try {
			while(true) {
				//System.out.println(pass++);
				inputDataSetStream = new FileInputStream(inputDataSource);
				Scanner scanner = new Scanner(inputDataSetStream);

				PrintWriter tempFileWriter = null;
				FileWriter fw = null;

				while(scanner.hasNextLine()){
					String fileLine = scanner.nextLine();
					Scanner lineScanner = new Scanner(fileLine);

					//scan timeStamp
					if(tempFileIndex != 0 && lineScanner.hasNext()) {
						outTimeStamp = lineScanner.nextLong();

						//prints skyLine point till moment
						List<Tuple> skylineTuple = window.removeAndGetOldertuples(outTimeStamp);
						if(this.printSkylineTuple) {
							for (Tuple tuple : skylineTuple) {
								outputWriter.println("Tuple ID " + tuple.getId() + " :: " + tuple.getData());
							}
						}
						skylineSetSize = skylineSetSize + skylineTuple.size();
					}

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

					//process tuple
					Tuple tuple = new Tuple(id , tupleData);				

					boolean isPruned = window.pruneDominatedTuples(tuple);
					//add to probable skyLine set
					if(!isPruned){
						tuple.setTimeStamp(inTimeStamp);
						inTimeStamp++;
						//add to window
						if(!window.isFull()){
							window.addTuple(tuple);
						}
						else {
							//pass it temporary file
							if(tempFile == null) {
								tempFile = File.createTempFile("skyline$", null);
								//System.out.println(tempFile.getAbsolutePath());
								fw = new FileWriter(tempFile, true);
								tempFileWriter = new PrintWriter(fw);
							}
							tempFileWriter.println(tuple.getTimeStamp()+" " + tuple);								
						}
					}
				}

				scanner.close();
				inputDataSetStream.close();

				//delete temporary file
				if(tempFileIndex != 0) 
					inputDataSource.delete();

				//if temporary file required pass it for next iteration
				if(tempFile != null){
					if(tempFileWriter != null)
						tempFileWriter.flush();
					tempFileWriter.close();
					if(fw != null)
						try {
							fw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}

					//change input data source and tempFile
					inputDataSource = tempFile;
					tempFileIndex++;
					tempFile = null;
				}else{
					break;
				}
			}
			List<Tuple> skylineTuple = window.getTupleLists();
			skylineSetSize = skylineSetSize + skylineTuple.size();


			//print skyline tuple from window
			if(this.printSkylineTuple) {
				for (Tuple tuple : skylineTuple) {
					outputWriter.println("Tuple ID " + tuple.getId() + " :: " + tuple.getData());
				}
				outputWriter.println(" ");
			}

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return skylineSetSize;

	}

	/**
	 * Implement SFS algorithm on input data source
	 * @param inputDataSource
	 * @param window
	 */
	private int SFSAlgorithm(File inputDataSource, Window window, PrintWriter outputWriter){
		//sort input data as per entropy function
		//long startTime = System.currentTimeMillis();
		File sortedFile = createSortedFile(inputDataSource);
		//long stopTime = System.currentTimeMillis();
		//outputWriter.println("\nTime for creating sorted tuple file :: " + (stopTime -startTime) + "ms");
		//pass sorted data to BNL 
		int skylineSetSize = BNLAlgorithm(sortedFile, window, outputWriter);
		//long stopTimeForBNL = System.currentTimeMillis();
		//outputWriter.println("\nTime for BNL Algorithm  :: " + (stopTimeForBNL -stopTime) + "ms");
		//Delete tempFile of sorted data
		sortedFile.delete();
		return skylineSetSize;
	}


	private File createSortedFile(File inputDataSource){
		InputStream inputDataSetStream = null;
		File sortedFile = null;
		
		try {

			//Read tuple in memory
			inputDataSetStream = new FileInputStream(inputDataSource);
			Scanner scanner = new Scanner(inputDataSetStream);
			List<Tuple> inMemoryTuple = new ArrayList<Tuple>();

			while(scanner.hasNextLine()){
				String fileLine = scanner.nextLine();
				Tuple tuple = Tuple.createTuple(fileLine);
				inMemoryTuple.add(tuple);
			}

			//sort list
			Collections.sort(inMemoryTuple, new Comparator<Tuple>() {

				public int compare(Tuple tuple1, Tuple tuple2) {
					if(tuple1 == null)
						throw new NullPointerException();
					else if( tuple2 == null )
						throw new NullPointerException();

					if(tuple1.getEntropyValue() < tuple2.getEntropyValue()) {
						return -1;
					}
					else if(tuple1.getEntropyValue() > tuple2.getEntropyValue()) {
						return 1;
					}
					return 0;
				}
			});

			//write sorted list to file
			sortedFile = File.createTempFile("SFS", null);
			FileWriter fw = new FileWriter(sortedFile, true);
			PrintWriter tempFileWriter = new PrintWriter(fw);
			for (Tuple tuple : inMemoryTuple) {
				tempFileWriter.println(tuple);								
			}
			tempFileWriter.flush();

			//release resource safely
			tempFileWriter.close();
			fw.close();
			scanner.close();
			inputDataSetStream.close();

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sortedFile;
		
		
	}

}

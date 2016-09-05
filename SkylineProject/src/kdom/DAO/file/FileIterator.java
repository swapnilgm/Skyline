/**
 * 
 */
package kdom.DAO.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import kdom.DAO.core.AbstractDataIterator;
import kdom.core.Tuple;

/**
 * @author Swapnil
 * @param <E>
 *
 */
public class FileIterator extends AbstractDataIterator {

	private Scanner scanner;
	
	/**
	 * @throws FileNotFoundException 
	 * 
	 */
	public FileIterator(File file) throws FileNotFoundException {
		scanner = new Scanner(file);
	}

	/**
	 * Check if there any more tuple entry available in database
	 */
	public boolean hasNext() {
		boolean hasNext = false;
		if(scanner != null)
		{
			hasNext = scanner.hasNext();
			if(!scanner.hasNext()){
				scanner.close();
			}
		}
		
		return hasNext;
	}

	/**
	 * Read next line from file
	 * Create tuple object associated with it and return
	 */
	public Tuple next() {
		Tuple tuple = null;
		if(hasNext()){
			String tupleString = scanner.nextLine();
			//logger.debug("tuple string :: "+tupleString);
			tuple = Tuple.createTuple(tupleString);
		} else {
			throw new NoSuchElementException();
		}
		return tuple;
	}


}

/**
 * 
 */
package kdom.DAO.inmemory;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import kdom.DAO.core.AbstractDataIterator;
import kdom.core.Tuple;

/**
 * @author Swapnil
 * @param <E>
 *
 */
public class InMemoryDbDataIterator extends AbstractDataIterator {

	private Iterator<Tuple> iterator;
	
	/**
	 * @throws FileNotFoundException 
	 * 
	 */
	public InMemoryDbDataIterator(Collection<Tuple> collectioin) {
		this.iterator = collectioin.iterator();
	}

	/**
	 * Check if there any more tuple entry available in database
	 */
	public boolean hasNext() {
		boolean hasNext = false;
		if(iterator != null)
		{
			hasNext = iterator.hasNext();
			
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
			tuple = iterator.next();
		} else {
			throw new NoSuchElementException();
		}
		return tuple;
	}


}

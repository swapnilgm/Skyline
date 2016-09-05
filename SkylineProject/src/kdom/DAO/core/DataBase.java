/**
 * 
 */
package kdom.DAO.core;

import java.util.ArrayList;

import kdom.core.Tuple;

/**
 * @author Swapnil
 * @param <T>
 *
 */
public interface DataBase extends Iterable<Tuple> {
	
	public DataIterator iterator();
	
	public ArrayList<Tuple> getAllTuples();

}

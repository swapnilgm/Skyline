/**
 * 
 */
package kdom.DAO.inmemory;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;

import kdom.DAO.core.DataBase;
import kdom.DAO.core.DataIterator;
import kdom.core.Tuple;

/**
 * @author Swapnil
 *
 */
public class InMemoryDB implements DataBase {

	private Collection<Tuple> collection;
	/**
	 * @throws FileNotFoundException 
	 * 
	 */
	public InMemoryDB(Collection<Tuple> collection) {
		this.collection = collection;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public DataIterator iterator() {
		DataIterator dataIterator = null;
		dataIterator = new InMemoryDbDataIterator(collection);
		return dataIterator;
	}

	public ArrayList<Tuple> getAllTuples() {
		DataIterator dataIterator = iterator();
		ArrayList<Tuple> tupleList = new ArrayList<Tuple>();
		while (dataIterator.hasNext()) {
			Tuple tuple = (Tuple) dataIterator.next();
			tupleList.add(tuple);
		}
		return tupleList;
	}
}

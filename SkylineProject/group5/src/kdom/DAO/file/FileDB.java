/**
 * 
 */
package kdom.DAO.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import kdom.DAO.core.DataBase;
import kdom.DAO.core.DataIterator;
import kdom.core.Tuple;

/**
 * @author Swapnil
 *
 */
public class FileDB implements DataBase {

	private File file;
	/**
	 * @throws FileNotFoundException 
	 * 
	 */
	public FileDB(File file) throws FileNotFoundException {
		this.file = file;
		if(!file.isFile() && !file.exists()){
			throw new FileNotFoundException();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public DataIterator iterator() {
		DataIterator dataIterator = null;
		try {
			dataIterator = new FileIterator(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

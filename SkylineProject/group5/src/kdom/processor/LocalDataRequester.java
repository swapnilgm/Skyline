/**
 * 
 */
package kdom.processor;

import java.util.List;

import kdom.DAO.core.DataBase;
import kdom.core.Tuple;
import kdom.properties.ServerProperties;

/**
 * @author ritika
 *
 */
public class LocalDataRequester implements DataFinder
{

	private List<Tuple> data = null; 


	public void run() {
		// TODO Auto-generated method stub
		DataBase dataCollector=ServerProperties.getDatabase();
		this.data=dataCollector.getAllTuples();
	}


	public List<Tuple> getData()
	{
		return this.data;
	}

}

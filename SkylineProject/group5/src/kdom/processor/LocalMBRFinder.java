/**
 * 
 */
package kdom.processor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import kdom.DAO.core.DataBase;
import kdom.DAO.core.DataIterator;
import kdom.core.MBR;
import kdom.core.Tuple;
import kdom.properties.ServerProperties;

import org.apache.log4j.Logger;

/**
 * @author Swapnil
 *
 */
public class LocalMBRFinder implements Runnable, MBRFinder {
	
	private static Logger logger = Logger.getLogger(LocalMBRFinder.class);
	
	private MBR mbr;
	
	private Set<String> queryDimensionSet;
	/**
	 * 
	 */
	public LocalMBRFinder(Set<String> queryDimensionSet) {
		this.mbr = null;
		this.queryDimensionSet = queryDimensionSet;
	}
	
	public void run(){
		
		logger.debug("Computing local MBR ");
		DataBase databse = ServerProperties.getDatabase();
		DataIterator dataIterator = databse.iterator();
		logger.debug("Iterating over local tuples ");
		
		if(dataIterator.hasNext()){
			
			Tuple tuple = dataIterator.next();
			
			//logger.debug("Tuple ::  "+ tuple.toString());
			//create mbr corresponding single tuple
			Map<String, Double> tupleData = tuple.getData();
			Map<String, Double> upperBound = new LinkedHashMap<String, Double>();
			Map<String, Double> lowerBound = new LinkedHashMap<String, Double>();
			
			for (String dimension : this.queryDimensionSet) {
				Double dimValue = tupleData.get(dimension);
				upperBound.put(dimension, dimValue);
				lowerBound.put(dimension, dimValue);
			}
			this.mbr = new MBR(lowerBound, upperBound);
			
			//this mbr will be adjusted/updated for next containment of next tuple
		}
		
		while (dataIterator.hasNext()) {
			Tuple tuple = (Tuple) dataIterator.next();			
			//logger.info("Tuple ::  "+ tuple.toString());
			mbr.adjustMbr(tuple.getData(), this.queryDimensionSet);			
		}
	}
	
	public MBR getMbr() {
		return this.mbr;
	}

}

/**
 * 
 */
package kdom.kdomfinder;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import kdom.DAO.core.DataBase;
import kdom.DAO.core.DataIterator;
import kdom.core.Tuple;

import org.apache.log4j.Logger;

/**
 * @author Swapnil
 *
 */
public class OneScanKDomFinder implements KDomFinder {

	final private static Logger logger = Logger.getLogger(OneScanKDomFinder.class);
	private int k;

	private Set<Tuple> kDominantTuples;

	private Set<Tuple> fullSkylines;

	private DataBase db;
	
	private Map<String, Double> filter;
	
	private Set<String> queryDimensionSet;
	/**
	 * 
	 */
	public OneScanKDomFinder(int k, DataBase db, Map<String, Double> filter, Set<String> queryDimensionSet) {
		this.k = k;
		this.filter = filter;
		this.kDominantTuples = new LinkedHashSet<Tuple>();
		this.fullSkylines = new LinkedHashSet<Tuple>();
		this.db = db;
		this.queryDimensionSet =queryDimensionSet;

	}

	public OneScanKDomFinder(int k, Set<Tuple> fullSkyline, DataBase db, Map<String, Double> filter, Set<String> queryDimensionSet) {
		this.k = k;
		this.filter = filter;
		this.kDominantTuples = new LinkedHashSet<Tuple>();
		this.fullSkylines = fullSkyline;
		this.db = db;
		this.queryDimensionSet = queryDimensionSet;

	}
	
	public void run(){

		this.kDominantTuples.clear();				
		DataIterator dataIterator = this.db.iterator();
		while (dataIterator.hasNext()) {
			boolean isFullSkyline = true;
			Tuple tuple = (Tuple) dataIterator.next();
			
			if(tuple.isFullDominatedBy(this.filter, this.queryDimensionSet)){
				continue;
			}
			
			Iterator<Tuple> fullSkyIterator = this.fullSkylines.iterator(); 
			while (fullSkyIterator.hasNext()) {
				Tuple fullSkyTuple = (Tuple) fullSkyIterator.next();
				if(tuple.isFullDominate(fullSkyTuple,this.queryDimensionSet)){
					fullSkyIterator.remove();
				} else if( fullSkyTuple.isFullDominate(tuple,this.queryDimensionSet)){
					isFullSkyline = false;
					break;
				}
			}

			if(isFullSkyline) {
				boolean isDominant = true;

				//Set<Tuple> dummyKdom = new HashSet<Tuple>(this.kDominantTuples);

				Iterator<Tuple> kDomIterator = this.kDominantTuples.iterator();
				while (kDomIterator.hasNext()) {
					Tuple kSkyTuple = (Tuple) kDomIterator.next();
					if( kSkyTuple.isKDominate(tuple, k, this.queryDimensionSet)){
						isDominant = false;						
					}
					if(tuple.isKDominate(kSkyTuple, k, this.queryDimensionSet)){
						kDomIterator.remove();
						this.fullSkylines.add(kSkyTuple);
					}
				}
				if(isDominant){
					this.kDominantTuples.add(tuple);
				} else {
					this.fullSkylines.add(tuple);
				}					
			}			
		}
		if(logger.isDebugEnabled()){
			logger.debug("Computer KDoms :: " + kDominantTuples.size());
			logger.debug("Computer FullSky :: " + fullSkylines.size());
		}
	}


	public Set<Tuple> getKdoms() {
		return this.kDominantTuples;
	}

	public Set<Tuple> getFullSkylines() {
		//Set<Tuple> fullSky = new HashSet<Tuple>(this.kDominantTuples);
		//fullSky.addAll(this.fullSkylines);
		return this.fullSkylines;
	}

	public int getNoOfMessage() {
		return 0;
	}

	public void setNoOfMessage(int noOfMessages) {
		//DO nothing
	}

}

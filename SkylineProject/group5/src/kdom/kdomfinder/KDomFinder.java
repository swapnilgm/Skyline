/**
 * 
 */
package kdom.kdomfinder;

import java.util.Set;

import kdom.core.Tuple;

/**
 * @author Swapnil
 *
 */
public interface KDomFinder extends Runnable {
	
	public Set<Tuple> getKdoms();
	
	public Set<Tuple> getFullSkylines();
	
	public int getNoOfMessage();

	public void setNoOfMessage(int noOfMessages);
}

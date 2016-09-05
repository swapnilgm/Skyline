/**
 * 
 */
package kdom.processor;

import java.util.List;

import kdom.core.Tuple;

/**
 * @author ritika
 *
 */
public interface DataFinder extends Runnable 
{

	public List<Tuple> getData();

}

/**
 * 
 */
package kdom.processor.TreeCreator;

import kdom.core.MBR;


/**
 * @author Swapnil
 *
 */
public class Forest extends ParallelTrees {

	/**
	 * 
	 */
	public Forest(int parallelDegree) {
		super(Integer.MAX_VALUE);
		
	}
	
	@Override
	protected double getPruningPower(MBR parentMbr, MBR childMbr){
		return parentMbr.enclosedArea(childMbr);
	}

}

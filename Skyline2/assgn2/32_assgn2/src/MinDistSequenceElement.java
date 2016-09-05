/**
 * 
 */

/**
 * Abstract class to bound min distance criteria on inherited classes
 * @author Swapnil
 *
 */
public abstract class MinDistSequenceElement implements Comparable<MinDistSequenceElement> {
	
	private double minDist;
	
	/**
	 * @return the minDist
	 */
	protected double getMinDist() {
		return minDist;
	}

	/**
	 * @param minDist the minDist to set
	 */
	protected void setMinDist(double minDist) {
		this.minDist = minDist;
	}

	public int compareTo(MinDistSequenceElement o) {
		if(o == null)
			throw new NullPointerException();
		int retrunValue = 0;
		double thisMinDist = this.getMinDist();
		double otherMinDist = o.getMinDist();
		if(thisMinDist == otherMinDist ){
			retrunValue = 0;
		} else if (thisMinDist > otherMinDist) {
			retrunValue = 1;
		} else {
			retrunValue = -1;
		}
		return retrunValue;
	}

}

/**
 * 
 */

/**
 * Random pair to make up entry of tuples 
 * @author Swapnil
 *
 */
public class Pair<T> {

	private T high;
	
	private T low;
	/**
	 * 
	 */
	public Pair(T tupleLow, T tupleHigh) {
		this.high =tupleHigh;
		this.low = tupleLow;
	}
	public T getHigh() {
		return high;
	}
	public T getLow() {
		return low;
	}

}

package kdom.core;
import java.util.Map;
import java.util.Set;

/**
 * 
 */

/**
 * @author Swapnil
 *
 */
public class MBR {

	/**
	 * String key:: Dimension name
	 * value :: lower bound value on key dimension 
	 */
	private Map<String, Double> lowerBound;
	
	private Map<String, Double> upperBound;

	/**
	 * just for json creation
	 */
	public MBR(){		
	}
	/**
	 * 
	 */
	public MBR(Map<String, Double> lowerBound, Map<String, Double> upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	public Map<String, Double> getUpperBound() {
		return upperBound;
	}
	public void setUpperBound(Map<String, Double> upperBound) {
		this.upperBound = upperBound;
	}

	public Map<String, Double> getLowerBound() {
		return lowerBound;
	}
	
	public void setLowerBound(Map<String, Double> lowerBound) {
		this.lowerBound = lowerBound;
	}
	
	
	/**
	 * Adjust the mbr as per the new dataPoint or tuple
	 * @param dataPoint
	 */
	public void adjustMbr(Map<String, Double> dataPoint, Set<String> queryDimensionSet){
		
		for (String dimension : queryDimensionSet) {
			
			Double value = dataPoint.get(dimension);
			Double lowerBoundValue = this.lowerBound.get(dimension);
			this.lowerBound.put(dimension, Math.min(lowerBoundValue,value));
			Double upperBoundValue = this.upperBound.get(dimension);
			this.upperBound.put(dimension, Math.max(upperBoundValue, value));
		}
	}

	public boolean isUnderDominationOf(MBR otherMbr) 
	{
		boolean isDominate = false;	
		isDominate = isDominate(otherMbr.getUpperBound(), this.upperBound);
		return isDominate;	
	}
	
	public boolean isDominate(MBR otherMbr){
		boolean isDominate = false;	
		isDominate = isDominate(this.upperBound, otherMbr.getLowerBound());
		return isDominate;	
	}
	
	private boolean isDominate(Map<String, Double> tempData, Map<String, Double> otherData){
		boolean isDominate = false;	

		//loop will compare all dimension values
		for (Map.Entry<String, Double> dimensionEntry : tempData.entrySet()) {

			Double localDimensionValue = dimensionEntry.getValue();
			Double otherDimensionValue = otherData.get(dimensionEntry.getKey());
			if(localDimensionValue > otherDimensionValue) {
				return false;
			} else if(localDimensionValue < otherDimensionValue){				
				isDominate = true;
			} 
		}

		return isDominate;		
	}
	
	public Double dominationArea(MBR otherMbr)
	{
		Map<String, Double> otherUpperBound=otherMbr.getUpperBound();
		double a,b,mbrArea=1;
		for (Map.Entry<String, Double> dimensionEntry : upperBound.entrySet()) {
			String dimension = dimensionEntry.getKey();
			a=dimensionEntry.getValue();
			b=otherUpperBound.get(dimension);
			mbrArea=mbrArea*(b-a);
		}
		return mbrArea;
	}


	public Double enclosedArea(MBR otherMbr)
	{
		Map<String, Double> otherLowerBound=otherMbr.getLowerBound();
		double u,lij,mbrArea=1, volume=1, li;
		for (Map.Entry<String, Double> dimensionEntry : upperBound.entrySet()) {
			String dimension = dimensionEntry.getKey();
			u=dimensionEntry.getValue();
			li = this.lowerBound.get(dimension);
			lij=Math.max(otherLowerBound.get(dimension), li);
			mbrArea=mbrArea*(u-lij);
			volume *=(u- li); 
		}
		if(volume == 0){
			return new Double(0);
		}
		return mbrArea/volume;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Bounds.LOWERBOUND + " = " + lowerBound + ", "+ Bounds.UPPERBOUND +" = " + upperBound;
	}

}

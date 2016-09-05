package kdom.properties;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import kdom.core.SiteDetail;

/**
 * Cluster properties 
 * @author Swapnil
 *
 */
public class ClusterProerties {

	private static Set<SiteDetail> siteDetail;
	
	private  static Map<String,Double> valueUpperBound;
	
	/**
	 * @return the valueUpperBound
	 */
	public static Map<String, Double> getValueUpperBound() {
		valueUpperBound = new LinkedHashMap<String, Double>();
		
		return valueUpperBound;
	}

	/**
	 * @param valueUpperBound the valueUpperBound to set
	 */
	public static void setValueUpperBound(Map<String, Double> valueUpperBound) {
		ClusterProerties.valueUpperBound = valueUpperBound;
	}

	public static Set<SiteDetail> getSiteDetail() {
		return siteDetail;
	}

	public static void setSiteDetail(Set<SiteDetail> siteDetail) {
		ClusterProerties.siteDetail = siteDetail;
	}
	
}

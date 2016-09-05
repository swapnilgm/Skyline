package kdom.properties;
import kdom.DAO.core.DataBase;
import kdom.core.SiteDetail;

/**
 * 
 */

/**
 * @author Swapnil
 *
 */
public class ServerProperties {
	
	
	private static DataBase database;
	
	private static SiteDetail localSiteDetails;

	public static DataBase getDatabase() {
		return database;
	}

	public static void setDatabase(DataBase database) {
		ServerProperties.database = database;
	}

	/**
	 * @return the localSiteDetails
	 */
	public static SiteDetail getLocalSiteDetails() {
		return localSiteDetails;
	}

	/**
	 * @param localSiteDetails the localSiteDetails to set
	 */
	public static void setLocalSiteDetails(SiteDetail localSiteDetails) {
		ServerProperties.localSiteDetails = localSiteDetails;
	}
	
}

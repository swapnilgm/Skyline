/**
 * 
 */
package kdom.util;

import java.io.IOException;
import java.util.StringTokenizer;

import kdom.core.SiteDetail;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.KeyDeserializer;

/**
 * @author Swapnil
 *
 */
public class SiteDetailDeserialiser extends KeyDeserializer {

	/**
	 * 
	 */
	public SiteDetailDeserialiser() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object deserializeKey(String arg0, DeserializationContext arg1)
			throws IOException, JsonProcessingException {
		// TODO Auto-generated method stub
		SiteDetail siteDetail = null;
		if(arg0.length() == 0) {
			
			return null;
		}
		else{
			StringTokenizer stringTokenizer = new StringTokenizer(arg0,"/:");
			//System.out.println("Stire details to tokenize ::"+arg0);
			String ip = stringTokenizer.nextToken();
			 ip = stringTokenizer.nextToken();
			
			int port = Integer.parseInt(stringTokenizer.nextToken());
			siteDetail = new SiteDetail(ip, port);
		}
		return siteDetail;
	}

}

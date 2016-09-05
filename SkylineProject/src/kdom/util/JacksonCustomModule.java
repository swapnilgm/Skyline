/**
 * 
 */
package kdom.util;

import kdom.core.SiteDetail;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * @author Swapnil
 *
 */
public class JacksonCustomModule extends SimpleModule {

	/**
	 * @param name
	 * @param version
	 */
	public JacksonCustomModule(String name, Version version) {
		super(name, version);
		 addKeyDeserializer(SiteDetail.class,new SiteDetailDeserialiser() );	
	}

}

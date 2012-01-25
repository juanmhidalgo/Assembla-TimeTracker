/**
 * 
 */
package com.starredsolutions.utils;

import android.content.Context;

import com.starredsolutions.assemblandroid.Constants;
import com.starredsolutions.utils.base.INetClient;
import com.starredsolutions.utils.base.IStrictMode;

/**
 * @author Juan M. Hidalgo <juanmanuel@itangelo.com>
 *
 */

/**
 * Factory class to create the correct instances
 * of a variety of classes with platform specific
 * implementations.
 * 
 */
public class PlatformSpecificImplementationFactory {
	/**
	 * Create a new StrictMode instance.
	 * @return StrictMode
	 */
	public static IStrictMode getStrictMode() {
		if(Constants.SUPPORTS_HONEYCOMB){
			return new HoneycombStrictMode();
		}else if(Constants.SUPPORTS_GINGERBREAD){
			return new LegacyStrictMode();
		}else{
			return null;
		}
	}
	
	/**
	 * Create a new NetClient instance
	 * @return NetClient
	 */
	public static INetClient getNetClient(Context context){
		if(Constants.SUPPORTS_GINGERBREAD){
			return new GingerbreadNetClient(context);
		}else{
			return new LegacyNetClient(context);
		}
	}
}

/**
 * 
 */
package com.starredsolutions.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class SettingsHelper {
	private static String TAG = "SettingsHelper";
	private static SettingsHelper instance = null;
	private SharedPreferences sp;
	private Context context;
	
	protected SettingsHelper(final Context ctx){
		context = ctx;
		sp = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static SettingsHelper getInstance(final Context ctx){
		if(instance == null){
			instance = new SettingsHelper(ctx);
		}
		return instance;
	}
	
	public String getString(String key,String defaultValue){
		return sp.getString(key, defaultValue);
	}
	
	public int getInt(String key,int defaultValue){
		try{
			return sp.getInt(key, defaultValue);
		}catch(ClassCastException e){
			String val;
			val = sp.getString(key, null);
			if(val != null){
				return Integer.parseInt(val);
			}else{
				return defaultValue;
			}
		}
	}
	
	
}

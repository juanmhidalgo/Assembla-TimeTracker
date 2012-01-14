/**
 * 
 */
package com.starredsolutions.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class SettingsHelper {
	private static String TAG = "SettingsHelper";
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);
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

	public boolean containsKey(String key){
		return sp.contains(key);
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
	
	/**
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public long  getLong(String key,long defaultValue){
		try{
			return sp.getLong(key, defaultValue);
		}catch(ClassCastException e){
			String val;
			val = sp.getString(key, null);
			if(val != null){
				return Long.parseLong(val);
			}else{
				return defaultValue;
			}
		}
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void putString(final String key,final String value){
		if(LOGV) Log.v(TAG,"setString(key=" + key + ",value=" + value + ")");
		(new Thread(new Runnable() {
			public void run() {
				sp.edit().putString(key, value).commit();
			}
		})).start();
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void putLong(final String key,final Long value){
		if(LOGV) Log.v(TAG,"setString(key=" + key + ",value=" + value + ")");
		(new Thread(new Runnable() {
			public void run() {
				sp.edit().putLong(key, value).commit();
			}
		})).start();
	}
}

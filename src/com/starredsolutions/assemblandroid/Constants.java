/**
 * 
 */
package com.starredsolutions.assemblandroid;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class Constants {
	public static boolean DEVELOPER_MODE = true;
	
	/**
     * Account type string.
     */
    public static final String ACCOUNT_TYPE = "com.starredsolutions.assembla";

    /**
     * Authtoken type string.
     */
    public static final String AUTHTOKEN_TYPE ="com.starredsolutions.assembla";
    
    public static final String USERID_KEY ="USER_ID";
    public static final String LAST_SYNC_KEY ="LAST_SYNC";
    public static final String CURRENT_TASK_KEY ="CURRENT_TASK";
    
    
    public static final long MIN_TIME_BW_SYNC = 5 * 60 *1000; //15 min
    
    
    public static boolean SUPPORTS_GINGERBREAD = false; //android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD;
    public static boolean SUPPORTS_HONEYCOMB = false; // android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;
    public static boolean SUPPORTS_FROYO = false; //android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO;
    public static boolean SUPPORTS_ECLAIR = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ECLAIR;
    
}

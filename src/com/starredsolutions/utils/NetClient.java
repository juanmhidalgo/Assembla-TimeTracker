/**
 * 
 */
package com.starredsolutions.utils;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.format.DateUtils;
import android.util.Log;

import com.starredsolutions.assemblandroid.Constants;
import com.starredsolutions.utils.base.INetClient;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public abstract class NetClient implements INetClient {
	protected static final String TAG = "NetClient";
	protected static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE) || Constants.DEVELOPER_MODE;
	
	protected static final int SECOND_IN_MILLIS = (int) DateUtils.SECOND_IN_MILLIS;
	protected Context mContext = null;
	
	
	protected OnGetUrlCompleteListener mOnComplete = null;
	protected ArrayList<NameValuePair> mHeaders = null;
	protected ArrayList<NameValuePair> mParams = null;
	
	protected RequestMethod mMethod;
	protected String mUrl = null;
	protected UsernamePasswordCredentials credentials;
	
	
	/**
     * Shared buffer used by {@link #getUrlContent(String)} when reading results
     * from an API request.
     */
    protected static byte[] sBuffer = new byte[512];
    
    
    protected NetClient(Context context){
    	mContext = context;
    }

    
	public void newRequest(RequestMethod method, String url,ArrayList<NameValuePair> params, ArrayList<NameValuePair> headers) {
		mMethod = method;
		mUrl = url;
		mParams = params;
		mHeaders = headers;
		mOnComplete = null;
	}
    
	public void newRequest(RequestMethod method, String url,ArrayList<NameValuePair> params, ArrayList<NameValuePair> headers,OnGetUrlCompleteListener onComplete) {
		newRequest(method, url, params, headers);
		mOnComplete = onComplete;
	}
    
	
	
	public String execute() throws NetClientException {
		if(mUrl == null){
			throw new NetClientException("Url is empty");
		}
		if(mOnComplete != null){
			getUrlContent(mMethod, mUrl, mParams, mHeaders, mOnComplete);
			return null;
		}else{
			return getUrlContent(mMethod, mUrl, mParams, mHeaders);
		}
	}
	
	
	public void addParam(String name, String value) {
		if(this.mParams == null){
			this.mParams = new ArrayList<NameValuePair>();
		}
		this.mParams.add(new BasicNameValuePair(name,value));
	}

	public void addHeader(String name, String value) {
		if(this.mHeaders == null){
			this.mHeaders = new ArrayList<NameValuePair>();
		}
		this.mHeaders.add(new BasicNameValuePair(name,value));
	}
	
	/**
     * Build and return a user-agent string that can identify this application
     * to remote servers. Contains the package name and version code.
     */
    protected String buildUserAgent() {
        try {
            final PackageManager manager = mContext.getPackageManager();
            final PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);

            // Some APIs require "(gzip)" in the user-agent string.
            return info.packageName + "/" + info.versionName
                    + " (" + info.versionCode + ") (gzip)";
        } catch (NameNotFoundException e) {
            return null;
        }
    }
    
	public void addHttpBasicAuthCredentials(String user, String pass) {
		credentials = new UsernamePasswordCredentials(user, pass);
	}


}

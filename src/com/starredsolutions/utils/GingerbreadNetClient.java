/**
 * 
 */
package com.starredsolutions.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.starredsolutions.assemblandroid.Constants;
import com.starredsolutions.utils.base.INetClient;
import com.starredsolutions.utils.base.INetClient.NetClientException;
import com.starredsolutions.utils.base.INetClient.OnGetUrlCompleteListener;
import com.starredsolutions.utils.base.INetClient.RequestMethod;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class GingerbreadNetClient implements INetClient {
	private static final String TAG = "GingerbreadNetClient";
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE) || Constants.DEVELOPER_MODE;
	private Context mContext;
	private OnGetUrlCompleteListener mOnComplete = null;
	private ArrayList<NameValuePair> mHeaders = null;
	private ArrayList<NameValuePair> mParams = null;
	
	private HttpURLConnection con = null;
	
	private RequestMethod mMethod;
	private String mUrl = null;
	private UsernamePasswordCredentials credentials;
	
	private static final int SECOND_IN_MILLIS = (int) DateUtils.SECOND_IN_MILLIS;
	
	/**
     * Shared buffer used by {@link #getUrlContent(String)} when reading results
     * from an API request.
     */
    private static byte[] sBuffer = new byte[512];
	
	public GingerbreadNetClient(Context context){
		mContext = context;
		newRequest(null, null, null, null,null);
	}

	/* (non-Javadoc)
	 * @see com.tangelo.proverbio.utils.base.INetClient#newRequest(com.tangelo.proverbio.utils.base.INetClient.RequestMethod, java.lang.String, java.util.ArrayList, java.util.ArrayList)
	 */
	
	public void newRequest(RequestMethod method, String url,
			ArrayList<NameValuePair> params, ArrayList<NameValuePair> headers) {
		mMethod = method;
		mUrl = url;
		mParams = params;
		mHeaders = headers;
		mOnComplete = null;
	}

	/* (non-Javadoc)
	 * @see com.tangelo.proverbio.utils.base.INetClient#newRequest(com.tangelo.proverbio.utils.base.INetClient.RequestMethod, java.lang.String, java.util.ArrayList, java.util.ArrayList, com.tangelo.proverbio.utils.base.INetClient.OnGetUrlCompleteListener)
	 */
	
	public void newRequest(RequestMethod method, String url,
			ArrayList<NameValuePair> params, ArrayList<NameValuePair> headers,
			OnGetUrlCompleteListener onComplete) {
		newRequest(method, url, params, headers);
		mOnComplete = onComplete;

	}

	/* (non-Javadoc)
	 * @see com.tangelo.proverbio.utils.base.INetClient#addParam(java.lang.String, java.lang.String)
	 */
	
	public void addParam(String name, String value) {
		if(this.mParams == null){
			this.mParams = new ArrayList<NameValuePair>();
		}
		this.mParams.add(new BasicNameValuePair(name,value));
	}

	/* (non-Javadoc)
	 * @see com.tangelo.proverbio.utils.base.INetClient#addHeader(java.lang.String, java.lang.String)
	 */
	
	public void addHeader(String name, String value) {
		if(this.mHeaders == null){
			this.mHeaders = new ArrayList<NameValuePair>();
		}
		this.mHeaders.add(new BasicNameValuePair(name,value));

	}

	private synchronized GetUrlResponse getRawUrlContent(RequestMethod method, String url,
			ArrayList<NameValuePair> params, ArrayList<NameValuePair> headers) throws NetClientException{
		
		String responseText = null;
		int status = 0;
		try {
			
			String combinedParams = "";
			if ((params != null) && !params.isEmpty()) {
				for(NameValuePair p : params) {
					String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(),"UTF-8");
					
					if(combinedParams.length() > 1) {
						combinedParams +=  "&" + paramString;
					} else {
						combinedParams += paramString;
					}
				}
			}
			
			if(method == RequestMethod.GET){
	    		url +="?" + combinedParams;
			}
			
			URL connUrl = new URL(url);
			con = (HttpURLConnection) connUrl.openConnection();
			con.setConnectTimeout(20 * SECOND_IN_MILLIS);
			con.setReadTimeout(20 * SECOND_IN_MILLIS);
			con.setRequestProperty("User-Agent", buildUserAgent());
			
			if(headers != null){
				for(NameValuePair h : headers) {
					con.addRequestProperty(h.getName(), h.getValue());
		        }
			}
			
			
			if (credentials != null) {
				final String username = credentials.getUserName();
				final String password=  credentials.getPassword();
				
				Authenticator.setDefault(new Authenticator(){
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password.toCharArray());
					}
				});
				  
			}

			switch (method) {
			case GET:
				con.setRequestMethod("GET");
				
				break;
			case POST:
				con.setRequestMethod("POST");
				if(!TextUtils.isEmpty(combinedParams)){
					con.setDoOutput(true);
					DataOutputStream out = new DataOutputStream(con.getOutputStream());
					out.writeBytes(combinedParams);
					out.flush();
					
				}
				break;
			case PUT:
				con.setRequestMethod("PUT");
				break;
			case DELETE:
				con.setRequestMethod("DELETE");
				break;

			default:
				break;
			}
			
			
			con.connect();
			
			try{
				if(con.getResponseCode() != HttpURLConnection.HTTP_OK){
					if(LOGV) Log.v(TAG,"Invalid response from server: " +con.getResponseMessage());
					throw new NetClientException("Invalid response from server: " +con.getResponseMessage());
				}
				
				ByteArrayOutputStream content = new ByteArrayOutputStream();

				InputStream inputStream = con.getInputStream();
				// Read response into a buffered stream
				int readBytes = 0;
				while ((readBytes = inputStream.read(sBuffer)) != -1) {
					content.write(sBuffer, 0, readBytes);
				}
				// Return result from buffered stream
				responseText = new String(content.toByteArray());
				
			}finally{
				con.disconnect();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return new GetUrlResponse(responseText, status);
	}
	
	/* (non-Javadoc)
	 * @see com.tangelo.proverbio.utils.base.INetClient#getUrlContent(com.tangelo.proverbio.utils.base.INetClient.RequestMethod, java.lang.String, java.util.ArrayList, java.util.ArrayList)
	 */
	
	public String getUrlContent(RequestMethod method, String url,
			ArrayList<NameValuePair> params, ArrayList<NameValuePair> headers)
					throws NetClientException {
		if(LOGV) Log.v(TAG,"getUrlContent [url " +url+"]");
		try {
			GetUrlResponse response;
			response = getRawUrlContent(method, url, params, headers);
			return response.mResponse;
		} catch (NetClientException e) {
			if(LOGV) Log.v(TAG,e.getMessage());
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.tangelo.proverbio.utils.base.INetClient#getUrlContent(com.tangelo.proverbio.utils.base.INetClient.RequestMethod, java.lang.String, java.util.ArrayList, java.util.ArrayList, com.tangelo.proverbio.utils.base.INetClient.OnGetUrlCompleteListener)
	 */
	
	public void getUrlContent(final RequestMethod method, final String url,
			final ArrayList<NameValuePair> params, final ArrayList<NameValuePair> headers,
			final OnGetUrlCompleteListener onComplete){
		(new Thread(new Runnable() {
			
			
			public void run() {
				try {
					GetUrlResponse response;
					response = getRawUrlContent(method, url, params, headers);
					if(onComplete != null){
						onComplete.OnGetUrlComplete(response.mStatus,response.mResponse);
					}
				} catch (NetClientException e) {
					if(LOGV) Log.v(TAG,e.getMessage());
					if(onComplete != null){
						onComplete.OnGetUrlComplete(0,e.getMessage());
					}
				}
			}
		})).start();

	}

	/**
	 * Execute a request after newRequest
	 */
	
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
	
	/**
     * Build and return a user-agent string that can identify this application
     * to remote servers. Contains the package name and version code.
     */
    private String buildUserAgent() {
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
	
	private class GetUrlResponse{
		public String mResponse;
		public int mStatus;
		public GetUrlResponse(String response,int status){
			mResponse = response;
			mStatus = status;
		}
	}

}

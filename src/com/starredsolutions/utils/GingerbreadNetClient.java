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

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class GingerbreadNetClient extends NetClient {
	
	private HttpURLConnection con = null;
	
	
	public GingerbreadNetClient(Context context){
		super(context);
		newRequest(null, null, null, null,null);
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

	
	
	private class GetUrlResponse{
		public String mResponse;
		public int mStatus;
		public GetUrlResponse(String response,int status){
			mResponse = response;
			mStatus = status;
		}
	}

}

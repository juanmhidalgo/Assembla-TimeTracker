/**
 * 
 */
package com.starredsolutions.utils.base;

import java.util.ArrayList;

import org.apache.http.NameValuePair;

/**
 * @author Juan M. Hidalgo <juanmanuel@itangelo.com>
 *
 */
public interface INetClient {
	public static enum RequestMethod{
		GET,
		POST,
		PUT,
		DELETE;
	}
	
	public static interface OnGetUrlCompleteListener{
		public void OnGetUrlComplete(int status,String result);
	}

	/**
     * Thrown when there were problems contacting the remote API server, either
     * because of a network error, or the server returned a bad status code.
     */
	public class NetClientException extends Exception{
		private static final long serialVersionUID = 1L;

		public NetClientException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public NetClientException(String detailMessage) {
            super(detailMessage);
        }
	}
	
	/**
	 * 
	 * @param user
	 * @param pass
	 */
	public void addHttpBasicAuthCredentials(String user, String pass);
	
	/**
	 * 
	 * @param method
	 * @param url
	 * @param params
	 * @param headers
	 */
	public void newRequest(RequestMethod method,String url,ArrayList <NameValuePair> params,ArrayList <NameValuePair> headers);
	/**
	 * 
	 * @param method
	 * @param url
	 * @param params
	 * @param headers
	 * @param onComplete
	 */
	public void newRequest(RequestMethod method,String url,ArrayList <NameValuePair> params,ArrayList <NameValuePair> headers,OnGetUrlCompleteListener onComplete);
	/**
	 * 
	 * @return
	 * @throws NetClientException
	 */
	public String execute() throws NetClientException;
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addParam(String name, String value);
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addHeader(String name, String value);
	/**
	 * Get Url Content
	 * @param method
	 * @param url
	 * @param params
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public String getUrlContent(RequestMethod method,String url,ArrayList <NameValuePair> params,ArrayList <NameValuePair> headers) throws NetClientException;
	/**
	 *  Get Url Content Async Method
	 * @param method
	 * @param url
	 * @param params
	 * @param headers
	 * @param onComplete
	 * @throws NetClientException 
	 */
	public void getUrlContent(RequestMethod method,String url,ArrayList <NameValuePair> params,ArrayList <NameValuePair> headers,OnGetUrlCompleteListener onComplete);
}

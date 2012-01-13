package com.starredsolutions.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.util.Log;

import com.starredsolutions.assemblandroid.TimeTrackerApplication;
import com.starredsolutions.utils.Utils;


/**
 * http://lukencode.com/2010/04/27/calling-web-services-in-android-using-httpclient/
 * 
 * @author David Lauzon (HTTP Basic Authentication + PUT/DELETE requests + XML content)
 * @date   2011/05/12
 * @author Luke Lowrey (wrote the initial RESTClient class)
 * @date   2010/04/27
 */
public class RestfulClient {
	public boolean log = true;
	public boolean verbose = false;
	
	
	private final Context context;
    private String url;
    
    private RequestMethod method;
    
	private ArrayList <NameValuePair> params;
    private ArrayList <NameValuePair> headers;

	private HttpClient client;
 
    // Only HTTP Basic authentication is supported for now 
    private Credentials credentials = null;

    private String messageBody = null;
    
    private int    statusCode;
    private String responseBody;
    private String statusPhrase;
 
    
    public int    getStatusCode() {    return statusCode; }
    public String getStatusPhrase() {  return statusPhrase; }
    public String getResponse() {      return responseBody; }

    
    public RestfulClient(RequestMethod method, String url) {
    	this.context = TimeTrackerApplication.getInstance().getApplicationContext();
    	newRequest(method, url);
    }
    
    public void newRequest(RequestMethod method, String url) {
        this.url = url;
        this.method = method;
        
        this.params = new ArrayList<NameValuePair>();
        this.headers = new ArrayList<NameValuePair>();
        
        //this.client = new DefaultHttpClient();
        this.client = Utils.getHttpClient(this.context);
        
        this.statusCode = 0;
        this.statusPhrase = this.responseBody = this.messageBody = "";
        this.credentials = null;
    }
 
    public void addParam(String name, String value) {
        params.add(new BasicNameValuePair(name, value));
    }
 
    public void addHeader(String name, String value) {
        headers.add(new BasicNameValuePair(name, value));
    }
    
    
    /**
     * Use **either** setBody() or AddParam()
     * 
     * @param data
     */
    public void setMessageBody(String data) {
    	this.messageBody = data;
    }
    
    /**
     * Adds HTTP Basic Authentication functionality
     * @author www.davidlauzon.net
     * @see    http://hc.apache.org/httpcomponents-client-ga/tutorial/html/authentication.html
     */
    public void addHttpBasicAuthCredentials(String user, String pass) {
    	// WARNING: request may be sent twice (first without auth, second with aut), see :
    	// http://dlinsin.blogspot.com/2009/08/http-basic-authentication-with-android.html
    	credentials = new UsernamePasswordCredentials(user, pass);
    }
    
    
    public void execute() throws RestfulException
    {
    	// Build appropriate request class for requested method
    	// Parameters will be automatically appended to URL for GET methods 
    	log("*** " + method + " " + url);
    	HttpRequestBase request;
    	
		try {
			request = buildRequest(method, url, params);
			
			// Add headers
	    	for(NameValuePair h : headers) {
	            request.addHeader(h.getName(), h.getValue());
	        }
	    	
	    	// Add parameters in the request body (only for POST & PUT) requests
        	//if ( (method == RequestMethod.POST || method == RequestMethod.PUT) && (!params.isEmpty())) {
        	if (method == RequestMethod.POST || method == RequestMethod.PUT) {
        		if (messageBody != null) {
        			((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(messageBody, HTTP.UTF_8));
    				verbose("Request Body: " + messageBody);
    				
        		} else if (!params.isEmpty()) {
        			// DEFAULT : Send params using an URL Encoded Form
        			((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
    				verbose("Request Body: " + params.toString());
        		}
        	}
        		
		} catch (UnsupportedEncodingException e) {
			throw new RestfulException("Error while building the request", e);
		}
    	

		// Execute the request
    	try {
			executeRequest(request);
		} catch (ClientProtocolException e) {
			throw new RestfulException("Error while executing the request", e);
		} catch (IOException e) {
			throw new RestfulException("Error while executing the request", e);
		}
    }
    
 
    private void executeRequest(HttpUriRequest request) 
    			throws ClientProtocolException, IOException
    {
        
        HttpResponse httpResponse;
        
        
        // Adds HTTP Basic Credentials if any
        if (credentials != null) {
        	URI uri = request.getURI();
	        
	        AuthScope scope = new AuthScope(uri.getHost(), uri.getPort());
	        //AuthScope scope = new AuthScope(uri.getHost(), uri.getPort(), AuthScope.ANY_SCHEME);
	        ((DefaultHttpClient)  client).getCredentialsProvider().setCredentials(scope, credentials);
        }
 
        try {
        	httpResponse = client.execute(request);
            
            
            statusCode = httpResponse.getStatusLine().getStatusCode();
            statusPhrase = httpResponse.getStatusLine().getReasonPhrase();
 
            HttpEntity entity = httpResponse.getEntity();
 
            if (entity != null) {
                InputStream instream = entity.getContent();
                responseBody = convertStreamToString(instream);
 
                // Closing the input stream will trigger connection release
                instream.close();
            } else {
            	responseBody = "";
            }
        	log("* HTTP Response Status: " + Integer.toString(statusCode) + " " + statusPhrase);
        	verbose("Response: " + responseBody);
 
        } catch (ClientProtocolException e)  {
            client.getConnectionManager().shutdown();
            throw e;
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
            throw e;
        }
    }
    
    
    static private HttpRequestBase buildRequest(RequestMethod method, String url, ArrayList <NameValuePair> params)
    		throws UnsupportedEncodingException
    {
    	switch(method) {
        	case GET:
         		// Add parameters in the URL
                String combinedParams = "";
                if (!params.isEmpty()) {
                    combinedParams += "?";
                    for(NameValuePair p : params) {
						String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(),"UTF-8");

                        if(combinedParams.length() > 1) {
                            combinedParams +=  "&" + paramString;
                        } else {
                            combinedParams += paramString;
                        }
                    }
                }
         		return new HttpGet(url + combinedParams);
         	
         	case POST:   return new HttpPost(url);
         	
         	case PUT:    return new HttpPut(url);
         	
         	case DELETE: return new HttpDelete(url);
         	
         	default:     return null;		// Just to make Java happy (this path will never be explored)
    	}
    }
    
    /**
     * To use the library in standard Java (non-Android) project,
     * just uncomment the System.out line.
     * @param message
     */
    private void log(String message) {
    	if (log) {
	    	// Uncomment line below if building an Android project 
	    	Log.i("RESTClient", message);
	    	// Uncomment line below if building a standard (non-Android) Java Project
	    	//System.out.println("[RESTClient] " + message);
    	}
    }
    
    private void verbose(String message) {
    	if (verbose)
    		log(message);
    }
    
 
    /**
     * Will trigger exception if received non 200 OK responses ? (ie: 304 Not Modified)
     * 
     * @param InputStream is
     * @return String
     * @throws IOException 
     */
    static private String convertStreamToString(InputStream is) throws IOException {
 
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        is.close();
        
        return sb.toString();
    }
}

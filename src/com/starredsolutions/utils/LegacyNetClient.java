/**
 * 
 */
package com.starredsolutions.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.util.Log;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class LegacyNetClient extends NetClient {
	private  HttpClient mClient;
	
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";
    private static final int HTTP_STATUS_OK = 200;
    
    

    /**
     * 
     * @param context
     */
    public LegacyNetClient(Context context){
    	super(context);
    	newRequest(null, null, null, null,null);
    }
    
    
	@Override
	public void newRequest(RequestMethod method, String url,ArrayList<NameValuePair> params, ArrayList<NameValuePair> headers) {
		super.newRequest(method, url, params, headers);
		mClient = getHttpClient(mContext);
	}

	
	
	private synchronized GetUrlResponse getRawUrlContent(RequestMethod method, String url,
			ArrayList<NameValuePair> params, ArrayList<NameValuePair> headers) throws NetClientException{
		
		String responseText = null;
		StatusLine status = null;
		HttpRequestBase request = null;
		InputStream inputStream = null;
		try {
			request = buildRequest(method, url, (params != null) ? params : mParams ,(headers != null) ? headers : mHeaders);
			HttpResponse response = mClient.execute(request);
			status = response.getStatusLine();
			if (status.getStatusCode()  != HTTP_STATUS_OK) {
				if(LOGV) Log.v(TAG,"Invalid response from server: " +status.toString());
				throw new NetClientException("Invalid response from server: " +status.toString());
			}
			// Pull content stream from response
			HttpEntity entity = response.getEntity();
			inputStream = entity.getContent();

			ByteArrayOutputStream content = new ByteArrayOutputStream();

			// Read response into a buffered stream
			int readBytes = 0;
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}
			// Return result from buffered stream
			responseText = new String(content.toByteArray());
			inputStream.close();
			
		} catch (ClientProtocolException e) {
			if(LOGV) Log.v(TAG,e.getMessage());
			throw new NetClientException("ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			if(LOGV) Log.v(TAG,e.getMessage());
			throw new NetClientException("IOException: " + e.getMessage());
		}
		
		return new GetUrlResponse(responseText, status);
		
	}

	public String getUrlContent(RequestMethod method, String url,
			ArrayList<NameValuePair> params, ArrayList<NameValuePair> headers)
				throws NetClientException {
		if(LOGV) Log.v(TAG,"getUrlContent [url " +url+"]");
		GetUrlResponse response;
		response = getRawUrlContent(method, url, params, headers);
		return response.mResponse;
	}

	public void getUrlContent(final RequestMethod method, final String url,
			final ArrayList<NameValuePair> params, final ArrayList<NameValuePair> headers,
			final OnGetUrlCompleteListener onComplete){
		
		(new Thread(new Runnable() {
			
			public void run() {
				try {
					GetUrlResponse response;
					response = getRawUrlContent(method, url, params, headers);
					if(onComplete != null){
						onComplete.OnGetUrlComplete(response.mStatus.getStatusCode(),response.mResponse);
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
     * Simple {@link HttpEntityWrapper} that inflates the wrapped
     * {@link HttpEntity} by passing it through {@link GZIPInputStream}.
     */
    private static class InflatingEntity extends HttpEntityWrapper {
        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        @Override
        public InputStream getContent() throws IOException {
            return new GZIPInputStream(wrappedEntity.getContent());
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }
    
    /**
     * Generate and return a {@link HttpClient} configured for general use,
     * including setting an application-specific user-agent string.
     */
    public HttpClient getHttpClient(Context context) {
        final HttpParams params = new BasicHttpParams();

        // Use generous timeouts for slow mobile networks
        HttpConnectionParams.setConnectionTimeout(params, 20 * SECOND_IN_MILLIS);
        HttpConnectionParams.setSoTimeout(params, 20 * SECOND_IN_MILLIS);

        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpProtocolParams.setUserAgent(params, buildUserAgent());

        final DefaultHttpClient client = new DefaultHttpClient(params);

        client.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(HttpRequest request, HttpContext context) {
                // Add header to accept gzip content
                if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                    request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                }
            }
        });

        client.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(HttpResponse response, HttpContext context) {
                // Inflate any responses compressed with gzip
                final HttpEntity entity = response.getEntity();
                final Header encoding = entity.getContentEncoding();
                if (encoding != null) {
                    for (HeaderElement element : encoding.getElements()) {
                        if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                            response.setEntity(new InflatingEntity(response.getEntity()));
                            break;
                        }
                    }
                }
            }
        });

        return client;
    }
    
    /**
     * 
     * @param method
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws UnsupportedEncodingException
     */
    private HttpRequestBase buildRequest(RequestMethod method, String url, 
    		ArrayList <NameValuePair> params,ArrayList <NameValuePair> headers)
    		throws UnsupportedEncodingException {
    	
    	HttpRequestBase request = null;
    	switch(method) {
	    	case GET:
	    		// Add parameters in the URL
	    		String combinedParams = "";
	    		if ((params != null) && !params.isEmpty()) {
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
	    		request = new HttpGet(url + combinedParams);
	    		break;
	
	    	case POST:
	    		request = new HttpPost(url);
	    		if ((params != null) && !params.isEmpty()) {
	    			((HttpPost) request).setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
	    		}
	    		break;
	    	case PUT:
	    		request = new HttpPut(url);
	    		((HttpPut) request).setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
	    		break;
	    	case DELETE:
	    		request = new HttpDelete(url);
	    		break;
	    	default:     return null;		// Just to make Java happy (this path will never be explored)
    	}
    	
    	if(headers != null){
	    	for(NameValuePair h : headers) {
	            request.addHeader(h.getName(), h.getValue());
	        }
    	}
    	
    	if (credentials != null) {
        	URI uri = request.getURI();
	        
	        AuthScope scope = new AuthScope(uri.getHost(), uri.getPort());
	        //AuthScope scope = new AuthScope(uri.getHost(), uri.getPort(), AuthScope.ANY_SCHEME);
	        ((DefaultHttpClient)  mClient).getCredentialsProvider().setCredentials(scope, credentials);
        }
    	
    	return request;
    }

	
	private class GetUrlResponse{
		public String mResponse;
		public StatusLine mStatus;
		public GetUrlResponse(String response,StatusLine status){
			mResponse = response;
			mStatus = status;
		}
	}

}

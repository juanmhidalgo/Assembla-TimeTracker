/**
 * 
 */
package com.starredsolutions.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
/**
 * @author juan
 *
 */
public class Utils {
	private static final int SECOND_IN_MILLIS = (int) DateUtils.SECOND_IN_MILLIS;
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";
    private static int SYNC_ACTIVITY_ID = 555; 
    
    /**
     * 
     * @param ctx
     * @return
     */
    public static boolean isGPSEnabled(Context ctx){
    	LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		return locationManager .isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    
    /**
     * 
     * @param ctx
     * @param subject
     * @param text
     */
    public static void share(final Context ctx, String subject,String text) {
		final Intent intent = new Intent(Intent.ACTION_SEND);

		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, text);

		ctx.startActivity(Intent.createChooser(intent, "Compartir..."));
	}
    
	public static boolean checkInternet(Context activity) {
		try {
			ConnectivityManager conMgr = (ConnectivityManager) activity.getSystemService(Activity.CONNECTIVITY_SERVICE);
			NetworkInfo netMob = conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo netWifi = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			return (((netMob != null) && netMob.isConnected()) || ((netWifi != null) && netWifi.isConnected()));
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("_checkInternet", e.getMessage());
			return false;
		}
	}
	
	/**
	 * 
	 * @param pUrl
	 * @param params
	 * @return
	 */
	public static String httpGet(String pUrl){
		StringBuffer result = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(pUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
			result = new StringBuffer(Utils.readStream(urlConnection.getInputStream()));
		} catch (MalformedURLException e) {
			Log.e("Utils::httpGet",e.getMessage());
		} catch (IOException e) {
			Log.e("Utils::httpGet",e.getMessage());
		}finally{
			urlConnection.disconnect();
		}
		return (result != null) ? result.toString() : null;
	}

	
	public static interface OnFetchFileCompleteListener {
        public void onFetchFileComplete(File result);
    }
	

	/**
	 * 
	 * @param context
	 * @param name
	 * @return
	 */
	public static boolean fetchedFileExists(final Context context,final String name){
		 File resultFile = null;
         try{
         	/*if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                 resultFile = new File(
                         Environment.getExternalStorageDirectory()
                                 + File.separator + "Android"
                                 + File.separator + "data"
                                 + File.separator + context.getPackageName()
                                 + File.separator + "downloads"
                                 + File.separator + name);
             }*/
        	 resultFile = new File(context.getCacheDir(),name);
         }catch(Exception e){
         }
         return resultFile != null && resultFile.exists();
	}
	
	/**
	 * 
	 * @param context
	 * @param name
	 * @return
	 */
	public static File getFetchedFile(final Context context,final String name){
		File resultFile = null;
		try{
			/*if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				resultFile = new File(
						Environment.getExternalStorageDirectory()
						+ File.separator + "Android"
						+ File.separator + "data"
						+ File.separator + context.getPackageName()
						+ File.separator + "downloads"
						+ File.separator + name);
			}*/
			resultFile = new File(context.getCacheDir(),name);
		}catch(Exception e){
		}
		
		if(resultFile != null && resultFile.exists()){
			return resultFile;
		}else{
			return null;
		}
	}
	
	/**
	 * Fetch File from url and save on downloads with name "saveAs"
	 * @param context
	 * @param url
	 * @param saveAs
	 * @param callback
	 */
	public static void fetchFile(final Context context, final String url,final String saveAs,final OnFetchFileCompleteListener callback){
		new AsyncTask<String, Void, File>() {
			protected File doInBackground(String... params) {
				final String url = params[0];
				final String fname = params[1];
				
                if (TextUtils.isEmpty(url)) {
                    return null;
                }
				
                if(TextUtils.isEmpty(saveAs)){
                	return null;
                }
                
                
                File resultFile = null;
                try{
                	/*if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                        resultFile = new File(
                                Environment.getExternalStorageDirectory()
                                        + File.separator + "Android"
                                        + File.separator + "data"
                                        + File.separator + context.getPackageName()
                                        + File.separator + "downloads"
                                        + File.separator + fname);
                    }*/
                	resultFile = new File(context.getCacheDir(),fname);
                }catch(Exception e){
                	
                }
                
                
                try {
                	// TODO: check for HTTP caching headers
                	final HttpClient httpClient = Utils.getHttpClient(context.getApplicationContext());
                	final HttpResponse resp = httpClient.execute(new HttpGet(url));
                	final HttpEntity entity = resp.getEntity();

                	final int statusCode = resp.getStatusLine().getStatusCode();
                	if (statusCode != HttpStatus.SC_OK || entity == null) {
                		return null;
                	}

                	final byte[] respBytes = EntityUtils.toByteArray(entity);

                	// Write response bytes to cache.
                	if (resultFile != null) {
                		try {
                			resultFile.getParentFile().mkdirs();
                			resultFile.createNewFile();
                			FileOutputStream fos = new FileOutputStream(resultFile);
                			fos.write(respBytes);
                			fos.close();
                		} catch (FileNotFoundException e) {
                			Log.w("fetchFile", "Error writing to bitmap cache: " + resultFile.toString(), e);
                		} catch (IOException e) {
                			Log.w("fetchFile", "Error writing to bitmap cache: " + resultFile.toString(), e);
                		}
                	}
                	return resultFile;
                } catch (Exception e) {
                	Log.w("fetchFile", "Problem while loading file: " + e.toString(), e);
                }
                return null;
			}

	        @Override
            protected void onPostExecute(File result) {
	        	if(callback != null){
	        		callback.onFetchFileComplete(result);
	        	}
            }
		}.execute(url,saveAs);
	}

	
	/**
	 * 
	 * @param in
	 * @return
	 */
	public static String readStream(InputStream in){
		BufferedInputStream bufferedInput = new BufferedInputStream(in, 8192);
		ByteArrayBuffer byteArray = new ByteArrayBuffer(50);
		int current = 0;
		try {
			while ((current = bufferedInput.read()) != -1) {
				byteArray.append((byte) current);
			}
		} catch (IOException e) {
			Log.e("Utils::readStream",e.getMessage());
		}
		return new String(byteArray.toByteArray());
	}

	/**
	 * 
	 * @param activity
	 * @return
	 */
	public static boolean firstRun(Context activity){
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
		boolean firstRun = p.getBoolean("FirstRun", true);
		p.edit().putBoolean("FirstRun", false).commit();
		return firstRun;
	}
	
	/**
	 * 
	 * @param activity
	 * @return
	 */
	public static long getSharedPreference(Context activity,String name,long newValue,long defaultValue){
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
		Long result = p.getLong(name, defaultValue);
		p.edit().putLong(name, newValue).commit();
		return result;
	}
	
	/**
	 * 
	 * @param context
	 * @param name
	 * @param value
	 */
	public static void setSharedPreference(final Context context,final String name,final String value) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                sp.edit().putString(name, value).commit();
                return null;
            }
        }.execute();
    }
	
	/**
	 * 
	 * @param context
	 * @param name
	 * @param value
	 */
	public static void setSharedPreference(final Context context,final String name,final long value) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... voids) {
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
				sp.edit().putLong(name, value).commit();
				return null;
			}
		}.execute();
	}
	
	/**
	 * 
	 * @param context
	 * @param name
	 * @param value
	 */
	public static void setSharedPreference(final Context context,final String name,final boolean value) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... voids) {
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
				sp.edit().putBoolean(name, value).commit();
				return null;
			}
		}.execute();
	}
	
	/**
	 * 
	 * @param activity
	 * @return
	 */
	public static long getSharedPreference(Context activity,String name){
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
		Long result = p.getLong(name, 0);
		return result;
	}
	
	/**
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static long getDiffDays(Date date1, Date date2){
		if(date2 !=null){
			return ((date2.getTime() - date1.getTime())/(1000*60*60*24));
		}else{
			return ((Calendar.getInstance(TimeZone.getDefault()).getTime().getTime() - date1.getTime())/(1000*60*60*24));
		}
	}
	
	/**
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static long getDiffHours(Date date1, Date date2){
		if(date2 !=null){
			return ((date2.getTime() - date1.getTime())/(1000*60*60));
		}else{
			return ((Calendar.getInstance(TimeZone.getDefault()).getTime().getTime() - date1.getTime())/(1000*60*60));
		}
	}
	
	/**
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static long getDiffMinutes(Date date1, Date date2){
		if(date2 !=null){
			return ((date2.getTime() - date1.getTime())/(1000*60));
		}else{
			return ((Calendar.getInstance(TimeZone.getDefault()).getTime().getTime() - date1.getTime())/(1000*60));
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static Date getCurrentDateTime(){
		return Calendar.getInstance(TimeZone.getDefault()).getTime();
	}
	
	
	/**
     * Generate and return a {@link HttpClient} configured for general use,
     * including setting an application-specific user-agent string.
     */
    public static HttpClient getHttpClient(Context context) {
        final HttpParams params = new BasicHttpParams();

        // Use generous timeouts for slow mobile networks
        HttpConnectionParams.setConnectionTimeout(params, 20 * SECOND_IN_MILLIS);
        HttpConnectionParams.setSoTimeout(params, 20 * SECOND_IN_MILLIS);

        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpProtocolParams.setUserAgent(params, buildUserAgent(context));

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
     * Build and return a user-agent string that can identify this application
     * to remote servers. Contains the package name and version code.
     */
    private static String buildUserAgent(Context context) {
        try {
            final PackageManager manager = context.getPackageManager();
            final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);

            // Some APIs require "(gzip)" in the user-agent string.
            return info.packageName + "/" + info.versionName
                    + " (" + info.versionCode + ") (gzip)";
        } catch (NameNotFoundException e) {
            return null;
        }
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
    
    public static void setSyncIndicator(final Context ctx, boolean state, String title,String text){
    	NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

    	if(state == false){
    		nm.cancel(SYNC_ACTIVITY_ID);
    		return;
    	}
    	
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n = new Notification(android.R.drawable.stat_notify_sync, null, System.currentTimeMillis());
        n.setLatestEventInfo(ctx, title, text, contentIntent);
        n.flags |= Notification.FLAG_ONGOING_EVENT;
        n.flags |= Notification.FLAG_NO_CLEAR;
        nm.notify(SYNC_ACTIVITY_ID, n);
    }

    
    public static float microdegreeToFloat(int microDegree){
    	return microDegree / 1000000F;
    }
    
    
    /**
     * 
     * @param array
     * @param value
     * @return
     */
    public static String[] addStringToArray(String[] array,String value){
    	if(array != null){
	    	List<String> list = new ArrayList<String>();
	    	Collections.addAll(list, array);
	    	list.add(value);
	    	return list.toArray(new String[0]);
    	}else{
    		return new String[]{value};
    	}
    }
    
    	
}

/**
 * 
 */
package com.starredsolutions.assemblandroid.provider;

import java.util.Arrays;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.starredsolutions.assemblandroid.Constants;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Spaces;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tasks;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;
import com.starredsolutions.assemblandroid.provider.AssemblaDatabase.Tables;
import com.starredsolutions.utils.Utils;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class AssemblaProvider extends ContentProvider{
	private static final String TAG = "AssemblaProvider";
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE) || Constants.DEVELOPER_MODE;
	
	private AssemblaDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    
    
    
    private static final int SPACES = 300;
    private static final int SPACES_ID = 301;
    private static final int TICKETS = 400;
    private static final int TICKETS_ID = 401;
    private static final int TICKETS_BY_SPACE = 402;
    private static final int TICKETS_BY_SPACE_AND_NUMBER = 403;
    
    private static final int TASKS = 500;
    private static final int TASKS_ID = 501;
    private static final int TASKS_BY_SPACE = 502;
    private static final int TASKS_BY_TICKET = 503;
    
    private static UriMatcher buildUriMatcher() {
    	final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AssemblaContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, AssemblaContract.PATH_SPACES, SPACES);
        matcher.addURI(authority, AssemblaContract.PATH_SPACES +"/#", SPACES_ID);
        matcher.addURI(authority, AssemblaContract.PATH_TICKETS, TICKETS);
        matcher.addURI(authority, AssemblaContract.PATH_TICKETS +"/#", TICKETS_ID);
        matcher.addURI(authority, AssemblaContract.PATH_TICKETS +"/space/#", TICKETS_BY_SPACE);
        matcher.addURI(authority, AssemblaContract.PATH_TICKETS +"/number/*/#", TICKETS_BY_SPACE_AND_NUMBER);
        
        matcher.addURI(authority, AssemblaContract.PATH_TASKS, TASKS);
        matcher.addURI(authority, AssemblaContract.PATH_TASKS +"/#", TASKS_ID);
        matcher.addURI(authority, AssemblaContract.PATH_TASKS +"/space/#", TASKS_BY_SPACE);
        matcher.addURI(authority, AssemblaContract.PATH_TASKS +"/ticket/#", TASKS_BY_TICKET);
        
        return matcher;
    }
    
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		Log.d(TAG, "gettype(uri=" + uri + ")");
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case SPACES:
				return Spaces.CONTENT_TYPE;
			case SPACES_ID:
				return Spaces.CONTENT_ITEM_TYPE;
			case TICKETS:
				return Tickets.CONTENT_TYPE;
			case TICKETS_ID:
				return Tickets.CONTENT_ITEM_TYPE;
			case TICKETS_BY_SPACE:
				return Tickets.CONTENT_TYPE;
			case TICKETS_BY_SPACE_AND_NUMBER:
				return Tickets.CONTENT_ITEM_TYPE;
			case TASKS:
				return Tasks.CONTENT_TYPE;
			case TASKS_ID:
				return Tasks.CONTENT_ITEM_TYPE;
			case TASKS_BY_SPACE:
				return Tasks.CONTENT_TYPE;
			case TASKS_BY_TICKET:
				return Tasks.CONTENT_TYPE;

			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (LOGV) Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        
        switch (match) {
        	case SPACES:
        		db.insertOrThrow(Tables.SPACES, null, values);
        		getContext().getContentResolver().notifyChange(uri, null);
        		return Spaces.CONTENT_URI;
        	case TICKETS:
        		db.insertOrThrow(Tables.TICKETS, null, values);
        		getContext().getContentResolver().notifyChange(uri, null);
        		return Spaces.CONTENT_URI;
        	case TASKS:
        		db.insertOrThrow(Tables.TASKS, null, values);
        		getContext().getContentResolver().notifyChange(uri, null);
        		return Spaces.CONTENT_URI;
        	default:
    			throw new UnsupportedOperationException("Unknown uri: " + uri);
        
        }
	}

	@Override
	public boolean onCreate() {
		final Context context = getContext();
		mOpenHelper = new AssemblaDatabase(context);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (LOGV) Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
		
		final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		Cursor c = null;
		
		final int match = sUriMatcher.match(uri);
		
		switch (match) {
			case SPACES:
			case SPACES_ID:
				qb.setTables(Tables.SPACES);
				break;
			case TICKETS:
				qb.setTables(Tables.TICKETS);
				break;
			case TICKETS_ID:
				Log.v(TAG,Tickets._ID + " = " + uri.getPathSegments().get(1));
				qb.setTables(Tables.TICKETS);
				qb.appendWhere(Tickets._ID + " = " + uri.getPathSegments().get(1));
				break;
			case TICKETS_BY_SPACE:
				qb.setTables(Tables.TICKETS);
				break;
			case TICKETS_BY_SPACE_AND_NUMBER:
				qb.setTables(Tables.TICKETS);
				break;
			case TASKS:
			case TASKS_BY_SPACE:
			case TASKS_BY_TICKET:
				qb.setTables(Tables.TASKS);
				break;
		}
		
		c = qb.query(db, projection, selection, selectionArgs, null, null, null);
		
		if(c != null){
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if (LOGV) Log.v(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
		int count = 0;
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
	        case SPACES:
	        	count = db.update(Tables.SPACES, values, selection, selectionArgs);
	        	break;
	        case TICKETS:
	        	count = db.update(Tables.TICKETS, values, selection, selectionArgs);
	        	break;
	        case TICKETS_BY_SPACE_AND_NUMBER:
	        	List<String> pathSegments = uri.getPathSegments();
	        	String spaceId = pathSegments.get(2);
	        	String ticketNumber = pathSegments.get(3);
	        	
	        	if(!TextUtils.isEmpty(selection)){
	        		selection += " AND " + Tickets.SPACE_ID + " =? AND " + Tickets.NUMBER + "=? ";
	        		selectionArgs = Utils.addStringToArray(selectionArgs, spaceId);
	        		selectionArgs = Utils.addStringToArray(selectionArgs, ticketNumber);
	        	}else{
	        		selection = Tickets.SPACE_ID + " =? AND " + Tickets.NUMBER + "=? ";
	        		selectionArgs = new String[]{spaceId,ticketNumber};
	        	}
	        	count = db.update(Tables.TICKETS, values, selection, selectionArgs);
	        	break;
	        case TASKS:
	        	count = db.update(Tables.TASKS, values, selection, selectionArgs);
	        	break;
	        default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
        getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}

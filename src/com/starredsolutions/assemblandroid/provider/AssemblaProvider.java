/**
 * 
 */
package com.starredsolutions.assemblandroid.provider;

import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.starredsolutions.assemblandroid.provider.AssemblaContract.Spaces;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;
import com.starredsolutions.assemblandroid.provider.AssemblaDatabase.Tables;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class AssemblaProvider extends ContentProvider{
	private static final String TAG = "AssemblaProvider";
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);
	
	private AssemblaDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    
    
    
    private static final int SPACES = 300;
    private static final int SPACES_ID = 301;
    private static final int TICKETS = 400;
    private static final int TICKETS_ID = 401;
    private static final int TICKETS_BY_SPACE = 402;
    
    private static UriMatcher buildUriMatcher() {
    	final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AssemblaContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, AssemblaContract.PATH_SPACES, SPACES);
        matcher.addURI(authority, AssemblaContract.PATH_SPACES +"/#", SPACES_ID);
        matcher.addURI(authority, AssemblaContract.PATH_TICKETS, TICKETS);
        matcher.addURI(authority, AssemblaContract.PATH_TICKETS +"/#", TICKETS_ID);
        matcher.addURI(authority, AssemblaContract.PATH_TICKETS +"/space/#", TICKETS_BY_SPACE);
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
				c = qb.query(db, projection, selection, selectionArgs, null, null, null);
				break;
			case TICKETS:
				qb.setTables(Tables.TICKETS);
				c = qb.query(db, projection, selection, selectionArgs, null, null, null);
				break;
			case TICKETS_BY_SPACE:
				qb.setTables(Tables.TICKETS);
				c = qb.query(db, projection, selection, selectionArgs, null, null, null);
				break;
		}
		
		
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
	        default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
        getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
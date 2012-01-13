/**
 * 
 */
package com.starredsolutions.assemblandroid.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.starredsolutions.assemblandroid.provider.AssemblaContract.SpacesColumns;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.TicketsColumns;

/**
 * @author Juan M. Hidalgo <juan@starredsolutions.com.ar>
 *
 */
public class AssemblaDatabase extends SQLiteOpenHelper {
	private static final String TAG = "AssemblaDatabase";
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);

    private static final String DATABASE_NAME = "assembla.db";
    private static final int DATABASE_VERSION = 3;
    
    interface Tables {
    	String SPACES = "spaces";
    	String TICKETS = "tickets";
    }
    
    public AssemblaDatabase(Context context){
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		if (LOGV) Log.v(TAG, "onCreate");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.SPACES + "( " 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SpacesColumns.SPACE_ID + " TEXT NOT NULL,"
                + SpacesColumns.NAME + " TEXT NOT NULL,"
                + SpacesColumns.DESCRIPTION+ " TEXT NOT NULL,"
                + SpacesColumns.CREATED_AT + " DATE)");
		//TICKET_ID,NUMBER,SPACE_ID,PRIORITY,STATUS,ASSIGNED_TO_ID,SUMMARY,DESCRIPTION,WORKING_HOURS};
		db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.TICKETS + "( " 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ TicketsColumns.TICKET_ID + " INTEGER NOT NULL,"
				+ TicketsColumns.NUMBER + " INTEGER NOT NULL,"
				+ TicketsColumns.SUMMARY + " TEXT NOT NULL,"
				+ TicketsColumns.SPACE_ID + " TEXT NOT NULL,"
				+ TicketsColumns.PRIORITY + " INTEGER NOT NULL,"
				+ TicketsColumns.STATUS + " TEXT NOT NULL,"
				+ TicketsColumns.ASSIGNED_TO_ID+ " INTEGER NOT NULL,"
				+ TicketsColumns.DESCRIPTION+ " TEXT NOT NULL,"
				+ TicketsColumns.WORKING_HOURS+ " REAL NOT NULL)");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (LOGV) Log.v(TAG, "onUpgrade From Version: " + oldVersion + " to " + newVersion);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.TICKETS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.SPACES);
		onCreate(db);
	}
}

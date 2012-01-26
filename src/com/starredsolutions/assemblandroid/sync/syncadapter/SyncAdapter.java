/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.starredsolutions.assemblandroid.sync.syncadapter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.ParseException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.format.DateUtils;
import android.util.Log;

import com.starredsolutions.assemblandroid.AssemblaAPIAdapter;
import com.starredsolutions.assemblandroid.Constants;
import com.starredsolutions.assemblandroid.exceptions.AssemblaAPIException;
import com.starredsolutions.assemblandroid.exceptions.XMLParsingException;
import com.starredsolutions.assemblandroid.models.Space;
import com.starredsolutions.assemblandroid.models.Task;
import com.starredsolutions.assemblandroid.models.Ticket;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Spaces;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tasks;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;
import com.starredsolutions.net.RestfulException;
import com.starredsolutions.utils.SettingsHelper;



//TODO Separate sync on methods, syncSpaces,syncTickets,syncTask
/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE) || Constants.DEVELOPER_MODE;
    
    private final AccountManager mAccountManager;
    private final Context mContext;
    private static ContentProviderClient mProvider;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {
        String authtoken = null;
        
        
        Long lastSync = SettingsHelper.getInstance(getContext()).getLong(Constants.LAST_SYNC_KEY, 0);
        Long nowSync = System.currentTimeMillis();
        
        java.text.SimpleDateFormat df = new SimpleDateFormat();

        /**
         * TODO After SDK 8 use syncResult.delayUntil
         */
        if( (lastSync != 0) && (nowSync - lastSync) < Constants.MIN_TIME_BW_SYNC ){
        	if(LOGV) Log.v(TAG, "Calling Assembla Sync - Too Soon [last=" + df.format(lastSync)  + ", now="+ df.format(nowSync)  + " ElapsedTime=" + ((nowSync - lastSync)/1000/60) + " min]");
        	return;
        }
        
        mProvider = provider;
        
        SettingsHelper.getInstance(getContext()).putLong(Constants.LAST_SYNC_KEY, nowSync);
         try {
        	 if(LOGV) Log.v(TAG, "Calling Assembla Sync [last=" + df.format(lastSync)  + ", now="+ df.format(nowSync)  + " ElapsedTime=" + ((nowSync - lastSync)/1000/60) + " min ]"); 
             // use the account manager to request the credentials
        	 
             authtoken =
                mAccountManager.blockingGetAuthToken(account,
                    Constants.AUTHTOKEN_TYPE, true /* notifyAuthFailure */);
             // fetch updates from the sample service over the cloud
            try {
				AssemblaAPIAdapter.getInstance(getContext()).setCredentials(account.name, mAccountManager.getPassword(account));
				ArrayList<Space> spaces = AssemblaAPIAdapter.getInstance(getContext()).getMySpaces();
				if(spaces != null){
					Cursor c = null;
					ContentValues cv = new ContentValues();
					for(Space sp:spaces){
						cv.clear();
						c = provider.query(Spaces.CONTENT_URI, Spaces.PROJECTION, Spaces.SPACE_ID + " = ?", new String[]{sp.getId()}, null);

						if(c != null && c.moveToFirst()){
							if (LOGV) Log.v(TAG, "Space From DB: " + sp.name());
							//TODO Update Space
							c.close();
						}else{
							if(c != null){
								c.close();
							}
							
							cv.put(Spaces.NAME, sp.getName());
							cv.put(Spaces.SPACE_ID, sp.getId());
							cv.put(Spaces.DESCRIPTION, sp.getDescription());
							cv.put(Spaces.CREATED_AT, "");	
							provider.insert(Spaces.CONTENT_URI, cv);
						}
						
						this.syncTickets(sp);
					}
					this.syncTasks();
					System.gc();
				}
				
			} catch (XMLParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AssemblaAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RestfulException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            
            // fetch and update status messages for all the synced users.
        } catch (final AuthenticatorException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "AuthenticatorException", e);
        } catch (final OperationCanceledException e) {
            Log.e(TAG, "OperationCanceledExcetpion", e);
        } catch (final IOException e) {
            Log.e(TAG, "IOException", e);
            syncResult.stats.numIoExceptions++;
        }catch (final ParseException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "ParseException", e);
        }
    }

    /**
     * 
     * @param sp
     * @throws XMLParsingException
     * @throws AssemblaAPIException
     * @throws RestfulException
     * @throws RemoteException
     */
    private void syncTickets(Space sp) throws XMLParsingException, AssemblaAPIException, RestfulException, RemoteException{
    	
		Cursor c = null;
		ContentValues cv = new ContentValues();
		ArrayList<Ticket> tickets = AssemblaAPIAdapter.getInstance(getContext()).getTicketsBySpaceId(sp.getId(), false, false);
		if(tickets != null){
			for(Ticket tk:tickets){
				cv.clear();
				c = mProvider.query(Tickets.CONTENT_URI, new String[]{Tickets._ID}, Tickets.NUMBER + " = ?", new String[]{ String.valueOf(tk.getNumber())},null);
				if(c != null && c.moveToFirst()){
					if(LOGV) Log.v(TAG,"Ticket From DB - Space: " + sp.getName() + " Number: "  + tk.getNumber() );
					//TODO Update Ticket
					c.close();
				}else{
					if(c != null){
						c.close();
					}
					cv.put(Tickets.SPACE_ID, sp.getId());
					cv.put(Tickets.TICKET_ID, tk.getId());
					cv.put(Tickets.NUMBER, tk.getNumber());
					cv.put(Tickets.SUMMARY, tk.getName());
					cv.put(Tickets.DESCRIPTION, tk.getDescription());
					cv.put(Tickets.PRIORITY, tk.getPriority());
					cv.put(Tickets.STATUS, tk.getStatusName());
					cv.put(Tickets.ASSIGNED_TO_ID, tk.getAssignedToId());
					cv.put(Tickets.WORKING_HOURS, tk.getWorkingHours());
					mProvider.insert(Tickets.CONTENT_URI, cv);
				}
			}
		}
    }
    
    /**
     * 
     * @throws XMLParsingException
     * @throws AssemblaAPIException
     * @throws RestfulException
     * @throws RemoteException
     */
    private void syncTasks() throws XMLParsingException, AssemblaAPIException, RestfulException, RemoteException{
    	ArrayList<Task> tasks = AssemblaAPIAdapter.getInstance(getContext()).getTasks();
    	DateUtils dt = new DateUtils();
    	
    	if(tasks != null){
    		Cursor c = null;
    		ContentValues cv = new ContentValues();
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US);
    		
    		for(Task tk: tasks){
    			cv.clear();
    			c = mProvider.query(Tasks.CONTENT_URI, new String[]{Tasks._ID}, Tasks.TASK_ID + "= ?", new String[]{String.valueOf(tk.getId())}, null);
    			if((c != null) && c.moveToFirst() ){
    				if(LOGV) Log.v(TAG,"Task From DB "  + tk.getDescription() );
    				//TODO Update Task
    				c.close();
    			}else{
    				if(c!= null){
    					c.close();
    				}
    				cv.put(Tasks.TASK_ID,tk.getId());
					cv.put(Tasks.TICKET_ID,tk.getTicketId());
					cv.put(Tasks.TICKET_NUMBER,tk.getTicketNumber());
					cv.put(Tasks.SPACE_ID,tk.getSpaceId());
					cv.put(Tasks.DESCRIPTION,tk.getDescription());
					cv.put(Tasks.HOURS,tk.getHours());
					cv.put(Tasks.USER_ID,tk.getUserId());
					if(tk.getBeginAt() != null){
						cv.put(Tasks.BEGIN_AT,sdf.format(tk.getBeginAt()));
						cv.put(Tasks.END_AT,sdf.format(tk.getEndAt()));
						cv.put(Tasks.UPDATED_AT,sdf.format(tk.getEndAt()));
					}
					mProvider.insert(Tasks.CONTENT_URI, cv);
    			}
    		}
    	}
    }
}

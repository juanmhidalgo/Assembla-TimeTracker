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
import java.util.ArrayList;

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
import android.util.Log;

import com.starredsolutions.assemblandroid.AssemblaAPIAdapter;
import com.starredsolutions.assemblandroid.Constants;
import com.starredsolutions.assemblandroid.exceptions.AssemblaAPIException;
import com.starredsolutions.assemblandroid.exceptions.XMLParsingException;
import com.starredsolutions.assemblandroid.models.Space;
import com.starredsolutions.assemblandroid.models.Ticket;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Spaces;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;
import com.starredsolutions.net.RestfulException;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);
    
    private final AccountManager mAccountManager;
    private final Context mContext;


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {
        String authtoken = null;
        
        
         try {
        	 if(LOGV) Log.v(TAG, "Calling Assembla sync"); 
             // use the account manager to request the credentials
             authtoken =
                mAccountManager.blockingGetAuthToken(account,
                    Constants.AUTHTOKEN_TYPE, true /* notifyAuthFailure */);
             // fetch updates from the sample service over the cloud
            try {
				AssemblaAPIAdapter.getInstance().setCredentials(account.name, mAccountManager.getPassword(account));
				ArrayList<Space> spaces = AssemblaAPIAdapter.getInstance().getMySpaces();
				if(spaces != null){
					ArrayList<Ticket> tickets = new ArrayList<Ticket>();
					Cursor c = null;
					ContentValues cv = new ContentValues();
					for(Space sp:spaces){
						cv.clear();
						c = provider.query(Spaces.CONTENT_URI, Spaces.PROJECTION, Spaces.SPACE_ID + " = ?", new String[]{sp.getId()}, null);

						if(c != null && c.moveToFirst()){
							if (LOGV) Log.v(TAG, "Space From DB: " + sp.name());
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
						
						tickets = AssemblaAPIAdapter.getInstance().getTicketsBySpaceId(sp.getId(), false, false);
						if(tickets != null){
							for(Ticket tk:tickets){
								cv.clear();
								c = provider.query(Tickets.CONTENT_URI, new String[]{Tickets._ID}, Tickets.NUMBER + " = ?", new String[]{ String.valueOf(tk.getNumber())},null);
								if(c != null && c.moveToFirst()){
									if(LOGV) Log.v(TAG,"Ticket From DB - Space: " + sp.getName() + " Number: "  + tk.getNumber() );
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
									provider.insert(Tickets.CONTENT_URI, cv);
								}
							}
						}
					}
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
}

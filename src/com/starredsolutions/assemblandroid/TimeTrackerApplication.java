package com.starredsolutions.assemblandroid;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.starredsolutions.assemblandroid.exceptions.AssemblaAPIException;
import com.starredsolutions.assemblandroid.exceptions.XMLParsingException;
import com.starredsolutions.assemblandroid.models.Space;
import com.starredsolutions.assemblandroid.models.Task;
import com.starredsolutions.assemblandroid.models.Ticket;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tasks;
import com.starredsolutions.assemblandroid.provider.AssemblaContract.Tickets;
import com.starredsolutions.net.RestfulException;
import com.starredsolutions.utils.LocalPersistenceHelper;
import com.starredsolutions.utils.base.OnCompleteListener;



public class TimeTrackerApplication extends Application{
    /*********************************************************************************************
     * SINGLETON PATTERN
     *********************************************************************************************/
    static private TimeTrackerApplication instance;
    static public  TimeTrackerApplication getInstance() { return instance; }
    
    
    /*********************************************************************************************
     * CONSTANTS
     *********************************************************************************************/
    static private final String TAG = "TimeTrackerApplication";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE) || Constants.DEVELOPER_MODE;
    
    
    /*********************************************************************************************
     * VARIABLES
     *********************************************************************************************/

	
	public String username;
	public String password;
	
	
	private Task currentTask = null;
	
	
	
	/*********************************************************************************************
     * PUBLIC METHODS
     *********************************************************************************************/
	/**
	 * SINGLETON: A private constructor enforces the Singleton pattern by preventing external instantiation.
	 * (EDIT: damn, Android needs a public constructor to initiate it)
	 */
	public TimeTrackerApplication() {
		instance = this;
	}
	
	/**
	 * Created when this class is instanciated by the Android environment
	 * 
	 * http://stackoverflow.com/questions/456211/activity-restart-on-rotation-android
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		currentTask = (Task) LocalPersistenceHelper.readObjectFromFile(instance, Constants.CURRENT_TASK_FNAME);
		AccountManager mAccountManager = AccountManager.get(instance);
		
		Account ac[] = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		if(ac.length > 0){
			username = ac[0].name;
			password = mAccountManager.getPassword(ac[0]);
		}
	}
	
	
	/**
	 * 
	 * @param _id
	 * @param space_id
	 * @param ticket_number
	 * @return
	 */
	public boolean startTicketTask(long _id,String space_id, int ticket_id, int ticket_number,String description){
		if(this.currentTask != null && this.currentTask.isStarted()){
			//TODO throw exception
		}
		currentTask = new Task(space_id,ticket_id);
		currentTask.set_id(_id);
		currentTask.setTicketNumber(ticket_number);
		currentTask.setDescription(description);
		currentTask.start();

		LocalPersistenceHelper.witeObjectToFile(instance, currentTask, Constants.CURRENT_TASK_FNAME);
		return true;
	}
	
	/**
	 * 
	 * @return
	 */
	public void stopTicketTask(final OnCompleteListener onComplete) {
		if((currentTask == null) || !currentTask.isStarted()){
			//TODO throw exception
		}
		
		(new Thread(new Runnable() {
			
			public void run() {
				try {
					
					currentTask.stop();
					AssemblaAPIAdapter.getInstance(instance).setCredentials(username, password);
					Task tk = AssemblaAPIAdapter.getInstance(instance)
							.saveTicketTask(currentTask.getSpaceId(), currentTask.getTicketId(), currentTask.getHours(), 
											currentTask.getBeginAt(), currentTask.getEndAt(), currentTask.getDescription());
					
					
					LocalPersistenceHelper.deleteObjectFile(instance, Constants.CURRENT_TASK_FNAME);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US);
					
					ContentValues cv = new ContentValues();
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
					getContentResolver().insert(Tasks.CONTENT_URI, cv);
					
					currentTask = null;
					if(onComplete != null){
						onComplete.onComplete(true,null);
					}
				} catch (AssemblaAPIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if(onComplete != null){
						onComplete.onComplete(false,e.getMessage());
					}
				} catch (XMLParsingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if(onComplete != null){
						onComplete.onComplete(false,e.getMessage());
					}
				} catch (RestfulException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if(onComplete != null){
						onComplete.onComplete(false,e.getMessage());
					}
				}
			}
		})).start();
	}
	
	/**
	 * 
	 * @return
	 */
	public Task getCurrentTask(){
		return currentTask;
	}
	
	
	

	/**
	 * 
	 * @param sp
	 * @param syncTasks
	 * @throws XMLParsingException
	 * @throws AssemblaAPIException
	 * @throws RestfulException
	 */
	public void syncTicketsForSpace(Space sp,boolean syncTasks) throws XMLParsingException, AssemblaAPIException, RestfulException{
		if(sp != null){
			syncTicketsForSpace(sp.getId(),syncTasks);
		}
	}
	
	
	/**
	 * 
	 * @param spaceId
	 * @param syncTasks
	 * @throws XMLParsingException
	 * @throws AssemblaAPIException
	 * @throws RestfulException
	 */
	public void syncTicketsForSpace(String spaceId,boolean syncTasks) throws XMLParsingException, AssemblaAPIException, RestfulException{
		Cursor c = null;
		ContentValues cv = new ContentValues();
		
		ContentResolver mProvider = this.getContentResolver();
		AssemblaAPIAdapter apiClient = AssemblaAPIAdapter.getInstance(this);
		apiClient.setCredentials(username, password);
		List<Ticket> tickets = apiClient.getTicketsBySpaceId(spaceId, false, false);
		if(tickets != null){
			for(Ticket tk:tickets){
				cv.clear();
				c = mProvider.query(Tickets.CONTENT_URI, new String[]{Tickets._ID}, Tickets.NUMBER + " = ?", new String[]{ String.valueOf(tk.getNumber())},null);
				if(c != null && c.moveToFirst()){
					cv.put(Tickets.SUMMARY, tk.getName());
					cv.put(Tickets.DESCRIPTION, tk.getDescription());
					cv.put(Tickets.PRIORITY, tk.getPriority());
					cv.put(Tickets.STATUS, tk.getStatusName());
					cv.put(Tickets.ASSIGNED_TO_ID, tk.getAssignedToId());
					cv.put(Tickets.WORKING_HOURS, tk.getWorkingHours());
					Uri uri = Tickets.buildTicketBySpaceAndNumberUri(spaceId, tk.getNumber());
					mProvider.update(uri, cv, null, null);
					
					c.close();
				}else{
					if(c != null){
						c.close();
					}
					cv.put(Tickets.SPACE_ID, spaceId);
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
			if(syncTasks){
				this.syncTasks();
			}
		}
	}
	
	/**
	 * 
	 * @param space
	 * @param ticket
	 * @throws XMLParsingException
	 * @throws AssemblaAPIException
	 * @throws RestfulException
	 */
	public void syncTasksForSpaceAndTicketNumber(Space space,Ticket ticket) throws XMLParsingException, AssemblaAPIException, RestfulException{
		if(space != null && ticket != null){
			syncTasksForSpaceAndTicketNumber(space.getId(), ticket.getNumber());
		}
	}
	
	/**
	 * 
	 * @param spaceId
	 * @param ticketNumber
	 * @throws XMLParsingException
	 * @throws AssemblaAPIException
	 * @throws RestfulException
	 */
	public void syncTasksForSpaceAndTicketNumber(String spaceId,int ticketNumber) throws XMLParsingException, AssemblaAPIException, RestfulException{
		Cursor c = null;
		ContentValues cv = new ContentValues();
		
		ContentResolver mProvider = this.getContentResolver();
		AssemblaAPIAdapter apiClient = AssemblaAPIAdapter.getInstance(this);
		apiClient.setCredentials(username, password);
		
		List<Task> tasks = apiClient.getTasksBySpaceIdAndTicketNumber(spaceId, ticketNumber);
		if(tasks != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US);
			for(Task tk: tasks){
    			cv.clear();
    			c = mProvider.query(Tasks.CONTENT_URI, new String[]{Tasks._ID}, Tasks.TASK_ID + "= ?", new String[]{String.valueOf(tk.getId())}, null);
    			if((c != null) && c.moveToFirst() ){//Already Exists
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
	
	/**
	 * 
	 * @throws XMLParsingException
	 * @throws AssemblaAPIException
	 * @throws RestfulException
	 */
	public void syncTasks() throws XMLParsingException, AssemblaAPIException, RestfulException{
		Cursor c = null;
		ContentValues cv = new ContentValues();
		
		ContentResolver mProvider = this.getContentResolver();
		AssemblaAPIAdapter apiClient = AssemblaAPIAdapter.getInstance(this);
		apiClient.setCredentials(username, password);
		
		List<Task> tasks = apiClient.getTasks();
		if(tasks != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US);
			for(Task tk: tasks){
				cv.clear();
				c = mProvider.query(Tasks.CONTENT_URI, new String[]{Tasks._ID}, Tasks.TASK_ID + "= ?", new String[]{String.valueOf(tk.getId())}, null);
				if((c != null) && c.moveToFirst() ){//Already Exists
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

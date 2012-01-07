package com.starredsolutions.assemblandroid.job;

import java.util.ArrayList;

import com.starredsolutions.assemblandroid.AssemblaAPIAdapter;
import com.starredsolutions.assemblandroid.asyncTask.ParsedArrayList;
import com.starredsolutions.assemblandroid.exceptions.AssemblaAPIException;
import com.starredsolutions.assemblandroid.exceptions.XMLParsingException;
import com.starredsolutions.assemblandroid.models.Space;
import com.starredsolutions.assemblandroid.models.Task;
import com.starredsolutions.assemblandroid.models.Ticket;
import com.starredsolutions.net.RestfulException;
import com.starredsolutions.utils.MyTimer;


/**
 * This class is used to syncronize the TimeEntries entered manually via the web interface and 
 * those entered via the TimeTracker.
 * 
 * You need to run this class as a Java application in cronjob. This will make sure that the total
 * workedhours ticket property will be accurate.
 * 
 * @author david
 */
public class SyncWorkedHoursJob {
	
	static private AssemblaAPIAdapter _assemblaAdapter;
	
	
	/**
	 * Start as follows:
	 * java -classpath bin:lib/commons-lang-2.6.jar:lib/httpclient-4.1.1.jar:lib/httpclient-cache-4.1.1.jar:lib/httpcore-4.1.jar:lib/httpmime-4.1.jar:lib/commons-codec-1.4.jar:lib/commons-logging-1.1.1.jar:lib/android-mock.jar com.starredsolutions.assemblandroid.job.SyncWorkedHoursJob
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new SyncWorkedHoursJob(args);
	}
	
	
	public SyncWorkedHoursJob(String[] args)  {
	    MyTimer timer = MyTimer.start("TotalTime");
		
	    try {
            String username = SyncWorkedHoursJobCredentials.USERNAME;
            String password = SyncWorkedHoursJobCredentials.PASSWORD;

            log("\t => Authenticating with user " + username + "\n");
            _assemblaAdapter = AssemblaAPIAdapter.getInstance();
            _assemblaAdapter.setCredentials(username, password);
            
            syncAllSpaces();
            
            timer.stop();
            
            log("\n\t => Total syncronizing time : " + timer + "\n\n");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void syncAllSpaces() throws XMLParsingException, AssemblaAPIException, RestfulException {
	    ArrayList<Space> spaces = _assemblaAdapter.getMySpaces();
	    log("\t => Found " + Integer.toString(spaces.size()) + " spaces.\n");
	    
	    for (Space space : spaces) {
	        syncSpace( space );
	    }
	}
	
	public void syncSpace(Space space) throws XMLParsingException, AssemblaAPIException, RestfulException {
		log("\n\t => Syncronizing space '" + space.name() + "'\n");
		
		MyTimer timer = MyTimer.start("FetchingTickets");
		ParsedArrayList<Ticket> tickets = space.reloadTickets(true, true);
		timer.stop();
		
		if (tickets != null) {
			log(String.format("\t ** Fetched %d tickets in %s\n", tickets.size(), timer) );
			
			for(Ticket ticket : tickets)
				syncTicket(space.id(), ticket);
		}
	}
	
	public void syncTicket(String spaceId, Ticket t) throws XMLParsingException, AssemblaAPIException, RestfulException {
		log(String.format("\n\t\t ** Syncronizing ticket #%d '%s'\n", t.number(), t.name()) );
		
		MyTimer timer = MyTimer.start("FetchingTimeEntries");
		ParsedArrayList<Task> tasks = t.reloadTasks();
		timer.stop();
		
		float hours = 0.0f;
		
		if (tasks != null) {
			log(String.format("\t\t ** Fetched %d time entries in %s\n", tasks.size(), timer) );
			log("\t\t  * Time entries (hh:mm:ss) : ");
			
			for (Task task : tasks) {
				log(task.elapsedTime() + " ");
				hours += task.hours();
			}
		}
		log(String.format("\n\t\t ** Total Tasks time (%.4f) vs Actual Ticket time (%.4f)\n", hours, t.workedHours() ));
		if (! t.workedHours().equals(hours)) {
			log("\t\t => Updating ticket...\n");
			t.setWorkedHours( hours );
			
			_assemblaAdapter.updateTicketHours(spaceId, t);
		}			
	}
	
	static public void log(String msg){
		System.out.print(msg);
	}
}

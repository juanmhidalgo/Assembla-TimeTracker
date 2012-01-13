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
		
	    try {
            String username = SyncWorkedHoursJobCredentials.USERNAME;
            String password = SyncWorkedHoursJobCredentials.PASSWORD;

            log("\t => Authenticating with user " + username + "\n");
            _assemblaAdapter = AssemblaAPIAdapter.getInstance();
            _assemblaAdapter.setCredentials(username, password);
            
            syncAllSpaces();
            
            
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
		
		ParsedArrayList<Ticket> tickets = space.reloadTickets(true, true);
		
		if (tickets != null) {
			for(Ticket ticket : tickets)
				syncTicket(space.id(), ticket);
		}
	}
	
	public void syncTicket(String spaceId, Ticket t) throws XMLParsingException, AssemblaAPIException, RestfulException {
		log(String.format("\n\t\t ** Syncronizing ticket #%d '%s'\n", t.number(), t.name()) );
		
		ParsedArrayList<Task> tasks = t.reloadTasks();
		
		float hours = 0.0f;
		
		if (tasks != null) {
			
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

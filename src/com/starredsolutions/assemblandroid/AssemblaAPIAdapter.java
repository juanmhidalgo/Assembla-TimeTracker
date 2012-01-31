package com.starredsolutions.assemblandroid;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.starredsolutions.assemblandroid.exceptions.AssemblaAPIException;
import com.starredsolutions.assemblandroid.exceptions.XMLParsingException;
import com.starredsolutions.assemblandroid.models.Space;
import com.starredsolutions.assemblandroid.models.Task;
import com.starredsolutions.assemblandroid.models.Ticket;
import com.starredsolutions.assemblandroid.models.TicketComparator;
import com.starredsolutions.assemblandroid.parsers.AssemblaParser;
import com.starredsolutions.net.RequestMethod;
import com.starredsolutions.net.RestfulClient;
import com.starredsolutions.net.RestfulException;
import com.starredsolutions.utils.SettingsHelper;

/**
 * AssemblaAPIAdapter is responsible for communicating with the Assembla Web 
 * Services and to provide helper methods to retrieving, creating, and 
 * modifying Assembla REST resources
 * 
 * XPath References:
 *   http://developer.android.com/reference/javax/xml/xpath/package-summary.html
 * 
 * @author david
 */
public class AssemblaAPIAdapter {
	private static final String TAG = "AssemblaAPIAdapter";
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE) || Constants.DEVELOPER_MODE;
	private Context context = null;
	
	// Singleton Pattern
	static private AssemblaAPIAdapter instance = null;
	
	static public AssemblaAPIAdapter getInstance(Context ctx) throws XMLParsingException {
		if (instance == null){
			instance = new AssemblaAPIAdapter(ctx);
		}
		return instance;
	}
	
	
	private String username = ""; // "assemblandroid";
	private String password = ""; // "login";
	private String userId   = ""; // "a9EIrcDOKr4imveJe5cbLA";
	
	private RestfulClient client;
	
	private int skippedTickets = 0;
	
	public int skippedTickets() { return this.skippedTickets; }
	
	public String getUserId() { return this.userId; }
	
	
	
	
	/**
	 * SINGLETON: A private constructor enforces the Singleton pattern by preventing external instanciation.
	 * @throws XMLParsingException 
	 */
	private AssemblaAPIAdapter(Context ctx) throws XMLParsingException {
		this.context = ctx;
	}
	
	
	public void setCredentials(String username, String password) throws XMLParsingException, RestfulException, AssemblaAPIException {
		this.username = username;
		this.password = password;
		
		getMyUserId();
		
	}
	
	private String request(RequestMethod method, String url) throws RestfulException {
		return request(method, url, "");
	}
	
	/**
	 * Send a REST request to Assembla
	 * @return String xml
	 * @throws RestfulException 
	 */
	
	private String request(RequestMethod method, String url, String xmlData) throws RestfulException {
		client = new RestfulClient( method, url );
		//client.verbose = true;
        client.addHeader("Accept", "application/xml");
        client.addHttpBasicAuthCredentials(username, password);
        
        if (xmlData.length() > 0) {
			client.addHeader("Content-Type", "application/xml");
        	client.setMessageBody( xmlData );
        }
        
        client.execute();
        
        return client.getResponse();
	}
	
	
	/**
	 * GET http://www.assembla.com/spaces/<space_id>
	 * curl -i -X GET -H "Accept: application/xml" https://assemblandroid:login@www.assembla.com/spaces/assemblandroid-timetracker
	 * @throws RestfulException 
	 * @throws XMLParsingException 
	 * @throws AssemblaAPIException 
	 */
	public Space getSpace( String spaceId ) throws RestfulException, XMLParsingException, AssemblaAPIException {
		String url = "https://www.assembla.com/spaces/" + spaceId;
		
		String response = request(RequestMethod.GET, url);

        if (client.getStatusCode() != 200) {
			String msg = "HTTP Error while retrieving Space. Expected status 200 OK, but got " + 
				Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
        
        return AssemblaParser.parseSpace(response);
	}
	
	/**
	 * GET http://www.assembla.com/spaces/my_spaces
	 * curl -i -X GET -H "Accept: application/xml" https://assemblandroid:login@www.assembla.com/spaces/my_spaces
	 * @throws XMLParsingException 
	 * @throws RestfulException 
	 * @throws AssemblaAPIException 
	 */
	public List<Space> getMySpaces() throws XMLParsingException, RestfulException, AssemblaAPIException {
		String url = "https://www.assembla.com/spaces/my_spaces";
		
		String response = request(RequestMethod.GET, url);

        if (client.getStatusCode() != 200) {
			String msg = "HTTP Error while retrieving MySpaces. Expected status 200 OK, but got " + 
				Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
        
		return AssemblaParser.parseSpaceList(response);
	}
	
	
	
	/**
	 * HTTPS unsupported !?!?
	 * GET http://www.assembla.com/user/best_profile/<user-id>
	 * curl -i -X GET -H "Accept: application/xml" http://assemblandroid:login@www.assembla.com/user/best_profile/assemblandroid
	 * @throws XMLParsingException 
	 * @throws RestfulException 
	 * @throws AssemblaAPIException 
	 */
	public String getMyUserId() throws XMLParsingException, RestfulException, AssemblaAPIException {
		
		
		if(!TextUtils.isEmpty(userId)){
			if(LOGV) Log.v(TAG,"getMyUserId from instance");
			return userId; 
		}else{
			if(SettingsHelper.getInstance(context).containsKey(Constants.USERID_KEY)){
				if(LOGV) Log.v(TAG,"getMyUserId from SharedPreferences");
				userId = SettingsHelper.getInstance(context).getString(Constants.USERID_KEY,null);
				return userId;
			}else{
				if(LOGV) Log.v(TAG,"getMyUserId from Assembla");
				String url = "https://www.assembla.com/user/best_profile/" + username;
				
				String response = request(RequestMethod.GET, url);
		
		        if (client.getStatusCode() != 200) {
					String msg = "HTTP Error while retrieving UserId. Expected status 200 OK, but got " + 
						Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
					throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
				}
		        return AssemblaParser.parseUserId(response);
			}
		}
	}
	
	
	
	/**
	 * GET https://www.assembla.com/spaces/<space_id>/tickets
	 * GET https://www.assembla.com/spaces/<space_id>/tickets/report/0
	 * 
	 * curl -i -X GET -H "Accept: application/xml" https://assemblandroid:login@www.assembla.com/spaces/assemblandroid-timetracker/tickets
	 * curl -i -X GET -H 'Accept: application/xml' "https://assemblandroid:login@www.assembla.com/spaces/dvpLtiDOOr4lSCeJe5cbCb/tickets/report/0"
	 * 
	 * @param spaceId
	 * @param includeClosed Include tickets that are already closed
	 * @param includeOthers Include tickets assigned to other users
	 * @throws RestfulException 
	 * @throws XMLParsingException 
	 * @throws AssemblaAPIException 
	 */
	public List<Ticket> getTicketsBySpaceId(String spaceId, boolean includeClosed, boolean includeOthers) 
				throws RestfulException, XMLParsingException, AssemblaAPIException
		{
		String url;
		
		if (includeClosed){
			url = "http://www.assembla.com/spaces/" + spaceId + "/tickets/report/7";
		}else{
			url = "https://www.assembla.com/spaces/" + spaceId + "/tickets";
		}
		
		String response = request(RequestMethod.GET, url);

        if (client.getStatusCode() != 200) {
			String msg = "HTTP Error while retrieving Tickets. Expected status 200 OK, but got " + 
				Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
		
        List<Ticket> tickets = AssemblaParser.parseTicketList(response, includeOthers, userId);
		Collections.sort(tickets, new TicketComparator());
		
		return tickets;
	}

	
	/**
	 * GET https://www.assembla.com/spaces/<space-id>/tickets/<ticket-number>
	 * curl -i -X GET -H "Accept: application/xml" https://assemblandroid:login@www.assembla.com/spaces/assemblandroid-timetracker/tickets/2
	 * @param spaceId
	 * @param number
	 * @throws RestfulException 
	 * @throws XMLParsingException 
	 * @throws AssemblaAPIException 
	 */
	public List<Task> getTasksBySpaceIdAndTicketNumber(String spaceId, int number) throws RestfulException, XMLParsingException, AssemblaAPIException {
		String url = "https://www.assembla.com/spaces/" + spaceId + "/tickets/" + Integer.toString(number);
		
		String response = request(RequestMethod.GET, url);
        
		if (client.getStatusCode() != 200) {
			String msg = "HTTP Error while retrieving Tasks. Expected status 200 OK, but got " + 
				Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
		return AssemblaParser.parseTaskListFromTicket(response);
	}
	
	/**
	 * GET https://www.assembla.com/spaces/<space-id>/tickets/<ticket-number>
	 * curl -i -X GET -H "Accept: application/xml" https://assemblandroid:login@www.assembla.com/user/time_entries
	 * @param spaceId
	 * @param number
	 * @throws RestfulException 
	 * @throws XMLParsingException 
	 * @throws AssemblaAPIException 
	 */
	public List<Task> getTasks() throws RestfulException, XMLParsingException, AssemblaAPIException {
		String url = "https://www.assembla.com/user/time_entries";
		
		String response = request(RequestMethod.GET, url);
		
		if (client.getStatusCode() != 200) {
			String msg = "HTTP Error while retrieving Tasks. Expected status 200 OK, but got " + 
					Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
		
		return AssemblaParser.parseTaskList(response);
	}

	/**
	 * 
	 * @param space_id
	 * @param ticket_id
	 * @param hours
	 * @param beginAt
	 * @param endAt
	 * @param description
	 * @return
	 * @throws AssemblaAPIException
	 * @throws RestfulException
	 * @throws XMLParsingException
	 */
	public Task saveTicketTask(String space_id,int ticket_id,float hours,Date beginAt,Date endAt,String description)throws AssemblaAPIException, RestfulException, XMLParsingException{
		String url = "https://www.assembla.com/user/time_entries";
		
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
			"<task>" + 
			"	<hours>" + Float.toString(hours) + "</hours>" + 
			"	<description>"+ description+"</description>" + 
			"	<begin-at>" + beginAt + "</begin-at>" + 
			"	<end-at>" + endAt + "</end-at>" +   			// 2011-05-04 17:15 UTC 
			"	<space-id>" + space_id + "</space-id>" + 
			"	<ticket-id>" + ticket_id + "</ticket-id>" + 
			"</task>";
		
		String response = request(RequestMethod.POST, url, xml);
		
		if (client.getStatusCode() != 201) {
			String msg = "HTTP Error while saving TimeEntry. Expected status 201 Created, but got " + 
				Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
		return AssemblaParser.parseTask(response);
	}
	
	/**
	 * POST https://www.assembla.com/user/time_entries
	 * curl -i -X POST -H "Content-Type:application/xml" -H "Accept: application/xml" -d "<task><hours>0.75</hours><description>my first time entry</description><begin-at>2011-05-04 16:30 UTC</begin-at><end-at>2011-05-04 17:15 UTC</end-at><space-id>dvpLtiDOOr4lSCeJe5cbCb</space-id><ticket-id>3779344</ticket-id><description>Test Time Entry</description></task>" https://assemblandroid:login@www.assembla.com/user/time_entries
	 * 
	 * PUT https://www.assembla.com/spaces/<space-id>/tickets/<ticket-number>
	 * curl -i -X PUT -H "Content-Type:application/xml" -H "Accept: application/xml" -d "<ticket><custom-fields><workedhours>3.5</workedhours><lastlogmessage>Hard work</lastlogmessage></custom-fields></ticket>" https://assemblandroid:login@www.assembla.com/spaces/assemblandroid-timetracker/tickets/12
	 * @param task
	 * @throws AssemblaAPIException 
	 * @throws RestfulException 
	 */
	public void saveTimeEntry(Space space, Ticket ticket, Task task) throws AssemblaAPIException, RestfulException {
		// TODO: verify that ticket hasn't changed since last commit
		
		// 1. Save the time entry
		String url = "https://www.assembla.com/user/time_entries";
		
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
			"<task>" + 
			"	<hours>" + Float.toString(task.elapsedHours()) + "</hours>" + 
			"	<description>" + TextUtils.htmlEncode(task.getDescription()) + "</description>" + 
			"	<begin-at>" + task.getBeginAt() + "</begin-at>" + 
			"	<end-at>" + task.getEndAt() + "</end-at>" +   			// 2011-05-04 17:15 UTC 
			"	<space-id>" + space.id() + "</space-id>" + 
			"	<ticket-id>" + ticket.id() + "</ticket-id>" + 
			"</task>";
		
		String response = request(RequestMethod.POST, url, xml);
		
		if (client.getStatusCode() != 201) {
			String msg = "HTTP Error while saving TimeEntry. Expected status 201 Created, but got " + 
				Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
		
		// 2. Update the project
		url = "https://www.assembla.com/spaces/" + space.id() + "/tickets/" + Integer.toString(ticket.number());
		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<ticket>"
			+ "  <assigned-to-id>" + this.userId + "</assigned-to-id>"
			+ "  <status>" + Integer.toString(ticket.status()) + "</status>"
			+ "  <custom-fields>"
			+ "    <workedhours>" + Float.toString(ticket.workedHours()) + "</workedhours>"
			+ "    <lastlogmessage>" + TextUtils.htmlEncode(task.getDescription()) + "</lastlogmessage>"
			+ "  </custom-fields>"
			+ "</ticket>";
		
		response = request(RequestMethod.PUT, url, xml);
		
		if (client.getStatusCode() != 200) {
			String msg = "HTTP Error while updating Ticket. Expected status 200 OK, but got " + 
				Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
	}
	
	
	/**
	 * PUT https://www.assembla.com/spaces/<space-id>/tickets/<ticket-number>
	 * @param spaceId
	 * @param ticket
	 * @throws AssemblaAPIException 
	 * @throws RestfulException 
	 */
	public void updateTicketHours(String spaceId, Ticket t) throws RestfulException, AssemblaAPIException {
		String url = "https://www.assembla.com/spaces/" + spaceId + "/tickets/" + Integer.toString(t.number());
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<ticket>"
			+ "  <custom-fields>"
			+ "    <workedhours>" + Float.toString(t.workedHours()) + "</workedhours>"
			+ "  </custom-fields>"
			+ "</ticket>";
		
		String response = request(RequestMethod.PUT, url, xml);
		
		if (client.getStatusCode() != 200) {
			String msg = "HTTP Error while updating Ticket. Expected status 200 OK, but got " + 
				Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
	}
}
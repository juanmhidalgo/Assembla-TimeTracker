package com.starredsolutions.assemblandroid;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.starredsolutions.assemblandroid.asyncTask.ParsedArrayList;
import com.starredsolutions.assemblandroid.exceptions.AssemblaAPIException;
import com.starredsolutions.assemblandroid.exceptions.XMLParsingException;
import com.starredsolutions.assemblandroid.models.Space;
import com.starredsolutions.assemblandroid.models.Task;
import com.starredsolutions.assemblandroid.models.Ticket;
import com.starredsolutions.assemblandroid.models.TicketComparator;
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
	private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);
	private Context context = null;
	
	// Singleton Pattern
	static private AssemblaAPIAdapter instance = null;
	
	static public AssemblaAPIAdapter getInstance(Context ctx) throws XMLParsingException {
		if (instance == null){
			instance = new AssemblaAPIAdapter(ctx);
		}
		return instance;
	}
	
	private SAXReader reader = new SAXReader(); // dom4j SAXReader

	
	private String username = ""; // "assemblandroid";
	private String password = ""; // "login";
	private String userId   = ""; // "a9EIrcDOKr4imveJe5cbLA";
	
	private RestfulClient client;
	
	private int skippedTickets = 0;
	
	public int skippedTickets() { return this.skippedTickets; }
	
	public String getUserId() { return this.userId; }
	
	
	static private InputStream stringToInputStream( String str ) throws UnsupportedEncodingException {
		return new ByteArrayInputStream(str.getBytes("UTF-8"));
	}
	
	
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
	 * 
	 * @param node
	 * @return
	 */
	private String getNodeValueAsString(Node node){
		if(node == null){
			return "";
		}
		return node.getStringValue();
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	private Integer getNodeValueAsInteger(Node node){
		if(node == null){
			return null;
		}
		String value = this.getNodeValueAsString(node);
		if(value != null){
			return new Integer(value);
		}
		return null;
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	private Float getNodeValueAsFloat(Node node){
		if(node == null){
			return null;
		}
		String value = this.getNodeValueAsString(node);
		if(value != null){
			return new Float(value);
		}
		return null;
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
        
        String id, name, description;
		try {
			Document doc = reader.read( stringToInputStream(response) );
			
			id 			= this.getNodeValueAsString(doc.selectSingleNode("/space/id"));
			name        = this.getNodeValueAsString(doc.selectSingleNode("/space/name"));
			description = this.getNodeValueAsString(doc.selectSingleNode("/space/description"));
			
			return new Space(id, name, description);
			
		} catch (Exception e) {
			throw new XMLParsingException("Error while parsing Space XML", e);
		}
	}
	
	/**
	 * GET http://www.assembla.com/spaces/my_spaces
	 * curl -i -X GET -H "Accept: application/xml" https://assemblandroid:login@www.assembla.com/spaces/my_spaces
	 * @throws XMLParsingException 
	 * @throws RestfulException 
	 * @throws AssemblaAPIException 
	 */
	public ArrayList<Space> getMySpaces() throws XMLParsingException, RestfulException, AssemblaAPIException {
		String url = "https://www.assembla.com/spaces/my_spaces";
		
		String response = request(RequestMethod.GET, url);

        if (client.getStatusCode() != 200) {
			String msg = "HTTP Error while retrieving MySpaces. Expected status 200 OK, but got " + 
				Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
        
		ArrayList<Space> spaces = new ArrayList<Space>();
        String id, name, description;
        
		try {
			Document doc = reader.read( stringToInputStream(response) );
			
			
			List<Node> nodes = (List<Node>) doc.selectNodes("/spaces/space");
			
			for(int i=0; i<nodes.size() ; i++) {
				org.dom4j.Node node = nodes.get(i);
				
				id          = this.getNodeValueAsString(node.selectSingleNode("id"));
				name        = this.getNodeValueAsString(node.selectSingleNode("name"));
				description = this.getNodeValueAsString(node.selectSingleNode("description"));
				
				
				spaces.add( new Space(id, name, description) );
				
			}
			
		}
		catch (Exception e)
		{
			throw new XMLParsingException("Error while parsing MySpaces XML", e);
		}
		
		return spaces;
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
		        
				try {
					Document doc = reader.read( stringToInputStream(response) );
					
					userId = this.getNodeValueAsString(doc.selectSingleNode("/user/id"));
					SettingsHelper.getInstance(this.context).putString(Constants.USERID_KEY, userId);
					
					return userId;
				} catch (Exception e) {
					throw new XMLParsingException("Error occured while parsing user profile XML", e);
				}
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
	public ParsedArrayList<Ticket> getTicketsBySpaceId(String spaceId, boolean includeClosed, boolean includeOthers) 
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
		
		ParsedArrayList<Ticket> tickets = new ParsedArrayList<Ticket>();
        
		int id, number, priority, status;
		String statusName, description, summary, assignedToId;
		float workingHours, workedHours;
		
		skippedTickets = 0;
        
		
		try {
			Document doc = reader.read( stringToInputStream(response) );
			
			List<Node> nodes =  (List<Node>) doc.selectNodes("/tickets/ticket");
			
			for(int i=0; i<nodes.size() ; i++) {
				Node node = nodes.get(i);
				
				// First, check that the ticket belongs to me, or is unassigned
				assignedToId = this.getNodeValueAsString(node.selectSingleNode("assigned-to-id"));
				
				// Filter other people tickets if requested
				if (!includeOthers) {
					if ( !assignedToId.equals(userId) && assignedToId.length() > 0)  {
						skippedTickets++;
						continue;
					}
				}
				
				id           = this.getNodeValueAsInteger(node.selectSingleNode("id"));
				number       = this.getNodeValueAsInteger(node.selectSingleNode("number"));
				priority     = this.getNodeValueAsInteger(node.selectSingleNode("priority"));
				status       = this.getNodeValueAsInteger(node.selectSingleNode("status"));
				
				statusName   = this.getNodeValueAsString(node.selectSingleNode("status-name"));
				description  = this.getNodeValueAsString(node.selectSingleNode("description"));
				summary      = this.getNodeValueAsString(node.selectSingleNode("summary"));
				workingHours = this.getNodeValueAsFloat(node.selectSingleNode("working-hours"));
				
				description  = TextUtils.htmlEncode(description);
				
				// Save workedHours only if the custom-field is present
				String tmpStr = this.getNodeValueAsString(node.selectSingleNode("custom-fields/custom-field[@name='workedhours']"));
				
				workedHours   =  !TextUtils.isEmpty(tmpStr) ?  new Float(tmpStr) : 0.0f;
				
				String lastLogMsg = this.getNodeValueAsString(node.selectSingleNode("custom-fields/custom-field[@name='lastlogmessage']"));
				
				// overwrite the spaceId received as argument
				spaceId      = this.getNodeValueAsString(node.selectSingleNode("space-id"));
				
				
				Ticket ticket = new Ticket(id, number, priority, status, statusName, description, 
						summary, workingHours, workedHours, spaceId, assignedToId, lastLogMsg);
				
				tickets.add(ticket);
			}
			
			// Sort tickets
			Collections.sort(tickets, new TicketComparator());
			tickets.setSkippedItems(skippedTickets);
			
			
		} catch (Exception e){
			throw new XMLParsingException("Error while parsing Tickets XML", e);
		}

		
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
	public ParsedArrayList<Task> getTasksBySpaceIdAndTicketNumber(String spaceId, int number) throws RestfulException, XMLParsingException, AssemblaAPIException {
		String url = "https://www.assembla.com/spaces/" + spaceId + "/tickets/" + Integer.toString(number);
		
		String response = request(RequestMethod.GET, url);
        
		if (client.getStatusCode() != 200) {
			String msg = "HTTP Error while retrieving Tasks. Expected status 200 OK, but got " + 
				Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
		
		ParsedArrayList<Task> tasks = new ParsedArrayList<Task>();
        
		int id, ticketId;
		Date beginAt = null, endAt = null;
		float hours;
		String description, userId;
		
        try {
			Document doc = reader.read( stringToInputStream(response) );
			
			List<Node> nodes =  (List<Node>) doc.selectNodes("/ticket/tasks/task");
			
			for(int i=0; i<nodes.size() ; i++) {
				Node node = nodes.get(i);
				
				id           = this.getNodeValueAsInteger(node.selectSingleNode("id"));
				hours        = this.getNodeValueAsFloat(node.selectSingleNode("hours"));
				description  = this.getNodeValueAsString(node.selectSingleNode("description"));
				ticketId     = this.getNodeValueAsInteger(node.selectSingleNode("ticket-id"));
				userId       = this.getNodeValueAsString(node.selectSingleNode("user-id"));
				// overwrite the spaceId received as argument
				spaceId      = this.getNodeValueAsString(node.selectSingleNode("space-id"));

				Task task = new Task(id, beginAt, endAt, hours, description, spaceId, ticketId, number, userId);
				tasks.add(task);
				
			}
		}catch (Exception e){
			throw new XMLParsingException("Error while parsing Tasks XML", e);
		}
		return tasks;
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
	public ArrayList<Task> getTasks() throws RestfulException, XMLParsingException, AssemblaAPIException {
		String url = "https://www.assembla.com/user/time_entries";
		
		String response = request(RequestMethod.GET, url);
		
		if (client.getStatusCode() != 200) {
			String msg = "HTTP Error while retrieving Tasks. Expected status 200 OK, but got " + 
					Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
		
		ArrayList<Task> tasks = new ArrayList<Task>();
		
		int id, ticketId,number;
		Date beginAt = null, endAt = null;
		float hours;
		String description, userId, spaceId;
		
		try {
			Document doc = reader.read( stringToInputStream(response) );
			
			List<Node> nodes =  (List<Node>) doc.selectNodes("/tasks/task");
			
			for(int i=0; i<nodes.size() ; i++) {
				Node node = nodes.get(i);
				
				id           = this.getNodeValueAsInteger(node.selectSingleNode("id"));
				hours        = this.getNodeValueAsFloat(node.selectSingleNode("hours"));
				description  = this.getNodeValueAsString(node.selectSingleNode("description"));
				ticketId     = this.getNodeValueAsInteger(node.selectSingleNode("ticket-id"));
				number	     = this.getNodeValueAsInteger(node.selectSingleNode("ticket-number"));
				userId       = this.getNodeValueAsString(node.selectSingleNode("user-id"));
				// overwrite the spaceId received as argument
				spaceId      = this.getNodeValueAsString(node.selectSingleNode("space-id"));
				
				Task task = new Task(id, beginAt, endAt, hours, description, spaceId, ticketId, number, userId);
				tasks.add(task);
				
			}
		}catch (Exception e){
			throw new XMLParsingException("Error while parsing Tasks XML", e);
		}
		return tasks;
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
			"	<description>" + TextUtils.htmlEncode(task.description()) + "</description>" + 
			"	<begin-at>" + task.beginAt() + "</begin-at>" + 
			"	<end-at>" + task.endAt() + "</end-at>" +   			// 2011-05-04 17:15 UTC 
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
			+ "    <lastlogmessage>" + TextUtils.htmlEncode(task.description()) + "</lastlogmessage>"
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
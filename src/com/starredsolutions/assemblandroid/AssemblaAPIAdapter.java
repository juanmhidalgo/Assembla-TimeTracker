package com.starredsolutions.assemblandroid;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.Node;

import org.dom4j.io.SAXReader;

import android.util.Log;
import com.starredsolutions.assemblandroid.R;
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
import com.starredsolutions.utils.MyTimer;

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
	static private final String TAG = "AssemblaTT";
	private static final String LOG_TAG = AssemblaAPIAdapter.class.getSimpleName();
	
	// Singleton Pattern
	static private AssemblaAPIAdapter instance = null;
	
	static public AssemblaAPIAdapter getInstance() throws XMLParsingException {
		if (instance == null)
			instance = new AssemblaAPIAdapter();
		return instance;
	}
	
	private DocumentBuilderFactory builderFactory;
	private DocumentBuilder builder;
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
	private AssemblaAPIAdapter() throws XMLParsingException {
		
		
		// parse the XML as a W3C Document
		builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new XMLParsingException("FATAL: Was unable to create a new DocumentBuilder. " +
					"You need to restart the application.", e, true);
		}
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
        
        //info.setText("Response : " + Integer.toString(client.getResponseCode())
        //		+ " " + client.getResponse() + "\nErrors: " + client.getErrorMessage());
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
		
		MyTimer.resume("URLRequests");
		String response = request(RequestMethod.GET, url);
		MyTimer.stop("URLRequests");

        if (client.getStatusCode() != 200) {
			String msg = "HTTP Error while retrieving Space. Expected status 200 OK, but got " + 
				Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
        
        String id, name, description;
		try {
//			Document doc = builder.parse( stringToInputStream(response) );
			org.dom4j.Document doc = reader.read( stringToInputStream(response) );
			
			id 			= doc.selectSingleNode("/space/id").getStringValue();
			name        = doc.selectSingleNode("/space/name").getStringValue();
			description = doc.selectSingleNode("/space/description").getStringValue();
			
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
		
		MyTimer.resume("URLRequests");
		String response = request(RequestMethod.GET, url);
		MyTimer.stop("URLRequests");

        if (client.getStatusCode() != 200) {
			String msg = "HTTP Error while retrieving MySpaces. Expected status 200 OK, but got " + 
				Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
        
		ArrayList<Space> spaces = new ArrayList<Space>();
        String id, name, description;
        
        MyTimer timer = MyTimer.start(Space.TIMER_PARSING);
		try {
			Document doc = reader.read( stringToInputStream(response) );
			
			
			List<Node> nodes = (List<Node>) doc.selectNodes("/spaces/space");
			
			for(int i=0; i<nodes.size() ; i++) {
				org.dom4j.Node node = nodes.get(i);
				
				
				id          = node.selectSingleNode("id").getStringValue();
				name        = node.selectSingleNode("name").getStringValue();
				description = node.selectSingleNode("description").getStringValue();
				
				timer.stop();
				
				//Space space = new Space(id, name, description);
				//Log.e("***********", "Space: " + space.toString());
				spaces.add( new Space(id, name, description) );
				
				timer.resume();
			}
			
		}
		catch (Exception e)
		{
			MyTimer.remove(Space.TIMER_PARSING);
			throw new XMLParsingException("Error while parsing MySpaces XML", e);
		}
		finally
		{
			MyTimer.stop(Space.TIMER_PARSING);
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
		// HTTPS unsupported !?!?
		String url = "http://www.assembla.com/user/best_profile/" + username;
		
		MyTimer.resume("URLRequests");
		String response = request(RequestMethod.GET, url);
		MyTimer.stop("URLRequests");

        if (client.getStatusCode() != 200) {
			String msg = "HTTP Error while retrieving UserId. Expected status 200 OK, but got " + 
				Integer.toString(client.getStatusCode()) + " " + client.getStatusPhrase();
			throw new AssemblaAPIException(msg, url, client.getStatusCode(), client.getStatusPhrase(), response);
		}
        
		MyTimer.start("XMLParsingUserProfile");
		try {
			Document doc = reader.read( stringToInputStream(response) );
			
			userId = doc.selectSingleNode("/user/id").getStringValue();
			
			MyTimer.stop("XMLParsingUserProfile");

			//throw new SAXException("dumb sax exception"); // TEMPORARY EXCEPTION
			
			return userId;
			
		} catch (Exception e) {
			throw new XMLParsingException("Error occured while parsing user profile XML", e);
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
		
		if (includeClosed)
			url = "http://www.assembla.com/spaces/" + spaceId + "/tickets/report/0";
		else
			url = "https://www.assembla.com/spaces/" + spaceId + "/tickets";
		
		MyTimer.resume("URLRequests");
		String response = request(RequestMethod.GET, url);
		MyTimer.stop("URLRequests");

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
        
		MyTimer timer = MyTimer.start(Ticket.TIMER_PARSING);
		
		try {
			Document doc = reader.read( stringToInputStream(response) );
			
			List<Node> nodes =  (List<Node>) doc.selectNodes("/tickets/ticket");
			
			for(int i=0; i<nodes.size() ; i++) {
				Node node = nodes.get(i);
				
				// First, check that the ticket belongs to me, or is unassigned
				assignedToId = (String) node.selectSingleNode("assigned-to-id").getStringValue();
				
				// Filter other people tickets if requested
				if (!includeOthers) {
					if ( !assignedToId.equals(userId) && assignedToId.length() > 0)  {
						//Log.i(TAG, "Skipping a ticket assigned to user '" + assignedToId + "' (my ID=" + userId + ")");
						skippedTickets++;
						continue;
						// Loop over to next node
					}
				}
				
				id           = new Integer(node.selectSingleNode("id").getStringValue());
				number       = new Integer(node.selectSingleNode("number").getStringValue());
				priority     = new Integer(node.selectSingleNode("priority").getStringValue());
				/**
				 * @todo el estado puede ser null OJO
				 */
				try{
					status       = new Integer(node.selectSingleNode("status").getStringValue());
				}catch(Exception e){
					e.printStackTrace();
					status = 0;
				}
				statusName   = node.selectSingleNode("status-name").getStringValue();
				description  = node.selectSingleNode("description").getStringValue();
				summary      = node.selectSingleNode("summary").getStringValue();;
				workingHours = new Float(node.selectSingleNode("working-hours").getStringValue());
				
				description  = decodeHTML(description);
				
				// Save workedHours only if the custom-field is present
				String tmpStr = "";
				try{
					tmpStr = (String) node.selectSingleNode("custom-fields/custom-field[@name='workedhours']").getStringValue();
				}catch(Exception e){
					e.printStackTrace();
				}
				
				workedHours   = (tmpStr.length() > 0) ?  new Float(tmpStr) : 0.0f;
				String lastLogMsg = "";
				
				try{
					lastLogMsg = (String) node.selectSingleNode("custom-fields/custom-field[@name='lastlogmessage']").getStringValue();
				}catch(Exception e){
					e.printStackTrace();
				}
				
				// overwrite the spaceId received as argument
				spaceId      = (String) node.selectSingleNode("space-id").getStringValue();
				
				timer.stop();
				
				Ticket ticket = new Ticket(id, number, priority, status, statusName, description, 
						summary, workingHours, workedHours, spaceId, assignedToId, lastLogMsg);
				
				//Log.e("***********", "Ticket: " + ticket.toString());
				tickets.add(ticket);
				
				timer.resume();
			}
			
			// Sort tickets
			Collections.sort(tickets, new TicketComparator());
			tickets.setSkippedItems(skippedTickets);
			
			
		} catch (Exception e)
		{
			MyTimer.remove(Ticket.TIMER_PARSING);
			throw new XMLParsingException("Error while parsing Tickets XML", e);
		}
		finally
		{
			MyTimer.stop(Ticket.TIMER_PARSING);
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
		
		Log.i(LOG_TAG, "calling["+url+"]");
		
		MyTimer.resume("URLRequests");
		String response = request(RequestMethod.GET, url);
		MyTimer.stop("URLRequests");
        
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
		
		MyTimer timer = MyTimer.start(Task.TIMER_PARSING);
        try {
			Document doc = reader.read( stringToInputStream(response) );

			
			List<Node> nodes =  (List<Node>) doc.selectNodes("/ticket/tasks/task");
			
			for(int i=0; i<nodes.size() ; i++) {
				Node node = nodes.get(i);
				
				id           = new Integer(node.selectSingleNode("id").getStringValue());
				
				hours        = new Float(node.selectSingleNode("hours").getStringValue());
				description  = node.selectSingleNode("description").getStringValue();
				ticketId     = new Integer(node.selectSingleNode("ticket-id").getStringValue());
				userId       = node.selectSingleNode("user-id").getStringValue();
				// overwrite the spaceId received as argument
				spaceId      = node.selectSingleNode("space-id").getStringValue();

				timer.stop();
				
				Task task = new Task(id, beginAt, endAt, hours, description, spaceId, ticketId, number, userId);
				//Log.e("***********", "Task: " + task.toString());  // bugs here
				tasks.add(task);
				
				timer.resume();
			}
		}
        catch (Exception e)
		{
        	MyTimer.remove(Task.TIMER_PARSING);
			throw new XMLParsingException("Error while parsing Tasks XML", e);
		}
		finally
		{
			MyTimer.stop(Task.TIMER_PARSING);
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
			"	<description>" + encodeHTML(task.description()) + "</description>" + 
			"	<begin-at>" + task.beginAt() + "</begin-at>" + 
			"	<end-at>" + task.endAt() + "</end-at>" +   			// 2011-05-04 17:15 UTC 
			"	<space-id>" + space.id() + "</space-id>" + 
			"	<ticket-id>" + ticket.id() + "</ticket-id>" + 
			"</task>";
		
		MyTimer.resume("URLRequests");
		String response = request(RequestMethod.POST, url, xml);
		MyTimer.stop("URLRequests");
		
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
			+ "    <lastlogmessage>" + encodeHTML(task.description()) + "</lastlogmessage>"
			+ "  </custom-fields>"
			+ "</ticket>";
		
		MyTimer.resume("URLRequests");
		response = request(RequestMethod.PUT, url, xml);
		MyTimer.stop("URLRequests");
		
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
	
	
	/**
	 * Don't know if it works ; never had to use it!
	 * 
	 * http://stackoverflow.com/questions/2918920/decode-html-entities-in-android
	 * 
	 * @param html
	 * @return String escaped
	 */
	static private String decodeHTML( String html ) {
		return StringEscapeUtils.unescapeHtml( html );
	}
	

	static private String encodeHTML( String text ) {
		return StringEscapeUtils.escapeHtml( text );
	}
	
}

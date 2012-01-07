package com.starredsolutions.assemblandroid.exceptions;


/**
 *  Exception thrown by the AssemblaAPIAdapter when it receives an unexpected answer from the 
 *  RestfulClient.
 *  
 *  For example : http status 422 Unprocessable Entity
 *  
 *  Another possible case could be raised by an UnsupportedEncodingException in 
 *  AssemblaAPIAdapter::stringToInputStream()
 *  
 * @author david
 */
public class AssemblaAPIException extends AbstractTimeTrackerException {
	private static final long serialVersionUID = 1L;
	
	private String url = "";
	private int statusCode = 0;
	private String statusPhrase = "";
	private String response = "";
	
	public String url() { return this.url; }
	public int statusCode() { return this.statusCode; }
	public String statusPhrase() { return this.statusPhrase; }
	public String response() { return this.response; }
	
	
	public AssemblaAPIException(String message, String url, int statusCode, String statusPhrase, String response) {
		super(message);
		this.url = url;
		this.statusCode = statusCode;
		this.statusPhrase = statusPhrase;
		this.response = response;
	}
}

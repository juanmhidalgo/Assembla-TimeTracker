package com.starredsolutions.assemblandroid.exceptions;

/**
 * Exception raised when using XPath. The causes could be :
 * 
 *   - ParserConfigurationException (once on xpath initialisation)  -> fatal
 *   - SAXException
 *   - IOException
 *   - XPathExpressionException
 * 
 * @author david
 */
public class XMLParsingException extends AbstractTimeTrackerException {
	private static final long serialVersionUID = 7085294649545610493L;
	private boolean _fatal = false;
	
	public boolean getFatal() { return _fatal; }

	public XMLParsingException(String detailMessage) {
		super(detailMessage);
	}
	
	public XMLParsingException(String detailMessage, boolean fatal) {
		super(detailMessage);
		_fatal = fatal;
	}
	
	public XMLParsingException(String detailMessage, Throwable cause) {
		super(detailMessage, cause);
	}
	
	public XMLParsingException(String detailMessage, Throwable cause, boolean fatal) {
		super(detailMessage, cause);
		_fatal = fatal;
	}
}

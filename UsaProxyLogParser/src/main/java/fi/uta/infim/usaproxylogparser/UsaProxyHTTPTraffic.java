package fi.uta.infim.usaproxylogparser;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 * 
 * Data model of a "HTTP traffic session" (UsaProxy terminology).
 * Represents a single page view and contains all the events that happen 
 * during the viewing.
 * @author Teemu Pääkkönen
 * 
 */
@XmlSeeAlso({UsaProxyLogEntry.class,UsaProxyHTTPTrafficStartEntry.class,UsaProxyPageEventEntry.class,UsaProxyHTTPTraffic.class,UsaProxySession.class,UsaProxyPageEvent.class,UsaProxyLog.class})
public class UsaProxyHTTPTraffic implements Serializable {

	private final class UsaProxyScreenComparator implements
			Comparator<UsaProxyScreen>, Serializable {
		
		private static final long serialVersionUID = 7274057511229626088L;

		@Override
		public int compare(UsaProxyScreen o1, UsaProxyScreen o2) {
			
			if ( o1.getInitialViewportEvent() == null && o2.getInitialViewportEvent() == null ) return 0;
			if ( o2.getInitialViewportEvent() == null ) return 1;
			if ( o1.getInitialViewportEvent() == null ) return -1;
			
			int tsCompare = o1.getInitialViewportEvent().getEntry().getTimestamp().compareTo(
					o2.getInitialViewportEvent().getEntry().getTimestamp() );
			if ( tsCompare == 0 )
			{
				// if two screens have the exact same timestamps, something is terribly wrong,
				// but we cant really figure out what, so we let the dupe screen pass for now 
				return o1.getID().compareTo(o2.getID());
			}
			return tsCompare;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((session == null) ? 0 : session.hashCode());
		result = prime * result
				+ ((sessionID == null) ? 0 : sessionID.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	/**
	 * Constructor for creating complete http traffic objects at once.
	 * Private. Use the {@link newHTTPTrafficSession} method to create objects.
	 * @param address User's IP address as a string, eg. "10.0.0.1"
	 * @param url The URL that was accessed, as a string, eg. "http://www.hs.fi"
	 * @param sessionID UsaProxy generated HTTP Traffic Index as string, eg. "10993"
	 * @param session The UsaProxy session during which this http traffic occurred
	 */
	private UsaProxyHTTPTraffic( String address, String url, 
			String sessionID, UsaProxySession session, UsaProxyHTTPTrafficStartEntry entry ) {
		super();
		setEntry(entry);
		setAddress( UsaProxyLog.ipAddressStringToInetAddress(address) );
		setUrl(url);
		this.sessionID = Integer.valueOf(sessionID);
		setSession(session);
	}

	/**
	 * Default no-arg constructor. Required during JAXB unmarshalling.
	 */
	public UsaProxyHTTPTraffic() {
		super();
	}

	/**
	 * Creates a new http traffic session object and inserts it into the session
	 * store. Sessions are indexed by the sessionID argument. Therefore,
	 * creating a new http traffic session with an already-existing ID will
	 * overwrite the old session object.
	 * Existing sessions can be accessed through the static methods of
	 * {@link UsaProxySessionStore}.
	 * 
	 * @param address User's IP address as a string, eg. "10.0.0.1"
	 * @param url The URL that was accessed, as a string, eg. "http://www.hs.fi"
	 * @param sessionID UsaProxy generated HTTP Traffic Index as string, eg. "10993"
	 * @return the newly created session object
	 */
	public static UsaProxyHTTPTraffic newHTTPTrafficSession( String address, 
			String url, String sessionID, UsaProxySession session, UsaProxyHTTPTrafficStartEntry entry )
	{
		UsaProxyHTTPTraffic t = new UsaProxyHTTPTraffic( address, url, sessionID, session, entry );
		UsaProxySessionStore.putHTTPTrafficSession(t);
		return t;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UsaProxyHTTPTraffic other = (UsaProxyHTTPTraffic) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (session == null) {
			if (other.session != null)
				return false;
		} else if (!session.equals(other.session))
			return false;
		if (sessionID == null) {
			if (other.sessionID != null)
				return false;
		} else if (!sessionID.equals(other.sessionID))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8243975513255895174L;

	/**
	 * IP address of user
	 */
	private InetAddress address;

	/**
	 * The URL accessed
	 */
	private URL url;
	
	/**
	 * The "HTTP Traffic index" - a unique identifier
	 */
	private Integer sessionID;
	
	/**
	 * The "screens" that have appeared during this http traffic session.
	 * Each screen object represents a single browser viewport state on
	 * the document represented by the {@link #url}.
	 */
	private LinkedList< UsaProxyScreen > screens =
			new LinkedList<UsaProxyScreen>();
	
	/**
	 * The UsaProxy session during which this http traffic occurred
	 */
	private UsaProxySession session;
	
	/**
	 * All the DOM elements that have triggered events during this http traffic
	 */
	private HashSet< UsaProxyDOMElement > domElements =
			new HashSet<UsaProxyDOMElement>();
	
	private UsaProxyHTTPTrafficStartEntry entry;
	
	/**
	 * HTTP headers in the original HTTP request. Parsed from the http traffic
	 * log file that corresponds to the traffic id.
	 */
	private HashMap< String, String > requestHeaders =
			new HashMap< String, String >();
	
	/**
	 * HTTP headers in the original HTTP response. Parsed from the corresponding
	 * http traffic log file.
	 */
	private HashMap< String, String > responseHeaders =
			new HashMap< String, String >();
	
	/**
	 * Returns the user's IP address or null if not set.
	 * @return user's IP address
	 */
	@XmlJavaTypeAdapter( InetAddressXmlAdapter.class )
	@XmlAttribute
	public InetAddress getAddress() {
		return address;
	}

	/**
	 * Setter for the user's IP address
	 * @param address user's IP address
	 */
	public void setAddress(InetAddress address) {
		this.address = address;
	}

	/**
	 * The URL accessed during this http traffic session
	 * @return a URL object
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * Generic setter for the URL accessed during this http traffic session.
	 * @param url the URL object
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * String-based setter for the URL accessed during this HTTP traffic session.
	 * @param url the URL as string, eg. "http://www.hs.fi"
	 */
	public void setUrl(String url) {
		String decodedURL = "";
		try {
			decodedURL = URLDecoder.decode(url, "ISO-8859-1" );
		} catch (UnsupportedEncodingException e1) {
			// Shouldn't happen
			throw new RuntimeException( "Unsupported encoding at URL: " + url );
		}
		try {
			setUrl( new URL( decodedURL ) );
		} catch (MalformedURLException e) {
			// Invalid URLs are critical. Abort.
			throw new RuntimeException( "Invalid URL: " + decodedURL );
		}
	}
	
	/**
	 * Returns the session ID. Session IDs are unique.
	 * @return the HTTP traffic session id (UsaProxy: httptrafficindex)
	 */
	@XmlAttribute( name="id" )
	public Integer getSessionID() {
		return sessionID;
	}

	/**
	 * Generic setter for the http traffic session ID. Identifiers must be unique.
	 * @param sessionID the http traffic session id
	 */
	public void setSessionID(Integer sessionID) {
		this.sessionID = sessionID;
	}

	@XmlElementWrapper
	@XmlElement( name="screen", type=UsaProxyScreen.class )
	public LinkedList< UsaProxyScreen > getScreens() {
		return screens;
	}

	public void setScreens(LinkedList< UsaProxyScreen > screens) {
		this.screens = screens;
	}

	public void sortScreens()
	{
		Collections.sort( this.screens, new UsaProxyScreenComparator() );
	}
	
	@XmlTransient
	public UsaProxySession getSession() {
		return session;
	}

	public void setSession(UsaProxySession session) {
		this.session = session;
		if ( session != null )
		{
			session.getHttpTrafficSessions().add(this);
			if ( entry != null )
			{
				session.testAndSetStart(getEntry().getTimestamp());
			}
		}
	}

	@XmlTransient
	public HashSet< UsaProxyDOMElement > getDomElements() {
		return domElements;
	}

	public void setDomElements(HashSet< UsaProxyDOMElement > domElements) {
		this.domElements = domElements;
	}

	public UsaProxyHTTPTrafficStartEntry getEntry() {
		return entry;
	}

	public void setEntry(UsaProxyHTTPTrafficStartEntry entry) {
		this.entry = entry;
	}

	public HashMap< String, String > getRequestHeaders() {
		return requestHeaders;
	}

	public void setRequestHeaders(HashMap< String, String > requestHeaders) {
		this.requestHeaders = requestHeaders;
	}

	public HashMap< String, String > getResponseHeaders() {
		return responseHeaders;
	}

	public void setResponseHeaders(HashMap< String, String > responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

}

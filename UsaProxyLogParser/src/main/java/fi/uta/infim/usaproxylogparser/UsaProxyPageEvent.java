package fi.uta.infim.usaproxylogparser;

import java.io.Serializable;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Data model of a page event, eg. a DOM event.
 * @author Teemu Pääkkönen
 *
 */
@XmlSeeAlso({UsaProxyLogEntry.class,UsaProxyHTTPTrafficStartEntry.class,UsaProxyPageEventEntry.class,UsaProxyHTTPTraffic.class,UsaProxySession.class,UsaProxyPageEvent.class,UsaProxyLog.class})
public class UsaProxyPageEvent implements Serializable {
	
	public UsaProxyPageEvent(String eventType, HashMap< String, String > attributes,
			String sessionID, String httpTrafficIndex, String ip, UsaProxyPageEventEntry entry) {
		super();
		
		setEntry(entry);
		
		this.setType(eventType);
		this.setSession(sessionID, ip);
		this.setHttpTrafficSession(httpTrafficIndex, ip);
		setDomPath( attributes.get( "dom" ), attributes.get( "nodeName" ) );
		setScreen( attributes.get( "screenID" ) );
		attributes.remove( "dom" );
		attributes.remove( "screenID" );
		attributes.remove( "nodeName" );
		
		setAttributes(attributes);
	}

	public UsaProxyPageEvent() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributes == null) ? 0 : attributes.hashCode());
		result = prime
				* result
				+ ((httpTrafficSession == null) ? 0 : httpTrafficSession
						.hashCode());
		result = prime * result + ((session == null) ? 0 : session.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UsaProxyPageEvent other = (UsaProxyPageEvent) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (httpTrafficSession == null) {
			if (other.httpTrafficSession != null)
				return false;
		} else if (!httpTrafficSession.equals(other.httpTrafficSession))
			return false;
		if (session == null) {
			if (other.session != null)
				return false;
		} else if (!session.equals(other.session))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1956085449312344380L;

	/**
	 * The type of this event. See {@link EventType} for details.
	 */
	private EventType type;

	/**
	 * The event's dom element.
	 * For certain events (eg. "load") this will be empty.
	 */
	private UsaProxyDOMElement domPath;
	
	/**
	 * Contains other attributes of the event in key-value pairs, such as
	 * 'offset'->'1462,398'. Attributes named at parse-time are stored in
	 * separate fields, such as {@link #type}.
	 */
	private HashMap< String, String > attributes;
	
	/**
	 * The event occurred during this session. The session identifies the 
	 * browser instance.
	 */
	private UsaProxySession session;
	
	/**
	 * The HTTP traffic session (page view session) during which this event
	 * occurred. HTTP traffic session identifies a single page view session.
	 */
	private UsaProxyHTTPTraffic httpTrafficSession;

	/**
	 * The ID of the screen on which the element first appeared or where
	 * the item finally disappeared off the screen.
	 */
	private UsaProxyScreen screen;
	
	@XmlTransient
	private UsaProxyPageEventEntry entry;
	
	@XmlAttribute
	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public void setType( String evtype )
	{
		// Find the correct event type by looping the enum values
		for ( EventType type : EventType.values() )
		{
			if ( evtype.toUpperCase().equals( type.toString().toUpperCase() ) )
			{
				this.type = type;
				break;
			}
		}
				
		// If the event type was not found, make noise
		if ( this.type == null )
		{
			throw new RuntimeException( "Invalid event type: " + evtype );
		}
	}

	public HashMap< String, String > getAttributes() {
		return attributes;
	}

	public void setAttributes(HashMap< String, String > attributes) {
		this.attributes = attributes;
	}

	@XmlJavaTypeAdapter( SessionAdapter.class )
	@XmlAttribute
	public UsaProxySession getSession() {
		return session;
	}

	public void setSession(UsaProxySession session) {
		this.session = session;
		if ( session != null )
		{
			session.testAndSetStart(this.getEntry().getTimestamp());
		}
	}

	public void setSession( String sessionId, String ip )
	{
		// Use an existing session if one exists in the session store
		UsaProxySession session = UsaProxySessionStore.getSessionById(sessionId);
		if ( session == null )
		{
			session = UsaProxySession.newSession(sessionId, ip, getEntry().getTimestamp());
		}
		setSession(session);
	}
	
	@XmlTransient
	public UsaProxyHTTPTraffic getHttpTrafficSession() {
		return httpTrafficSession;
	}

	public void setHttpTrafficSession(UsaProxyHTTPTraffic httpTrafficSession) {
		this.httpTrafficSession = httpTrafficSession;
	}

	public void setHttpTrafficSession(String id, String ip) {
		UsaProxyHTTPTraffic httpSession = UsaProxySessionStore.getHTTPTrafficSessionById(id);
		if ( httpSession == null )
		{
			// If session is not found, create a new empty session and hope that
			// another log entry updates the info. (url will be replaced with the string "dummyurl")
			httpSession = UsaProxyHTTPTraffic.newHTTPTrafficSession( ip, "http://dummyurl", id, getSession(), null );
		}
		else
		{
			// Session will be missing if the http traffic object was instantiated by a
			// traffic start entry.
			httpSession.setSession(getSession());
		}
		setHttpTrafficSession(httpSession);
	}

	public UsaProxyDOMElement getDomPath() {
		return domPath;
	}

	public void setDomPath(UsaProxyDOMElement domPath) {
		this.domPath = domPath;
		domPath.getEvents().add(this);
	}
	
	public void setDomPath(String domPath, String nodeName) {
		UsaProxyDOMElement element = UsaProxySessionStore.getDOMElementById( 
				getHttpTrafficSession().getSessionID(),	domPath );
		if ( element == null )
		{
			element = UsaProxyDOMElement.newDOMElement(domPath, getHttpTrafficSession(), nodeName);
		}
		setDomPath(element);
	}
	
	@XmlTransient
	public UsaProxyScreen getScreen() {
		return screen;
	}

	public void setScreen(UsaProxyScreen screen) {
		this.screen = screen;
		screen.getEvents().add(this);
	}
	
	private void setScreen(String screenID) {
		UsaProxyScreen s = UsaProxySessionStore.getScreenById(getHttpTrafficSession().getSessionID(), screenID);
		if ( s == null )
		{
			s = UsaProxyScreen.newScreen( Integer.parseInt(screenID), getHttpTrafficSession() );
		}
		setScreen(s);
	}

	public UsaProxyPageEventEntry getEntry() {
		return entry;
	}

	public void setEntry(UsaProxyPageEventEntry entry) {
		this.entry = entry;
	}

}

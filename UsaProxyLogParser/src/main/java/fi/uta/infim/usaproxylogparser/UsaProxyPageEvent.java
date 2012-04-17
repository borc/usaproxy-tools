/*
 * UsaProxyLogParser - Java API for UsaProxy-fork logs
 *  Copyright (C) 2012 Teemu Pääkkönen - University of Tampere
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fi.uta.infim.usaproxylogparser;

import java.io.Serializable;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import fi.uta.infim.usaproxylogparser.jaxb.SessionAdapter;

/**
 * Data model of a page event, eg. a DOM event.
 * @author Teemu Pääkkönen
 *
 */
@XmlSeeAlso({UsaProxyLogEntry.class,UsaProxyHTTPTrafficStartEntry.class,UsaProxyPageEventEntry.class,UsaProxyHTTPTraffic.class,UsaProxySession.class,UsaProxyPageEvent.class,UsaProxyLog.class})
public class UsaProxyPageEvent implements Serializable {
	
	/**
	 * Constructor for full page event objects
	 * @param eventType event type as logged
	 * @param attributes event attributes as a map of key value pairs
	 * @param sessionID session id as logged
	 * @param httpTrafficIndex http traffic id as logged
	 * @param ip user's IP address as logged (will be parsed)
	 * @param entry the log entry that contains this event
	 */
	UsaProxyPageEvent(String eventType, HashMap< String, String > attributes,
			String sessionID, String httpTrafficIndex, String ip, UsaProxyPageEventEntry entry) {
		super();
		
		setEntry(entry);
		
		this.setType(eventType);
		this.setSession(sessionID, ip);
		this.setHttpTrafficSession(httpTrafficIndex, ip);
		setDomPath( attributes.get( "dom" ), attributes.get( "nodeName" ), attributes.get( "contents" ) );
		setScreen( attributes.get( "screenID" ) );
		attributes.remove( "dom" );
		attributes.remove( "screenID" );
		attributes.remove( "nodeName" );
		attributes.remove( "contents" );
		
		setAttributes(attributes);
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
	
	/**
	 * The log entry that contains this event
	 */
	@XmlTransient
	private UsaProxyPageEventEntry entry;
	
	/**
	 * The event type. See {@link EventType} for details.
	 * @return this event's type
	 */
	@XmlAttribute
	public EventType getType() {
		return type;
	}

	void setType(EventType type) {
		this.type = type;
	}

	/**
	 * Sets event type by string representation. Will throw a runtime exception
	 * if event type is unknown (ie. not known by {@link EventType}).
	 * @param evtype the event name as string
	 */
	void setType( String evtype )
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

	/**
	 * The attributes of this event in key-value pairs. Contains only the
	 * attributes that havent been mapped to member variables.
	 * @return map of event attributes
	 */
	public HashMap< String, String > getAttributes() {
		return attributes;
	}

	void setAttributes(HashMap< String, String > attributes) {
		this.attributes = attributes;
	}

	/**
	 * The session during which this event occurred.
	 * @return session object
	 */
	@XmlJavaTypeAdapter( SessionAdapter.class )
	@XmlAttribute
	public UsaProxySession getSession() {
		return session;
	}

	void setSession(UsaProxySession session) {
		this.session = session;
		if ( session != null )
		{
			session.testAndSetStart(this.getEntry().getTimestamp());
		}
	}

	/**
	 * Sets session by session id and ip address. Makes sure that an existing
	 * session is used if one exists in the session store. Otherwise creates
	 * a new one. 
	 * @param sessionId session id, as logged
	 * @param ip user's ip address, as logged
	 */
	void setSession( String sessionId, String ip )
	{
		// Use an existing session if one exists in the session store
		UsaProxySession session = UsaProxySessionStore.getSessionById(sessionId);
		if ( session == null )
		{
			session = UsaProxySession.newSession(sessionId, ip, getEntry().getTimestamp());
		}
		setSession(session);
	}
	
	/**
	 * The HTTP traffic session during which this event occurred.
	 * @return http traffic session of this event
	 */
	@XmlTransient
	public UsaProxyHTTPTraffic getHttpTrafficSession() {
		return httpTrafficSession;
	}

	void setHttpTrafficSession(UsaProxyHTTPTraffic httpTrafficSession) {
		this.httpTrafficSession = httpTrafficSession;
	}

	/**
	 * Sets HTTP traffic session by id and user's ip. Makes sure that an
	 * existing HTTP traffic session is used if one exists in the session
	 * store. If not, a new one will be created. Note that a HTTP traffic
	 * session should contain a URL as well, but event entries don't know
	 * the URL. Therefore a dummy url (http://dummyurl) is used instead.
	 * @param id HTTP traffic id, as logged
	 * @param ip user's ip address, as logged
	 */
	void setHttpTrafficSession(String id, String ip) {
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

	/**
	 * The DOM element whose context this event occurred in.
	 * @return a dom element
	 */
	public UsaProxyDOMElement getDomPath() {
		return domPath;
	}

	void setDomPath(UsaProxyDOMElement domPath) {
		this.domPath = domPath;
		domPath.getEvents().add(this);
	}
	
	/**
	 * Sets the DOM element referenced by this event with logged values. If 
	 * the element already exists in the session store, the existing one will
	 * be used instead. If not, a new one will be created. However, this
	 * method updates the contents of the element unless the parameter is null.
	 * @param domPath the dom path, as logged
	 * @param nodeName node name as logged
	 * @param contents element contents as a string - can be null
	 */
	void setDomPath(String domPath, String nodeName, String contents) {
		UsaProxyDOMElement element = UsaProxySessionStore.getDOMElementById( 
				getHttpTrafficSession().getSessionID(),	domPath );
		if ( element == null )
		{
			element = UsaProxyDOMElement.newDOMElement(domPath, getHttpTrafficSession(), nodeName, contents);
		}
		else
		{
			// Element contents can be missing in some cases. Set if found.
			// Note that this leads to the element object only knowing the last
			// contents logged.
			if ( contents != null )
			{
				element.setContents(contents);
			}
		}
		setDomPath(element);
	}
	
	/**
	 * The screen during which this event occurred.
	 * @return screen object
	 */
	@XmlTransient
	public UsaProxyScreen getScreen() {
		return screen;
	}

	void setScreen(UsaProxyScreen screen) {
		this.screen = screen;
		screen.getEvents().add(this);
	}
	
	/**
	 * Sets screen using the screen id. Makes sure that an existing screen is
	 * used if one exists in the session store. If not, a new one is created.
	 * @param screenID
	 */
	void setScreen(String screenID) {
		UsaProxyScreen s = UsaProxySessionStore.getScreenById(getHttpTrafficSession().getSessionID(), screenID);
		if ( s == null )
		{
			s = UsaProxyScreen.newScreen( Integer.parseInt(screenID), getHttpTrafficSession() );
		}
		setScreen(s);
	}

	/**
	 * The log entry that contains this event.
	 * @return event's log entry
	 */
	public UsaProxyPageEventEntry getEntry() {
		return entry;
	}

	void setEntry(UsaProxyPageEventEntry entry) {
		this.entry = entry;
	}

}

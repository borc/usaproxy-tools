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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A UsaProxy "screen". A screen is a single unique state of the browser
 * viewport. Every element appears and disappears within a screen, and all
 * events happen within a screen. A screen is contained in a HTTP
 * traffic session. A single HTTP traffic session can contain multiple screens.
 * @author Teemu Pääkkönen
 *
 */
public class UsaProxyScreen implements Serializable {

	protected UsaProxyScreen() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((screenID == null) ? 0 : screenID.hashCode());
		result = prime
				* result
				+ ((httpTrafficSession == null) ? 0 : httpTrafficSession
						.hashCode());
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
		UsaProxyScreen other = (UsaProxyScreen) obj;
		if (screenID == null) {
			if (other.screenID != null)
				return false;
		} else if (!screenID.equals(other.screenID))
			return false;
		if (httpTrafficSession == null) {
			if (other.httpTrafficSession != null)
				return false;
		} else if (!httpTrafficSession.equals(other.httpTrafficSession))
			return false;
		return true;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8795671281132159416L;

	/**
	 * Private constructor. Use {@link #newScreen(Integer, UsaProxyHTTPTraffic)}
	 * for creating objects of this class.
	 * @param iD id number of the screen
	 */
	private UsaProxyScreen(Integer iD ) {
		super();
		screenID = iD;
	}

	/**
	 * Creates a new screen object and places it in the session store.
	 * @param id screen's id number
	 * @param httpTrafficSession the containing http traffic session
	 * @return the newly created screen object
	 */
	static UsaProxyScreen newScreen( Integer id, UsaProxyHTTPTraffic httpTrafficSession )
	{
		UsaProxyScreen screen = new UsaProxyScreen(id );
		UsaProxySessionStore.putScreen(screen, httpTrafficSession);
		return screen;
	}
	
	/**
	 * Every screenful has a per-httpTraffic unique ID.
	 */
	private Integer screenID;
	
	/**
	 * The http traffic session this screenful is tied to.
	 */
	private UsaProxyHTTPTraffic httpTrafficSession;

	/**
	 * All the events that happened in this screen.
	 */
	private List< UsaProxyPageEvent > events =
			new ArrayList<UsaProxyPageEvent>();
	
	/**
	 * The viewport change event that initialized this screen.
	 */
	private UsaProxyViewportChangeEvent initialViewportEvent;
	
	/**
	 * The scroll start event that marks the end point of this screen.
	 * Can be null if no scrolling happened. In that case, either:
	 * 1. the HTTP traffic session ended here, or
	 * 2. the browser window was resized
	 */
	private UsaProxyScrollStartEvent scrollStart;
	
	/**
	 * The identifier of this screen. Unique.
	 * @return unique identifier of this screen
	 */
	@XmlAttribute
	public Integer getScreenID() {
		return screenID;
	}

	void setScreenID(Integer screenID) {
		this.screenID = screenID;
	}

	/**
	 * The HTTP traffic session during which this screen was seen.
	 * @return a http traffic session object
	 */
	@XmlTransient
	public UsaProxyHTTPTraffic getHttpTrafficSession() {
		return httpTrafficSession;
	}

	void setHttpTrafficSession(UsaProxyHTTPTraffic httpTrafficSession) {
		this.httpTrafficSession = httpTrafficSession;
		httpTrafficSession.getScreens().add(this);
	}

	/**
	 * All the events that occurred within this screen. May be empty.
	 * @return list of events
	 */
	@XmlElements({ 
		@XmlElement( name="event", type=UsaProxyPageEvent.class ),
		@XmlElement( name="appearance", type=UsaProxyAppearanceEvent.class ),
		@XmlElement( name="disappearance", type=UsaProxyDisappearanceEvent.class ),
		@XmlElement( name="initialViewport", type=UsaProxyViewportChangeEvent.class ),
		@XmlElement( name="scrollstart", type=UsaProxyScrollStartEvent.class )})
	public List< UsaProxyPageEvent > getEvents() {
		return events;
	}

	void setEvents(List< UsaProxyPageEvent > events) {
		this.events = events;
	}

	/**
	 * The event that initialized this screen.
	 * @return screen's initializing event
	 */
	@XmlTransient
	public UsaProxyViewportChangeEvent getInitialViewportEvent() {
		return initialViewportEvent;
	}

	void setInitialViewportEvent(UsaProxyViewportChangeEvent initialViewportEvent) {
		this.initialViewportEvent = initialViewportEvent;
	}

	/**
	 * The scroll start event that marks the end point of this screen.
	 * Can be null if no scrolling happened. In that case, either:
	 * 1. the HTTP traffic session ended here, or
	 * 2. the browser window was resized 
	 * @return scroll start event, or null
	 */
	@XmlTransient
	public UsaProxyScrollStartEvent getScrollStart() {
		return scrollStart;
	}

	void setScrollStart(UsaProxyScrollStartEvent scrollStart) {
		this.scrollStart = scrollStart;
	}
	
	/**
	 * Returns the surrogate ID. Usually null, unless loaded from database.
	 * @return surrogate ID
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the surrogate ID. You should avoid using this unless you need to
	 * manage IDs manually.
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Surrogate ID. Null, unless object is loaded from a database.
	 */
	private Long id;
}

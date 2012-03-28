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

import java.util.HashMap;

/**
 * A single element's appearance event. Appearances are not logged for all
 * elements unless UsaProxy is set up that way.
 * @author Teemu Pääkkönen
 *
 */
public class UsaProxyAppearanceEvent extends UsaProxyVisibilityEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5560586912788697846L;

	/**
	 * No-argument constructor for JAXB. Use is not recommended.
	 */
	public UsaProxyAppearanceEvent() {
		super();
	}

	/**
	 * Constructs a full appearance event object and adds the event to the dom
	 * element's appearance list.
	 * @param eventType event type as string (as logged)
	 * @param attributes other event attributes as a map
	 * @param sessionID the session ID (as logged)
	 * @param httpTrafficIndex the HTTP traffic id (as logged)
	 * @param ip user's IP address as logged
	 * @param entry the log entry object that contains this event
	 */
	UsaProxyAppearanceEvent(String eventType,
			HashMap<String, String> attributes, String sessionID,
			String httpTrafficIndex, String ip, UsaProxyPageEventEntry entry ) {
		super(eventType, attributes, sessionID, httpTrafficIndex, ip, entry);
		
		getDomPath().getAppears().add(this);
	}

	
}

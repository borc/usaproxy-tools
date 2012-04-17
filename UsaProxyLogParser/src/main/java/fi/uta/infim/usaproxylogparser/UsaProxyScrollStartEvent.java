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
 * A scroll start event is triggered when the user starts scrolling a page.
 * A "scroll stop" event is triggered when the scrolling is stopped.
 * @author Teemu Pääkkönen
 *
 */
public class UsaProxyScrollStartEvent extends UsaProxyPageEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7484995661351167897L;

	/**
	 * Constructs a full scroll start event.
	 * @param eventType event type name as logged
	 * @param attributes map of event attributes, key-value pairs 
	 * @param sessionID session id as logged
	 * @param httpTrafficIndex http traffic id as logged
	 * @param ip user's IP address as logged
	 * @param entry the log entry that contains this event
	 */
	UsaProxyScrollStartEvent(String eventType,
			HashMap<String, String> attributes, String sessionID,
			String httpTrafficIndex, String ip, UsaProxyPageEventEntry entry) {
		super(eventType, attributes, sessionID, httpTrafficIndex, ip, entry);
		
		getScreen().setScrollStart(this); // Only one scroll start per screen can exist
	}

}

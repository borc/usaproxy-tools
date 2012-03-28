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
 * A single element's disappearance event. Disappearances are only logged for
 * certain elements that UsaProxy is set up to watch.
 * @author Teemu Pääkkönen
 *
 */
public class UsaProxyDisappearanceEvent extends UsaProxyVisibilityEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6698231841198500351L;

	/**
	 * no-arg constructor for JAXB. Do not use.
	 */
	public UsaProxyDisappearanceEvent() {
		super();
	}

	/**
	 * Constructs a full disappearance event object and adds it to the DOM
	 * element's disappearance list.
	 * @param eventType event type as logged
	 * @param attributes other event attributes as a map
	 * @param sessionID session id as logged
	 * @param httpTrafficIndex HTTP traffic id as logged
	 * @param ip user's IP address as logged
	 * @param entry the log entry that contains this event
	 */
	UsaProxyDisappearanceEvent(String eventType,
			HashMap<String, String> attributes, String sessionID,
			String httpTrafficIndex, String ip, UsaProxyPageEventEntry entry) {
		super(eventType, attributes, sessionID, httpTrafficIndex, ip, entry);

		getDomPath().getDisappears().add(this);
	}

}

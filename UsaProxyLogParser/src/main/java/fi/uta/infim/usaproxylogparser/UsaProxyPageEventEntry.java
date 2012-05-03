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

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A page event log entry. Can be a DOM event or some other event such as 'scroll'.
 * @author Teemu Pääkkönen
 *
 */
@XmlSeeAlso({UsaProxyLogEntry.class,UsaProxyHTTPTrafficStartEntry.class,UsaProxyPageEventEntry.class,UsaProxyHTTPTraffic.class,UsaProxySession.class,UsaProxyPageEvent.class,UsaProxyLog.class})
public class UsaProxyPageEventEntry extends UsaProxyLogEntry implements Serializable {

	protected UsaProxyPageEventEntry() {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2933150130413400056L;
	
	/**
	 * Constructor for a full entry object. Left public for parser implementations.
	 * @param ip user's ip address, as logged
	 * @param timestamp log timestamp, as logged
	 * @param index HTTP traffic id, as logged
	 * @param sessionId session id, as logged
	 * @param event event's name, as logged
	 * @param attributes map of event attributes in key-value pairs
	 */
	public UsaProxyPageEventEntry(String ip,
			String timestamp, String index, String sessionId, String event,
			HashMap< String, String > attributes ) {
		super( timestamp );
		EventType eventtype = EventType.fromValue(event);
		if ( eventtype.equals( EventType.APPEAR ) )
		{
			this.event = new UsaProxyAppearanceEvent( event, attributes, 
					sessionId, index, ip, this );
		}
		else if ( eventtype.equals( EventType.DISAPPEAR ) )
		{
			this.event = new UsaProxyDisappearanceEvent( event, attributes, 
					sessionId, index, ip, this );
		}
		else if ( eventtype.equals( EventType.VIEWPORTCHANGE ) )
		{
			this.event = new UsaProxyViewportChangeEvent( event, attributes, 
					sessionId, index, ip, this );
		}
		else if ( eventtype.equals( EventType.SCROLLSTART ) )
		{
			this.event = new UsaProxyScrollStartEvent( event, attributes, 
					sessionId, index, ip, this );
		}
		else
		{
			this.event = new UsaProxyPageEvent( event, attributes, sessionId, index, ip, this );
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((event == null) ? 0 : event.hashCode());
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
		UsaProxyPageEventEntry other = (UsaProxyPageEventEntry) obj;
		if (event == null) {
			if (other.event != null)
				return false;
		} else if (!event.equals(other.event))
			return false;
		return true;
	}

	/**
	 * The event described by this log entry. Each page event log entry 
	 * describes exactly one page event.
	 */
	private UsaProxyPageEvent event;

	/**
	 * The event that is described by this log entry.
	 * @return event described in this entry
	 */
	@XmlTransient
	public UsaProxyPageEvent getEvent() {
		return event;
	}

	void setEvent(UsaProxyPageEvent event) {
		this.event = event;
	}
	
}

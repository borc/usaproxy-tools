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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Data model of a single log entry (one line).
 * Abstract since there are different kinds of lines with some similarities.
 * Extends CommonTree in order to be used in the ANTLR parser.
 * @author Teemu Pääkkönen
 *
 */
@XmlSeeAlso({UsaProxyLogEntry.class,UsaProxyHTTPTrafficStartEntry.class,UsaProxyPageEventEntry.class,UsaProxyHTTPTraffic.class,UsaProxySession.class,UsaProxyPageEvent.class,UsaProxyLog.class})
public abstract class UsaProxyLogEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7121100953768617501L;
	
	/**
	 * The date time format used in UsaProxy logs.
	 */
	private static final SimpleDateFormat usaProxyTSFormat = 
			new SimpleDateFormat( "yyyy-MM-dd,HH:mm:ss.SSS" );

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
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
		UsaProxyLogEntry other = (UsaProxyLogEntry) obj;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

	/**
	 * Constructor for log entries.
	 * @param timestamp the timestamp of this log entry, as logged
	 */
	UsaProxyLogEntry(String timestamp) {
		super();
		try {
			this.timestamp = usaProxyTSFormat.parse( timestamp );
		} catch (ParseException e) {
			// Invalid time stamp is a critical error
			throw new RuntimeException( "Invalid timestamp: " + timestamp );
		}
	}

	/**
	 * No-arg JAXB constructor. Do not use.
	 */
	public UsaProxyLogEntry() {
		super();
	}

	/**
	 * The exact time when the action described in this log entry happened.
	 */
	private Date timestamp;

	/**
	 * The time stamp of this log entry. Doesn't necessarily represent the 
	 * exact moment when the event described in the entry occurred.
	 * @return time stamp of log entry
	 */
	@XmlAttribute
	public Date getTimestamp() {
		return timestamp;
	}

	void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}

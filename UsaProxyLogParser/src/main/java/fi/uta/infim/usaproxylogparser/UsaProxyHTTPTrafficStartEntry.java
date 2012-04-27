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

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;


/**
 * 
 * HTTP Traffic session start event (log entry).
 * @author Teemu Pääkkönen
 *
 */
@XmlSeeAlso({UsaProxyLogEntry.class,UsaProxyHTTPTrafficStartEntry.class,UsaProxyPageEventEntry.class,UsaProxyHTTPTraffic.class,UsaProxySession.class,UsaProxyPageEvent.class,UsaProxyLog.class})
public class UsaProxyHTTPTrafficStartEntry extends UsaProxyLogEntry implements Serializable {


	protected UsaProxyHTTPTrafficStartEntry() {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3384448643279934410L;

	/**
	 * The HTTP traffic session initiated by this event.
	 */
	private UsaProxyHTTPTraffic httpTrafficSession;

	/**
	 * Constructor. Left public for different log parser implementations.
	 * @param address user's IP address as logged
	 * @param url the url accessed as logged
	 * @param sessionID session id as logged
	 * @param timestamp timestamp as logged
	 */
	public UsaProxyHTTPTrafficStartEntry( String address, String url, 
			String sessionID, String timestamp ) {
		super( timestamp );
		setHttpTrafficSession(address, url, sessionID);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UsaProxyHTTPTrafficStartEntry other = (UsaProxyHTTPTrafficStartEntry) obj;
		if (httpTrafficSession == null) {
			if (other.httpTrafficSession != null)
				return false;
		} else if (!httpTrafficSession.equals(other.httpTrafficSession))
			return false;
		return true;
	}

	/**
	 * The HTTP traffic session that was initialized by this entry.
	 * @return this entry's http traffic session object
	 */
	@XmlTransient
	public UsaProxyHTTPTraffic getHttpTrafficSession() {
		return httpTrafficSession;
	}

	void setHttpTrafficSession(UsaProxyHTTPTraffic httpTrafficSession) {
		this.httpTrafficSession = httpTrafficSession;
	}
	
	/**
	 * A special setter for the http traffic session. Makes sure that an existing
	 * session is used if one corresponding to the session ID exists in the 
	 * session store. Otherwise creates a new one.
	 * @param address user's IP address as logged
	 * @param url the url accessed, as logged
	 * @param sessionID session id as logged
	 */
	void setHttpTrafficSession(String address, String url, String sessionID) {
		UsaProxyHTTPTraffic htSession = UsaProxySessionStore.getHTTPTrafficSessionById(sessionID);
		if ( htSession == null )
		{
			// Inserting a null session, since it is not known yet
			htSession = UsaProxyHTTPTraffic.newHTTPTrafficSession(address, url, sessionID, null, this);
		}
		else
		{
			// URL will be missing if the http traffic session already exists,
			// since it must have been instantiated by a page event entry.
			htSession.setUrl( url );
			htSession.setEntry(this);
		}
		setHttpTrafficSession(htSession);
	}
	
}

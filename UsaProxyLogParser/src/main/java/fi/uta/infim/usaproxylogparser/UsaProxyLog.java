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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Root node for a parsed UsaProxy 2.0 log.
 * @author Teemu Pääkkönen
 * 
 * 
 */
@XmlRootElement
@XmlSeeAlso({UsaProxyLogEntry.class,UsaProxyHTTPTrafficStartEntry.class,UsaProxyPageEventEntry.class,UsaProxyHTTPTraffic.class,UsaProxySession.class,UsaProxyPageEvent.class,UsaProxyLog.class})
public class UsaProxyLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 232316961022187562L;

	/**
	 * All the log entries of this log. Note that no duplicate entries may exist.
	 */
	protected Collection< UsaProxyLogEntry > entries;

	/**
	 * The UsaProxy sessions contained in this log.
	 */
	private Collection< UsaProxySession > sessions;
	
	/**
	 * Regular constructor. Left public for parser implementations.
	 * @param entries the log entries contained in this log.
	 */
	public UsaProxyLog(ArrayList<UsaProxyLogEntry> entries) {
		super();
		this.entries = entries;
		UsaProxySessionStore.assignSessionsTo(this);
	}

	/**
	 * All log entries contained in this log. May be empty.
	 * @return list of log entries
	 */
	@XmlTransient
	public Collection< UsaProxyLogEntry > getEntries() {
		return entries;
	}

	void setEntries(Collection< UsaProxyLogEntry > entries) {
		this.entries = entries;
	}
	
	/**
	 * Takes an IP address fragment string (eg. "1" or "255") and converts it to byte.
	 * Works for IPv4 fragments in decimal format only.
	 * @param fragment the fragment to convert
	 * @return the fragment as a byte
	 */
	private static byte ipFragmentToByte( String fragment )
	{
		return (byte) Integer.parseInt(fragment);
	}
	
	/**
	 * Takes an IPv4 address string and converts it to InetAddress
	 * @param ip address as string
	 * @return supplied address as an InetAddress
	 */
	public static InetAddress ipAddressStringToInetAddress( String ip )
	{
		String[] addressParts = ip.split( "\\." );
		byte[] ipAddressArray = new byte[]{ ipFragmentToByte( addressParts[ 0 ] ),
				ipFragmentToByte( addressParts[ 1 ] ), 
				ipFragmentToByte( addressParts[ 2 ] ),
				ipFragmentToByte( addressParts[ 3 ] ) };
		try {
			return InetAddress.getByAddress( ipAddressArray );
		} catch (UnknownHostException e) {
			// In case the address is unknown (unlikely), null will be
			// returned instead. The IP address part is not critical.
			return null;
		}
	}

	/**
	 * A read-only view to the sessions collection.
	 * @return an unmodifiable collection containing all current usaproxy sessions
	 */
	@XmlElement( name="session" )
	public Collection< UsaProxySession > getSessions() {
		return Collections.unmodifiableCollection( sessions );
	}

	void setSessions(Collection< UsaProxySession > sessions) {
		this.sessions = sessions;
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

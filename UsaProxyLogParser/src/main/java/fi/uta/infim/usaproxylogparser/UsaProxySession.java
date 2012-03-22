package fi.uta.infim.usaproxylogparser;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import fi.uta.infim.usaproxylogparser.jaxb.InetAddressXmlAdapter;

/**
 * Data model of UsaProxy 2.0 session. A session consists of a user's IP
 * address and one or more HTTP traffic sessions.
 * @author Teemu Pääkkönen
 * 
 */
@XmlSeeAlso({UsaProxyLogEntry.class,UsaProxyHTTPTrafficStartEntry.class,UsaProxyPageEventEntry.class,UsaProxyHTTPTraffic.class,UsaProxySession.class,UsaProxyPageEvent.class,UsaProxyLog.class})
public class UsaProxySession implements Serializable {

	/**
	 * Compares http traffic session objects. Orders by time using 
	 * {@link java.util.Date#compareTo(Date)}. If comparison by time fails,
	 * objects will be compared by http traffic id using 
	 * {@link java.lang.Integer#compareTo(Integer)}.
	 * @author Teemu Pääkkönen
	 *
	 */
	private final class HTTPTrafficComparator implements
			Comparator<UsaProxyHTTPTraffic> {
		@Override
		public int compare(UsaProxyHTTPTraffic o1,
				UsaProxyHTTPTraffic o2) {
			int tsCompare = 0;
			try
			{
				tsCompare = o1.getEntry().getTimestamp().compareTo(
					o2.getEntry().getTimestamp() );
			}
			catch ( NullPointerException e )
			{
				tsCompare = 0; // set to 0 to force session id comparison
			}
			if ( tsCompare == 0 )
			{
				tsCompare = o1.getSessionID().compareTo( o2.getSessionID() );
			}
			return tsCompare;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5827063936997297562L;

	/**
	 * Constructs a session object using values from the log.
	 * @param sessionID session id as logged
	 * @param address user's IP address as logged
	 * @param start session start time (timestamp of first log entry in this session)
	 */
	private UsaProxySession(String sessionID, String address, Date start) {
		super();
		this.sessionID = sessionID;
		setAddress( UsaProxyLog.ipAddressStringToInetAddress(address) );
		setStart(start);
	}

	/**
	 * No-arg constructor for JAXB. Do not use.
	 */
	public UsaProxySession() {
		super();
	}

	/**
	 * Creates a new usaproxy session object and inserts it in the session store.
	 * @param sessionID identifier
	 * @param address user's ip address
	 * @return the created object
	 */
	public static UsaProxySession newSession( String sessionID, String address, Date start )
	{
		UsaProxySession s = new UsaProxySession(sessionID, address, start );
		UsaProxySessionStore.putSession(s);
		return s;
	}
	
	/**
	 * Session ID. A string such as 'FLmtgNQHaVj7'. Unique.
	 * Can be used to identify the browser instance.
	 */
	private String sessionID;
	
	/**
	 * IP address of user.
	 */
	private InetAddress address;

	/**
	 * The HTTP traffics contained in this session. Each HTTP traffic
	 * describes a single page load. Orderable by timestamp with 
	 * {@link HTTPTrafficComparator}.
	 */
	private HashSet< UsaProxyHTTPTraffic > httpTrafficSessions =
			new HashSet<UsaProxyHTTPTraffic>();
	
	/**
	 * Timestamp of the first log entry during this session.
	 */
	private Date start;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result
				+ ((sessionID == null) ? 0 : sessionID.hashCode());
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
		UsaProxySession other = (UsaProxySession) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (sessionID == null) {
			if (other.sessionID != null)
				return false;
		} else if (!sessionID.equals(other.sessionID))
			return false;
		return true;
	}

	@XmlAttribute
	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	@XmlJavaTypeAdapter( InetAddressXmlAdapter.class )
	@XmlAttribute
	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	@XmlElement( name="httpTraffic" )
	public HashSet< UsaProxyHTTPTraffic > getHttpTrafficSessions() {
		return httpTrafficSessions;
	}

	public void setHttpTrafficSessions(HashSet< UsaProxyHTTPTraffic > httpTrafficSessions) {
		this.httpTrafficSessions = httpTrafficSessions;
	}

	/**
	 * Creates a sorted copy of the http traffic sessions list contained within
	 * this session.
	 * @return a sorted copy of http traffic session list
	 */
	public ArrayList< UsaProxyHTTPTraffic > getSortedHttpTrafficSessions()
	{
		ArrayList< UsaProxyHTTPTraffic > list = 
				new ArrayList< UsaProxyHTTPTraffic >( this.httpTrafficSessions );
		Collections.sort( list, new HTTPTrafficComparator() );
		return list;
	}
	
	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}
	
	/**
	 * Sets start time according to parameter, if existing value is null
	 * or represents a later time than the parameter.
	 * @param start timestamp to test against
	 */
	public void testAndSetStart(Date start) {
		if ( this.start == null || start.before(this.start) )
		{
			this.start = start;
		}
	}
}

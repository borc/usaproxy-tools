package fi.uta.infim.usaproxylogparser;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Data model of UsaProxy 2.0 session
 * @author Teemu Pääkkönen
 * 
 */
@XmlSeeAlso({UsaProxyLogEntry.class,UsaProxyHTTPTrafficStartEntry.class,UsaProxyPageEventEntry.class,UsaProxyHTTPTraffic.class,UsaProxySession.class,UsaProxyPageEvent.class,UsaProxyLog.class})
public class UsaProxySession implements Serializable {

	private final class HTTPTrafficComparator implements
			Comparator<UsaProxyHTTPTraffic> {
		@Override
		public int compare(UsaProxyHTTPTraffic o1,
				UsaProxyHTTPTraffic o2) {
			int tsCompare = o1.getEntry().getTimestamp().compareTo(
					o2.getEntry().getTimestamp() );
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

	private UsaProxySession(String sessionID, String address, Date start) {
		super();
		this.sessionID = sessionID;
		setAddress( UsaProxyLog.ipAddressStringToInetAddress(address) );
		setStart(start);
	}

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
	private LinkedList< UsaProxyHTTPTraffic > httpTrafficSessions =
			new LinkedList<UsaProxyHTTPTraffic>();
	
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
	public LinkedList< UsaProxyHTTPTraffic > getHttpTrafficSessions() {
		return httpTrafficSessions;
	}

	public void setHttpTrafficSessions(LinkedList< UsaProxyHTTPTraffic > httpTrafficSessions) {
		this.httpTrafficSessions = httpTrafficSessions;
	}

	public void sortHttpTrafficSessions()
	{
		Collections.sort( this.httpTrafficSessions, new HTTPTrafficComparator() );
	}
	
	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}
	
	public void testAndSetStart(Date start) {
		if ( this.start == null || start.before(this.start) )
		{
			this.start = start;
		}
	}
}

package fi.uta.infim.usaproxylogparser;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

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
	protected Vector< UsaProxyLogEntry > entries;

	/**
	 * The UsaProxy sessions contained in this log.
	 */
	private Collection< UsaProxySession > sessions;
	
	public UsaProxyLog() {
		super();
	}

	public UsaProxyLog(Vector<UsaProxyLogEntry> entries) {
		super();
		this.entries = entries;
		UsaProxySessionStore.assignSessionsTo(this);
	}

	@XmlTransient
	public Vector< UsaProxyLogEntry > getEntries() {
		return entries;
	}

	public void setEntries(Vector< UsaProxyLogEntry > entries) {
		this.entries = entries;
	}
	
	public static InetAddress ipAddressStringToInetAddress( String ip )
	{
		String[] addressParts = ip.split( "\\." );
		byte[] ipAddressArray = new byte[]{ Byte.parseByte( addressParts[ 0 ] ),
				Byte.parseByte( addressParts[ 1 ] ), 
				Byte.parseByte( addressParts[ 2 ] ),
				Byte.parseByte( addressParts[ 3 ] ) };
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
	 * @return
	 */
	@XmlElement( name="session" )
	public Collection< UsaProxySession > getSessions() {
		return Collections.unmodifiableCollection( sessions );
	}

	public void setSessions(Collection< UsaProxySession > sessions) {
		this.sessions = sessions;
	}
}

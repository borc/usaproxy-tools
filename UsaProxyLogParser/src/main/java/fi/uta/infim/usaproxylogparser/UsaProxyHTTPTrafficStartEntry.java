package fi.uta.infim.usaproxylogparser;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;


/**
 * 
 * HTTP Traffic session start event (log entry).
 * @author Teemu Pääkkönen
 *
 */
@XmlSeeAlso({UsaProxyLogEntry.class,UsaProxyHTTPTrafficStartEntry.class,UsaProxyPageEventEntry.class,UsaProxyHTTPTraffic.class,UsaProxySession.class,UsaProxyPageEvent.class,UsaProxyLog.class})
public class UsaProxyHTTPTrafficStartEntry extends UsaProxyLogEntry {

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

	/**
	 * No-arg constructor for JAXB. Do not use.
	 */
	public UsaProxyHTTPTrafficStartEntry() {
		super();
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

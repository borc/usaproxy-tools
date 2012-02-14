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

	public UsaProxyHTTPTrafficStartEntry( String address, String url, 
			String sessionID, String timestamp ) {
		super( timestamp );
		setHttpTrafficSession(address, url, sessionID);
	}

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

	public void setHttpTrafficSession(UsaProxyHTTPTraffic httpTrafficSession) {
		this.httpTrafficSession = httpTrafficSession;
	}
	
	public void setHttpTrafficSession(String address, String url, String sessionID) {
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

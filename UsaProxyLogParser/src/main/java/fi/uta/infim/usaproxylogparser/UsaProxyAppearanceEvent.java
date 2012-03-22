package fi.uta.infim.usaproxylogparser;

import java.util.HashMap;

/**
 * A single element's appearance event. Appearances are not logged for all
 * elements unless UsaProxy is set up that way.
 * @author Teemu Pääkkönen
 *
 */
public class UsaProxyAppearanceEvent extends UsaProxyVisibilityEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5560586912788697846L;

	/**
	 * No-argument constructor for JAXB. Use is not recommended.
	 */
	public UsaProxyAppearanceEvent() {
		super();
	}

	/**
	 * Constructs a full appearance event object and adds the event to the dom
	 * element's appearance list.
	 * @param eventType event type as string (as logged)
	 * @param attributes other event attributes as a map
	 * @param sessionID the session ID (as logged)
	 * @param httpTrafficIndex the HTTP traffic id (as logged)
	 * @param ip user's IP address as logged
	 * @param entry the log entry object that contains this event
	 */
	UsaProxyAppearanceEvent(String eventType,
			HashMap<String, String> attributes, String sessionID,
			String httpTrafficIndex, String ip, UsaProxyPageEventEntry entry ) {
		super(eventType, attributes, sessionID, httpTrafficIndex, ip, entry);
		
		getDomPath().getAppears().add(this);
	}

	
}

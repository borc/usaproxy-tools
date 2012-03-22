package fi.uta.infim.usaproxylogparser;

import java.util.HashMap;

/**
 * A scroll start event is triggered when the user starts scrolling a page.
 * A "scroll stop" event is triggered when the scrolling is stopped.
 * @author Teemu Pääkkönen
 *
 */
public class UsaProxyScrollStartEvent extends UsaProxyPageEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7484995661351167897L;

	/**
	 * no-arg constructor for JAXB. Do not use.
	 */
	public UsaProxyScrollStartEvent() {
		super();
	}

	/**
	 * Constructs a full scroll start event.
	 * @param eventType event type name as logged
	 * @param attributes map of event attributes, key-value pairs 
	 * @param sessionID session id as logged
	 * @param httpTrafficIndex http traffic id as logged
	 * @param ip user's IP address as logged
	 * @param entry the log entry that contains this event
	 */
	public UsaProxyScrollStartEvent(String eventType,
			HashMap<String, String> attributes, String sessionID,
			String httpTrafficIndex, String ip, UsaProxyPageEventEntry entry) {
		super(eventType, attributes, sessionID, httpTrafficIndex, ip, entry);
		
		getScreen().setScrollStart(this); // Only one scroll start per screen can exist
	}

}

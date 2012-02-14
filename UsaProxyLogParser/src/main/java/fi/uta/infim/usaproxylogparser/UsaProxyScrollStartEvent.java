package fi.uta.infim.usaproxylogparser;

import java.util.HashMap;

public class UsaProxyScrollStartEvent extends UsaProxyPageEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7484995661351167897L;

	public UsaProxyScrollStartEvent() {
		super();
	}

	public UsaProxyScrollStartEvent(String eventType,
			HashMap<String, String> attributes, String sessionID,
			String httpTrafficIndex, String ip, UsaProxyPageEventEntry entry) {
		super(eventType, attributes, sessionID, httpTrafficIndex, ip, entry);
		
		getScreen().setScrollStart(this); // Only one scroll start per screen can exist
	}

}

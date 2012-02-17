package fi.uta.infim.usaproxylogparser;

import java.util.HashMap;

public class UsaProxyAppearanceEvent extends UsaProxyVisibilityEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5560586912788697846L;

	public UsaProxyAppearanceEvent() {
		super();
	}

	public UsaProxyAppearanceEvent(String eventType,
			HashMap<String, String> attributes, String sessionID,
			String httpTrafficIndex, String ip, UsaProxyPageEventEntry entry ) {
		super(eventType, attributes, sessionID, httpTrafficIndex, ip, entry);
		
		getDomPath().getAppears().add(this);
	}

	
}

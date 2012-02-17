package fi.uta.infim.usaproxylogparser;

import java.util.HashMap;

public class UsaProxyDisappearanceEvent extends UsaProxyVisibilityEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6698231841198500351L;

	public UsaProxyDisappearanceEvent() {
		super();
	}

	public UsaProxyDisappearanceEvent(String eventType,
			HashMap<String, String> attributes, String sessionID,
			String httpTrafficIndex, String ip, UsaProxyPageEventEntry entry) {
		super(eventType, attributes, sessionID, httpTrafficIndex, ip, entry);

		getDomPath().getDisappears().add(this);
	}

}

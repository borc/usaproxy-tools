package fi.uta.infim.usaproxylogparser;

import java.io.Serializable;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;

public class UsaProxyScreen implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8795671281132159416L;

	public UsaProxyScreen() {
		super();
	}

	private UsaProxyScreen(Integer iD ) {
		super();
		ID = iD;
	}

	public static UsaProxyScreen newScreen( Integer id, UsaProxyHTTPTraffic httpTrafficSession )
	{
		UsaProxyScreen screen = new UsaProxyScreen(id );
		UsaProxySessionStore.putScreen(screen, httpTrafficSession);
		return screen;
	}
	
	/**
	 * Every screenful has a per-httpTraffic unique ID.
	 */
	private Integer ID;
	
	/**
	 * The http traffic session this screenful is tied to.
	 */
	private UsaProxyHTTPTraffic httpTrafficSession;

	private Vector< UsaProxyPageEvent > events =
			new Vector<UsaProxyPageEvent>();
	
	private UsaProxyViewportChangeEvent initialViewportEvent;
	
	private UsaProxyScrollStartEvent scrollStart;
	
	@XmlAttribute
	public Integer getID() {
		return ID;
	}

	public void setID(Integer iD) {
		ID = iD;
	}

	@XmlTransient
	public UsaProxyHTTPTraffic getHttpTrafficSession() {
		return httpTrafficSession;
	}

	public void setHttpTrafficSession(UsaProxyHTTPTraffic httpTrafficSession) {
		this.httpTrafficSession = httpTrafficSession;
		httpTrafficSession.getScreens().add(this);
	}

	@XmlElements({ 
		@XmlElement( name="event", type=UsaProxyPageEvent.class ),
		@XmlElement( name="appearance", type=UsaProxyAppearanceEvent.class ),
		@XmlElement( name="initialViewport", type=UsaProxyViewportChangeEvent.class ),
		@XmlElement( name="scrollstart", type=UsaProxyScrollStartEvent.class )})
	public Vector< UsaProxyPageEvent > getEvents() {
		return events;
	}

	public void setEvents(Vector< UsaProxyPageEvent > events) {
		this.events = events;
	}

	@XmlTransient
	public UsaProxyViewportChangeEvent getInitialViewportEvent() {
		return initialViewportEvent;
	}

	public void setInitialViewportEvent(UsaProxyViewportChangeEvent initialViewportEvent) {
		this.initialViewportEvent = initialViewportEvent;
	}

	@XmlTransient
	public UsaProxyScrollStartEvent getScrollStart() {
		return scrollStart;
	}

	public void setScrollStart(UsaProxyScrollStartEvent scrollStart) {
		this.scrollStart = scrollStart;
	}
	
}

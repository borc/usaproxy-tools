package fi.uta.infim.usaproxylogparser;

import java.io.Serializable;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A UsaProxy "screen". A screen is a single unique state of the browser
 * viewport. Every element appears and disappears within a screen, and all
 * events happen within a screen. A screen is contained in a HTTP
 * traffic session. A single HTTP traffic session can contain multiple screens.
 * @author Teemu Pääkkönen
 *
 */
public class UsaProxyScreen implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8795671281132159416L;

	/**
	 * No-arg constructor for JAXB. Do not use.
	 */
	public UsaProxyScreen() {
		super();
	}

	/**
	 * Private constructor. Use {@link #newScreen(Integer, UsaProxyHTTPTraffic)}
	 * for creating objects of this class.
	 * @param iD id number of the screen
	 */
	private UsaProxyScreen(Integer iD ) {
		super();
		ID = iD;
	}

	/**
	 * Creates a new screen object and places it in the session store.
	 * @param id screen's id number
	 * @param httpTrafficSession the containing http traffic session
	 * @return the newly created screen object
	 */
	static UsaProxyScreen newScreen( Integer id, UsaProxyHTTPTraffic httpTrafficSession )
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

	/**
	 * All the events that happened in this screen.
	 */
	private Vector< UsaProxyPageEvent > events =
			new Vector<UsaProxyPageEvent>();
	
	/**
	 * The viewport change event that initialized this screen.
	 */
	private UsaProxyViewportChangeEvent initialViewportEvent;
	
	/**
	 * The scroll start event that marks the end point of this screen.
	 * Can be null if no scrolling happened. In that case, either:
	 * 1. the HTTP traffic session ended here, or
	 * 2. the browser window was resized
	 */
	private UsaProxyScrollStartEvent scrollStart;
	
	@XmlAttribute
	public Integer getID() {
		return ID;
	}

	void setID(Integer iD) {
		ID = iD;
	}

	@XmlTransient
	public UsaProxyHTTPTraffic getHttpTrafficSession() {
		return httpTrafficSession;
	}

	void setHttpTrafficSession(UsaProxyHTTPTraffic httpTrafficSession) {
		this.httpTrafficSession = httpTrafficSession;
		httpTrafficSession.getScreens().add(this);
	}

	@XmlElements({ 
		@XmlElement( name="event", type=UsaProxyPageEvent.class ),
		@XmlElement( name="appearance", type=UsaProxyAppearanceEvent.class ),
		@XmlElement( name="disappearance", type=UsaProxyDisappearanceEvent.class ),
		@XmlElement( name="initialViewport", type=UsaProxyViewportChangeEvent.class ),
		@XmlElement( name="scrollstart", type=UsaProxyScrollStartEvent.class )})
	public Vector< UsaProxyPageEvent > getEvents() {
		return events;
	}

	void setEvents(Vector< UsaProxyPageEvent > events) {
		this.events = events;
	}

	@XmlTransient
	public UsaProxyViewportChangeEvent getInitialViewportEvent() {
		return initialViewportEvent;
	}

	void setInitialViewportEvent(UsaProxyViewportChangeEvent initialViewportEvent) {
		this.initialViewportEvent = initialViewportEvent;
	}

	@XmlTransient
	public UsaProxyScrollStartEvent getScrollStart() {
		return scrollStart;
	}

	void setScrollStart(UsaProxyScrollStartEvent scrollStart) {
		this.scrollStart = scrollStart;
	}
	
}

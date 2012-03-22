package fi.uta.infim.usaproxylogparser;

import java.util.HashMap;

/**
 * A viewport change event is triggered when the user scrolls the page or
 * resizes the browser window. Viewport and document dimensions are logged each
 * time this event occurs.
 * @author Teemu Pääkkönen
 *
 */
public class UsaProxyViewportChangeEvent extends UsaProxyPageEvent {

	/**
	 * No-arg constructor for JAXB. Do not use.
	 */
	public UsaProxyViewportChangeEvent() {
		super();
	}

	/**
	 * Constructs a full viewport change event object.
	 * @param eventType event name as logged
	 * @param attributes event attributes, map
	 * @param sessionID session id as logged
	 * @param httpTrafficIndex http traffic id as logged
	 * @param ip user's ip address as logged
	 * @param entry the log entry that contains this event
	 */
	UsaProxyViewportChangeEvent(String eventType,
			HashMap<String, String> attributes, String sessionID,
			String httpTrafficIndex, String ip, UsaProxyPageEventEntry entry) {
		super(eventType, attributes, sessionID, httpTrafficIndex, ip, entry);
		
		setViewportTop( Double.valueOf( attributes.get("top") ) );
		setViewportBottom( Double.valueOf( attributes.get("bottom") ) );
		setViewportLeft( Double.valueOf( attributes.get("left") ) );
		setViewportRight( Double.valueOf( attributes.get("right") ) );
		
		setDocumentHeight( Integer.valueOf( attributes.get( "documentHeight" ) ) );
		setDocumentWidth( Integer.valueOf( attributes.get( "documentWidth" ) ) );
		
		setViewportHeight( Integer.valueOf( attributes.get("viewportHeight")));
		setViewportWidth( Integer.valueOf( attributes.get("viewportWidth")));
		
		attributes.remove( "top" );
		attributes.remove( "bottom" );
		attributes.remove( "left" );
		attributes.remove( "right" );
		
		attributes.remove( "documentHeight" );
		attributes.remove( "documentWidth" );
		
		attributes.remove( "viewportHeight" );
		attributes.remove( "viewportWidth" );
		
		getScreen().setInitialViewportEvent(this);
		getScreen().setHttpTrafficSession(getHttpTrafficSession());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2564037375339245988L;

	/**
	 * The position of the viewport's top, relative to the document's height.
	 * Percentage. 0% = top, 100% = bottom.
	 */
	private Double viewportTop;
	
	/**
	 * The position of the viewport's bottom, relative to the document's height.
	 * Percentage. 0% = top, 100% = bottom.
	 */
	private Double viewportBottom;
	
	/**
	 * The position of the viewport's left edge, relative to the document's width.
	 * Percentage. 0% = left edge, 100% = right edge.
	 */
	private Double viewportLeft;
	
	/**
	 * The position of the viewport's right edge, relative to the document's width.
	 * Percentage. 0% = left edge, 100% = right edge.
	 */
	private Double viewportRight;
	
	/**
	 * Current height of the document in pixels.
	 */
	private Integer documentHeight;
	
	/**
	 * Current width of the document in pixels.
	 */
	private Integer documentWidth;
	
	/**
	 * Current height of the viewport in pixels.
	 */
	private Integer viewportHeight;
	
	/**
	 * Current width of the viewport in pixels.
	 */
	private Integer viewportWidth;

	public Double getViewportTop() {
		return viewportTop;
	}

	void setViewportTop(Double viewportTop) {
		this.viewportTop = viewportTop;
	}

	public Double getViewportBottom() {
		return viewportBottom;
	}

	void setViewportBottom(Double viewportBottom) {
		this.viewportBottom = viewportBottom;
	}

	public Double getViewportLeft() {
		return viewportLeft;
	}

	void setViewportLeft(Double viewportLeft) {
		this.viewportLeft = viewportLeft;
	}

	public Double getViewportRight() {
		return viewportRight;
	}

	void setViewportRight(Double viewportRight) {
		this.viewportRight = viewportRight;
	}

	public Integer getDocumentHeight() {
		return documentHeight;
	}

	void setDocumentHeight(Integer documentHeight) {
		this.documentHeight = documentHeight;
	}

	public Integer getDocumentWidth() {
		return documentWidth;
	}

	void setDocumentWidth(Integer documentWidth) {
		this.documentWidth = documentWidth;
	}

	public Integer getViewportHeight() {
		return viewportHeight;
	}

	void setViewportHeight(Integer viewportHeight) {
		this.viewportHeight = viewportHeight;
	}

	public Integer getViewportWidth() {
		return viewportWidth;
	}

	void setViewportWidth(Integer viewportWidth) {
		this.viewportWidth = viewportWidth;
	}
	
	
}

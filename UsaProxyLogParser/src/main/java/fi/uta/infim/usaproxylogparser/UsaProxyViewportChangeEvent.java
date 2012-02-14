package fi.uta.infim.usaproxylogparser;

import java.util.HashMap;

public class UsaProxyViewportChangeEvent extends UsaProxyPageEvent {

	public UsaProxyViewportChangeEvent() {
		super();
	}

	public UsaProxyViewportChangeEvent(String eventType,
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
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2564037375339245988L;

	private Double viewportTop;
	
	private Double viewportBottom;
	
	private Double viewportLeft;
	
	private Double viewportRight;
	
	private Integer documentHeight;
	
	private Integer documentWidth;
	
	private Integer viewportHeight;
	
	private Integer viewportWidth;

	public Double getViewportTop() {
		return viewportTop;
	}

	public void setViewportTop(Double viewportTop) {
		this.viewportTop = viewportTop;
	}

	public Double getViewportBottom() {
		return viewportBottom;
	}

	public void setViewportBottom(Double viewportBottom) {
		this.viewportBottom = viewportBottom;
	}

	public Double getViewportLeft() {
		return viewportLeft;
	}

	public void setViewportLeft(Double viewportLeft) {
		this.viewportLeft = viewportLeft;
	}

	public Double getViewportRight() {
		return viewportRight;
	}

	public void setViewportRight(Double viewportRight) {
		this.viewportRight = viewportRight;
	}

	public Integer getDocumentHeight() {
		return documentHeight;
	}

	public void setDocumentHeight(Integer documentHeight) {
		this.documentHeight = documentHeight;
	}

	public Integer getDocumentWidth() {
		return documentWidth;
	}

	public void setDocumentWidth(Integer documentWidth) {
		this.documentWidth = documentWidth;
	}

	public Integer getViewportHeight() {
		return viewportHeight;
	}

	public void setViewportHeight(Integer viewportHeight) {
		this.viewportHeight = viewportHeight;
	}

	public Integer getViewportWidth() {
		return viewportWidth;
	}

	public void setViewportWidth(Integer viewportWidth) {
		this.viewportWidth = viewportWidth;
	}
	
	
}

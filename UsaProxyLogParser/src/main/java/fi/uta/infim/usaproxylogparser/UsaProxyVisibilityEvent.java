package fi.uta.infim.usaproxylogparser;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;

/**
 * A visibility event is either an appearance event or a disappearance event.
 * This is a superclass for such events.
 * @author Teemu Pääkkönen
 *
 */
public class UsaProxyVisibilityEvent extends UsaProxyPageEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1765163513464819756L;

	/**
	 * Browser viewport edge types.
	 * @author Teemu Pääkkönen
	 *
	 */
	@XmlEnum
	public enum Edge
	{
		/**
		 * Viewport top edge
		 */
		TOP( "top" ),
		
		/**
		 * Viewport left side edge
		 */
		LEFT( "left" ),
		
		/**
		 * Viewport right side edge
		 */
		RIGHT( "right" ),
		
		/**
		 * Viewport bottom edge
		 */
		BOTTOM( "bottom" ),
		
		/**
		 * No edge. Element was on the screen when the page was loaded.
		 */
		INITIAL( "initial" ),
		
		/**
		 * Actual edge is not known. The edge through which an element
		 * disappeared during resizing cannot be detected in an efficient way.
		 */
		UNKNOWN ( "unknown" );
		
		/**
		 * Internal value of the enum entry. Used for JAXB marshalling and unmarshalling.
		 */
		private final String value;
		
		/**
		 * Constructor. Requires the internal value to be set.
		 * @param value internal value used in JAXB [un]marshalling.
		 */
		Edge( String value )
		{
			this.value = value;
		}
		
		/**
		 * Returns the internal value used in JAXB [un]marshalling.
		 * @return internal value
		 */
		public String value() {
	        return value;
	    }
		
		/**
		 * Creates an enum object from the primitive string value
		 * @param value "top", "left", "right", "bottom", "initial" or "unknown"
		 * @return the enum representation of supplied string
		 */
		public static Edge fromValue(String value) {
	        for (Edge c: Edge.values()) {
	            if (c.value.equals(value)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(value);
	    }
	}

	/**
	 * The browser viewport edge through which the element traversed when 
	 * it appared/disappeared.
	 */
	protected Edge edge;
	
	/**
	 * The position of the element's top, relative to the document's height.
	 * Percentage. 0% = top of document, 100% = bottom of document.
	 */
	private Double topPosition;
	
	/**
	 * The position of the element's bottom, relative to the document's height.
	 * Percentage. 0% = top of document, 100% = bottom of document.
	 */
	private Double bottomPosition;

	/**
	 * Constructs a full visibility event. Note that this constructor should 
	 * only be called by subclass constructors.
	 */
	protected UsaProxyVisibilityEvent(String eventType,
			HashMap<String, String> attributes, String sessionID,
			String httpTrafficIndex, String ip, UsaProxyPageEventEntry entry) {
		super(eventType, attributes, sessionID, httpTrafficIndex, ip, entry);
		
		this.edge = Edge.fromValue( attributes.get("edge") );
		setTopPosition( Double.valueOf( attributes.get("relativeTop")));
		setBottomPosition( Double.valueOf( attributes.get("relativeBottom")));
		
		attributes.remove( "edge" );
		attributes.remove( "relativeTop" );
		attributes.remove( "relativeBottom" );
	}

	/**
	 * No-arg constructor for JAXB. Do not use.
	 */
	public UsaProxyVisibilityEvent() {
		super();
	}

	@XmlAttribute
	public Edge getEdge() {
		return edge;
	}

	public void setEdge(Edge edge) {
		this.edge = edge;
	}

	public Double getTopPosition() {
		return topPosition;
	}

	public void setTopPosition(Double topPosition) {
		this.topPosition = topPosition;
	}

	public Double getBottomPosition() {
		return bottomPosition;
	}

	public void setBottomPosition(Double bottomPosition) {
		this.bottomPosition = bottomPosition;
	}

}
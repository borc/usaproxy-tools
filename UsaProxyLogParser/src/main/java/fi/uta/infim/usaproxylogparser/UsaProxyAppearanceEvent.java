package fi.uta.infim.usaproxylogparser;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;

public class UsaProxyAppearanceEvent extends UsaProxyPageEvent {

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
		
		public static Edge fromValue(String value) {
	        for (Edge c: Edge.values()) {
	            if (c.value.equals(value)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(value);
	    }
	}
	
	public UsaProxyAppearanceEvent() {
		super();
	}

	public UsaProxyAppearanceEvent(String eventType,
			HashMap<String, String> attributes, String sessionID,
			String httpTrafficIndex, String ip, UsaProxyPageEventEntry entry ) {
		super(eventType, attributes, sessionID, httpTrafficIndex, ip, entry);
		this.edge = Edge.fromValue( attributes.get("edge") );
		this.disappearance = this.getType().equals( EventType.DISAPPEAR );
		
		setTopPosition( Double.valueOf( attributes.get("relativeTop")));
		setBottomPosition( Double.valueOf( attributes.get("relativeBottom")));
		
		attributes.remove( "edge" );
		attributes.remove( "relativeTop" );
		attributes.remove( "relativeBottom" );
		
		if ( isDisappearance() )
		{
			getDomPath().getDisappears().add(this);
		}
		else
		{
			getDomPath().getAppears().add(this);
		}
	}

	/**
	 * Is this a disappearance instead of appearance?
	 */
	private boolean disappearance;
	
	/**
	 * The browser viewport edge through which the element traversed when 
	 * it appared/disappeared.
	 */
	private Edge edge;

	private Double topPosition;
	
	private Double bottomPosition;
	
	@XmlAttribute
	public Edge getEdge() {
		return edge;
	}

	public void setEdge(Edge edge) {
		this.edge = edge;
	}

	@XmlAttribute
	public boolean isDisappearance() {
		return disappearance;
	}

	public void setDisappearance(boolean disappearance) {
		this.disappearance = disappearance;
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

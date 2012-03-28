/*
 * UsaProxyLogParser - Java API for UsaProxy-fork logs
 *  Copyright (C) 2012 Teemu Pääkkönen - University of Tampere
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
		String value() {
	        return value;
	    }
		
		/**
		 * Creates an enum object from the primitive string value
		 * @param value "top", "left", "right", "bottom", "initial" or "unknown"
		 * @return the enum representation of supplied string
		 */
		static Edge fromValue(String value) {
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
	UsaProxyVisibilityEvent(String eventType,
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

	/**
	 * Through which edge did the element travel to trigger this event?
	 * @return The edge through which this element traveled
	 */
	@XmlAttribute
	public Edge getEdge() {
		return edge;
	}

	void setEdge(Edge edge) {
		this.edge = edge;
	}

	/**
	 * @return The position of the element's top, relative to the document's height.
	 * Percentage. 0% = top, 100% = bottom.
	 */
	public Double getTopPosition() {
		return topPosition;
	}

	void setTopPosition(Double topPosition) {
		this.topPosition = topPosition;
	}

	/**
	 * @return The position of the element's bottom, relative to the document's height.
	 * Percentage. 0% = top, 100% = bottom.
	 */
	public Double getBottomPosition() {
		return bottomPosition;
	}

	void setBottomPosition(Double bottomPosition) {
		this.bottomPosition = bottomPosition;
	}

}
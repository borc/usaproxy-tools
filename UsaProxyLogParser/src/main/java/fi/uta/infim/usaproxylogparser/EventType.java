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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * UsaProxy 2.0 event types.
 * @author Teemu Pääkkönen
 *
 */
@XmlType(name = "event-type")
@XmlEnum( String.class )
public enum EventType {
	
	/**
	 * Load event for images and documents
	 */
	@XmlEnumValue( "load" )
	LOAD( "load" ),
	
	/**
	 * The mouseover DOM event
	 */
	@XmlEnumValue( "mouseover" )
	MOUSEOVER( "mouseover" ),
	
	/**
	 * Browser window resize event
	 */
	@XmlEnumValue( "resize" )
	RESIZE( "resize" ),
	
	/**
	 * Page scroll event. Not a DOM event. UsaProxy has a periodical timer
	 * for checking when the page has been scrolled.
	 */
	@XmlEnumValue( "scroll" )
	SCROLL( "scroll" ),
	
	/**
	 * mousemove DOM event
	 */
	@XmlEnumValue( "mousemove" )
	MOUSEMOVE( "mousemove" ),
	
	/**
	 * mousedown DOM event
	 */
	@XmlEnumValue( "mousedown" )
	MOUSEDOWN( "mousedown" ),
	
	/**
	 * keypress DOM event
	 */
	@XmlEnumValue( "keypress" )
	KEYPRESS( "keypress" ),
	
	/**
	 * focus DOM event
	 */
	@XmlEnumValue( "focus" )
	FOCUS( "focus" ),
	
	/**
	 * blur DOM event
	 */
	@XmlEnumValue( "blur" )
	BLUR( "blur" ),
	
	/**
	 * change DOM event
	 */
	@XmlEnumValue( "change" )
	CHANGE( "change" ),
	
	/**
	 * select DOM event
	 */
	@XmlEnumValue( "select" )
	SELECT( "select" ),

	/**
	 * The appear event is triggered when any part of an element is scrolled
	 * into view.
	 */
	@XmlEnumValue( "appear" )
	APPEAR( "appear" ),
	
	/**
	 * The disappear event is triggered when an element is scrolled completely
	 * out of view.
	 */
	@XmlEnumValue( "disappear" )
	DISAPPEAR( "disappear" ),
	
	/**
	 * The viewport change event is triggered when the browser window is scrolled
	 * or resized, ie. the viewport is changed. If triggered by scrolling,
	 * only the end of the scrolling will trigger logging. See {@link EventType#SCROLLSTART}
	 * for further info.
	 */
	@XmlEnumValue( "viewportChange" )
	VIEWPORTCHANGE( "viewportChange" ),
	
	/**
	 * Scroll start event is triggered when the user begins scrolling a page.
	 * Note that this will be logged once for every viewportChange event.
	 * The difference is that this event is logged when the scrolling begins and
	 * the viewport change is logged when the scrolling stops.
	 */
	@XmlEnumValue( "scrollStart" )
	SCROLLSTART( "scrollStart" ),
	
	/**
	 * Events recorded by plugins.
	 */
	@XmlEnumValue( "plugin" )
	PLUGIN( "plugin" );
	
	/**
	 * Internal value of the enum entry. Used for JAXB marshalling and unmarshalling.
	 */
	private final String value;
	
	/**
	 * Constructor. Requires the internal value to be set.
	 * @param value internal value used in JAXB [un]marshalling.
	 */
	EventType( String value )
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
	 * JAXB unmarshalling helper. Converts an internal value to an enum object.
	 * @param value internal value (XML representation)
	 * @return corresponding enum object
	 */
	static EventType fromValue(String value) {
        for (EventType c: EventType.values()) {
            if (c.value.equals(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException(value);
    }
}
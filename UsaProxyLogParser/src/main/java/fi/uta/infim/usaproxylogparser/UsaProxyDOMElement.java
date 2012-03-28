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

import java.io.Serializable;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

/**
 * An object of this class represents a DOM element. Each DOM element in a 
 * UsaProxy log is given a unique DOM path (eg. "abaab") within the HTTP
 * traffic session. The DOM path represents the element's location in the
 * DOM tree, with "a" being the first child, "b" the second, etc.
 * An element object contains all the events that were triggered and logged
 * in this element's context.
 * @author Teemu Pääkkönen
 *
 */
public class UsaProxyDOMElement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4849107937656330641L;

	/**
	 * No-arg constructor for JAXB. Do not use.
	 */
	public UsaProxyDOMElement() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((httpTraffic == null) ? 0 : httpTraffic.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UsaProxyDOMElement other = (UsaProxyDOMElement) obj;
		if (httpTraffic == null) {
			if (other.httpTraffic != null)
				return false;
		} else if (!httpTraffic.equals(other.httpTraffic))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	/**
	 * Private constructor. Use {@link #newDOMElement} for creating new objects.
	 * @param path the DOM path in UsaProxy format (eg. "abaab")
	 * @param httpTraffic the HTTP traffic session during which this event occurred
	 * @param nodeName node name (eg. "H1" or "P")
	 * @param contents element contents (if available); if null, report generator
	 * will attempt parsing content from the original http traffic log file.
	 */
	private UsaProxyDOMElement(String path, UsaProxyHTTPTraffic httpTraffic, 
			String nodeName, String contents) {
		super();
		this.path = path;
		setHttpTraffic( httpTraffic );
		setNodeName(nodeName);
		setContents(contents);
	}
	
	/**
	 * Constructs a new DOM element object and places it in the session store.
	 * See {@link #UsaProxyDOMElement(String, UsaProxyHTTPTraffic, String, String)}
	 * for parameters.
	 * @return the constructed object
	 */
	static UsaProxyDOMElement newDOMElement( String path, 
			UsaProxyHTTPTraffic traffic, String nodeName, String contents )
	{
		UsaProxyDOMElement e = new UsaProxyDOMElement(path, traffic, nodeName, contents);
		UsaProxySessionStore.putDOMElement(e);
		return e;
	}

	/**
	 * The path of the dom element in UsaProxy format.
	 * For example: "abd" for the fourth child of the second element in the
	 * document. The fist "a" always refers to the document itself and the
	 * next letter is typically "b" for the body element ("a" is usually HEAD). 
	 */
	private String path;
	
	/**
	 * A DOM element is always tied to a http traffic object. DOM elements'
	 * {@link #path paths} are unique within a single http traffic session.
	 */
	private UsaProxyHTTPTraffic httpTraffic;

	/**
	 * The events that occurred within this DOM element's context
	 */
	private Vector< UsaProxyPageEvent > events =
			new Vector<UsaProxyPageEvent>();
	
	/**
	 * All the appearance events of this element
	 */
	private Vector< UsaProxyAppearanceEvent > appears =
			new Vector<UsaProxyAppearanceEvent>();
	
	/**
	 * All the disappearance events of this element
	 */
	private Vector< UsaProxyDisappearanceEvent > disappears =
			new Vector<UsaProxyDisappearanceEvent>();
	
	/**
	 * The node name of the element, eg. "h1"
	 */
	private String nodeName;
	
	/**
	 * If contents logging was active during logging, this member will contain
	 * the element's text contents. Note that elements' contents can be empty.
	 * This member will be null if contents logging was not active.
	 */
	private String contents;
	
	/**
	 * The DOM path of this element. Represents the element's place in the 
	 * DOM tree (a = first child, b = second, etc.)
	 * @return the DOM path of this element (eg. "abaab")
	 */
	@XmlAttribute
	public String getPath() {
		return path;
	}

	void setPath(String path) {
		this.path = path;
	}

	/**
	 * This element's http traffic session.
	 * @return the http traffic session object
	 */
	@XmlTransient
	public UsaProxyHTTPTraffic getHttpTraffic() {
		return httpTraffic;
	}

	void setHttpTraffic(UsaProxyHTTPTraffic httpTraffic) {
		this.httpTraffic = httpTraffic;
		httpTraffic.getDomElements().add(this);
	}

	/**
	 * Every event that occurred in this element's context. List may be empty.
	 * @return events that occurred in this element's context
	 */
	@XmlTransient
	public Vector< UsaProxyPageEvent > getEvents() {
		return events;
	}

	void setEvents(Vector< UsaProxyPageEvent > events) {
		this.events = events;
	}

	/**
	 * All the appearance events of this element. An element can appear several
	 * times within a HTTP traffic session, but only once during a screen.
	 * @return appearance events of this element
	 */
	@XmlTransient
	public Vector< UsaProxyAppearanceEvent > getAppears() {
		return appears;
	}

	void setAppears(Vector< UsaProxyAppearanceEvent > appears) {
		this.appears = appears;
	}

	/**
	 * All the disappearance events of this element. An element may disappear
	 * zero or more times within a http traffic session, but only once during
	 * a screen.
	 * @return disappearances of this element
	 */
	@XmlTransient
	public Vector< UsaProxyDisappearanceEvent > getDisappears() {
		return disappears;
	}

	void setDisappears(Vector< UsaProxyDisappearanceEvent > disappears) {
		this.disappears = disappears;
	}

	/**
	 * The node name of this element (eg. "h1" or "p")
	 * @return node name of this element
	 */
	@XmlAttribute
	public String getNodeName() {
		return nodeName;
	}

	void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	/**
	 * The contents of this element. Null if not found or logged.
	 * @return contents of this element
	 */
	public String getContents() {
		return contents;
	}

	void setContents(String contents) {
		this.contents = contents;
	}
	
}

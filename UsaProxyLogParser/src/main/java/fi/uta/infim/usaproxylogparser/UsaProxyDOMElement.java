package fi.uta.infim.usaproxylogparser;

import java.io.Serializable;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

public class UsaProxyDOMElement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4849107937656330641L;

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

	private UsaProxyDOMElement(String path, UsaProxyHTTPTraffic httpTraffic, 
			String nodeName, String contents) {
		super();
		this.path = path;
		setHttpTraffic( httpTraffic );
		setNodeName(nodeName);
		setContents(contents);
	}
	
	public static UsaProxyDOMElement newDOMElement( String path, 
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
	
	@XmlAttribute
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@XmlTransient
	public UsaProxyHTTPTraffic getHttpTraffic() {
		return httpTraffic;
	}

	public void setHttpTraffic(UsaProxyHTTPTraffic httpTraffic) {
		this.httpTraffic = httpTraffic;
		httpTraffic.getDomElements().add(this);
	}

	@XmlTransient
	public Vector< UsaProxyPageEvent > getEvents() {
		return events;
	}

	public void setEvents(Vector< UsaProxyPageEvent > events) {
		this.events = events;
	}

	@XmlTransient
	public Vector< UsaProxyAppearanceEvent > getAppears() {
		return appears;
	}

	public void setAppears(Vector< UsaProxyAppearanceEvent > appears) {
		this.appears = appears;
	}

	@XmlTransient
	public Vector< UsaProxyDisappearanceEvent > getDisappears() {
		return disappears;
	}

	public void setDisappears(Vector< UsaProxyDisappearanceEvent > disappears) {
		this.disappears = disappears;
	}

	@XmlAttribute
	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
	
}

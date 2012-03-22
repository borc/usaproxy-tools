package fi.uta.infim.usaproxylogparser;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A page event log entry. Can be a DOM event or some other event such as 'scroll'.
 * @author Teemu Pääkkönen
 *
 */
@XmlSeeAlso({UsaProxyLogEntry.class,UsaProxyHTTPTrafficStartEntry.class,UsaProxyPageEventEntry.class,UsaProxyHTTPTraffic.class,UsaProxySession.class,UsaProxyPageEvent.class,UsaProxyLog.class})
public class UsaProxyPageEventEntry extends UsaProxyLogEntry {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2933150130413400056L;
	
	/**
	 * Constructor for a full entry object.
	 * @param ip user's ip address, as logged
	 * @param timestamp log timestamp, as logged
	 * @param index HTTP traffic id, as logged
	 * @param sessionId session id, as logged
	 * @param event event's name, as logged
	 * @param attributes map of event attributes in key-value pairs
	 */
	public UsaProxyPageEventEntry(String ip,
			String timestamp, String index, String sessionId, String event,
			HashMap< String, String > attributes ) {
		super( timestamp );
		EventType eventtype = EventType.fromValue(event);
		if ( eventtype.equals( EventType.APPEAR ) )
		{
			this.event = new UsaProxyAppearanceEvent( event, attributes, 
					sessionId, index, ip, this );
		}
		else if ( eventtype.equals( EventType.DISAPPEAR ) )
		{
			this.event = new UsaProxyDisappearanceEvent( event, attributes, 
					sessionId, index, ip, this );
		}
		else if ( eventtype.equals( EventType.VIEWPORTCHANGE ) )
		{
			this.event = new UsaProxyViewportChangeEvent( event, attributes, 
					sessionId, index, ip, this );
		}
		else if ( eventtype.equals( EventType.SCROLLSTART ) )
		{
			this.event = new UsaProxyScrollStartEvent( event, attributes, 
					sessionId, index, ip, this );
		}
		else
		{
			this.event = new UsaProxyPageEvent( event, attributes, sessionId, index, ip, this );
		}
	}

	/**
	 * No-arg constructor for JAXB. Do not use.
	 */
	public UsaProxyPageEventEntry() {
		super();
	}

	/**
	 * The event described by this log entry. Each page event log entry 
	 * describes exactly one page event.
	 */
	private UsaProxyPageEvent event;

	@XmlTransient
	public UsaProxyPageEvent getEvent() {
		return event;
	}

	public void setEvent(UsaProxyPageEvent event) {
		this.event = event;
	}
	
}

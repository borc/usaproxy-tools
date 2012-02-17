package fi.uta.infim.usaproxylogparser;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

/**
 * 
 * @author Teemu Pääkkönen
 * A page event. Can be a DOM event or some other event such as 'scroll'.
 *
 */
@XmlSeeAlso({UsaProxyLogEntry.class,UsaProxyHTTPTrafficStartEntry.class,UsaProxyPageEventEntry.class,UsaProxyHTTPTraffic.class,UsaProxySession.class,UsaProxyPageEvent.class,UsaProxyLog.class})
public class UsaProxyPageEventEntry extends UsaProxyLogEntry {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2933150130413400056L;
	
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

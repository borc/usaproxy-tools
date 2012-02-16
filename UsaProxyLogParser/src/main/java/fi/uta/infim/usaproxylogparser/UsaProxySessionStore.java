package fi.uta.infim.usaproxylogparser;

import java.util.Collections;
import java.util.HashMap;

public final class UsaProxySessionStore {

	private static final HashMap< String, UsaProxySession > sessions =
			new HashMap<String, UsaProxySession>();
	
	private static final HashMap< Integer, UsaProxyHTTPTraffic > httpTrafficSessions =
			new HashMap<Integer, UsaProxyHTTPTraffic>();
	
	private static final HashMap< Integer, HashMap< Integer, UsaProxyScreen > > screens =
			new HashMap<Integer, HashMap<Integer,UsaProxyScreen>>();
	
	private static final HashMap< Integer, HashMap< String, UsaProxyDOMElement > > domElements =
			new HashMap<Integer, HashMap<String,UsaProxyDOMElement>>();
	
	/**
	 * Disallow instantiation. This is a singleton-ish class.
	 */
	private UsaProxySessionStore(){}
	
	public static UsaProxySession getSessionById( String id )
	{
		return sessions.get(id);
	}
	
	public static void putSession( UsaProxySession session )
	{
		sessions.put( session.getSessionID(), session );
	}
	
	public static UsaProxyHTTPTraffic getHTTPTrafficSessionById( String id )
	{
		return httpTrafficSessions.get(Integer.valueOf( id ));
	}
	
	public static void putHTTPTrafficSession( UsaProxyHTTPTraffic hts )
	{
		httpTrafficSessions.put( hts.getSessionID(), hts );
	}
	
	public static UsaProxyScreen getScreenById( Integer httpTrafficId, String id )
	{
		return !screens.containsKey( httpTrafficId ) ? null : 
				screens.get( httpTrafficId ).get( Integer.parseInt(id) );
	}
	
	public static void putScreen( UsaProxyScreen screen, UsaProxyHTTPTraffic httpTraffic )
	{
		HashMap<Integer,UsaProxyScreen> theMap = screens.get( httpTraffic.getSessionID() );
		if ( theMap == null )
		{
			theMap = new HashMap<Integer,UsaProxyScreen>();
			screens.put( httpTraffic.getSessionID(), theMap );
		}
		theMap.put( screen.getID(), screen );
	}
	
	public static UsaProxyDOMElement getDOMElementById( Integer httpTrafficId, String id )
	{
		return !domElements.containsKey( httpTrafficId ) ? null : 
			domElements.get( httpTrafficId ).get( id );
	}
	
	public static void putDOMElement( UsaProxyDOMElement element )
	{
		HashMap<String,UsaProxyDOMElement> theMap = domElements.get( element.getHttpTraffic().getSessionID() );
		if ( theMap == null )
		{
			theMap = new HashMap<String,UsaProxyDOMElement>();
			domElements.put( element.getHttpTraffic().getSessionID(), theMap );
		}
		theMap.put( element.getPath(), element );
	}

	/**
	 * Assigns an unmodifiable reference to the collection of sessions into the specified
	 * log object. The collection is immutable in order to prevent modifications
	 * via this reference. The {@link UsaProxySession newSession} method should be used to
	 * modify the collection.
	 * @param log
	 */
	public static void assignSessionsTo( UsaProxyLog log )
	{
		log.setSessions(Collections.unmodifiableCollection(sessions.values()));
	}
}

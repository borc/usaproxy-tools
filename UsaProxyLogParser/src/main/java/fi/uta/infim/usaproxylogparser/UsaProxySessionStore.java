package fi.uta.infim.usaproxylogparser;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * A class for storing usaproxy session and session-related objects.
 * This is a static store, and will hold the objects infinitely.
 * @author Teemu Pääkkönen
 *
 */
public final class UsaProxySessionStore {

	/**
	 * UsaProxy sessions, key = session ID.
	 */
	private static final HashMap< String, UsaProxySession > sessions =
			new HashMap<String, UsaProxySession>();
	
	/**
	 * HTTP traffic sessions, key = http traffic id
	 */
	private static final HashMap< Integer, UsaProxyHTTPTraffic > httpTrafficSessions =
			new HashMap<Integer, UsaProxyHTTPTraffic>();
	
	/**
	 * UsaProxy screens, key = (http traffic id, screen id).
	 * Each screen id is unique within a http traffic session.
	 */
	private static final HashMap< Integer, HashMap< Integer, UsaProxyScreen > > screens =
			new HashMap<Integer, HashMap<Integer,UsaProxyScreen>>();
	
	/**
	 * DOM elements, key = (http traffic id, dom path).
	 * Each DOM element path is unique within a http traffic session.
	 */
	private static final HashMap< Integer, HashMap< String, UsaProxyDOMElement > > domElements =
			new HashMap<Integer, HashMap<String,UsaProxyDOMElement>>();
	
	/**
	 * Disallow instantiation. This is a singleton-ish class.
	 */
	private UsaProxySessionStore(){}
	
	/**
	 * Finds a session by session id.
	 * @param id session id
	 * @return the session object if it exists in the store. Otherwise null.
	 */
	public static UsaProxySession getSessionById( String id )
	{
		return sessions.get(id);
	}
	
	static void putSession( UsaProxySession session )
	{
		sessions.put( session.getSessionID(), session );
	}
	
	/**
	 * Finds a HTTP traffic session by http traffic id.
	 * @param id http traffic id
	 * @return the http traffic session if it exists in the store. Otherwise null.
	 */
	public static UsaProxyHTTPTraffic getHTTPTrafficSessionById( String id )
	{
		return httpTrafficSessions.get(Integer.valueOf( id ));
	}
	
	static void putHTTPTrafficSession( UsaProxyHTTPTraffic hts )
	{
		httpTrafficSessions.put( hts.getSessionID(), hts );
	}
	
	/**
	 * Finds a screen by http traffic id and screen id.
	 * @param httpTrafficId http traffic id
	 * @param id screen id (as logged)
	 * @return the screen if it exists in the store. Otherwise null.
	 */
	public static UsaProxyScreen getScreenById( Integer httpTrafficId, String id )
	{
		return !screens.containsKey( httpTrafficId ) ? null : 
				screens.get( httpTrafficId ).get( Integer.parseInt(id) );
	}
	
	static void putScreen( UsaProxyScreen screen, UsaProxyHTTPTraffic httpTraffic )
	{
		HashMap<Integer,UsaProxyScreen> theMap = screens.get( httpTraffic.getSessionID() );
		if ( theMap == null )
		{
			theMap = new HashMap<Integer,UsaProxyScreen>();
			screens.put( httpTraffic.getSessionID(), theMap );
		}
		theMap.put( screen.getID(), screen );
	}
	
	/**
	 * Finds a DOM element by supplied traffic id and dom path.
	 * @param httpTrafficId the HTTP traffic id
	 * @param id DOM path of the element
	 * @return the element if it exists in the store, otherwise null
	 */
	public static UsaProxyDOMElement getDOMElementById( Integer httpTrafficId, String id )
	{
		return !domElements.containsKey( httpTrafficId ) ? null : 
			domElements.get( httpTrafficId ).get( id );
	}
	
	static void putDOMElement( UsaProxyDOMElement element )
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
	 * @param log the log to insert the sessions into
	 */
	static void assignSessionsTo( UsaProxyLog log )
	{
		log.setSessions(Collections.unmodifiableCollection(sessions.values()));
	}
	
	/**
	 * Returns an unmodifiable reference to the http traffic sessions map.
	 * The collection should only be modified by {@link UsaProxySession newSession} method.
	 * @return an unmodifiable reference to http traffic sessions
	 */
	public static Collection<UsaProxyHTTPTraffic> getHTTPTrafficSessions()
	{
		return Collections.unmodifiableCollection( httpTrafficSessions.values() );
	}
}

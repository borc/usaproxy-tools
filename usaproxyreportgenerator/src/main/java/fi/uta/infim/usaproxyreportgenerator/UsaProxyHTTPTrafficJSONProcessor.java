package fi.uta.infim.usaproxyreportgenerator;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import fi.uta.infim.usaproxylogparser.UsaProxyAppearanceEvent;
import fi.uta.infim.usaproxylogparser.UsaProxyDOMElement;
import fi.uta.infim.usaproxylogparser.UsaProxyDisappearanceEvent;
import fi.uta.infim.usaproxylogparser.UsaProxyHTTPTraffic;
import fi.uta.infim.usaproxylogparser.UsaProxyPageEvent;
import fi.uta.infim.usaproxylogparser.UsaProxyScreen;
import fi.uta.infim.usaproxylogparser.UsaProxySessionStore;
import fi.uta.infim.usaproxylogparser.UsaProxyVisibilityEvent;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

public class UsaProxyHTTPTrafficJSONProcessor implements JsonBeanProcessor {

	@Override
	public JSONObject processBean(Object arg0, JsonConfig arg1) {
		UsaProxyHTTPTraffic traffic = (UsaProxyHTTPTraffic) arg0;
		try {
			JSONObject jsonHTTPTraffic = new JSONObject();
			jsonHTTPTraffic.accumulate( "viewportMovement", getViewportMovement(traffic));
			jsonHTTPTraffic.accumulate( "domElements", getDOMElements(traffic));
			jsonHTTPTraffic.accumulate( "url", traffic.getUrl().toString());
			jsonHTTPTraffic.accumulate( "id", traffic.getSessionID() );
			jsonHTTPTraffic.accumulate( "requestHeaders", traffic.getRequestHeaders() );
			jsonHTTPTraffic.accumulate( "responseHeaders", traffic.getResponseHeaders() );
			jsonHTTPTraffic.accumulate( "timestamp", null == traffic.getEntry() ? null : 
				removeTimezone( traffic.getEntry().getTimestamp() ) );
			
			return jsonHTTPTraffic;
		} catch (Exception e) {
			throw new RuntimeException( e );
		}
	}

	private JSONObject getDOMElements( UsaProxyHTTPTraffic traffic ) throws IOException
	{
		JSONObject elements = new JSONObject();
		JSONObject details = new JSONObject();
		JSONArray sightings = new JSONArray();
		
		for ( UsaProxyDOMElement e : traffic.getDomElements() )
		{
			if ( e == null || e.getPath() == null ) continue; // Don't process empty elements
			JSONObject thisSightings = getSightings(e);
			sightings.add( getTopDataset(e.getPath(), thisSightings, e.getPath()) );
			sightings.add( getBottomDataset(e.getPath(), thisSightings, "rgb(50,50,255)", e.getPath() ) );
			
			details.accumulate( e.getPath(), JSONObject.fromObject(e, App.getConfig()) );
		}
		
		elements.accumulate( "details", details );
		elements.accumulate( "sightings", sightings );
		return elements;
	}
	
	private JSONArray getViewportMovement( UsaProxyHTTPTraffic traffic )
	{
		JSONArray movement = new JSONArray();
		JSONObject movements = getViewportLines(traffic);
		String vpMovementDatasetName = "_viewport";
		movement.add( getTopDataset( vpMovementDatasetName, movements, "_vpTop") );
		movement.add( getBottomDataset( vpMovementDatasetName, movements, "rgb(255,50,50)", "_vpBottom") );
		
		return movement;
	}
	
	static private String generateDatasetTopId( String elementTopName )
	{
		return elementTopName + "Top";
	}
	
	private JSONObject getTopDataset( String elementTopName, JSONObject sightings, String elementDomId )
	{
		JSONObject dataset = new JSONObject();
		dataset.accumulate( "data", sightings.getJSONArray( "top" ) );
		dataset.accumulate( "id", generateDatasetTopId( elementTopName ) );
		JSONObject lines = new JSONObject();
		lines.accumulate( "show", true );
		lines.accumulate( "lineWidth", 0 );
		dataset.accumulate( "lines", lines );
		dataset.accumulate( "elementDomId", elementDomId );
		dataset.accumulate( "hoverable", true );
		return dataset;
	}
	
	private JSONObject getBottomDataset( String elementTopName, JSONObject sightings, String color, String elementDomId )
	{
		JSONObject dataset = new JSONObject();
		dataset.accumulate( "data", sightings.getJSONArray( "bottom" ) );
		JSONObject lines = new JSONObject();
		lines.accumulate( "show", true );
		lines.accumulate( "lineWidth", 0 );
		lines.accumulate( "fill", 0.2 );
		dataset.accumulate( "lines", lines );
		dataset.accumulate( "color", color );
		dataset.accumulate( "fillBetween", generateDatasetTopId( elementTopName ) );
		dataset.accumulate( "hoverable", true );
		dataset.accumulate( "elementDomId", elementDomId );
		return dataset;
	}
	
	@SuppressWarnings("deprecation")
	public static long removeTimezone( Date timestamp )
	{
		return timestamp.getTime() - (60 * 1000 * timestamp.getTimezoneOffset());
	}
	
	private JSONObject getSightings( UsaProxyDOMElement element )
	{
		JSONObject jsonElement = new JSONObject();
		JSONArray top = new JSONArray();
		JSONArray bottom = new JSONArray();
		
		Date lastAppear = null;
		Double lastTop = null;
		Double lastBottom = null;
		
		for ( UsaProxyPageEvent e : element.getEvents() )
		{
			if ( UsaProxyVisibilityEvent.class.isInstance(e) )
			{
				JSONArray topPoint = new JSONArray();
				topPoint.add( removeTimezone( e.getEntry().getTimestamp() ) );
				topPoint.add( ((UsaProxyVisibilityEvent)e).getTopPosition() );
				top.add(topPoint);
				
				JSONArray bottomPoint = new JSONArray();
				bottomPoint.add( removeTimezone( e.getEntry().getTimestamp() ) );
				bottomPoint.add( ((UsaProxyVisibilityEvent)e).getBottomPosition() );
				bottom.add(bottomPoint);
				
				if ( UsaProxyAppearanceEvent.class.isInstance( e ) && (
						lastAppear == null || lastAppear.before( e.getEntry().getTimestamp() ) ) )
				{
					lastAppear = e.getEntry().getTimestamp();
					lastTop = ((UsaProxyVisibilityEvent)e).getTopPosition();
					lastBottom = ((UsaProxyVisibilityEvent)e).getBottomPosition();
				}
			}
			
			if ( UsaProxyDisappearanceEvent.class.isInstance( e ) )
			{
				JSONArray topDisappearPoint = new JSONArray();
				topDisappearPoint.add( removeTimezone( e.getEntry().getTimestamp() ) + 1 );
				topDisappearPoint.add( null );
				top.add(topDisappearPoint);
					
				JSONArray bottomDisappearPoint = new JSONArray();
				bottomDisappearPoint.add( removeTimezone( e.getEntry().getTimestamp() ) + 1 );
				bottomDisappearPoint.add( null );
				bottom.add(bottomDisappearPoint);
			}
				
		}
		
		// Add an artificial end point if disappearances are missing
		// -> Last endpoint is missing
		if ( element.getDisappears().size() < element.getAppears().size() )
		{
			JSONArray topArtificialEndPoint = new JSONArray();
			topArtificialEndPoint.add( removeTimezone( lastAppear ) + ARTIFICIALEND );
			topArtificialEndPoint.add( lastTop );
			top.add(topArtificialEndPoint);
			
			JSONArray bottomArtificialEndPoint = new JSONArray();
			bottomArtificialEndPoint.add( removeTimezone( lastAppear ) + ARTIFICIALEND );
			bottomArtificialEndPoint.add( lastBottom );
			bottom.add(bottomArtificialEndPoint);
		}
		
		jsonElement.accumulate( "top", top );
		jsonElement.accumulate( "bottom", bottom );
		return jsonElement;
	}
	
	private static final long ARTIFICIALEND = 20000;
	
	private JSONObject getViewportLines( UsaProxyHTTPTraffic traffic )
	{
		traffic.sortScreens(); // make sure screens are sorted by timestamp
		JSONObject viewport = new JSONObject();
		Vector< JSONArray > topPoints = new Vector<JSONArray>();
		Vector< JSONArray > bottomPoints = new Vector<JSONArray>();
		for ( UsaProxyScreen s : traffic.getScreens() )
		{
			long beginTime;
			try
			{
				beginTime = removeTimezone( s.getInitialViewportEvent().getEntry().getTimestamp() );
			}
			catch ( NullPointerException e )
			{
				// Null pointer exceptions can occur if the logger has missed the
				// scroll stop event that triggers logging a viewport change event.
				// Such viewport states must be ignored.
				continue;
			}
			
			long endTime;
			try
			{
				endTime = removeTimezone( s.getScrollStart().getEntry().getTimestamp() );
			}
			catch ( NullPointerException e )
			{
				// If the end time cannot be determined, the viewport has either
				// been resized or this is the last screen. Generate an end time.
				if ( UsaProxySessionStore.getScreenById(traffic.getSessionID(), new Integer(s.getID() + 1).toString()) != null )
				{
					// If a next screen exists, this was a resize event
					endTime = beginTime + 1;
				}
				else
				{
					// If a next screen does not exist, this was the last screen
					// and we should generate an end time further in future.
					endTime = beginTime + ARTIFICIALEND;
				}
			}
				
			JSONArray topBegin = new JSONArray();
			topBegin.add(beginTime);
			// Top can be NaN if JS calculation failed
			Double top = s.getInitialViewportEvent().getViewportTop();
			Double notNaNTop = !top.isNaN() ? top : 0.0;
			topBegin.add( notNaNTop );
			topPoints.add( topBegin );
				
			JSONArray topEnd = new JSONArray();
			topEnd.add(endTime);
			topEnd.add( notNaNTop );
			topPoints.add( topEnd );
				
			JSONArray bottomBegin = new JSONArray();
			bottomBegin.add(beginTime);
			// Bottom can be NaN if JS calculation failed
			Double bottom = s.getInitialViewportEvent().getViewportBottom();
			Double notNaNBottom = !bottom.isNaN() ? bottom : 0.0;
			bottomBegin.add(notNaNBottom);
			bottomPoints.add( bottomBegin );
				
			JSONArray bottomEnd = new JSONArray();
			bottomEnd.add(endTime);
			bottomEnd.add(notNaNBottom);
			bottomPoints.add( bottomEnd );
		}
		viewport.accumulate( "top", topPoints );
		viewport.accumulate( "bottom", bottomPoints );
		return viewport;
	}
}

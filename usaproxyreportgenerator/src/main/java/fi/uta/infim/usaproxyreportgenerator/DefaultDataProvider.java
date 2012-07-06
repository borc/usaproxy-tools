package fi.uta.infim.usaproxyreportgenerator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import fi.uta.infim.usaproxylogparser.UsaProxyAppearanceEvent;
import fi.uta.infim.usaproxylogparser.UsaProxyDOMElement;
import fi.uta.infim.usaproxylogparser.UsaProxyDisappearanceEvent;
import fi.uta.infim.usaproxylogparser.UsaProxyHTTPTraffic;
import fi.uta.infim.usaproxylogparser.UsaProxyPageEvent;
import fi.uta.infim.usaproxylogparser.UsaProxyScreen;
import fi.uta.infim.usaproxylogparser.UsaProxySessionStore;
import fi.uta.infim.usaproxylogparser.UsaProxyVisibilityEvent;

public class DefaultDataProvider implements IBrowsingDataProvider {

	/**
	 * Milliseconds to add to the last appearances of objects in order to 
	 * generate an artificial disappearance time if the actual disappearance 
	 * was not logged.
	 */
	private static final long ARTIFICIALEND = 20000;
	
	private JSONObject viewportLines = null;
	
	private UsaProxyHTTPTraffic traffic = null;
	
	protected DefaultDataProvider(UsaProxyHTTPTraffic traffic) {
		super();
		this.traffic = traffic;
	}

	/**
	 * Extracts viewport states from the supplied http traffic session object
	 * and creates Flot-compatible data arrays from them. The returned object
	 * contains two members, 'top' and 'bottom', who hold the top data array
	 * and bottom data array, respectively.
	 * @param traffic the traffic session to extract viewport states from
	 * @return a JSON object containing Flot-compatible data arrays for plotting
	 */
	private JSONObject getViewportLines( UsaProxyHTTPTraffic traffic )
	{
		JSONObject viewport = new JSONObject();
		Vector< JSONArray > topPoints = new Vector<JSONArray>();
		Vector< JSONArray > bottomPoints = new Vector<JSONArray>();
		for ( UsaProxyScreen s : traffic.getScreens() )
		{
			long beginTime;
			try
			{
				beginTime = UsaProxyHTTPTrafficJSONProcessor.removeTimezone( 
						s.getInitialViewportEvent().getEntry().getTimestamp() );
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
				endTime = UsaProxyHTTPTrafficJSONProcessor.removeTimezone( 
						s.getScrollStart().getEntry().getTimestamp() );
			}
			catch ( NullPointerException e )
			{
				// If the end time cannot be determined, the viewport has either
				// been resized or this is the last screen. Generate an end time.
				UsaProxyScreen nextScreen = UsaProxySessionStore.getScreenById( 
						traffic.getSessionID(), new Long( s.getScreenID() + 1 ).toString() );
				if ( nextScreen != null )
				{
					// Was resized, take next screen into account
					endTime = UsaProxyHTTPTrafficJSONProcessor.removeTimezone( 
							nextScreen.getInitialViewportEvent().getEntry().getTimestamp() ) - 1L;
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
	
	/**
	 * Extracts element appearances and disappearances from an element object
	 * and creates Flot-compatible data arrays for top and bottom edges.
	 * The top edge data is contained in the returned object's member called 'top'
	 * and the bottom edge data in a member called 'bottom'. 
	 * @param element the dom element to extract sightings from
	 * @return a JSON object containing two Flot-compatible data arrays for plotting
	 */
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
				topPoint.add( UsaProxyHTTPTrafficJSONProcessor.removeTimezone( 
						e.getEntry().getTimestamp() ) );
				topPoint.add( ((UsaProxyVisibilityEvent)e).getTopPosition() );
				top.add(topPoint);
				
				JSONArray bottomPoint = new JSONArray();
				bottomPoint.add( UsaProxyHTTPTrafficJSONProcessor.removeTimezone( 
						e.getEntry().getTimestamp() ) );
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
				topDisappearPoint.add( UsaProxyHTTPTrafficJSONProcessor.removeTimezone( 
						e.getEntry().getTimestamp() ) + 1 );
				topDisappearPoint.add( null );
				top.add(topDisappearPoint);
					
				JSONArray bottomDisappearPoint = new JSONArray();
				bottomDisappearPoint.add( UsaProxyHTTPTrafficJSONProcessor.removeTimezone( 
						e.getEntry().getTimestamp() ) + 1 );
				bottomDisappearPoint.add( null );
				bottom.add(bottomDisappearPoint);
			}
				
		}
		
		// Add an artificial end point if disappearances are missing
		// -> Last endpoint is missing
		if ( element.getDisappears().size() < element.getAppears().size() )
		{
			JSONArray topArtificialEndPoint = new JSONArray();
			topArtificialEndPoint.add( UsaProxyHTTPTrafficJSONProcessor.removeTimezone( 
					lastAppear ) + ARTIFICIALEND );
			topArtificialEndPoint.add( lastTop );
			top.add(topArtificialEndPoint);
			
			JSONArray bottomArtificialEndPoint = new JSONArray();
			bottomArtificialEndPoint.add( UsaProxyHTTPTrafficJSONProcessor.removeTimezone( 
					lastAppear ) + ARTIFICIALEND );
			bottomArtificialEndPoint.add( lastBottom );
			bottom.add(bottomArtificialEndPoint);
		}
		
		jsonElement.accumulate( "top", top );
		jsonElement.accumulate( "bottom", bottom );
		return jsonElement;
	}

	@Override
	public JSONArray getViewportMovementTopDataset() {
		if ( viewportLines == null ) viewportLines = getViewportLines( traffic );
		return viewportLines.getJSONArray( "top" );
	}

	@Override
	public JSONArray getViewportMovementBottomDataset() {
		if ( viewportLines == null ) viewportLines = getViewportLines( traffic );
		return viewportLines.getJSONArray( "bottom" );
	}

	@Override
	public Map<String, JSONArray> getDOMElementSightingsTopDatasets() {
		if ( domData == null ) updateDOMElementData(traffic);
		return domData.domTops;
	}

	@Override
	public Map<String, JSONArray> getDOMElementSightingsBottomDatasets() {
		if ( domData == null ) updateDOMElementData(traffic);
		return domData.domBottoms;
	}

	private class DOMData {
		public Map<String, JSONArray> domTops = null;
		
		public Map<String, JSONArray> domBottoms = null;
		
		public Map< String, JSONObject > domDetails = null;
	}
	
	private DOMData domData = null;
	
	private void updateDOMElementData( UsaProxyHTTPTraffic traffic )
	{
		domData = new DOMData();
		domData.domTops = new HashMap<String, JSONArray>();
		domData.domBottoms = new HashMap<String, JSONArray>();
		domData.domDetails = new HashMap<String, JSONObject>();
		
		for ( UsaProxyDOMElement e : traffic.getDomElements() )
		{
			if ( e == null || e.getPath() == null ) continue; // Don't process empty elements
			JSONObject thisSightings = getSightings(e);
			domData.domTops.put( e.getPath(), thisSightings.getJSONArray( "top" ) );
			domData.domBottoms.put( e.getPath(), thisSightings.getJSONArray( "bottom" ) );
			domData.domDetails.put( e.getPath(), JSONObject.fromObject(e, App.getConfig()) );
		}
	}

	@Override
	public Map<String, JSONObject> getDOMElementDetails() {
		if ( domData == null ) updateDOMElementData(traffic);
		return domData.domDetails;
	}
}

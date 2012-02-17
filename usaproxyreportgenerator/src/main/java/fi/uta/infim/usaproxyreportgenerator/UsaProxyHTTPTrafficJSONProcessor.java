package fi.uta.infim.usaproxyreportgenerator;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import fi.uta.infim.usaproxylogparser.UsaProxyAppearanceEvent;
import fi.uta.infim.usaproxylogparser.UsaProxyDOMElement;
import fi.uta.infim.usaproxylogparser.UsaProxyHTTPTraffic;
import fi.uta.infim.usaproxylogparser.UsaProxyPageEvent;
import fi.uta.infim.usaproxylogparser.UsaProxyScreen;
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
			jsonHTTPTraffic.accumulate( "timestamp", null == traffic.getEntry() ? null : 
				traffic.getEntry().getTimestamp().getTime() );
			
			return jsonHTTPTraffic;
		} catch (Exception e) {
			throw new RuntimeException( e );
		}
	}

	private JSONObject getDOMElements( UsaProxyHTTPTraffic traffic ) throws XPathExpressionException, IOException, TransformerException
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
		movement.add( getTopDataset( vpMovementDatasetName, movements, "Viewport top") );
		movement.add( getBottomDataset( vpMovementDatasetName, movements, "rgb(255,50,50)", "Viewport bottom") );
		
		return movement;
	}
	
	static private String generateDatasetTopId( String elementTopName )
	{
		return elementTopName + "Top";
	}
	
	private JSONObject getTopDataset( String elementTopName, JSONObject sightings, String label )
	{
		JSONObject dataset = new JSONObject();
		dataset.accumulate( "data", sightings.getJSONArray( "top" ) );
		dataset.accumulate( "id", generateDatasetTopId( elementTopName ) );
		JSONObject lines = new JSONObject();
		lines.accumulate( "show", true );
		lines.accumulate( "lineWidth", 0 );
		dataset.accumulate( "lines", lines );
		dataset.accumulate( "invisibleLabel", label );
		dataset.accumulate( "hoverable", true );
		return dataset;
	}
	
	private JSONObject getBottomDataset( String elementTopName, JSONObject sightings, String color, String label )
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
		dataset.accumulate( "invisibleLabel", label );
		return dataset;
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
			if ( UsaProxyAppearanceEvent.class.isInstance(e) )
			{
				JSONArray topPoint = new JSONArray();
				topPoint.add( e.getEntry().getTimestamp().getTime() );
				topPoint.add( ((UsaProxyAppearanceEvent)e).getTopPosition() );
				top.add(topPoint);
				
				JSONArray bottomPoint = new JSONArray();
				bottomPoint.add( e.getEntry().getTimestamp().getTime() );
				bottomPoint.add( ((UsaProxyAppearanceEvent)e).getBottomPosition() );
				bottom.add(bottomPoint);
				
				if ( ((UsaProxyAppearanceEvent)e).isDisappearance() )
				{
					JSONArray topDisappearPoint = new JSONArray();
					topDisappearPoint.add( e.getEntry().getTimestamp().getTime() + 1 );
					topDisappearPoint.add( null );
					top.add(topDisappearPoint);
					
					JSONArray bottomDisappearPoint = new JSONArray();
					bottomDisappearPoint.add( e.getEntry().getTimestamp().getTime() + 1 );
					bottomDisappearPoint.add( null );
					bottom.add(bottomDisappearPoint);
				}
				else
				{
					if ( lastAppear == null || lastAppear.before( e.getEntry().getTimestamp() ) )
					{
						lastAppear = e.getEntry().getTimestamp();
						lastTop = ((UsaProxyAppearanceEvent)e).getTopPosition();
						lastBottom = ((UsaProxyAppearanceEvent)e).getBottomPosition();
					}
				}
				
				
			}
		}
		
		// Add an artificial end point if disappearances are missing
		// -> Last endpoint is missing
		if ( element.getDisappears().size() < element.getAppears().size() )
		{
			JSONArray topArtificialEndPoint = new JSONArray();
			topArtificialEndPoint.add( lastAppear.getTime() + ARTIFICIALEND );
			topArtificialEndPoint.add( lastTop );
			top.add(topArtificialEndPoint);
			
			JSONArray bottomArtificialEndPoint = new JSONArray();
			bottomArtificialEndPoint.add( lastAppear.getTime() + ARTIFICIALEND );
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
				beginTime = s.getInitialViewportEvent().getEntry().getTimestamp().getTime();
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
				endTime = s.getScrollStart().getEntry().getTimestamp().getTime();
			}
			catch ( NullPointerException e )
			{
				// If the end time cannot be determined, assume an end time of
				// beginTime + 20 seconds.
				endTime = beginTime + ARTIFICIALEND;
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

/*
 * UsaProxyReportGenerator - tool for processing UsaProxy-fork logs
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

package fi.uta.infim.usaproxyreportgenerator;

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

/**
 * Processes UsaProxyHTTPTraffic objects into JSON objects.
 * @author Teemu Pääkkönen
 *
 */
public class UsaProxyHTTPTrafficJSONProcessor implements JsonBeanProcessor {

	/**
	 * Processes a UsaProxyHTTPTraffic object into a JSON object
	 */
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

	/**
	 * Iterates through a HTTPTraffic object for DOM elements. Returns a JSON
	 * object with two members: 'details' and 'sightings'. 'Details' contains
	 * detailed information on each object. 'Sightings' contains an array of
	 * data points for plotting the element's sightings.
	 * @param traffic the traffic object whose dom elements are iterated through
	 * @return a JSON object containing the details and sightings of every element
	 */
	private JSONObject getDOMElements( UsaProxyHTTPTraffic traffic )
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
	
	/**
	 * Iterates through a HTTP traffic session object to find all viewport states 
	 * during the session. Creates a JSON array containing data points ready to
	 * be plotted using flot.
	 * @param traffic the HTTP traffic session to be iterated over
	 * @return a JSON array containing ready-to-plot datapoints for flot
	 */
	private JSONArray getViewportMovement( UsaProxyHTTPTraffic traffic )
	{
		JSONArray movement = new JSONArray();
		JSONObject movements = getViewportLines(traffic);
		String vpMovementDatasetName = "_viewport";
		movement.add( getTopDataset( vpMovementDatasetName, movements, "_vpTop") );
		movement.add( getBottomDataset( vpMovementDatasetName, movements, "rgb(255,50,50)", "_vpBottom") );
		
		return movement;
	}
	
	/**
	 * Generates a name for the viewport/element top edge flot dataset.
	 * This is used for filling the are between the top and bottom edge.
	 * Deterministic -> each unique input generates the same unique output each time.
	 * @param elementTopName the base id to generate the id from.
	 * @return the generated ID
	 */
	static private String generateDatasetTopId( String elementTopName )
	{
		return elementTopName + "Top";
	}
	
	/**
	 * Creates a Flot dataset JSON object. Only to be used with Flot.
	 * This dataset represents the top edge of the plotted element.
	 * @param elementTopName a unique element id
	 * @param sightings a JSON object containing the actual plot data
	 * @param elementDomId Element's UsaProxy DOM path
	 * @return the created Flot dataset (a JSON object)
	 */
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
	
	/**
	 * Creates a Flot dataset JSON object. Only to be used with Flot.
	 * This dataset represents the bottom edge of the plotted element.
	 * @param elementTopName a unique element id - notice that this id should
	 * be the same as what was passed to {@link #getTopDataset}.
	 * @param sightings a JSON object containing the actual plot data
	 * @param color fill color for the area between top and bottom
	 * @param elementDomId Element's UsaProxy DOM path
	 * @return the created Flot dataset (a JSON object)
	 */
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
	
	/**
	 * "Removes" the time zone from a timestamp by subtracting the time zone
	 * offset from the time stamp. This is done since UsaProxy log entries 
	 * don't contain a time zone and Flot expects time zone -less timestamps.
	 * @param timestamp the timestamp to remove time zone from
	 * @return time zone -less version of the timestamp in unix epoch style
	 */
	@SuppressWarnings("deprecation")
	public static long removeTimezone( Date timestamp )
	{
		return timestamp.getTime() - (60 * 1000 * timestamp.getTimezoneOffset());
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
	
	/**
	 * Milliseconds to add to the last appearances of objects in order to 
	 * generate an artificial disappearance time if the actual disappearance 
	 * was not logged.
	 */
	private static final long ARTIFICIALEND = 20000;
	
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
				if ( UsaProxySessionStore.getScreenById(traffic.getSessionID(), new Integer(s.getScreenID() + 1).toString()) != null )
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

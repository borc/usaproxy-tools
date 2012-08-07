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

import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;

import fi.uta.infim.usaproxylogparser.UsaProxyHTTPTraffic;
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

	public static final AbstractMap.SimpleEntry<String, String> DEFAULTVIEWPORTCOLOR = 
			new AbstractMap.SimpleEntry<String, String>( "DEFAULTVIEWPORTCOLOR", "rgb(255,50,50)" );
	
	public static final AbstractMap.SimpleEntry<String, String> DEFAULTELEMENTCOLOR = 
			new AbstractMap.SimpleEntry<String, String>( "DEFAULTELEMENTCOLOR", "rgb(50,50,255)" );
	
	private DataProvider provider = null;
	
	/**
	 * Processes a UsaProxyHTTPTraffic object into a JSON object
	 */
	@Override
	public JSONObject processBean(Object arg0, JsonConfig arg1) {
		UsaProxyHTTPTraffic traffic = (UsaProxyHTTPTraffic) arg0;
		try {
			provider = App.getDataProviderClass().getDeclaredConstructor( 
					UsaProxyHTTPTraffic.class ).newInstance( traffic );
			JSONObject jsonHTTPTraffic = new JSONObject();
			jsonHTTPTraffic.accumulate( "viewportMovement", getViewportMovement(traffic));
			JSONObject domElements = new JSONObject();
			domElements.accumulate( "details", provider.getDOMElementDetails() );
			domElements.accumulate( "sightings", getDOMElements(traffic).get( "sightings" ) );
			jsonHTTPTraffic.accumulate( "domElements", domElements );
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
	 * Iterates through a HTTP traffic session object to find all viewport states 
	 * during the session. Creates a JSON array containing data points ready to
	 * be plotted using flot.
	 * @param traffic the HTTP traffic session to be iterated over
	 * @return a JSON array containing ready-to-plot datapoints for flot
	 */
	private JSONArray getViewportMovement(UsaProxyHTTPTraffic traffic) {
		JSONArray movement = new JSONArray();
		JSONObject movements = new JSONObject();
		movements.accumulate( "top", provider.getViewportMovementTopDataset() );
		movements.accumulate( "bottom", provider.getViewportMovementBottomDataset() );
		String vpMovementDatasetName = "_viewport";
		movement.add( getTopDataset( vpMovementDatasetName, movements.getJSONArray( "top" ), 
				UsaProxyHTTPTrafficJSONProcessor.DEFAULTVIEWPORTCOLOR.getValue(), null) );
		movement.add( getBottomDataset( vpMovementDatasetName, movements.getJSONArray( "bottom" ), 
				UsaProxyHTTPTrafficJSONProcessor.DEFAULTVIEWPORTCOLOR.getValue(), null) );
		
		return movement;
	}
	
	/**
	 * Creates a Flot dataset JSON object. Only to be used with Flot.
	 * This dataset represents the top edge of the plotted element.
	 * @param elementTopName a unique element id
	 * @param sightings a JSON object containing the actual plot data
	 * @param elementDomId Element's UsaProxy DOM path
	 * @return the created Flot dataset (a JSON object)
	 */
	private JSONObject getTopDataset( String elementTopName, JSONArray topSightings, String color, String elementDomId )
	{
		JSONObject dataset = new JSONObject();
		dataset.accumulate( "data", topSightings );
		dataset.accumulate( "id", generateDatasetTopId( elementTopName ) );
		JSONObject lines = new JSONObject();
		lines.accumulate( "show", true );
		lines.accumulate( "lineWidth", 0 );
		dataset.accumulate( "lines", lines );
		dataset.accumulate( "color", color );
		dataset.accumulate( "elementDomId", elementDomId );
		dataset.accumulate( "hoverable", true );
		return dataset;
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
	 * This dataset represents the bottom edge of the plotted element.
	 * @param elementTopName a unique element id - notice that this id should
	 * be the same as what was passed to {@link #getTopDataset}.
	 * @param sightings a JSON object containing the actual plot data
	 * @param color fill color for the area between top and bottom
	 * @param elementDomId Element's UsaProxy DOM path
	 * @return the created Flot dataset (a JSON object)
	 */
	private JSONObject getBottomDataset( String elementTopName, JSONArray bottomSightings, String color, String elementDomId )
	{
		JSONObject dataset = new JSONObject();
		dataset.accumulate( "data", bottomSightings );
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
		JSONArray sightings = new JSONArray();
		
		Map<String, JSONArray> topData = provider.getDOMElementSightingsTopDatasets();
		Map<String, JSONArray> bottomData = provider.getDOMElementSightingsBottomDatasets();
		
		for ( String domPath : topData.keySet() )
		{
			sightings.add( getTopDataset( domPath, topData.get(domPath), 
					UsaProxyHTTPTrafficJSONProcessor.DEFAULTELEMENTCOLOR.getValue(), domPath ) );
		}
		for ( String domPath : topData.keySet() )
		{
			sightings.add( getBottomDataset( domPath, bottomData.get(domPath), 
					UsaProxyHTTPTrafficJSONProcessor.DEFAULTELEMENTCOLOR.getValue(), domPath ) );
		}
		
		elements.accumulate( "details", provider.getDOMElementDetails() );
		elements.accumulate( "sightings", sightings );
		return elements;
	}
}

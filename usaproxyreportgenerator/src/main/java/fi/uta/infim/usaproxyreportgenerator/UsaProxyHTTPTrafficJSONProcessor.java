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

import fi.uta.infim.usaproxylogparser.UsaProxyHTTPTraffic;
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
	
	/**
	 * Processes a UsaProxyHTTPTraffic object into a JSON object
	 */
	@Override
	public JSONObject processBean(Object arg0, JsonConfig arg1) {
		UsaProxyHTTPTraffic traffic = (UsaProxyHTTPTraffic) arg0;
		IBrowsingDataProvider provider = new DefaultDataProvider();
		try {
			JSONObject jsonHTTPTraffic = new JSONObject();
			jsonHTTPTraffic.accumulate( "viewportMovement", provider.getViewportMovement(traffic));
			JSONObject domElements = new JSONObject();
			domElements.accumulate( "details", provider.getDOMElementDetails(traffic) );
			domElements.accumulate( "sightings", provider.getDOMElementSightings(traffic) );
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
	
	
	
}

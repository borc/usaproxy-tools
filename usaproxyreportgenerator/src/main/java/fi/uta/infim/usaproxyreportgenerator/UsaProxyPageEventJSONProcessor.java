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

import fi.uta.infim.usaproxylogparser.UsaProxyPageEvent;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

/**
 * Processor for different kinds of UsaProxy events
 * @author Teemu Pääkkönen
 *
 * @param <T> class type extending UsaProxyPageEvent.
 */
public class UsaProxyPageEventJSONProcessor< T extends UsaProxyPageEvent > implements JsonBeanProcessor {

	/**
	 * See {@link UsaProxyHTTPTrafficJSONProcessor#removeTimezone}
	 */
	private static long removeTimezone( Date timestamp )
	{
		return UsaProxyHTTPTrafficJSONProcessor.removeTimezone(timestamp);
	}
	
	/**
	 * Processes an event object into a JSON object. Processes attributes and
	 * the time stamp.
	 */
	@Override
	public JSONObject processBean(Object arg0, JsonConfig arg1) {
		@SuppressWarnings("unchecked")
		T event = (T)arg0;
		JSONObject jsonEvent = new JSONObject();
		jsonEvent.accumulate("attributes", event.getAttributes() );
		jsonEvent.accumulate( "timestamp", removeTimezone( event.getEntry().getTimestamp() ) );
		return jsonEvent;
	}

}

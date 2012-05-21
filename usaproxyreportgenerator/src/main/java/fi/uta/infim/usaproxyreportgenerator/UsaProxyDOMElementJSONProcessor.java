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

import fi.uta.infim.usaproxylogparser.UsaProxyDOMElement;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

/**
 * Processes UsaProxyDOMElement objects into JSON objects. Processes only the
 * following members: path, nodeName, contents.
 * @author Teemu Pääkkönen
 *
 */
public class UsaProxyDOMElementJSONProcessor implements JsonBeanProcessor {

	/**
	 * Processes a UsaProxyDOMElement into a JSON object.
	 */
	@Override
	public JSONObject processBean(Object arg0, JsonConfig arg1) {
		
		UsaProxyDOMElement element = (UsaProxyDOMElement) arg0;
		
		JSONObject details = new JSONObject();
		details.accumulate( "path", element.getPath() );
		details.accumulate( "nodeName", element.getNodeName() );
		details.accumulate( "content", element.getContents() );
		
		// Original image URL for image elements, so the URL can be shown in
		// element details dialog.
		if ( "img".equalsIgnoreCase(element.getNodeName()) )
		{
			// Grab the image URL from the first appearance event
			details.accumulate( "img", element.getAppears().iterator().next().getAttributes().get( "img" ) );
		}
		
		return details;
	}

}

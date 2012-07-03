package fi.uta.infim.usaproxyreportgenerator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import fi.uta.infim.usaproxylogparser.UsaProxyHTTPTraffic;

/**
 * Interface for data providers. The report generator requires two 
 * data sets: a viewport (background) data set, and a DOM element data set.
 * The reports also require a set of element details corresponding to the
 * flot DOM element data set. 
 * @author Teemu Pääkkönen
 *
 */
public interface IBrowsingDataProvider {

	/**
	 * Provides viewport movement data for Flot.
	 * @param traffic the traffic object from which to extract the data
	 * @return Flot-compatible data array
	 */
	public JSONArray getViewportMovement( UsaProxyHTTPTraffic traffic );
	
	/**
	 * Provides DOM element visibility data for Flot.
	 * @param traffic the traffic object from which to extract the data
	 * @return Flot-compatible data array
	 */
	public JSONArray getDOMElementSightings( UsaProxyHTTPTraffic traffic );
	
	/**
	 * Provides element details data for the report generator.
	 * @param traffic the traffic object from which to extract the data
	 * @return Details data object for the report generator
	 */
	public JSONObject getDOMElementDetails( UsaProxyHTTPTraffic traffic );
	
}

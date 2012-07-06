package fi.uta.infim.usaproxyreportgenerator;

import java.util.Map;

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
	 * Provides the top dataset of viewport movement data for Flot.
	 * @param traffic the traffic object from which to extract the data
	 * @return Flot-compatible data array
	 */
	public JSONArray getViewportMovementTopDataset();
	
	/**
	 * Provides the bottom dataset of viewport movement data for Flot.
	 * @param traffic the traffic object from which to extract the data
	 * @return Flot-compatible data array
	 */
	public JSONArray getViewportMovementBottomDataset();
	
	/**
	 * Provides the top datasets of DOM element visibility data for Flot.
	 * Keys are unique identifiers that match the element details keys.
	 * @param traffic the traffic object from which to extract the data
	 * @return Flot-compatible data array
	 */
	public Map< String, JSONArray > getDOMElementSightingsTopDatasets();
	
	/**
	 * Provides the bottom datasets of DOM element visibility data for Flot.
	 * Keys are unique identifiers that match the element details keys.
	 * @param traffic the traffic object from which to extract the data
	 * @return Flot-compatible data array
	 */
	public Map< String, JSONArray > getDOMElementSightingsBottomDatasets();
	
	/**
	 * Provides element details data for the report generator.
	 * Keys are unique identifiers that match the element sightings keys.
	 * @param traffic the traffic object from which to extract the data
	 * @return Details data object for the report generator
	 */
	public Map< String, JSONObject > getDOMElementDetails();
	
}

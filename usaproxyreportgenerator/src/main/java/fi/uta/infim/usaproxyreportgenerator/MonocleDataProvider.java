package fi.uta.infim.usaproxyreportgenerator;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import fi.uta.infim.usaproxylogparser.EventType;
import fi.uta.infim.usaproxylogparser.UsaProxyHTTPTraffic;
import fi.uta.infim.usaproxylogparser.UsaProxyPageEvent;

public class MonocleDataProvider extends DefaultDataProvider implements IBrowsingDataProvider {

	private class ViewportData {
		public JSONArray tops;
		public JSONArray bottoms;
	}
	
	private ViewportData viewportData = null;
	
	MonocleDataProvider(UsaProxyHTTPTraffic traffic) {
		super( traffic );
	}

	private void updateViewportMovement(UsaProxyHTTPTraffic traffic) {
		viewportData = new ViewportData();
		viewportData.tops = new JSONArray();
		viewportData.bottoms = new JSONArray();
		for ( UsaProxyPageEvent event : traffic.getEvents() )
		{
			if ( event.getType().equals( EventType.PLUGIN ) && "monocle:turn".equals( 
					event.getAttributes().get("eventType") ) )
			{
				// Top point
				JSONArray topPoint = new JSONArray();
				topPoint.add( UsaProxyHTTPTrafficJSONProcessor.removeTimezone( 
						event.getEntry().getTimestamp() ) );
				topPoint.add( Double.parseDouble( event.getAttributes().get( "top" ) ) );
				viewportData.tops.add( topPoint );
				
				// Bottom point
				JSONArray bottomPoint = new JSONArray();
				bottomPoint.add( UsaProxyHTTPTrafficJSONProcessor.removeTimezone( 
						event.getEntry().getTimestamp() ) );
				bottomPoint.add( Double.parseDouble( event.getAttributes().get( "bottom" ) ) );
				viewportData.bottoms.add( bottomPoint );
			}
		}
	}

	@Override
	public JSONArray getViewportMovementTopDataset() {
		if ( viewportData == null ) updateViewportMovement(traffic);
		return viewportData.tops;
	}

	@Override
	public JSONArray getViewportMovementBottomDataset() {
		if ( viewportData == null ) updateViewportMovement(traffic);
		return viewportData.bottoms;
	}

}

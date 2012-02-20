package fi.uta.infim.usaproxyreportgenerator;

import fi.uta.infim.usaproxylogparser.UsaProxyPageEvent;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

public class UsaProxyPageEventJSONProcessor< T extends UsaProxyPageEvent > implements JsonBeanProcessor {

	@Override
	public JSONObject processBean(Object arg0, JsonConfig arg1) {
		@SuppressWarnings("unchecked")
		T event = (T)arg0;
		JSONObject jsonEvent = new JSONObject();
		jsonEvent.accumulate("attributes", event.getAttributes() );
		jsonEvent.accumulate( "timestamp", event.getEntry().getTimestamp().getTime() );
		return jsonEvent;
	}

}

package fi.uta.infim.usaproxyreportgenerator;

import fi.uta.infim.usaproxylogparser.UsaProxyScreen;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

public class UsaProxyScreenJSONProcessor implements JsonBeanProcessor {

	@Override
	public JSONObject processBean(Object arg0, JsonConfig arg1) {
		UsaProxyScreen screen = (UsaProxyScreen) arg0;
		JSONObject jsonScreen = new JSONObject();
		jsonScreen.accumulate( "id", screen.getID(), arg1 );
		return jsonScreen;
	}

}

package fi.uta.infim.usaproxyreportgenerator;

import fi.uta.infim.usaproxylogparser.UsaProxyDOMElement;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

public class UsaProxyDOMElementJSONProcessor implements JsonBeanProcessor {

	@Override
	public JSONObject processBean(Object arg0, JsonConfig arg1) {
		
		UsaProxyDOMElement element = (UsaProxyDOMElement) arg0;
		
		JSONObject details = new JSONObject();
		details.accumulate( "path", element.getPath() );
		details.accumulate( "nodeName", element.getNodeName() );
		details.accumulate( "content", element.getContents() );
		
		return details;
	}

}

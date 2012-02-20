package fi.uta.infim.usaproxyreportgenerator;

import javax.xml.xpath.XPathExpressionException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Node;

import fi.uta.infim.usaproxylogparser.UsaProxyDOMElement;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

public class UsaProxyDOMElementJSONProcessor implements JsonBeanProcessor {

	@Override
	public JSONObject processBean(Object arg0, JsonConfig arg1) {
		
		UsaProxyDOMElement element = (UsaProxyDOMElement) arg0;
		
		Node trafficLogRoot;
		try {
			trafficLogRoot = App.getLogFileHandler().parseLog( element.getHttpTraffic() );
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
		
		JSONObject details = new JSONObject();
		details.accumulate( "path", element.getPath() );
		details.accumulate( "appearances", element.getAppears(), App.getConfig() );
		details.accumulate( "disappearances", element.getDisappears(), App.getConfig() );
		details.accumulate( "nodeName", element.getNodeName() );
		try {
			details.accumulate( "content", XPathAPI.eval( 
					trafficLogRoot, 
					UsaProxyHTTPTrafficLogHandler.usaProxyDOMPathToXPath( element.getPath() ) ).toString() );
		} catch (XPathExpressionException e) {
			throw new RuntimeException( "Error with XPath expression generated from path " + element.getPath() );
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return details;
	}

}

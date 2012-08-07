package fi.uta.infim.usaproxyreportgenerator;

import fi.uta.infim.usaproxylogparser.UsaProxyHTTPTraffic;

public abstract class DataProvider implements IBrowsingDataProvider {

	protected UsaProxyHTTPTraffic traffic = null;
	
	protected DataProvider( UsaProxyHTTPTraffic traffic ) {
		super();
		this.traffic = traffic;
	}

}

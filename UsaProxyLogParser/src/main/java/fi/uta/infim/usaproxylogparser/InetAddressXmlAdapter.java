package fi.uta.infim.usaproxylogparser;

import java.net.InetAddress;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class InetAddressXmlAdapter extends XmlAdapter< String, InetAddress > {

	@Override
	public String marshal(InetAddress arg0) throws Exception {
		return arg0.getHostAddress();
	}

	@Override
	public InetAddress unmarshal(String arg0) throws Exception {
		return UsaProxyLog.ipAddressStringToInetAddress(arg0);
	}
	
}
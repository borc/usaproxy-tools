package fi.uta.infim.usaproxylogparser;

import java.net.InetAddress;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter class for Marshalling/Unmarshalling InetAddress objects.
 * @author Teemu P��kk�nen
 *
 */
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
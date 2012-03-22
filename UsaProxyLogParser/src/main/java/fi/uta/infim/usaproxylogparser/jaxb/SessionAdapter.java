package fi.uta.infim.usaproxylogparser.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import fi.uta.infim.usaproxylogparser.UsaProxySession;
import fi.uta.infim.usaproxylogparser.UsaProxySessionStore;

/**
 * Adapter for marshalling and unmarshalling UsaProxySession objects.
 * INCOMPLETE! Only for testing purposes.
 * @author Teemu Pääkkönen
 *
 */
public class SessionAdapter extends XmlAdapter< String, UsaProxySession > {

	private SessionAdapter() {
		super();
	}

	@Override
	public String marshal(UsaProxySession arg0) throws Exception {
		return arg0.getSessionID();
	}

	@Override
	public UsaProxySession unmarshal(String arg0) throws Exception {
		return UsaProxySessionStore.getSessionById(arg0);
	}
	
}
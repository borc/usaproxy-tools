/*
 * UsaProxyLogParser - Java API for UsaProxy-fork logs
 *  Copyright (C) 2012 Teemu Pääkkönen - University of Tampere
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
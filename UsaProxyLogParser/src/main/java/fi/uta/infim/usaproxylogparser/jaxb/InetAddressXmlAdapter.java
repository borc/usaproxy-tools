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

import java.net.InetAddress;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import fi.uta.infim.usaproxylogparser.UsaProxyLog;

/**
 * Adapter class for Marshalling/Unmarshalling InetAddress objects.
 * @author Teemu Pääkkönen
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
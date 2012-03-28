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

package fi.uta.infim.usaproxylogparser;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * UsaProxy 2.0 log parser interface definition.
 * @author Teemu Pääkkönen
 *
 */
public interface IUsaProxyLogParser {

	/**
	 * Parses the provided UsaProxy log and generates an object-based
	 * representation of it. The representation can be used to generate XML
	 * output using JAXB.
	 * @param file The file containing the log.
	 * @return The root object (node) of the log.
	 */
	public UsaProxyLog parseLog( File file ) throws IOException, ParseException;
	
	/**
	 * Parses the provided UsaProxy log and generates an object-based
	 * representation of it. The representation can be used to generate XML
	 * output using JAXB.
	 * @param filename The file name of the log file.
	 * @return The root object (node) of the log.
	 */
	public UsaProxyLog parseLog( String filename ) throws IOException, ParseException;
		
}

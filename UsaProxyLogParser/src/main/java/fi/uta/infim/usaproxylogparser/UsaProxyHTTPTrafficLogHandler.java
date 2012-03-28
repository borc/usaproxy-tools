/*
 * UsaProxyLogParser - Java API for UsaProxy-fork logs
 *  Copyright (C) 2012 Teemu P��kk�nen - University of Tampere
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

/**
 * A class that handles finding HTTP traffic log files.
 * @author Teemu P��kk�nen
 *
 */
public class UsaProxyHTTPTrafficLogHandler {

private File logFile; // Log file location
	
	/**
	 * Constructor.
	 * @param logFile the UsaProxy main log file (log.txt)
	 */
	public UsaProxyHTTPTrafficLogHandler(File logFile) {
		super();
		this.logFile = logFile;
	}

	/**
	 * Finds the root directory of http traffic logs
	 * @return a File object containing the root directory of http traffic logs
	 */
	private File findHTTPTrafficLogRoot()
	{
		return new File( logFile.getParentFile(), "httpTraffic/log" );
	}
	
	/**
	 * Finds a single HTTP traffic log file. 
	 * @param traffic the http traffic session whose log is to be searched for
	 * @return the http traffic log file
	 */
	public File findHTTPTrafficLog( UsaProxyHTTPTraffic traffic )
	{
		return new File( findHTTPTrafficLogRoot(), "httpTraffic" + traffic.getSessionID() + ".txt" );
	}
	
}

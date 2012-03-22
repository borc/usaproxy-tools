package fi.uta.infim.usaproxylogparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * A class that parses UsaProxy HTTP traffic log files for HTTP headers.
 * @author Teemu Pääkkönen
 *
 */
class UsaProxyHTTPTrafficLogParser {

	/**
	 * A handler is needed for accessing the files.
	 */
	private UsaProxyHTTPTrafficLogHandler handler;

	/**
	 * Constructor.
	 * @param handler a handler for accessing the http log files
	 */
	UsaProxyHTTPTrafficLogParser(UsaProxyHTTPTrafficLogHandler handler) {
		super();
		this.handler = handler;
	}
	
	/**
	 * Finds the HTTP request headers for a particular http traffic session
	 * @param traffic the http traffic session whose headers are searched for
	 * @return the HTTP request headers of the supplied http traffic session
	 * @throws IOException when http traffic log file is not found or is inaccessible
	 */
	HashMap< String, String > getRequestHeaders( UsaProxyHTTPTraffic traffic ) throws IOException
	{
		return getHeaders(traffic, false);
	}
	
	/**
	 * Finds the HTTP response headers for a http traffic session
	 * @param traffic the http traffic session object to get headers for
	 * @return the HTTP response headers for the supplied http traffic session
	 * @throws IOException when the http traffic log file is not found or cannot be read
	 */
	HashMap< String, String > getResponseHeaders( UsaProxyHTTPTraffic traffic ) throws IOException
	{
		return getHeaders(traffic, true);
	}
	
	/**
	 * Grabs HTTP headers from a http log file
	 * @param traffic the traffic session to get headers for
	 * @param response get response headers? if false, request headers are returned.
	 * @return HTTP headers in key-value pairs
	 * @throws IOException when the HTTP traffic log is not found or cannot be read
	 */
	private HashMap< String, String > getHeaders( UsaProxyHTTPTraffic traffic, boolean response ) throws IOException
	{
		File httpTrafficLogFile = handler.findHTTPTrafficLog(traffic);
		
		HashMap< String, String > headers = new HashMap<String, String>();
		
		// Init a reader for the file
		BufferedReader filereader = new BufferedReader( 
				new InputStreamReader( new FileInputStream(httpTrafficLogFile) ) );
		
		// Find the beginning of request/response headers, 
		// marked by the string "[request]"/"[response]"
		String line;
		while ( (line = filereader.readLine()) != null && !line.equals( response ? "[response]" : "[request]" ) );
		
		// Skip the request/response line
		filereader.readLine();
		
		// Read headers until an empty line is encountered
		while ( (line = filereader.readLine()) != null && !line.equals( "" ) )
		{
			int keyValueSeparatorAt = line.indexOf( ':' );
			headers.put( line.substring(0, keyValueSeparatorAt), 
					line.substring(keyValueSeparatorAt + 2) );
		}
		
		return headers;
	}
}

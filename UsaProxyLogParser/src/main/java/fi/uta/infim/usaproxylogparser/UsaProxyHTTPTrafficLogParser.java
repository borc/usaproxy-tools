package fi.uta.infim.usaproxylogparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class UsaProxyHTTPTrafficLogParser {

	private UsaProxyHTTPTrafficLogHandler handler;

	public UsaProxyHTTPTrafficLogParser(UsaProxyHTTPTrafficLogHandler handler) {
		super();
		this.handler = handler;
	}
	
	public HashMap< String, String > getRequestHeaders( UsaProxyHTTPTraffic traffic ) throws IOException
	{
		return getHeaders(traffic, false);
	}
	
	public HashMap< String, String > getResponseHeaders( UsaProxyHTTPTraffic traffic ) throws IOException
	{
		return getHeaders(traffic, true);
	}
	
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

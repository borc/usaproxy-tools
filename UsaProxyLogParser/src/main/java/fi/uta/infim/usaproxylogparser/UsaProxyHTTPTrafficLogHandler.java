package fi.uta.infim.usaproxylogparser;

import java.io.File;

/**
 * A class that handles finding HTTP traffic log files.
 * @author Teemu Pääkkönen
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

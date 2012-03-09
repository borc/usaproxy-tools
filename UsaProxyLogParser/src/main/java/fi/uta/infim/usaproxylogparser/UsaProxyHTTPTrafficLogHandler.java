package fi.uta.infim.usaproxylogparser;

import java.io.File;

public class UsaProxyHTTPTrafficLogHandler {

private File logFile; // Log file location
	
	public UsaProxyHTTPTrafficLogHandler(File logFile) {
		super();
		this.logFile = logFile;
	}

	private File findHTTPTrafficLogRoot()
	{
		return new File( logFile.getParentFile(), "httpTraffic/log" );
	}
	
	public File findHTTPTrafficLog( UsaProxyHTTPTraffic traffic )
	{
		return new File( findHTTPTrafficLogRoot(), "httpTraffic" + traffic.getSessionID() + ".txt" );
	}
	
}

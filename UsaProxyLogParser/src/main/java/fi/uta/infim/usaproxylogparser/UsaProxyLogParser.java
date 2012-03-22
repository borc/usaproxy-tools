package fi.uta.infim.usaproxylogparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import fi.uta.infim.usaproxylogparser.antlr.TLexer;
import fi.uta.infim.usaproxylogparser.antlr.TParser;
import fi.uta.infim.usaproxylogparser.antlr.TParser.log_return;

/**
 * An ANTLR-based implementation of the {@link IUsaProxyLogParser} interface.
 * @author Teemu Pääkkönen
 *
 */
public class UsaProxyLogParser implements IUsaProxyLogParser {

	/**
	 * The main UsaProxy log file (log.txt)
	 */
	private File logFile;
	
	@Override
	public UsaProxyLog parseLog(File file) throws IOException, ParseException {
		FileInputStream fis = new FileInputStream(file);
		logFile = file;
		return parseLog(fis);
	}

	@Override
	public UsaProxyLog parseLog(String filename) throws IOException, ParseException {
		File logFile = new File( filename );
		return parseLog(logFile);
	}

	/**
	 * Parses an UsaProxy main log file from a stream
	 * @param logStream a stream containing the log
	 * @return an object representation of the log
	 * @throws IOException if the stream cannot be read or there is an error reading
	 * @throws ParseException if the log file cannot be parsed
	 */
	private UsaProxyLog parseLog(InputStream logStream) throws IOException, ParseException {
		TLexer lexer = new TLexer();
		lexer.setCharStream( new ANTLRInputStream( logStream ) );
		TokenStream tokens = new CommonTokenStream( lexer );
		TParser parser = new TParser( tokens );
		try {
			log_return logParseResult = parser.log();
			UsaProxyLog parsedLog = logParseResult.log;
			parseHTTPTrafficLogs();
			return parsedLog;
		} catch (RecognitionException e) {
			throw new java.text.ParseException(
					"Unable to parse log at line " + e.line + ", character " + e.charPositionInLine, 
					e.index );
		}
	}

	/**
	 * Parses HTTP traffic logs for HTTP request and response headers.
	 * Seeks the session store for HTTP traffic sessions and amends them accordingly.
	 * @throws IOException 
	 */
	private void parseHTTPTrafficLogs() throws IOException
	{
		UsaProxyHTTPTrafficLogHandler handler = new UsaProxyHTTPTrafficLogHandler(logFile);
		UsaProxyHTTPTrafficLogParser httpParser = new UsaProxyHTTPTrafficLogParser(handler);
		for ( UsaProxyHTTPTraffic httpTrafficSession : UsaProxySessionStore.getHTTPTrafficSessions() )
		{
			httpTrafficSession.setRequestHeaders( httpParser.getRequestHeaders(httpTrafficSession) );
			httpTrafficSession.setResponseHeaders( httpParser.getResponseHeaders(httpTrafficSession) );
		}
	}
}

package fi.uta.infim.usaproxylogparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
		} catch (XPathExpressionException e) {
			throw new RuntimeException(
					"Unable to parse log. Invalid XPath expression: " + e.getMessage()  );
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(
					"Unable to parse log. Invalid configuration: " + e.getMessage()  );
		} catch (SAXException e) {
			throw new RuntimeException(
					"Unable to parse log. Problem parsing HTML content: " + e.getMessage()  );
		}
	}

	/**
	 * Parses HTTP traffic logs for HTTP request and response headers, as well
	 * as element contents.
	 * Seeks the session store for HTTP traffic sessions and amends them accordingly.
	 * @throws IOException when the HTTP traffic log cannot be opened
	 * @throws SAXException when parsing of HTML content fails
	 * @throws ParserConfigurationException when parser configuration is invalid
	 * @throws XPathExpressionException when an XPath generated from a UsaProxy DOM path is invalid
	 */
	private void parseHTTPTrafficLogs() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException
	{
		UsaProxyHTTPTrafficLogHandler handler = new UsaProxyHTTPTrafficLogHandler(logFile);
		UsaProxyHTTPTrafficLogParser httpParser = new UsaProxyHTTPTrafficLogParser(handler);
		for ( UsaProxyHTTPTraffic httpTrafficSession : UsaProxySessionStore.getHTTPTrafficSessions() )
		{
			httpTrafficSession.setRequestHeaders( httpParser.getRequestHeaders(httpTrafficSession) );
			httpTrafficSession.setResponseHeaders( httpParser.getResponseHeaders(httpTrafficSession) );
			
			Document httpTrafficDocument = null;
			for ( UsaProxyDOMElement element : httpTrafficSession.getDomElements() )
			{
				if ( element.getContents() == null && element.getPath() != null )
				{
					if ( httpTrafficDocument == null )
					{
						httpTrafficDocument = httpParser.parseLog(httpTrafficSession);
					}
					element.setContents( XPathFactory.newInstance().newXPath().compile(
							UsaProxyHTTPTrafficLogParser.usaProxyDOMPathToXPath( element.getPath() ) )
							.evaluate(httpTrafficDocument, XPathConstants.STRING).toString() );
				}
			}
		}
	}
}

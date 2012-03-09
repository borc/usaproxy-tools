package fi.uta.infim.usaproxyreportgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.xalan.xsltc.trax.SAX2DOM;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fi.uta.infim.usaproxylogparser.UsaProxyHTTPTraffic;
import fi.uta.infim.usaproxylogparser.UsaProxyHTTPTrafficLogHandler;

public class UsaProxyHTTPTrafficLogDocumentReader {

	private UsaProxyHTTPTrafficLogHandler handler;
	
	public UsaProxyHTTPTrafficLogDocumentReader(File logFile) {
		super();
		handler = new UsaProxyHTTPTrafficLogHandler(logFile); 
	}
	
	/**
	 * Seeks the http traffic log file until the actual document is found.
	 * @param traffic the http traffic object
	 * @return a reader object with the file contents seeked to the beginning of the actual document.
	 * @throws IOException
	 */
	private Reader getSeekedLogReader( UsaProxyHTTPTraffic traffic ) throws IOException
	{
		FileInputStream fis = new FileInputStream( handler.findHTTPTrafficLog(traffic) );
		BufferedReader fileReader = new BufferedReader( new InputStreamReader( fis ) );
		int emptyLinesFound = 0;
		while ( fileReader.ready() )
		{
			// Actual document start after the 2nd empty line
			if ( fileReader.readLine().trim().length() == 0 ) emptyLinesFound++;
			if ( emptyLinesFound == 2 ) break;
		}
		return fileReader;
	}
	
	public Node parseLog( UsaProxyHTTPTraffic traffic ) throws ParserConfigurationException, IOException, SAXException
	{
		Parser p = new Parser();
		p.setFeature("http://xml.org/sax/features/namespace-prefixes",true);
		// to define the html: prefix (off by default)
		SAX2DOM sax2dom = new SAX2DOM();
		p.setContentHandler(sax2dom);
		p.parse(new InputSource( getSeekedLogReader(traffic)));
		Node doc = sax2dom.getDOM();
		return doc;
	}
	
	public static String usaProxyDOMPathToXPath( String usaProxyPath ) throws IOException, XPathExpressionException
	{
		String path = "";
		StringReader reader = new StringReader(usaProxyPath);
		while ( reader.ready() )
		{
			int depth = 0; // Index of the child element
			int currentChar;
			String prefix = "0";
			while ( Character.isDigit(currentChar = reader.read()) )
			{
				prefix += String.valueOf( (char) currentChar );
			}
			if ( currentChar == -1 ) break;
			depth += Integer.parseInt( prefix ) * 26;
			depth += currentChar - ((int) 'a') + 1; // assuming ascii charset
			path += "/*[" + String.valueOf(depth) + "]";
		}
			
		return path;
	}
}

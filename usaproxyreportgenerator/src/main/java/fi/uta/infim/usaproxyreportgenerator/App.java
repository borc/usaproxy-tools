package fi.uta.infim.usaproxyreportgenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import fi.uta.infim.usaproxylogparser.UsaProxyAppearanceEvent;
import fi.uta.infim.usaproxylogparser.UsaProxyDOMElement;
import fi.uta.infim.usaproxylogparser.UsaProxyDisappearanceEvent;
import fi.uta.infim.usaproxylogparser.UsaProxyHTTPTraffic;
import fi.uta.infim.usaproxylogparser.UsaProxyLog;
import fi.uta.infim.usaproxylogparser.UsaProxyLogParser;
import fi.uta.infim.usaproxylogparser.UsaProxySession;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class App 
{
	private static File logFile;
	
	private static UsaProxyHTTPTrafficLogHandler logFileHandler;
	
	private static JsonConfig config;
	
	private static final String TEMPLATEDIR = "templates";
	
	private static final String REPORTTEMPLATE = "session-report.ftl";
	
	public static UsaProxyHTTPTrafficLogHandler getLogFileHandler()
	{
		return logFileHandler;
	}
	
	public static JsonConfig getConfig()
	{
		return config;
	}
	
    public static void main( String[] args ) throws DOMException, ParserConfigurationException, IOException, SAXException, TemplateException, XPathExpressionException
    {
    	UsaProxyLogParser parser = new UsaProxyLogParser();
    	UsaProxyLog log;
    	File logfile;
    	try
    	{
    		logfile = new File( args[ 0 ] );
    		System.out.print( "Parsing log file... " );
    		log = parser.parseLog( logfile );
    		System.out.println( "done." );
    		logFile = logfile;
    		logFileHandler = new UsaProxyHTTPTrafficLogHandler(logFile);
    		
    	}
    	catch( IOException ioe )
    	{
    		System.err.println( "Error opening log file.");
    		return;
    	}
    	catch( ParseException pe )
    	{
    		System.err.println( "Error parsing log file." );
    		return;
    	}
    	catch( IndexOutOfBoundsException e )
    	{
    		System.err.println( "Please supply a file name." );
    		return;
    	}
    	
    	config = new JsonConfig();
    	config.registerJsonBeanProcessor( UsaProxyHTTPTraffic.class, 
    			new UsaProxyHTTPTrafficJSONProcessor() );
    	config.registerJsonBeanProcessor( UsaProxyDOMElement.class, 
    			new UsaProxyDOMElementJSONProcessor() );
    	config.registerJsonBeanProcessor( UsaProxyAppearanceEvent.class, 
    			new UsaProxyPageEventJSONProcessor<UsaProxyAppearanceEvent>() );
    	config.registerJsonBeanProcessor( UsaProxyDisappearanceEvent.class, 
    			new UsaProxyPageEventJSONProcessor<UsaProxyDisappearanceEvent>() );
    	
    	for ( UsaProxySession s : log.getSessions() )
    	{
    		File outputDir;
    		try
    		{
    			outputDir = new File( args[ 1 ] );
    		}
    		catch( IndexOutOfBoundsException e )
    		{
    			// Use CWD if output dir not specified
    			outputDir = new File( "." );
    		}
    		System.out.print( "Generating report for session " + s.getSessionID() + "... " );
    		generateHTMLReport(s, outputDir);
    		System.out.println( "done." );
    	}
    }
    
    private static void generateHTMLReport( UsaProxySession session, File outputDir ) throws DOMException, ParserConfigurationException, IOException, SAXException, TemplateException, XPathExpressionException
    {
    	JSONArray httptraffic = new JSONArray();
    	for ( UsaProxyHTTPTraffic t : session.getSortedHttpTrafficSessions() )
		{
    		System.out.print( "processing traffic id " + t.getSessionID() + "... " );
			httptraffic.add( JSONObject.fromObject(t, config) );
		}
    	
    	JSONObject tf = new JSONObject();
    	tf.accumulate("httptraffics", httptraffic);
    	
    	Map< String, Object > sessionRoot = new HashMap<String, Object>();
    	
    	sessionRoot.put("data", tf.toString());
    	sessionRoot.put( "timestamp", session.getStart() );
    	sessionRoot.put( "id", session.getSessionID() );
    	sessionRoot.put( "ip", session.getAddress().getHostAddress() );
    	
    	
    	
    	Configuration cfg = new Configuration();
    	// Specify the data source where the template files come from.
    	cfg.setClassForTemplateLoading( App.class, "/" + TEMPLATEDIR );
    	cfg.setObjectWrapper(new DefaultObjectWrapper());
    	
    	Template temp = cfg.getTemplate(REPORTTEMPLATE);

    	Map<String, Object> root = new HashMap<String, Object>();
    	root.put( "session", sessionRoot );
    	
    	try
    	{
    		Writer out = new OutputStreamWriter( new FileOutputStream( 
    			new File( outputDir, "usaproxy-session-report-" + session.getSessionID() + ".html") ) );
    		temp.process(root, out);
        	out.flush();
    	}
    	catch( IOException e )
    	{
    		System.err.println( "Error opening output file: " + e.getLocalizedMessage() );
    	}
    	
    }

}

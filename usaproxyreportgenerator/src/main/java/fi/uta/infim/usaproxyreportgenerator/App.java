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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
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

public final class App 
{
	private static File logFile;
	
	private static UsaProxyHTTPTrafficLogDocumentReader logFileHandler;
	
	private static JsonConfig config;
	
	private static final String TEMPLATEDIR = "templates";
	
	//private static final String REPORTTEMPLATE = "session-report.ftl";
	private static final String REPORTTEMPLATE = "session-report-new.ftl";
	
	private static final String HTTPTRAFFICLISTIDPREFIX = "http-traffic-id-";
	
	private static final String PLACEHOLDERIDPREFIX = "placeholder-id-";
	
	/**
	 * Command line parser
	 */
	private static final CommandLineParser parser = new GnuParser();
	
	/**
	 * Command line options
	 */
	private static final Options cliOptions = createCLIOptions();
	
	private static CommandLine cli = null;
	
	@SuppressWarnings("static-access")
	private static Options createCLIOptions() {
		// Command line options
		Options cliOptions = new Options();
		cliOptions.addOption( OptionBuilder.withArgName( "directory" )
				.hasArg()
				.withDescription( "output directory for reports" )
				.create( "outputDir" ) );
		return cliOptions;
	}

	public static UsaProxyHTTPTrafficLogDocumentReader getLogFileHandler()
	{
		return logFileHandler;
	}
	
	public static JsonConfig getConfig()
	{
		return config;
	}
	
	private static void printHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "java -jar usaproxyreportgenerator.jar [OPTIONS] logFile", cliOptions );
	}
	
    public static void main( String[] args ) throws DOMException, ParserConfigurationException, IOException, SAXException, TemplateException, XPathExpressionException
    {
    	try
    	{
    		// Command line arguments
    		cli = parser.parse(cliOptions, args);
    	}
    	catch( org.apache.commons.cli.ParseException e )
    	{
    		System.err.println( e.getMessage() );
    		printHelp();
    		return;
    	}
    	
    	UsaProxyLogParser parser = new UsaProxyLogParser();
    	UsaProxyLog log;
    	File logfile;
    	try
    	{
    		logfile = new File( cli.getArgs()[ 0 ] );
    		System.out.print( "Parsing log file... " );
    		log = parser.parseLog( logfile );
    		System.out.println( "done." );
    		logFile = logfile;
    		logFileHandler = new UsaProxyHTTPTrafficLogDocumentReader(logFile);
    		
    	}
    	catch( IOException ioe )
    	{
    		System.err.println( "Error opening log file.");
    		printHelp();
    		return;
    	}
    	catch( ParseException pe )
    	{
    		System.err.println( "Error parsing log file." );
    		printHelp();
    		return;
    	}
    	catch( IndexOutOfBoundsException e )
    	{
    		System.err.println( "Please supply a file name." );
    		printHelp();
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
    		// Use CWD if output dir is not supplied
   			outputDir = new File( cli.hasOption( "outputDir" ) ? cli.getOptionValue( "outputDir" ) : "." );
    		System.out.print( "Generating report for session " + s.getSessionID() + "... " );
    		generateHTMLReport(s, outputDir);
    		System.out.println( "done." );
    	}
    }
    
    private static void generateHTMLReport( UsaProxySession session, File outputDir ) throws DOMException, ParserConfigurationException, IOException, SAXException, TemplateException, XPathExpressionException
    {
    	JSONObject httptraffic = new JSONObject();
    	for ( UsaProxyHTTPTraffic t : session.getSortedHttpTrafficSessions() )
		{
    		System.out.print( "processing traffic id " + t.getSessionID() + "... " );
			httptraffic.accumulate( t.getSessionID().toString(), JSONObject.fromObject(t, config) );
		}
    	
    	JSONObject tf = new JSONObject();
    	tf.accumulate("httptraffics", httptraffic);
    	
    	Map< String, Object > sessionRoot = new HashMap<String, Object>();
    	
    	sessionRoot.put("data", tf.toString());
    	sessionRoot.put( "timestamp", session.getStart() );
    	sessionRoot.put( "id", session.getSessionID() );
    	sessionRoot.put( "ip", session.getAddress().getHostAddress() );
    	sessionRoot.put( "httpTraffics", session.getHttpTrafficSessions() );
    	
    	Configuration cfg = new Configuration();
    	// Specify the data source where the template files come from.
    	cfg.setClassForTemplateLoading( App.class, "/" + TEMPLATEDIR );
    	cfg.setObjectWrapper(new DefaultObjectWrapper());
    	
    	Template temp = cfg.getTemplate(REPORTTEMPLATE);

    	Map<String, Object> root = new HashMap<String, Object>();
    	root.put( "session", sessionRoot );
    	root.put( "httpTrafficListIdPrefix", HTTPTRAFFICLISTIDPREFIX );
    	root.put( "placeholderIdPrefix", PLACEHOLDERIDPREFIX );
    	
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

/*
 * UsaProxyReportGenerator - tool for processing UsaProxy-fork logs
 *  Copyright (C) 2012 Teemu Pääkkönen - University of Tampere
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

package fi.uta.infim.usaproxyreportgenerator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;

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

/**
 * Main class for the report generator command line application
 * @author Teemu Pääkkönen
 *
 */
public final class App 
{
	/**
	 * URL of the application main JAR
	 */
	public static final URL APPLICATION_URL =
			App.class.getProtectionDomain().getCodeSource().getLocation();
	
	/**
	 * The root directory of the application. Note that this is the actual
	 * location of the report generator JAR file, not the PWD.
	 */
	@SuppressWarnings("deprecation")
	public static final File APPLICATION_DIR =  
			new File( URLDecoder.decode( APPLICATION_URL.getPath() ) ).getParentFile();
	
	/**
	 * The plugins directory. Data providers will be searched for in here.
	 */
	public static final File PLUGINS_DIR = new File( APPLICATION_DIR, "plugins" );
	
	/**
	 * Configuration for the JSON processors
	 */
	private static JsonConfig config;
	
	/**
	 * Template directory name. This directory contains the freemarker HTML
	 * templates. The directory will be included in the JAR file during Maven
	 * packaging process.
	 */
	private static final String TEMPLATEDIR = "templates";
	
	/**
	 * Name of the template file to be used for generating reports. This file
	 * must reside in the {@link #TEMPLATEDIR}.
	 */
	private static final String REPORTTEMPLATE = "session-report-new.ftl";
	
	/**
	 * This value is used by the report template to generate IDs for list items
	 * in the http traffic session list.
	 */
	private static final String HTTPTRAFFICLISTIDPREFIX = "http-traffic-id-";
	
	/**
	 * This value is used by the report template to generate IDs for plot
	 * placeholder DIV elements.
	 */
	private static final String PLACEHOLDERIDPREFIX = "placeholder-id-";
	
	/**
	 * Command line parser
	 */
	private static final CommandLineParser parser = new GnuParser();
	
	/**
	 * Command line options
	 */
	private static final Options cliOptions = createCLIOptions();
	
	/**
	 * A representation of the parsed command line
	 */
	private static CommandLine cli = null;
	
	private static final Class<? extends DataProvider> DEFAULT_DATA_PROVIDER_CLASS = DefaultDataProvider.class;
	
	private static Class<? extends DataProvider> dataProviderClass = DEFAULT_DATA_PROVIDER_CLASS;
	
	/**
	 * Creates the options object for the CLI parser. Command line parameters
	 * are set up here.
	 * @return options object for CLI parser
	 */
	@SuppressWarnings("static-access")
	private static Options createCLIOptions() {
		// Command line options
		Options cliOptions = new Options();
		cliOptions.addOption( OptionBuilder.withArgName( "directory" )
				.hasArg()
				.withDescription( "output directory for reports" )
				.create( "outputDir" ) );
		cliOptions.addOption( OptionBuilder.withArgName( "class" )
				.hasArg()
				.withDescription( "full name of the data provider class" )
				.create( "dataProvider" ) );
		return cliOptions;
	}

	/**
	 * Returns the JSON processor configuration
	 * @return the JSON processor configuration
	 */
	public static JsonConfig getConfig()
	{
		return config;
	}
	
	/**
	 * Prints the command line help text to std out.
	 */
	private static void printHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "java -jar usaproxyreportgenerator.jar [OPTIONS] <logFile1 [logFile2 ... logFileN]>", cliOptions );
	}
	
	/**
	 * Prints the GPL license information text to std out.
	 */
	private static void printLicense()
	{
		System.out.println( "UsaProxyReportGenerator version 0.0.2-SNAPSHOT, " +
				"Copyright (C) 2012 Teemu Pääkkönen - University of Tampere" );
		System.out.println( "UsaProxyReportGenerator comes with ABSOLUTELY NO WARRANTY; for details see gpl.txt." );
		System.out.println( "This is free software, and you are welcome to redistribute it " +
				"under certain conditions; see gpl.txt for details.");
	}
	
	private static void setupJsonConfig()
	{
		config = new JsonConfig();
    	config.registerJsonBeanProcessor( UsaProxyHTTPTraffic.class, 
    			new UsaProxyHTTPTrafficJSONProcessor() );
    	config.registerJsonBeanProcessor( UsaProxyDOMElement.class, 
    			new UsaProxyDOMElementJSONProcessor() );
    	config.registerJsonBeanProcessor( UsaProxyAppearanceEvent.class, 
    			new UsaProxyPageEventJSONProcessor<UsaProxyAppearanceEvent>() );
    	config.registerJsonBeanProcessor( UsaProxyDisappearanceEvent.class, 
    			new UsaProxyPageEventJSONProcessor<UsaProxyDisappearanceEvent>() );
	}
	
	private static void setupDataProvider()
	{
		// A data provider class can be specified on the CLI.
		if ( cli.hasOption( "dataProvider") )
		{
			// Look for JAR files in the plugin dir
			File[] jars = PLUGINS_DIR.listFiles( (FileFilter) new WildcardFileFilter( "*.jar", IOCase.INSENSITIVE ) );
			URL[] jarUrls = new URL[ jars.length ];
			for ( int i = 0; i < jars.length; ++i )
			{
				try {
					jarUrls[ i ] = jars[ i ].toURI().toURL();
				} catch (MalformedURLException e) {
					// Skip URL if not valid
					continue;
				}
			}

			ClassLoader loader = URLClassLoader.newInstance( jarUrls, ClassLoader.getSystemClassLoader() );
			String className = cli.getOptionValue( "dataProvider" );
			
			// Try to load the named class using a class loader. Fall back to
			// default if this fails.
			try {
				@SuppressWarnings("unchecked")
				Class<? extends DataProvider> cliOptionClass = (Class<? extends DataProvider>) Class.forName(className, true, loader);
				if ( !DataProvider.class.isAssignableFrom(cliOptionClass) )
				{
					throw new ClassCastException( cliOptionClass.getCanonicalName() );
				}
				dataProviderClass = cliOptionClass;
			} catch (ClassNotFoundException e) {
				System.out.flush();
				System.err.println( "Specified data provider class not found: " + e.getMessage() );
				System.err.println( "Falling back to default provider." );
			} catch (ClassCastException e) {
				System.out.flush();
				System.err.println( "Specified data provider class is invalid: " + e.getMessage() );
				System.err.println( "Falling back to default provider." );
			}
			System.err.flush();
		}
	}
	
	/**
	 * Entry point.
	 * @param args command line arguments
	 */
	public static void main( String[] args )
    {
    	printLicense();
    	System.out.println();
    	
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
    	
    	File outputDir;
		// Use CWD if output dir is not supplied
		outputDir = new File( cli.getOptionValue( "outputDir", "." ) );
    	
		// Set up the browsing data provider that mines the log entries for 
		// visualizable data.
		setupDataProvider();  
		
		// Output CLI options, so that the user sees what is happening
		System.out.println( "Output directory: " + outputDir.getAbsolutePath() );
		System.out.println( "Data provider class: " + dataProviderClass.getCanonicalName() );
    	
    	UsaProxyLogParser parser = new UsaProxyLogParser();
    	UsaProxyLog log;
    	try
    	{
    		String filenames[] = cli.getArgs();
    		if ( filenames.length == 0 )
    		{
    			throw new IndexOutOfBoundsException();
    		}
    		
    		// Interpret remaining cli args as file names
    		System.out.print( "Parsing log file... " );
    			log = parser.parseFilesByName( Arrays.asList( filenames ) );
    		System.out.println( "done." );
    	}
    	catch( IOException ioe )
    	{
    		System.err.println( "Error opening log file: " + ioe.getMessage() );
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
    	catch ( NoSuchElementException e )
    	{
    		System.err.println( "Error opening log file: " + e.getMessage() );
    		printHelp();
    		return;
    	}
    	
    	setupJsonConfig(); // Set up JSON processors
    	
		// Iterate over sessions and generate a report for each one.
    	for ( UsaProxySession s : log.getSessions() )
    	{
    		System.out.print( "Generating report for session " + s.getSessionID() + "... " );
    		try {
				generateHTMLReport(s, outputDir);
				System.out.println( "done." );
			} catch (IOException e) {
				System.err.println( "I/O error generating report for session id " + 
						s.getSessionID() + ": " + e.getMessage() );
				System.err.println( "Skipping." );
			} catch (TemplateException e) {
				System.err.println( "Error populating template for session id " + 
						s.getSessionID() + ": " + e.getMessage() );
				System.err.println( "Skipping." );
			}
    	}
    }
    
    /**
     * Generates the actual HTML report file for a single session.
     * @param session the session to generate the report for
     * @param outputDir output directory for reports
     * @throws IOException if the report template cannot be read
     * @throws TemplateException if there is a problem processing the template
     */
    private static void generateHTMLReport( UsaProxySession session, File outputDir ) throws IOException, TemplateException
    {
    	JSONObject httptraffic = new JSONObject();
    	for ( UsaProxyHTTPTraffic t : session.getSortedHttpTrafficSessions() )
		{
    		System.out.print( "processing traffic id " + t.getSessionID() + "... " );
			httptraffic.accumulate( t.getSessionID().toString(), JSONObject.fromObject(t, config) );
		}
    	
    	JSONObject tf = new JSONObject();
    	tf.accumulate( "DEFAULTELEMENTCOLOR", UsaProxyHTTPTrafficJSONProcessor.DEFAULTELEMENTCOLOR );
    	tf.accumulate( "DEFAULTVIEWPORTCOLOR", UsaProxyHTTPTrafficJSONProcessor.DEFAULTVIEWPORTCOLOR );
    	tf.accumulate("httptraffics", httptraffic);
    	
    	Map< String, Object > sessionRoot = new HashMap<String, Object>();
    	
    	sessionRoot.put("data", tf.toString());
    	sessionRoot.put( "timestamp", session.getStart() );
    	sessionRoot.put( "id", session.getSessionID() );
    	sessionRoot.put( "ip", session.getAddress().getHostAddress() );
    	sessionRoot.put( "httpTraffics", session.getSortedHttpTrafficSessions() );
    	
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

	public static Class<? extends DataProvider> getDataProviderClass() {
		return dataProviderClass;
	}

}

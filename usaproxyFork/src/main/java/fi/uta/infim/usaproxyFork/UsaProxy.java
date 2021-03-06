package fi.uta.infim.usaproxyFork;
import java.io.*;
import java.net.*; 
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** 
 *  UsaProxy - HTTP proxy for tracking, logging, and replay of user interactions on websites
 *  in order to enable web-based collaboration.
 *  <br><br>
    Copyright (C) 2006  Monika Wnuk - Media Informatics Group at the University of Munich
    Copyright (C) 2012 Teemu Pääkkönen - University of Tampere
    <br><br>
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
 *  <br><br>
 *  UsaProxy main class manages deployment and operation mode settings,
 *  together with global objects such as <code>EventManager</code>, <code>ChatManager</code>, or <code>FileSender</code>.<br><br>
 *  UsaProxy is started together with a couple of information on the proxy mode (e.g.
 *  regular proxy, transparent proxy, application in front of a web server), on the sharing mode (i.e.
 *  remote monitoring or shared browsing), and on the logging mode. UsaProxy can be set up to either
 *  enable web-based collaboration, or for simple logging of interactions, or both.
 *  In addition, a UsaProxy instance ID can be assigned which identifies the launched instance.<br><br>
 *  Finally, the proxy is started and waits constantly for incoming HTTP requests.
 */
public class UsaProxy {
	
	/**
	 * The root directory of the application. Note that this is the actual
	 * location of the UsaProxy JAR file, not the PWD.
	 */
	@SuppressWarnings("deprecation")
	public static final File APPLICATION_DIR = new File( URLDecoder.decode( 
			UsaProxy.class.getProtectionDomain().getCodeSource().getLocation().getPath() ) ).getParentFile();
	
	/**
	 * The plugins directory.
	 */
	public static final File PLUGINS_DIR = new File( APPLICATION_DIR, "plugins" );
	
	/**
	 * The directory containing CSS files
	 */
	public static final File CSS_DIR = new File( APPLICATION_DIR, "css" );
	
	/**
	 * Serveable HTML files reside here
	 */
	public static final File HTML_DIR = new File( APPLICATION_DIR, "html" );
	
	/**
	 * Image files directory
	 */
	public static final File IMG_DIR = new File( APPLICATION_DIR, "img" );
	
	/**
	 * Javascript files directory
	 */
	public static final File JS_DIR = new File( APPLICATION_DIR, "js" );
	
	/**  UsaProxy port. */
    private int 				port;	
    /**  UsaProxy IP address. */
    private InetAddress			ip;		
    /**  The UsaProxy deployment mode: regular proxy, proxy which forwards requests to another proxy, or
	  *	 server module. */
    private Mode				mode;		
    /**  Is responsible for the delivery of UsaProxy-specific 
     *   files such as the tracking JavaScript. */
    private FileSender 			fileSender;		
    /**  Provides methods for the management and exchange of chat messages. */
    private ChatManager		 	chatManager;	
    /**  Provides methods for the management of exchanged interactions/events 
     *   and for synchronized event logging to a file. */
    private EventManager		eventManager;
    /**  Manages the httptrafficindex which references the corresponding 
     *   txt-file containing the recorded HTTP request/response combination. */
    private HTTPTraffic 		httpTraffic;
    /**  manages users and the collaboration sessions which they participate in. */
    private Users				users;			
    
    /** True if UsaProxy is deployed in asymmetric remote monitoring mode. */
    private boolean				isRM; 			
    /** True if UsaProxy is deployed in symmetric shared browsing mode */
    private boolean				isSB; 	
    /** True if storage of user actions to a log file is enabled */
    private boolean				isLogging; 		
    /** Defines if all events are logged ("all") or only the web page requests ("pagereq") */
    private String				logMode;		
    
    /** The UsaProxy instance ID */
    private String				id;	
    
    /** The node types whose appearances and disappearances are to be logged */
    private String[]			nodeTypes;

    /**
     * Log the contents of all elements? Can be slow. Defaults to false.
     */
    private boolean				isLoggingContents;
    
    /**
     * Limit the logging of contents to this amount of characters. Set to -1
     * for no limit. Default: -1.
     */
    private int					contentsLoggingLimit;
    
    /**
     * Only in server mode:
     * Log the external URL (UsaProxy URL) instead of the actual URL?
     * This is useful when the actual URL is only accessible to UsaProxy, so
     * logging it would be pointless.
     */
    private boolean 			logExternalUrl;
    
    /**
     * How to split logs. See LogSplitType documentation for more information.
     */
    private LogSplitType		logSplitType;
    
    /**
     * When to split logs. For example, if split type is set to DAILY, setting
     * {@link #logSplitAt} to 16 will make UsaProxy split logs at 4 P.M.
     */
    private Integer				logSplitAt;
    
    /**
     * Increase or decrease the time interval of log splitting. For example,
     * if split type is set to DAILY, setting {@link #logSplitInterval} to 2
     * will make UsaProxy split logs every 2 days.
     */
    private Integer				logSplitInterval;
    
    /**
     * Attempt to detect dynamic changes to the document?
     * This feature can be very CPU intensive. Use with extreme care.
     */
    private DynamicDetectionType dynamicDetection = DynamicDetectionType.NONE;
    
    /**
     * Attempt to log frame contents? This only makes sense for generated
     * contents frames and non-UsaProxy frames. If a frame has a URL that
     * goes through UsaProxy, it will be logged whether this parameter is set
     * to non-zero or not.
     */
    private int 				maxFrameDepth = 0;
    
    /**
     * Which plugins to load? Will attempt to load scripts from the plugins
     * directory. File names are of format 'usaproxy.<name>.plugin.js' where
     * <name> is the plugin name provided in the CLI argument.
     * Mapping from plugin's name (directory) to it's descriptor.
     */
    private Map< String, PluginDescriptor >			plugins;
    
    /** If true all <code>System.out.println</code> messages are printed. */
    final static boolean 		DEBUG = false;	
    
    /** Constructor: creates a UsaProxy instance on a specific port,
     *  in the specified proxy mode (e.g. regular proxy, transparent proxy, proxy in front of a web server),
     *  in the specified sharing mode and logging mode, and with the defined instance ID.
     *  
     *  @param port is the port the UsaProxy will run on
     *  @param mode represents the deployment mode of UsaProxy
     *  @param rm is true, if asymmetric remote monitoring mode must be enabled
     *  @param sb is true, if symmetric shared browsing mode must be enabled
     *  @param isLogging defines if logging of events is enabled
     *  @param logMode specifies the type of logging (e.g. "all": full logging, "pagereq": only page requests)
     *  @param id is the name of this UsaProxy instance
     *  @param nodeTypes the node types whose appearances and disappearances are to be logged
     *  @param logExternalUrl log UsaProxy URL instead of the actual URL (only in server mode)
     */
	public UsaProxy(
			int port, 
			Mode mode, 
			boolean rm, 
			boolean sb, 
			boolean isLogging, 
			String logMode, 
			String id,
			String[] nodeTypes,
			boolean isLoggingContents,
			int contentsLimit,
			boolean logExternalUrl,
			LogSplitType splitType,
			Integer splitAt,
			Integer splitInterval,
			DynamicDetectionType ddType,
			int maxFrameDepth,
			Map<String, PluginDescriptor> plugins
			) {
		
		this.port 					= port;
		this.mode 					= mode;
		this.isRM 					= rm;
		this.isSB 					= sb;
		this.isLogging				= isLogging;
		this.logMode				= logMode;
		this.id 					= id;
		this.nodeTypes				= nodeTypes;
		this.setLoggingContents(isLoggingContents);
		this.setContentsLoggingLimit(contentsLimit);
		this.setLogExternalUrl(logExternalUrl);
		this.logSplitType			= splitType;
		this.logSplitAt				= splitAt;
		this.logSplitInterval		= splitInterval;
		this.setDynamicDetection(ddType);
		this.setMaxFrameDepth(maxFrameDepth);
		this.setPlugins(plugins);
		
		try {
			this.ip			= java.net.InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			System.err.println("\nAn ERROR occured while retrieving UsaProxy IP address:\n"
					+ e );
            System.exit(1);
		}
		
		/** start UsaProxy-Server */
		proxyStart();
		
	}
    
	/** Starts the UsaProxy application. The global manager objects are defined and a
	 *  <code>ServerSocket</code> object is generated according to 
	 *  the UsaProxy IP and port information. In an endless loop UsaProxy accepts incoming connections
	 *  and assigns each an individual processing thread (i.e. <code>ClientRequest</code>) */
    private void proxyStart() {

		/** Display message */
    	
		fileSender	 	= new FileSender();
		chatManager		= new ChatManager();
		eventManager	= new EventManager(this);
		httpTraffic		= new HTTPTraffic();
		users			= new Users();
	
		try {
	
		    /** Create server socket */
		    ServerSocket server = new ServerSocket(port); 
		    
		    /** Display start message */
			System.out.println("UsaProxy started at port " + port + " with ID: " + (id=="" ? "undefined":id));
			if (isRM) System.out.println("Joint experience via: Remote Monitoring");
			if (isSB) System.out.println("Joint experience via: Shared Browsing");
			if(!isRM && !isSB) System.out.println("Simple logging mode without joint experience");
		    System.out.println("Logging: " + (isLogging ? 
		    		(logMode.equals("pagereq") ? "on (only page requests)":"on")
		    		:"off"));
			printConfigurationOption( "Node types to be tracked", Arrays.toString( nodeTypes ) );
			printConfigurationOption( "Log element contents?", isLoggingContents );
			printConfigurationOption( "Limit contents logging to", contentsLoggingLimit );
			printConfigurationOption( "Log external urls?", logExternalUrl );
			printConfigurationOption( "Log splitting type", logSplitType );
			printConfigurationOption( "Split logs at", logSplitAt );
			printConfigurationOption( "Split interval", logSplitInterval );
			printConfigurationOption( "Dynamic detection type", dynamicDetection );
			printConfigurationOption( "Maximum frame penetration depth", maxFrameDepth );
			printConfigurationOption( "Plugins to load", plugins.values() );
			System.out.println();
			System.out.println("UsaProxy ready for accepting incoming connections !\n");
	
		    /** endless loop */
		    while (true) { 
	
				/** wait for next incoming request and accept it */
				Socket clientConnect = server.accept(); 
		
				/** Display message: new client with his address  */
				if (DEBUG) System.out.println("\nNew client connected:\n" + clientConnect.getInetAddress().getHostAddress());
		
				/** new ClientRequest which will handle this request */
				new ClientRequest (clientConnect, this);
		    } 

		/** Handle IO exception */
		} catch (IOException e) {
        	System.err.println("\nAn ERROR occured while starting UsaProxy:\n"
							+ e );
			System.exit(1);
		}
    }

    /** Creates a new UsaProxy instance with a number of
     *  deployment/sharing/logging modes specified by command line switches.
     *  Depending on command line arguments the respective proxy type (e.g. server-side setup)
     *  and operation mode (e.g. pure logging, shared browsing) are instantiated */
	public static void main(String[] args) {
			
		/** Command line switches:
		 *  -port <port> 				is the UsaProxy port the client is contacting (optional; default: 8000)
		 *  -remoteIP <IP address> 		is the address of the gateway proxy
		 *  							resp. web server UsaProxy always forwards requests to
		 *  -remotePort <port>			is the gateway's resp. web server's port (in combination
		 *  							with switch -remoteIP!)
		 *  -server						starts UsaProxy in server mode (in combination
		 *  							with switches -remoteIP and -remotePort!)
		 *  -rm							starts UsaProxy in Remote Monitoring mode (exclusive)
		 *  -sb							starts UsaProxy in Shared Browsing mode (exclusive)
		 *  -log						enables logging of events into log.txt	(additional or stand-alone)
		 *  -logMode pagereq|all		specifies logging mode, e.g. all events ("all"),
		 *  							only httptraffic log entries/ page requests ("pagereq")
		 *  							(optional; default: all)
		 *  -id <id>					identifies the UsaProxy instance (useful for distinguishing
		 *  							between versions and authentication of users); default: undefined
		 *  -nodeTypes <types>			lists the node types whose appearances and
		 *  							disappearances are to be logged, separated by a 
		 *  							semi-colon (;) default: img;h1;h2;h3;h4;h5;h6 		
		 *  -logContents				Log also element contents on appear events?
		 *  -logContentsLimit <n>		Limit contents logging to n characters.
		 *  -logSplit h|d|w|m			Split logs hourly/daily/weekly/monthly. Default: No splitting.
		 *  -logSplitAt <hh>			Start a new log file every h/d/w/m
		 *  							 * at hh minutes on the hour (hourly)
		 *  							 * at hh o'clock (daily) (24h)
		 *  							 * on weekday number hh (1=monday) (weekly)
		 *  							 * on the hh'th day of the month (monthly)
		 *  -logSplitInterval <nnn>		Split logs every nnn h/d/w/m instead of every h/d/w/m. Default: 1.
		 *  -dynamicDetection a|t|m|n   EXPERIMENTAL Detection of dynamic content. Set to:
		 *                               * [a]utomatic
		 *                               * [t]imer-based
		 *                               * [m]utation event based
		 *                               * [n]one (DEFAULT)
		 *  -framePenetrationDepth <n>  Penetrate into frames and iframes. N is the maximum depth.
		 *                              Default is 0. Note that frames whose contents are loaded
		 *                              through UsaProxy will be logged separately in any case.
		 *                              Only local content and content originating from the same
		 *                              location as the parent page can be logged because of the Same
		 *                              Origin constraint.
		 *  -plugins <names>			list of plugins to load, separated by a semi-colon (;)
		 *                              
		 */
		
		/** UsaProxy modes:
		 *  Proxy					no switches mandatory (e.g. java UsaProxy [-port <port>])
		 *  Remote					(all requests are forwarded to another gateway proxy) 
		 *  						switches -remoteIP and -remotePort mandatory
		 *  						(e.g. java UsaProxy [-port <port>] -remoteIP <IP address> -remotePort <port>)
		 *  Transparent				see "Proxy"
		 *  Transparent Remote		see "Remote"
		 *  Server					(UsaProxy as part of a web server resp. in front of a web server)
		 *  						switches -remoteIP, -remotePort and -server mandatory
		 *  						(e.g. java UsaProxy [-port <port>] -remoteIP <IP address> -remotePort <port> -server
		 */
		
		System.out.println( "UsaProxyFork version 0.0.2-SNAPSHOT, " +
				"Copyright (C) 2006  Monika Wnuk - Media Informatics Group at the University of Munich, " +
				"Copyright (C) 2012 Teemu Pääkkönen - University of Tampere" );
		System.out.println( "UsaProxyFork comes with ABSOLUTELY NO WARRANTY; for details see gpl.txt." );
		System.out.println( "This is free software, and you are welcome to redistribute it " +
				"under certain conditions; see gpl.txt for details.");
		System.out.println();
		
		int index;
		
		/** switch -port */
		/** default UsaProxy port */
		int port	= 8000;
		/** try to detect port declaration and port */
		if ((index = indexOf(args, "-port"))!=-1) {
			try {
				port = Integer.parseInt(args[index+1]);
			}
			catch (NumberFormatException e) {
            	System.err.println("\nAn ERROR occured while binding UsaProxy to port "
            					+ args[index+1] + ":\n"
								+ "Correct usage of switch -port: -port <port>\n"
								+ "Port will be set to 8000!");
            }
			catch (IndexOutOfBoundsException e) {
				System.err.println("\nAn ERROR occured while binding UsaProxy to a port:\n"
						+ "No port number specified!\n"
						+ "Correct usage of switch -port: -port <port>\n"
						+ "Port will be set to 8000!");
			}
		}
		
		/** switches -remoteIP and -remotePort */
		String remoteIP = "";
		int remotePort = -1;
		/** try to detect remoteIP declaration and remotePort */
		if ((index = indexOf(args, "-remoteIP"))!=-1) {
			try {
				remoteIP = args[index+1];
			}
			catch (IndexOutOfBoundsException e) {
				System.err.println("\nAn ERROR occured while defining remoteIP:\n"
						+ "No remoteIP specified!\n"
						+ "Correct usage of switch -port: -remoteIP <IP address>\n"
						+ "UsaProxy will be started as regular proxy on port " + port + "!");
			}
		}
		
		/** try to detect remote port declaration and port */
		if ((index = indexOf(args, "-remotePort"))!=-1) {
			if(!remoteIP.equals("")) {
				try {
					remotePort = Integer.parseInt(args[index+1]);
					
				}
				catch (NumberFormatException e) {
	            	System.err.println("\nAn ERROR occured while defining remotePort "
	            					+ args[index+1] + ":\n"
									+ "Correct usage of switch -remotePort: -remotePort <port>\n"
									+ "UsaProxy will be started as regular proxy on port " + port + "!");
	            }
				catch (IndexOutOfBoundsException e) {
					System.err.println("\nAn ERROR occured while defining a remotePort:\n"
							+ "No port number specified!\n"
							+ "Correct usage of switch -remotePort: -remotePort <port>\n"
							+ "UsaProxy will be started as regular proxy on port " + port + "!");
				}
			/** remotePort specified without specified remoteIP */
			} else {
				System.err.println("\nAn ERROR occured while defining remotePort:\n"
						+ "No remoteIP specified!\n"
						+ "To correctly use switch -remotePort also specify switch -remoteIP!\n"
						+ "UsaProxy will be started as regular proxy on port " + port + "!");
			}
		}
		
		/** if remoteIP specified without specified remotePort */
		if (!remoteIP.equals("") && remotePort==-1) {
			/** reset remoteIP */
			remoteIP = "";
			System.err.println("\nAn ERROR occured while defining remoteIP:\n"
					+ "No remotePort specified!\n"
					+ "To correctly use switch -remoteIP also specify switch -remotePort!\n"
					+ "UsaProxy will be started as regular proxy on port " + port + "!");
		}
		
		/** generate remote InetAddress if remoteIP and remotePort specified */
		InetAddress remoteAddress = null;
		if (!remoteIP.equals("") && remotePort!=-1) {
			try {	
            	remoteAddress = InetAddress.getByName(remoteIP);
            }
            catch(UnknownHostException e) {
            	System.err.println("\nAn ERROR occured while generating remote InetAddress:\n"
								+ "Please specify a valid IP address !\n"
								+ "UsaProxy will be started as regular proxy on port " + port + "!");

            }
		}
		
		/** switch -server */
		boolean server = false;
		boolean exUrls = false;
		/** try to detect server declaration */
		if ((index = indexOf(args, "-server"))!=-1) {
			if(!remoteIP.equals("") && remotePort!=-1) {
				server = true;
				
				// Check whether we want to log external URLs
				exUrls = indexOf( args, "-logExternalUrl" ) != -1;
				
			/** server mode specified without specified remoteIP resp. remotePort */
			} else {
				/** reset remoteIP and remotePort */
				remoteIP = "";
				remotePort = -1;
				System.err.println("\nAn ERROR occured while specifying server mode:\n"
						+ "No remoteIP resp. remotePort specified!\n"
						+ "To correctly use switch -server also specify switches -remoteIP and -remotePort!\n"
						+ "UsaProxy will be started as regular proxy on port " + port + "!");
			}
		}
		
		/** switch -rm */
		boolean rm = false;
		/** try to detect rm declaration */
		if ((index = indexOf(args, "-rm"))!=-1) {
			rm = true;
		}
		
		/** switch -sb */
		boolean sb = false;
		/** try to detect sb declaration */
		if ((index = indexOf(args, "-sb"))!=-1) {
			if(!rm) {
				sb = true;
			}
			/** also rm specified; combination not possible */
			else {
				System.err.println("\nAn ERROR occured while specifying shared browsing mode:\n"
						+ "Also Remote Monitoring mode specified!\n"
						+ "-rm and -sb must be used exclusively!\n"
						+ "UsaProxy will be started in Remote Monitoring mode!");
			}
		}
		
		/** switch -log */
		boolean log = false;
		/** try to detect log declaration */
		if ((index = indexOf(args, "-log"))!=-1) {
			log = true;
		}
		
		/** check further log parameters */
		String logMode = "";
		/** try to detect logMode specification */
		if (log) {
			/** if additional parameter exists */
			if(args.length>(index+1) && !args[index+1].startsWith("-"))
				logMode = args[index+1];
		}
		
		/** set logMode = "all" if logging is enabled
		 *  but no logMode parameter was specified */
		if (log && logMode.equals("")) logMode = "all";

		/** if neither a sharing mode nor a log mode were specified
		 *  full logging is enabled */
		if (!log && !rm && !sb) {
			log = true;
			logMode = "all";
		}
		
		/** switch -id */
		String id = "";
		/** try to detect id declaration */
		if ((index = indexOf(args, "-id"))!=-1) {
			try {
				id = args[index+1];
			}
			catch (IndexOutOfBoundsException e) {
				System.err.println("\nAn ERROR occured while defining UsaProxy ID:\n"
						+ "No ID specified!\n"
						+ "Correct usage of switch -id: -id <id>\n"
						+ "UsaProxy will be started with ID: undefined !");
			}
		}
		
		/**
		 * node types
		 */
		String nodeTypes[] = {"img", "h1", "h2", "h3", "h4", "h5", "h6"};
		if ( (index = indexOf( args, "-nodeTypes" )) != -1 )
		{
			if ( args.length > (index+1) && !args[index+1].startsWith("-") )
			{
				String nodetypeArg = args[ index + 1 ];
				nodeTypes = nodetypeArg.split( ";" );
			}
			else
			{
				System.err.println( "An error occurred while parsing for node types." );
				System.err.println( "UsaProxy will log the default node types." );
			}
		}

		/**
		 * Log element contents?
		 */
		boolean logContents = indexOf( args, "-logContents" ) != -1;
		
		int limitContents = -1;
		if ( (index = indexOf( args, "-logContentsLimit" )) != -1 )
		{
			try
			{
				limitContents = Integer.parseInt( args[index+1] );
				if ( limitContents < -1 )
				{
					limitContents = -1;
					System.err.println( "Invalid limit value!" );
					System.err.println( "Default limit of " + limitContents + " will be used." );
				}
			}
			catch( IndexOutOfBoundsException e )
			{
				System.err.println( "No limit value supplied!" );
				System.err.println( "Default limit of " + limitContents + " will be used." );
			}
			catch( NumberFormatException e )
			{
				System.err.println( "Invalid limit value!" );
				System.err.println( "Default limit of " + limitContents + " will be used." );
			}
		}
		
		/*
		 * Handle log splitting cli args.
		 * Other log splitting args will be ignored if -logSplit is not given.
		 */
		LogSplitType lsType = LogSplitType.NOSPLIT;
		Integer lsAt = 0;
		Integer lsInterval = 1;
		if ( (index = indexOf( args, "-logSplit" )) != -1 )
		{
			try 
			{
				lsType = LogSplitType.getTypeByCLIArg( args[ index + 1 ] );
			} 
			catch (NoSuchFieldException e) {
				System.err.println( "Invalid logSplit argument value! Valid values are: h, d, w, m." );
				System.err.println( "Defaulting to no log splitting." );
			}
			catch( IndexOutOfBoundsException e )
			{
				System.err.println( "No logSplit argument value supplied!" );
				System.err.println( "Defaulting to no log splitting." );
			}
			
			// Set logSplitAt to the minimum (default) value as determined
			// by the Enum LogSplitType.
			lsAt = lsType.minSplitValue();
			
			/*
			 * When to split the log. Handle logSplitAt argument.
			 */
			if ( (index = indexOf( args, "-logSplitAt" )) != -1 )
			{
				try
				{
					lsAt = Integer.parseInt( args[index+1] );
					if ( lsAt.compareTo( lsType.minSplitValue() ) < 0 || 
							lsAt.compareTo( lsType.maxSplitValue() ) > 0 )
					{
						lsAt = lsType.minSplitValue();
						System.err.println( "Invalid logSplitAt value!" );
						System.err.println( "Default value of " + lsAt + " will be used." );
					}
				}
				catch( IndexOutOfBoundsException e )
				{
					System.err.println( "No logSplitAt value supplied!" );
					System.err.println( "Default value of " + lsAt + " will be used." );
				}
				catch( NumberFormatException e )
				{
					System.err.println( "Invalid logSplitAt value!" );
					System.err.println( "Default value of " + lsAt + " will be used." );
				}
			}
			
			if ( (index = indexOf( args, "-logSplitInterval" )) != -1 )
			{
				try
				{
					lsInterval = Integer.parseInt( args[index+1] );
					if ( lsInterval < 1 )
					{
						lsInterval = 1;
						System.err.println( "Invalid logSplitInterval value!" );
						System.err.println( "Default value of " + lsInterval + " will be used." );
					}
				}
				catch( IndexOutOfBoundsException e )
				{
					System.err.println( "No logSplitInterval value supplied!" );
					System.err.println( "Default value of " + lsInterval + " will be used." );
				}
				catch( NumberFormatException e )
				{
					System.err.println( "Invalid logSplitInterval value!" );
					System.err.println( "Default value of " + lsInterval + " will be used." );
				}
			}
		}

		/**
		 * Check for dynamic detection type command line argument
		 */
		DynamicDetectionType ddType = DynamicDetectionType.NONE;
		if ( (index = indexOf( args, "-dynamicDetection" )) != -1 )
		{
			try 
			{
				ddType = DynamicDetectionType.getTypeByCLIArg( args[ index + 1 ] );
			} 
			catch (NoSuchFieldException e) {
				System.err.println( "Invalid dynamicDetection argument value! Valid values are: a, t, m, n." );
				System.err.println( "Defaulting to no dynamic detection." );
			}
			catch( IndexOutOfBoundsException e )
			{
				System.err.println( "No dynamicDetection argument value supplied!" );
				System.err.println( "Defaulting to no dynamic detection." );
			}
		}
		
		/**
		 * Frame penetration depth CLI arg
		 */
		int maxFrameDepth = 0;
		if ( (index = indexOf( args, "-framePenetrationDepth" )) != -1 )
		{
			try 
			{
				maxFrameDepth = Integer.parseInt( args[ index + 1 ] );
				if ( maxFrameDepth < 0 ) maxFrameDepth = 0;
			}
			catch ( IndexOutOfBoundsException e )
			{
				System.err.println( "No framePenetrationDepth argument value supplied!" );
				System.err.println( "Defaulting to 0." );
			}
			catch ( NumberFormatException e )
			{
				System.err.println( "Invalid framePenetrationDepth argument value supplied!" );
				System.err.println( "Defaulting to 0." );
			}
		}
		
		/**
		 * Plugins
		 */
		Map<String, PluginDescriptor> plugins = new HashMap<String, PluginDescriptor>();
		if ( (index = indexOf( args, "-plugins" )) != -1 )
		{
			if ( args.length > (index+1) && !args[index+1].startsWith("-") )
			{
				String pluginsArg = args[ index + 1 ];
				String pluginNames[] = pluginsArg.split( ";" );
				for ( int i = 0; i < pluginNames.length; ++i )
				{
					PluginDescriptor descriptor = 
							PluginDescriptorFactory.getDescriptorByName(pluginNames[ i ]);
					plugins.put( pluginNames[ i ], descriptor );
				}
			}
			else
			{
				System.err.println( "An error occurred while parsing for plugins." );
				System.err.println( "Plugins NOT loaded." );
			}
		}
		
		System.out.println("Trying to start UsaProxy at port: " + port);
		
		/** generate a mode instance */
		Mode mode;
		/** if remote (remote, transparent remote, or server) */
		if(!remoteIP.equals("")) {
			if(server) {
				mode = new ServerMode(remoteAddress, remotePort);
				System.out.println("Deployment: server-side mode");
			}
			else {
				mode = new RemoteMode(remoteAddress, remotePort);
				System.out.println("Deployment: forwarding to remote gateway");
			}
		/** no remote info (regular or transparent) */
		} else {
			mode = new ProxyMode();
			System.out.println("Deployment: regular/transparent proxy mode");
		}
		
		/** generate an UsaProxy instance */
		new UsaProxy(port, mode, rm, sb, log, logMode, id, nodeTypes, 
				logContents, limitContents, exUrls, lsType, lsAt, 
				lsInterval, ddType, maxFrameDepth, plugins );
			
	}

	public static <T extends Object> void printConfigurationOption( String optionName, T optionValue )
	{
		System.out.println( optionName + ": " + optionValue.toString() );
	}
	
	/**
	 * Returns the respective <code>Mode</code> implementation.
	 * 
	 * @return the proxy mode.
	 */
	public Mode getMode() {
		return mode;
	}
	
	/**
	 * Returns the <code>ChatManager</code> object which manages
	 * chat messages exchanged during shared sessions.
	 * 
	 * @return the <code>ChatManager</code> instance.
	 */
	public ChatManager getChatManager() {
		return chatManager;
	}
	
	/**
	 * Returns the <code>EventManager</code> object which manages the
	 * logging and delivering of user events.
	 * 
	 * @return the <code>EventManager</code> instance.
	 */
	public EventManager getEventManager() {
		return eventManager;
	}

	/**
	 * Returns the <code>FileSender</code> object which encloses the file delivery mechanism.
	 * 
	 * @return the <code>FileSender</code> instance.
	 */
	public FileSender getFileSender() {
		return fileSender;
	}

	/**
	 * Returns the <code>HTTPTraffic</code> object which manages the caching
	 * of server responses together with the corresponding request and response headers.
	 * 
	 * @return the HttpTraffic instance.
	 */
	public HTTPTraffic getHttpTraffic() {
		return httpTraffic;
	}
	
	/**
	 * Returns the UsaProxy port number.
	 * @return the port.
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Returns the UsaProxy IP address.
	 * @return the ip.
	 */
	public InetAddress getIP() {
		return ip;
	}
	
	/**
	 * Returns the <code>Users</code> object which manages the users participating
	 * (or not yet participating) in a shared session.
	 * 
	 * @return the <code>Users</code> instance.
	 */
	public Users getUsers() {
		return users;
	}

	/**
	 * Returns true if UsaProxy is deployed in remote monitoring operation mode.
	 * 
	 * @return true in case UsaProxy operates in remote monitoring mode.
	 */
	public boolean isRM() {
		return isRM;
	}
	
	/**
	 * Returns true if UsaProxy is deployed in shared browsing operation mode.
	 * 
	 * @return true in case UsaProxy operates in shared browsing mode.
	 */
	public boolean isSB() {
		return isSB;
	}
	
	/**
	 * Returns true if event logging is enabled.
	 * 
	 * @return true in case UsaProxy logs occured user events.
	 */
	public boolean isLogging() {
		return isLogging;
	}
	
	/**
	 * Returns the logging mode.
	 * 
	 * @return "all" if every user event is logged, if only page requests are logged, then "pagereq" is available
	 */
	public String getLogMode() {
		return logMode;
	}
	
	/**
	 * Returns the individual UsaProxy ID. For security reasons,
	 * this ID must accompany each special JavaScript (proxyscript.js)
	 * request. In addition, it is applied to the remote monitoring URL
	 * and to the setAdmin request.
	 * 
	 * @return the UsaProxy ID.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Returns the node types whose appearances and disappearances are to
	 * be logged. Default: {"img", "h1", "h2", "h3", "h4", "h5", "h6"}
	 * @return array of node types
	 */
	public String[] getNodeTypes()
	{
		return nodeTypes;
	}
	
	/** Returns the index of the specified string in the corresponding array.
	 * 
	 * @param array is the string array to examine
	 * @param value is the string to search for
	 * @return the integer position in the array if some antry maps to the
	 * value argument in this array, -1 otherwise
	 */
	public static int indexOf(String[] array, String value) {
		int index = -1;
		for (int i=0; i<array.length; i++) {
			if (array[i]!=null && array[i].equals(value)) {
				index = i;
				return index;
			}
		}
		return index;
	}

	public boolean isLoggingContents() {
		return isLoggingContents;
	}

	public void setLoggingContents(boolean isLoggingContents) {
		this.isLoggingContents = isLoggingContents;
	}

	public int getContentsLoggingLimit() {
		return contentsLoggingLimit;
	}

	public void setContentsLoggingLimit(int contentsLoggingLimit) {
		this.contentsLoggingLimit = contentsLoggingLimit;
	}

	public boolean isLogExternalUrl() {
		return logExternalUrl;
	}

	public void setLogExternalUrl(boolean logExternalUrl) {
		this.logExternalUrl = logExternalUrl;
	}

	public Integer getLogSplitAt()
	{
		return logSplitAt;
	}
	
	public Integer getLogSplitInterval()
	{
		return logSplitInterval;
	}
	
	public LogSplitType getLogSplit()
	{
		return logSplitType;
	}

	public DynamicDetectionType getDynamicDetection() {
		return dynamicDetection;
	}

	public void setDynamicDetection(DynamicDetectionType dynamicDetection) {
		this.dynamicDetection = dynamicDetection;
	}

	public int getMaxFrameDepth() {
		return maxFrameDepth;
	}

	public void setMaxFrameDepth(int maxFrameDepth) {
		this.maxFrameDepth = maxFrameDepth;
	}

	public Map< String, PluginDescriptor > getPlugins() {
		return plugins;
	}

	public void setPlugins(Map< String, PluginDescriptor > plugins) {
		this.plugins = plugins;
	}

}

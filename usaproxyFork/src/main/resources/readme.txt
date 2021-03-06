UsaProxyFork - A HTTP proxy/server with user interaction tracking
version 0.0.2-SNAPSHOT
Copyright (C) 2006  Monika Wnuk - Media Informatics Group at the University of Munich
Copyright (C) 2012 Teemu Pääkkönen - University of Tampere

#### description ####

UsaProxyFork is an enhanced version of UsaProxy 2.0. It allows tracking/logging
of user interaction within websites.

##### start UsaProxy proxy #####


To start the UsaProxy proxy do the following:

Being in the main folder open a terminal and type in:
java -jar usaproxyFork.jar

Command line switches:
-port <port> 			is the port UsaProxy is listening for incoming connections
						best between 1024 and 65535.

-remoteIP <IP address>	is the address of the gateway proxy
						resp. web server UsaProxy always forwards requests to

-remotePort <port>		is the gateway's resp. web server's port (in combination
						with switch -remoteIP!)

-server					starts UsaProxy in server mode (in combination
						with switches -remoteIP and -remotePort!)

-rm						starts UsaProxy in remote monitoring mode (exclusive)

-sb						starts UsaProxy in shared browsing mode (exclusive)

-log					enables logging of events to log.txt

-logMode				optional parameter; if assigned the value "pagereq" only page requests
						(incl. usaproxyload requests) are recorded, else all events are logged

-id <string>			optional UsaProxy instance ID: automatically used within the 
						JavaScript reference string which is inserted in the delivered web
						pages in order to provide more security (old/unknown requests will be rejected).
						
-nodeTypes				the node types whose appearances and
						disappearances are to be logged, separated by a 
						semi-colon (;) default: img;h1;h2;h3;h4;h5;h6
						 		
-logContents			Log also element contents on appear events?

-logContentsLimit <n>	Limit contents logging to n characters.				

-logExternalUrl			Only in server mode:
						Log the external URL (UsaProxy URL) instead of the actual URL?
						This is useful when the actual URL is only accessible to UsaProxy, so
						logging it would be pointless.
						
-logSplit h|d|w|m		Split logs hourly/daily/weekly/monthly. Default: No splitting.

-logSplitAt <hh>		Start a new log file every h/d/w/m
						 * at hh minutes on the hour (hourly)
						 * at hh o'clock (daily) (24h)
						 * on weekday number hh (1=monday) (weekly)
						 * on the hh'th day of the month (monthly)
						 
-logSplitInterval <nnn>	Split logs every nnn h/d/w/m instead of every h/d/w/m. Default: 1.						

-dynamicDetection a|t|m|n   EXPERIMENTAL Detection of dynamic content. Set to:
		                     * [a]utomatic
		                     * [t]imer-based
		                     * [m]utation event based
		                     * [n]one (DEFAULT)
		 
-framePenetrationDepth <n>  Penetrate into frames and iframes. N is the maximum depth.
                            Default is 0. Note that frames whose contents are loaded
                            through UsaProxy will be logged separately in any case.
                            Only local content and content originating from the same
                            location as the parent page can be logged because of the Same
                            Origin constraint.
                            
-plugins <names>			list of plugins to load, separated by a semi-colon (;)				

		
UsaProxy modes:
Proxy			no switches mandatory (e.g. java -jar UsaProxy.jar [-port <port>])
Remote			(all request must be forwarded to Gateway proxy) 
			switches -remoteIP and -remotePort mandatory
			(e.g. java -jar UsaProxy.jar [-port <port>] -remoteIP <IP address> -remotePort <port>)
Transparent		see "Proxy"
Transparent Remote	see "Remote"
Server			(UsaProxy as part of a web server resp. in front of a web server)
			switches -remoteIP, -remotePort and -server mandatory
			(e.g. java -jar UsaProxy.jar [-port <port>] -remoteIP <IP address> -remotePort <port> -server

Examples:

java -jar UsaProxy.jar -port 2727 	// regular proxy on port 2727
java -jar UsaProxy.jar -port 2727 -remoteIP 275.275.2.2 -remotePort 81  // proxy forwarding all requests to another gateway proxy
java -jar UsaProxy.jar -port 2727 -remoteIP 275.275.2.2 -remotePort 2666 -server  // proxy being set up on the web server side (in front of specific web server)

Remote monitoring mode, e.g.:
java -jar UsaProxy.jar -port 2727 -rb

Shared browsing mode with logging, e.g.:
java -jar UsaProxy.jar -port 2727 -sb -log

Pure logging mode, e.g.:
java -jar UsaProxy.jar -log


Notes:

optional: server-side deployment:
if UsaProxy is set up as server-side proxy:
-remoteIP: web address of the corresponding webserver (such as www.google.de)
-remoteIP: port the corresponding webserver is running on

in server mode UsaProxy is typically set up on port 80 and forwarding to a specific web server port !=80
java -jar UsaProxy.jar -port 80 -remoteIP www.google.de -remotePort 2666 -server

Scenarios of server-side deployment:

Scenario 1:
Imagine you (e.g. google) would like to put UsaProxy in front of your web server. You'll have to reconfigure the Apache so that it will be running on e.g. port 8000. You won't have to modify the references in your HTML pages. They may still implicitely refer to default port 80. And in fact - all requests shall further on go to port 80, so that UsaProxy may forward them to port 8000. In that case, UsaProxy would be started as follows:

java -jar UsaProxy.jar -port 80 -remoteIP www.google.de -remotePort 8000 -server

Scenario 2: a dedicated UsaProxy server:
You want to keep your web server running unchanged as www.google.de (IP 1.1.1.1) but nevertheless put UsaProxy in front. For this purpose, you'll have to modify the DNS entry in such a way that www.google.de will refer to IP 1.1.1.2 from now on. On the new computer 1.1.1.2 only UsaProxy will be running. In that case, UsaProxy would be started as follows:

java -jar UsaProxy.jar -port 80 -remoteIP 1.1.1.1 -remotePort 80 -server


##### connect to UsaProxy proxy #####


To be able to coduct a shared browsing session 
or a usability test using the UsaProxy proxy in regular proxy mode do the following:

1. Go to the respective browser properties menu
2. Register UsaProxy as proxy with the respective IP address and port
3. Surf the Web as usual or connect with another user in order to collaborate on the visited web pages.

If UsaProxy is used as transparent/server proxy modifying browser propoerties isn't neccessary


##### initiate a shared session #####

Remote monitoring mode:

A remote monitoring session is started when a user (i.e. the potential monitored person) clicks on a
"Live-Support" button. This button must be available within a web page in the form:

<INPUT type="button" value="Live Support" name="proposebut" id="proposebut">

An example is available on http://www.fnuked.de/casestudy

The support assistant (i.e. the potential monitoring partner) sees the other user's session ID on the 
remote monitoring overview page (which is delivered by UsaProxy on-the-fly when e.g. http://www.google.de/remotemonitoring is typed
in) in a list of "proposals". Before this can happen, the assistant must click on "register" in order to receive such proposals.
Once he clicks on the "accept" button, the shared session is started: he is redirected to the other user's web page, a chat layer and the second mouse pointer are displayed.

Shared Browsing:

Both users can visit the shared browsing overview page (which is delivered by UsaProxy on-the-fly when e.g. http://www.google.de/sharedbrowsing is typed in) and select the other user from the list of potential partners. The other user is subsequently displayed a div popup, on whatever web page he is, by which he can accept the proposal.

A session is terminated by click on "x" in the upper right corner of the chat box.


##### view the log files #####


1. Log file

If logging is ebanled log.txt contains all UsaProxy traffic in the form:
<Client-IP> <datestamp> <httptrafficindex> <session ID> <event> [<attributes>]

<httptrafficindex>: see also 2. HTTPTraffic folder; for each requested page an individual ID is stored so that instead of a long URL only an ID is submitted.

Examples:
141.84.8.77 2005-10-25,11:5:58 sd=2 sid=HW3pKPnfh5gY event=load size=1280x867
141.84.8.77 2005-10-25,11:6:02 sd=2 sid=HW3pKPnfh5gY event=mousemove offset=672,7 dom=abda


2. HTTPTraffic folder

Each HTTP request is stored to an individually named txt-file (increasing index as suffix).
All HTML-type server responses (headers and data) are stored to the file which the respective request was written to.
In addition, for every HTML-type server response a log entry (in log.txt) with the respective individual
index is generated (httptraffic log entry).

The individual txt-files are stored under "httpTraffic/log" and are named as follows:
httpTraffic<index>.txt

File httpTraffic.txt (directly under "httpTraffic") contains the current index and should not be altered. You can reset the index to "0" manually by changing the number in httpTraffic.txt - but attention: UsaProxy will start to store traffic to file httpTraffic0.txt (data will be appended in case file already exists)
File isCachingEnabled.txt (also directly under "httpTraffic") contains the information whether
this kind of HTTPTraffic logging shall take place. If you don't like to have UsaProxy store the HTTP traffic that is produced, change the value to "false".


##### license #####

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    
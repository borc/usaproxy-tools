package fi.uta.infim.usaproxylogparser;

import java.io.*;
import java.text.ParseException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Test program for the UsaProxy log parser, XML output
 * @author Teemu Pääkkönen
 */
class Main {

	/**
	 * Main function (entry point). Takes a log file name and outputs the contents 
	 * in XML format.
	 * @param args command line arguments
	 */
    public static void main(String[] args)
    {
    	try {
    		if ( args.length < 1 )
    		{
    			System.err.println( "Please supply a log file name" );
    			return;
    		}
    		String filename = args[0];
    		UsaProxyLog log = new UsaProxyLogParser().parseLog( new File( filename ) );

    		JAXBContext jc = JAXBContext.newInstance( UsaProxyLog.class );
    		Marshaller m = jc.createMarshaller();
    		
    		if ( args.length == 1 )
    		{
    			m.marshal( log, System.out );
    		}
    		else
    		{
    			m.marshal( log, new File( args[1] ) );
    		}
		} catch (IOException e) {
			System.err.println( "Error opening log file: " + e.getMessage() );
		} catch (ParseException e) {
			System.err.println( "Error parsing log file: " + e.getMessage() );
		} catch (JAXBException e) {
			System.err.println( "Error marshalling log to XML format: " + e.getMessage() );
		}
    }

}

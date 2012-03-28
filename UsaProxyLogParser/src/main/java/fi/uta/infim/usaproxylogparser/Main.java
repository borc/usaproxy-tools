/*
 * UsaProxyLogParser - Java API for UsaProxy-fork logs
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

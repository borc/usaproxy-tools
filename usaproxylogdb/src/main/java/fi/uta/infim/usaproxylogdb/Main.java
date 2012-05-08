/*
 * UsaProxyLog2DB - tool for storing UsaProxy-fork logs into a database
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

package fi.uta.infim.usaproxylogdb;

import java.io.File;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import fi.uta.infim.usaproxylogparser.UsaProxyLog;
import fi.uta.infim.usaproxylogparser.UsaProxyLogParser;

public final class Main {

	private static void printError( Throwable e, String error, boolean emptyLine, boolean printError )
	{
		if ( emptyLine ) System.err.println();
		if ( printError ) System.err.println( "ERROR: " + error );
		System.err.println( e.getMessage() );
		if ( e.getCause() != null ) printError( e.getCause(), "", false, false );
	}
	
	/**
	 * Prints the GPL license information text to std out.
	 */
	private static void printLicense()
	{
		System.out.println( "UsaProxyLog2DB version 0.0.2-SNAPSHOT, " +
				"Copyright (C) 2012 Teemu Pääkkönen - University of Tampere" );
		System.out.println( "UsaProxyLog2DB comes with ABSOLUTELY NO WARRANTY; for details see gpl.txt." );
		System.out.println( "This is free software, and you are welcome to redistribute it " +
				"under certain conditions; see gpl.txt for details.");
	}
	
	/**
	 * @param args
	 * @throws Throwable 
	 */
	@SuppressWarnings({ "deprecation" })
	public static void main(String[] args) {

		printLicense();
		
		UsaProxyLog loki;
		
		try 
		{
			System.out.print( "Parsing log file... " );
			loki = new UsaProxyLogParser().parseLog( args[ 0 ] );
			System.out.println( "done." );
		} 
		catch( ArrayIndexOutOfBoundsException ae )
		{
			printError( ae, "Please provide a file name.", true, true );
			return;
		}
		catch (Exception e) 
		{
			printError( e, "Unable to parse log file.", true, true );
			return;
		}

		SessionFactory sf;
		
		try
		{
			System.out.print( "Establishing database connection... " );
			Configuration cfg = new Configuration()
				.configure( new File("hibernate.cfg.xml"))
				.addURL( UsaProxyLog.class.getResource( "/META-INF/orm.xml" ) );
			sf = cfg.buildSessionFactory();
			System.out.println( "done." );
		}
		catch ( Exception e )
		{
			printError( e, "Unable to configure database connectivity. Check your configuration.", true, true );
			return;
		}
		
		try
		{
			System.out.print( "Persisting log contents... " );
			Session s = sf.openSession();
			Transaction tx = s.beginTransaction();
			s.persist( loki );
			tx.commit();
			s.close();
			sf.close();
			System.out.println( "done." );
		}
		catch ( Exception e )
		{
			printError( e, "Unable to persist data.", true, true );
			return;
		}
	}

}

package fi.uta.infim.usaproxylogparser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * UsaProxy 2.0 log parser interface definition.
 * @author Teemu Pääkkönen
 *
 */
public interface IUsaProxyLogParser {

	/**
	 * Parses the provided UsaProxy log and generates an object-based
	 * representation of it. The representation can be used to generate XML
	 * output using JAXB.
	 * @param file The file containing the log.
	 * @return The root object (node) of the log.
	 */
	public UsaProxyLog parseLog( File file ) throws IOException, ParseException;
	
	/**
	 * Parses the provided UsaProxy log and generates an object-based
	 * representation of it. The representation can be used to generate XML
	 * output using JAXB.
	 * @param filename The file name of the log file.
	 * @return The root object (node) of the log.
	 */
	public UsaProxyLog parseLog( String filename ) throws IOException, ParseException;
	
	/**
	 * Parses the provided UsaProxy log and generates an object-based
	 * representation of it. The representation can be used to generate XML
	 * output using JAXB.
	 * @param logStream A stream containing the log data.
	 * @return The root object (node) of the log.
	 */
	public UsaProxyLog parseLog( InputStream logStream ) throws IOException, ParseException;
	
}

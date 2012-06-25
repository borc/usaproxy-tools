package fi.uta.infim.usaproxylogparser;

/**
 * Signals that a DOM path found in a UsaProxy log file was invalid.
 * @author Teemu Pääkkönen
 *
 */
public class InvalidDOMPathException extends Exception {

	public InvalidDOMPathException(String string) {
		super( string );
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2520850283367409882L;

	
}

package fi.uta.infim.usaproxyFork;

/**
 * How to detect dynamic document changes.
 * @author Teemu Pääkkönen
 *
 */
public enum DynamicDetectionType {

	/**
	 * Let the client side script choose the most appropriate method for the
	 * browser used. Prefers mutation events.
	 */
	AUTO,
	
	/**
	 * Detect changes with a recurring check. This is very CPU intensive
	 * if the document is large. This works with any browser.
	 */
	TIMER,
	
	/**
	 * Detect changed by listening to mutation events. This method does not work
	 * in many browsers; notably IE9. The mutation event model is also deprecated
	 * in DOM level 3. A replacement is expected to appear in DOM4.
	 */
	MUTATION_EVENT,
	
	/**
	 * Don't detect dynamic changes actively. This doesn't mean that dynamic
	 * changes won't be noticed at all - there just will be no active attempts
	 * to detect changes. Instead, changes to element positions will be
	 * checked for during appearance/disappearance logging.
	 */
	NONE;
	
	/**
	 * Returns the corresponding enum value for a given CLI argument value (h|d|w|m).
	 * @param pCLIArg the CLI argument
	 * @return corresponding enum value
	 * @throws NoSuchFieldException if CLI argument does not match an enum value.
	 */
	public static DynamicDetectionType getTypeByCLIArg( String pCLIArg ) throws NoSuchFieldException
	{
		if ( "a".equalsIgnoreCase(pCLIArg) ) return AUTO;
		if ( "t".equalsIgnoreCase(pCLIArg) ) return TIMER;
		if ( "m".equalsIgnoreCase(pCLIArg) ) return MUTATION_EVENT;
		if ( "n".equalsIgnoreCase(pCLIArg) ) return NONE;
		
		throw new NoSuchFieldException( "Enum field for argument '" + pCLIArg + "' is unknown." );
	}
}

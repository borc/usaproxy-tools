package fi.uta.infim.usaproxyFork;

public enum LogSplitType {

	HOURLY,
	
	DAILY,
	
	WEEKLY,
	
	MONTHLY,
	
	NOSPLIT;
	
	public static LogSplitType getTypeByCLIArg( String pCLIArg ) throws NoSuchFieldException
	{
		if ( "h".equalsIgnoreCase(pCLIArg) ) return HOURLY;
		if ( "d".equalsIgnoreCase(pCLIArg) ) return DAILY;
		if ( "w".equalsIgnoreCase(pCLIArg) ) return WEEKLY;
		if ( "m".equalsIgnoreCase(pCLIArg) ) return MONTHLY;
		
		throw new NoSuchFieldException( "Enum field for argument '" + pCLIArg + "' is unknown." );
	}
	
	public Integer minSplitValue()
	{
		switch( this )
		{
		case HOURLY:
		case DAILY:
			return 0;
		case WEEKLY:
		case MONTHLY:
			return 1;
		default:
			return null;
		}
	}
	
	public Integer maxSplitValue()
	{
		switch( this )
		{
		case HOURLY:
			return 59;
		case DAILY:
			return 23;
		case WEEKLY:
			return 7;
		case MONTHLY:
			return 28;
		default:
			return null;
		}
	}
}

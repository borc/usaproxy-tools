package fi.uta.infim.usaproxyFork;

import java.util.Date;

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
	
	@SuppressWarnings("deprecation")
	public void zeroOut( Date pDate )
	{
		switch( this )
		{
		case MONTHLY:
		case WEEKLY:
			pDate.setHours( 0 );
		case DAILY:
			pDate.setMinutes( 0 );
		case HOURLY:
			pDate.setSeconds( 0 );
		default:
			break;
		}
	}
	
	static final long MINUTEMSECS = 60000L;
	static final long HOURMSECS = 3600000L;
	static final long DAYMSECS = 86400000L;
	static final long WEEKMSECS = 604800000L;
	
	private Long oneIntervalMsecs()
	{
		switch( this )
		{
		case HOURLY:
			return HOURMSECS;
		case DAILY:
			return DAYMSECS;
		case WEEKLY:
			return WEEKMSECS;
		default:
			return null;
		}
	}
	
	private Long splitTimeUnitMsecs()
	{
		switch( this )
		{
		case HOURLY:
			return MINUTEMSECS;
		case DAILY:
			return HOURMSECS;
		case WEEKLY:
			return DAYMSECS;
		default:
			return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	private Integer distanceToSplit( Date pDate, int splitAt )
	{
		switch( this )
		{
		case HOURLY:
			return ((60 - splitAt + pDate.getMinutes()) % 60);
		case DAILY:
			return ((24 - splitAt + pDate.getHours()) % 24);
		case WEEKLY:
			return ( ( 7 - splitAt + pDate.getDay() ) % 7 );
		default:
			return null;
		}
	}
	
	private long calculateNextSplit( Date originalTime, int interval, int splitAt )
	{
		return originalTime.getTime() + 
				(interval * this.oneIntervalMsecs()) -
				(this.distanceToSplit(originalTime, splitAt) * this.splitTimeUnitMsecs());
	}
	
	@SuppressWarnings("deprecation")
	public Date formatNextSplit( Date lastLogSplit, int interval, int splitAt )
	{
		Date nextLogSplit = null;
		
		switch( this )
		{
		case HOURLY:
		case DAILY:
		case WEEKLY:
			nextLogSplit = new Date( lastLogSplit.getTime() );
			this.zeroOut(nextLogSplit);
			nextLogSplit.setTime( this.calculateNextSplit(nextLogSplit, interval, splitAt));
			break;
			
		case MONTHLY:
			nextLogSplit = new Date( lastLogSplit.getTime() );
			nextLogSplit.setDate( splitAt );
			this.zeroOut(nextLogSplit);
			nextLogSplit.setMonth( (lastLogSplit.getMonth() + interval -
					( nextLogSplit.getDate() > lastLogSplit.getDate() ? 1 : 0 ) ) % 12 );
			
			break;
			
		case NOSPLIT:
			nextLogSplit = null;
			break;
			
		}
		
		return nextLogSplit;
	}
}

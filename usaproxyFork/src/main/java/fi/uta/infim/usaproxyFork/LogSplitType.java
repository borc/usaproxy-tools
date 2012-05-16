/* 
 *  UsaProxy - HTTP proxy for tracking, logging, and replay of user interactions on websites
 *  in order to enable web-based collaboration.
 *  <br><br>
 *  Copyright (C) 2006  Monika Wnuk - Media Informatics Group at the University of Munich
 *  Copyright (C) 2012 Teemu Pääkkönen - University of Tampere
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
*/
package fi.uta.infim.usaproxyFork;

import java.util.Date;

/**
 * An enumeration for different log splitting types.
 * @author Teemu Pääkkönen
 *
 */
public enum LogSplitType {

	/**
	 * Start a new log file once every n hours.
	 */
	HOURLY,
	
	/**
	 * Start a new log file once every n days.
	 */
	DAILY,
	
	/**
	 * Start a new log file once every n weeks.
	 */
	WEEKLY,
	
	/**
	 * Start a new log file once every n months.
	 */
	MONTHLY,
	
	/**
	 * Log everything into a single file.
	 */
	NOSPLIT;
	
	/**
	 * Returns the corresponding enum value for a given CLI argument value (h|d|w|m).
	 * @param pCLIArg the CLI argument
	 * @return corresponding enum value
	 * @throws NoSuchFieldException if CLI argument does not match an enum value.
	 */
	public static LogSplitType getTypeByCLIArg( String pCLIArg ) throws NoSuchFieldException
	{
		if ( "h".equalsIgnoreCase(pCLIArg) ) return HOURLY;
		if ( "d".equalsIgnoreCase(pCLIArg) ) return DAILY;
		if ( "w".equalsIgnoreCase(pCLIArg) ) return WEEKLY;
		if ( "m".equalsIgnoreCase(pCLIArg) ) return MONTHLY;
		
		throw new NoSuchFieldException( "Enum field for argument '" + pCLIArg + "' is unknown." );
	}
	
	/**
	 * The minimum value for the logSplitAt argument (when to start a new log file).
	 * @return min val for logSplitAt
	 */
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
	
	/**
	 * The maximum value for the logSplitAt argument (when to start a new log file).
	 * @return max val for logSplitAt
	 */
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
	
	/**
	 * When determining the next log split time, some fields of the timestamp
	 * should be set to 0, according to split type. This method takes care of that.
	 * @param pDate the timestamp to zero out
	 */
	@SuppressWarnings("deprecation")
	public void zeroOut( Date pDate )
	{
		// Note that there should be no break statements in this switch-case
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
	
	/**
	 * One minute in milliseconds
	 */
	private static final long MINUTEMSECS = 60000L;
	
	/**
	 * One hour in milliseconds
	 */
	private static final long HOURMSECS = 3600000L;
	
	/**
	 * One day in milliseconds
	 */
	private static final long DAYMSECS = 86400000L;
	
	/**
	 * One week in milliseconds
	 */
	private static final long WEEKMSECS = 604800000L;
	
	/**
	 * Returns the interval time unit's length in milliseconds for a 
	 * given enum value. 
	 * @return interval time unit's length in milliseconds
	 */
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
	
	/**
	 * Returns the split time unit's length in milliseconds for a 
	 * given enum value.
	 * @return split time unit's length in milliseconds
	 */
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
	
	/**
	 * Calculates the distance between the given timestamp and the previous
	 * split time in split time units. Only applicable for HOURLY, DAILY and WEEKLY.
	 * @param pDate timestamp to calculate distance to
	 * @param splitAt the log split time (in split time units)
	 * @return distance between the given timestamp and the previous split time in split time units
	 */
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
	
	/**
	 * Returns the next log split time in Unix epoch format.
	 * Only applicable for HOURLY, DAILY and WEEKLY.
	 * @param originalTime last split time
	 * @param interval log split interval
	 * @param splitAt log split time
	 * @return next log split time in unix epoch format
	 */
	private long calculateNextSplit( Date originalTime, int interval, int splitAt )
	{
		Date zeroedOutLastSplit = new Date( originalTime.getTime() );
		this.zeroOut(zeroedOutLastSplit);
		return zeroedOutLastSplit.getTime() + 
				(interval * this.oneIntervalMsecs()) -
				(this.distanceToSplit(zeroedOutLastSplit, splitAt) * this.splitTimeUnitMsecs());
	}
	
	/**
	 * Returns the next split time.
	 * @param lastLogSplit last log split time
	 * @param interval log split interval in interval time units
	 * @param splitAt log split time in split time units
	 * @return next split time
	 */
	@SuppressWarnings("deprecation")
	public Date getNextSplit( Date lastLogSplit, int interval, int splitAt )
	{
		Date nextLogSplit = null;
		
		switch( this )
		{
		case HOURLY:
		case DAILY:
		case WEEKLY:
			nextLogSplit = new Date();
			nextLogSplit.setTime( this.calculateNextSplit(lastLogSplit, interval, splitAt));
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

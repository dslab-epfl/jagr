package roc.pinpoint.analysis.structure;

import java.util.*;
import java.io.Serializable;

public class GrossStatistics implements Deviants, Serializable {

    static final int INITIAL_BUFFER_SIZE = 2;

    public static final int COUNT_DEVIATION = 0;
    public static final int MEAN_DEVIATION = 1;
    public static final int STDDEV_DEVIATION = 2;
    public static final int NUM_INDICES=3;

    AbstractStatistics[] stats;
    Object[] refs;
    int statsSize;

    Statistics counts;
    Statistics means;
    Statistics stddevs;

    public GrossStatistics() {
	stats = new AbstractStatistics[ INITIAL_BUFFER_SIZE ];
	refs = new Object[ INITIAL_BUFFER_SIZE ];
	counts = new Statistics();
	means = new Statistics();
	stddevs = new Statistics();
    }

    public void addStatistics( Collection c ) {
	ensureStatsCapacity( statsSize + c.size() );

	Iterator iter = c.iterator();
	while( iter.hasNext() ) {
	    AbstractStatistics s = (AbstractStatistics)iter.next();
	    addStatistics( s );
	}
    }

    protected synchronized void ensureStatsCapacity( int size ) {
	if( size < stats.length ) 
	    return;

	AbstractStatistics[] tempBuf = new AbstractStatistics[ size ];
	Object[] tempBuf2 = new Object[ size ];
	System.arraycopy( stats, 0, tempBuf, 0, statsSize );
	System.arraycopy( refs, 0, tempBuf2, 0, statsSize );
	stats = tempBuf;
	refs = tempBuf2;
    }


    public void addStatistics( AbstractStatistics statistics ) {
	addStatistics( statistics, statistics );
    }

    public synchronized void addStatistics( AbstractStatistics statistics, 
					    Object ref ) {
	if( statsSize == stats.length )
	    ensureStatsCapacity( stats.length * 2 );

	stats[ statsSize ] = statistics;
	refs[ statsSize ] = ref;
	statsSize++;
	counts.addValue( statistics.getCount(), statistics );
	means.addValue( statistics.getMean(), statistics );
	stddevs.addValue( statistics.getStdDev(), statistics );
    }


    public Statistics meanStatistics() {
	return means;
    }

    public Statistics countStatistics() {
	return counts;
    }

    public Statistics stdDevStatistics() {
	return stddevs;
    }

    public BitSet checkForDeviations( AbstractStatistics statistics ) {
	BitSet ret = new BitSet( NUM_INDICES );
	/*
	System.err.println( "GrossStatistics.checkForDeviations" );
	*/
	ret.set( COUNT_DEVIATION, 
		 !counts.isDeviant( statistics.getCount() ));
	ret.set( MEAN_DEVIATION,
		 !means.isDeviant( statistics.getMean() ));
	ret.set( STDDEV_DEVIATION,
		 !stddevs.isDeviant( statistics.getStdDev() ));
	/*
	System.err.println( "count is deviant: " + ret.get( COUNT_DEVIATION ));
	System.err.println( "mean is deviant: " + ret.get( MEAN_DEVIATION ));
	System.err.println( "stddev is deviant: " + ret.get( STDDEV_DEVIATION ));
	*/

	return ret;
    }

    public double getDeviation( AbstractStatistics statistics ) {
	double meanDev, countDev, stddevDev;
	
	meanDev = means.getDeviation( statistics.getMean() );
	countDev = counts.getDeviation( statistics.getCount() ) / counts.getMean();
	stddevDev = stddevs.getDeviation( statistics.getStdDev() );

	return Math.max( meanDev, Math.max( countDev, stddevDev ));
    }

    public double getDeviation( Object o ) throws ClassCastException {
	return getDeviation( (AbstractStatistics)o );
    }

    public boolean isDeviant( AbstractStatistics statistics ) {
	return !checkForDeviations( statistics).isEmpty();
    }

    public boolean isDeviant( Object dev ) {
	return isDeviant( (AbstractStatistics)dev );
    }
    

    public void setAcceptableDeviation( double dev ) {
	counts.setAcceptableDeviation( dev );
	means.setAcceptableDeviation( dev );
	stddevs.setAcceptableDeviation( dev );
    }

    public SortedSet getDeviants() {
	SortedSet ret = new TreeSet();

	for( int i=0; i<stats.length; i++ ) {
	    if( isDeviant( stats[i] )) {
		ret.add( new RankedObject( getDeviation( stats[i] ), refs[i] )); 
	    }
	}

	return ret;
    }

}

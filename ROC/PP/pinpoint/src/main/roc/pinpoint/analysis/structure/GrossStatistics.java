/**
    Copyright (C) 2004 Emre Kiciman and Stanford University

    This file is part of Pinpoint

    Pinpoint is free software; you can distribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    Pinpoint is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Pinpoint; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**/
package roc.pinpoint.analysis.structure;

// marked for release 1.0a

import java.util.*;
import java.io.Serializable;

/**
 * The GrossStatistics class allows comparisons to be made between
 * multiple sets of AbstractStatistics instances.  For example, where
 * each AbstractStatistics instance contains a distribution of
 * numbers, the GrossStatistics class lets us make comparisons between
 * these sets of numbers.  For example, what is the likelihood that a
 * given set of numbers was created by the same stochastic process
 * that generated other sets of numbers.
 *
 */
public class GrossStatistics implements Deviants, Serializable {

    static final long serialVersionUID = -6621499754367981432L;

    static final int INITIAL_BUFFER_SIZE = 2;

    public static final int COUNT_DEVIATION = 0;
    public static final int MEAN_DEVIATION = 1;
    public static final int STDDEV_DEVIATION = 2;
    public static final int NUM_INDICES=3;

    AbstractStatistics[] stats;
    Object[] refs;
    int statsSize;
    double acceptableDeviation = 1.0;

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
    
    public int size() {
	return statsSize;
    }

    public AbstractStatistics getStatistics( int idx ) {
	return stats[idx];
    }

    public Object getReference( int idx ) {
	return refs[idx];
    }

    public synchronized void removeStatistics( int idx ) {
	statsSize--;
	stats[idx] = stats[statsSize];
	refs[idx] = refs[statsSize];
	recalcStats();
    }

    /**
     * Adds several AbstractStatistics to this GrossStatistics
     * at once.
     * @param c a collection containing AbstractStatistics instances.
     */
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


    /**
     * adds an AbstractStatistics instance to this GrossStatistics
     * instance.  This function simply wraps addStatistics( AbstractStatistics statistics, Object ref), using the AbstractStatistics as its own reference value.
     */
    public void addStatistics( AbstractStatistics statistics ) {
	addStatistics( statistics, statistics );
    }

    /**
     * adds an AbstractStatistics instance with an associated reference
     * object.  Later, when we return deviant objects, we'll return
     * this reference value, instead of the statistics.
     */
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

    public synchronized void recalcStats() {
	counts = new Statistics();
	means = new Statistics();
	stddevs = new Statistics();

	for( int i=0; i<statsSize; i++ ) {
	    counts.addValue( stats[i].getCount(),stats[i] );
	    means.addValue( stats[i].getMean(),stats[i] );
	    stddevs.addValue( stats[i].getStdDev(),stats[i] );
	}
    }


    public synchronized AbstractStatistics getStatistics( Object ref ) {

	if( ref == null )
	    return null;

	for( int i=0; i<statsSize; i++  ) {
	    if( ref.equals( refs[i] )) {
		return stats[i];
	    }
	}

	return null;
    }

    /**
     * replaces the AbstractStatistics for a given reference value
     * with a new set of statistics.
     */
    public synchronized void replaceStatistics( AbstractStatistics statistics,
						Object ref ) {

	if( ref != null ) {
	    for( int i=0; i<statsSize; i++  ) {
		if( ref.equals( refs[i] )) {
		    stats[i] = statistics;
		    refs[i] = ref;
		    recalcStats();
		    return;
		}
	    }
	}

        addStatistics( statistics, ref );
    }


    /**
     * returns a set of numbers representing the mean of
     * each of the AstractStatistics stored within this GrossStatistics.
     */
    public Statistics meanStatistics() {
	return means;
    }

    /**
     * returns a set of numbers representing the count of
     * each of the AstractStatistics stored within this GrossStatistics.
     */
    public Statistics countStatistics() {
	return counts;
    }

    /**
     * returns a set of numbers representing the stddev of
     * each of the AstractStatistics stored within this GrossStatistics.
     */
    public Statistics stdDevStatistics() {
	return stddevs;
    }

    public BitSet checkForDeviations( AbstractStatistics statistics,
				      double normalizeThisCount,
				      double normalizeOtherCount ) {
	BitSet ret = new BitSet( NUM_INDICES );
	/*
	System.err.println( "GrossStatistics.checkForDeviations" );
	*/
	ret.set( COUNT_DEVIATION, 
		 !counts.isDeviant( statistics.getCount() ));
	ret.set( MEAN_DEVIATION,
		 !means.isDeviant( statistics.getMean() ));
	ret.set( STDDEV_DEVIATION,
		 !stddevs.isDeviant( (normalizeThisCount * statistics.getStdDev() / normalizeOtherCount ) / counts.getMean() ));
	/*
	System.err.println( "count is deviant: " + ret.get( COUNT_DEVIATION ));
	System.err.println( "mean is deviant: " + ret.get( MEAN_DEVIATION ));
	System.err.println( "stddev is deviant: " + ret.get( STDDEV_DEVIATION ));
	*/

	return ret;
    }


    public BitSet checkForDeviations( AbstractStatistics statistics ) {
	return checkForDeviations( statistics, 1.0, 1.0 );
    }

    public double getDeviation( AbstractStatistics statistics ) {
	return getDeviation( statistics, 1.0, 1.0 );
    }

    private double[] getAllValues() {
	int count = 0;
	for( int i=0; i<statsSize; i++ ) {
	    count += stats[i].valuesSize;
	}

	double[] t = new double[count];

	int curr = 0;
	for( int i=0; i<statsSize; i++ ) {
	    System.arraycopy( stats[i].values, 0, t, curr, stats[i].valuesSize );
	    curr += stats[i].valuesSize;
	}

	return t;
    }

    
    /**
     * returns the median absolute deviation, used for calculating
     * deviation when we have few samples because of its robustness
     * to outliers.
     */
    public double getMedianAbsoluteDeviation( AbstractStatistics statistics ) {

	// 1. calculate median
	double[] all = getAllValues();

	Arrays.sort( all );
	double median = all[ all.length/2 ];

	// 2. iterate over all, calculate their diff from
	//    median.
	double[] dev = new double[ all.length ];
	for( int i=0; i<all.length; i++ ) {
	    dev[i] = Math.abs( all[i] - median );
	}

	// 3. calculate the median absolute deviation
	Arrays.sort( dev );
	double medianDev = dev[ dev.length / 2 ];


	// 4. calculate the deviation of elements in statistics
	double avg = 0;

	if( medianDev != 0 && statistics.valuesSize > 0 ) {
	    double[] s = new double[ statistics.valuesSize ];
	    for( int i=0; i<statistics.valuesSize; i++ ) {
		s[i] = Math.abs( statistics.values[i] - median ) / medianDev;
		avg += s[i];
	    }
	    avg = avg/s.length;
	}
	

	return avg;
    }

    public double getDeviation( AbstractStatistics statistics,
				double normalizeThisCount,
				double normalizeOtherCount ) {
	double meanDev, countDev, stddevDev;
	
	meanDev = means.getDeviation( statistics.getMean() ) / means.getStdDev();
	return meanDev;

	/**
	countDev = counts.getDeviation(normalizeThisCount * statistics.getCount() / normalizeOtherCount) / counts.getStdDev();
	stddevDev = stddevs.getDeviation( statistics.getStdDev() ) / stddevs.getStdDev();

	return Math.max( meanDev, Math.max( countDev, stddevDev ));
	*/
    }

    public double getDeviation( Object o ) throws ClassCastException {
	return getDeviation( (AbstractStatistics)o );
    }

    public boolean isDeviant( AbstractStatistics statistics ) {
	return isDeviant( statistics, 1.0, 1.0 );
    }

    public boolean isDeviant( AbstractStatistics statistics, 
			      double normalizeThisCount,
			      double normalizeOtherCount ) {
	return !checkForDeviations( statistics,
				    normalizeThisCount,
				    normalizeOtherCount ).isEmpty();
    }

    public boolean isDeviant( Object dev ) {
	return isDeviant( (AbstractStatistics)dev );
    }
    

    public void setAcceptableDeviation( double dev ) {
	this.acceptableDeviation = dev;
	counts.setAcceptableDeviation( dev );
	means.setAcceptableDeviation( dev );
	stddevs.setAcceptableDeviation( dev );
    }

    public SortedSet getDeviants() {
	SortedSet ret = new TreeSet();

	for( int i=0; i<statsSize; i++ ) {
	    if( isDeviant( stats[i] )) {
		double d = getMedianAbsoluteDeviation( stats[i] );
		if( d >= acceptableDeviation ) {
		    ret.add( new RankedObject( d, refs[i] ));
		}
	    }
	}

	return ret;
    }

    public String toString() {
	StringBuffer str = new StringBuffer();
	str.append( "{GrossStatistics:" );

	str.append( "\n\t{<counts> (" + counts.getMean() + "," + counts.getStdDev() + ")" );
	str.append( "\n\t{<means> (" + means.getMean() + "," + means.getStdDev() + ")" );
	str.append( "\n\t{<stddev> (" + stddevs.getMean() + "," + stddevs.getStdDev() + ")" );

	for( int i=0; i<statsSize; i++ ) {
	    str.append( "\n\t" );
	    str.append( stats[i].toString() );
	}
	str.append( "\n}" );

	return str.toString();
    }

}

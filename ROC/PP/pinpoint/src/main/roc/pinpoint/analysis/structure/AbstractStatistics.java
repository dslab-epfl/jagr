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

// marked for release 1.0

import java.util.SortedSet;
import java.util.TreeSet;
import java.io.Serializable;

/**
 * base class for objects that want to manage and calculate statistics
 * about a set of numbers
 *
 */
public abstract class AbstractStatistics implements Deviants, Serializable {

    static final long serialVersionUID = 7026485845563444729L;

    private static final int INITIAL_SIZE = 2;

    double[] values;
    Object[] refs;
    int valuesSize;

    boolean cacheIsValid;
    double cachedMean;
    double cachedStdDev;
    double cachedMin;
    double cachedMax;

    double acceptableDeviation = 2;

    protected AbstractStatistics() {
	values = new double[ INITIAL_SIZE ];
	refs = new Object[ INITIAL_SIZE ];
	valuesSize = 0;
	cacheIsValid = false;
    }

    protected AbstractStatistics( AbstractStatistics statistics ) {
	this.values = statistics.values;
	this.refs = statistics.refs;
	this.valuesSize = statistics.valuesSize;
	this.cacheIsValid = false;
	this.acceptableDeviation = statistics.acceptableDeviation;
    }
    
    protected synchronized void updateCachedStatistics() {

	if( cacheIsValid )
	    return;

	cachedMin = values[0];
	cachedMax = values[0];

	double total = 0;
	for( int i=0; i<valuesSize; i++ ) {
	    total += values[i];
	    if( values[i] < cachedMin )
		cachedMin = values[i];
	    if( values[i] > cachedMax )
		cachedMax = values[i];
	}
	cachedMean = total / valuesSize;

	double sumSquaredDeviation = 0;
	for( int i=0; i<valuesSize; i++ ) {
	    sumSquaredDeviation +=
		Math.pow( values[i] - cachedMean, 2 );
	}
	cachedStdDev = Math.sqrt( sumSquaredDeviation );
    }


    /**
     * @return the number of values stored in this object
     */
    public double getCount() {
	return valuesSize;
    }

    /**
     * @return the mean of the values stored in this object
     */
    public double getMean() {
	updateCachedStatistics();
	return cachedMean;
    }

    /**
     * @return the min of the values stored in this object
     */
    public double getMin() {
	updateCachedStatistics();
	return cachedMin;
    }

    /**
     * @return the max of the values stored in this object
     */
    public double getMax() {
	updateCachedStatistics();
	return cachedMax;
    }

    /**
     * @return the stddev of the values stored in this object
     */
    public double getStdDev() {
	updateCachedStatistics();
	return cachedStdDev;
    }

    protected synchronized void ensureValuesCapacity( int size ) {
	if( values.length > size )
	    return;

	double[] tempBuf = new double[ size ];
	Object[] tempBuf2 = new Object[ size ];
	System.arraycopy( values, 0, tempBuf, 0, valuesSize );
	System.arraycopy( refs, 0, tempBuf2, 0, valuesSize );
	values = tempBuf;
	refs = tempBuf2;
    }

    /**
     * subclasses should use this method to add values to the
     * set of numbers tracked by the abstract statistics class.
     */
    protected synchronized void addValue( double v, Object ref ) {
	cacheIsValid = false;

	if( valuesSize == values.length ) {
	    ensureValuesCapacity( values.length * 2 );
	}
	values[valuesSize] = v;
	refs[valuesSize] = ref;
	valuesSize++;
    }
    
    protected synchronized void addValues(AbstractStatistics as) {
        cacheIsValid = false;
        
        if(valuesSize+as.valuesSize >= values.length) {
            ensureValuesCapacity( valuesSize +as.valuesSize );
        }
        
        System.arraycopy(as.values,0,values,valuesSize,as.valuesSize);
        System.arraycopy(as.refs,0,refs,valuesSize,as.valuesSize);
        valuesSize += as.valuesSize;
    }

    protected synchronized void removeFirstValues( int num ) {
	cacheIsValid = false;

	if( num == 0 || valuesSize == 0 ) {
	    // do nothing
	}
	else if( valuesSize <= num ) {
	    // throw everything away
	    valuesSize = 0;
	    values = new double[ INITIAL_SIZE ];
	    refs = new Object[ INITIAL_SIZE ];
	}
	else {
	    // throw away first num items
	    valuesSize = valuesSize-num;
	    System.arraycopy(values,num,values,0,valuesSize);
	    System.arraycopy(refs,num,refs,0,valuesSize);
	}
    }

    /**
     * sets the threshold for the acceptable deviation from the norm, 
     * used by the isDeviant() method
     */
    public void setAcceptableDeviation( double dev ) {
	acceptableDeviation = dev;
    }

    /**
     * returns the current value of the acceptable deviation threshold
     */
    public double getAcceptableDeviation() {
	return acceptableDeviation;
    }

    /**
     * returns the object references associated with unacceptably 
     * deviant values.
     */
    public synchronized SortedSet getDeviants() {
	SortedSet ret = new TreeSet();

	double d = acceptableDeviation * getStdDev();

	for( int i=0; i < valuesSize; i++ ) {
	    if( Math.abs( values[i] - cachedMean ) > d ) {
		ret.add( new RankedObject( values[i]-cachedMean, refs[i] ));
	    }
	}

	return ret;
    }

    protected boolean isDeviant( double v ) {
	/*
	System.err.println( "abstractstatistics.isDeviant" );
	System.err.println( "ABS(" + v + " - " + getMean() + " ) >? ( " +
			    acceptableDeviation + " * " + getStdDev() + " ) = " +
			    (Math.abs(v - getMean()) > ( acceptableDeviation * getStdDev())));
		
	*/	    
	return Math.abs(v - getMean()) > ( acceptableDeviation * getStdDev());
    }

    /**
     * returns the deviation of the value v with respect to the distribution
     * of values stored within this object.
     */
    public double getDeviation( double v ) {
	return Math.abs(v - getMean());
    }

    /**
     * returns the deviation of an arbitrary object v.  subclasses should
     * implement this method by extracting a representative value for this
     * object and return getDeviation() of that value.
     */
    public abstract double getDeviation( Object v ) throws ClassCastException;

    /**
     * returns true if the deviation of this object, as returned by
     * getDeviation( Object v ) does not meet the acceptable deviation
     * threshold.
     */
    public abstract boolean isDeviant( Object o ) throws ClassCastException;


    public String toString() {
	return "{AbstractStatistics: count=" + getCount() + 
	    ", mean=" + getMean() + 
	    ", stddev=" + getStdDev() + "}";
    }
}

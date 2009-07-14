package roc.pinpoint.analysis.structure;

import java.util.SortedSet;
import java.util.TreeSet;
import java.io.Serializable;

public abstract class AbstractStatistics implements Deviants, Serializable {

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


    public double getCount() {
	return valuesSize;
    }


    public double getMean() {
	updateCachedStatistics();
	return cachedMean;
    }

    public double getMin() {
	updateCachedStatistics();
	return cachedMin;
    }

    public double getMax() {
	updateCachedStatistics();
	return cachedMax;
    }

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

    protected synchronized void addValue( double v, Object ref ) {
	cacheIsValid = false;

	if( valuesSize == values.length ) {
	    ensureValuesCapacity( values.length * 2 );
	}

	values[valuesSize] = v;
	refs[valuesSize] = ref;
	valuesSize++;
    }

    public void setAcceptableDeviation( double dev ) {
	acceptableDeviation = dev;
    }

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

    public double getDeviation( double v ) {
	return Math.abs(v - getMean());
    }

    public abstract double getDeviation( Object v ) throws ClassCastException;

    public abstract boolean isDeviant( Object o ) throws ClassCastException;


    public String toString() {
	return "{AbstractStatistics: count=" + getCount() + 
	    ", mean=" + getMean() + 
	    ", stddev=" + getStdDev() + "}";
    }
}

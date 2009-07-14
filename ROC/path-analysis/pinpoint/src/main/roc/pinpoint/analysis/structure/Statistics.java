package roc.pinpoint.analysis.structure;

import java.util.SortedSet;

public class Statistics extends AbstractStatistics {

    public Statistics() {
    }

    public void addValue( double v, Object ref ) {
	super.addValue( v, ref );
    }
    
    public void setAcceptableDeviation( double dev ) {
	super.setAcceptableDeviation( dev );
    }

    public double getAcceptableDeviation() {
	return super.getAcceptableDeviation();
    }

    public double getDeviation( Object o ) throws ClassCastException {
	return getDeviation( ((Number)o).doubleValue() );
    }

    public boolean isDeviant( double v ) {
	return super.isDeviant( v );
    }

    public boolean isDeviant( Object o ) throws ClassCastException {
	if( !( o instanceof Number )) 
	    throw new ClassCastException( "Can't measure deviation of " + o.getClass().toString() + " in roc.pinpoint.analysis.structure.Statistics.isDeviant()" );

	return isDeviant( ((Number)o).doubleValue() );
    }

    public SortedSet getDeviants() {
	return super.getDeviants();
    }

}

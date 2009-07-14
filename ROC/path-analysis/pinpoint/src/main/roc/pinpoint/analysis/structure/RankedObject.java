package roc.pinpoint.analysis.structure;

import java.io.Serializable;

public class RankedObject implements Comparable,  Serializable {
    double rank;
    Object value;

    public RankedObject() {
	this( 0, null );
    }

    public RankedObject( double rank, Object value ) {
	this.rank = rank;
	this.value = value;
    }

    public void setRank( double rank ) {
	this.rank = rank;
    }
    
    public double getRank() {
	return rank;
    }

    public void setValue( Object value ) {
	this.value = value;
    }

    public Object getValue() {
	return value;
    }

    public int compareTo( Object o ) {
	if( !( o instanceof RankedObject )) {
	    throw new ClassCastException( "cannot compare " + 
					  "RankedObject to " +
					  o.getClass().toString() );
	}
	    
	RankedObject other = (RankedObject)o;
	    
	/*
	if( this.rank == other.rank )
	    return 0;
	*/

	if( this.rank > other.rank )
	    return 1;

	// if( this.rank < other.rank )
	return -1;
    }

    public String toString() {
	return "{rank=" + rank + ", value=" + value + "}\n";
    }
}

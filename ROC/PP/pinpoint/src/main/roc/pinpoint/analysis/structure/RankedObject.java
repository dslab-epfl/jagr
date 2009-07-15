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

import java.io.Serializable;

/**
 * RankedObject is a simple wrapper class around arbitrary objects
 * that lets us compare and sort them based on some rank value.
 */
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

    public boolean equals( Object o ) {
	if( !(o instanceof RankedObject ) )
	    return false;
	
	RankedObject other = (RankedObject)o;
	return ( value.equals( other.value ));
    }
    
    public int hashCode() {
	return value.hashCode();
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

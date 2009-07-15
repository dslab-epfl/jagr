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
import java.util.*;

/**
 * This class tracks usage information about an object.  For example,
 * what requests used a given a component.
 */
public class UsageTracker implements Identifiable, Serializable, Distanceable {

    private static final int INITIAL_CAPACITY = 2;

    Identifiable value;
    HashSet usedwith;

    public UsageTracker( Identifiable value ) {
	this.value = value;
	usedwith = new HashSet( INITIAL_CAPACITY );
    }

    public void addUsedWith( Object o ) {
	usedwith.add( o );
    }

    public Identifiable getValue() {
	return value;
    }

    public double getDistance( Distanceable d ) {
	try {
	    return getDistance( JACCARD, d );
	}
	catch( UnsupportedDistanceMetricException e ) {
	    throw new RuntimeException( "ERROR: Default distance metric is not supported? Should never happen" );
	}
    }

    public double getDistance( int distancemetric, Distanceable d ) 
	throws UnsupportedDistanceMetricException {

	if( distancemetric == JACCARD ) {
	    return getJaccardDistance( d );
	}
	else {
	    throw new UnsupportedDistanceMetricException( distancemetric );
	}
    }
    

    public double getJaccardDistance( Distanceable d ) {
	if( !(d instanceof UsageTracker )) 
	    throw new ClassCastException( "cannot compare UsageTracker to " + d.getClass().toString() );

	double ret = 0;
	UsageTracker other = (UsageTracker)d;

	HashSet intersection = new HashSet( this.usedwith );
	intersection.retainAll( other.usedwith );

	double a = intersection.size();
	double b = this.usedwith.size() - a;
	double c = other.usedwith.size() - a;

	// distance = 0 is perfect match, 1.0 is completely different
	ret = (b+c)/(a+b+c);
	
	return ret;
    }
    
    public boolean matchesId( Map id ) {
	return value.matchesId( id );
    }

    public Map getId() {
	return value.getId();
    }

    public String toString() {
	return "{UsageTracker: o=" + value.toString() + ";  usedWith=" + usedwith.toString() + "}";
    }

}

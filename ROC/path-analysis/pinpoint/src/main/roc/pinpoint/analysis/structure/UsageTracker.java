package roc.pinpoint.analysis.structure;

import java.io.Serializable;
import java.util.*;

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

}

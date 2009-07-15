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
package roc.pinpoint.analysis.plugins2.unified;

import java.util.*;

import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;
import roc.pinpoint.analysis.plugins2.fe.*;

/**
 * this plugin implements an HTTP/HTML front end, allowing users to
 * on-demand compare the Components inside records against other components
 *
 * @author emrek
 *
 */
public class ComponentDistances implements HttpRecordPlugin, Plugin {

    PluginArg[] args = {
    };

    public PluginArg[] getPluginArguments() {
	return args;
    }

    public void start( String id, Map args, AnalysisEngine engine ) {
    }

    public void stop() {
    }

    public String doHttp( RecordCollection rc, Record rec ) 
	throws PluginException {

	String ret;

	Object v = rec.getValue();
	Map recordValues = extractRecordValues( rc );

	ret = "<p><font size=\"-2\">roc.pinpoint.analysis.plugins2.unified.</font>" + 
	    "<b>ComponentDistances</b><p>";

	if( v instanceof WeightedSimpleComponentBehavior ) {
	    ret += getDistances( (WeightedSimpleComponentBehavior)v, 
				recordValues );
	}
	else if( v instanceof Distanceable ) {
	    ret += getDistances( (Distanceable)v,
				recordValues );
	}
	else {
	    ret += "could not get distances for this object!";
	}

	return ret;
    }

    Map extractRecordValues( RecordCollection rc ) {
	Map ret = new HashMap( rc.size() );

	Map records = rc.getAllRecords();
	Iterator iter = records.keySet().iterator();
	while( iter.hasNext() ) {
	    Object key = iter.next();
	    Record rec = (Record)records.get( key );
	    Object o = rec.getValue();
	    ret.put( key, o );
	}

	return ret;
    }

    String getDistances( WeightedSimpleComponentBehavior wscb, 
			 Map recordValues ) {

	LockedComponentBehavior lcb = wscb.lockValues();
	
	Map distanceValues = new HashMap( recordValues.size() );
	Iterator iter = recordValues.keySet().iterator();
	while( iter.hasNext() ) {
	    Object key = iter.next();
	    WeightedSimpleComponentBehavior v = 
		(WeightedSimpleComponentBehavior)recordValues.get( key );
	    distanceValues.put( key, v.lockValues() );
	}

	return getDistances( lcb, distanceValues );
    }

    String getDistances( Distanceable d, 
			 Map recordValues ) {
	
	//System.err.println( "[DEBUG] comparing '" + d + "' to " + recordValues.size() + " other items!" );

	SortedSet set = new TreeSet();

	Iterator iter = recordValues.keySet().iterator();
	while( iter.hasNext() ) {
	    Object key = iter.next();
	    Distanceable other = (Distanceable)recordValues.get( key );
	    double dist = d.getDistance( other );
	    set.add( new Distance( dist, 
				   key, 
				   other ));
	}

	StringBuffer ret = new StringBuffer();
	ret.append( "<pre>" );
	iter = set.iterator();
	while( iter.hasNext() ) {
	    Object o = iter.next();
	    ret.append( o.toString() );
	    ret.append( "\n" );
	}
	ret.append( "</pre>" );

	return ret.toString();
    }


    public static class Distance implements Comparable {
	double distance;
	Object name;
	Distanceable obj;

	Distance( double distance, Object name, Distanceable obj ) {
	    this.distance = distance;
	    this.name = name;
	    this.obj = obj;
	}
	
	public String toString() {
	    return "[" + distance + "] " + name.toString();
	}
	
	public int compareTo( Object o ) {
	    Distance other = (Distance)o;

	    int ret;

	    if( this.distance < other.distance ) 
		ret = -1;
	    else if( this.distance > other.distance )
		ret = 1;
	    else {
		ret = this.obj.hashCode() - other.obj.hashCode();
	    }

	    return ret;
	}

	public boolean equals( Object o ) {
	    if( !(o instanceof Distance) )  {
		return false;
	    }

	    return (compareTo(o) == 0 );
	}

    }

}

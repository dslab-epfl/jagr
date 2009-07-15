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
/*
 * Created on Aug 31, 2003
 *
 */
package roc.pinpoint.analysis.pcfg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import roc.pinpoint.analysis.structure.Identifiable;

/**
 * This class is a node in the Grammar.  Each node represents a 
 * component in some system, and holds statistics about what other
 * components are called by this component, and how often.
 * 
 * @author emrek
 *
 */
public class Node implements Identifiable, Serializable {

    static final long serialVersionUID = -6295813059952559780L;
	
    Map id; /* id is a component id */

    int total;
    Map childcount;  /** key = List<id>, value= count **/

    /**
     * constructor
     * @param id the identifying attributes of this component
     */
    public Node( Map id ) {
        this.id = id;
        total = 0;
        childcount = new HashMap();
    }

    public int branchfactor() {
	return childcount.size();
    }

    public String toString() {
	String ret = "[" + id + "; ";
	
	Iterator iter = childcount.keySet().iterator();
	while( iter.hasNext() ) {
	    List id = (List)iter.next();
	    Integer count = (Integer)childcount.get(id);
	    
	    ret += "\t\t{";
	    Iterator iter2 = id.iterator();
	    while( iter2.hasNext() ) {
		ret += iter2.next().toString();
	    }
	    ret += "} = " + count + "\n";
	}

	ret += "]";
	
	return ret;
    }

    public void addChildSet( List childids ) {
    	Integer cInt = (Integer)childcount.get( childids );
 	int c = 0;
    	if( cInt != null ) {
    	    c = cInt.intValue();
    	}
    	c++;
    	total++;
    	childcount.put( childids, new Integer( c ));

    }

    public double getScore( List childids ) {
    	Integer cInt = (Integer)childcount.get( childids );
    	int c = 0;
    	if( cInt != null ) {
    	    c = cInt.intValue();
    	}

	if( childcount.size() == 0 ) {
	    return 0.0;
	}

	double val = ((double)c / (double)total );
	if( val > 1.0 ) {
	    System.err.println( "EMK: Ack! : val > 1 !!\nc= " + 
				c + "\ntotal= " + total );
	}

	double avg = 1d / (double)childcount.size();



	double ret = val - avg;
	if( ret > 0 ) {
	    ret = 0;
	}
	ret *= -1.0;
	
	if( Double.isNaN(ret) || ret == Double.POSITIVE_INFINITY ) {
	    System.err.println( "EMK: ack! ret = " + ret );
	    //System.err.println( "stddev = " + stddev );
	    System.err.println( "childcount.size = " + childcount.size() );
	    System.err.println( "val = " + val );
	    System.err.println( "avg = " + avg );
	    System.err.println( "total = " + total );
	    System.err.println( "c = " + c );
	}

	return ret;
    }


    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.structure.Identifiable#getId()
     */
    public Map getId() {
	return id;
    }

    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.structure.Identifiable#matchesId(java.util.Map)
     */
    public boolean matchesId(Map attrs) {
	return id.equals( attrs );
    }

}

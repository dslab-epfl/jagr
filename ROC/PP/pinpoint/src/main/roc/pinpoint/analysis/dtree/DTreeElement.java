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
package roc.pinpoint.analysis.dtree;

import java.util.*;
import roc.pinpoint.analysis.structure.*;

public class DTreeElement {

    private Set attrs;

    private int numNegative;
    private int numPositive;

    public DTreeElement( Identifiable id,
			 int numNegative,
			 int numPositive ) {
	this( FlattenMap( id.getId() ),
	      numNegative, numPositive );
    }

    public DTreeElement( Map attrs,
			 int numNegative,
			 int numPositive ) {
	this( FlattenMap( attrs ),
	      numNegative,
	      numPositive );
    }

    public DTreeElement( Set attrs,
			 int numNegative,
			 int numPositive ) {
	this.attrs = attrs;
	this.numNegative = numNegative;
	this.numPositive = numPositive;
    }

   protected static Set FlattenMap( Map idmap ) {
	Set ret = new TreeSet();
	Iterator iter = idmap.entrySet().iterator();
	while( iter.hasNext() ) {
	    Map.Entry e = (Map.Entry) iter.next();
	    Object k = e.getKey();
	    Object v = e.getValue();

	    if( v instanceof Collection ) {
		Iterator iter2 = ((Collection)v).iterator();
		while( iter2.hasNext() ) {
		    Object v2 = iter2.next();
		    ret.add( ((k==null)?"null":k.toString()) + "=" +
			     ((v2==null)?"null":v2.toString()) );
		}

	    }
	    else {
		ret.add( ((k==null)?"null":k.toString()) + "=" +
			 ((v==null)?"null":v.toString()) );
	    }
	}

	return ret;
    }

    public Set getAttrs() {
	return attrs;
    }
			
    public int getNumPositive() {
	return numPositive;
    }

    public void setNumPositive( int numPositive ) {
	this.numPositive = numPositive;
    }
    
    public int getNumNegative() {
	return numNegative;
    }

    public void setNumNegative( int numNegative ) {
	this.numNegative = numNegative;
    }

    public double getEntropy() {
	return DTreeUtil.CalculateEntropy( numPositive, numNegative );
    }
}

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

import java.util.*;

public class Component extends AbstractStatistics implements Identifiable {

    static final long serialVersionUID = -9212697530774015954L;

    String singleattr;
    Map attrs;
    int hashcode=-1;

    public Component( String attr ) {
        singleattr = attr;
        attrs = null;
    }

    public Component( Map attrs ) {
        singleattr=null;
	if( attrs.containsKey("name") ) {
	    // special case for URLs
	    String n = (String)attrs.get( "name" );
	    if( n != null ) {
		int idx = n.indexOf( "?" );
		if( idx != -1 && n.startsWith( "/" )) {
		    n = n.substring( 0, idx );
		    attrs.put( "name", n );
		}
	    }
	}
	this.attrs = attrs;
    }

    public boolean matchesId( Map id ) {
	if( attrs == null )
            return false;

	return attrs.equals( id );
    }

    public Map getId() {
	return attrs;
    }

    /*
    public void addValue( double v, Object ref ) {
	super.addValue( v, ref );
    }
    */

    public boolean isDeviant( Object o ) throws ClassCastException {
	if( !( o instanceof Number )) 
	    throw new ClassCastException( "Can't measure deviation of " + o.getClass().toString() + " in roc.pinpoint.analysis.structure.Component.isDeviant()" );

	return isDeviant( ((Number)o).doubleValue() );
    }

    public double getDeviation( Object o ) throws ClassCastException {
	return getDeviation( ((Number)o).doubleValue() );
    }

    public boolean equals( Object o ) {
	if( !( o instanceof Component ))
	    return false;

        Component other = (Component)o;
	
        return ((singleattr == null )?(other.singleattr == null):(singleattr.equals(other.singleattr))) &&
            (attrs == null)?(other.attrs==null):(attrs.equals(other.attrs ));
    }

    public int hashCode() {
        if( hashcode == -1 ) {
            hashcode = (singleattr==null)?attrs.hashCode():singleattr.hashCode();
        }
        return hashcode;
    }

    
    public static Component ReduceComponent( Component c, Collection keys ) {
	Map attrs = IdentifiableHelper.ReduceMap( c.attrs, keys );
	return new Component( attrs );
    }

    public String toString() {
	return "{Component: attrs=" + attrs.toString()  
	    /* + ", stats=" + super.toString()*/ + "}";
    }

}

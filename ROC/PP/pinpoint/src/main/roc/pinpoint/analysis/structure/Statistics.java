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

import java.util.SortedSet;

/**
 * A wrapper implementation around AbstractStatistics that exposes
 * the underlying functionality as public methods.  This class can
 * be used to represents a plain set of numbers where there is no
 * need to tie the set of numbers with other information.
 */
public class Statistics extends AbstractStatistics {

    static final long serialVersionUID = 2511802295516914088L;

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

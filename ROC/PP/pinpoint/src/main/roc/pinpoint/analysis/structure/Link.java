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

/**
 * This class represents a Link between two Components.
 * The values stored in a Link can be used to represent the
 * performance of the link.  Alternatively, we can also simply
 * use the count of the number of times a link is used.
 */
public class Link extends AbstractStatistics {

    Component src;
    Component sink;

    int hashcode=-1;

    static final long serialVersionUID = -8436884405839590879L;

    public Link( Component src, Component sink ) {
	this.src = src;
	this.sink = sink;
    }

    public Link( Component src, Component sink, Link statistics ) {
	super( statistics );
	this.src = src;
	this.sink = sink;
    }

    public Component getSource() {
	return src;
    }

    public Component getSink() {
	return sink;
    }

    public void addValue( double v, Object ref ) {
	super.addValue( v, ref );
    }
    
    public void addLink(Link link ) {
        super.addValues(link);
    }

    public void removeFirstValues( int num ) {
	super.removeFirstValues(num);
    }

    public boolean isDeviant( Object o ) throws ClassCastException {
	if( !( o instanceof Number )) 
	    throw new ClassCastException( "Can't measure deviation of " + o.getClass().toString() + " in roc.pinpoint.analysis.structure.Link.isDeviant()" );

	return isDeviant( ((Number)o).doubleValue() );
    }

    public double getDeviation( Object o ) throws ClassCastException {
	return getDeviation( ((Number)o).doubleValue() );
    }

    public boolean equals( Object o ) {
	if( !(o instanceof Link ))
	    return false;

	if( src.equals(((Link)o).src ) &&
	    sink.equals(((Link)o).sink ))
	    return true;
	else 
	    return false;
    }

    public int hashCode() {
        if( hashcode == -1 ) {
            hashcode = src.hashCode() + sink.hashCode();
        }
        return hashcode;
    }


    public String toString() {
	return "{Link: src=" + src.toString() +
	    ", sink=" + sink.toString() + ", stats=" + super.toString() + "}";
    }
}

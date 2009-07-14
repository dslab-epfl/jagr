package roc.pinpoint.analysis.structure;

import java.io.Serializable;
import java.util.List;

public class Link extends AbstractStatistics {

    Component src;
    Component sink;

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
	return src.hashCode() + sink.hashCode();
    }


    public String toString() {
	return "{Link: src=" + src.toString() +
	    ", sink=" + sink.toString() + ", stats=" + super.toString() + "}";
    }
}

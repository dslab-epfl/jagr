package roc.pinpoint.analysis.structure;

import java.util.*;
import java.io.Serializable;

public class Component extends AbstractStatistics implements Identifiable {

    Map attrs;
   
    public Component( Map attrs ) {
	if( attrs.containsKey("name") ) {
	    // special case for URLs
	    String n = (String)attrs.get( "name" );
	    int idx = n.indexOf( "?" );
	    if( idx != -1 && n.startsWith( "/" )) {
		n = n.substring( 0, idx );
		attrs.put( "name", n );
	    }
	}
	this.attrs = attrs;
    }

    public boolean matchesId( Map id ) {
	
	return attrs.equals( id );
    }

    public Map getId() {
	return attrs;
    }

    public void addValue( double v, Object ref ) {
	super.addValue( v, ref );
    }

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
	
	return attrs.equals( ((Component)o).attrs );
    }

    public int hashCode() {
	return attrs.hashCode();
    }

    
    public static Map ReduceMap( Map m, Collection keys ) {
	Map attrs = new HashMap();
	Iterator iter = keys.iterator();
	while( iter.hasNext() ) {
	    Object k = iter.next();
	    if( m.containsKey( k )) {
		attrs.put( k, m.get( k ));
	    }
	}

	return attrs;
    }

    public static Component ReduceComponent( Component c, Collection keys ) {
	Map attrs = ReduceMap( c.attrs, keys );
	return new Component( attrs );
    }

    public String toString() {
	return "{Component: attrs=" + attrs.toString()  
	    /* + ", stats=" + super.toString()*/ + "}";
    }

}

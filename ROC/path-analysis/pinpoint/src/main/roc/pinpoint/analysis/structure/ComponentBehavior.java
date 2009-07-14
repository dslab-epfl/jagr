package roc.pinpoint.analysis.structure;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.Serializable;

public class ComponentBehavior implements Identifiable, Serializable {

    static final int DEFAULT_INITIAL_CAPACITY=2;

    Component comp;
    Map links;

    public ComponentBehavior( Component comp ) {
	this.comp = comp;
	links = new HashMap( DEFAULT_INITIAL_CAPACITY );
    }

    public synchronized void addLinkToSrc( Component src, Object ref ) {
	Link l = new Link( src, comp );
	addLink( l, ref );
    }

    public synchronized void addLinkToSink( Component sink, Object ref ) {
	Link l = new Link( comp, sink );
	addLink( l, ref );
    }

    protected synchronized void addLink( Link l, Object ref ) {
	Link storedLink = null;
	if( links.containsKey( l )) {
	    storedLink = (Link)links.get( l );
	}
	else {
	    storedLink = l;
	    links.put( l, l );
	}

	storedLink.addValue( 1.0, ref );
    }

    // TODO: define equals, etc.

    public boolean matchesId( Map id ) {
	return comp.matchesId( id );
    }

    public Map getId() {
	return comp.getId();
    }

    public String toString() {
	return "{ComponentBehavior: component=" + comp.toString() + 
	    ", links=" + links.toString() + "}";
    }

}

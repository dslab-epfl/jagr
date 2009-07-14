package roc.pinpoint.analysis.structure;

import java.io.Serializable;
import java.util.*;

public class GrossComponentBehavior 
    implements Identifiable, Deviants, Serializable {

    public static final int INITIAL_CAPACITY = 2;

    Map id;
    
    Collection componentBehaviors;
    // todo: keep some mapping from logical links back to the links of
    //       each componentBehaviors
    Map linkStatistics;

    Map physicalLinkToLogicalLink;

    Set logicalComponentAttrs;

    double acceptableDeviation = 2.0;

    public GrossComponentBehavior( Set logicalComponentAttrs ) {
	componentBehaviors = new LinkedList();
	linkStatistics = new HashMap( INITIAL_CAPACITY );
	this.logicalComponentAttrs = logicalComponentAttrs;
	physicalLinkToLogicalLink = new HashMap( INITIAL_CAPACITY );
    }

    public void setLogicalComponentAttrs( Set attrs ) {
	logicalComponentAttrs = attrs;
    }

    public synchronized void setAcceptableDeviation( double dev ) {
	this.acceptableDeviation = dev;
	Iterator iter = linkStatistics.values().iterator();
	while( iter.hasNext() ) {
	    ((Deviants)iter.next()).setAcceptableDeviation( acceptableDeviation );
	}
    }

    public boolean matchesId( Map id ) {
	if( this.id == null )
	    return false;

	return this.id.equals( id );
    }

    public Map getId() {
	return id;
    }

    public void addComponentBehavior( ComponentBehavior b ) {
	if( id == null ) {
	    id = Component.ReduceMap( b.getId(), logicalComponentAttrs );
	}
	else {
	    // TODO: double-check that new component matches existing logical id
	}

	synchronized( b ) {
	    componentBehaviors.add( b );
	    
	    Iterator iter = b.links.values().iterator();
	    while( iter.hasNext() ) {
		Link l = (Link)iter.next();
	    
		Link logicallink = createLogicalLink( l );

		GrossStatistics s = (GrossStatistics)
		    linkStatistics.get( logicallink );
		if( s == null ) {
		    synchronized( this ) {
			s = new GrossStatistics();
			s.setAcceptableDeviation( acceptableDeviation );
		    }
		    linkStatistics.put( logicallink, s );
		}
		
		s.addStatistics( logicallink, b );
	    }
	}
    }

    Link createLogicalLink( Link l ) {

	if( physicalLinkToLogicalLink.containsKey( l ))
	    return (Link)physicalLinkToLogicalLink.get( l );

	Component src = Component.ReduceComponent( l.getSource(),
						   logicalComponentAttrs );
	Component sink = Component.ReduceComponent( l.getSink(),
						    logicalComponentAttrs );
	Link ret = new Link( src, sink, l );
	physicalLinkToLogicalLink.put( l, ret );
	
	return ret;
    }

    public SortedSet getDeviants() {
	SortedSet ret = new TreeSet();

	Iterator cbIter = componentBehaviors.iterator();
checkingComponents:
	while( cbIter.hasNext() ) {
	    ComponentBehavior cb = (ComponentBehavior)cbIter.next();
	    if( isDeviant( cb )) 
		ret.add( new RankedObject( getDeviation( cb ), cb ));
	}

	return ret;
    }

    public boolean isDeviant( ComponentBehavior cb ) {
	// todo: we also want to check that we catch any *missing*
	// links in the componentBehavior instance.

	/*
	if( cb.getId().get( "name" ).equals( "TheInventory" )) {
	    System.err.println( "\n\n\n*****HEY LOOK HERE ******\n***********" );
	}
	else {
	    System.err.println( "--------" );
	}
	*/
	

	//	System.err.println( "Checking GCB.isDeviant( " + cb.getId() + " )" );

	Iterator linkIter = cb.links.values().iterator();
	while( linkIter.hasNext() ) {
	    Link cLink = (Link)linkIter.next();
	    Link logicalLink = createLogicalLink( cLink );

	    /*
	    System.err.println( "checking physical link: " + cLink );
	    System.err.println( "created logicallink: " + logicalLink );
	    */

	    GrossStatistics stats = (GrossStatistics)
		linkStatistics.get( logicalLink );
	    if( stats == null ) {
		System.err.println( "GCB: in GCB " + id + 
				    " could not find link matching " +
				    logicalLink );
		return true;
	    }
	    else if( stats.isDeviant( cLink ) ) {
		//System.err.println( "GCB: is DEVIANT!" );
		return true;
	    }
	}
	
	return false;
    }

    public double getDeviation( ComponentBehavior cb ) {
	Iterator linkIter = cb.links.values().iterator();
	double ret = 0;

	//System.err.println( "GCB: getDeviation()" );

	while( linkIter.hasNext() ) {
	    Link cLink = (Link)linkIter.next();
	    Link logicalLink = (Link)physicalLinkToLogicalLink.get(cLink);
		
	    GrossStatistics stats = (GrossStatistics)
		linkStatistics.get( logicalLink );
	    if( stats == null ) {
		ret = Math.max( ret, 100 );
	    }
	    else if( stats.isDeviant( cLink ) ) {
		ret = Math.max( ret, stats.getDeviation( cLink ) );
	    }
	}

	//System.err.println( "deviation = " + ret );

	return ret;
    }

    public double getDeviation( Object o ) throws ClassCastException {
	return getDeviation( (ComponentBehavior)o );
    }

    public boolean isDeviant( Object o ) {
	return isDeviant( (ComponentBehavior) o );
    }

    public String toString() {
	return "{GrossComponentBehavior: id=" + id.toString() + 
	    ", componentbehaviors=" + componentBehaviors.toString() + "}";
    }

}

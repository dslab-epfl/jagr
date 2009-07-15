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

import java.util.Map;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.io.Serializable;

import org.apache.log4j.Logger;

public class ComponentBehavior implements Identifiable, Serializable, Distanceable {

    static Logger log = Logger.getLogger( "ComponentBehavior" );

    static final int DEFAULT_INITIAL_CAPACITY=2;

    static final long serialVersionUID = -8146417926696582158L;


    Component comp;
    Map links;

    Map oldLinkCounts;

    double cachedLinkNormalizer;
    boolean cacheIsValid = false;

    public ComponentBehavior( Component comp ) {
	this.comp = comp;
	links = new HashMap( DEFAULT_INITIAL_CAPACITY );
	oldLinkCounts = new HashMap( DEFAULT_INITIAL_CAPACITY );
    }
    
    public Component getComponent() {
	return comp;
    }

    public synchronized void markBehaviorAsOld() {
	Iterator iter = links.keySet().iterator();
	while( iter.hasNext() ) {
	    Object k = iter.next();
	    Link l = (Link)links.get(k);
	    int m = (int) (l.getCount() / 2);
	    log.debug( "markBehaviorAsOld() \t marking " + m + " items from link " + l + " as old" );
	    oldLinkCounts.put( k, new Integer( m ));
	}
    }

    public synchronized void removeOld() {
	cacheIsValid = false;

	Iterator iter = oldLinkCounts.keySet().iterator();
	while( iter.hasNext() ) {
	    Object k = iter.next();
	    int m = ((Integer)oldLinkCounts.get(k)).intValue();
	    Link l = (Link)links.get(k);
	    l.removeFirstValues( m );
	    log.debug( "removeOld() \t removing " + m + " items from link " + l );
	}
	oldLinkCounts.clear();
    }

    public synchronized Set getLinks() {
        return new HashSet( links.values() );
    }
    
    public synchronized void addLinkToSrc( Component src, double weight, Object ref ) {
	Link l = new Link( src, comp );
	addLink( l, weight, ref );
    }

    public synchronized void addLinkToSink( Component sink, double weight, Object ref ) {
	Link l = new Link( comp, sink );
	addLink( l, weight, ref );
    }


    public synchronized void addLinkToSrc( Component src ) {
	Link l = new Link( src, comp );
	addLink( l );
    }

    public synchronized void addLinkToSink( Component sink ) {
	Link l = new Link( comp, sink );
	addLink( l );
    }

    public synchronized void addCanonicalLinkToSrc( Component src ) {
	Link l = CanonicalLink.get( src, comp );
	addLink( l );
    }

    public synchronized void addCanonicalLinkToSink( Component sink ) {
	Link l = CanonicalLink.get( comp, sink );
	addLink( l );
    }


    public synchronized double getLinkNormalizer() {
       
	if( !cacheIsValid ) {
	    double ret = 0;

	    //Statistics tempStat = new Statistics();
	    Iterator iter = links.keySet().iterator();
	    while( iter.hasNext() ) {
		Link l = (Link)iter.next();
		ret += l.getCount();
		//tempStat.addValue( l.getCount(), null );
	    }
	    cachedLinkNormalizer = ret; //tempStat.getMean();
	    cacheIsValid = true;
	}

	return cachedLinkNormalizer;
    }

    protected synchronized void addLink( Link l, double weight, Object ref ) {
	cacheIsValid = false;

	Link storedLink = null;
	if( links.containsKey( l )) {
	    storedLink = (Link)links.get( l );
	}
	else {
	    storedLink = l;
	    links.put( l, l );
	}

	storedLink.addValue( weight, ref );
    }

    protected synchronized void addLink( Link l ) {
        cacheIsValid = false;

        Link storedLink = null;
        if( links.containsKey( l )) {
            storedLink = (Link)links.get( l );
            storedLink.addLink( l );
        }
        else {
            storedLink = l;
            links.put( l, l );
        }
    }
    
    
    public synchronized void addComponentBehavior(ComponentBehavior cb ) {
        Iterator iter = cb.links.values().iterator();
        while (iter.hasNext()) {
            Link link = (Link) iter.next();
            
        }
    }
    
    
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


    public double getDistance( Distanceable d ) {
        try {
            return getDistance( Distanceable.DEFAULT_DISTANCE, d );
        }
        catch( UnsupportedDistanceMetricException bad ) {
            throw new RuntimeException( bad );
        }

    }

    private HashSet extractLinkedComponents() {

        HashSet ret = new HashSet(links.size());

        Iterator iter = links.keySet().iterator();
        while( iter.hasNext() ) {
            Link l = (Link)iter.next();
            if( comp.equals( l.src ))
                ret.add( l.sink );
            else if( comp.equals( l.sink ))
                ret.add( l.src );
            else
                throw new RuntimeException( "BADNESS IN EXTRACTLINKED COMPONENTS!!!!" );
        }

        return ret;
    }

    public double getDistance( int distancemetric, Distanceable d ) 
        throws UnsupportedDistanceMetricException {

        ComponentBehavior other = (ComponentBehavior)d;

        /*a*/
        HashSet myNeighbors = extractLinkedComponents();
        
        /*b*/
        HashSet otherNeighbors = other.extractLinkedComponents();
            
        int a_and_b=0;
        int a_not_b=0;

        Iterator iter = myNeighbors.iterator();
        while( iter.hasNext( )) {
            Component n = (Component)iter.next();
            if( otherNeighbors.contains( n )) {
                a_and_b++;
            }
            else {
                a_not_b++;
            }
        }
        
        int b_not_a = otherNeighbors.size() - a_and_b;

        return DistanceableHelper.calculateDistance( distancemetric,
                                                     Distanceable.JACCARD,
                                                     a_and_b,
                                                     a_not_b,
                                                     b_not_a,
                                                     0 );
    }

}

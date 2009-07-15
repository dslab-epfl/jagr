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
import java.io.Serializable;
import roc.pinpoint.tracing.Observation;

/**
 * The Path class represents the ordered tree of component calls
 * made by a request as it flows through a system.  This class records
 * the requestid, the components associated with the Path, and the links
 * between those components, errors reported during the path, and any
 * request classifiers reported by the middleware during the tracing
 * process.
 *
 */
public class Path implements Identifiable, Distanceable, Serializable {

    private static final int INITIAL_CAPACITY = 2;

    String requestid;

    // the core path structure is in this PathNode
    PathNode root;

    // any associations with other paths (e.g., via JMS messages)
    Set pathAssociations;
    
    // ... and we also keep a quick set of components used in the path
    Set components;

    // ... and a quick (unordered) list of links too
    Set links;

    // ... and a list of errors!
    Set errors;

    // ... and the path's requesttypes
    Set requestTypes;    

    public Path() {
	requestid = null;
	components = new HashSet( INITIAL_CAPACITY );
	links = new HashSet( INITIAL_CAPACITY );
	errors = new HashSet( INITIAL_CAPACITY );
	requestTypes = new HashSet( INITIAL_CAPACITY );
	pathAssociations = new HashSet( INITIAL_CAPACITY );
	root = null;
    }


    // not the same as the Path's Identifiable.  
    //  requestid is the uniqueid used during tracing.
    public void setRequestId( String reqid ) {
	this.requestid = reqid;
    }

    public String getRequestId() {
	return requestid;
    }

    public void addPathAssociation( String requestid ) {
	pathAssociations.add( requestid );
    }

    public Set getPathAssociations() {
	return pathAssociations;
    }

    public boolean matchesId( Map attrs ) {
	return requestTypes.equals( attrs.get( "requestTypes" ));
    }

    public Map getId() {
	return Collections.singletonMap( "requestTypes", requestTypes );
    }

    public void addError( Observation err ) {
	errors.add( err );
    }

    public boolean hasErrors() {
	return errors.size() > 0;
    }

    public Set getErrors() {
	return errors;
    }

    public void addRequestType( String reqType ) {
	requestTypes.add( reqType );
    }

    public boolean isRequestType( String reqType ) {
	return requestTypes.contains( reqType );
    }

    public Set getRequestTypes() {
	return requestTypes;
    }

    public PathNode createRoot( Component comp ) {
	// assert that ( root == null )
	Map rt = comp.getId();
	if( rt != null )
	    addRequestType( rt.toString() );
	root = new PathNode( comp );
	return root;
    }
    
    public PathNode getRootNode() {
	return root;
    }

    Map cacheModifiedJaccardDistances = null;

    public double getDistance( Distanceable d ) {
	try {
	    return getDistance( MODIFIED_JACCARD, d );
	}
	catch( UnsupportedDistanceMetricException e ) {
	    throw new RuntimeException( "ERROR: default distance metric is unknown?!", e );
	}
    }

    public double getDistance( int metric, Distanceable d ) 
	throws UnsupportedDistanceMetricException {
	if( metric == MODIFIED_JACCARD ) {
	    return getModifiedJaccardDistance( d );
	}
	else {
	    throw new UnsupportedDistanceMetricException( metric );
	}
    }

    /** TODO
    public double getEqualityDistance( Distanceable d ) {

	if(!( d instanceof Path ))
	    throw new ClassCastException( "can't measure distance between a Path and a non-Path object: " + d.getClass().toString() );


	// TODO iteratively check the nodes in the path and throw an exception if *anything* is different

    }
    **/

    public double getModifiedJaccardDistance( Distanceable d ) {
	if(!( d instanceof Path ))
	    throw new ClassCastException( "can't measure distance between a Path and a non-Path object: " + d.getClass().toString() );

	// right now, we'll just say a path is similar if it uses the same
	//  components and has the same links.  later, we should also
	//  take into account the order of calls, etc.
	// 
	// distance = ((union of components) - (intersection of components))
	//            + ((union of links) - (intersection of links))
	// then we return the normalized distance (divide by total num components and links)

	Path other = (Path)d;

	if( cacheModifiedJaccardDistances == null )
	    cacheModifiedJaccardDistances = new HashMap();

	Double retCache = (Double)cacheModifiedJaccardDistances.get( other );

	if( retCache == null ) {
	    Set unionComponents = new HashSet( components );
	    unionComponents.addAll( other.components );
	    Set intersectionComponents = new HashSet( components );
	    intersectionComponents.retainAll( other.components );

	    Set unionLinks = new HashSet( links );
	    unionLinks.addAll( other.links );
	    Set intersectionLinks = new HashSet( links );
	    intersectionLinks.retainAll( other.links );
	    
	    double ret =
		((double)(unionComponents.size() - (double)intersectionComponents.size())
		 + ((double)unionLinks.size() - (double)intersectionLinks.size() )) 
		/ ((double)unionComponents.size() + (double)unionLinks.size());
	    
	    retCache = new Double( ret );
	    cacheModifiedJaccardDistances.put( other, retCache );
	}

	return retCache.doubleValue();
    }


    public static Path CreateLogicalPath( Path path, Set definingAttrs ) {
	Path ret = new Path();

	CreateLogicalPathHelper( path.getRootNode(), 
				 null, ret, 
				 definingAttrs );

	return ret;
    }

    private static void CreateLogicalPathHelper( PathNode srcNode, 
						 PathNode destParent,
						 Path destPath,
						 Set definingAttrs ) {
	Component logicalComponent = 
	    Component.ReduceComponent( srcNode.getComponent(), definingAttrs );

	PathNode destNode = null;

	if( destParent == null ) {
	    destNode = destPath.createRoot( logicalComponent );
	}
	else {
	    destNode = destParent.addCallee( logicalComponent );
	}

	// iterate over srcNode's children
	Iterator iter = srcNode.getCallees().iterator();
	while( iter.hasNext() ) {
	    PathNode srcChild = (PathNode)iter.next();
	    CreateLogicalPathHelper( srcChild, destNode, destPath, 
				     definingAttrs );
	}	
    }

    public Set getLinks() {
	return links;
    }

    public Set getComponents() {
	return components;
    }

    // package methods, to be used by PathNode internals

    void addComponent( Component c ) {
	components.add( c );
    }

    void addLink( Link l ) {
	links.add( l );
    }

    public String toString() {
	return "{Path: requesttypes=" + requestTypes + "}\n" +
	    ((root==null)?"ROOT IS NULL" : root.toString( "  " ));
    }

    public class PathNode implements Serializable {
	Component component;

	List callees;  // ordered list of calls from this node
	PathNode caller; // caller

        PathNode( Component comp ) {
	    this( comp, null );
	}

	PathNode( Component component, PathNode caller ) {
	    this.component = component;
	    callees = new LinkedList();
	    this.caller = caller;
	    addComponent( component );
	}

	public PathNode addCallee( Component calledComp ) {
	    PathNode ret = new PathNode( calledComp, this );  
	    callees.add( ret );
	    
	    addLink( new Link( component, calledComp ));

	    return ret;
	}

	public List getCallees() {
	    return callees;
	}

	public PathNode getCaller() {
	    return caller;
	}
	
	public Component getComponent() {
	    return component;
	}


	public String toString( String tab ) {
	    StringBuffer ret = new StringBuffer();

	    ret.append( tab ).append( "CALL" ).append( component.getId() ).append( "\n" );
	    Iterator iter = callees.iterator();
	    String newtab = tab + "   ";
	    while( iter.hasNext() ) {
		PathNode pn = (PathNode)iter.next();
		ret.append( pn.toString( newtab ) );
	    }

	    ret.append( tab ).append( "RETURN" ).append( "\n" );

	    return ret.toString();
	}
    }

}

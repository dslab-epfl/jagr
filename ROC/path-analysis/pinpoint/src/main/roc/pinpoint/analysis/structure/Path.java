package roc.pinpoint.analysis.structure;

import java.util.*;
import java.io.Serializable;
import roc.pinpoint.tracing.Observation;

public class Path implements Distanceable, Serializable {

    private static final int INITIAL_CAPACITY = 2;

    // the core path structure is in this PathNode
    PathNode root;
    
    // ... and we also keep a quick set of components used in the path
    Set components;

    // ... and a quick (unordered) list of links too
    Set links;

    // ... and a list of errors!
    Set errors;

    // ... and the path's requesttypes
    Set requestTypes;
    

    public Path() {
	components = new HashSet( INITIAL_CAPACITY );
	links = new HashSet( INITIAL_CAPACITY );
	errors = new HashSet( INITIAL_CAPACITY );
	requestTypes = new HashSet( INITIAL_CAPACITY );
	root = null;
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
	root = new PathNode( comp );
	return root;
    }
    
    public PathNode getRootNode() {
	return root;
    }

    Map cacheDistances = null;

    public double getDistance( Distanceable d ) {
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

	if( cacheDistances == null )
	    cacheDistances = new HashMap();

	Double retCache = (Double)cacheDistances.get( other );

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
	    cacheDistances.put( other, retCache );
	}

	return retCache.doubleValue();
    }


    /*
    public boolean matchesId( Map attrs ) {
	// TODO: pull out "requesttypes" attributes, see if they match...
	return false;
    }

    public Map getId() {
	// TODO: pull out "requesttypes" attributes as Id ?? 
	return null;
    }
    */

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
	return "{Path: requesttypes=" + requestTypes + "}";
    }

    public class PathNode {
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
    }

}

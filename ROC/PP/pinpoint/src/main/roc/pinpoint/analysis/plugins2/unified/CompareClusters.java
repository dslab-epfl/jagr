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
package roc.pinpoint.analysis.plugins2.unified;

import java.util.*;
import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.clustering.*;
import roc.pinpoint.analysis.structure.LockedComponentBehavior;

public class CompareClusters implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String REF_COLLECTION_NAME_ARG = "refCollection";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.", 
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( REF_COLLECTION_NAME_ARG,
		       "reference collection.",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null )

    };
    
    RecordCollection inputCollection;
    RecordCollection outputCollection;
    RecordCollection refCollection;

    Set testClusters;
    Set refClusters;
    Map refComponentToCluster;

    public PluginArg[] getPluginArguments() {
        return args;
    }

    public void start( String id, Map args, AnalysisEngine engine ) 
        throws PluginException {

        inputCollection = (RecordCollection)
            args.get( INPUT_COLLECTION_NAME_ARG );
        outputCollection = (RecordCollection)
            args.get( OUTPUT_COLLECTION_NAME_ARG );
        refCollection = (RecordCollection)
            args.get( REF_COLLECTION_NAME_ARG );

        inputCollection.registerListener( this );
        refCollection.registerListener( this );
    }

    public void stop() {
        inputCollection.unregisterListener( this );
    }

    public void addedRecord( String collectionName, Record rec ) {

        if( collectionName.equals( refCollection.getName() )) {
            System.err.println( "loaded refclusters" );
            refClusters = (Set)rec.getValue();
        }
        else {
            System.err.println( "loaded testclusters" );
            testClusters = (Set)rec.getValue();
        }
        
        if( testClusters != null &&
            refClusters != null ) {
            System.err.println( "comparing test and ref clusters" );
            resetSummaries( testClusters );
            resetSummaries( refClusters );
	    Set errors = checkDifference( testClusters, refClusters );
	    
	    outputCollection.setRecord( "errors",
					new Record( errors ));
        }
        else {
            System.err.println( "testclusters and refclusters are not ready yet" );
        }
    }

    private Map makeRefMap( Set clusters ) {
	HashMap ret = new HashMap();
	Iterator iter = clusters.iterator();
	while( iter.hasNext() ) {
	    Cluster c = (Cluster)iter.next();
	    List l = c.getAllElements();
	    Iterator lcbIter = l.iterator();
	    while( lcbIter.hasNext() ) {
		LockedComponentBehavior lcb = (LockedComponentBehavior)lcbIter.next();
		//ret.put( lcb.getComponentName(), c );
		ret.put( lcb.getComponentName(), lcb );
	    }
	}
	return ret;
    }

    private void resetSummaries( Set clusters ) {
        Iterator iter = clusters.iterator();
        while( iter.hasNext() ) {
            ((Cluster)iter.next()).resetSummaryComponentBehavior();
        }
    }

    private Set checkDifference( Set testClusters,
				  Set refClusters ) {

	Map refMapNameToCluster = makeRefMap( refClusters );
	Map testMapNameToCluster = makeRefMap( testClusters );
	

	Set unusedRefClusters = new HashSet( refClusters );

	Set ret = new TreeSet();

        Iterator iter = testClusters.iterator();
        while( iter.hasNext() ) {
            Cluster c = (Cluster)iter.next();

            Cluster bestRef = findClosestRef( c, refClusters );

	    if( bestRef == null )
		continue;

            unusedRefClusters.remove( bestRef );
            Set errs = compareClusterToRef( bestRef, c,
					    refMapNameToCluster,
					    testMapNameToCluster );
	    ret.addAll( errs );
        }

	System.err.println( "TOTAL: " + unusedRefClusters.size() + 
			    " unreferenced Clusters" );

        // TODO: report unusedRefClusters as a potential problem

	return ret;
    }

    private Cluster findClosestRef( Cluster c, Set refClusters ) {
        double minDistance = Double.MAX_VALUE;
        Cluster ret = null;

        Iterator iter = refClusters.iterator();
        while( iter.hasNext() ) {
            Cluster ref = (Cluster)iter.next();
            double distance = ref.getDistance( c );
            if( distance < minDistance ) {
                minDistance = distance;
                ret = ref;
            }
        }

	if( minDistance > 0.2 ) {
	    ret = null;
	}

        return ret;
    }

    
    public void compareSets( Set aSet, Set bSet,
			     Set onlyA, Set both, Set onlyB ) {
	HashMap aMap = new HashMap( aSet.size() );
	HashMap bMap = new HashMap( bSet.size() );

	HashSet aNames = new HashSet( aSet.size() );
	HashSet bNames = new HashSet( bSet.size() );

	compareSetHelper1( aSet, aMap, aNames );
	compareSetHelper1( bSet, bMap, bNames );

	HashSet tmp = new HashSet( aNames );

	aNames.removeAll( bNames );
	bNames.removeAll( tmp );
	tmp.removeAll( aNames );

	compareSetHelper2( aNames, aMap, onlyA );
	compareSetHelper2( tmp, aMap, both );
	compareSetHelper2( bNames, bMap, onlyB );
    }


    /**
     *  iterate over the LockedComponentBehavior's in 'set' and
     *  pull out their component names into 'names', and add a
     *  mapping from name to LCB in 'map'
     */
    private void compareSetHelper1( Set lcbSet, Map map, Set names ) {
	Iterator iter = lcbSet.iterator();
	while( iter.hasNext() ) {
	    LockedComponentBehavior lcb = (LockedComponentBehavior)
		iter.next();
	    names.add( lcb.getComponentName() );
	    map.put( lcb.getComponentName(), lcb );
	}
    }

    /**
     *  iterate over the component names in 'names' and, given
     *  the mapping in 'map', add the associated LockedComponentBehaviors
     *  to 'set'
     */
    private void compareSetHelper2( Set names, Map map, Set out ) {
	Iterator iter = names.iterator();
	while( iter.hasNext() ) {
	    String n = (String)iter.next();
	    out.add( map.get( n ));
	}
    }



    public Set compareClusterToRef( Cluster ref, Cluster test, 
				     Map refMapNameToCluster,
				     Map testMapNameToCluster ) {

        HashSet testElements = new HashSet( test.getAllElements() );
        HashSet refElements = new HashSet( ref.getAllElements() );

	HashSet onlyTest = new HashSet();
	HashSet both = new HashSet();
	HashSet onlyRef = new HashSet();

	compareSets( testElements, refElements,
		     onlyTest, both, onlyRef );

	HashSet errorExtra = filterErrors( "extra element", 
					   onlyTest, refMapNameToCluster );
	HashSet errorMissing = filterErrors( "missing element", 
					     onlyRef, testMapNameToCluster );

	Set ret = new HashSet( errorExtra.size() + errorMissing.size() );
	ret.addAll( errorExtra );
	ret.addAll( errorMissing );
	    
	/*

        System.err.println( "Comparing TestCluster#" + 
                            test.getID() + "[w=" + test.getSize() + "]" +
			    " to RefCluster#" +
                            ref.getID() + "[w=" + ref.getSize() + "]" +
			    " DISTANCE=" + ref.getDistance( test ) );

        printSet( "TestCluster: extra el: ", onlyTest );
	printSet( "TestCluster: missing el: ", onlyRef );

	*/

	/* TODO: compare structure of clusters too... */

	return ret;
    }

    private HashSet filterErrors( String msg, Set errors, Map refMap ) {
	HashSet ret = new HashSet( errors.size() );

	Iterator iter = errors.iterator();
	while( iter.hasNext() ) {
	    LockedComponentBehavior lcbError = (LockedComponentBehavior)iter.next();
	    String compname = lcbError.getComponentName();
	    /*
	    Cluster ref = (Cluster)refMap.get( compname );
	    if( ref == null ) {
		System.err.println( "found no matching cluster for compname '" + compname + "'" );
		continue;
	    }
	    LockedComponentBehavior lcbRef = ref.getSummaryComponentBehavior();
	    */

	    LockedComponentBehavior lcbRef = (LockedComponentBehavior)refMap.get( compname );
	    if( lcbRef == null ) {
		//System.err.println( "found no matching LCBRef for compname '" + compname + "'" );
		continue;
	    }

	    double dist = lcbRef.getDistance( lcbError );
	    double weight = lcbRef.getWeight();
	    if( dist < 0.1 ) {
		//System.err.println( "suppressing error: " + compname + "[" + dist + "," + weight + "]" );
	    }
	    else if( weight < 500 ) {
		//System.err.println( "suppressing error: " + compname + "[" + dist + "," + weight + "]" );
	    }
	    else {
		System.err.println( "significant error: " + compname + "[" + dist + "," + weight + "]" );
		lcbRef.getDistanceVerbose( lcbError );

		ret.add( new ErrorInfo( dist * weight, 
					(msg + ": " + 
					 lcbError.getComponentName() + 
					 "; dist=" + dist + 
					 "; weight=" + weight),
					lcbError, lcbRef ));
	    }
	}
	    
	return ret;
    }


    private void printSet( String msg, Set el ) {
        Iterator iter = el.iterator();
        while( iter.hasNext() ) {
            System.err.println( msg + ": " + 
				((LockedComponentBehavior)iter.next()).toShortString() );
        }
    }

    public void removedRecords( String collectionName, List items ) {
        // do nothing
    }

    class ErrorInfo implements Comparable {

	double rank;
	String msg;

	LockedComponentBehavior testLCB;
	LockedComponentBehavior refLCB;

	public ErrorInfo( double rank,
			  String  msg,
			  LockedComponentBehavior testLCB,
			  LockedComponentBehavior refLCB ) {
	    this.rank = rank;
	    this.msg = msg;
	    this.testLCB = testLCB;
	    this.refLCB = refLCB;
	}

	public int compareTo( Object o ) {
	    ErrorInfo other = (ErrorInfo)o;

	    if( rank < other.rank ) {
		return -1;
	    } else if ( rank > other.rank ) {
		return 1;
	    }
	    else {
		return 0;
	    }
	}

	public String toString() {
	    return "ErrorInfo: " + 
		"[rank= " + rank +
		", msg='" + msg + "'" +
		", test={" + testLCB + "}" +
		", ref={" + refLCB + "}]\n";
	}

    }

}

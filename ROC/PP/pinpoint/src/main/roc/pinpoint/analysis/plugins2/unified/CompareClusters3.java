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
import roc.pinpoint.analysis.structure.*;

public class CompareClusters3 implements Plugin, RecordCollectionListener {

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

	    printClusters( refClusters );

	    Map translationTable = buildTranslationTable( refClusters );

	    resetSummaries( testClusters );
	    resetSummaries( refClusters );
	    fillInBlankTranslations( translationTable, 
				     testClusters, refClusters );

	    Set newTestClusters = translateClusters( testClusters, 
						     translationTable, false );
	    Set newRefClusters = translateClusters( refClusters, 
						    translationTable, false );

	    Set errors = checkDifference( newTestClusters, newRefClusters,
					  translationTable );
	    
	    outputCollection.setRecord( "errors",
					new Record( errors ));
        }
        else {
            System.err.println( "testclusters and refclusters are not ready yet" );
        }
    }


    private void printClusters( Set clusters ) {
	Iterator iter = clusters.iterator();
	while( iter.hasNext() ) {
	    Cluster c = (Cluster)iter.next();
	    System.err.println( "Ref Cluster: " + c.toShortString() );
	}
    }

    private void resetSummaries( Set clusters ) {
        Iterator iter = clusters.iterator();
        while( iter.hasNext() ) {
            ((Cluster)iter.next()).resetSummaryComponentBehavior();
        }
    }


    private Map buildTranslationTable( Set clusters ) {
	// iterate through all the Components within a cluster, and build a mapping from
	// component name to the cluster.

	Map ret = new HashMap();

	Iterator iter = clusters.iterator();
	while( iter.hasNext() ) {
	    Cluster c = (Cluster)iter.next();

	    String cname = "Cluster#" + c.getID();
	    
	    List allel = c.getAllElements();
	    Iterator iter2 = allel.iterator();
	    while( iter2.hasNext() ) {
		LockedComponentBehavior lcb = (LockedComponentBehavior)iter2.next();
		ret.put( lcb.getComponentName(), cname );
	    }
	}

	return ret;
    }


    /**
    private void fillInBlankTranslations( Map translationTable, Set clusters ) {
	// iterate through components in clusters, and find any components that
	// are not in the translation table, and add dummy entries for each...

	Iterator iter = clusters.iterator();
	while( iter.hasNext() ) {
	    Cluster c = (Cluster)iter.next();
	    List allel = c.getAllElements();
	    Iterator iter2 = allel.iterator();
	    while( iter2.hasNext() ) {
		LockedComponentBehavior lcb = (LockedComponentBehavior)iter2.next();
		if( !translationTable.containsKey( lcb.getComponentName() )) {
		   translationTable.put( lcb.getComponentName(), lcb.getComponentName() );
		}
	    }
	}
    }
    */

    private void fillInBlankTranslations( Map translationTable, 
					  Set testClusters, Set refClusters ) {
	
	List blankTranslations = new LinkedList();

	// 1. iterate over testClusters and pull out components that are *not* in translationTable
	Iterator iter = testClusters.iterator();
	while( iter.hasNext() ) {
	    Cluster c = (Cluster)iter.next();
	    List allel = c.getAllElements();
	    Iterator iter2 = allel.iterator();
	    while( iter2.hasNext() ) {
		LockedComponentBehavior lcb = (LockedComponentBehavior)iter2.next();
		if( !translationTable.containsKey( lcb.getComponentName() )) {
		    blankTranslations.add( lcb );
		}
	    }
	}
	iter = null;


	blankTranslations = translateComponents( blankTranslations, translationTable );
	testClusters = translateClusters( testClusters, translationTable, true );

	// 5. we're done when all translations are in...
	while( blankTranslations.size() > 0 ) {

	    System.err.println( "numblanktranslations to fill in = " + blankTranslations.size() );
	    
	    LockedComponentBehavior bestLCB = null;
	    Cluster.ClusterDistance bestClusterDistance = new Cluster.ClusterDistance( null, null, Double.MAX_VALUE );

	    // 2. find all the bestdistances from remaining components and refclusters.
	    iter = blankTranslations.iterator();
	    int numlooped = 0;
	    while( iter.hasNext() ) {
		LockedComponentBehavior lcb = (LockedComponentBehavior)iter.next();
		Cluster.ClusterDistance currDistance = findClosestRef( lcb, refClusters );
		//System.err.println( "comparing currDist = " + currDistance.distance + " to bestDist = " + bestClusterDistance.distance );
		if( currDistance.distance < bestClusterDistance.distance ) {
		    bestClusterDistance = currDistance;
		    bestLCB = lcb;
		}
		numlooped++;
	    }
	    iter = null;

	    if( bestLCB == null ) {
		throw new RuntimeException( "bestLCB == null : numlooped=" + numlooped + "; blankTransSize = " + blankTranslations.size() );
	    }

	    // 3. assign a translation for the min distanced component.  
	    String oldname = bestLCB.getComponentName();
	    String newname = "Cluster#" + bestClusterDistance.left.getID();

	    blankTranslations.remove( bestLCB );

	    translationTable.put( oldname, newname );

	    // 4. translate all the references to this component, and go back to step 2.
	    blankTranslations = translateComponents( blankTranslations,
						     oldname, newname );
	}

    
    }

    private List translateComponents( List components, Map translationTable ) {
	List ret = new LinkedList();

	Iterator iter = components.iterator();
	while( iter.hasNext() ) {
	    LockedComponentBehavior lcb =(LockedComponentBehavior)iter.next();

	    WeightedSimpleComponentBehavior scb = new WeightedSimpleComponentBehavior( lcb.getComponentName() );
	    lcb.appendTo( scb, translationTable, true ); // TODO: pass flag to tell appendTo to ignore missing translations
	    ret.add( scb.lockValues() );
	}

	return ret;
    }

    private List translateComponents( List components, 
				      String oldname,
				      String newname ) {
	Map translationTable = Collections.singletonMap( oldname, newname );
	return translateComponents( components, translationTable );
    }


    private Set translateClusters( Set clusters,
				    String oldname,
				    String newname ) {
	Map translationTable = Collections.singletonMap( oldname, newname );
	return translateClusters( clusters, translationTable, true );
    }
				      
				      

    private Cluster.ClusterDistance findClosestRef( LockedComponentBehavior lcb, Set refClusters ) {
	double minDistance = Double.MAX_VALUE;
	Cluster.ClusterDistance ret = null;

	Cluster c = new Cluster( lcb );

	Iterator iter = refClusters.iterator();
	while( iter.hasNext() ) {
	    Cluster ref = (Cluster)iter.next();
	    double distance = ref.getDistance( c );
	    if( distance < minDistance ) {
		minDistance = distance;
		ret = new Cluster.ClusterDistance( ref, c, distance );
	    }
	}

	if( ret == null ) {
	    throw new RuntimeException( "Couldn't find closest ref: LCB=" + lcb + "; refClusters.size()=" + refClusters.size() );
	}

	return ret;
    }


    private Set translateClusters( Set clusters, Map translationTable,
				   boolean useDefaultMapping ) {
	Set ret = new HashSet( clusters.size() );

	Iterator iter = clusters.iterator();
	while( iter.hasNext() ) {
	    Cluster c =(Cluster)iter.next();

	    WeightedSimpleComponentBehavior scb = new WeightedSimpleComponentBehavior( "TestCluster#"+c.getID() );
	    LockedComponentBehavior lcb = c.getSummaryComponentBehavior();
	    lcb.appendTo( scb, translationTable, useDefaultMapping );
	    scb.addElements( c.getAllElements() );
	    ret.add( new Cluster( scb.lockValues(), scb.getElements() ));
	}

	return ret;
    }


    private Set checkDifference( Set testClusters,
				 Set refClusters,
				 Map translationTable ) {

	Map refMapNameToCluster = makeRefMap( refClusters );
	Map testMapNameToCluster = makeRefMap( testClusters );

	Set ret = new TreeSet();

	Set unusedRefClusters = new HashSet( refClusters );

	int count=0;

        Iterator iter = testClusters.iterator();
        while( iter.hasNext() ) {
            Cluster c = (Cluster)iter.next();

            Cluster bestRef = findClosestRef( c, refClusters );

	    if( bestRef == null ) {
		System.err.println( "[" + (count++) + 
				    "] Cluster had no best ref!" + c.toShortString() );
		continue;
	    }

            unusedRefClusters.remove( bestRef );
            Set errs = compareClusterToRef( bestRef, c,
					    refMapNameToCluster,
					    testMapNameToCluster,
					    translationTable );
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
	    System.err.println( "-----------------------------------------------------------------" );
	    System.err.println( "closest distance was " + minDistance + 
				" to " + ret );
	    ret = null;
	}

        return ret;
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
				    Map testMapNameToCluster,
				    Map translationTable  ) {

        HashSet testElements = new HashSet( test.getAllElements() );
        HashSet refElements = new HashSet( ref.getAllElements() );

	HashSet onlyTest = new HashSet();
	HashSet both = new HashSet();
	HashSet onlyRef = new HashSet();

	compareSets( testElements, refElements,
		     onlyTest, both, onlyRef );

	HashSet errorExtra = filterErrors( "extra element", 
					   onlyTest, refMapNameToCluster,
					   translationTable );
	HashSet errorMissing = filterErrors( "missing element", 
					     onlyRef, testMapNameToCluster,
					     translationTable );

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

    private HashSet filterErrors( String msg, Set errors, Map refMap, 
				  Map translationTable ) {
	HashSet ret = new HashSet( errors.size() );

	Iterator iter = errors.iterator();
	while( iter.hasNext() ) {
	    LockedComponentBehavior lcbError = (LockedComponentBehavior)iter.next();
	    String compname = lcbError.getComponentName();

	    System.err.println( "[filterError] ---- analyzing " + compname );

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

	    LockedComponentBehavior canonicalRef = lcbRef.canonicalize( translationTable );
	    LockedComponentBehavior canonicalError = lcbError.canonicalize( translationTable );

	    double dist = canonicalRef.getDistance( canonicalError );
	    double weight = lcbRef.getWeight();
	    if( dist < 0.05 ) {
		System.err.println( "suppressing error: " + compname + "[" + dist + "," + weight + "]" );
	    }
	    else if( weight < 10 ) {
		System.err.println( "suppressing error: " + compname + "[" + dist + "," + weight + "]" );
	    }
	    else {
		System.err.println( "significant error: " + compname + "[" + dist + "," + weight + "," + (dist*weight) + "]" );
		canonicalRef.getDistanceVerbose( canonicalError );

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

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
package roc.pinpoint.analysis.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import swig.util.Debug;

import roc.pinpoint.analysis.structure.Distanceable;
import roc.pinpoint.analysis.structure.UnsupportedDistanceMetricException;
import roc.pinpoint.analysis.structure.Identifiable;

// hack
import roc.pinpoint.analysis.structure.LockedComponentBehavior;


/**
 * Data clustering class.  this class represents a cluster of elements,
 * and also implements some simple hierarchical data clustering algorithms.
 * @author emrek
 *
 */
public class Cluster
    implements Distanceable, Serializable, Comparable, Identifiable {

    static final long serialVersionUID = 5559889575925819548L;

    /**
     * Unweighted Pair Groups Method Average. Computes the
     * distance between two clusters as the unweighted average of all pairs of
     * elements among the two clusters.
     */
    public static final int UPGMA_CLUSTERMETHOD = 0;

    /**
     * Single Linkage Clustering.  Computes the distance between
     * two clusters as the nearest-neighbor distance, or the smallest
     * distance among all pairs of elements in the clusters.
     */
    public static final int SLINK_CLUSTERMETHOD = 1;

    /**
     * Complete Linkage Clustering.  Computes the distance between two clusters
     * as the furthest-neighbor distance, or the greatest distance among all
     * pairs of elements in the clusters.
     */
    public static final int CLINK_CLUSTERMETHOD = 2;

    /** 
     * the number of different cluster methods allowed.  Used for
     * asserting that (chosenClusterMethod < MAX_CLUSTERMETHOD)
     */
    public static final int MAX_CLUSTERMETHOD = 3;

    private double distance;

    private Cluster left;
    private Cluster right;

    private ClusterDistance bestClusterDistance;

    private ArrayList el;
    private ArrayList allel;
    private LockedComponentBehavior summary;

    static Object lockTotalClusters = new Object();
    static int numTotalClusters = 0;

    int id;

    public List getElements() {
        return el;
    }

    public List getAllElements() {
        return allel;
    }

    public void resetSummaryComponentBehavior() {
        summary = new LockedComponentBehavior( allel.toArray() );
    }

    public LockedComponentBehavior getSummaryComponentBehavior() {
        return summary;
    }

    private void initID() {
        synchronized( lockTotalClusters ) {
            id = numTotalClusters++;
        }
    }

    public int getID() {
        return id;
    }

    public boolean matchesId( Map attrs ) {
	return( attrs.equals( getId() ));
    }

    public Map getId() {
	return Collections.singletonMap( "id", new Integer( id ));
    }

    /**
     * creates a cluster containing only a single element
     * @param el a cluster element
     */
    public Cluster(LockedComponentBehavior el) {
        left = null;
        right = null;
        this.el = new ArrayList(1);
        this.el.add( el );
        this.allel = new ArrayList(1);
        this.allel.add( el );
        this.summary = el;
        initID();
    }

    /**
     * creates a cluster containing only a single element
     * @param el a cluster element
     */
    public Cluster(LockedComponentBehavior el, ArrayList allel) {
        left = null;
        right = null;
        this.el = new ArrayList(1);
        this.el.add( el );
        
        for( int i=0; i<allel.size(); i++ ) {
            Object e = allel.get(i);
            if( !( e instanceof LockedComponentBehavior )) {
                throw new RuntimeException( "el not a LCB! but a " + e.getClass() );
            }
        }

        this.allel = new ArrayList( allel.size() );
        this.allel.addAll( allel );
        this.summary = el;
        initID();
    }

    /**
     * creates a new cluster by merging two other clusters. 
     * @param l  first (left) cluster to merge
     * @param r  second (right) cluster to merge
     * @param distance  the distance between these two clusters
     */
    public Cluster(Cluster l, Cluster r, double distance) {
        left = l;
        right = r;
        this.distance = distance;
        this.el = new ArrayList(l.el.size() + r.el.size());
        this.el.addAll( l.el );
        this.el.addAll( r.el );

        this.allel = new ArrayList( l.allel.size() + r.allel.size() );
        this.allel.addAll( l.allel );
        this.allel.addAll( r.allel );

        this.summary = new LockedComponentBehavior( new Object[] {l.summary,r.summary} );

        initID();
    }

    /**
     * 
     * @return Cluster  the "left" child cluster 
     */
    public Cluster getLeft() {
        return left;
    }

    /**
     * 
     * @return Cluster the "right" child cluster
     */
    public Cluster getRight() {
        return right;
    }

    /**
     * 
     * @param distance the calculated distance between the two child clusters
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * 
     * @return double the distance between the two child clusters ('left' and
     * 'right')
     */
    public double getDistance() {
        return distance;
    }

    /**
     * 
     * @return int the number of basic elements in this cluster (including the
     * elements in the child clusters)
     */
    public double getSize() {
        return summary.getWeight();
    }

    class Counter {
        int counter = 0;
    }

    String toString(Counter counter) {

        String ret = "{ Cluster #" + id + " {\n\t ";
        ret += "bestClusterDistance=" + bestClusterDistance + "\n\t";
        ret += summary.toString();
        ret += "}\n";

        counter.counter++;
        ret += "}\n";
        

        return ret;
    }


    public String toShortString(){
        String ret = "{ Cluster #" + id + " {\n\t ";
        ret += summary.toShortString();
        ret += "}\n";
        ret += "}\n";
        return ret;
    }


    public void printString() {
        printString( new Counter() );
    }

    public void printString(Counter counter) {

        System.out.print( "{ Cluster #" + id + " { " );
        System.out.print( "\n\t bestdistance=" + bestClusterDistance );
        System.out.print( "\n\t" + summary.toString() );
        System.out.println( "\t}" );

        counter.counter++;

        if (left != null) {
            System.out.println( "\twas combined (dist = " + distance
                                + ") from "
                                + "Cluster #"
                                + "[TODO]"
                                + " and Cluster #"
                                + "[TODO]" );

            left.printString(counter);
            right.printString(counter);
        }
    }


/**
 * 
 * @see java.lang.Object#toString()
 */
    public String toString() {
        return toString(new Counter());
    }

/**
 * calculates the distance between this and another cluster using the
 * specified cluster method and the given distance coefficient.
 * @param c  cluster
 * @param clustermethod  cluster method to use
 * @param distancecoeff  distance coefficient to use.
 * @return double distance
 *
 * @deprecated
 */
    public double calculateDistance(
        Cluster c,
        int clustermethod,
        int distancecoeff ) 
	throws UnsupportedDistanceMetricException {

        return this.summary.getDistance( c.summary );
    }


    public double getDistance( Distanceable other ) {
	try {
	    return getDistance( -1, other );
	}
	catch( UnsupportedDistanceMetricException e ) {
	    // should never happen
	    throw new RuntimeException( e );
	}
    }


    /**
     * calculates the distance between this and another cluster using the
     * UPGMA cluster method
     * @param other cluster
     * @return double distance
     *
     */
    public double getDistance( int disttype, Distanceable other ) 
	throws UnsupportedDistanceMetricException {
	Cluster c = (Cluster)other;

        return this.summary.getDistance( disttype, c.summary );
    }


    /**
     * build a set of clusters, one cluster per element
     * @param el array of elements
     * @return List List of generated clusters.
     */
    public static HashSet BuildClusters( Collection el) {
        HashSet ret = new HashSet(el.size());

	Iterator iter = el.iterator();
	while( iter.hasNext() ) {
	    Object o = iter.next();
	    if( !(o instanceof Distanceable )) {
		System.err.println( "Cluster: cannot cast " + o.getClass().toString() + " to interface Distanceable" );
	    }
	    ret.add( new Cluster( (LockedComponentBehavior)iter.next() ));
	}

        return ret;
    }

/**
 * build a set of clusters, one per cluster element 
 * @param el list of cluster elements
 * @return List  list of generated clusters
 */
    public static HashSet BuildClusters(List el) {
        HashSet ret = new HashSet(el.size());

        Iterator iter = el.iterator();
        while (iter.hasNext()) {
            ret.add(new Cluster((LockedComponentBehavior) iter.next()));
        }

        return ret;
    }


    public static HashSet DoClustering( HashSet clusters, double stopdistance ) {
        SortedSet distances = new TreeSet();
        
        GenerateDistances( distances, clusters, stopdistance );
        
        System.out.println( "begin cluster: " + clusters.size() + 
                            " clusters" );

        int i=0;
        while( distances.size() > 0 ) {

            List newClusters = MergeClusters( distances, clusters,
                                                 stopdistance );

            GenerateDistances( distances, clusters, newClusters,
                               stopdistance );
        }

         System.out.println( "end cluster: " + clusters.size() + 
                             " clusters in the end;\n\t" +
                             numTotalClusters + " total clusters instantiated during mergin" );

        return clusters;
    }

    private static void GenerateDistances( SortedSet distances,
                                           HashSet initialClusters, double threshold ) {
        GenerateDistances( distances, initialClusters, null, threshold );
    }

    private static void GenerateDistances( SortedSet distances,
                                           HashSet allClusters, List newClusters,
                                           double threshold ) {
        ArrayList allList = new ArrayList( allClusters );
        List newList = null;
        boolean isSame = false;

        if( newClusters != null ) {
            newList = newClusters;
        }
        else {
            newList = new ArrayList( allClusters );
            isSame = true;
        }
    
        for( int i=0; i<newList.size(); i++ ) {
            Cluster currLeft = (Cluster)newList.get(i);
            if( currLeft.bestClusterDistance != null &&
                currLeft.bestClusterDistance.distance == 0 ) {
                continue;
            }

            for( int j= (isSame)?(i+1):0; j<allList.size(); j++ ) {
                Cluster currRight = (Cluster)allList.get(j);

                if( currLeft == currRight ) {
                    continue;
                }

                if( currRight.bestClusterDistance != null &&
                    currRight.bestClusterDistance.distance == 0 ) {
                    continue;
                }

                int retval = GenerateDistancesHelper( distances, newList,
                                                      currLeft, currRight, threshold );

                if( retval == 0 ) {
                    break;
                }
            }
        }
    }

    private static int GenerateDistancesHelper( SortedSet distances, List newClusters,
                                                Cluster currLeft, Cluster currRight, double threshold ) {
        double currdistance = currRight.getDistance( currLeft );

        if( currdistance > threshold ) {
            return 1;
        }

        double leftdistance = (currLeft.bestClusterDistance == null )
            ? (Double.MAX_VALUE):(currLeft.bestClusterDistance.distance);
        double rightdistance = (currRight.bestClusterDistance == null )
            ? (Double.MAX_VALUE):(currRight.bestClusterDistance.distance);

        if(( leftdistance > currdistance ) &&
           ( rightdistance > currdistance )) {
            ClusterDistance newDistance = new ClusterDistance();
            newDistance.left = currLeft;
            newDistance.right = currRight;
            newDistance.distance = currdistance;

            if( currRight.bestClusterDistance != null ) {
                Cluster orphan = currRight.bestClusterDistance.getOther( currRight );
                distances.remove( currRight.bestClusterDistance);
                orphan.bestClusterDistance = null;
                newClusters.add( orphan );
            }

            if( currLeft.bestClusterDistance != null ) {
                Cluster orphan = currLeft.bestClusterDistance.getOther( currLeft );
                distances.remove( currLeft.bestClusterDistance );
                orphan.bestClusterDistance = null;
                newClusters.add( orphan );
            }

            currLeft.bestClusterDistance = newDistance;
            currRight.bestClusterDistance = newDistance;
            distances.add( newDistance );
            if( newDistance.distance == 0.0 ) {
                return 0;
            }
        }

        return 1;
    }


    public static List MergeClusters( SortedSet distances, HashSet allClusters, double threshold ) {
        List newClusters = new ArrayList();
        ClusterDistance cd = (ClusterDistance)distances.first();

        boolean removed = distances.remove(cd);
        if( removed == false ) {
            System.out.println( "PROBLEMS! failed removing clusterdistance: " +
                                cd.toString() );
            dumpSet( distances );

            throw new RuntimeException( "ack!!!" );
        }


        while( cd != null ) {
            allClusters.remove( cd.left );
            allClusters.remove( cd.right );


            //System.err.println( "Merged Cluster: dist=" + cd.distance + "(" + distances.size() + ")" );
            //System.err.println( "\tCLUSTER LEFT IS: " + cd.left.toString() );
            //System.err.println( "\tCLUSTER RIGHT IS: " + cd.right.toString() );

            Cluster mergedCluster = new Cluster( cd.left, cd.right,
                                                 cd.distance );
            //System.err.println( "\tRESULTING CLUSTER IS: " + mergedCluster.toString() );


            allClusters.add( mergedCluster );
            newClusters.add( mergedCluster );

            ClusterDistance next = null;

            if( distances.size() > 0 ) {
                ClusterDistance tmp = (ClusterDistance)distances.first();
                if( cd.distance == tmp.distance ) {
                    next = tmp;
                    distances.remove( next );
                }
            }

            cd = next;
        }

        return newClusters;
    }

    public int compareTo( Object o ) {
        Cluster other = (Cluster)o;

        double thissize = getSize();
        double othersize = other.getSize();

        if( thissize < othersize )
            return 1;
        else if( thissize > othersize )
            return -1;
        else {
            return this.hashCode() - other.hashCode();
        }
    }

    static private void dumpSet( SortedSet distances ) {
        Iterator iter = distances.iterator();
        int i=0;
        while( iter.hasNext() ) {
            ClusterDistance cd = (ClusterDistance)iter.next();
            System.out.println( "#" + i + ": " + cd.toString() );
            i++;
        }

    }

    public static class ClusterDistance implements Comparable, Serializable {
        public Cluster left;
        public Cluster right;
        public double distance;

	ClusterDistance() {
	}

	public ClusterDistance( Cluster left, Cluster right, double distance ) {
	    this.left = left;
	    this.right = right;
	    this.distance = distance;
	}

        public String toString() {
            return "ClusterDistance{" + left.toShortString() + "[" + left.hashCode() + "]" + 
                ", " + right.toShortString() + "[" + right.hashCode() + "], " + distance + "}";
        }
       
        public int compareTo( Object o ) {

            ClusterDistance other = (ClusterDistance)o;

            int ret;

            if( this.distance < other.distance )
                ret= -1;
            else if( this.distance > other.distance )
                ret = 1;
            else {
                ret = left.hashCode() - other.left.hashCode();
                if( ret == 0 ) {
                    ret = right.hashCode() - other.right.hashCode();
                }
            }

            return ret;
        }

        public boolean equals( Object o ) {
            if( !(o instanceof ClusterDistance) ) {
                return false;
            }

            boolean ret = ( compareTo( o ) == 0 );
            
            return ret;
        }

        public Cluster getOther( Cluster a ) {
            if( left == a )
                return right;
            if( right == a )
                return left;

            throw new RuntimeException( "ACK!" );
        } 
    }

}

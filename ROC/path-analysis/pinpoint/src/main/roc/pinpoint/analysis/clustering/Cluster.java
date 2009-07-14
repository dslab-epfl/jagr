package roc.pinpoint.analysis.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import swig.util.Debug;

import roc.pinpoint.analysis.structure.Distanceable;

/**
 * Data clustering class.  this class represents a cluster of elements,
 * and also implements some simple hierarchical data clustering algorithms.
 * @author emrek
 *
 */
public class Cluster implements Distanceable {

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

    private Distanceable[] el;

    Map cacheDistances;

    /**
     * creates a cluster containing only a single element
     * @param el a cluster element
     */
    public Cluster(Distanceable el) {
        left = null;
        right = null;
        this.el = new Distanceable[1];
        this.el[0] = el;
	cacheDistances = new HashMap();
    }

    /**
     * creates a cluster containing many elements
     * @param els a list of clusterelements
     */
    public Cluster(List els) {
        left = null;
        right = null;
        this.el = new Distanceable[els.size()];
        Iterator iter = els.iterator();
        int i = 0;
        while (iter.hasNext()) {
            el[i++] = (Distanceable) iter.next();
        }
	cacheDistances = new HashMap();
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
        this.el = new Distanceable[left.el.length + right.el.length];
        System.arraycopy(left.el, 0, this.el, 0, left.el.length);
        System.arraycopy(right.el, 0, this.el, left.el.length, right.el.length);
	cacheDistances = new HashMap();
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
    public int getSize() {
        return el.length;
    }

    /**
     * special case function, returns count of all elements except those
     * labeled as failures.
     * @return int count of non-failure elements
     *
     * @deprecated
     */
    public int getSizeExceptFailures() {
        int ret = 0;

        for (int i = 0; i < el.length; i++) {
            String n = ((ClusterElement)el[i]).getName();
            if (!(n.equals("TotalFailure")
                || n.equals("InternalFailure")
                || n.equals("ExternalFailure"))) {
                ret++;
            }
        }

        return ret;
    }

    /**
     *  
     * @param name name of an element
     * @return boolean whether the named element is in this cluster
     *
     * @deprecated
     */
    public boolean contains(String name) {
        boolean ret = false;
        for (int i = 0; i < el.length; i++) {
            if (name.equals(((ClusterElement)el[i]).getName())) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    /**
     * 
     * @param names a collection of element names
     * @return boolean returns true if all names are elements in this
     * cluster false otherwise.
     *
     * @deprecated
     */
    public boolean containsAll(Collection names) {
        Iterator iter = names.iterator();
        boolean ret = true;

        while (iter.hasNext()) {
            String n = (String) iter.next();
            boolean b = false;

            for (int i = 0; i < el.length; i++) {
                if (n.equals(((ClusterElement)el[i]).getName())) {
                    b = true;
                    break;
                }
            }

            if (!b) {
                ret = false;
                break;
            }
        }

        return ret;
    }

    /**
     * returns the number of cluster elements which are not failure markers, and
     * also not in the given collection.
     * @param names names which are true-positives, not false-positives
     * @return int count of false-positives.
     *
     * @deprecated
     */
    public int getNumFalsePositives(Collection names) {
        Iterator iter = names.iterator();
        int ret = 0;

        for (int i = 0; i < el.length; i++) {
            String n = ((ClusterElement)el[i]).getName();
            if (!(names.contains(n)
                || n.equals("TotalFailure")
                || n.equals("InternalFailure")
                || n.equals("ExternalFailure"))) {
                ret++;
                }
        }

        return ret;
    }

    class Counter {
        int counter = 0;
    }

    String toString(Counter counter) {

        String ret = "{ Cluster #" + counter.counter + " { ";
        for (int i = 0; i < el.length; i++) {
            ret += el[i].toString();
            if (i < el.length) {
                ret += ",";
            }
        }
        ret += "}\n";

        counter.counter++;

        if (left != null) {
            String leftdescr = left.toString(counter);
            int leftcount = counter.counter;
            String rightdescr = right.toString(counter);
            int rightcount = counter.counter;

            ret += "was combined (dist = "
                + distance
                + ") from "
                + "Cluster #"
                + leftcount
                + " and Cluster #"
                + rightcount
                + "\n";
            ret += "}\n";
            ret += leftdescr;
            ret += rightdescr;
        }
        else {
            ret += "}\n";
        }

        return ret;
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
 * @return float distance
 *
 * @deprecated
 */
    public float calculateDistance(
        Cluster c,
        int clustermethod,
        int distancecoeff ) {
        Debug.Assert(0 <= clustermethod);
        Debug.Assert(clustermethod < MAX_CLUSTERMETHOD);

        float[] distance = new float[el.length * c.el.length];

        for (int d = 0, i = 0; i < el.length; i++) {
            for (int j = 0; j < c.el.length; j++, d++) {
                distance[d] = ((ClusterElement)el[i]).calculateDistance((ClusterElement)c.el[j], distancecoeff);
            }
        }

        float ret = -1;

        if (clustermethod == UPGMA_CLUSTERMETHOD) {
            ret = 0;
            for (int i = 0; i < distance.length; i++) {
                ret += distance[i];
            }
            ret = ret / distance.length;
        }
        else if (clustermethod == SLINK_CLUSTERMETHOD) {
            ret = distance[0];
            for (int i = 1; i < distance.length; i++) {
                if (distance[i] < ret) {
                    ret = distance[i];
                }
            }
        }
        else if (clustermethod == CLINK_CLUSTERMETHOD) {
            ret = distance[0];
            for (int i = 1; i < distance.length; i++) {
                if (distance[i] > ret) {
                    ret = distance[i];
                }
            }
        }
        else {
            Debug.AssertNotReached();
        }

        return ret;
    }


    /**
     * calculates the distance between this and another cluster using the
     * UPGMA cluster method
     * @param other cluster
     * @return float distance
     *
     */
    public double getDistance( Distanceable other ) {
	Cluster c = (Cluster)other;

	Double retCache = (Double)cacheDistances.get( c );

	if( retCache == null ) {
	    double[] distance = new double[el.length * c.el.length];
	
	    for (int d = 0, i = 0; i < el.length; i++) {
		for (int j = 0; j < c.el.length; j++, d++) {
		    distance[d] = el[i].getDistance( c.el[j] );
		}
	    }

	    double ret = 0;
	    
	    // UPGMA
	    for (int i = 0; i < distance.length; i++) {
		ret += distance[i];
	    }
	    ret = ret / ((double)distance.length);

	    retCache = new Double( ret );
	    cacheDistances.put( c, retCache );
	}	    

        return retCache.doubleValue();
    }


    /**
     * build a set of clusters, one cluster per element
     * @param el array of elements
     * @return List List of generated clusters.
     */
    public static List BuildClusters(Distanceable[] el) {
        ArrayList ret = new ArrayList(el.length);

        for (int i = 0; i < el.length; i++) {
            ret.add(new Cluster(el[i]));
        }

        return ret;
    }

    /**
     * build a set of clusters, one cluster per element
     * @param el array of elements
     * @return List List of generated clusters.
     */
    public static List BuildClusters( Collection el) {
        ArrayList ret = new ArrayList(el.size());

	Iterator iter = el.iterator();
	while( iter.hasNext() ) {
	    Object o = iter.next();
	    if( !(o instanceof Distanceable )) {
		System.err.println( "Cluster: cannot cast " + o.getClass().toString() + " to interface Distanceable" );
	    }
	    ret.add( new Cluster( (Distanceable)iter.next() ));
	}

        return ret;
    }

/**
 * build a set of clusters, one per cluster element 
 * @param el list of cluster elements
 * @return List  list of generated clusters
 */
    public static List BuildClusters(List el) {
        ArrayList ret = new ArrayList(el.size());

        Iterator iter = el.iterator();
        while (iter.hasNext()) {
            ret.add(new Cluster((Distanceable) iter.next()));
        }

        return ret;
    }

/**
 * hierarchical clustering:  merge a set of clusters together until reaching
 * the threshold stop distance
 * @param clusters list of clusters
 * @param stopdistance threshold distance to stop clustering
 * @return List set of generated clusters.
 *
 */
    public static List MergeCluster( List clusters, double stopdistance) {

        double d = stopdistance - 1;

        while ((clusters.size() > 1) && (d <= stopdistance)) {
            d = MergeClusterHelper( clusters );
            System.err.println("merged cluster: dist = " + d);
        }

        return clusters;
    }


/**
 * helper function for merging clusters
 * @param clusters list of clusters
 * @return float calculated distance
 * 
 */
    protected static double MergeClusterHelper( List clusters ) {

        double[][] distances = new double[clusters.size()][clusters.size()];
        int mini = -1, minj = -1;
        double minDist = 0;

        for (int i = 0; i < clusters.size(); i++) {
            for (int j = i + 1; j < clusters.size(); j++) {
                distances[i][j] =
                    ((Cluster) clusters.get(i)).getDistance((Cluster) clusters.get(j));
                if ((mini == -1) || (distances[i][j] < minDist)) {
                    minDist = distances[i][j];
                    mini = i;
                    minj = j;
                }
            }
        }

        Debug.Assert(mini != -1);

        Cluster mcluster =
            new Cluster(
                (Cluster) clusters.get(mini),
                (Cluster) clusters.get(minj),
                minDist);

        Object oi = clusters.get(mini);
        Object oj = clusters.get(minj);
        clusters.remove(oi);
        clusters.remove(oj);
        clusters.add(mcluster);

        return minDist;
    }

/**
 * hierarchical clustering:  merge a set of clusters together until reaching
 * the threshold stop distance
 * @param clusters list of clusters
 * @param clustermethod method of calculating distance between clusters
 * @param distancecoeff distance coefficient
 * @param stopdistance threshold distance to stop clustering
 * @return List set of generated clusters.
 *
 * @deprecated
 */
    public static List MergeCluster(
        List clusters,
        int clustermethod,
        int distancecoeff,
        float stopdistance) {

        float d = stopdistance - 1;

        while ((clusters.size() > 1) && (d <= stopdistance)) {
            d = MergeClusterHelper(clusters, clustermethod, distancecoeff);
            System.err.println("merged cluster: dist = " + d);
        }

        return clusters;
    }


/**
 * helper function for merging clusters
 * @param clusters list of clusters
 * @param clustermethod method for calculating distance among clusters
 * @param distancecoeff distance coefficient
 * @return float calculated distance
 * 
 * @deprecated
 */
    protected static float MergeClusterHelper(
        List clusters,
        int clustermethod,
        int distancecoeff) {

        float[][] distances = new float[clusters.size()][clusters.size()];
        int mini = -1, minj = -1;
        float minDist = -10;

        for (int i = 0; i < clusters.size(); i++) {
            for (int j = i + 1; j < clusters.size(); j++) {
                distances[i][j] =
                    ((Cluster) clusters.get(i)).calculateDistance(
                        (Cluster) clusters.get(j),
                        clustermethod,
                        distancecoeff);

                if ((minDist == -10) || (distances[i][j] < minDist)) {
                    minDist = distances[i][j];
                    mini = i;
                    minj = j;
                }
            }
        }

        Debug.Assert(minDist != -10);

        Cluster mcluster =
            new Cluster(
                (Cluster) clusters.get(mini),
                (Cluster) clusters.get(minj),
                minDist);

        Object oi = clusters.get(mini);
        Object oj = clusters.get(minj);
        clusters.remove(oi);
        clusters.remove(oj);
        clusters.add(mcluster);

        return minDist;
    }

}

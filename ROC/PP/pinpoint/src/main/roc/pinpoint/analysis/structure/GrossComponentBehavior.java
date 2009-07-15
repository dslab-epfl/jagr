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

import java.io.Serializable;
import java.util.*;

import swig.util.Debug;

import org.apache.log4j.Logger;

/**
 * The GrossComponentBehavior represents the group behavior of
 * a set of components.  Each component's behavior is represented
 * separately as a ComponentBehavior object.  When looking for
 * deviant components, we compare a ComponentBehavior to the
 * set of all stored ComponentBehaviors using the chi^2 test of
 * goodness of fit.
 *
 */
public class GrossComponentBehavior
    implements Identifiable, Deviants, Serializable {

    static final long serialVersionUID = -1116918076267190072L;

    static Logger log = Logger.getLogger( "GrossComponentBehavior" );

    public static final int INITIAL_CAPACITY = 2;

    Map id;

    Collection componentBehaviors;


    Map linkStatistics;

    Map physicalLinkToLogicalLink;

    Set logicalComponentAttrs;

    double acceptableDeviation = 0.25;

    double cachedLinkNormalizer;
    boolean cacheIsValid = false;

    public GrossComponentBehavior(Set logicalComponentAttrs) {
        componentBehaviors = new LinkedList();
        linkStatistics = new HashMap(INITIAL_CAPACITY);
        this.logicalComponentAttrs = logicalComponentAttrs;
        physicalLinkToLogicalLink = new HashMap(INITIAL_CAPACITY);
    }

    public void setLogicalComponentAttrs(Set attrs) {
        logicalComponentAttrs = attrs;
    }

    public synchronized void setAcceptableDeviation(double dev) {
        this.acceptableDeviation = dev;
        Iterator iter = linkStatistics.values().iterator();
        while (iter.hasNext()) {
            ((Deviants)iter.next()).setAcceptableDeviation(acceptableDeviation);
        }
    }

    public boolean matchesId(Map id) {
        if (this.id == null)
            return false;

        return this.id.equals(id);
    }

    public Map getId() {
        return id;
    }

    public Iterator getComponentBehaviorIterator() {
        return componentBehaviors.iterator();
    }

    public double getLinkNormalizer() {

        if (!cacheIsValid) {

            double ret = 0;
            Iterator iter = componentBehaviors.iterator();
            while (iter.hasNext()) {
                ComponentBehavior cb = (ComponentBehavior)iter.next();
                ret += cb.getLinkNormalizer();
            }
            ret /= (double)componentBehaviors.size();

            cachedLinkNormalizer = ret; //tempStat.getMean();
            cacheIsValid = true;
        }

        return cachedLinkNormalizer;
    }

    /**
     * Adds a ComponentBehavior object to the set of ComponentBehaviors
     * represented by this GrossComponentBehavior instance.
     */
    public void addComponentBehavior(ComponentBehavior b) {
        cacheIsValid = false;

        Map bId = IdentifiableHelper.ReduceMap(b.getId(), logicalComponentAttrs);

        if (id == null) {
            id = bId;
        }

        Debug.Assert( id.equals( bId ));
        

        synchronized (b) {
            componentBehaviors.add(b);

            Iterator iter = b.links.values().iterator();
            while (iter.hasNext()) {
                Link l = (Link)iter.next();

                Link logicallink = createLogicalLink(l);

                GrossStatistics s =
                    (GrossStatistics)linkStatistics.get(logicallink);
                if (s == null) {
                    synchronized (this) {
                        s = new GrossStatistics();
                        s.setAcceptableDeviation(acceptableDeviation);
                    }
                    linkStatistics.put(logicallink, s);
                }

                s.addStatistics(logicallink, b);
            }
        }
    }

    Link createLogicalLink(Link l) {

        if (physicalLinkToLogicalLink.containsKey(l))
            return (Link)physicalLinkToLogicalLink.get(l);

        Component src =
            Component.ReduceComponent(l.getSource(), logicalComponentAttrs);
        Component sink =
            Component.ReduceComponent(l.getSink(), logicalComponentAttrs);
        Link ret = new Link(src, sink, l);
        physicalLinkToLogicalLink.put(l, ret);

        return ret;
    }

    /** 
     * returns a sorted set of copmonent behaviors, ranked by their
     * deviation.  
     */
    public SortedSet getDeviants() {
        SortedSet ret = new TreeSet();

        Iterator cbIter = componentBehaviors.iterator();
        checkingComponents : while (cbIter.hasNext()) {
            ComponentBehavior cb = (ComponentBehavior)cbIter.next();
            if (isDeviant(cb))
                ret.add(new RankedObject(getDeviation(cb), cb));
        }

        return ret;
    }

    public boolean isDeviant(ComponentBehavior cb) {

        double dev = getDeviation(cb);
        log.debug( "ID: " + cb.getId().toString() + ": deviation=" + dev );

        if (dev > acceptableDeviation)
            return true;

        return false;
    }

    protected double getDeviationHelper(
        Link logicalLink,
        Link physicalLink,
        ComponentBehavior cb) {

        double ret = 0;
        GrossStatistics stats =
            (GrossStatistics)linkStatistics.get(logicalLink);

	
	double actual;
	double expected;

	actual = (physicalLink == null ) ? 0 : physicalLink.getCount();

	expected = cb.getLinkNormalizer() * 
	    (logicalLink.getCount() / this.getLinkNormalizer());

	log.debug( "\tactual= " + actual + " ; expect= " + expected );

	// chi^2
	ret = (actual - expected) * (actual - expected) / expected;

	log.debug( "\tret= " + ret );

	return ret;
    }

    /**
     * calculates the chi^2 test of goodness of fit for a ComponentBehavior
     * in relation to the contents of this GrossComponentBehavior object
     * The returned deviation is normalized s.t. 1.0 is the theshold for
     * declaring a deviation with a confidence of 0.005
     */
    public double getDeviation(ComponentBehavior cb) {
        Iterator linkIter = cb.links.values().iterator();
        double ret = 0;
	int count = 0;

        log.debug( "GCB: getDeviation() START" );
        Set unprocessedGrossLinks = new HashSet(linkStatistics.keySet());

        while (linkIter.hasNext()) {
            Link cLink = (Link)linkIter.next();
            Link logicalLink = createLogicalLink(cLink);
            unprocessedGrossLinks.remove(logicalLink);
            ret += getDeviationHelper(logicalLink, cLink, cb);
	    count++;
        }

        Iterator unprocessedLinkIter = unprocessedGrossLinks.iterator();
        while (unprocessedLinkIter.hasNext()) {
            Link logicalLink = (Link)unprocessedLinkIter.next();
            ret += getDeviationHelper(logicalLink, null, cb);
	    count++;
        }

	/**
	// this is the modified jaccard metric
	ret = 1.0 - (ret) / (2.0 - ret);
	**/

	// divide ret by c for the chi^2 statistic
	// so that if ret > 1, it's bad, if its < 1, it's ok.
	
	if( count > 1 ) {
	    double chi = getChi2( count-1 );
	    log.debug( "\tcount= " + count + " ; chi2=" + chi );
	    ret = ret / chi;
	}
	else {
	    if( ret > 0.0 ) 
		ret = 1.0;
	    log.debug( "\tcount= " + count + " ; chi2=NA" );
	}

	log.debug( "GCB: getDeviation() END: ret= " + ret );

        return ret;
    }

    private double getChi2( int n ) {
	double[] chi_0995 = {
	    7.87943857662242, // 1 degree of freedom
	    10.5966347330961,
	    12.8381564665987,
	    14.8602590005602,
	    16.749602343639,
	    18.5475841785111,
	    20.2777398749626,
	    21.9549549906595,
	    23.5893507812574,
	    25.1881795719712,
	    26.7568489164696,
	    28.299518822046,
	    29.8194712236532,
	    31.3193496225953,
	    32.8013206457918,
	    34.2671865378267,
	    35.7184656590046,
	    37.1564514566067,
	    38.5822565549342,
	    39.9968463129386,
	    41.4010647714176,
	    42.7956549993085,
	    44.1812752499711,
	    45.5585119365306,
	    46.9278901600808,
	    48.2898823324568,
	    49.6449152989942,
	    50.9933762684994,
	    52.3356177859336,
	    53.6719619302406,
	    55.0027038800239,
	    56.3281149597109,
	    57.6484452558585,
	    58.9639258755194,
	    60.274770904781,
	    61.5811791147573,
	    62.8833354537412,
	    64.1814123574062,
	    65.475570903468,
	    66.7659618328039,
	    68.0527264554416,
	    69.3359974569004,
	    70.6158996179664,
	    71.8925504589992,
	    73.166060818225,
	    74.4365353721017,
	    75.7040731046948,
	    76.9687677320445,
	    78.2307080866899,
	    79.4899784668289,
	    80.7466589540133,
	    82.0008257027754,
	    83.2525512051611,
	    84.5019045327765,
	    85.748951558641,
	    86.9937551608717,
	    88.2363754099821,
	    89.476869741381,
	    90.7152931144758,
	    91.9516981596297,
	    93.1861353140891,
	    94.4186529478745,
	    95.6492974805285,
	    96.8781134895179,
	    98.1051438110095,
	    99.3304296336631,
	    100.554010586028,
	    101.775924818064,
	    102.996209077265,
	    104.214898779817,
	    105.432028077177,
	    106.647629918433,
	    107.861736108763,
	    109.074377364285,
	    110.28558336358,
	    111.495382796113,
	    112.70380340779,
	    113.910872043852,
	    115.116614689292,
	    116.321056506969,
	    117.524221873581,
	    118.726134413634,
	    119.926817031548,
	    121.126291942023,
	    122.324580698781,
	    123.521704221777,
	    124.717682822992,
	    125.912536230897,
	    127.106283613653,
	    128.298943601145,
	    129.49053430592,
	    130.681073343076,
	    131.870577849188,
	    133.059064500317,
	    134.246549529152,
	    135.433048741346,
	    136.61857753108,
	    137.803150895913,
	    138.986783450939,
	    140.169489442314
	};

	if( n > 101 )
	    n = 101;

	return chi_0995[n-1];
    }

    protected double getPerformanceDeviationHelper(
        Link logicalLink,
        Link physicalLink,
        ComponentBehavior cb) {
        double ret = 0;
        GrossStatistics stats =
            (GrossStatistics)linkStatistics.get(logicalLink);

        if (stats != null) {
            ret += (stats.meanStatistics().getDeviation(physicalLink.getMean())
            /*/ stats.meanStatistics().getStdDev() */
            ) * (physicalLink.getCount() / cb.getLinkNormalizer());

            ret
                += (stats
                    .stdDevStatistics()
                    .getDeviation(physicalLink.getStdDev())
            /*/ stats.stdDevStatistics().getStdDev()*/
            ) * (physicalLink.getCount() / cb.getLinkNormalizer());

        }
        else {
            log.warn( "unexpected link when calculating performance deviation" );
        }

        return ret;
    }

    public double getPerformanceDeviation(ComponentBehavior cb) {
        Iterator linkIter = cb.links.values().iterator();
        double ret = 0;

        while (linkIter.hasNext()) {
            Link cLink = (Link)linkIter.next();
            Link logicalLink = createLogicalLink(cLink);
            double dev = getPerformanceDeviationHelper(logicalLink, cLink, cb);

            if (cLink.getSink().equals(cb.getComponent())) {
                ret += dev;
            }
            else {
                ret -= dev;
            }
        }

        // TODO: munge ret? normalize it?

        return ret;
    }

    public double getDeviation(Object o) throws ClassCastException {
        return getDeviation((ComponentBehavior)o);
    }

    public boolean isDeviant(Object o) {
        return isDeviant((ComponentBehavior)o);
    }

    public String toString() {
        String ret = "{GrossComponentBehavior: id="
            + id.toString()
            + ", componentbehaviors="
            + componentBehaviors.toString()
            + "}";
	ret += "\n";

	ret += "*******";

	Iterator iter = linkStatistics.keySet().iterator();
	while( iter.hasNext() ) {
	    Link logicallink = (Link)iter.next();
	    GrossStatistics gs = (GrossStatistics)linkStatistics.get( logicallink );
	    ret += "LOGICALLINK: " + logicallink.toString() + ": " + gs.toString() + "\n";
	}

	return ret;
    }

}

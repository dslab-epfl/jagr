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
package roc.pinpoint.analysis.dtree;

import java.util.*;
import roc.pinpoint.analysis.structure.*;

/**
 * Simple decision-tree learning algorithm
 * <i>Note: the decision-tree code in this package is due for a rewrite,
 * as it inappropriately blends rule extraction with tree generation,
 * which makes several techniques, like alternative branch exploration and
 * pruning more difficult</i>
 *
 * <p>
 * See the plugin roc.pinpoint.analysis.plugins2.DTreeCorrelation, and its
 * DTreeCorrelation.MyCorrelationTask.run() method to see an example usage
 * of this decision tree.
 *
 */
public class DTree {

    static double DEFAULT_GAIN_THRESHOLD = 0.005;

    public static Set DiagnosePathFailures( Set paths ) {
	DTreeGroup root = new DTreeGroup();

	// 1. translate paths into DTreeElements
	Iterator pathsIter = paths.iterator();
	while( pathsIter.hasNext() ) {
	    Path p = (Path)pathsIter.next();

	    System.err.println( "DTree: Adding path: " /*+ p.toString()*/ );

	    if( p.getRequestTypes().contains( "/estore/populate" )) {
		System.err.println( "ignoring path for /estore/populate" );
		continue;
	    }

	    DTreeElement el = CreateElement( p );
	    root.addElement( el );
	}

	// 2. recursively divide the root group and generate a rule.
	Set rules = LearnDTreeRules( root, DEFAULT_GAIN_THRESHOLD );
	

	Set ret = null;

	if( rules != null ) {
	    // lets pick out only the simplest rules
	    Map rulesMap = new HashMap();
	    Iterator iter = rules.iterator();
	    while( iter.hasNext() ) {
		DTreeRule rule = (DTreeRule)iter.next();
		int pos = rule.coversPositive;
		
		RankedObject r = (RankedObject)rulesMap.get( new Integer( pos ));
		if(( r == null ) || ( r.getRank() > rule.getDepth())) {
		    List l = new LinkedList();
		    l.add( rule );
		    r = new RankedObject( rule.getDepth(), l );
		    rulesMap.put( new Integer( pos ), r );
		}
		else if( r.getRank() == rule.getDepth() ) {
		    List l = (List)r.getValue();
		    l.add( rule );
		}
	    }
	    
	    ret = new HashSet();
	    iter = rulesMap.values().iterator();
	    while( iter.hasNext() ) {
		RankedObject ro = (RankedObject)iter.next();
		List l = (List)ro.getValue();
		Iterator iter2 = l.iterator();
		while( iter2.hasNext() ) {
		    DTreeRule rule = (DTreeRule)iter2.next();
		    ret.add( rule );
		}
	    }
	}
	 
	return ret;
    }

    public static DTreeElement CreateElement( Path p ) {
	Map attrs = new HashMap();

	Iterator compIter = p.getComponents().iterator();
	while( compIter.hasNext() ) {
	    Component c = (Component)compIter.next();
	    
	    Map id = c.getId();
	    /*
	    Iterator attrIter = id.keySet().iterator();
	    while( attrIter.hasNext() ) {
		String s = (String)attrIter.next();

		String v = (String)id.get( s );

		Set hs = (Set)attrs.get( s );
		if( hs == null ) {
		    hs = new HashSet();
		    attrs.put( s, hs );
		}
		hs.add( v );
	    }
	    */
	    String compname = (String)id.get("name");

	    Set nameset = (Set)attrs.get("name");
	    if( nameset == null ) {
		nameset = new HashSet();
		attrs.put( "name", nameset );
	    }
	    nameset.add( compname );

	    String compip = (String)id.get( "ipaddress" );
	    Set ipset = (Set)attrs.get("ipaddress");
	    if( ipset == null ) {
		ipset = new HashSet();
		attrs.put( "ipaddress", ipset );
	    }
	    ipset.add( compip );

	    /*
	    // special case: add ipaddress and name together...
	    String instance = id.get("name") + ":" + id.get( "ipaddress");
	    Set instanceset = (Set)attrs.get( "instance" );
	    if( instanceset == null ) {
		instanceset = new HashSet();
		attrs.put( "instance", instanceset );
	    }
	    instanceset.add( instance );
	    */
	}

	int numPositive;
	int numNegative;
	
	if( p.hasErrors() ) {
	    System.err.println( "\tPath has errors" ); 
	    numPositive = 1;
	    numNegative = 0;
	}
	else {
	    System.err.println( "\tPath has no errors" ); 
	    numPositive = 0;
	    numNegative = 1;
	}

	DTreeElement ret = new DTreeElement( attrs,
					     numNegative, numPositive );

	return ret;	
    }

    public static Set LearnDTreeRules( DTreeGroup group,
				       double gainThreshold ) {
	
	double groupEntropy = group.getEntropy();
	double minEntropy = group.getMinEntropy();

	System.err.println( "DTree: (groupEntropy,minEntropy) = (" + 
			    groupEntropy + ", " + minEntropy + ")" );

	if( groupEntropy - minEntropy < gainThreshold ) {
	    System.err.println( "DTree: below threshold nothing to learn." );
	    return null;  // found nothing to learn; return
	}

	Set minAttrs = group.getEntropyMinAttributes();
	Set matchingChildren = group.getMinMatchingChildren();
	Set nonmatchingChildren = group.getMinNonmatchingChildren();

	SortedSet ret = new TreeSet();

	System.err.println( "PUSH: choosing attribute(s): " + minAttrs );

	if( matchingChildren.size() != 0 &&
	    nonmatchingChildren.size() != 0 ) {
	    LearnDTreeRulesHelper( ret, matchingChildren, gainThreshold,
				   minAttrs, true );
	    LearnDTreeRulesHelper( ret, nonmatchingChildren, gainThreshold,
				   minAttrs, false );
	}

	System.err.println( "POP: choosing attribute ..." );

	return ret;
    }

    private static void LearnDTreeRulesHelper( SortedSet ret, 
					Set elements,
					double gainThreshold,
					Set attrs, boolean matched ) {
	DTreeGroup group = new DTreeGroup( elements );

	Set rules = LearnDTreeRules( group, gainThreshold );
	
	String attr = "";
	Iterator iterAttrs = attrs.iterator();
	while( iterAttrs.hasNext() ) {
	    Object a = iterAttrs.next();
	    attr += a.toString();
	    if( iterAttrs.hasNext() ) {
		attr += "||";
	    }
	}
	
	if( attrs.size() > 1 ) {
	    attr = "(" + attr + ")";
	}

	if( rules == null ) {
	    DTreeRule r = new DTreeRule( attr, (matched?"TRUE":"FALSE"),
					 (group.leansPositive()?"TRUE":"FALSE" ));
	    r.coversPositive = group.cache_totalPositive;
	    r.coversNegative = group.cache_totalNegative;
	    ret.add( r );
	}
	else {
	    Iterator rIter = rules.iterator();
	    while( rIter.hasNext() ) {
		DTreeRule r = (DTreeRule)rIter.next();
		DTreeRule n = new DTreeRule( attr, (matched?"TRUE":"FALSE"),
					     r );
		ret.add( n );
	    }
	}
    }


    /**
     * test code for sanity-checking the decision tree algorithm
     */
    public static void main( String[] argv ) {

	String OUTLOOK = "OUTLOOK";
	String SUNNY = "sunny";
	String OVERCAST = "overcast";
	String RAIN = "rain";

	String TEMPERATURE = "TEMPERATURE";
	String HOT = "hot";
	String MILD = "mild";
	String COOL = "cool";
	
	String HUMIDITY = "HUMIDITY";
	String HIGH = "high";
	String NORMAL = "normal";
	String LOW = "low";

	String WIND = "WIND";
	String WEAK = "weak";
	String STRONG = "strong";

	String PLAYTENNIS = "PLAYTENNIS";
	String YES = "yes";
	String NO = "no";

	String[] outlooks = {
	    SUNNY,
	    SUNNY,
	    OVERCAST,
	    RAIN,
	    RAIN,
	    RAIN,
	    OVERCAST,
	    SUNNY,
	    SUNNY,
	    RAIN,
	    SUNNY,
	    OVERCAST,
	    OVERCAST,
	    RAIN };

	String[] temps = {
	    HOT,
	    HOT,
	    HOT,
	    MILD,
	    COOL,
	    COOL,
	    COOL,
	    MILD,
	    COOL,
	    MILD,
	    MILD,
	    MILD,
	    HOT,
	    MILD };

	String[] humidities = {
	    HIGH,
	    HIGH,
	    HIGH,
	    HIGH,
	    NORMAL,
	    NORMAL,
	    NORMAL,
	    HIGH,
	    NORMAL,
	    NORMAL,
	    NORMAL,
	    HIGH,
	    NORMAL,
	    HIGH };

	String[] winds = {
	    WEAK,
	    STRONG,
	    WEAK,
	    WEAK,
	    WEAK,
	    STRONG,
	    STRONG,
	    WEAK,
	    WEAK,
	    WEAK,
	    STRONG,
	    STRONG,
	    WEAK,
	    STRONG };

	boolean[] tennis = {
	    false,
	    false,
	    true,
	    true,
	    true,
	    false,
	    true,
	    false,
	    true,
	    true,
	    true,
	    true,
	    true,
	    false };


	DTreeElement[] el = new DTreeElement[14];

	DTreeGroup group = new DTreeGroup();

	for( int i=0; i<el.length; i++ ) {
	    Map attrs = new HashMap();
	    attrs.put( OUTLOOK, Collections.singleton( outlooks[i] ));
	    attrs.put( TEMPERATURE, Collections.singleton( temps[i] ));
	    attrs.put( HUMIDITY, Collections.singleton( humidities[i] ));
	    attrs.put( WIND, Collections.singleton( winds[i] ));
	    
	    int posCount= (tennis[i])?(1):(0);
	    int negCount= 1 - posCount;

	    el[i] = new DTreeElement( attrs, negCount, posCount );

	    group.addElement( el[i] );
	}

	System.err.println( "Added " + el.length + " elements; DTreeGroup contains " + group.size() + " elements" );

	Set rules = LearnDTreeRules( group, 0.01 );

	Iterator iter = rules.iterator();
	while( iter.hasNext()) {
	    DTreeRule rule = (DTreeRule)iter.next();
	    System.err.println( rule.toString() );
	}
    }

    
}

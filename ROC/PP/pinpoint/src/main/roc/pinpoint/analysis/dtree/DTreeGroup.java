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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class DTreeGroup {

    Set elements;

    double cache_entropy;

    SortedSet cache_entropySortedAttributes;

    Set cache_entropyMinimizingAttributes;
    double cache_minEntropy;

    int cache_totalPositive;
    int cache_totalNegative;

    Set cache_matchingChildren;
    Set cache_nonmatchingChildren;

    public DTreeGroup() {
	this( new HashSet());
    }

    public DTreeGroup( Set elements ) {
	this.elements = elements;
	clearCache();
    }

    public synchronized void addElement( DTreeElement el ) {
	elements.add( el );
	clearCache();
    }

    public Set getElements() {
	return elements;
    }

    public synchronized int size() {
	return elements.size();
    }

    public synchronized void removeElement( DTreeElement el ) {
	elements.remove( el );
	clearCache();
    }

    public void removeAllElements( DTreeGroup group ) {
	Iterator iter = group.elements.iterator();
	while( iter.hasNext() ) {
	    DTreeElement el = (DTreeElement)iter.next();
	    if( elements.contains( el )) {
		removeElement( el );
	    }
	}
    }

    protected synchronized void clearCache() {
	cache_entropyMinimizingAttributes = null;
	cache_entropy = 0;
	cache_minEntropy = 0;
	cache_totalPositive = 0;
	cache_totalNegative = 0;
	cache_matchingChildren = null;
	cache_nonmatchingChildren = null;
    }

    public synchronized boolean leansPositive() {
	doEntropyMinimization();
	return (cache_totalPositive > cache_totalNegative);
    }

    protected synchronized void doEntropyMinimization( ){
	
	if( cache_entropyMinimizingAttributes != null ) {
	    return;
	}

	clearCache();

	Map indexByAttr = new HashMap();

        // 1. index all elements by attribute
        // 2. at the same time, calculate total negative & positive;
        Iterator elIter = elements.iterator();
        while (elIter.hasNext()) {
            DTreeElement el = (DTreeElement) elIter.next();
            entropyMinimizationHelper(indexByAttr, el);
            cache_totalNegative += el.getNumNegative();
            cache_totalPositive += el.getNumPositive();
        }	

	int total = cache_totalNegative + cache_totalPositive;
	
	cache_entropy = DTreeUtil.CalculateEntropy( cache_totalNegative,
						    cache_totalPositive );

	System.err.println( "*** Current: p=" + cache_totalPositive + "; n=" + cache_totalNegative + "; e=" + cache_entropy );

	Iterator iter = indexByAttr.keySet().iterator();
	while( iter.hasNext() ) {
	    Object attr = iter.next();
	    Helper h = (Helper) indexByAttr.get( attr );
	    double h_entropy = h.getEntropy();
	    double h_infogain = h.getInfoGain();
	    System.err.println( "    ... deciding by attr '"
				+ attr
				+ "' has match[p=" + h.myPositive + "; n=" + h.myNegative + "]"
				+ " and nomatch[p=" + h.otherPositive + "; n=" + h.otherNegative + "]"
				+ " and gives entropy = "
				+ h_entropy 
				+ " and infogain = "
				+ h_infogain );
	}

	// 3. sort all attribute entries by information gain...
	TreeSet sortedHelpers = new TreeSet();
	sortedHelpers.addAll( indexByAttr.values() );

	// 3.5 store this sorted list in case anyone wants it later
	cache_entropySortedAttributes = sortedHelpers;


	// 4. pick out the helper with the lowest entropy/highest info gain
	//    and extract the important information...
	Helper minHelper = (Helper) sortedHelpers.last();

	Helper dummyHelper = 
	    new Helper( minHelper.getInfoGain() - 0.000000001 );

	// 5. then pick out all helpers that have equal info gain.
	Set tiedHelpers = sortedHelpers.tailSet( dummyHelper );
	Iterator iterTied = tiedHelpers.iterator();
	cache_entropyMinimizingAttributes = new HashSet();
	cache_minEntropy = minHelper.getEntropy();
	while( iterTied.hasNext() ) {
	    Helper tied = (Helper)iterTied.next();
	    cache_entropyMinimizingAttributes.add( tied.myAttribute );
	}

	// TODO: optimisitically, we're assuming here that all the attributes
	//  tied with the minimum are actually the same... need to check that
	//  assumption, really.

	// 6. pull out the matching and not-matching children to
	//    branch the decision tree.
	cache_matchingChildren = minHelper.myElements;
	cache_nonmatchingChildren = new HashSet( this.elements );
	cache_nonmatchingChildren.removeAll( cache_matchingChildren );
    }

    private void entropyMinimizationHelper( Map index, DTreeElement el ) {
	Set attrs = el.getAttrs();
	Iterator attrIter = attrs.iterator();
	while( attrIter.hasNext() ) {
	    Object a = attrIter.next();
	    Helper helper = (Helper) index.get( a );
	    if( helper == null ) {
		helper = new Helper( a );
		index.put( a, helper );
	    }
	    helper.addElement( el );
	}
    }

    public synchronized double getEntropy() {
	doEntropyMinimization();
	return cache_entropy;
    }

    public synchronized Set getEntropyMinAttributes( ){
	doEntropyMinimization();
	return cache_entropyMinimizingAttributes;
    }

    // horrible name
    public synchronized SortedSet getSortedEntropyAttributes() {
	doEntropyMinimization();
	return cache_entropySortedAttributes;
    }

    public synchronized double getMinEntropy() {
	doEntropyMinimization();
	return cache_minEntropy;
    }

    public synchronized Set getMinMatchingChildren() {
	doEntropyMinimization();
	return cache_matchingChildren;
    }

    public synchronized Set getMinNonmatchingChildren() {
	doEntropyMinimization();
	return cache_nonmatchingChildren;
    }

    class Helper implements Comparable {
	public Object myAttribute;

	Set myElements;

	int myPositive;
	int myNegative;

	int otherPositive;
	int otherNegative;

	boolean isValid;

	double myEntropy = 0;
	double myInfogain = 0;

	Helper( double myDummyInfoGain ) {
	    // this constructor is only used to extract tailSets from a 
	    //  sorted set of stuff.
	    isValid = true;
	    myInfogain = myDummyInfoGain;
	}

	Helper( Object attribute ) {
	    this.myAttribute = attribute;
	    myElements = new HashSet();
	    isValid = false;
	}

	public void addElement( DTreeElement el ) {
	    myElements.add( el );
	    myNegative += el.getNumNegative();
	    myPositive += el.getNumPositive();
	}

	private void calcMyEntropy() {
	    
	    int myTotal = myNegative + myPositive;

	    otherNegative = cache_totalNegative - myNegative;
	    otherPositive = cache_totalPositive - myPositive;

	    System.err.println( "MyHelper::calcMyEntropy() -> cp=" + cache_totalPositive 
				+ "; cn=" + cache_totalNegative + "; p=" + myPositive 
				+ "; n=" + myNegative + "; op=" + otherPositive + "; on=" + otherNegative );
				

	    int otherTotal = otherNegative + otherPositive;

	    myEntropy = ((double)(( myTotal * DTreeUtil.CalculateEntropy( myPositive,
								     myNegative )) 
				  + ( otherTotal * DTreeUtil.CalculateEntropy( otherPositive,
									  otherNegative )))) 
		/ ((double)(myTotal + otherTotal ));
	    
	    myInfogain = DTreeGroup.this.cache_entropy - myEntropy;

	    isValid = true;
	}

	private double getEntropy() {
	    if( !isValid ) {
		this.calcMyEntropy();
	    }
	    return myEntropy;
	}

	private double getInfoGain() {
	    if( !isValid ) {
		this.calcMyEntropy();
	    }
	    return myInfogain;
	}

	public int compareTo( Object o ) {
            if (o == null || !(o instanceof DTreeGroup.Helper)) {
                throw new ClassCastException(
                    "cannot compare "
                        + "DTreeGroup.Helper to "
                        + ((o == null) ? "null" : (o.getClass().toString())));
            }

	    Helper other = (Helper)o;
	    
	    if( this.getInfoGain() > other.getInfoGain() ) {
		return 1;
	    }
	    else if ((this.getInfoGain() == other.getInfoGain() ) &&
		     (this.myPositive > other.myPositive )) {
		return 1;
	    }
	    
	    return -1;
	}

    }

}

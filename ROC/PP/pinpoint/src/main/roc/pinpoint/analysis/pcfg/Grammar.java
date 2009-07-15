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
/*
 * Created on Aug 31, 2003
 *
 */
package roc.pinpoint.analysis.pcfg;

import java.io.Serializable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import roc.pinpoint.analysis.structure.Component;
import roc.pinpoint.analysis.structure.Path;

/**
 * This class represents a probabilistic context free grammar
 * 
 * @author emrek
 *
 */
public class Grammar implements Serializable {

    static final long serialVersionUID = -5594864782669440863L;

    Node start; // start symbol in grammar

    Map nonterminals;

    public Grammar() {
	nonterminals = new HashMap();
        start = new Node(null);
    }

    /**
     * returns the mean branching factor in this grammar
     */
    public int getBranchingFactor() {
	int[] branchfactor = new int[ nonterminals.size() ];
	
	Iterator iter = nonterminals.values().iterator();
	int i=0;
	while( iter.hasNext() ) {
	    Node node = (Node)iter.next();
	    branchfactor[i] = node.branchfactor();
	    i++;
	}

	Arrays.sort( branchfactor );


	int min = branchfactor[0];
	int max = branchfactor[branchfactor.length - 1];
	int median = branchfactor[ branchfactor.length / 2];
	double mean = 0;
	for( i=0; i<branchfactor.length; i++ ) {
	    mean += branchfactor[i];
	}
	mean /= branchfactor.length;

	System.err.println( "BranchingFactor: [min/median/mean/max/size] = " +
			    "[" + min + "/" + median + "/" + mean + "/" + 
			    max + "/" + branchfactor.length + "]" );

	return (int)mean;
    }

    /**
     * adds the information from this path to adjust the probabilities in
     * this grammar
     */
    public void addPath(Path p) {

	//System.err.println( "GRAMMAR:ADDPATH(): adding:\n" + p.toString() );

        Path.PathNode root = p.getRootNode();
        List rootList = new ArrayList(1);
	Object id = getPathNodeId( root );
	if( id != null )
	    rootList.add( id ); 
        start.addChildSet(rootList);

        addPathHelper(root);

	//System.err.println( "GRAMMAR:ADDPATH(): RESULT = \n" + this.toString() );
    }

    private void addPathHelper(Path.PathNode pn) {

        Map id = getPathNodeId(pn);
	if( id == null )
	    return;
	
        Node n = (Node) nonterminals.get(id);

	if( n == null ) {
	    n = new Node( id );
	    nonterminals.put( id, n );
	}

        List childList = getChildList(pn);
        n.addChildSet(childList);

        Iterator iter = pn.getCallees().iterator();
        while (iter.hasNext()) {
            Path.PathNode next = (Path.PathNode) iter.next();
            addPathHelper(next);
        }
    }

    private List getChildList(Path.PathNode pn) {
        List callees = pn.getCallees();
        List ret = new ArrayList(callees.size());

        Iterator iter = callees.iterator();
        while (iter.hasNext()) {
            Path.PathNode child = (Path.PathNode) iter.next();

	    Map id = getPathNodeId(child); 
	    
	    if( id != null ) {
		ret.add( id );
	    }
        }

        return ret;
    }

    private Map getPathNodeId(Path.PathNode pn) {

        Component comp = pn.getComponent();
	Map id= comp.getId();

	if( "org.jnp.server.NamingContext".equals( id.get( "name" )) ||
	    "org.jboss.ha.HAJNDI".equals( id.get( "name" ))) {
	    return null;
	}


	Map ret = new HashMap();
	ret.put( "name", id.get( "name" ));
	ret.put( "methodName", id.get( "methodName" ));
	

        return ret;
    }


    private int derivCount = 0;


    /**
     * gets ths score of a path according to this grammar.  A score
     * of 0 means a perfect fit, higher scores are poorer fits.
     */
    public double getScore(Path p) {
        double ret;

	System.err.println( "Calc. Probability of p=" + p.toString() );


        Path.PathNode root = p.getRootNode();
        List rootList = new ArrayList(1);
	Object id = getPathNodeId( root );
	if( id != null )
	    rootList.add( id );

	ret = 1.0;

	derivCount = 0;
        ret *= getScoreHelper(root);

	System.err.println( "...FINAL: ret = " + ret + 
			    " derivCount=" + derivCount );

	ret = ret / (double)derivCount;

	System.err.println( "ret=" + ret );
	System.err.println( "-------------------------" );

        return ret;
    }


    private double getScoreHelper(Path.PathNode pn) {
	double ret = 0;

        Map id = getPathNodeId(pn);

	if( id == null )
	    return 0;

        Node n = (Node) nonterminals.get(id);

	derivCount++;



	if( n != null ) {
	    List childList = getChildList(pn);
	    double p = n.getScore(childList);

	    System.err.println( "\tprobabilityhelper.a: " + id +
				" -> " + childList + " has p=" + p );

	    ret = p;

	    System.err.println( "\tret=" + ret );

	    Iterator iter = pn.getCallees().iterator();
	    while (iter.hasNext()) {
		Path.PathNode next = (Path.PathNode) iter.next();
		p = getScoreHelper( next );

		System.err.println( "\tprobabilityhelper.b: " + id +
				    " -> " + childList + " has p=" + p );

		ret += p;

		System.err.println( "\tret=" + ret );
	    }
	}
	else {
	    System.err.println( "\tprobabilityhelper: didnt find " + id );
	    // we've already penalized earlier for not finding
	    //  the expansion to "id", so let's not penalize again.

	    ret = 0.0;
	}

	return ret;
    }

    public String toString() {
	String ret = "";

	ret += "GRAMMAR: \n";
	ret += "\tStartNode = " + start.toString();

	Iterator iter = nonterminals.keySet().iterator();
	while( iter.hasNext() ) {
	    Map k =(Map)iter.next();
	    ret += "\t" + k + " = " + (nonterminals.get(k)) + "\n";
	}

	return ret;
    }

}

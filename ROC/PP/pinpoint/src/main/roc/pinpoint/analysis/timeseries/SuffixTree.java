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
package roc.pinpoint.analysis.timeseries;

import java.util.*;
import roc.pinpoint.analysis.structure.*;

/**
 * SuffixTree allows fast and compact representation of all the
 * suffixes of a string, as well as the relative frequency of all
 * substrings of a string.  This class also provides functions for
 * comparing two suffix trees, and finding substrings that occur
 * with different frequencies in both.
 *
 */
public class SuffixTree {

    private byte[] string;
    int start;
    int length;

    private Node root;

    SuffixTree( byte[] string ) {
	this( string, 0, string.length );
    }

    SuffixTree( byte[] string, int start, int length ) {
	this.string = string;
	this.start = start;
	this.length = length;
	
	root = new Node( new Word( string, 0, 0 ) );

	// TODO
	// THIS IS NOT A LINEAR ALGORITHM.  BUT I DONT
	//  QUITE GROK UKKONEN YET.
	for( int i=start; i<length; i++ ) {
	    for( int j=start; j<=i; j++ ) {
		Word w = new Word( string, j, i-j+1 );
		//System.err.println( "Adding Word: " + w.toString() );
		root.add( w, w );
	    }
	}

	root.doCount();
    }

    public SortedSet getSurprises( SuffixTree x, double threshold,
				   int maxLength, byte ignore ) {
	SortedSet ret = new TreeSet();

	LinkedList nodes = new LinkedList();

	{   // begin a breadth-first search
	    Iterator iter = x.root.childEdges.iterator();
	    while( iter.hasNext( )) {
		Edge edge = (Edge)iter.next();
		nodes.add( edge.child );
	    }
	}


	while( !nodes.isEmpty() ) {
	    Node u = (Node)nodes.remove(0);

	    if( u.suffix.len > maxLength )
		continue;

	    //System.err.println( "Checking substring '" + u.suffix.toString() + "' for surprises..." );

	    double normalizer = ((double)( x.length - u.suffix.len + 1 )) 
		/ ((double)( this.length - u.suffix.len + 1 ));

	    //System.err.println( "\tnormalizer = " + normalizer );
		
	    double e = calculateExpectation( u.suffix, normalizer );

	    //System.err.println( "\tExp = " + e + "; actual = " + u.count );

	    double s = Math.abs( e - u.count ) / (e + u.count);

	    //System.err.println( "\tSurprise = " + s );

	    if( s >= threshold && !u.suffix.endsWith( ignore )) {
		ret.add( new RankedObject( s, u.suffix ));
	    }

	    // breadth-first search
	    Iterator iter = u.childEdges.iterator();
	    while( iter.hasNext() ) {
		Edge edge = (Edge)iter.next();
		nodes.add( edge.child );
	    }
	}

	return ret;
    }



    public double calculateExpectation( Word w, double normalizer ) {

	int c = getCount( w );
	if( c > 0 ) {
	    return normalizer * c;
	}

	// otherwise, calculate based on substrings of w

	double ret;
	double d;

	int l;
	boolean covers = false;

	for( l=w.len-1; l>1; l-- ) {

	    covers = true;

	    for( int i=0; covers && i<=w.len-l; i++ ) {
		int n = getCount( new Word( w, i, l ));
		if( n == 0 ) 
		    covers = false;
	    }
	    
	    if( covers ) {
		break;
	    }
	}

	if( l > 1 ) {
	    double f1 = 1.0;
	    for( int i=0; i<=w.len-l; i++ ) {
		Word tw = new Word( w, i, l );
		int t = getCount( tw );
		//System.err.println( "\tcount of " + tw.toString() + " = " + t );
		f1 *= t;
	    }

	    double f2 = 1.0;
	    for( int i=1; i<=w.len-l; i++ ) {
		Word tw = new Word( w, i, l-1 );
		int t = getCount( tw );
		//		System.err.println( "\tcount of " + tw.toString() + " = " + t );
		f2 *= t;
	    }

	    //System.err.println( "\t[l=" + l + "; f1=" + f1 + " f2=" + f2 + "]" );
	    ret = normalizer * f1/f2;
	}
	else {
	    double f1 = 1.0;
	    for( int i=0; i<w.len; i++ ) {
		f1 *= ((double)getCount( new Word( w, i, 1 )) / (double)length );
	    }

	    ret = ( length - w.len + 1 ) * f1;
	}

	

	return ret;
    }

    public int getCount( byte[] w ) {
	return getCount( new Word( w ));
    }

    private int getCount( Word w ) {
	Node n = root.getNode( w );
	if( n == null )
	    return 0;
	else 
	    return n.getCount();
    }

    public String toString() {
	return "{TREE: " + root.toString() + "}";
    }


    class Word {
	// represents the word formed by the len characters at idx in s
	byte[] s;
	int idx;
	int len;

	Word( byte[] w ) {
	    this( w, 0, w.length );
	}

	Word( byte[] s, int idx, int len ) {
	    this.s = s;
	    this.idx = idx;
	    this.len = len;
	}

	Word( Word w, int idx, int len ) {
	    this( w.s, w.idx + idx, len );
	}

	boolean endsWith( byte b ) {
	    return ( s[idx+len-1] == b );
	}

	int matches( Word w ) {
	    int i;
	    for( i=0; 
		 (i<w.len) 
		     && (i<len)
		     && (w.s[w.idx+i] == s[idx+i]);
		 i++ ) {
		// nothing
	    }

	    return i;
	}

	public String toString() {
	    byte[] ret = new byte[ len ];
	    byte base = (byte)'a';
	    for( int i=0; i<len; i++ ) {
		ret[i] = (byte)(base + string[ idx + i ]);
	    }
	    return new String( ret );

	}
    }

    class Node {
	int count;
	Edge parentEdge;
	LinkedList childEdges;

	Word suffix;

	Node( Word suffix ) {
	    this.suffix = suffix;
	    parentEdge = null;
	    childEdges = new LinkedList();
	}

	Node getNode( Word w ) {
	    Node ret = null;

	    Edge e = findNextEdge( w );
	    if( e != null ) {
		int l = e.label.matches( w );
		if( l == w.len && l <= e.label.len ) {
		    ret = e.child;
		}
		else if( l <= w.len && l == e.label.len ) {
		    ret = e.child.getNode( new Word( w, l, w.len-l ));
		}
		else { // if( l < w.len && l < e.label.len ) 
		    // no match
		    ret = null;
		}
	    }

	    return ret;
	}

	Edge findNextEdge( Word w ) {
	    Edge ret = null;
	    Iterator iter = childEdges.iterator();
	    while( iter.hasNext() ) {
		Edge e = (Edge)iter.next();
		if( e.label.matches( w ) > 0 ) {
		    // assert that there's only one matching edge.
		    // assert (ret==null);
		    ret = e;
		    break;
		}
	    }
	    return ret;
	}

	void add( Word w, Word suffix ) {
	    Edge e = findNextEdge( w );

	    if( e == null ) {
		addHelper( w, suffix );
	    }
	    else if( e.label.len < w.len ) {
		// recurse to the child of e, and add suffix of w
		Word next = new Word( w, e.label.len, w.len - e.label.len );
		e.child.add( next, suffix );
	    }
	    else {
		// do nothing. the word is already added as an implicit word
	    }
	    
	}

	private Node addHelper( Word w, Word suffix ) {
	    // todo creates a new edge and node and returns the node.
	    Node ret = new Node( suffix );
	    Edge e = new Edge( w, this, ret );
	    childEdges.add( e );
	    return ret;
	}

	int getCount() {
	    return count;
	}

	int doCount() {
	    int ret = 0;
	    
	    if( childEdges.size() == 0 ) {
		ret = 1;
	    }
	    else {
		Iterator iter = childEdges.iterator();
		while( iter.hasNext() ) {
		    Edge e = (Edge)iter.next();
		    ret += e.child.doCount();
		}
	    }

	    this.count = ret;

	    return ret;
	}

	public String toString() {
	    String ret = "{ NODE(" + count + "): ";
	    Iterator iter = childEdges.iterator();
	    while( iter.hasNext() ) {
		Edge e = (Edge)iter.next();
		ret += e.toString();
	    }
	    ret += "}";
	    return ret;
	}
    }

    class Edge {
	Word label;
	Node parent;
	Node child;

	Edge( Word label, Node parent, Node child ) {
	    this.label = label;
	    this.parent = parent;
	    this.child = child;
	    child.parentEdge = this;
	}

	Node split( Word w ) {
	    // assert: w is a prefix of label
	    // split this edge into two edges, return the newly created
	    //   node in the middle
	    
	    // Word l1 = w;
	    Word l2 = new Word( label, label.idx + w.len, label.len - w.len  );

	    Node n = new Node( new Word( child.suffix,
					 0,
					 child.suffix.len - l2.len ));
	    Edge e = new Edge( l2, n, child );
	    
	    this.label = w;
	    this.child = n;

	    return n;
	}

	public String toString() {
	    String ret = "{ EDGE: " + label.toString() + 
		":" + child.toString() + "}";

	    return ret;
	}
    }


    public static void main( String[] args ) {

	byte[] w1 = args[0].getBytes();
	byte[] w2 = args[1].getBytes();


	SuffixTree ref = new SuffixTree( w1 );

	/**
	System.out.println( "------------------------" );	
	System.out.println( ref.toString() );
	System.out.println( "------------------------" );

	System.out.println( "'" + new String( w1 ) + 
			    "' contains " + ref.getCount( w2 ) + 
			    " occurrences of '" + new String( w2 ) + "'" );
	**/

	SuffixTree x = new SuffixTree( w2 );

	SortedSet s = ref.getSurprises( x, 0, 3, (byte)255 /* phony value */ );
	
	Iterator iter = s.iterator();
	while( iter.hasNext() ) {
	    RankedObject ro = (RankedObject)iter.next();
	    System.out.println( ro.toString() );
	}


	return;
    }

}

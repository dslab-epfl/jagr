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

public class DTreeRule implements Comparable {

    String attr;
    String val;
    DTreeRule sub;

    String result;

    int coversPositive;
    int coversNegative;

    public DTreeRule( String attr, String val, String result ) {
	this.attr = attr;
	this.val = val;
	this.sub = null;
	this.result = result;
    }

    public DTreeRule( String attr, String val, DTreeRule sub ) {
	this.attr = attr;
	this.val = val;
	this.sub = sub;
	this.result = null;
	coversPositive = sub.coversPositive;
	coversNegative = sub.coversNegative;
    }

    public int getDepth() {
	if( sub == null ) {
	    return 1;
	}
	else {
	    return 1 + sub.getDepth();
	}
    }

    public String getResult() {
	if( result != null )
	    return result;
	else 
	    return sub.getResult();
    }


    public int compareTo( Object o ) {

	DTreeRule other = (DTreeRule)o;

	if( this.coversPositive > other.coversPositive ) 
	    return 1;
	//	else if( this.coversPositive == other.coversPositive )
	//    return 0;
	else
	    return -1;
    }
    
    public String toStringHelper() {
	String ret;

	ret = "(" + attr + "==" + val + ")";

	if( sub != null ) {
	    ret += " && ";
	    ret += sub.toStringHelper();
	}

	if( result != null ) {
	    ret += " THEN [" + result + "]";
	}

	return ret;
    }

    public String toString() {
	String ret = "(" + coversPositive + "): ";

	ret += toStringHelper();
       
	return ret;
    }

}

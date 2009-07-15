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

import swig.util.Debug;
import roc.pinpoint.analysis.structure.Distanceable;
import roc.pinpoint.analysis.structure.DistanceableHelper;
import roc.pinpoint.analysis.structure.UnsupportedDistanceMetricException;

/**
 * represents a boolean-valued cluster element
 * @author emrek
 *
 */
public class ClusterElement implements Distanceable, Serializable {

    int distanceCoeff = JACCARD;

    private String name;
    private boolean[] attributes;

    /**
     * simple constructor
     * @param attributes  boolean attributesof the new cluster element
     */
    public ClusterElement(boolean[] attributes) {
        this.attributes = attributes;
    }

    /**
     * @return String the name of this cluster element
     */
    public String getName() {
        return name;
    }

    /**
     * set the name of this cluster element.
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setDistanceCoeff( int coeff ) {
	this.distanceCoeff = coeff;
    }

    public int getDistanceCoeff() {
	return distanceCoeff;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "{ ClusterEl: " + name + "}";
    }

    /**
     * @return boolean[]  returns the attributes of this cluster element
     */
    public boolean[] getAttributes() {
        return attributes;
    }

    public double getDistance( int disttype, Distanceable other ) 
	throws UnsupportedDistanceMetricException {
	
	return getDistance( disttype, (ClusterElement)other );
    }

    /**
     * calculates the distance between two cluster elements using the
     * specified distance coefficient
     * @param el  cluster element to compare this to
     * @param coeff  distance coefficient to use.
     * @return  calculated distance
     */
    public double getDistance(int disttype, ClusterElement el ) 
	throws UnsupportedDistanceMetricException {

        Debug.Assert(el != null);
        //Debug.Assert( attributes.length == el.attributes.length );
        Debug.Assert((disttype >= 0) && (disttype < MAX_COEFF));

        int a = 0, b = 0, c = 0, d = 0;

        int len =
            (attributes.length > el.attributes.length)
                ? attributes.length
                : el.attributes.length;

        for (int i = 0; i < attributes.length; i++) {
            boolean j = i < attributes.length ? attributes[i] : false;
            boolean k = i < el.attributes.length ? el.attributes[i] : false;
            if (j) {
                if (k) {
                    a++;
                }
                else {
                    b++;
                }
            }
            else {
                if (k) {
                    c++;
                }
                else {
                    d++;
                }
            }
        }

        return DistanceableHelper.calculateDistance( disttype,distanceCoeff,
                                              a,b,c,d);
    }

    public double getDistance( Distanceable d ) {
	try {
	    return getDistance( distanceCoeff, (ClusterElement)d );
	}
	catch( UnsupportedDistanceMetricException e ) {
	    // should never happen.
	    throw new RuntimeException( e );
	}
    }

}

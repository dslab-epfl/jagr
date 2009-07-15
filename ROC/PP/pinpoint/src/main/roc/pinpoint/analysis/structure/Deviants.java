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

import java.util.SortedSet;

/**
 * This interface should be implemented by classes that can
 * measure a deviation from some norm.
 */
public interface Deviants {

    /**
     * returns a ranked set of objects that deviate from the
     * norm represented by the implementing class
     */
    public SortedSet getDeviants();


    /**
     * sets a threshold for the acceptable deviation allowed.
     * the meaning of this threshold may vary based on how the
     * deviation is calculated
     */
    public void setAcceptableDeviation( double dev );

    /**
     * returns true if the given object deviates from the norm
     * represented by the implementing class.
     */
    public boolean isDeviant( Object o ) throws ClassCastException;

    /**
     * returns the deviation of the given object from the norm
     * represented by the implementing class
     */
    public double getDeviation( Object o ) throws ClassCastException;

}

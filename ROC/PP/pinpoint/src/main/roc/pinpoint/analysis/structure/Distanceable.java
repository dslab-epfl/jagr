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


// marked for release 1.0
/**
 * This interface should be implemented by classes for which
 * it makes sense to measure the distance between instances.
 *
 * Several distance metrics may be supported, though not all
 * classes are required to support each.
 */
public interface Distanceable {

    /**
     * a Jaccard "distance", modified to handle vectors of real numbers
     * between 0 and 1, and not just boolean-valued vectors
     */
    public static final int DEFAULT_DISTANCE = -1;
    public static final int MODIFIED_JACCARD = 0;
    public static final int KULLBACK_LIEBLER = 1;
    public static final int JACCARD          = 2;
    public static final int SIMPLE_MATCH     = 3;
    public static final int YULE             = 4;
    public static final int HAMMAN           = 5;
    public static final int SORENSON         = 6;
    public static final int COMMON_COUNT     = 7;
    public static final int CHI_SQ           = 8;

    public static final int MAX_COEFF = 9;

    public String[] DISTANCE_NAMES = {
	"Modified Jaccard",
	"Kullback-Liebler",
	"Jaccard",
	"Simple match",
	"Yule",
	"Hamman",
	"Sorenson",
	"Common-count",
	"Chi^2"
    };

    /**
     * returns the distance between this object and the distancable
     * d, as measured by the default distancemetric.
     */
    public double getDistance( Distanceable d );

    /**
     * returns the distance between this object and the distancable
     * d, as measured by the distancemetric specified.
     */
    public double getDistance( int distancemetric, Distanceable d )
	throws UnsupportedDistanceMetricException;;

}

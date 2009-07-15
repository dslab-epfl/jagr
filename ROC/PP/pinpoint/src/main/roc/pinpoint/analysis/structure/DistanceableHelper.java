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

import swig.util.Debug;

public class DistanceableHelper {

    static public double calculateDistance( int distCoeff, 
                                     int defaultDistanceCoeff,
                                     double a_and_b, 
                                     double a_not_b, double b_not_a,
                                     double neither ) 
        throws UnsupportedDistanceMetricException {

        double distance;

        if( distCoeff == Distanceable.DEFAULT_DISTANCE ) {
            distCoeff = defaultDistanceCoeff;
        }

        //Debug.Assert((distCoeff >= 0) && (distCoeff < Distanceable.MAX_COEFF));

        if (distCoeff == Distanceable.SIMPLE_MATCH) {
            // simple matching coeff.
            distance = 1-((double) (a_and_b + neither)) / ((double) (a_and_b + a_not_b + b_not_a + neither));
        }
        else if (distCoeff == Distanceable.YULE) {
            //Yule coeff.
            distance = 1 -
                (((double) a_and_b * neither) - ((double) a_not_b * b_not_a))
                    / (((double) a_and_b * neither) + ((double) a_not_b * b_not_a));
        }
        else if (distCoeff == Distanceable.JACCARD) {
            // Jacard Similarity Coeff. -- doesn't take into account
            // 0-0 matches (d)
            distance = 1 - ((double) a_and_b) / ((double) a_and_b + a_not_b + b_not_a);
        }
        else if (distCoeff == Distanceable.HAMMAN) {
            // Hamman Coeff
            distance = 1 - ((double) a_and_b + neither - a_not_b - b_not_a) / ((double) a_and_b + neither + a_not_b + b_not_a);
        }
        else if (distCoeff == Distanceable.SORENSON) {
            // Sorenson Coeff
            distance = 1 - ((double) 2 * a_and_b) / ((double) 2 * a_and_b + a_not_b + b_not_a );
        }
        else if (distCoeff == Distanceable.COMMON_COUNT) {
            // this isn't a "real" statistical technique, but its 
            // useful for dependency analysis

            distance = a_and_b;
        }
	else {
	    throw new UnsupportedDistanceMetricException( 
		"DistanceableHelper does not support " + 
		distCoeff + ":" +
		((distCoeff >= 0) && (distCoeff < Distanceable.DISTANCE_NAMES.length )?
		 Distanceable.DISTANCE_NAMES[ distCoeff ]:"unknown distance metric"));
	}
        

        return distance;
    }

}

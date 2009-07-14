package roc.pinpoint.analysis.clustering;

import swig.util.Debug;
import roc.pinpoint.analysis.structure.Distanceable;

/**
 * represents a boolean-valued cluster element
 * @author emrek
 *
 */
public class ClusterElement implements Distanceable {

    /**
     *  simple matching distance coefficient
     */
    public static final int SIMPLE_MATCH_COEFF = 0;

    /**
     *  yule coefficient
     */
    public static final int YULE_COEFF = 1;

    /**
     * jacard coefficient
     */
    public static final int JACARD_COEFF = 2;

    /**
     * hamman coefficient
     */
    public static final int HAMMAN_COEFF = 3;

    /**
     * sorenson coefficient
     */
    public static final int SORENSON_COEFF = 4;

    /**
     * common count coefficient
     * this isn't a "real" statistical technique, but its useful for dependency
     * analysis
     */
    public static final int COMMON_COUNT_COEFF = 5;

    int distanceCoeff = JACARD_COEFF;

    /**
     * max number of distance coefficients, used to assert that
     * (distanceCoefficient < MAX_COEFF )
     */
    public static final int MAX_COEFF = 6;

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

    /**
     * calculates the distance between two cluster elements using the
     * specified distance coefficient
     * @param el  cluster element to compare this to
     * @param coeff  distance coefficient to use.
     * @return float  calculated distance
     */
    public float calculateDistance(ClusterElement el, int coeff) {
        Debug.Assert(el != null);
        //Debug.Assert( attributes.length == el.attributes.length );
        Debug.Assert((coeff >= 0) && (coeff < MAX_COEFF));

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

        float distance = -66;

        if (coeff == SIMPLE_MATCH_COEFF) {
            // simple matching coeff.
            distance = ((float) (a + d)) / ((float) (a + b + c + d));
        }
        else if (coeff == YULE_COEFF) {
            //Yule coeff.
            distance =
                (((float) a * d) - ((float) b * c))
                    / (((float) a * d) + ((float) b * c));
        }
        else if (coeff == JACARD_COEFF) {
            // Jacard Similarity Coeff. -- doesn't take into account
            // 0-0 matches (d)
            distance = ((float) a) / ((float) a + b + c);
        }
        else if (coeff == HAMMAN_COEFF) {
            // Hamman Coeff
            distance = ((float) a + d - b - c) / ((float) a + d + b + c);
        }
        else if (coeff == SORENSON_COEFF) {
            // Sorenson Coeff
            distance = ((float) 2 * a) / ((float) 2 * a + b + c);
        }
        else if (coeff == COMMON_COUNT_COEFF) {
            // this isn't a "real" statistical technique, but its 
            // useful for dependency analysis

            distance = a;
        }

        return -distance;
    }

    public double getDistance( Distanceable d ) {
	return calculateDistance( (ClusterElement)d, distanceCoeff );
    }

}

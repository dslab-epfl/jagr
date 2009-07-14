package roc.pinpoint.tracing;

/**
 * an exception while observing
 * 
 * @author emrek
 *
 */
public class ObservationException extends Exception {

    /**
     * @see java.lang.Throwable#Throwable(String)
     */
    public ObservationException(String s) {
        super(s);
    }

}

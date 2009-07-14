package roc.pinpoint.tracing;

/**
 * 
 * @author emrek@cs.stanford.edu
 *
 */
public interface ObservationSubscriber {

    /**
     * listen for an observation.  this is a blocking call, and might not
     * return if no observation is received.
     * @return Observation the next observation
     * @throws ObservationException an error occurred while reading the next
     * observation
     */
    Observation receive() throws ObservationException;

}

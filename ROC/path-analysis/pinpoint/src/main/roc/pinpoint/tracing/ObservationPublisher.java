package roc.pinpoint.tracing;

/**
 * 
 *  @author  emrek@cs.stanford.edu
 *
 */
public interface ObservationPublisher {

    /**
     * publish an observation
     * @param observation  observation to publish
     * @throws ObservationException an exception occurred while publishing
     */
    void send(Observation observation) throws ObservationException;

}

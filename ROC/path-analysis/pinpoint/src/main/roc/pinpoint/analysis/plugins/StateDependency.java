package roc.pinpoint.analysis.plugins;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * represents the state dependencies of a class of requests
 * @author emrek
 *
 */
public class StateDependency implements Serializable {

    private String requestClassifier;

    private long totalOccurences;

    // the dependentState maps a state id (such as a database table) to
    //   the number of times the given request type depended on this state.
    //   To calculate how the percentage of requests of this type which
    //   use a particular table, divide by the totalOccurrences of the
    //   request type.
    private Map writeDependentState;
    private Map readDependentState;

    /**
     * constructor
     * @param requestClassifier the id of a class of requests
     */
    public StateDependency(String requestClassifier) {
        this.requestClassifier = requestClassifier;
        writeDependentState = new HashMap();
        readDependentState = new HashMap();
        totalOccurences = 0;
    }

    /**
     * increment the number of times this class of request has been observed
     */
    public void incrementTotalOccurences() {
        totalOccurences++;
    }

    /**
     * 
     * @return long total number of times this class of request has been
     * observed
     */
    public long getTotalOccurences() {
        return totalOccurences;
    }

 
    /**
      * @param read  get read accesses if true, write if false.
      * @return Map the names of the state this request class has accessed,
      * mapped to an Integer, the number of times the state has been read or
      * written
      */
    private Map getStateMap(boolean read) {
        if (read) {
            return readDependentState;
        }
        else {
            return writeDependentState;
        }
    }


    /**
     * @param read get read accesses if true, or write accesses if false
     * @return Set returns a set of names of the state read or written by this
     * class of requests.
     */
    public synchronized Set getStateAccessed(boolean read) {
        return getStateMap(read).keySet();
    }

    /**
     * increments the number of times this class of requests has read or written
     * a piece of state
     * @param stateId id of the state read or written
     * @param read true if this is a read request, false otherwise
     */
    public synchronized void incrementStateAccess(
        String stateId,
        boolean read) {
        long l = getNumTimesStateAccessed(stateId, read);
        l++;
        System.err.println("incstateacces: " + stateId);
        getStateMap(read).put(stateId, new Long(l));
    }

    /**
     * 
     * @param stateId state name
     * @param read true to get read accesses, false for write accesses
     * @return long count of the number of times this request class has accessed
     * this state
     */
    public synchronized long getNumTimesStateAccessed(
        String stateId,
        boolean read) {
        Long l = (Long) getStateMap(read).get(stateId);
        return (l == null) ? 0 : ((Long) l).longValue();
    }

    /**
     * returns the number of times state has been accessed, normalized by the
     * total number of requests of this class.
     * @param stateId state name
     * @param read   true to get read accesses, false for write accesses
     * @return double the normalized count of the number of times this request
     * class has accessed this state
     */
    public double getNormalizedStateAccess(String stateId, boolean read) {
        return ((double) getNumTimesStateAccessed(stateId, read))
            / ((double) totalOccurences);
    }

    /**
     * 
     * @see java.lang.Object#toString()
     */
    public synchronized String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("StateDependency: [");
        buf.append("\n\trequestClassifer = ");
        buf.append(requestClassifier);
        buf.append("\n\ttotalOccurences = ");
        buf.append(Long.toString(totalOccurences));

        buf.append("\n\tWrites = ");
        buf.append(writeDependentState.toString());
        buf.append("\n\tReads = ");
        buf.append(readDependentState.toString());
        buf.append("\n]");
        return buf.toString();
    }

}

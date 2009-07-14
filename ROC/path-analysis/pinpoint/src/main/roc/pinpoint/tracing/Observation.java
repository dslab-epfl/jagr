package roc.pinpoint.tracing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * this class represents a single observation, made as a request accesses a
 * resource or uses a component
 * @author emrek
 *
 */
public class Observation implements Serializable, Comparable {

    /* types of events */
    
    /** NULL event, means this observation should be ignored by most tools. **/
    public static final int EVENT_NULL = 0;
    /** a component was used **/
    public static final int EVENT_COMPONENT_USE = 1;
    /** a database access **/
    public static final int EVENT_DATABASE_USE = 2;
    /** a likely error was observed **/
    public static final int EVENT_ERROR = 3;
    /** component details **/
    public static final int EVENT_COMPONENT_DETAILS = 4;
    // more observation events to come later, i'm sure...

    /** text description of event types, for toString() **/
    public static final String[] TEXT_EVENT_DESCR =
        { "null", "component use", "database use", "error", 
	  "component details" };

    public int eventType;
    public String requestId; // id of request we are observing (if applicable)
    public int sequenceNum;

    public long originTimestamp; // time (at origin) event was observed
    public long collectedTimestamp; // time (at central observer)

    public Map originInfo;
    /* component name, method name, args, etc. of origin of observation */

    public Map rawDetails;
    /* unparsed details, such as a low-level SQL query, stack trace, etc. */

    public Map attributes; // other attributes of observation

    /**
     * default constructor 
     * @see java.lang.Object#Object()
     */
    public Observation() {
        eventType = EVENT_NULL;

        originInfo = new HashMap();
        rawDetails = new HashMap();
        attributes = new HashMap();
    }

    /**
     * constructor
     * @param eventType eventype
     * @param requestId request id
     * @param sequenceNum sequence number
     * @param originInfo information about where this observation was collected
     * @param rawDetails raw details, such as sql statements, or stack traces
     * @param attributes attributes of the observation
     */
    public Observation(
        int eventType,
        String requestId,
        int sequenceNum,
        Map originInfo,
        Map rawDetails,
        Map attributes) {
        this.eventType = eventType;
        this.requestId = requestId;
        this.sequenceNum = sequenceNum;
        this.originTimestamp = System.currentTimeMillis();
        this.collectedTimestamp = -1;
        this.originInfo = originInfo;
        this.rawDetails = rawDetails;
        if (this.rawDetails == null) {
            this.rawDetails = new HashMap();
        }
        else if (!(this.rawDetails instanceof HashMap)) {
            this.rawDetails = new HashMap(this.rawDetails);
        }
        this.attributes = attributes;
        if (this.attributes == null) {
            this.attributes = new HashMap();
        }
        else if (!(this.attributes instanceof HashMap)) {
            this.attributes = new HashMap(this.attributes);
        }
    }

    /**
     * @see roc.pinpoint.tracing.Observation#Observation(int,
     *  String,    int, Map, Map, Map )
     */
    public Observation(
        int eventType,
        RequestInfo reqinfo,
        Map originInfo,
        Map rawDetails,
        Map attributes) {
        this(
            eventType,
            reqinfo.getRequestId(),
            reqinfo.getSeqNum(),
            originInfo,
            rawDetails,
            attributes);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Observation: [\n");
        buf.append("\teventType = ");
        buf.append(TEXT_EVENT_DESCR[eventType]);
        buf.append("\n\trequestId = ");
        buf.append(requestId);
        buf.append("\n\tsequenceNum = ");
        buf.append(sequenceNum);
        buf.append("\n\toriginTimestamp = ");
        buf.append(originTimestamp);
        buf.append("\n\tcollectedTimestamp = ");
        buf.append(collectedTimestamp);
        buf.append("\n\toriginInfo = ");
        buf.append(originInfo);
        buf.append("\n\trawDetails = ");
        buf.append(rawDetails);
        buf.append("\n\tattributes = ");
        buf.append(attributes);
        buf.append("\n]");

        return buf.toString();
    }

    /**
     * compare/sort based on sequence number of observation
     * @see java.lang.Comparable#compareTo(Object)
     */
    public int compareTo(Object o) {

        if (!(o instanceof Observation)) {
            throw new ClassCastException(
                "Cannot compare Observation to " + o.getClass().toString());
        }

        Observation obs = (Observation) o;

        if (!obs.requestId.equals( this.requestId )) {
	    System.err.println( "THIS=" + this.toString() );
	    System.err.println( "OTHER=" + o.toString() );
            throw new ClassCastException("Cannot compare Observations" 
   + " with different request ids!");
        }

        return (this.sequenceNum < obs.sequenceNum)
            ? -1
            : ((this.sequenceNum > obs.sequenceNum) ? 1 : 0);
    }

}

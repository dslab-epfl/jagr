package roc.pinpoint.tracing;

import java.io.Serializable;
import java.util.Random;

/**
 * Id and properties associated with a request.  Used inside the system being
 * traced.
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">
 *               mikechen@cs. berkeley.edu</A>)
 * @version $Id: RequestInfo.java,v 1.2 2002/12/28 12:27:30 emrek Exp $
 */

public class RequestInfo implements Serializable {

    private static Random rand = new Random();

    private String requestId;
    private int seqNum = -1;

    /**
     * generates a random request id
     * @see java.lang.Object#Object()
     */
    public RequestInfo() {
        this(System.currentTimeMillis() + "_" + Math.abs(rand.nextInt()));
    }

    /**
     * @param id creates a new requestinfo with the given id
     */
    public RequestInfo(String id) {
        this.requestId = id;
    }

    /**
     * returns the id of this request
     * @return String
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * set the id of this request
     * @param requestId requestid
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * set the sequence number
     * @param seqNum sequence number
     */
    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }
    
    /**
     * increment the sequence number. should be incremented each time we publish
     * an observation.
     * @return int the current sequence number, after incrementing.
     */
    public int incrementSeqNum() {
        seqNum++;
        return seqNum;
    }

    /**
     * @return int the current sequence number
     */
    public int getSeqNum() {
        return seqNum;
    }

}

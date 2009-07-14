package tracing;

import java.util.*;

/**
 * A ThreadLocal implementation of a RequestMarker. The marker is local
 * to the current thread so that it's preserved across software components.
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @version $Id: ThreadLocalRequestMarker.java,v 1.3 2002/11/26 00:07:01 mikechen Exp $
 */ 

public class ThreadLocalRequestMarker {

    // a variable local to the current thread
    private static ThreadLocal threadLocalMarker = new ThreadLocal();
    
    protected static RequestMarker getMarker() {
	RequestMarker marker = (RequestMarker)threadLocalMarker.get();
	if (marker == null) {
	    marker = new RequestMarker();
	    threadLocalMarker.set(marker);
	}
	return marker;
    }

    /**
     * Returns the request ID.
     */

    public static String getRequestId() {
	RequestMarker marker = getMarker();
	return marker.getRequestId();
    }

    /**
     * Sets the request ID.
     */

    public static void setRequestId(String id) {
	RequestMarker marker = getMarker();
	marker.setRequestId(id);
    }

    /**
     * Sets the sequence number.
     */

    public static void setSeqNum(int seqNum) {
	RequestMarker marker = getMarker();
	marker.setSeqNum(seqNum);
    }

    /**
     * Returns the sequence number.
     */

    public static int getSeqNum() {
	RequestMarker marker = getMarker();
	return marker.getSeqNum();
    }

    /**
     * Increment the sequence number.
     */

    public static int incrementSeqNum() {
	RequestMarker marker = getMarker();
	return marker.incrementSeqNum();
    }

    public static Map getRequestMap() {
	RequestMarker marker = getMarker();
	return marker.getRequestMap();
    }

    public static List getRequestList() {
	RequestMarker marker = getMarker();
	return marker.getRequestList();
    }


    
}







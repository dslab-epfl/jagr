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
package roc.pinpoint.tracing;

// marked for release 1.0

import java.io.Serializable;
import java.util.Random;

/**
 * ID and properties associated with a request.  Used inside the system being
 * traced to track requests.
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">
 *               mikechen@cs. berkeley.edu</A>)
 * @version $Id: RequestInfo.java,v 1.7 2004/05/10 23:42:13 emrek Exp $
 */

public class RequestInfo implements Serializable {

    private static Random rand = new Random();

    private String requestId;
    private int seqNum = -1;

    private boolean debug = false;

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

    public String toString() { 
	StringBuffer buf = new StringBuffer();
	buf.append("RequestInfo: [" );
	buf.append(" requestId=" ).append( requestId );
	buf.append(", seqNum=" ).append( Integer.toString(seqNum ));
	buf.append("]");
	return buf.toString();
    }

}

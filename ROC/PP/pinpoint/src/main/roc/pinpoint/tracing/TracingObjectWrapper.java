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

/**
 *  This class is used for wrapping arbitrary return values together
 *  with a request's sequence number, through existing "generic" APIs.
 *  E.g., in JBoss's org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxy
 *
 *
 */
public class TracingObjectWrapper implements Serializable {

    Object obj;
    int seqnum;

    public TracingObjectWrapper( Object obj, int seqnum ) {
	this.obj = obj;
	this.seqnum = seqnum;
    }

    public int getRequestSeqNum() {
	return seqnum;
    }

    public Object get() {
	return obj;
    }

}

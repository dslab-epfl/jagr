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
package roc.pinpoint.loadgen;

import java.util.List;

import roc.loadgen.Request;


public class PPRequest extends Request {

    List observations;

    public PPRequest( List observations ) {
	this.observations = observations;
    }

    public List getObservations() {
	return observations;
    }


    /**
     * toString method: creates a String representation of the object
     * @return the String representation
     * @author info.vancauwenberge.tostring plugin
    
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("PPRequest[");
        buffer.append("observations = ").append(observations);
        buffer.append("]");
        return buffer.toString();
    }
}


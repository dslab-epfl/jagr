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

import roc.loadgen.*;

public class PPResponse implements Response {

    final static public PPResponse EMPTY_RESPONSE = new PPResponse();
    
    public boolean isOK() {
	return true;
    }

    public boolean isError() {
	return false;
    }

    public Throwable getThrowable() {
	return null;
    }

    /**
     * toString methode: creates a String representation of the object
     * @return the String representation
     * @author info.vancauwenberge.tostring plugin
    
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("PPResponse[");
        buffer.append("]");
        return buffer.toString();
    }
}

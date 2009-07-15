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


import roc.loadgen.AbortSessionException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.Response;
import roc.loadgen.Session;
import roc.pinpoint.loadgen.model.Site;

import java.io.File;
import java.util.Arrays;
public class PPSession extends Session {

    public static final String ARG_FILENAME = "filename";
    public static final String ARG_SLEEPMS = "sleepms";
    
    Arg[] argDefinitions = { 
		new Arg( ARG_FILENAME, 
		         "name of the sitemodel to load",
		         Arg.ARG_STRING,
		         true,
		         null ),
		new Arg( ARG_SLEEPMS,
			 "how long to sleep between requests",
			 Arg.ARG_INTEGER,
			 true,
			 null )
    };

    private Site site;
    private int sleepms;

    public Arg[] getArguments() {
	return argDefinitions;
    }

    public void start() throws AbortSessionException {

        try {
            site = new Site(new File((String)args.get(ARG_FILENAME)));
        }
        catch (Exception e) {
            throw new AbortSessionException("Could not initialize site model", e);
        }
	sleepms = ((Integer)args.get(ARG_SLEEPMS)).intValue();
    }

    public void resetSession() {
	// NO-OP
    }

    public Request getNextRequest() throws AbortSessionException {
	try {
	    Thread.sleep(sleepms);
	}
	catch( InterruptedException ignore ) {
	    ignore.printStackTrace();
	}
	return new PPRequest(site.getRandomRequestPath());
    }

    public void processResponse( Request req, Response resp ) 
	throws AbortSessionException {
        // NO-OP
    }

    /**
     * toString methode: creates a String representation of the object
     * @return the String representation
     * @author info.vancauwenberge.tostring plugin
    
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("PPSession[");
        buffer.append("ARG_FILENAME = ").append(ARG_FILENAME);
        if (argDefinitions == null) {
            buffer.append(", argDefinitions = ").append("null");
        }
        else {
            buffer.append(", argDefinitions = ").append(
                Arrays.asList(argDefinitions).toString());
        }
        buffer.append("]");
        return buffer.toString();
    }
}

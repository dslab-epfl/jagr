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

/**
 * This class is a convenience class for allow trace points to easily
 * report their observations back to a central location over a pluggable
 * transport protocol.  This class provides static methods for publishing
 * observations and is responsible for loading the appropriate protocol
 * plugin (as defined in an Java Property "roc.pinpoint.tracing.Publisher")
 * The plugin may also depend on other java Properties set.  See the
 * comments for the specific publisher.
 *
 */
public class GlobalObservationPublisher {

    static Object lock = new Object();
    static ObservationPublisher publisher;

    static boolean reportedProblem = false;

    // don't instantiate
    private GlobalObservationPublisher() {
    }

    /**
     * This function initializes the publisher.  It will be called
     * automatically by Send() if the publisher has not already been
     * initialized, so there is no requirement to call it directly.
     */
    public static final boolean InitPublisher() {

	synchronized( lock ) {
	    if( publisher != null )
		return true;

	String classname = System.getProperty( "roc.pinpoint.tracing.Publisher" );

	if( classname == null ) {
	    if( !reportedProblem ) {
		System.err.println( "GlobalObservationPublsher: no publisher classname initialized: Pinpoint observations will be DROPPED!" );
		reportedProblem = true;
	    }
	    return false;
	}

	try {
	    Class sInterface = 
		Class.forName( "roc.pinpoint.tracing.ObservationPublisher" );
	    Class sClass = Class.forName( classname );
	    
	    if( !(sInterface.isAssignableFrom( sClass ))) {
		if( !reportedProblem ) {
		    System.err.println( "GlobalObservationPublisher: " +
					classname + " not a publisher!" );
		    reportedProblem = true;
		}
	    }
	    else {
		publisher = (ObservationPublisher) sClass.newInstance();
		return true;
	    }
	}
	catch( ClassNotFoundException cnfe ) {
	    if( !reportedProblem ) {
		System.err.println("GlobalObservationPublisher: Class " + 
				   classname + " not found");
		reportedProblem = true;
	    }
        }
        catch (IllegalAccessException iae) {
	    if( !reportedProblem ) {
		System.err.println("GlobalObservationPublisher: " + 
				   "Illegal Access: " + classname);
		reportedProblem = true;
	    }
        }
        catch (InstantiationException ie) {
	    if( !reportedProblem ) {
		System.err.println( "GlobalObservationPublisher: " + 
				    "Could not instantiate subscriber " +
				    classname);
		reportedProblem = true;
	    }
        }

	}

	return false;
    }

    /**
     * Sends an observation using the publisher defined in the java
     * property "roc.pinpoint.tracing.Publisher"
     */
    public static final void Send( Observation obs ) {

	if(( publisher == null ) && 
	   (!InitPublisher())){
	    //System.err.println( "GlobalObservationPublisher: COULD NOT INIT PUBLISHER, DROPPPING " + obs );
	    // couldn't initialize publisher 
	   return;
	}
	
	try {
	    publisher.send( obs );
	}
	catch( ObservationException e ) {
	    e.printStackTrace();
	    //System.err.println( "Could not send PINPOINT Observation (continuing...):" + e.getMessage() );
	}
    }
    

}

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

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import roc.pinpoint.tracing.java.EnvironmentDetails;

/**
 * 
 *
 *
 */
public class TracingHelper {

    public static String TYPE_EJB="EJB";
    public static String TYPE_URL="URL";
    public static String TYPE_JNDI="JNDI";
    public static String TYPE_SERVLET = "SERVLET";
    public static String TYPE_JSP="JSP";
    public static String TYPE_JMS="JMS";
    public static String TYPE_DB="DB";
    public static String TYPE_JSPATTR="jspattr";


    /**
     *  Creates and publishes an observation that a method is being called
     *  @param obsloc name of the observation point (not the component name)
     *  @param type type of the component which generated exception
     *  @param clazz name of class being entered
     *  @param method name of method being called
     *  @param arg interesting argument to be recorded
     */
    public static Map ReportMethodBegin( String obs, String type, 
					 String clazz, String method,
					 String arg ) {
	return ReportMethod( obs, type, clazz, method, arg, true );
    }

    /**
     *  Creates and publishes an observation that a method call is returning
     *  @param obsloc name of the observation point (not the component name)
     *  @param type type of the component which generated exception
     *  @param clazz name of class being entered
     *  @param method name of method being called
     *  @param arg interesting argument to be recorded
     */
    public static Map ReportMethodEnd( String obs, String type, 
				       String clazz, String method, 
				       String arg ) {
	return ReportMethod( obs, type, clazz, method, arg, false );
    }


    /**
     *  Creates and publishes an observation that a method is being called
     *  @param obsloc name of the observation point (not the component name)
     *  @param type type of the component which generated exception
     *  @param clazz name of class being entered
     *  @param method name of method being called
     */
    public static Map ReportMethodBegin( String obs, String type,
					 String clazz, String method ) {
	return ReportMethod( obs, type, clazz, method, null, true );
    }

    /**
     *  Creates and publishes an observation that a method call is returning
     *  @param obsloc name of the observation point (not the component name)
     *  @param type type of the component which generated exception
     *  @param clazz name of class being entered
     *  @param method name of method being called
     */
    public static Map ReportMethodEnd( String obs, String type,
				       String clazz, String method ) {
	return ReportMethod( obs, type, clazz, method, null, false );
    }


    private static List ExtractStackTrace( Throwable t ) {
	StackTraceElement[] ste = t.getStackTrace();
	java.util.List ret =
	    new java.util.ArrayList( ste.length );
	
	for( int i=0; i < ste.length; i++ ) {
	    // todo, later, we might want a more structure storage 
	    //       of the details in ste[ i ], rather than
	    //       just a toString() dump.
	    ret.add( ste[ i ].toString() );
	}

	return ret;
    }

    /**
     *  Creates and publishes an observation about an exception.
     *  @param obsloc name of the observation point (not the component name)
     *  @param type type of the component which generated exception
     *  @param t    the exception being thrown
     */
    public static void ReportException( String obsloc,
					String type,
					Throwable t ) {

	Map originInfo = EnvironmentDetails.GenerateOriginInfo( type, 
								null,
								null );
	Map attributes = (HashMap)SimpleObjectPool.HASHMAP_POOL.get();
	attributes.put( "observationlocation", obsloc );

	Map rawDetails = null;
	if( t != null ) {
	    rawDetails = (HashMap)SimpleObjectPool.HASHMAP_POOL.get();
	    rawDetails.put( "exception", t.toString() );
	    rawDetails.put( "stacktrace", ExtractStackTrace( t ));
	}

	Report( Observation.EVENT_ERROR, originInfo, attributes, rawDetails );
    }


    /**
     *  Creates an publishes an observation that a request for a particular
     *  URL has begun/ended.  This need not be the first observation for this 
     *  request.  Optionally, also reports a classification for this
     *  request.
     *
     *  @param obsloc name of the observation point (not the component name)
     *  @param url the url being accessed
     *  @param requestclassifier  classification of request, may be null
     *  @param isBegin true if this is the beginning of a request, false if it is the end.
     *
     */
    public static void ReportURL( String obsloc,
				  String url,
				  String requestclassifier,
				  boolean isBegin ) {

       Map originInfo = EnvironmentDetails.GenerateOriginInfo( TYPE_URL,
							       url,
							       null );
       if( requestclassifier != null ) {
	   originInfo.put( "requestclassifier", requestclassifier );
       }

       Map attributes = (HashMap)SimpleObjectPool.HASHMAP_POOL.get();
       attributes.put( "observationlocation", obsloc );
       attributes.put( "stage", isBegin?"METHODCALLBEGIN":"METHODCALLEND" );

       Report( Observation.EVENT_COMPONENT_USE, originInfo, attributes, null );
    }

    /**
     *  General function for reporting beginning/ending of a method
     *  @param obsloc name of the observation point (not the component name)
     *  @param type type of the component which generated exception
     *  @param clazz name of class being entered
     *  @param method name of method being called
     *  @param arg interesting argument to be recorded
     *  @param isBegin true if this is the beginning of a call, false if it is the end.
     */
    public static Map ReportMethod( String obsloc,
				    String type, 
				    String clazz, String method,
				    String arg,
				    boolean isBegin ) {
	
       Map originInfo = EnvironmentDetails.GenerateOriginInfo( type,
							       clazz,
							       method );
       if( arg != null ) {
	   originInfo.put( "arg", arg );
       }

       Map attributes = (HashMap)SimpleObjectPool.HASHMAP_POOL.get();
       attributes.put( "observationlocation", obsloc );
       attributes.put( "stage", isBegin?"METHODCALLBEGIN":"METHODCALLEND" );

       Report( Observation.EVENT_COMPONENT_USE, originInfo, attributes, null );

       return Collections.unmodifiableMap( originInfo );
    }


    
    /**
     *  General function for reporting atomic component calls.  e.g., where
     *  the internals of the call are opaque, and we can only report that
     *  the call was made, not when it began/ended.
     *  @param obsloc name of the observation point (not the component name)
     *  @param type type of the component which generated exception
     *  @param clazz name of class being entered
     */
    public static Map ReportAtomicMethod( String obsloc,
					  String type, 
					  String clazz ) {
	
       Map originInfo = EnvironmentDetails.GenerateOriginInfo( type,
							       clazz,
							       null );
       Map attributes = (HashMap)SimpleObjectPool.HASHMAP_POOL.get();
       attributes.put( "observationlocation", obsloc );
       attributes.put( "stage", "METHODCALLATOMIC" );

       Report( Observation.EVENT_COMPONENT_USE, originInfo, attributes, null );

       return Collections.unmodifiableMap( originInfo );
    }


    private static void Report( int eventType, Map originInfo,
				Map attributes, Map rawDetails ) {
	RequestInfo reqInfo = ThreadedRequestTracer.getRequestInfo();
	reqInfo.incrementSeqNum();

	Observation obs =
	    new Observation( eventType, 
			     reqInfo,
			     originInfo,
			     rawDetails,
			     attributes );
	
	GlobalObservationPublisher.Send( obs );
    }


}

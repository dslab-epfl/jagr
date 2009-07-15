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
/*
 * Created on Feb 18, 2004
 * 
 * To change the template for this generated file go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
package roc.pinpoint.loadgen;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import roc.loadgen.AbortRequestException;
import roc.loadgen.AbortSessionException;
import roc.loadgen.Arg;
import roc.loadgen.Request;
import roc.loadgen.RequestInterceptor;
import roc.loadgen.Response;
import roc.pinpoint.tracing.Observation;
import roc.pinpoint.tracing.ObservationException;
import roc.pinpoint.tracing.ObservationPublisher;

/**
 * @author emrek
 * 
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DoPPInterceptor extends RequestInterceptor {

    public static final String ARG_SERVER_NAME = "servers";
    public static final String ARG_SERVER_PORT = "port";

    private Class publisherClass;
    private ArrayList publishers;
    private static int currPublisherIndex;
    
    Arg[] argDefinitions =
        {
            new Arg(
                ARG_SERVER_NAME,
                "name of the server to generate load against",
                Arg.ARG_LIST,
                true,
                null),
            new Arg(
                ARG_SERVER_PORT,
                "port number to connect to on the servers",
                Arg.ARG_INTEGER,
                true,
                null)};

    /*
     * (non-Javadoc)
     * 
     * @see roc.loadgen.RequestInterceptor#getArguments()
     */
    public Arg[] getArguments() {
        return argDefinitions;
    }

    /* (non-Javadoc)
     * @see roc.loadgen.RequestInterceptor#start()
     */
    public void start() throws AbortSessionException {
        super.start();
        List l = (List)args.get(ARG_SERVER_NAME);
        int port = ((Integer)args.get(ARG_SERVER_PORT)).intValue();

	InitPublisherClass();
        publishers = new ArrayList(l.size());
        
        Iterator iter = l.iterator();
        while (iter.hasNext()) {
            String server = (String) iter.next();
            publishers.add(createPublisher(server,port));
        }
        
        currPublisherIndex = 0;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see roc.loadgen.RequestInterceptor#invoke(roc.loadgen.Request)
     */
    public Response invoke(Request req)
        throws AbortRequestException, AbortSessionException {

        List observations = ((PPRequest)req).getObservations();
        Iterator iter = observations.iterator();
        while (iter.hasNext()) {
            Observation obs = (Observation) iter.next();

            ObservationPublisher pub = (ObservationPublisher)publishers.get(currPublisherIndex);
            currPublisherIndex++;
            if(currPublisherIndex >= publishers.size())
                currPublisherIndex=0;

            try {
                pub.send(obs);
            }
            catch( ObservationException e) {
                throw new AbortSessionException(e);
            }
        }
        
        return PPResponse.EMPTY_RESPONSE;
    }

    private ObservationPublisher createPublisher(String hostname, int port ) throws AbortSessionException {
        try {
            Class[] argtypes = null;
            argtypes = new Class[] { 
		java.lang.String.class,
		java.lang.Integer.TYPE };
            
            Object[] initargs = new Object[2];
            initargs[0] = hostname;
            initargs[1] = new Integer( port );
            ObservationPublisher ret = (ObservationPublisher)publisherClass.getConstructor(argtypes).newInstance(initargs);
            return ret;
        }
        catch(NoSuchMethodException nsm ) {
            throw new AbortSessionException("could not find appropriate constructor for class :"+publisherClass.getName(),
                    nsm);
        }
        catch(InvocationTargetException ite ) {
            throw new AbortSessionException("problem creating observationpublisher:"+publisherClass.getName(),
                    ite);
        }
        catch (IllegalAccessException iae) {
            throw new AbortSessionException("Illegal Access: " + publisherClass.getName(),
                    iae);
        }
        catch (InstantiationException ie) {
            throw new AbortSessionException("Could not instantiate subscriber " + publisherClass.getName(),
                    ie);
        }
    }
    
    /**
     * This function initializes the publisher.  It will be called
     * automatically by Send() if the publisher has not already been
     * initialized, so there is no requirement to call it directly.
     */
    private void InitPublisherClass() throws AbortSessionException {
        String classname = System.getProperty( "roc.pinpoint.tracing.Publisher" );

        try {
            Class sInterface = 
                Class.forName( "roc.pinpoint.tracing.ObservationPublisher" );
            Class sClass = Class.forName( classname );
            
            if( !(sInterface.isAssignableFrom( sClass ))) {
                System.err.println( classname + " not a publisher!" );
            }
            else {
                publisherClass = sClass;
            }
        }
        catch( ClassNotFoundException cnfe ) {
            throw new AbortSessionException("Class " + classname + " not found", cnfe);
        }
    }
    
    
}

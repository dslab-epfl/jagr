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
package roc.jboss.pinpoint;

// marked for release 1.0

import java.util.Map;
import java.rmi.ServerException;

import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.ejb.Container;
import org.jboss.metadata.BeanMetaData;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;

import javax.ejb.EJBException;

import roc.pinpoint.tracing.RequestInfo;
import roc.pinpoint.tracing.ThreadedRequestTracer;
import roc.pinpoint.tracing.TracingHelper;
import roc.pinpoint.tracing.java.EnvironmentDetails;

public class TracingInterceptor extends AbstractInterceptor {

    protected String ejbName;
    protected Container container;

    public void setContainer(Container container) {
        this.container = container;
    }

    public Container getContainer() {
        return container;
    }

    public void create() throws Exception {
        super.start();
	
        BeanMetaData md = getContainer().getBeanMetaData();
        ejbName = md.getEjbName();

	//ejbName = container.getServiceName().toString();

	//ejbName = container.getServiceName().getKeyPropertyListString();

        //ejbName = container.getJmxName();
    }

    /**
     * this method reports a method call begin observation, calls the
     * next invoker, then reports a method call end observation.
     */
    public Object invokeHome(Invocation invocation) throws Exception {

        String methodName;
        if (invocation.getMethod() != null) {
            methodName = invocation.getMethod().getName();
        }
        else {
            methodName = "<no method>";
        }

        boolean cleanup = setupRequestInfoIfNeeded();
        reportPinpointMethodObservation(methodName, true);

        Object ret;

        try {
            // call the next interceptor
            ret = getNext().invokeHome(invocation);
        }
        catch (Throwable e) {
            reportPinpointErrorObservation(e, methodName );
            throw handleException(e, invocation);
        }
        finally {
            reportPinpointMethodObservation(methodName, false);
            cleanupRequestInfoIfNeeded(cleanup);
        }

        return ret;
    }

    /**
     * this method reports a method call begin observation, calls the
     * next invoker, then reports a method call end observation.
     */
    public Object invoke(Invocation invocation) throws Exception {

        String methodName;
        if (invocation.getMethod() != null) {
            methodName = invocation.getMethod().getName();
        }
        else {
            methodName = "<no method>";
        }

        boolean cleanup = setupRequestInfoIfNeeded();
        reportPinpointMethodObservation(methodName, true);

        Object ret;

        try {
            // call the next interceptor
            ret = getNext().invoke(invocation);
        }
        catch (Throwable e) {
            reportPinpointErrorObservation(e, methodName);
            throw handleException(e, invocation);
        }
        finally {
            reportPinpointMethodObservation(methodName, false);
            cleanupRequestInfoIfNeeded(cleanup);
        }

        return ret;
    }

	Map generateAttributes() {
		Map ret = new java.util.HashMap();
		ret.put("observationLocation", "roc.jboss.pinpoint" );
		return ret;
	}

    Map generateAttributes(boolean isBegin) {
		Map ret = generateAttributes();
        ret.put("stage", (isBegin) ? "METHODCALLBEGIN" : "METHODCALLEND");
        return ret;
    }

    public void reportPinpointMethodObservation(
        String methodName,
        boolean isBegin) {

        Map PP_originInfo = EnvironmentDetails.GenerateOriginInfo( TracingHelper.TYPE_EJB, ejbName, methodName);

        RequestInfo PP_reqInfo = ThreadedRequestTracer.getRequestInfo();
        PP_reqInfo.incrementSeqNum();

        Map PP_attributes = generateAttributes(isBegin);

        roc.pinpoint.tracing.Observation PP_obs =
            new roc.pinpoint.tracing.Observation(
                roc.pinpoint.tracing.Observation.EVENT_COMPONENT_USE,
                PP_reqInfo,
                PP_originInfo,
                null,
                PP_attributes);

        roc.pinpoint.tracing.GlobalObservationPublisher.Send(PP_obs);
    }

    public void reportPinpointErrorObservation(
        Throwable e,
        String methodName ) {

        //      report details of the exception, this is a likely failure
        roc.pinpoint.tracing.RequestInfo PP_reqInfo =
            roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo();
        PP_reqInfo.incrementSeqNum();

        java.util.Map PP_rawDetails = new java.util.HashMap();
        StackTraceElement[] PP_ste = e.getStackTrace();
        java.util.List PP_stacktrace = new java.util.ArrayList(PP_ste.length);
        for (int PP_i = 0; PP_i < PP_ste.length; PP_i++) {

            PP_stacktrace.add(PP_ste[PP_i].toString());
        }
        PP_rawDetails.put("exception", e.toString());
        PP_rawDetails.put("stacktrace", PP_stacktrace);

		Map PP_originInfo = EnvironmentDetails.GenerateOriginInfo( TracingHelper.TYPE_EJB, ejbName, methodName );
		Map PP_attributes = generateAttributes();

        roc.pinpoint.tracing.Observation PP_obs =
            new roc.pinpoint.tracing.Observation(
                roc.pinpoint.tracing.Observation.EVENT_ERROR,
                PP_reqInfo,
                PP_originInfo,
                PP_rawDetails,
                PP_attributes);
        roc.pinpoint.tracing.GlobalObservationPublisher.Send(PP_obs);

    }


	/*
	 * modeled after handleException in LogInterceptor; this is a bit of a pain!
	 */
    private Exception handleException(Throwable t, Invocation invocation) {

        InvocationType type = invocation.getType();
        boolean isLocal =
            type == InvocationType.LOCAL || type == InvocationType.LOCALHOME;

        if (t instanceof Exception) {
            return (Exception)t;
        }
        else if (t instanceof Error) {

	    Exception e = new Exception( t );
            Exception ret;
            if (isLocal) {
                ret = new EJBException("Unexpected Error", e );
            }
            else {
                ret = new ServerException("Unexpected Error", e);
            }
            return ret;
        }
        else {
	    Exception e = new Exception( t );
            Exception ret;
            if (isLocal) {
                ret = new EJBException("Unexpected Throwable", e);
            }
            else {
                ret = new ServerException("Unexpected Throwable", e);
            }
            return ret;
        }
    }

    private boolean setupRequestInfoIfNeeded() {
        RequestInfo reqInfo = ThreadedRequestTracer.getRequestInfo();
        if( reqInfo.getSeqNum() == -1 ) {
            // System.err.println("EMKDEBUG: request "+ reqInfo.getRequestId() +" first entered JBoss through EJB.  Will clean up later");
            return true;
        }
        return false;
    }
 
    private void cleanupRequestInfoIfNeeded( boolean cleanup ) {
        if(cleanup) {
            ThreadedRequestTracer.setRequestInfo(null);
        }
    }
}

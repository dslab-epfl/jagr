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
package roc.jboss.faultinjector;

// marked for release 1.0

import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;


import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.ejb.Container;
import org.jboss.metadata.BeanMetaData;
import org.jboss.invocation.Invocation;

import roc.pinpoint.tracing.java.EnvironmentDetails;
import roc.pinpoint.injection.FaultGenerator;
import roc.pinpoint.injection.FaultTrigger;

public class FaultInjectingInterceptor extends AbstractInterceptor {

    protected String ejbName;
    protected Container container;

    protected Map stuckresults;

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
    }

    public Object invokeHome(Invocation invocation) throws Exception {

        Object ret = null;

	ReturnInstruction ri = checkFaultInjection( invocation, true, null, null );
        if (ri.enabled ) {
            return ri.ret;
        }

        // call the next interceptor
        ret = getNext().invokeHome(invocation);

	ri = checkFaultInjection( invocation, false, ri, ret );
        if (ri.enabled) {
            return ri.ret;
        }

        return ret;
    }

    public Object invoke(Invocation invocation) throws Exception {

        Object ret = null;

	ReturnInstruction ri = checkFaultInjection( invocation, true, null, null );
        if (ri.enabled ) {
            return ri.ret;
        }

        // call the next interceptor
        ret = getNext().invoke(invocation);

	ri = checkFaultInjection( invocation, false, ri, ret );
        if (ri.enabled) {
            return ri.ret;
        }

        return ret;
    }

    /**
     * checks and injects a fault, if necessary.
     * returns true if the caller should stop exection, false if it should continue.
     * 
     * @param invocation
     * @param isBefore
     * @return
     * @throws Exception
     */
    ReturnInstruction checkFaultInjection(Invocation invocation, boolean isBefore, ReturnInstruction prevri, Object realRet)
        throws Exception {

	// check to see if we gave ourselves instructions from the previous
	//  iteration of checkFaultInjection
	if( prevri != null ) {
	    if( prevri.saveValue ) {
		stuckresults.put( invocation.getMethod(),
				  realRet );
	    }
	}

        /*
         * for now, we just don't inject faults _after_ the method call... only before
         */
        if (!isBefore) {
	    return new ReturnInstruction( false, null );
        }

        Method m = invocation.getMethod();

        String methodName;
        if (m != null) {
            methodName = m.getName();
        }
        else {
            methodName = "<no method>";
        }

        Map originInfo =
            EnvironmentDetails.GenerateCompleteOriginInfo(ejbName, methodName);

        // fault injection
        FaultTrigger trigger = FaultGenerator.CheckFaultTriggers(originInfo);

        int fault = (trigger==null)?(FaultTrigger.FT_NOFAULT):(trigger.faultType);

        if (fault == FaultTrigger.FT_NOFAULT) {
            // invoke normally . don't inject any fault
	    return new ReturnInstruction( false, null );
        }
        else if (FaultGenerator.isAutomatableFault(trigger)) {
            System.out.println(
                "INJECTED FAULT: AUTOMATABLE FAULT (details follow) in "
                    + m.toString());
            FaultGenerator.GenerateFault(trigger);
            // if we reach here, we only injected a performance fault
            //    continue functioning normally
	    return new ReturnInstruction( false, null );
	}
        else if (fault == FaultTrigger.FT_THROWEXPECTEDEXCEPTION) {
            if (m == null | m.getExceptionTypes().length == 0) {
                // if there's no throws exceptions, don't throw anything.
		return new ReturnInstruction( false, null );
            }

            Class[] exceptions = m.getExceptionTypes();
            System.out.println(
                "INJECTED FAULT: THREW EXPECTED EXCEPTION ("
                    + exceptions[0].getName()
                    + ") in "
                    + m.toString());

            throw (Exception) (exceptions[0].newInstance());
        }
        else if (fault == FaultTrigger.FT_NULLCALL) {
            System.out.println(
                "INJECTED FAULT: OMITTED CALL to " + m.toString());
            // do nothing. it's a null call.
            return new ReturnInstruction( true, null );
        }
	else if (fault == FaultTrigger.FT_STUCKRESULT ) {
	    if( stuckresults == null ) {
		stuckresults = new HashMap();
	    }

	    Object ret = stuckresults.get( m );
	    if( ret == null ) {
		ReturnInstruction ri = new ReturnInstruction( false, null );
		ri.saveValue = true;
		return ri;
	    }
	    else {
		System.out.println(
				   "INJECTED FAULT: STUCK FAULT in " + m.toString());
		return new ReturnInstruction( true, ret );
	    }

	}

        // never reached -- error in Pinpoint.
        // just call original code
        return new ReturnInstruction( false, null );
    }

    class ReturnInstruction {

	boolean enabled;
	Object ret;

	boolean saveValue;

	ReturnInstruction( boolean enabled, Object ret ) {
	    this.enabled = enabled;
	    this.ret = ret;
	    this.saveValue = false;
	}

    }

}

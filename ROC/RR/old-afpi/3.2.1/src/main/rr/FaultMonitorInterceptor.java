/* $Id: FaultMonitorInterceptor.java,v 1.3 2003/09/03 01:04:35 candea Exp $ */

package rr;

import java.util.Hashtable;
import java.util.LinkedList;
import java.lang.Class;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.rmi.ServerException;

import javax.ejb.EJBException;
import javax.management.*;

import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.ejb.Container;
import org.jboss.metadata.BeanMetaData;
import org.jboss.invocation.*;

/** An EJB interceptor that detects exceptions thrown by the bean and reports
 *  them to the FaultInjectionService MBean.
 *
 *   OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE OBSOLETE 
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.3 $
 */

public class FaultMonitorInterceptor 
    extends AbstractInterceptor 
{
    //---------------------------------------------------------------------------
    // FIELDS
    //---------------------------------------------------------------------------
    Container container;
    String componentName="unknown";

    //---------------------------------------------------------------------------
    // PUBLIC METHODS
    //---------------------------------------------------------------------------
    public void setContainer(Container container) 
    {
	this.container = container;
        componentName = container.getBeanMetaData().getEjbName();
    }


    public Container getContainer() 
    {
        return container;
    }


    public void create() throws Exception 
    {
        super.start();
    }


    /***************************************************************************
     * Catch exceptions on their way out for Home invocations.                 *
     ***************************************************************************/
    public Object invokeHome(Invocation invocation) throws Exception 
    {
        Object ret;
        try { 
	    ret = getNext().invokeHome(invocation); 
	} 
	catch (Throwable t) {
	    throw handlePotentialFault(t, invocation);
        }
        return ret;
    }

    /***************************************************************************
     * Catch exceptions on their way ou for invocations.                       *
     ***************************************************************************/
    public Object invoke(Invocation invocation) throws Exception
    {
        Object ret;
        try {
            ret = getNext().invoke(invocation);
        }
	catch (Throwable t) {
            throw handlePotentialFault(t, invocation);
        }
        return ret;
    }


    //---------------------------------------------------------------------------
    // PRIVATE METHODS
    //---------------------------------------------------------------------------


    /***************************************************************************
     * Report a Throwable to the FI Service, and then re-throw it.             *
     ***************************************************************************/
    private Exception handlePotentialFault( Throwable thrown, Invocation invocation )
	throws Exception
    {
	Exception toThrow;

	reportToFIService(thrown, invocation.getMethod().getName());

        InvocationType type = invocation.getType();
        boolean isLocal = (type==InvocationType.LOCAL) || 
	                  (type==InvocationType.LOCALHOME);

        if (thrown instanceof Exception) 
	{
            toThrow = (Exception) thrown;
        }
        else {
	    if ( isLocal ) 
	    {
                toThrow = new EJBException("Unexpected Error");
            }
            else 
            {
                toThrow = new ServerException("Unexpected Error");
            }
            toThrow.initCause(thrown);
	}

	throw toThrow;
    }
    

    /****************************************************************************
     * Report a Throwable to the FI Service.                                    *
     ****************************************************************************/
    private void reportToFIService( Throwable thrown, String mName )
    {
	try {
	    MBeanServer srv = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
	    ObjectName mbean = new ObjectName("RR:service=FaultInjectionService");
	    srv.invoke(mbean, "reportThrowable", 
		       new Object[] { componentName, mName, thrown }, 
		       new String[] { "java.lang.String", "java.lang.String", "java.lang.Throwable" });
	} 
	catch( Exception e ) {
	    e.printStackTrace();
	}
    }


}

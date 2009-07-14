package rr;

import java.util.Hashtable;
import java.util.LinkedList;
import java.lang.Class;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.rmi.ServerException;

import javax.ejb.*;
import javax.management.*;

import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.ejb.Container;
import org.jboss.metadata.BeanMetaData;
import org.jboss.invocation.*;

/** An EJB interceptor that:
 *  (a) inspects beans and dumps their information into the DB;
 *  (b) executes injections when requested by the FaultInjectionService.
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.1 $
 */

public class EJBInterceptor 
    extends org.jboss.ejb.plugins.AbstractInterceptor
{
    rr.Interceptor mixin;

    /* following two fields obsolete ??? */
    protected Container container;    // a reference to the EJB container
    private   Component myComp;  // component that corresponds to this EJB

    /**
     * Instantiates the interceptor and sets up the DB agent.
     *
     **/
    public void create() 
	throws Exception 
    {
        super.start();

	mixin = new Interceptor();

        BeanMetaData md = getContainer().getBeanMetaData();

	System.out.println("+++++ FaultInjectionInterceptor:create(): " + md.getJndiName());

	// FIXME:
	//  - look at getEjbReferences() and getEjbLocalReferences()
	//  - can we use getFields() and inject data faults into them ?

	// Turn the EJB info into a component and report it to the FaultInjectionService
	myComp = convertToComponent(md);
	mixin.addComponent( myComp );
    }



    /***************************************************************************
     * invokeHome                                                              *
     *                                                                         *
     * Intercept call to home interface.  Check if there we have a fault to    *
     * inject and does so, if needed.  Otherwise just pass control to the next *
     * interceptor.                                                            *
     ***************************************************************************/
    public Object invokeHome( Invocation invocation ) 
	throws Exception 
    {
	String methodName = invocation.getMethod().getName();

	/* Invoke next interceptor and catch throwables, to report them
	 * (monitoring function).  After reporting, propagate them up. */
	Object ret;
	try {
	    /* Give AFPI the opportunity to inject a fault, if needed */
	    mixin.preInvoke( myComp.shortName, methodName );
	    
	    ret = getNext().invokeHome(invocation);
	}
	catch( Exception exc ) {
	    mixin.reportFault( myComp.canonicalName, methodName, (Throwable)exc );
	    throw exc;
	}
	catch( Error err ) {
	    mixin.reportFault( myComp.canonicalName, methodName, (Throwable)err );
	    throw err;
	}

	return ret;
    }

    /***************************************************************************
     * invoke                                                                  *
     *                                                                         *
     * Intercept call to remote interface.  Check if there we have a fault to  *
     * inject and does so, if needed.  Otherwise just pass control to the next *
     * interceptor.                                                            *
     ***************************************************************************/
    public Object invoke( Invocation invocation )  
	throws Exception
    {
	String methodName = invocation.getMethod().getName();

	/* Invoke next interceptor and catch throwables, to report them
	 * (monitoring function).  After reporting, propagate them up.  */
	Object ret;
	try {
	    /* Give AFPI the opportunity to inject a fault, if needed */
	    mixin.preInvoke( myComp.shortName, methodName );

	    ret = getNext().invoke( invocation );
	}
	catch( Exception exc ) {
	    mixin.reportFault( myComp.canonicalName, methodName, (Throwable)exc );
	    throw exc;
	}
	catch( Error err ) {
	    mixin.reportFault( myComp.canonicalName, methodName, (Throwable)err );
	    throw err;
	}

	return ret;
    }

    public void setContainer(Container container) 
    {
        this.container = container;
    }

    public Container getContainer() 
    {
        return container;
    }

    //---------------------------------------------------------------------------
    // PRIVATE METHODS
    //---------------------------------------------------------------------------

    /****************************************************************************
     * convertToComponent
     *
     * Reflects the given EJB (extract methods and exceptions).  We do
     * this here because the FaultInjectionService (a) may be remote,
     * and (b) it should not have to understand the semantics of Java
     * classes.  
     *
     *
     * FIXME: The fact that we strip "ejb" from the front of the
     * method is a hack.  We need to figure out the logic behind this.
     *
     * FIXME: do we care about exceptions that are not declared by the
     * method, but that could propagate from its callees?
     ****************************************************************************/
    private Component convertToComponent( BeanMetaData md )
	throws java.lang.ClassNotFoundException
    {
	String type="EJB (";
	if ( md.isClustered() ) {
	    type += "Clustered ";
	}

	if ( md.isSession() ) { 
	    type += "SES)"; 
	}
	else if ( md.isMessageDriven() ) {
	    type += "MDB)"; 
	}
	else if ( md.isEntity() ) { 
	    type += "ENT)";
	}
	else {
	    type += "unknown)";
	}

	Component component = new Component(md.getJndiName(), type, null);

	if ( md.isContainerManagedTx() ) { 
	    component.setPersistence("CMP"); 
	} else if ( md.isBeanManagedTx() ) { 
	    component.setPersistence("BMP"); 
	}

	// Go through each of the EJB's methods and add them to the RR component
	Method[] ejbMethods = Class.forName(md.getEjbClass()).getDeclaredMethods();

	for (int i=0 ; i < ejbMethods.length ; i++) {
	    Method method = ejbMethods[i];  // the method we're looking at

	    // If it's a special method, skip over it
	    String mName = method.getName();
	    if ( mName.equals("ejbActivate") || 
		 mName.equals("ejbPassivate") ||
		 mName.equals("ejbCreate") || 
		 mName.equals("ejbPostCreate") ||
		 mName.equals("ejbRemove") || 
		 mName.equals("ejbDestroy") ||
		 mName.equals("ejbStore") || 
		 mName.equals("ejbLoad") ) 
	    {
		continue;
	    }
	
	    MethodSignature sig = new MethodSignature(mName);

	    sig.faults = new LinkedList();
	    Class[] excs = method.getExceptionTypes();
	    for (int j=0 ; j < excs.length ; j++)
		sig.addFault( excs[j].getName() );

	    component.addMethod(sig.name);
	}

	return component;
    }
}

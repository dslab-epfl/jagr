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
import org.jboss.invocation.Invocation;

/** An EJB interceptor that:
 *  (a) inspects beans and reports their interface to the FaultInjectionService;
 *  (b) executes injections when requested by the FaultInjectionService.
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.8 $
 */

public class FaultInjectionInterceptor 
    extends AbstractInterceptor 
{
    /*===== FIELDS ===========================================================*/

    protected Container container;    // a reference to the EJB container
    private   Component myComp;  // component that corresponds to this EJB
    private   Exception badException; // exception to inject (none, if null)
    private   String    badMethod;    // method where to inject the exception

    /*===== PUBLIC METHODS ===================================================*/

    /**************************************************************************
     * scheduleFault                                                          *
     *                                                                        *
     * Schedule a fault to be injected on the next invocation of the given    *
     * method.  Also validate the method name and fault, not to throw up at   *
     * injection time.                                                        *
     **************************************************************************/
    public String scheduleFault( String mName, String fName )
	throws Exception
    {
	//
	// Create the injectable exception object; this also validates the fault name
	//
	Class fc = Class.forName(fName);
	Constructor cons = fc.getConstructor(new Class[] { Class.forName("java.lang.String") });
	badException = (Exception) cons.newInstance(new Object[] { (String)"AFPI-induced" });
	
	//
	// Make sure the given method exists in this component
	//
	if (myComp.methods.get(mName) == null) {
	    throw new ObjectNotFoundException(myComp.name + " doesn't have method " + mName); 
	}

	badMethod = mName;
	return "Scheduled <B>" + fName + "</B> in method <B>" + mName + "</B> of component <B>" + myComp.name + "</B>";
    }

    /***************************************************************************
     * invokeHome                                                              *
     *                                                                         *
     * Intercept call to home interface.  Check if there we have a fault to    *
     * inject and does so, if needed.  Otherwise just pass control to the next *
     * interceptor.                                                            *
     ***************************************************************************/
    public Object invokeHome(Invocation invocation) 
	throws Exception 
    {
        String mName = invocation.getMethod().getName();
	System.out.println("++++++++++ invokeHome/call to " + myComp.name + ":" + mName);

	injectFault(invocation);

	return getNext().invokeHome(invocation);
    }

    /***************************************************************************
     * invoke                                                                  *
     *                                                                         *
     * Intercept call to remote interface.  Check if there we have a fault to  *
     * inject and does so, if needed.  Otherwise just pass control to the next *
     * interceptor.                                                            *
     ***************************************************************************/
    public Object invoke(Invocation invocation) 
	throws Exception
    {
	String mName = invocation.getMethod().getName();
	System.out.println("++++++++++ invoke/call to " + myComp.name + ":" + mName);

	injectFault(invocation);

	return getNext().invokeHome(invocation);
    }

    public void setContainer(Container container) 
    {
        this.container = container;
    }

    public Container getContainer() 
    {
        return container;
    }

    public void create() throws Exception 
    {
        super.start();
        BeanMetaData md = getContainer().getBeanMetaData();

	// TODO:
	//  - look at getEjbReferences() and getEjbLocalReferences()
	//  - can we use getFields() and inject data faults into them ?

	// Turn the EJB info into a component and report it to the FaultInjectionService
	myComp = convertToComponent(md);
	reportToFIService(myComp);
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
	Component component = new Component(md.getJndiName(), this);

	if (md.isSession())            { component.setType("session EJB"); }
	else if (md.isMessageDriven()) { component.setType("msg-driven EJB"); }
	else if (md.isEntity())        { component.setType("entity EJB"); }

	if (md.isContainerManagedTx()) { component.setPersistence("container-managed"); }
	else if (md.isBeanManagedTx()) { component.setPersistence("bean-managed"); }

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
	
            // FIXME: this is just a hack; we chop off "ejb" prefixes and change the
	    // first letter of the remaining substring to lower case.
	    if (mName.startsWith("ejb")) {  
		mName = Character.toLowerCase(mName.charAt(3)) + mName.substring(4);
	    }

	    MethodSignature sig = new MethodSignature(mName);

	    sig.faults = new LinkedList();
	    Class[] excs = method.getExceptionTypes();
	    for (int j=0 ; j < excs.length ; j++)
		sig.addFault( excs[j].getName() );

	    component.addMethod(sig);
	}

	return component;
    }


    /****************************************************************************
     * reportToFIService
     *
     * Send a "new component" notification and information to the
     * FaultInjectionService.
     ****************************************************************************/
    private void reportToFIService( Component component )
    {
	try {
	    MBeanServer srv = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
	    ObjectName mbean = new ObjectName("RR:service=FaultInjectionService");
	    srv.invoke(mbean, "addNewComponent", 
		       new Object[] { component }, new String[] { "rr.Component" });
	} 
	catch( Exception e ) {
	    e.printStackTrace();
	}
    }

    private void injectFault( Invocation invocation ) throws Exception
    {
	Method meth = invocation.getMethod();
	if (meth != null  &&  meth.getName().equals(badMethod)) {
	    System.out.println("+++++ INJECTING " + badException.toString());
	    throw badException;
	}
    }

}

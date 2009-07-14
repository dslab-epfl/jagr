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
 *   @version $Revision: 1.1 $
 */

public class MBeanInterceptor
{
    /*===== FIELDS ===========================================================*/

    
    /* comes from

       protected ObjectInstance registerMBean(Object object, ObjectName name, 
       ClassLoader cl)
       throws InstanceAlreadyExistsException, 
       MBeanRegistrationException, 
       NotCompliantMBeanException
    */
      
    public void registerMBean(Object object, ObjectName name, ClassLoader cl)
    {
	System.out.println("~~~~~ INTERCEPT: registerMBean " + name);
    }

    // comes from
    //   Object mbean = registry.get(name).getResourceInstance();
    
    public void unregisterMBean(Object mbean)
    {
	System.out.println("~~~~~ INTERCEPT: unregisterMBean " + mbean);
    }
    

    /* comes from
       
       public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
       throws InstanceNotFoundException, MBeanException, ReflectionException
       {
       MBeanEntry entry = registry.get(name);
       ClassLoader newTCL = entry.getClassLoader();
       DynamicMBean mbean = entry.getMBean();
       
       Thread thread = Thread.currentThread();
       ClassLoader oldTCL = thread.getContextClassLoader();
       try
       {
         if (newTCL != oldTCL && newTCL != null)
         thread.setContextClassLoader(newTCL);
       
       
       return mbean.invoke(operationName, params, signature);
    */

    public void invoke(DynamicMBean mbean, String operationName)
    {
	System.out.println("~~~~~ INTERCEPT: invoke " + mbean + operationName);
    }
}

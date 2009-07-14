/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: MBeanInvocationHandler.java,v 1.1.1.1 2002/11/16 03:16:50 mikechen Exp $

package org.jboss.net.jmx;

import org.jboss.net.axis.AxisInvocationHandler;

import javax.management.ObjectName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import java.lang.reflect.Proxy;
import java.lang.reflect.Method;

import java.net.URL;
import java.util.Map;

/*
 * Helper class for dealing with remote JMX beans in typed or untyped ways.
 * @created  1. Oktober 2001, 18:22
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 1.1.1.1 $
 */

public class MBeanInvocationHandler extends AxisInvocationHandler {

   /** Creates new MBeanInvocationHandler pointing to a given url, using the given engine */
   public MBeanInvocationHandler(URL endpoint)  {
      super(endpoint);
   }

   /** Creates new MBeanInvocationHandler pointing to a given url, using the given engine */
   public MBeanInvocationHandler(URL endpoint, Map methodMap)  {
      super(endpoint, methodMap);
   }

   /** Creates new MBeanInvocationHandler pointing to a given url, using the given engine */
   public MBeanInvocationHandler(URL endpoint, Map methodMap, Map interfaceMap) {
      super(endpoint, methodMap, interfaceMap);
   }

   /** Creates new MBeanInvocationHandler pointing to a given url, using the given engine */
   public MBeanInvocationHandler(URL endpoint, Map methodMap, Map interfaceMap, boolean maintainSession)
       {
      super(endpoint, methodMap, interfaceMap,maintainSession);
   }

   /** Creates new MBeanInvocationHandler */
   public MBeanInvocationHandler(Call call, Map methodMap, Map interfaceMap) {
      super(call,methodMap,interfaceMap);
   }

   /** Creates new MBeanInvocationHandler */
   public MBeanInvocationHandler(URL endpoint, Service service,Map methodMap, Map interfaceMap) {
      super(endpoint,service,methodMap,interfaceMap);
   }

   /** invocation using method signature */
   public Object invoke(
      String serviceName,
      String methodName,
      Object[] arguments,
      Class[] classes)
      throws java.rmi.RemoteException 
   {
     Object [] realArgs = null;
     if( classes != null )
      {
        // convert classes to strings
        String[] classNames = new String[classes.length];
        for (int count = 0; count < classes.length; count++)
           classNames[count] = classes[count].getName();
      
        // we convert the parameter structure
        realArgs =
         arguments != null ? new Object[arguments.length + 1] : new Object[1];
        realArgs[0] = classNames;
      }
      if (arguments != null)
         System.arraycopy(arguments, 0, realArgs, 1, arguments.length);
      return invoke(serviceName, methodName, realArgs);
   }

   /** default creation of services */
   public static Object createMBeanService(Class _interface, URL endpoint)
       {
      return createAxisService(_interface, new MBeanInvocationHandler(endpoint));
   }

   /** default creation of services */
   public static Object createMBeanService(
      Class _interface,
      MBeanInvocationHandler handler)
      {
      return createAxisService(_interface, handler);
   }

}

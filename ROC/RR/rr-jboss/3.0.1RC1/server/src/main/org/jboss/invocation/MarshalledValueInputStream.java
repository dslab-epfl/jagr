/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation;

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/** An ObjectInputStream subclass used by the MarshalledValue class to
 ensure the classes and proxies are loaded using the thread context
 class loader.

@author Scott.Stark@jboss.org
@version $Revision: 1.1.1.1 $
 */
public class MarshalledValueInputStream extends ObjectInputStream
{
   /** Creates a new instance of MarshalledValueOutputStream */
   public MarshalledValueInputStream(InputStream is) throws IOException
   {
      super(is);
   }

   /** Use the thread context class loader to resolve the class
    * @exception IOException Any exception thrown by the underlying OutputStream.
    */
   protected Class resolveClass(ObjectStreamClass v) throws IOException,
      ClassNotFoundException
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      String className = v.getName();
      return loader.loadClass(className);
   }

   protected Class resolveProxyClass(String[] interfaces) throws IOException,
      ClassNotFoundException
   {
      // Load the interfaces from the thread context class loader
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class[] ifaceClasses = new Class[interfaces.length];
      for (int i = 0; i < interfaces.length; i++)
      {
          ifaceClasses[i] = loader.loadClass(interfaces[i]);
      }
      return java.lang.reflect.Proxy.getProxyClass(loader, ifaceClasses);
   }
}

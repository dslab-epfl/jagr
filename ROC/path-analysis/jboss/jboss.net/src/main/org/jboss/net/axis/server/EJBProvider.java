/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: EJBProvider.java,v 1.1.1.1 2002/11/16 03:16:50 mikechen Exp $

package org.jboss.net.axis.server;

import org.jboss.net.axis.XMLResourceProvider;

// Axis stuff
import org.apache.axis.Handler;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;

// xml messaging
import javax.xml.rpc.namespace.QName;

// JNDI
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

// EJB
import javax.ejb.EJBHome;

// reflection
import java.lang.reflect.Method;

// io
import java.rmi.RemoteException;

/**
 * A JBoss-compatible EJB Provider that exposes the methods of
 * a stateless bean. Basically its a slimmed downed derivative of 
 * the Axis-EJBProvider without the Corba-stuff that is working under
 * the presumption that the right classloader has already been set
 * by the invocation chain (@see org.jboss.net.axis.SetClassLoaderHandler).
 * <br>
 * <h3>Change History</h3>
 * <ul>
 * <li> jung, 22.03.02: slimmed down and renamed. </li>
 * <li> jung, 09.03.02: axis alpha 3 is here. </li>
 * </ul>
 * <br>
 * <h3>To Do</h3>
 * <ul>
 * </ul>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @created 5. Oktober 2001, 13:02
 * @version $Revision: 1.1.1.1 $
 */

public class EJBProvider extends org.apache.axis.providers.java.EJBProvider {

   /** the real remote class we are shielding */
   protected Class remoteClass;

   /** Creates new EJBProvider */
   public EJBProvider() {
   }

   /**
    * Return the object which implements the service. Makes the usual
    * lookup->create call wo the PortableRemoteDaDaDa for the sake of Corba.
    * @param msgContext the message context
    * @param clsName The JNDI name of the EJB home class
    * @return an object that implements the service
    */
   protected Object getNewServiceObject(MessageContext msgContext, String clsName)
      throws Exception {
      // Get the EJB Home object from JNDI
      Object ejbHome = new InitialContext().lookup(clsName);

      // Invoke the create method of the ejbHome class without actually
      // touching any EJB classes (i.e. no cast to EJBHome)
      Method createMethod = ejbHome.getClass().getMethod("create", empty_class_array);
      Object result = createMethod.invoke(ejbHome, empty_object_array);

      return result;
   }

   /**
    * Return the class name of the service
    */
   protected Class getServiceClass(MessageContext msgContext, 
                                    String beanJndiName) throws Exception 
    {
      if (remoteClass == null) {
         try {
            EJBHome ejbHome =
               (EJBHome) new InitialContext().lookup(beanJndiName);
            remoteClass = ejbHome.getEJBMetaData().getRemoteInterfaceClass();
         } catch (RemoteException e) {
            throw new RuntimeException("Could not access meta-data through home " + e);
         } catch (NamingException e) {
            throw new RuntimeException("Could not access meta-data through home " + e);
         }
      }

      return remoteClass;
   }

   /**
    * Generate the WSDL for this service.
    * We need to rearrange the classloader stuff for that purpose.
    */
   public void generateWSDL(MessageContext msgContext) throws AxisFault {
      EngineConfiguration engineConfig = msgContext.getAxisEngine().getConfig();

      if (engineConfig instanceof XMLResourceProvider) {
         XMLResourceProvider config = (XMLResourceProvider) engineConfig;
         ClassLoader newLoader =
            config.getMyDeployment().getClassLoader(
               new QName(null, msgContext.getTargetService()));
         ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
         try {
            Thread.currentThread().setContextClassLoader(newLoader);
            super.generateWSDL(msgContext);
         } finally {
            Thread.currentThread().setContextClassLoader(currentLoader);
         }
      }

   }

}
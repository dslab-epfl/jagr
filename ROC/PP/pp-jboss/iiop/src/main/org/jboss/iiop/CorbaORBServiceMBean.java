/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.iiop;

import javax.management.ObjectName;

import org.jboss.util.jmx.ObjectNameFactory;

/**
 *   Mbean interface for the JBoss CORBA ORB service.
 *      
 *   @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *   @author <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 *   @version $Revision: 1.1.1.1 $
 */
public interface CorbaORBServiceMBean
   extends org.jboss.system.ServiceMBean
{
   // Constants -----------------------------------------------------
   public static final ObjectName OBJECT_NAME = 
         ObjectNameFactory.create(":service=JBossCorbaORB");
    
   // Public --------------------------------------------------------
   public String getORBClass();
   public void setORBClass(String orbClass);

   public String getORBSingletonClass();
   public void setORBSingletonClass(String orbSingletonClass);

   public String getORBSingletonDelegate();
   public void setORBSingletonDelegate(String orbSingletonDelegate);

   public void setORBPropertiesFileName(String orbPropertiesFileName);
   public String getORBPropertiesFileName();

   public String getPortableInterceptorInitializerClass();
   public void setPortableInterceptorInitializerClass(
                                 String portableInterceptorInitializerClass);

}


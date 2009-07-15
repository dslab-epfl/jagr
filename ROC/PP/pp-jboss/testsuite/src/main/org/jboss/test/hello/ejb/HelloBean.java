/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.hello.ejb;

import java.io.Serializable;
import javax.ejb.EJBException;

import org.jboss.test.util.ejb.SessionSupport;
import org.jboss.test.hello.interfaces.Hello;
import org.jboss.test.hello.interfaces.HelloData;
import org.jboss.test.hello.interfaces.HelloException;

/**
 *      
 *   @author Scott.Stark@jboss.org
 *   @version $Revision: 1.1.1.1 $
 */
public class HelloBean
   extends SessionSupport
{
   public String hello(String name)
   {
      return "Hello "+name+"!";
   }
   public String helloException(String name)
      throws HelloException
   {
      throw new HelloException("Catch me");
   }

   public Hello helloHello(Hello hello)
   {
      return hello;
   }

   public String howdy(HelloData name)
   {
      return "Howdy "+name.getName()+"!";
   }

   public String sleepingHello(String name, long sleepTimeMS)
   {
      if( sleepTimeMS <= 0 )
         sleepTimeMS = 1;
      try
      {
         Thread.sleep(sleepTimeMS);
      }
      catch(InterruptedException ignore)
      {
      }
      return "Hello "+name+"!";
   }

   public Object getCNFEObject()
   {
      return new ServerData();
   }
   public void throwException()
   {
      throw new EJBException("Something went wrong");
   }

   static class ServerData implements Serializable
   {
   }
}

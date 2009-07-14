
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.test.jca.adapter; 

import java.io.PrintWriter;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

// Generated package name
/**
 * ManagedConnectionFactory.java
 *
 *
 * Created: Mon Dec 31 17:01:55 2001
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class TestManagedConnectionFactory implements ManagedConnectionFactory
{
   //number the managed connections
   int id;

   public TestManagedConnectionFactory ()
   {
      
   }

   // implementation of javax.resource.spi.ManagedConnectionFactory interface

   /**
    *
    * @return <description>
    */
   public int hashCode()
   {
     // TODO: implement this javax.resource.spi.ManagedConnectionFactory method
     return 0;
   }

   /**
    *
    * @param param1 <description>
    * @return <description>
    */
   public boolean equals(Object other)
   {
      return (other != null) && (other.getClass() == getClass());
   }

   /**
    *
    * @param param1 <description>
    * @exception javax.resource.ResourceException <description>
    */
   public void setLogWriter(PrintWriter param1) throws ResourceException
   {
     // TODO: implement this javax.resource.spi.ManagedConnectionFactory method
   }

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
     // TODO: implement this javax.resource.spi.ManagedConnectionFactory method
     return null;
   }

   /**
    *
    * @param param1 <description>
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public Object createConnectionFactory(ConnectionManager cm) throws ResourceException
   {
     // TODO: implement this javax.resource.spi.ManagedConnectionFactory method
     return new TestConnectionFactory(cm, this);
   }

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public Object createConnectionFactory() throws ResourceException
   {
      throw new ResourceException("not yet implemented");
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      return new TestManagedConnection(subject, (TestConnectionRequestInfo)cri, id++);
   }

   /**
    * Describe <code>matchManagedConnections</code> method here.
    *
    * @param candidates a <code>Set</code> value
    * @param subject a <code>Subject</code> value
    * @param cri a <code>ConnectionRequestInfo</code> value
    * @return a <code>ManagedConnection</code> value
    * @exception ResourceException if an error occurs
    */
   public ManagedConnection matchManagedConnections(Set candidates, Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      if (candidates.isEmpty()) 
      {
         return null;
      } // end of if ()
      return (ManagedConnection)candidates.iterator().next();
   }

}

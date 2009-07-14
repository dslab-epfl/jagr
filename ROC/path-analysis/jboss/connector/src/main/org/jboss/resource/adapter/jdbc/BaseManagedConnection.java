/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.jboss.resource.adapter.jdbc;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * Abstract base class for ManagedConnections.
 *
 * @author    Aaron Mulder <ammulder@alumni.princeton.edu>
 * @author    <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version   $Revision: 1.1.1.1 $
 */
public abstract class BaseManagedConnection implements ManagedConnection
{
   /**
    * Description of the Field
    */
   protected ArrayList listeners;
   private String user;
   private Logger log;

   /**
    * Constructor for the BaseManagedConnection object
    *
    * @param user  Description of Parameter
    */
   public BaseManagedConnection(String user)
   {
      this.user = user;
      listeners = new ArrayList();
      log = Logger.getLogger(getClass().getName() + "." + this);

   }

   /*
    * We ignore this and use log4j
    */
   /**
    * Sets the LogWriter attribute of the BaseManagedConnection object
    *
    * @param writer  The new LogWriter value
    */
   public void setLogWriter(PrintWriter writer)
   {
   }

   /**
    * Gets the LogWriter attribute of the BaseManagedConnection object
    *
    * @return   The LogWriter value
    */
   public PrintWriter getLogWriter()
   {
      return null;
   }

   /**
    * Gets the User attribute of the BaseManagedConnection object
    *
    * @return   The User value
    */
   public String getUser()
   {
      return user;
   }

   /**
    * #Description of the Method
    *
    * @param Tx                     Description of Parameter
    * @exception ResourceException  Description of Exception
    */
   public void associateConnection(Object Tx)
          throws ResourceException
   {
      throw new ResourceException("associateConnection not supported");
   }

   /**
    * Adds a feature to the ConnectionEventListener attribute of the
    * BaseManagedConnection object
    *
    * @param listener  The feature to be added to the ConnectionEventListener
    *      attribute
    */
   public void addConnectionEventListener(ConnectionEventListener listener)
   {
      listeners.add(listener);
   }

   /**
    * #Description of the Method
    *
    * @param listener  Description of Parameter
    */
   public void removeConnectionEventListener(ConnectionEventListener listener)
   {
      listeners.remove(listener);
   }

   /**
    * #Description of the Method
    *
    * @exception ResourceException  Description of Exception
    */
   public void destroy()
          throws ResourceException
   {
      listeners.clear();
      listeners = null;
      log = null;
      user = null;
   }

   /**
    * Gets the Log attribute of the BaseManagedConnection object
    *
    * @return   The Log value
    */
   protected Logger getLog()
   {
      return log;
   }

   /**
    * #Description of the Method
    *
    * @param evt  Description of Parameter
    */
   protected void fireConnectionEvent(ConnectionEvent evt)
   {
      List local = (List)listeners.clone();
      for (int i = local.size() - 1; i >= 0; i--)
      {
         if (evt.getId() == ConnectionEvent.CONNECTION_CLOSED)
         {
            ((ConnectionEventListener)local.get(i)).connectionClosed(evt);
         }
         else if (evt.getId() == ConnectionEvent.CONNECTION_ERROR_OCCURRED)
         {
            ((ConnectionEventListener)local.get(i)).connectionErrorOccurred(evt);
         }
      }
   }
}

/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.httpsession.beanimpl.ejb;


import java.rmi.RemoteException;
import java.rmi.MarshalledObject;

import javax.ejb.EJBException;

import org.jboss.ha.httpsession.interfaces.SerializableHttpSession;

/**
 * Core implementation of methods for the bean.
 *
 * @see org.jboss.ha.httpsession.interfaces.ClusteredHTTPSession
 * @see org.jboss.ha.httpsession.ejb.ClusteredHTTPSessionBeanAbstract
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>31. decembre 2001 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public abstract class ClusteredHTTPSessionBeanImpl extends ClusteredHTTPSessionBeanAbstract
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   protected SerializableHttpSession tmpSession = null;
   protected boolean isModified = false;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   // Z implementation ----------------------------------------------
   
   // ClusteredHTTPSessionBeanAbstract overrides ---------------------------------------------------
   
   public void ejbStore () throws EJBException, RemoteException
   {
      if (tmpSession != null)
         // the tmpSession has been assigned. Furthermore, if ejbStore is called
         // it means that isModified==true => we need to rebuild a serialized representation
         //
         serializeSession();
   }
   
   public void ejbLoad () throws EJBException, RemoteException
   {
      // the tmp value is no more valid: a new serialized representation is just loaded.
      // it will be transformed only if explicitly asked.
      //
      tmpSession = null;
      isModified = false;
   }
   
   public SerializableHttpSession getSession ()
   {
      if (tmpSession == null)
      {
         // this is the first access to the object representation.
         // we use a lazy scheme => we unserialize now
         unserializeSession ();
      }
      return this.tmpSession;
   }
   
   public void setSession (SerializableHttpSession session)
   {
      if (tmpSession == null)
         isModified = true;
      else
         isModified = session.areAttributesModified (tmpSession);
      
      // in any case, we update the "time" attributes
      //
      this.setCreationTime (session.getContentCreationTime ());
      this.setLastAccessedTime (session.getContentLastAccessTime ());
      
      // in any cases, we assign the new session: this is because the session
      // may have internal data that is not used for the isModified comparison
      // (such as last access time). Consequently, if we use a load-balancer with
      // sticky sessions, the values will be kept in cache correctly whereas if we
      // don't have sticky session, these values will only be clustered-saved if
      // an attributed is modified!
      //
      this.tmpSession = session;
   }
   
    // Optimisation: called by the CMP engine
    //
    public boolean isModified () { return this.isModified; }
    
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   protected void serializeSession() throws EJBException
   {
      // In the current release, we use MarshalledObject. It is probably not a
      // solution at all but it is very easy to change
      //
      try
      {
         this.setSerializedSession (new MarshalledObject (this.tmpSession));      
      }
      catch (Exception e)
      {
         throw new EJBException (e.toString ());
      }
   }
   
   protected void unserializeSession() throws EJBException
   {
      try
      {         
         MarshalledObject mo = (MarshalledObject)this.getSerializedSession ();
         if (mo != null)
            this.tmpSession = (SerializableHttpSession)(mo.get ());
      }
      catch (Exception e)
      {
         throw new EJBException (e.toString ());
      }
   }

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}

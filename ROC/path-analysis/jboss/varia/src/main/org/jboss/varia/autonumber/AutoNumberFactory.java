/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.varia.autonumber;

import javax.naming.InitialContext;

import org.jboss.logging.Logger;

/**
 * AutoNumberFactory can persistently auto number items. 
 *
 * @author <a href="mailto:michel.anke@wolmail.nl">Michel de Groot</a>
 * @version $Revision: 1.1.1.1 $
 */
public class AutoNumberFactory
{
   private static final Logger log = Logger.getLogger(AutoNumberFactory.class);
   
   private static AutoNumberHome autoNumberHome;
   
   /**
    * Gets the next key for the given collection.
    * Note 1: you must deploy EJB AutoNumber
    * Note 2: the keys are persistent in your database, independent of 
    * the actual table
    * Note 3: you can only add instances to the collection which have a 
    * key generated by this method, otherwise the keys are not guaranteed
    * to be unique
    * Note 4: key values are >= 0
    * @param collectionName the name of the collection for which you want an autonumber
    * @throws ArrayIndexOutOfBoundsException if no more numbers are available
    */
   public static Integer getNextInteger(String collectionName)
      throws ArrayIndexOutOfBoundsException
   {
      Integer value = null;
      AutoNumber autoNumber = null;
      if (autoNumberHome == null) {
         try {
            autoNumberHome = (AutoNumberHome)new InitialContext().lookup("JBossUtilAutoNumber");
         }
         catch (javax.naming.NamingException e) {
            log.error("operation failed", e);
         }
      }
      
      try {
         autoNumber = (AutoNumber)autoNumberHome.findByPrimaryKey(collectionName);
      }
      catch (javax.ejb.FinderException e) {
         // autonumber does not exist yet, create one at value 0
         try {
            autoNumber = autoNumberHome.create(collectionName);
         }
         catch (javax.ejb.CreateException x) {
            log.error("operation failed", x);
         }
         catch (java.rmi.RemoteException x) {
            log.error("operation failed", x);
         }
         
         try {
            autoNumber.setValue(new Integer(0));
         }
         catch (java.rmi.RemoteException x) {
            log.error("operation failed", x);
         }
      }
      catch (java.rmi.RemoteException e) {
         log.error("operation failed", e);
      }
      
      try {
         value = autoNumber.getValue();
         autoNumber.setValue(new Integer(value.intValue()+1));
      }
      catch (java.rmi.RemoteException e) {
         log.error("operation failed", e);
      }
      
      return value;
   }
   
   /**
    * Resets the given autonumber to zero.
    * Use with extreme care!
    */
   public static void resetAutoNumber(String collectionName) {
      setAutoNumber(collectionName,new Integer(0));
   }
   
   /**
    * Sets the given autonumber to the given value so that it starts
    * counting at the given value.
    * Use with extreme care!
    */
   public static void setAutoNumber(String collectionName, Integer value) {
      AutoNumber autoNumber = null;
      if (autoNumberHome == null) {
         try {
            autoNumberHome = (AutoNumberHome)
               new InitialContext().lookup("JBossUtilAutoNumber");
         }
         catch (javax.naming.NamingException e) {
            log.error("operation failed", e);            
         }
      }
      
      try {
         autoNumber = (AutoNumber)autoNumberHome.findByPrimaryKey(collectionName);
      }
      catch (javax.ejb.FinderException e) {
         // autonumber does not exist yet, create one

         try {
            autoNumber = autoNumberHome.create(collectionName);
         }
         catch (javax.ejb.CreateException x) {
            log.error("operation failed", x);
         }
         catch (java.rmi.RemoteException x) {
            log.error("operation failed", x);
         }
      }
      catch (java.rmi.RemoteException e) {
         log.error("operation failed", e);
      }
      
      try {
         autoNumber.setValue(value);
      }
      catch (java.rmi.RemoteException e) {
         log.error("operation failed", e);
      }
   }	
}

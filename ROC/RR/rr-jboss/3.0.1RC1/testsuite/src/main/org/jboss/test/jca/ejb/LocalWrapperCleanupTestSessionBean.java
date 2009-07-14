
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */


package org.jboss.test.jca.ejb;


import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Statement;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.log4j.Category;
import org.jboss.resource.adapter.jdbc.local.LocalConnection;
import java.sql.ResultSet;

/**
 * LocalWrapperCleanupTestSessionBean.java
 *
 *
 * Created: Thu May 23 14:50:16 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 *
 *
 * @ejb:bean   name="LocalWrapperCleanupTestSession"
 *             jndi-name="LocalWrapperCleanupTestSession"
 *             view-type="remote"
 *             type="Stateless"
 *
 */

public class LocalWrapperCleanupTestSessionBean
   implements SessionBean  
{

   private Category log = Category.getInstance(getClass().getName());


   /**
    * Describe <code>testAutoCommitInReturnedConnection</code> method here.
    *
    * @exception EJBException if an error occurs
    * @ejb:interface-method
    * @ejb:transaction type="NotSupported"
    */
   public void testAutoCommitInReturnedConnection() throws EJBException
   {
      try 
      {
         DataSource ds = (DataSource)new InitialContext().lookup("java:/SingleConnectionDS");
         Connection c = ds.getConnection();
         if (c.getAutoCommit() == false) 
         {
            throw new EJBException("Initial autocommit state false!");
         } // end of if ()
         c.setAutoCommit(false);
         c.commit();
         c.close();
         c = ds.getConnection();
         if (c.getAutoCommit() == false) 
         {
            throw new EJBException("Returned and reaccessed autocommit state false!");
         } // end of if ()      
         c.close();

      }
      catch (EJBException e)
      {
         throw e;
      } // end of try-catch
      catch (Exception e)
      {
         throw new EJBException("Untested problem in test: " + e);
      } // end of catch
   }


   /**
    * Describe <code>testAutoCommit</code> method here.
    *
    * @exception EJBException if an error occurs
    * @ejb:interface-method
    * @ejb:transaction type="NotSupported"
    */
   public void testAutoCommit() throws EJBException
   {
      try 
      {
         DataSource ds = (DataSource)new InitialContext().lookup("java:/DefaultDS");
         Connection c1 = ds.getConnection();
         Connection uc1 = ((LocalConnection)c1).getUnderlyingConnection();
         if (c1.getAutoCommit() == false) 
         {
            throw new EJBException("Initial autocommit state false!");
         } // end of if ()
         
         Connection c2 = ds.getConnection();
         if (c2.getAutoCommit() == false) 
         {
            throw new EJBException("Initial autocommit state false!");
         } // end of if ()
         Statement s1 = c1.createStatement();
         Statement s2 = c2.createStatement();
         s1.execute("create table autocommittest (id integer)");
         try 
         {
            s1.execute("insert into autocommittest values (1)");
            uc1.rollback();
            ResultSet rs2 = s2.executeQuery("select * from autocommittest where id = 1");
            if (!rs2.next()) 
            {
               throw new EJBException("Row not visible to other connection, autocommit failed");
            } // end of if ()
            rs2.close();

         }
         finally
         {
            s1.execute("drop table autocommittest"); 
            s1.close();
            c1.close();
            s2.close();
            c2.close();
         } // end of try-catch
         

      }
      catch (EJBException e)
      {
         throw e;
      } // end of try-catch
      catch (Exception e)
      {
         throw new EJBException("Untested problem in test: " + e);
      } // end of catch
      
   }


   /**
    * Describe <code>testTxIsolationInReturnedConnection</code> method here.
    *
    * @exception EJBException if an error occurs
    * @ejb:interface-method
    * @ejb:transaction type="NotSupported"
    */
   /* This test requires a real database, not hsqldb, which apparently has no tx isolation!
   public void testTxIsolationInReturnedConnection() throws EJBException
   {
      try 
      {
         DataSource ds = (DataSource)new InitialContext().lookup("java:/SingleConnectionDS");
         Connection c = ds.getConnection();
         int ti = c.getTransactionIsolation();
         if (Connection.TRANSACTION_READ_COMMITTED == c.getTransactionIsolation()) 
         {
            throw new EJBException("default tx isolation read committed!");
         } // end of if ()
         log.info("tx isolation: " + ti);
         c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
         if (Connection.TRANSACTION_READ_COMMITTED != c.getTransactionIsolation()) 
         {
            throw new EJBException("Cannot set tx isolation!");
         } // end of if ()
         
         c.close();
         c = ds.getConnection();
         if (ti != c.getTransactionIsolation()) 
         {
            throw new EJBException("Returned and reaccessed tx isolation state wrong!");
         } // end of if ()      
         c.close();

      }
      catch (EJBException e)
      {
         throw e;
      } // end of try-catch
      catch (Exception e)
      {
         throw new EJBException("Untested problem in test: " + e);
      } // end of catch
   }
   */

   public void ejbCreate() 
   {
   }


   public void ejbActivate() throws RemoteException
   {
   }

   public void ejbPassivate() throws RemoteException
   {
   }

   public void ejbRemove() throws RemoteException
   {
   }

   public void setSessionContext(SessionContext ctx) throws RemoteException
   {
   }

   public void unsetSessionContext() throws RemoteException
   {
   }

}


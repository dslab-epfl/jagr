/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

//import org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerError;
import java.rmi.ServerException;
import java.util.Map;

import javax.ejb.EJBObject;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.transaction.TransactionRolledbackException;

import org.apache.log4j.NDC;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.metadata.BeanMetaData;


/** 
 * An interceptor used to log all invocations. It also handles any
 * unexpected exceptions.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.8 $
 */
public class LogInterceptor extends AbstractInterceptor
{
   // Static --------------------------------------------------------
   
   // Attributes ----------------------------------------------------
   protected String ejbName;
   protected boolean callLogging;
   protected Container container;
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setContainer(Container container)
   {
      this.container = container;
   }
   
   public Container getContainer()
   {
      return container;
   }
   
   // Container implementation --------------------------------------
   public void create()
      throws Exception
   {
      super.start();
      
      BeanMetaData md = getContainer().getBeanMetaData();
      ejbName = md.getEjbName();

      // Should we log call details
      callLogging = md.getContainerConfiguration().getCallLogging();
   }
   
   /**
    * This method logs the method, calls the next invoker, and handles 
    * any exception.
    *
    * @param invocation contain all infomation necessary to carry out the 
    * invocation
    * @return the return value of the invocation
    * @exception Exception if an exception during the invocation
    */
   public Object invokeHome(Invocation invocation)
      throws Exception
   {
      NDC.push(ejbName);

      String methodName;
      if (invocation.getMethod() != null) 
      {
         methodName = invocation.getMethod().getName();
      }
      else
      {
         methodName = "<no method>";
      }
      
      boolean trace = log.isTraceEnabled();
      if (trace)
      {
         log.trace("Start method=" + methodName);
      }

      // Log call details
      if (callLogging)
      {
         StringBuffer str = new StringBuffer("InvokeHome: ");

	 //// BEGIN: MIKECHEN
	 if (invocation.getId() != null)
         {
            str.append("[id=" + invocation.getId().toString() + "], ");
         }
	 //str.append("objectName = " + invocation.getObjectName());
	 //str.append("beanClass = " + container.getBeanClass().getName());
	 str.append("container=");
	 str.append(getContainer());
	 str.append(", ");
	 str.append(ejbName);
	 str.append(".");
	 //// END: MIKECHEN

         str.append(methodName);
         str.append("(");
         Object[] args = invocation.getArguments();
         if (args != null)
         {
            for (int i = 0; i < args.length; i++)
            {
               if (i > 0) 
               {
                  str.append(",");
               }
               str.append(args[i]);
            }
         }
         str.append(")");
	 //// BEGIN: MIKECHEN
	 //// upgraded the log level from debug to info
         log.info(str.toString());
	 //// END: MIKECHEN
      }

      try
      {
         return getNext().invokeHome(invocation);
      }
      catch(Throwable e)
      {
         throw handleException(e, invocation);
      }
      finally
      {
         if (trace)
         {
            log.trace("End method=" + methodName);
         }
         NDC.pop();
      }
   }

   /**
    * This method logs the method, calls the next invoker, and handles 
    * any exception.
    *
    * @param invocation contain all infomation necessary to carry out the 
    * invocation
    * @return the return value of the invocation
    * @exception Exception if an exception during the invocation
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      NDC.push(ejbName);


      String methodName;
      if (invocation.getMethod() != null) 
      {
         methodName = invocation.getMethod().getName();
      }
      else 
      {
         methodName = "<no method>";
      }

      boolean trace = log.isTraceEnabled();
      if (trace)
      {
         log.trace("Start method=" + methodName);
      }

      //System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      //System.err.println("container.ejbModule: " + getContainer().getEjbModule().getName());
      //System.err.println("container.getBeanClass: " + getContainer().getBeanClass().getName());
      //System.err.println("ejbName: " + ejbName);



      ////////////////////////////////////////////////////////////
     //// read in the stack trace
      //System.err.println("LogInterceptor");
      /*
	 try {
	     throw new Exception();
	 }
	 catch (Exception e) {
      java.io.StringWriter sw = new java.io.StringWriter();
      java.io.PrintWriter pw = new java.io.PrintWriter(sw);
      e.printStackTrace(pw);
      
      String stacktrace = new String(sw.getBuffer());
      java.io.BufferedReader br = new java.io.BufferedReader(new java.io.StringReader(stacktrace));
      
      String line;
      String prevLine = null;
      int remaining = 1;
      String caller = null;
      String callee = null;
      String callerMethod = null;
      String calleeMethod = null;

      try {
	  while ((line = br.readLine()) != null) {
	      if (line.indexOf("sun.reflect") != -1) {
		  if (prevLine != null) {
		      prevLine = prevLine.substring("at ".length());
		      prevLine = prevLine.substring(0, prevLine.indexOf("("));
		      //str.append(", caller is ");
		      //str.append(prevLine);
		      
		      // sourceEjb is the callee
		      String ejb    = prevLine.substring(0, prevLine.lastIndexOf("."));
		      String method = prevLine.substring(prevLine.lastIndexOf(".")+1);
		      if (remaining == 2) {
			  callee = ejb;
			  calleeMethod = method;
		      }
		      else {
			  caller = ejb;
			  callerMethod = method;
		      }
		      
		  }
		  remaining--;
		  if (remaining == 0)
		      break;
	      }
	      prevLine = line;
	  }
	  //System.err.println(caller + "." + callerMethod + " --> " + callee + "." + calleeMethod);
	  System.err.println(caller + "." + callerMethod + " --> " + ejbName + "." + methodName);
      }
      catch (Exception oe) {
	  oe.printStackTrace();
      }
	 }
      */
	 /////////////////////////////////////////////////////////////



      // Log call details
      if (callLogging)
      {
         StringBuffer str = new StringBuffer("Invoke: ");	     
         if (invocation.getId() != null)
         {
            str.append("[id=" + invocation.getId().toString() + "], ");
         }
	 //// BEGIN: MIKECHEN
	 /*
	 try {
	     throw new Exception();
	 }
	 catch (Exception fakeE) {
	     fakeE.printStackTrace();
	 }
	 */
	 str.append("container=");
	 str.append(getContainer());
	 str.append(", ");
	 str.append(ejbName);
	 str.append(".");



 


	 //// END: MIKECHEN
         str.append(methodName);
         str.append("(");
         Object[] args = invocation.getArguments();
         if (args != null)
         {
            for (int i = 0; i < args.length; i++)
            {
               if (i > 0)
               {
                  str.append(",");
               }
               str.append(args[i]);
            }
         }
         str.append(")");
	 //// BEGIN: MIKECHEN
	 //// upgraded the log level from debug to info
         log.info(str.toString());
	 //// END: MIKECHEN
      }

      try
      {
         return getNext().invoke(invocation);
      }
      catch(Throwable e)
      {
         throw handleException(e, invocation);
      }
      finally
      {
         if (trace)
         {
            log.trace("End method=" + methodName);
         }
         NDC.pop();
      }
   }

   // Private -------------------------------------------------------
   private Exception handleException(Throwable e, Invocation invocation)
   {

      int type = invocation.getType();
      boolean isLocal = 
            type == Invocation.LOCAL ||
            type == Invocation.LOCALHOME;
      
      if (e instanceof TransactionRolledbackLocalException ||
            e instanceof TransactionRolledbackException)
      {
         // If we got a remote TransactionRolledbackException for a local
         // invocation convert it into a TransactionRolledbackLocalException
         if (isLocal && e instanceof TransactionRolledbackException) 
         {
            TransactionRolledbackException remoteTxRollback = 
                  (TransactionRolledbackException)e;

            Exception cause;
            if (remoteTxRollback.detail instanceof Exception) 
            {
               cause = (Exception)remoteTxRollback.detail;
            }
            else if (remoteTxRollback.detail instanceof Error) 
            {
               String msg = formatException(
                     "Unexpected Error", 
                     remoteTxRollback.detail);
               cause = new EJBException(msg);
            }
            else 
            {
               String msg = formatException(
                     "Unexpected Throwable", 
                     remoteTxRollback.detail);
               cause = new EJBException(msg);
            }

            e = new TransactionRolledbackLocalException(
                  remoteTxRollback.getMessage(),
                  cause);
         }

         // If we got a local TransactionRolledbackLocalException for a remote
         // invocation convert it into a TransactionRolledbackException
         if (!isLocal && e instanceof TransactionRolledbackLocalException) 
         {
            TransactionRolledbackLocalException localTxRollback = 
                  (TransactionRolledbackLocalException)e;
            e = new TransactionRolledbackException(
                  localTxRollback.getMessage());
            ((TransactionRolledbackException)e).detail = 
                  localTxRollback.getCausedByException();
         }

         // get the data we need for logging
         Throwable cause = null;
         String exceptionType = null;
         if (e instanceof TransactionRolledbackException)
         {
            cause = ((TransactionRolledbackException)e).detail;
            exceptionType = "TransactionRolledbackException";
         } 
         else 
         {
            cause = 
               ((TransactionRolledbackLocalException)e).getCausedByException();
            exceptionType = "TransactionRolledbackLocalException";
         }

         // log the exception
         if (cause != null)
         {
            // if the cause is an EJBException unwrap it for logging
            if ((cause instanceof EJBException) &&
                  (((EJBException) cause).getCausedByException() != null))
            {
               cause = ((EJBException) cause).getCausedByException();
            }
            log.error(exceptionType + ", causedBy:", cause);
         }
         else
         {
            log.error(exceptionType + ":", e);
         }
         return (Exception)e;
      }
      else if (e instanceof NoSuchEntityException)
      {
         NoSuchEntityException noSuchEntityException = 
               (NoSuchEntityException) e;
         if (noSuchEntityException.getCausedByException() != null)
         {
            log.error("NoSuchEntityException, causedBy:", 
                  noSuchEntityException.getCausedByException());
         }
         else
         {
            log.error("NoSuchEntityException:", noSuchEntityException);
         }

         if (isLocal) 
         {
            return new NoSuchObjectLocalException(
                  noSuchEntityException.getMessage(),
                  noSuchEntityException.getCausedByException());
         }
         else 
         {
            NoSuchObjectException noSuchObjectException = 
                  new NoSuchObjectException(noSuchEntityException.getMessage());
            noSuchObjectException.detail = noSuchEntityException;
            return noSuchObjectException;
         }
      }
      else if (e instanceof EJBException)
      {
         EJBException ejbException = (EJBException) e;
         if (ejbException.getCausedByException() != null)
         {
            log.error("EJBException, causedBy:", 
                  ejbException.getCausedByException());
         }
         else
         {
            log.error("EJBException:", ejbException);
         }

         if (isLocal) 
         {
            return ejbException;
         }
         else 
         {
            // Remote invocation need a remote exception
            return new ServerException("EJBException:", ejbException);
         }
      }
      else if (e instanceof RuntimeException)
      {
         RuntimeException runtimeException = (RuntimeException)e;
         log.error("RuntimeException:", runtimeException);

         if (isLocal) 
         {
            return new EJBException("RuntimeException", runtimeException);
         } 
         else 
         {
            return new ServerException("RuntimeException", runtimeException);
         }
      }
      else if (e instanceof Error)
      {
         log.error("Unexpected Error:", e);
         if (isLocal) 
         {
            String msg = formatException("Unexpected Error", e);
            return new EJBException(msg);
         }
         else 
         {
            return new ServerError("Unexpected Error", (Error)e);
         }
      }
      else if (e instanceof Exception)
      {
         if (callLogging)
         {
            log.info("Application Exception", e);
         }
         return (Exception)e;
      }
      else
      {
         // The should not happen
         String msg = formatException("Unexpected Throwable", e);
         log.warn("Unexpected Throwable", e);
         if (isLocal)
         {
            return new EJBException(msg);
         }
         else
         {
            return new ServerException(msg);
         }
      }
   }

   private String formatException(String msg, Throwable t)
   {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      if (msg != null)
         pw.println(msg);
      t.printStackTrace(pw);
      return sw.toString();
   }
   
   // Monitorable implementation ------------------------------------
   public void sample(Object s)
   {
      // Just here to because Monitorable request it but will be removed soon
   }
   public Map retrieveStatistic()
   {
      return null;
   }
   public void resetStatistic()
   {
   }
}

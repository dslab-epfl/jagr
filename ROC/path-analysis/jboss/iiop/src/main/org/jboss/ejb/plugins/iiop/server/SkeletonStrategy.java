/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.iiop.server;

import java.lang.reflect.Method;

import java.rmi.RemoteException;
import org.omg.CORBA.portable.UnknownException;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import org.jboss.iiop.rmi.ExceptionAnalysis;
import org.jboss.iiop.rmi.RMIIIOPViolationException;

import org.jboss.ejb.plugins.iiop.CDRStream;
import org.jboss.ejb.plugins.iiop.CDRStreamReader;
import org.jboss.ejb.plugins.iiop.CDRStreamWriter;

/**
 * A <code>SkeletonStrategy</code> for a given method knows how to unmarshal
 * the sequence of method parameters from a CDR input stream, how to marshal
 * into a CDR output stream the return value of the method, and how to marshal
 * into a CDR output stream any exception thrown by the method.
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 1.1.1.1 $
 */
public class SkeletonStrategy 
{
   /**
    * Each <code>CDRStreamReader</code> in the array unmarshals a method 
    * parameter.
    */
   private CDRStreamReader[] paramReaders;

   /**
    * A <code>Method</code> instance.
    */
   private Method m;

   /**
    * Each <code>ExceptionWriter</code> in the array knows how to marshal 
    * an exception that may be thrown be the method. The array is sorted so
    * that no writer for a derived exception appears after the base 
    * exception writer.
    */
   private ExceptionWriter[] excepWriters;

   /**
    * A <code>CDRStreamWriter</code> that marshals the return value of the 
    * method.
    */
   private CDRStreamWriter retvalWriter;

   // Public  -----------------------------------------------------------------

   /*
    * Constructs a <code>SkeletonStrategy</code> for a given method.
    */
   public SkeletonStrategy(Method m) 
   {
      // Keep the method
      this.m = m;

      // Initialize paramReaders
      Class[] paramTypes = m.getParameterTypes();
      int len = paramTypes.length;
      paramReaders = new CDRStreamReader[len];
      for (int i = 0; i < len; i++) {
            paramReaders[i] = CDRStream.readerFor(paramTypes[i]);
      }

      // Initialize excepWriters
      Class[] excepTypes = m.getExceptionTypes();
      len = excepTypes.length; 
      int n = 0;
      for (int i = 0; i < len; i++) {
         if (!RemoteException.class.isAssignableFrom(excepTypes[i])) {
            n++;
         }
      }
      excepWriters = new ExceptionWriter[n];
      int j = 0;
      for (int i = 0; i < len; i++) {
         if (!RemoteException.class.isAssignableFrom(excepTypes[i])) {
            excepWriters[j++] = new ExceptionWriter(excepTypes[i]);
         }
      }
      ExceptionWriter.arraysort(excepWriters);

      // Initialize retvalWriter
      retvalWriter = CDRStream.writerFor(m.getReturnType());
   }

   /**
    * Unmarshals the sequence of method parameters from an input stream.
    *
    * @param in  a CDR input stream
    * @return    an object array with the parameters.
    */
   public Object[] readParams(InputStream in) 
   {
      int len = paramReaders.length;
      Object[] params = new Object[len];
      for (int i = 0; i < len; i++ ) {
         params[i] = paramReaders[i].read(in);
      }
      return params;
   }
   
   /**
    * Returns this <code>SkeletonStrategy</code>'s method.
    */
   public Method getMethod() 
   {
      return m;
   }

   /**
    * Returns true if this <code>SkeletonStrategy</code>'s method is non void.
    */
   public boolean isNonVoid() 
   {
      return (retvalWriter != null);
   }

   /**
    * Marshals into an output stream the return value of the method.
    *
    * @param out    a CDR output stream
    * @param retVal the value to be written.
    */
   public void writeRetval(OutputStream out, Object retVal) 
   {
      retvalWriter.write(out, retVal);
   }

   /**
    * Marshals into an output stream an exception thrown by the method.
    *
    * @param out    a CDR output stream
    * @param e      the exception to be written.
    */
   public void writeException(OutputStream out, Exception e) 
   {
      int len = excepWriters.length;
      for (int i = 0; i < len; i++) {
         if (excepWriters[i].getExceptionClass().isInstance(e)) {
            excepWriters[i].write(out, e);
            return;
         }
      }
      throw new UnknownException(e);
   }

   // Static inner class (private) --------------------------------------------

   /**
    * An <code>ExceptionWriter</code> knows how to write exceptions of a given
    * class to a CDR output stream.
    */
   private static class ExceptionWriter 
         implements CDRStreamWriter 
   {
      /**
       * The exception class.
       */
      private Class clz;
      
      /**
       * The CORBA repository id of the exception class.
       */
      private String reposId;
      
      /**
       * Constructs an <code>ExceptionWriter</code> for a given exception 
       * class.
       */
      ExceptionWriter(Class clz) 
      {
         this.clz = clz;
         try {
            this.reposId = ExceptionAnalysis.getExceptionAnalysis(clz)
               .getExceptionRepositoryId();
         }
         catch (RMIIIOPViolationException e) {
            throw new RuntimeException("Cannot obtain "
                                       + "exception repository id for " 
                                       + clz.getName() + ":\n" + e);
         }
      }
      
      /**
       * Gets the exception <code>Class</code>.
       */
      Class getExceptionClass() 
      {
         return clz;
      }
      
      /**
       * Writes an exception to a CDR output stream.
       */
      public void write(OutputStream out, Object excep) 
      {
         out.write_string(reposId);
         out.write_value((Exception)excep, clz);
      }
      
      /**
       * Sorts an <code>ExceptionWriter</code> array so that no derived 
       * exception strategy appears after a base exception strategy.
       */
      static void arraysort(ExceptionWriter[] a) 
      {
         int len = a.length;
         
         for (int i = 0; i < len - 1; i++) {
            for (int j = i + 1; j < len; j++) {
               if (a[i].clz.isAssignableFrom(a[j].clz)) {
                  ExceptionWriter tmp = a[i];
                  
                  a[i] = a[j];
                  a[j] = tmp;
               }
            }
         }
      }
      
   } // end of inner class ExceptionWriter
   
}

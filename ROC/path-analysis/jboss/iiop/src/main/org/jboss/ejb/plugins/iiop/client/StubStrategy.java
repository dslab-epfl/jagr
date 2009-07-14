/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.iiop.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.rmi.UnexpectedException;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import org.jboss.ejb.plugins.iiop.CDRStream;
import org.jboss.ejb.plugins.iiop.CDRStreamReader;
import org.jboss.ejb.plugins.iiop.CDRStreamWriter;

/**
 * A <code>StubStrategy</code> for a given method knows how to marshal
 * the sequence of method parameters into a CDR output stream, how to unmarshal
 * from a CDR input stream the return value of the method, and how to unmarshal
 * from a CDR input stream an application exception thrown by the method.
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 1.1.1.1 $
 */
public class StubStrategy
{
   // Fields ------------------------------------------------------------------

   /**
    * Each <code>CDRStreamWriter</code> in the array marshals a method 
    * parameter.
    */
   private CDRStreamWriter[] paramWriters;

   /**
    * List of exception classes.
    */
   private List exceptionList;

   /**
    * Maps exception repository ids into exception classes.
    */
   private Map exceptionMap;

   /**
    * A <code>CDRStreamReader</code> that unmarshals the return value of the 
    * method.
    */
   private CDRStreamReader retvalReader;

   /**
    * If this <code>StubStrategy</code> is for a method that returns a
    * remote interface, this field contains the remote interface's 
    * <code>Class</code>. Otherwise it contains null.
    */
   private Class retvalRemoteInterface;

  // Static ------------------------------------------------------------------

   /**
    * Returns a <StubStrategy> for a method, given descriptions of the 
    * method parameters, exceptions, and return value. Parameter and return 
    * value descriptions are "marshaller abbreviated names".
    *
    * @param paramTypes  a string array with marshaller abbreviated names for
    *                    the method parameters
    * @param excepIds    a string array with the CORBA repository ids of the
    *                    exceptions thrown by the method
    * @param excepTypes  a string array with the Java class names of the 
    *                    exceptions thrown by the method
    * @param retvalType  marshaller abbreaviated name for the return value of
    *                    the method
    * @param cl          a <code>ClassLoader</code> to load value classes 
    *                    (if null, the current thread's context class loader 
    *                    will be used)
    * @return a <StubStrategy> for the operation with the parameters,
    *         exceptions, and return value specified.
    * @see org.jboss.ejb.plugins.iiop.CDRStream#abbrevFor(Class clz)
    */
   public static StubStrategy forMethod(String[] paramTypes, 
                                        String[] excepIds,
                                        String[] excepTypes, 
                                        String retvalType, 
                                        ClassLoader cl) 
   {
      // This "factory method" exists just because I have found it easier 
      // to invoke a static method (rather than invoking operator new) 
      // from a stub class dynamically assembled by an instance of
      // org.jboss.proxy.ProxyAssembler.

      return new StubStrategy(paramTypes, excepIds, 
                              excepTypes, retvalType, cl);
   }


   // Constructor -------------------------------------------------------------

   /**
    * Constructs a <StubStrategy> for a method, given descriptions of the 
    * method parameters, exceptions, and return value. Parameter and return 
    * value descriptions are "marshaller abbreviated names".
    *
    * @param paramTypes  a string array with marshaller abbreviated names for
    *                    the method parameters
    * @param excepIds    a string array with the CORBA repository ids of the
    *                    exceptions thrown by the method
    * @param excepTypes  a string array with the Java class names of the 
    *                    exceptions thrown by the method
    * @param retvalType  marshaller abbreaviated name for the return value of
    *                    the method
    * @param cl          a <code>ClassLoader</code> to load value classes 
    *                    (if null, the current thread's context class loader 
    *                    will be used)
    * @see org.jboss.ejb.plugins.iiop.CDRStream#abbrevFor(Class clz)
    */
   private StubStrategy(String[] paramTypes, String[] excepIds, 
                        String[] excepTypes, String retvalType, 
                        ClassLoader cl) 
   {
      if (cl == null) {
         cl = Thread.currentThread().getContextClassLoader();
      }
      
      // Initialize paramWriters
      int len = paramTypes.length;
      paramWriters = new CDRStreamWriter[len];
      for (int i = 0; i < len; i++) {
            paramWriters[i] = CDRStream.writerFor(paramTypes[i], cl);
      }

      // Initialize exception list and exception map
      exceptionList = new ArrayList();
      exceptionMap = new HashMap();
      len = excepIds.length;
      for (int i = 0; i < len; i++) {
         try {
            Class clz = cl.loadClass(excepTypes[i]);
            exceptionList.add(clz);
            exceptionMap.put(excepIds[i] , clz);
         }
         catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading class " 
                                       + excepTypes[i] + ": " + e);
         }
      }

      // Initialize retvalReader
      retvalReader = CDRStream.readerFor(retvalType, cl);

      // Initialize retvalRemoteInterface
      if (retvalType.charAt(0) == 'R') {
         try {
            retvalRemoteInterface = cl.loadClass(retvalType.substring(1));
         }
         catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading class " 
                                       + retvalType.substring(1) + ": " + e);

         }
      }
   }

   // Public  -----------------------------------------------------------------

   /**
    * Marshals the sequence of method parameters into an output stream.
    *
    * @param out    a CDR output stream
    * @param params an object array with the parameters.
    */
   public void writeParams(OutputStream out, Object[] params) 
   {
      int len = params.length;
      
      if (len != paramWriters.length) {
          throw new RuntimeException("Cannot marshal parameters: "
                                     + "unexpected number of parameters");
      }
      for (int i = 0; i < len; i++ ) {
         paramWriters[i].write(out, params[i]);
      }
   }
   
   /**
    * Returns true if this <code>StubStrategy</code>'s method is non void.
    */
   public boolean isNonVoid() 
   {
      return (retvalReader != null);
   }

   /**
    * Unmarshals from an input stream the return value of the method.
    *
    * @param in    a CDR input stream
    * @return      a value unmarshaled from the stream.
    */
   public Object readRetval(InputStream in) 
   {
      return retvalReader.read(in);
   }

   /**
    * Unmarshals from an input stream an exception thrown by the method.
    *
    * @param in    a CDR input stream
    * @return      an exception unmarshaled from the stream.
    */
   public Exception readException(InputStream in) 
   {
      String repositoryId = in.read_string();
      Class exceptionClass = (Class)exceptionMap.get(repositoryId);
      if (exceptionClass == null) {
         return new UnexpectedException(repositoryId);
      }
      else {
         return (Exception)in.read_value(exceptionClass);
      }
   }

   /**
    * Checks if a given <code>Throwable</code> instance corresponds to an 
    * exception declared by this <code>StubStrategy</code>'s method.
    *
    * @param t     an exception class
    * @return      true if <code>t</code> is an instance of any of the 
    *              exceptions declared by this <code>StubStrategy</code>'s 
    *              method, false otherwise.
    */
   public boolean isDeclaredException(Throwable t)
   {
      Iterator iterator = exceptionList.iterator();
      while (iterator.hasNext()) {
         if (((Class)iterator.next()).isInstance(t)) {
            return true;
         }
      }
      return false;
   }
   
   /**
    * Converts the return value of a local invocation into the expected type.
    * A conversion is needed if the return value is a remote interface
    * (in this case <code>PortableRemoteObject.narrow()</code> must be called).
    * 
    * @param obj the return value to be converted
    * @return the converted value.
    */
   public Object convertLocalRetval(Object obj) 
   {
      if (retvalRemoteInterface == null)
         return obj;
      else 
         return PortableRemoteObject.narrow(obj, retvalRemoteInterface);
   }
      
}

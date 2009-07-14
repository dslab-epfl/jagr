/*
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.http.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.MarshalledValue;
import org.jboss.logging.Logger;
import org.jboss.util.jmx.MBeanServerLocator;
import org.jboss.util.jmx.JMXExceptionDecoder;

/** This servlet accepts a post containing a MarshalledInvocation, extracts
 the Invocation object, and then routes the invocation via JMX so the object
 specified via the invokerName ini parameter. The method signature of the
 invoker must be Object invoke(org.jboss.invocation.Invocation).

 * @author  Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class InvokerServlet extends HttpServlet
{
   private static Logger log = Logger.getLogger(InvokerServlet.class);
   /** A serialized MarshalledInvocation */
   private static String REQUEST_CONTENT_TYPE =
      "application/x-java-serialized-object; class=org.jboss.invocation.MarshalledInvocation";
   /** A serialized MarshalledValue */
   private static String RESPONSE_CONTENT_TYPE =
      "application/x-java-serialized-object; class=org.jboss.invocation.MarshalledValue";
   private MBeanServer mbeanServer;
   private ObjectName localInvokerName;

   /** Initializes the servlet.
    */
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      try
      {
         String name = config.getInitParameter("invokerName");
         if( name == null )
            name = "jboss:service=invoker,type=http";
         localInvokerName = new ObjectName(name);
         log.debug("localInvokerName="+localInvokerName);
         mbeanServer = MBeanServerLocator.locate();
      }
      catch(MalformedObjectNameException e)
      {
         throw new ServletException("Failed to build invokerName", e);
      }
   }

   /** Destroys the servlet.
    */
   public void destroy()
   {
      
   }

   /** Read a MarshalledInvocation and dispatch it to the target JMX object
    invoke(Invocation) object.

    @param request servlet request
    @param response servlet response
    */
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
      {
         log.debug("processRequest, ContentLength: "+request.getContentLength());
         log.debug("processRequest, ContentType: "+request.getContentType());
      }

      try
      {
         response.setContentType(RESPONSE_CONTENT_TYPE);
         // See if the request already has the MarshalledInvocation
         MarshalledInvocation mi = (MarshalledInvocation) request.getAttribute("MarshalledInvocation");
         if( mi == null )
         {
            // Get the invocation from the post
            ServletInputStream sis = request.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(sis);
            mi = (MarshalledInvocation) ois.readObject();
            ois.close();
         }
         Object[] params = {mi};
         String[] sig = {"org.jboss.invocation.Invocation"};
         Object value = mbeanServer.invoke(localInvokerName, "invoke", params, sig);
         MarshalledValue mv = new MarshalledValue(value);
         ServletOutputStream sos = response.getOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(sos);
         oos.writeObject(mv);
         oos.close();
      }
      catch(Throwable t)
      {
         t = JMXExceptionDecoder.decode(t);
         log.error("Invoke failed", t);
         // Marshall the exception
         response.resetBuffer();
         MarshalledValue mv = new MarshalledValue(t);
         ServletOutputStream sos = response.getOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(sos);
         oos.writeObject(mv);
         oos.close();
      }
   }

   /** Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      processRequest(request, response);
   }

   /** Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      processRequest(request, response);
   }
   
   /** Returns a short description of the servlet.
    */
   public String getServletInfo()
   {
      return "An HTTP to JMX invocation servlet";
   }
   
}

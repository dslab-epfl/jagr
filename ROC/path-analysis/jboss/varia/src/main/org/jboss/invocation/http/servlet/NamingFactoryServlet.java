/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation.http.servlet;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.invocation.InvokerInterceptor;
import org.jboss.invocation.MarshalledValue;
import org.jboss.invocation.http.interfaces.HttpInvokerProxy;
import org.jboss.logging.Logger;
import org.jboss.proxy.GenericProxyFactory;
import org.jboss.system.Registry;
import org.jboss.util.Strings;
import org.jnp.interfaces.Naming;

/** Create a Naming interface proxy that uses HTTP to communicate with the
 * JBoss JNDI naming service. Any request to this servlet receives a 
 * serialized object stream containing a MarshalledValue with the Naming proxy
 * as its content.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class NamingFactoryServlet extends HttpServlet
{
   /** A serialized MarshalledValue */
   private static String RESPONSE_CONTENT_TYPE =
      "application/x-java-serialized-object; class=org.jboss.invocation.MarshalledValue";
   private static String DEFAULT_INVOKER = "jboss:service=Naming";
   private Logger log;

   private ObjectName namingInvokerName;
   private Object namingProxy;
   private URL externalURL;

   /** Initializes the servlet.
    */
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      String category = getClass().getName() + '.' + config.getServletName();
      log = Logger.getLogger(category);
      try
      {
         String name = config.getInitParameter("invokerName");
         if( name == null )
            name = DEFAULT_INVOKER;
         namingInvokerName = new ObjectName(name);
         log.debug("Using invokerName="+namingInvokerName);
      }
      catch(MalformedObjectNameException e)
      {
         throw new ServletException("Failed to build invokerName", e);
      }

      try
      {
         // Export the Invoker interface
         String externalURL = config.getInitParameter("externalURL");
         if( externalURL == null )
            externalURL = "http://localhost:8080/invoker/InvokerServlet";
         // Replace any system property references
         externalURL = Strings.replaceProperties(externalURL);
         log.debug("Using externalURL="+externalURL);
         /** Create an HttpInvokerProxy that posts invocations to the
          externalURL. This proxy will be associated with a naming JMX invoker
          given by the namingInvokerName.
          */
         HttpInvokerProxy delegateInvoker = new HttpInvokerProxy(externalURL);
         Integer nameHash = new Integer(namingInvokerName.hashCode());
         Registry.bind(namingInvokerName, delegateInvoker);
         Registry.bind(nameHash, namingInvokerName);

         Object cacheID = null;
         String jndiName = null;
         Class[] ifaces = {Naming.class};
         ArrayList interceptorClasses = new ArrayList();
         interceptorClasses.add(InvokerInterceptor.class);
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         GenericProxyFactory proxyFactory = new GenericProxyFactory();
         namingProxy = proxyFactory.createProxy(cacheID, namingInvokerName,
            jndiName, interceptorClasses, loader, ifaces);
         log.debug("Created Naming proxy, namingInvoker="+namingInvokerName);
      }
      catch(Exception e)
      {
         log.debug("Failed to build invokerName", e);
         throw new ServletException("Failed to build invokerName", e);
      }
   }

   /** Destroys the servlet.
    */
   public void destroy()
   {
      
   }
   
   /** Return a Naming service proxy for any GET/POST made against this servlet
    * @param response servlet response
    */
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("processRequest");
      try
      {
         response.setContentType(RESPONSE_CONTENT_TYPE);
         MarshalledValue mv = new MarshalledValue(namingProxy);
         if( trace )
            log.trace("Serialized Naming proxy, size="+mv.size());
         //response.setContentLength(mv.size());
         ServletOutputStream sos = response.getOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(sos);
         oos.writeObject(mv);
         oos.flush();
         oos.close();
      }
      catch(Throwable t)
      {
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
      return "A factory servlet for Naming proxies";
   }
   
}

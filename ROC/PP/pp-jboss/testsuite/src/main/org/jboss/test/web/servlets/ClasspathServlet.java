package org.jboss.test.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.test.web.util.Util;

/** A servlet that accesses classes in WEB-INF/classes using Class.forName
 * during its initialization.
 *
 * @author  Scott.Scott@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class ClasspathServlet extends HttpServlet
{
   org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(getClass());
   StringBuffer initInfo = new StringBuffer();
   
   public void init(ServletConfig config) throws ServletException
   {
      String param = config.getInitParameter("failOnError");
      boolean failOnError = true;
      if( param != null && Boolean.valueOf(param).booleanValue() == false )
         failOnError = false;
      try
      {
         Class clazz = Class.forName("org.jboss.test.web.util.ClassInClasses");
         initInfo.append("Successfully loaded class: "+clazz.getName());
         ClassLoader cl = clazz.getClassLoader();
         ProtectionDomain pd = clazz.getProtectionDomain();
         CodeSource cs = pd.getCodeSource();
         initInfo.append("\n  ClassLoader : "+cl.getClass().getName()+':'+cl.hashCode());
         initInfo.append("\n  CodeSource.location : "+cs.getLocation());
      }
      catch(Exception e)
      {
         log.error("Failed to init", e);
         if( failOnError == true )
            throw new ServletException("Failed to init ClasspathServlet", e);
         else
         {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            initInfo.append("\nFailed to init\n");
            initInfo.append(sw.toString());
         }
      }
   }

   public void destroy()
   {
   }

   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<html>");
      out.println("<head><title>ClasspathServlet</title></head>");
      out.println("<body>Was initialized<br>");
      out.println("<pre>\n");
      out.println(initInfo.toString());
      out.println("</pre>\n");
      out.println("</html>");
      out.close();
   }

   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      processRequest(request, response);
   }
   
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      processRequest(request, response);
   }
}

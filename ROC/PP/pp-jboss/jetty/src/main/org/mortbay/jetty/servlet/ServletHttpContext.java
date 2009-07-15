// ========================================================================
// Copyright (c) 2001 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ServletHttpContext.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.jetty.servlet;

import javax.servlet.ServletContext;
import org.mortbay.http.HttpContext;
import org.mortbay.util.Code;

/* ------------------------------------------------------------ */
/** ServletHttpContext.
 * Extends HttpContext with conveniance methods for adding servlets.
 * Enforces a single ServletHandler per context.
 * @version $Id: ServletHttpContext.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
 * @author Greg Wilkins (gregw)
 */
public class ServletHttpContext extends HttpContext
{    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param server 
     * @param contextPathSpec 
     */
    public ServletHttpContext()
    {
        super();
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return The ServletContext. 
     */
    public ServletContext getServletContext()
    {
        ServletHandler shandler=getServletHandler();
        if (shandler!=null)
            return shandler.getServletContext();
        return null;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the context ServletHandler.
     * Conveniance method. If no ServletHandler exists, a new one is added to
     * the context.
     * @return ServletHandler
     */
    public synchronized ServletHandler getServletHandler()
    {
        ServletHandler shandler=(ServletHandler) getHandler(ServletHandler.class);
        if (shandler==null)
        {
            shandler=new ServletHandler();
            addHandler(shandler);
        }
        return shandler;
    }
    
    /* ------------------------------------------------------------ */
    /** Add a servlet to the context.
     * Conveniance method.
     * If no ServletHandler is found in the context, a new one is added.
     * @param name The name of the servlet.
     * @param pathSpec The pathspec within the context
     * @param className The classname of the servlet.
     * @return The ServletHolder.
     * @exception ClassNotFoundException 
     * @exception InstantiationException 
     * @exception IllegalAccessException 
     */
    public synchronized ServletHolder addServlet(String pathSpec,
                                                 String className)
        throws ClassNotFoundException,
               InstantiationException,
               IllegalAccessException
    {
        return addServlet(className,pathSpec,className);
    }
    
    /* ------------------------------------------------------------ */
    /** Add a servlet to the context.
     * If no ServletHandler is found in the context, a new one is added.
     * @param name The name of the servlet.
     * @param pathSpec The pathspec within the context
     * @param className The classname of the servlet.
     * @return The ServletHolder.
     * @exception ClassNotFoundException 
     * @exception InstantiationException 
     * @exception IllegalAccessException 
     */
    public synchronized ServletHolder addServlet(String name,
                                                 String pathSpec,
                                                 String className)
        throws ClassNotFoundException,
               InstantiationException,
               IllegalAccessException
    {
        return getServletHandler().addServlet(name,pathSpec,className,null);
    }

    /* ------------------------------------------------------------ */
    /** Setup context for serving dynamic servlets.
     * @deprecated Use org.mortbay.jetty.servlet.Invoker
     */
    public synchronized void setDynamicServletPathSpec(String pathSpecInContext)
    {
        Code.warning("setDynamicServletPathSpec is deprecated.");
    }

    /* ------------------------------------------------------------ */
    public void stop()
        throws InterruptedException
    {
        super.stop();
    }
    
    /* ------------------------------------------------------------ */
    public String toString()
    {
        return "Servlet"+super.toString(); 
    }
    
}

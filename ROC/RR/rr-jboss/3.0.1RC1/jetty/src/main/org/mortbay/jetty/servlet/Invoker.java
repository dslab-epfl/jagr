// ===========================================================================
// Copyright (c) 1996-2002 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Invoker.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ---------------------------------------------------------------------------

package org.mortbay.jetty.servlet;


import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mortbay.util.Code;
import org.mortbay.util.Log;
import org.mortbay.util.URI;

/* ------------------------------------------------------------ */
/**  Dynamic Servlet Invoker.  
 * This servlet invokes anonymous servlets that have not been defined   
 * in the web.xml or by other means. The first element of the pathInfo  
 * of a request passed to the envoker is treated as a servlet name for  
 * an existing servlet, or as a class name of a new servlet.            
 * This servlet is normally mapped to /servlet/*                        
 * This servlet support the following initParams:                       
 * <PRE>                                                                     
 *  nonContextServlets       If false, the invoker can only load        
 *                           servlets from the contexts classloader.    
 *                           This is false by default and setting this  
 *                           to true may have security implications.    
 *                                                                      
 *  verbose                  If true, log dynamic loads                 
 *                                                                      
 *  *                        All other parameters are copied to the     
 *                           each dynamic servlet as init parameters    
 * </PRE>
 * @version $Id: Invoker.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
 * @author Greg Wilkins (gregw)
 */
public class Invoker extends HttpServlet
{
    private ServletHandler _servletHandler;
    private Map.Entry _thisEntry;
    private Map _parameters;
    private boolean _systemServlets;
    private boolean _verbose;
        
    /* ------------------------------------------------------------ */
    public void init()
    {
        ServletContext config=getServletContext();
        _servletHandler=((ServletHandler.Context)config).getServletHandler();

        Enumeration e = getInitParameterNames();
        while(e.hasMoreElements())
        {
            String param=(String)e.nextElement();
            String value=getInitParameter(param);
            String lvalue=value.toLowerCase();
            if ("nonContextServlets".equals(param))
            {
                _systemServlets=value.length()>0 && value.startsWith("t");
            }
            if ("verbose".equals(param))
            {
                _verbose=value.length()>0 && value.startsWith("t");
            }
            else
            {
                if (_parameters==null)
                    _parameters=new HashMap();
                _parameters.put(param,value);
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    protected void service(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
    {
        // Get the servlet class
        String servletClass = request.getPathInfo();
        if (servletClass==null || servletClass.length()<=1 )
        {
            response.sendError(404);
            return;
        }
        
        int i0=servletClass.charAt(0)=='/'?1:0;
        int i1=servletClass.indexOf('/',i0);
        String info=i1<0?null:servletClass.substring(i1);
        servletClass=i1<0?servletClass.substring(i0):servletClass.substring(i0,i1);           
        if (servletClass.endsWith(".class"))
            servletClass=servletClass.substring(0,servletClass.length()-6);
        if (servletClass==null || servletClass.length()==0)
        {
            response.sendError(404);
            return;
        }
                    
        // Determine the path spec
        String path=URI.addPaths(request.getServletPath(),servletClass);
        
        // Try a named dispatcher
        RequestDispatcher rd = getServletContext().getNamedDispatcher(servletClass);
        if (rd!=null)
        {
            rd.forward(request,response);
            return;
        }
        
        synchronized(_servletHandler)
        {
            // setup this entry
            if (_thisEntry==null)
                _thisEntry=_servletHandler.getHolderEntry(request.getServletPath());
            
            // Look for existing mapping
            Map.Entry entry = _servletHandler.getHolderEntry(path);

            if (entry==null || entry==_thisEntry)
            {
                // Make a holder
                ServletHolder holder=new ServletHolder(_servletHandler,servletClass,servletClass);
                if (_parameters!=null)
                    holder.putAll(_parameters);
                
                try {holder.start();}
                catch (Exception e)
                {
                    Code.debug(e);
                    throw new UnavailableException(e.toString());
                }

                // Check it is from an allowable classloader
                if (!_systemServlets)
                {
                    Object servlet=holder.getServlet();
                    
                    if (_servletHandler.getClassLoader()!=
                        servlet.getClass().getClassLoader())
                    {
                        holder.stop();
                        String msg=
                            "Dynamic servlet "+
                            servletClass+
                            " not loaded from context "+
                            request.getContextPath();
                        Code.warning(msg);
                        throw new UnavailableException(msg);
                    }
                }

                if (_verbose)
                    log("Dynamic load '"+servletClass+"' at "+path);
                _servletHandler.addServletHolder(path+"/*",holder);
                _servletHandler.addServletHolder(path+".class/*",holder);
            }

            // Dispatch to path
            rd=request.getRequestDispatcher(URI.encodePath(URI.addPaths(path,info)));
            if (rd!=null)
                rd.forward(request,response);
            else
                response.sendError(404);
        }
    }
}

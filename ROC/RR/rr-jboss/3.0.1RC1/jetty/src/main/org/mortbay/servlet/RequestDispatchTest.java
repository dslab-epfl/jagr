// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: RequestDispatchTest.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ---------------------------------------------------------------------------

package org.mortbay.servlet;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.GenericServlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;
import org.mortbay.util.Code;


/* ------------------------------------------------------------ */
/** Test Servlet RequestDispatcher.
 * 
 * @version $Id: RequestDispatchTest.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
 * @author Greg Wilkins (gregw)
 */
public class RequestDispatchTest extends HttpServlet
{
    /* ------------------------------------------------------------ */
    String pageType;

    /* ------------------------------------------------------------ */
    public void init(ServletConfig config)
         throws ServletException
    {
        super.init(config);
    }

    /* ------------------------------------------------------------ */
    public void doPost(HttpServletRequest sreq, HttpServletResponse sres) 
        throws ServletException, IOException
    {
        doGet(sreq,sres);
    }
    
    /* ------------------------------------------------------------ */
    public void doGet(HttpServletRequest sreq, HttpServletResponse sres) 
        throws ServletException, IOException
    {
        sreq=new HttpServletRequestWrapper(sreq);
        sres=new HttpServletResponseWrapper(sres);
        
        String prefix = sreq.getContextPath()!=null
            ? sreq.getContextPath()+sreq.getServletPath()
            : sreq.getServletPath();
        
        sres.setContentType("text/html");

        String info ;

        if (sreq.getAttribute("javax.servlet.include.servlet_path")!=null)
            info=(String)sreq.getAttribute("javax.servlet.include.path_info");
        else
            info=sreq.getPathInfo();
        
        if (info==null)
            info="NULL";

        if (info.startsWith("/include/"))
        {
            info=info.substring(8);
            if (info.indexOf('?')<0)
                info+="?Dispatch=include";
            else
                info+="&Dispatch=include";

            
            if (System.currentTimeMillis()%2==0)
            {
                PrintWriter pout=null;
                pout = sres.getWriter();
                pout.write("<H1>Include: "+info+"</H1><HR>");
                
                RequestDispatcher dispatch = getServletContext()
                    .getRequestDispatcher(info);
                if (dispatch==null)
                {
                    pout = sres.getWriter();
                    pout.write("<H1>Null dispatcher</H1>");
                }
                else
                    dispatch.include(sreq,sres);
                
                pout.write("<HR><H1>-- Included (writer)</H1>");
            }
            else 
            {
                OutputStream out=null;
                out = sres.getOutputStream();
                out.write(("<H1>Include: "+info+"</H1><HR>").getBytes());   

                RequestDispatcher dispatch = getServletContext()
                    .getRequestDispatcher(info);
                if (dispatch==null)
                {
                    out = sres.getOutputStream();
                    out.write("<H1>Null dispatcher</H1>".getBytes());
                }
                else
                    dispatch.include(sreq,sres);
                
                out.write("<HR><H1>-- Included (outputstream)</H1>".getBytes());
            }
        }
        else if (info.startsWith("/forward/"))
        {
            info=info.substring(8);
            if (info.indexOf('?')<0)
                info+="?Dispatch=forward";
            else
                info+="&Dispatch=forward";
            RequestDispatcher dispatch =
                getServletContext().getRequestDispatcher(info);
            if (dispatch!=null)
                dispatch.forward(sreq,sres);
            else
            {
                PrintWriter pout = sres.getWriter();
                pout.write("<H1>No dispatcher for: "+info+"</H1><HR>");
                pout.flush();
            }
        }
        else if (info.startsWith("/includeN/"))
        {
            info=info.substring(10);
            PrintWriter pout;

            if (info.startsWith("/null"))
                info=info.substring(5);
            else
            {
                pout = sres.getWriter();
                pout.write("<H1>Include named: "+info+"</H1><HR>");
            }
            
            RequestDispatcher dispatch = getServletContext()
                .getNamedDispatcher(info);
            if (dispatch!=null)
                dispatch.include(sreq,sres);
            else
            {
                pout = sres.getWriter();
                pout.write("<H1>No servlet named: "+info+"</H1>");
            }
            
            pout = sres.getWriter();
            pout.write("<HR><H1>Included ");
        }
        else if (info.startsWith("/forwardN/"))
        {
            info=info.substring(10);
            RequestDispatcher dispatch = getServletContext()
                .getNamedDispatcher(info);
            if (dispatch!=null)
                dispatch.forward(sreq,sres);
            else
            {
                PrintWriter pout = sres.getWriter();
                pout.write("<H1>No servlet named: "+info+"</H1>");
                pout.flush();
            }
        }
        else
        {
            PrintWriter pout = sres.getWriter();
            pout.write("<H1>Dispatch URL must be of the form: </H1>"+
                       "<PRE>"+prefix+"/include/path\n"+
                       prefix+"/forward/path\n"+
                       prefix+"/includeN/name\n"+
                       prefix+"/forwardN/name</PRE>"
                       );
            pout.flush();
        }
    }

    /* ------------------------------------------------------------ */
    public String getServletInfo()
    {
        return "Include Servlet";
    }

    /* ------------------------------------------------------------ */
    public synchronized void destroy()
    {
        Code.debug("Destroyed");
    }
    
}

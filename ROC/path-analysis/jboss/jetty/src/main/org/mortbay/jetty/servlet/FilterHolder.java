// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: FilterHolder.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ---------------------------------------------------------------------------

package org.mortbay.jetty.servlet;

import java.util.Enumeration;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import org.mortbay.util.Code;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.PathMap;

/* --------------------------------------------------------------------- */
/** 
 * @version $Id: FilterHolder.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Greg Wilkins
 */
public class FilterHolder
    extends Holder
{
    /* ------------------------------------------------------------ */
    public static final int
        __REQUEST=1,
        __FORWARD=2,
        __INCLUDE=4,
        __ERROR=8;

    public static int type(String type)
    {
        if ("request".equalsIgnoreCase(type))
            return __REQUEST;
        if ("forward".equalsIgnoreCase(type))
            return __FORWARD;
        if ("include".equalsIgnoreCase(type))
            return __INCLUDE;
        if ("error".equalsIgnoreCase(type))
            return __ERROR;
        return 0;
    }
    
    /* ------------------------------------------------------------ */
    private PathMap _pathSpecs;
    private int _applyTo;

    private transient Filter _filter;
    private transient Config _config;
        
    /* ---------------------------------------------------------------- */
    /** Constructor for Serialization.
     */
    FilterHolder()
    {}
    
    /* ---------------------------------------------------------------- */
    public FilterHolder(HttpHandler httpHandler,
                        String name,
                        String className)
    {
        super(httpHandler,name,className);
    }

    /* ------------------------------------------------------------ */
    /** Add a type that this filter applies to.
     * @param type Of __REQUEST, __FORWARD, __INCLUDE or __ERROR
     */
    public void applyTo(int type)
    {
        _applyTo|=type;
    }

    /* ------------------------------------------------------------ */
    /** Add a type that this filter applies to.
     * @param type "REQUEST", "FORWARD", "INCLUDE" or "ERROR"
     */
    public void applyTo(String type)
    {
        _applyTo|=type(type);
    }

    
    /* ------------------------------------------------------------ */
    /** Add A path spec that this filter applies to.
     * @param pathSpec 
     */
    public void addPathSpec(String pathSpec)
    {
        if (_pathSpecs==null)
            _pathSpecs=new PathMap();
        _pathSpecs.put(pathSpec,pathSpec);
    }
    
    /* ------------------------------------------------------------ */
    public boolean isMappedToPath()
    {
        return _pathSpecs!=null;
    }

    /* ------------------------------------------------------------ */
    /** Check if this filter applies.
     * @param type The type of request: __REQUEST,__FORWARD,__INCLUDE or __ERROR.
     * @return True if this filter applies
     */
    public boolean appliesTo(int type)
    {
        return(_applyTo&type)!=0 ;
    }
    
    /* ------------------------------------------------------------ */
    /** Check if this filter applies to a path.
     * @param path The path to check.
     * @param type The type of request: __REQUEST,__FORWARD,__INCLUDE or __ERROR.
     * @return True if this filter applies
     */
    public boolean appliesTo(String path, int type)
    {
        return
            (_applyTo&type)!=0 &&
            _pathSpecs!=null &&
            _pathSpecs.getMatch(path)!=null;
    }
    
    /* ------------------------------------------------------------ */
    public String appliedPathSpec(String path)
    {
        if (_pathSpecs==null)
            return null;
        Map.Entry entry = _pathSpecs.getMatch(path);
        if (entry==null)
            return null;
        return (String)entry.getKey();
    }

    /* ------------------------------------------------------------ */
    public void start()
        throws Exception
    {
        super.start();
        
        if (!javax.servlet.Filter.class
            .isAssignableFrom(_class))
        {
            super.stop();
            throw new IllegalStateException("Servlet class "+_class+
                                            " is not a javax.servlet.Filter");
        }

        _filter=(Filter)newInstance();
        _config=new Config();
        _filter.init(_config);
    }

    /* ------------------------------------------------------------ */
    public void stop()
    {
        if (_filter!=null)
            _filter.destroy();
        _filter=null;
        _config=null;
        super.stop();   
    }
    
    /* ------------------------------------------------------------ */
    public Filter getFilter()
    {
        return _filter;
    }

    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    class Config implements FilterConfig
    {
        /* ------------------------------------------------------------ */
        public String getFilterName()
        {
            return FilterHolder.this.getName();
        }

        /* ------------------------------------------------------------ */
        public ServletContext getServletContext()
        {
            return ((WebApplicationHandler)_httpHandler).getServletContext();
        }
        
        /* -------------------------------------------------------- */
        public String getInitParameter(String param)
        {
            return FilterHolder.this.getInitParameter(param);
        }
    
        /* -------------------------------------------------------- */
        public Enumeration getInitParameterNames()
        {
            return FilterHolder.this.getInitParameterNames();
        }
    }
    
}






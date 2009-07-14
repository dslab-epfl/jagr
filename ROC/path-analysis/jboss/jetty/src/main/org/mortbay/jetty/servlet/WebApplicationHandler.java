// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: WebApplicationHandler.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ---------------------------------------------------------------------------

package org.mortbay.jetty.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.InclusiveByteRange;
import org.mortbay.http.MultiPartResponse;
import org.mortbay.http.PathMap;
import org.mortbay.util.ByteArrayISO8859Writer;
import org.mortbay.util.CachedResource;
import org.mortbay.util.Code;
import org.mortbay.util.IO;
import org.mortbay.util.LazyList;
import org.mortbay.util.Log;
import org.mortbay.util.MultiException;
import org.mortbay.util.MultiException;
import org.mortbay.util.MultiMap;
import org.mortbay.util.Resource;
import org.mortbay.util.StringUtil;
import org.mortbay.util.URI;

/* --------------------------------------------------------------------- */
/** WebApp HttpHandler.
 * This handler extends the ServletHandler with security, filter and resource
 * capabilities to provide full J2EE web container support.
 * <p>
 * @since Jetty 4.1
 * @see org.mortbay.jetty.servlet.WebApplicationContext
 * @version $Id: WebApplicationHandler.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Greg Wilkins
 */
public class WebApplicationHandler extends ServletHandler 
{
    private Map _filterMap=new HashMap();
    private List _pathFilters=new ArrayList();
    private MultiMap _servletFilterMap=new MultiMap();
    private boolean _acceptRanges=true;
    
    private transient boolean _started=false;
    
    /* ------------------------------------------------------------ */
    public boolean isAcceptRanges()
    {
        return _acceptRanges;
    }
    
    /* ------------------------------------------------------------ */
    /** Set if the handler accepts range requests.
     * Default is false;
     * @param ar True if the handler should accept ranges
     */
    public void setAcceptRanges(boolean ar)
    {
        _acceptRanges=ar;
    }
    
    /* ------------------------------------------------------------ */
    public FilterHolder defineFilter(String name, String className)
    {
        FilterHolder holder = new FilterHolder(this,name,className);
        _filterMap.put(holder.getName(),holder);
        return holder;
    }
    
    /* ------------------------------------------------------------ */
    public FilterHolder getFilter(String name)
    {
        return (FilterHolder)_filterMap.get(name);
    }

    /* ------------------------------------------------------------ */
    public FilterHolder mapServletToFilter(String servletName,
                                           String filterName)
    {
        FilterHolder holder =(FilterHolder)_filterMap.get(filterName);
        if (holder==null)
            throw new IllegalArgumentException("Unknown filter :"+filterName);
        Code.debug("Filter servlet ",servletName," --> ",filterName);
        _servletFilterMap.add(servletName,holder);
        return holder;
    }
    
    /* ------------------------------------------------------------ */
    public FilterHolder mapPathToFilter(String pathSpec,
                                        String filterName)
    {
        FilterHolder holder =(FilterHolder)_filterMap.get(filterName);
        if (holder==null)
            throw new IllegalArgumentException("Unknown filter :"+filterName);
        
        Code.debug("Filter path ",pathSpec," --> ",filterName);

        if (!holder.isMappedToPath())
            _pathFilters.add(holder);
        holder.addPathSpec(pathSpec);
        
        return holder;
    }

    /* ------------------------------------------------------------ */
    public boolean isStarted()
    {
        return _started&&super.isStarted();
    }
    
    /* ----------------------------------------------------------------- */
    public synchronized void start()
        throws Exception
    {
        // Start filters
        MultiException mex = new MultiException();
        
        try {super.start();}
        catch (Exception e){mex.add(e);}
        
        Iterator iter = _filterMap.values().iterator();
        while (iter.hasNext())
        {
            FilterHolder holder = (FilterHolder)iter.next();
            try{holder.start();}
            catch(Exception e) {mex.add(e);}
        }
        
        Code.debug("Path Filters: "+_pathFilters);
        Code.debug("Servlet Filters: "+_servletFilterMap);

        _started=true;
        
        mex.ifExceptionThrow();
    }
    
    /* ------------------------------------------------------------ */
    public synchronized void stop()
        throws  InterruptedException
    {
        // Stop filters
        try
        {
            Iterator iter = _filterMap.values().iterator();
            while (iter.hasNext())
            {
                FilterHolder holder = (FilterHolder)iter.next();
                holder.stop();
            }
            super.stop();
        }
        finally
        {
            _started=false;
        }
    }

    /* ------------------------------------------------------------ */
    void dispatch(String pathInContext,
                  HttpServletRequest request,
                  HttpServletResponse response,
                  ServletHolder servletHolder)
        throws ServletException,
               UnavailableException,
               IOException
    {
        // Determine request type.
        int requestType=0;

        if (request instanceof Dispatcher.DispatcherRequest)
        {
            // Forward or include
            requestType=((Dispatcher.DispatcherRequest)request).getFilterType();
        }
        else
        {
            // Error or request
            ServletHttpRequest servletHttpRequest=(ServletHttpRequest)request;
            ServletHttpResponse servletHttpResponse=(ServletHttpResponse)response;
            HttpRequest httpRequest=servletHttpRequest.getHttpRequest();
            HttpResponse httpResponse=servletHttpResponse.getHttpResponse();

            if (httpResponse.getStatus()!=HttpResponse.__200_OK)
            {
                // Error
                requestType=FilterHolder.__ERROR;
            }
            else
            {
                // Request
                requestType=FilterHolder.__REQUEST;
                // protect web-inf and meta-inf
                if (StringUtil.startsWithIgnoreCase(pathInContext,"/web-inf")  ||
                    StringUtil.startsWithIgnoreCase(pathInContext,"/meta-inf"))
                {
                    response.sendError(HttpResponse.__403_Forbidden);
                    return;
                }
                
                // Security Check
                if (!getHttpContext().checkSecurityContstraints
                    (pathInContext,
                     servletHttpRequest.getHttpRequest(),
                     servletHttpResponse.getHttpResponse()))
                    return;
            }
        }
        
        // Build list of filters
        LazyList filters = null;
        
        // Path filters
        if (pathInContext!=null && _pathFilters.size()>0)
        {
            for (int i=0;i<_pathFilters.size();i++)
            {
                FilterHolder holder=(FilterHolder)_pathFilters.get(i);
                if (holder.appliesTo(pathInContext,requestType))
                    filters=LazyList.add(filters,holder);
            }
        }
        
        // Servlet filters
        if (servletHolder!=null && _servletFilterMap.size()>0)
        {
            Object o=_servletFilterMap.get(servletHolder.getName());
            if (o!=null)
            {
                if (o instanceof List)
                {
                    List list=(List)o;
                    for (int i=0;i<list.size();i++)
                    {
                        FilterHolder holder = (FilterHolder)list.get(i);
                        if (holder.appliesTo(requestType))
                            filters=LazyList.add(filters,holder);
                    }
                }
                else
                {
                    FilterHolder holder = (FilterHolder)o;
                    if (holder.appliesTo(requestType))
                        filters=LazyList.add(filters,holder);
                } 
            }
        }
        
        // Do the handling thang
        if (LazyList.size(filters)>0)
        {
            Chain chain=new Chain(pathInContext,filters,servletHolder);
            chain.doFilter(request,response);
        }
        else
        {
            // Call servlet
            if (servletHolder!=null)
            {
                if (Code.verbose()) Code.debug("call servlet ",servletHolder);
                servletHolder.handle(request,response);
            }
            else // Not found
                notFound(request,response);
        }
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class Chain implements FilterChain
    {
        String _pathInContext;
        int _filter=0;
        LazyList _filters;
        ServletHolder _servletHolder;

        /* ------------------------------------------------------------ */
        Chain(String pathInContext,
              LazyList filters,
              ServletHolder servletHolder)
        {
            _pathInContext=pathInContext;
            _filters=filters;
            _servletHolder=servletHolder;
        }
        
        /* ------------------------------------------------------------ */
        public void doFilter(ServletRequest request, ServletResponse response)
            throws IOException,
                   ServletException
        {
            if (Code.verbose()) Code.debug("doFilter ",_filter);
            
            // pass to next filter
            if (_filter<LazyList.size(_filters))
            {
                FilterHolder holder = (FilterHolder)LazyList.get(_filters,_filter++);
                if (Code.verbose()) Code.debug("call filter ",holder);
                Filter filter = holder.getFilter();
                filter.doFilter(request,response,this);
                return;
            }

            // Call servlet
            if (_servletHolder!=null)
            {
                if (Code.verbose()) Code.debug("call servlet ",_servletHolder);
                _servletHolder.handle(request,response);
            }
            else // Not found
                notFound((HttpServletRequest)request,
                         (HttpServletResponse)response);
        }
    }
}


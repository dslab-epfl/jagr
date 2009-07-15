// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ProxyHandler.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.http.handler;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashSet;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpMessage;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.HttpTunnel;
import org.mortbay.util.Code;
import org.mortbay.util.IO;
import org.mortbay.util.InetAddrPort;
import org.mortbay.util.StringMap;
import org.mortbay.util.URI;

/* ------------------------------------------------------------ */
/** Proxy request handler.
 * A HTTP/1.1 Proxy.  This implementation uses the JVMs URL implementation to
 * make proxy requests.
 * <P>The HttpTunnel mechanism is also used to implement the CONNECT method.
 *
 * 
 * @version $Id: ProxyHandler.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
 * @author Greg Wilkins (gregw)
 */
public class ProxyHandler extends AbstractHttpHandler
{
    /* ------------------------------------------------------------ */
    /** Map of leg by leg headers (not end to end).
     * Should be a set, but more efficient string map is used instead.
     */
    protected StringMap _DontProxyHeaders = new StringMap();
    {
        Object o = new Object();
        _DontProxyHeaders.setIgnoreCase(true);
        _DontProxyHeaders.put("Proxy-Connection",o);
        _DontProxyHeaders.put(HttpFields.__Connection,o);
        _DontProxyHeaders.put(HttpFields.__KeepAlive,o);
        _DontProxyHeaders.put(HttpFields.__TransferEncoding,o);
        _DontProxyHeaders.put(HttpFields.__TE,o);
        _DontProxyHeaders.put(HttpFields.__Trailer,o);
        _DontProxyHeaders.put(HttpFields.__ProxyAuthorization,o);
        _DontProxyHeaders.put(HttpFields.__ProxyAuthenticate,o);
        _DontProxyHeaders.put(HttpFields.__Upgrade,o);
    }
    
    /* ------------------------------------------------------------ */
    /**  Map of allows schemes to proxy
     * Should be a set, but more efficient string map is used instead.
     */
    protected StringMap _ProxySchemes = new StringMap();
    {
        Object o = new Object();
        _ProxySchemes.setIgnoreCase(true);
        _ProxySchemes.put(HttpMessage.__SCHEME,o);
        _ProxySchemes.put(HttpMessage.__SSL_SCHEME,o);
        _ProxySchemes.put("ftp",o);
    }
    
    /* ------------------------------------------------------------ */
    /** Set of allowed CONNECT ports.
     */
    protected HashSet _allowedConnectPorts = new HashSet();
    {
        _allowedConnectPorts.add(new Integer(80));
        _allowedConnectPorts.add(new Integer(8000));
        _allowedConnectPorts.add(new Integer(8080));
        _allowedConnectPorts.add(new Integer(8888));
        _allowedConnectPorts.add(new Integer(443));
        _allowedConnectPorts.add(new Integer(8443));
    }
    
    /* ------------------------------------------------------------ */
    public void handle(String pathInContext,
                       String pathParams,
                       HttpRequest request,
                       HttpResponse response)
        throws HttpException, IOException
    {
        URI uri = request.getURI();
        
        // Is this a CONNECT request?
        if (HttpRequest.__CONNECT.equalsIgnoreCase(request.getMethod()))
        {
            handleConnect(pathInContext,pathParams,request,response);
            return;
        }
        
        try
        {
            // Do we proxy this?
            URL url=isProxied(uri);
            if (url==null)
                return;
            Code.debug("PROXY URL=",url);

            URLConnection connection = url.openConnection();

            // Set method
            HttpURLConnection http = null;
            if (connection instanceof HttpURLConnection)
            {
                http = (HttpURLConnection)connection;
                http.setRequestMethod(request.getMethod());
                http.setInstanceFollowRedirects(false);
            }

            // check connection header
            connection.setRequestProperty("Via","1.1 (jetty)");
            String connectionHdr = request.getField(HttpFields.__Connection);
            if (connectionHdr!=null &&
                (connectionHdr.equalsIgnoreCase(HttpFields.__KeepAlive)||
                 connectionHdr.equalsIgnoreCase(HttpFields.__Close)))
                connectionHdr=null;

            // copy headers
            Enumeration enum = request.getFieldNames();
            while (enum.hasMoreElements())
            {
                // XXX could be better than this!
                String hdr=(String)enum.nextElement();
                if (_DontProxyHeaders.containsKey(hdr))
                    continue;
                if (connectionHdr!=null && connectionHdr.indexOf(hdr)>=0)
                    continue;
                
                String val=request.getField(hdr);
                connection.setRequestProperty(hdr,val);
            }           

            // a little bit of cache control
            String cache_control = request.getField(HttpFields.__CacheControl);
            if (cache_control!=null &&
                (cache_control.indexOf("no-cache")>=0 ||
                 cache_control.indexOf("no-store")>=0))
                connection.setUseCaches(false);

            // customize Connection
            customizeConnection(pathInContext,pathParams,request,connection);
            
            try
            {    
                connection.setDoInput(true);
                
                // do input thang!
                InputStream in=request.getInputStream();
                if (in.available()>0) // XXX need better tests than this
                {
                    connection.setDoOutput(true);
                    IO.copy(in,connection.getOutputStream());
                }
                
                // Connect
                connection.connect();    
            }
            catch (Exception e)
            {
                Code.ignore(e);
            }
            
            InputStream proxy_in = null;

            // handler status codes etc.
            if (http!=null)
            {
                proxy_in = http.getErrorStream();
                int code=HttpResponse.__500_Internal_Server_Error;
                
                code=http.getResponseCode();
                response.setStatus(code);
                response.setReason(http.getResponseMessage());
            }
            
            if (proxy_in==null)
            {
                try {proxy_in=connection.getInputStream();}
                catch (Exception e)
                {
                    Code.ignore(e);
                    proxy_in = http.getErrorStream();
                }
            }
            
            // set response headers
            int h=0;
            String hdr=connection.getHeaderFieldKey(h);
            String val=connection.getHeaderField(h);
            
            while(hdr!=null || val!=null)
            {
                if (hdr!=null && val!=null && !_DontProxyHeaders.containsKey(hdr))
                    response.setField(hdr,val);
                h++;
                hdr=connection.getHeaderFieldKey(h);
                val=connection.getHeaderField(h);
            }
            response.setField("Via","1.1 (jetty)");
            
            request.setHandled(true);
            if (proxy_in!=null)
                IO.copy(proxy_in,response.getOutputStream());
            
        }
        catch (Exception e)
        {
            Code.warning(e.toString());
            Code.ignore(e);
            if (!response.isCommitted())
                response.sendError(HttpResponse.__400_Bad_Request);
        }
    }
    
    /* ------------------------------------------------------------ */
    public void handleConnect(String pathInContext,
                              String pathParams,
                              HttpRequest request,
                              HttpResponse response)
        throws HttpException, IOException
    {
        URI uri = request.getURI();
        
        try
        {
            Code.debug("CONNECT: ",uri);
            InetAddrPort addrPort=new InetAddrPort(uri.toString());
            
            Integer port = new Integer(addrPort.getPort());
            if (!_allowedConnectPorts.contains(port))
                response.setStatus(HttpResponse.__403_Forbidden);
            else
            {
                Socket socket = new Socket(addrPort.getInetAddress(),addrPort.getPort());
                request.getHttpConnection().setHttpTunnel(new HttpTunnel(socket));
                response.setStatus(HttpResponse.__200_OK);
            }
        }
        catch (Exception e)
        {
            Code.ignore(e);
            response.setStatus(HttpResponse.__405_Method_Not_Allowed);
        }
        finally
        {
            response.setContentLength(0);
            request.setHandled(true);
        }    
    }
    
        
    /* ------------------------------------------------------------ */
    /** Customize proxy connection.
     * Method to allow derived handlers to customize the connection.
     */
    protected void customizeConnection(String pathInContext,
                                       String pathParams,
                                       HttpRequest request,
                                       URLConnection connection)
        throws IOException
    {
    }
    
    
    /* ------------------------------------------------------------ */
    /** Is URL Proxied.
     * Method to allow derived handlers to select which URIs are proxied and
     * to where.
     * @return The URL to proxy to, or null if the passed URI should not be proxied.
     * The default implementation returns the passed uri if it has a schema
     * that is in the _ProxySchemes map.
     */
    protected URL isProxied(URI uri)
        throws MalformedURLException
    {
        // Is this a proxy request?
        String scheme=uri.getScheme();
        return  (scheme!=null && _ProxySchemes.containsKey(scheme))?new URL(uri.toString()):null;
    }    
}

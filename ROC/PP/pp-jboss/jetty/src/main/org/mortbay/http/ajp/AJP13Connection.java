// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: AJP13Connection.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.http.ajp;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import org.mortbay.http.HttpConnection;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpMessage;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.Version;
import org.mortbay.util.Code;
import org.mortbay.util.LineInput;


/* ------------------------------------------------------------ */
/** 
 * @version $Id: AJP13Connection.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
 * @author Greg Wilkins (gregw)
 */
public class AJP13Connection extends HttpConnection
{
    private AJP13InputStream _ajpIn;
    private AJP13OutputStream _ajpOut;
    private String _remoteHost;
    private String _remoteAddr;
    private String _serverName;
    private int _serverPort;
    private boolean _isSSL;
    
    /* ------------------------------------------------------------ */
    public AJP13Connection(AJP13Listener listener,
                           InputStream in,
                           OutputStream out,
                           Socket socket,
                           int bufferSize)
        throws IOException
    {
        super(listener,
              null,
              new AJP13InputStream(in,out,bufferSize),
              out,
              socket);

        LineInput lin = (LineInput)getInputStream().getInputStream();
        _ajpIn=(AJP13InputStream)lin.getInputStream();
        _ajpOut=new AJP13OutputStream(getOutputStream().getFilterStream(),
                                      bufferSize);
        _ajpOut.setCommitObserver(this);
        getOutputStream().setBufferedOutputStream(_ajpOut,true);
    }

    /* ------------------------------------------------------------ */
    /** Get the Remote address.
     * @return the remote address
     */
    public InetAddress getRemoteInetAddress()
    {
        return null;
    }

    /* ------------------------------------------------------------ */
    public void destroy()
    {
        if (_ajpIn!=null)_ajpIn.destroy();
        _ajpIn=null;
        if (_ajpOut!=null)_ajpOut.destroy();
        _ajpOut=null;
        _remoteHost=null;
        _remoteAddr=null;
        _serverName=null;
        
    }
    
    /* ------------------------------------------------------------ */
    /** Get the Remote address.
     * @return the remote host name
     */
    public String getRemoteAddr()
    {
        return _remoteAddr;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the Remote address.
     * @return the remote host name
     */
    public String getRemoteHost()
    {
        return _remoteHost;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the listeners HttpServer .
     * Conveniance method equivalent to getListener().getHost().
     * @return HttpServer.
     */
    public String getServerName()
    {
        return _serverName;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the listeners Port .
     * Conveniance method equivalent to getListener().getPort().
     * @return HttpServer.
     */
    public int getServerPort()
    {
        return _serverPort;
    }

    /* ------------------------------------------------------------ */
    /** Get the listeners Default scheme. 
     * Conveniance method equivalent to getListener().getDefaultProtocol().
     * @return HttpServer.
     */
    public String getDefaultScheme()
    {
        return _isSSL?HttpMessage.__SSL_SCHEME:super.getDefaultScheme();
    }
    
    /* ------------------------------------------------------------ */
    public boolean isSSL()
    {
        return _isSSL;
    }
    
    /* ------------------------------------------------------------ */
    public boolean handleNext()
    {
        Socket socket=(Socket)getConnection();
        AJP13Packet packet=null;
        HttpRequest request = getRequest();
        HttpResponse response = getResponse();
        HttpContext context = null;
        boolean gotRequest=false;
        boolean persistent=false;
        
        try
        {
            try
            {
                packet = null;
                packet = _ajpIn.nextPacket();
                if (packet==null)
                    return false;
            }
            catch (IOException e)
            {
                Code.ignore(e);
                return false;
            }
            
            int type=packet.getByte();
            if (Code.debug())
                Code.debug("AJP13 type="+type+" size="+packet.unconsumedData());
            
            switch (type)
            {
              case AJP13Packet.__FORWARD_REQUEST:
                  request.setTimeStamp(System.currentTimeMillis());
                  statsRequestStart();
                  
                  request.setState(HttpMessage.__MSG_EDITABLE);
                  request.setMethod(packet.getMethod());
                  request.setVersion(packet.getString());
                  request.setPath(packet.getString());
                  _remoteAddr=packet.getString();
                  _remoteHost=packet.getString();
                  _serverName=packet.getString();
                  _serverPort=packet.getInt();
                  _isSSL=packet.getBoolean();
                  
                  // Headers
                  int h=packet.getInt();
                  for (int i=0;i<h;i++)
                      request.setField(packet.getHeader(),packet.getString());
                  
                  // Handler other attributes
                  byte attr=packet.getByte();
                  while ((0xFF&attr)!=0xFF)
                  {
                      String value=packet.getString();
                      switch (attr)
                      {
                        case 10: // request attribute
                            request.setAttribute(value,packet.getString());
                            break;
                        case 9: // SSL session
                            Code.warning("not implemented: sslsession="+value);
                            break;
                        case 8: // SSL cipher
                            request.setAttribute("javax.servlet.request.cipher_suite",value);
                            break;
                        case 7: // SSL cert
                            request.setAttribute("javax.servlet.request.X509Certificate",value);
                            break;
                        case 6: // JVM Route
                            request.setAttribute("org.mortbay.http.ajp.JVMRoute",value);
                            break;
                        case 5: // Query String
                            request.setQuery(value);
                            break;
                        case 4: // AuthType
                            request.setAuthType(value);
                            break;
                        case 3: // Remote User
                            request.setAuthUser(value);
                            break;
                            
                        case 2:  // servlet path not implemented
                        case 1:  // context not implemented
                        default:
                            Code.warning("Unknown attr: "+attr+"="+value);  
                      }
                      
                      attr=packet.getByte();
                  }
                  gotRequest=true;
                  request.setState(HttpMessage.__MSG_RECEIVED);
                  
                  // Complete response
                  if (request.getContentLength()==0 &&
                      request.getField(HttpFields.__TransferEncoding)==null)
                      _ajpIn.close();
                  
                  // Prepare response
                  response.setState(HttpMessage.__MSG_EDITABLE);
                  response.setVersion(HttpMessage.__HTTP_1_1);
                  response.setDateField(HttpFields.__Date,_request.getTimeStamp());
                  // response.setField(HttpFields.__Server,Version.__VersionDetail);
                  response.setField(HttpFields.__ServletEngine,Version.__ServletEngine);
                  
                  // Service request
                  Code.debug("REQUEST: ",request);
                  context=service(request,response);
                  Code.debug("RESPONSE: ",response);

                  break;
                  
              default:
                  Code.debug("Ignored: "+packet);
                  persistent=false;
            }

            persistent=true;   
        }
        catch (SocketException e)
        {
            Code.ignore(e);
        }
        catch (Exception e)
        {
            Code.warning(e);
            try{
                if (gotRequest)
                    _ajpOut.close();
            }
            catch (IOException e2){Code.ignore(e2);}
        }
        finally
        {
            // abort if nothing received.
            if (packet==null || !gotRequest)
                return false;
            
            // flush and end the output
            try{
                //Consume unread input.
                // while(_ajpIn.skip(4096)>0 || _ajpIn.read()>=0);

                // end response
                getOutputStream().close();
                if (!persistent)
                    _ajpOut.end();

                // Close the outout
                _ajpOut.close();

                // reset streams
                getOutputStream().resetStream();
                getOutputStream().addObserver(this);
                getInputStream().resetStream();
                _ajpIn.resetStream();
                _ajpOut.resetStream();
            }
            catch (Exception e)
            {
                Code.debug(e);
                persistent=false;
            }
            finally
            {
                statsRequestEnd();
                if (context!=null)
                    context.log(request,response,-1);
            }
        }
        return persistent;
    }



    /* ------------------------------------------------------------ */
    protected void setupOutputStream()
        throws IOException
    {
        // Nobble the OutputStream for HEAD requests
        if (HttpRequest.__HEAD.equals(getRequest().getMethod()))
            getOutputStream().nullOutput();
    }
}

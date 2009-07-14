// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: AJP13Connection.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.http.ajp;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Enumeration;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpFields;
import org.mortbay.http.Version;
import org.mortbay.http.HttpConnection;
import org.mortbay.http.HttpListener;
import org.mortbay.http.HttpMessage;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.HttpServer;
import org.mortbay.util.Code;
import org.mortbay.util.InetAddrPort;
import org.mortbay.util.IO;
import org.mortbay.util.Log;
import org.mortbay.util.ThreadPool;
import org.mortbay.util.ThreadedServer;


/* ------------------------------------------------------------ */
/** 
 * @version $Id: AJP13Connection.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
 * @author Greg Wilkins (gregw)
 */
public class AJP13Connection extends HttpConnection
{
    private AJP13InputStream _ajpIn;
    private AJP13OutputStream _ajpOut;
    private AJP13Packet _ajpResponse;
    private String _remoteHost;
    private String _remoteAddr;
    private String _serverName;
    private int _serverPort;
    private boolean _isSSL;
    
    /* ------------------------------------------------------------ */
    public AJP13Connection(AJP13Listener listener,
                           AJP13InputStream in,
                           AJP13OutputStream out,
                           Socket socket)
        throws IOException
    {
        super(listener,null,in,out,socket);
        _ajpIn=in;
        _ajpOut=out;
        _ajpResponse=new AJP13Packet(listener.getBufferSize());
        _ajpResponse.addByte((byte)'A');
        _ajpResponse.addByte((byte)'B');
        _ajpResponse.addInt(0);
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
        boolean persistent=false;
        
        try
        {
            try
            {
                packet = null;
                packet = _ajpIn.nextPacket();
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
                  
                  request.setState(HttpMessage.__MSG_RECEIVED);
                  
                  // Complete response
                  if (request.getContentLength()==0 &&
                      request.getField(HttpFields.__TransferEncoding)==null)
                      _ajpIn.close();
                  
                  // Prepare response
                  response.setState(HttpMessage.__MSG_EDITABLE);
                  response.setVersion(HttpMessage.__HTTP_1_1);
                  response.setDateField(HttpFields.__Date,_request.getTimeStamp());
                  response.setField(HttpFields.__Server,Version.__VersionDetail);
                  response.setField(HttpFields.__ServletEngine,Version.__ServletEngine);
                  
                  // Service request
                  Code.debug("REQUEST: ",request);
                  context=service(request,response);
                  Code.debug("RESPONSE: ",response);

                  break;
                  
              default:
                  Code.warning("Not implemented: "+packet);
                  persistent=false;
            }

            persistent=true;   
        }
        catch (Exception e)
        {
            Code.warning(e);
            try{_ajpOut.end(false);}catch (IOException e2){Code.ignore(e2);}
        }
        finally
        {
            // abort if nothing received.
            if (packet==null)
                return false;
            
            // flush and end the output
            try{
                //Consume unread input.
                // while(_ajpIn.skip(4096)>0 || _ajpIn.read()>=0);

                // end response
                getOutputStream().flush(true);
                response.commit();
                _ajpOut.end(persistent);

                // Close the outout
                _ajpOut.close();

                // reset streams
                getOutputStream().resetStream();
                getOutputStream().addObserver(this);
                getInputStream().resetStream();
                _ajpIn.recycle();
                _ajpOut.recycle();
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
    
    
    /* ------------------------------------------------------------ */
    protected void commitResponse()
        throws IOException
    {
        HttpResponse response = getResponse();
        
        _ajpResponse.resetData();
        _ajpResponse.addByte(AJP13Packet.__SEND_HEADERS);
        _ajpResponse.addInt(response.getStatus());
        _ajpResponse.addString(response.getReason());

        int mark=_ajpResponse.getMark();
        _ajpResponse.addInt(0);

        int nh=0;
        Enumeration e1=_response.getFieldNames();
        while(e1.hasMoreElements())
        {
            String h=(String)e1.nextElement();
            Enumeration e2=_response.getFieldValues(h);
            while(e2.hasMoreElements())
            {
                _ajpResponse.addHeader(h);
                _ajpResponse.addString((String)e2.nextElement());
                nh++;
            }
        }

        if (nh>0)
            _ajpResponse.setInt(mark,nh);
        _ajpResponse.setDataSize();

        _ajpOut.write(_ajpResponse);
        response.setState(HttpMessage.__MSG_SENDING);
    }
}

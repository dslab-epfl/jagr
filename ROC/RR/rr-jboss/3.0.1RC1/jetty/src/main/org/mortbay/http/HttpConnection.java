// ========================================================================
// Copyright (c) 1999,2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: HttpConnection.java,v 1.4 2003/04/07 00:57:41 emrek Exp $
// ========================================================================

package org.mortbay.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import org.mortbay.util.Code;
import org.mortbay.util.LifeCycle;
import org.mortbay.util.StringUtil;
import org.mortbay.util.ThreadPool;
import org.mortbay.util.ThreadPool.PoolThread;
import org.mortbay.util.LineInput;

import roc.config.ROCConfig;


/* ------------------------------------------------------------ */
/** A HTTP Connection.
 * This class provides the generic HTTP handling for
 * a connection to a HTTP server. An instance of HttpConnection
 * is normally created by a HttpListener and then given control
 * in order to run the protocol handling before and after passing
 * a request to the HttpServer of the HttpListener.
 *
 * This class is not synchronized as it should only ever be known
 * to a single thread.
 *
 * @see HttpListener
 * @see HttpServer
 * @version $Id: HttpConnection.java,v 1.4 2003/04/07 00:57:41 emrek Exp $
 * @author Greg Wilkins (gregw)
 */
public class HttpConnection
    implements OutputObserver
{
    /* ------------------------------------------------------------ */
    private static ThreadLocal __threadConnection=new ThreadLocal();
    
    /* ------------------------------------------------------------ */
    protected HttpRequest _request;
    protected HttpResponse _response;

    private HttpListener _listener;
    private ChunkableInputStream _inputStream;
    private ChunkableOutputStream _outputStream;
    private boolean _persistent;
    private boolean _close;
    private boolean _keepAlive;
    private String _version;
    private int _dotVersion;
    private boolean _outputSetup;
    private Thread _handlingThread;
    
    private InetAddress _remoteInetAddress;
    private String _remoteAddr;
    private String _remoteHost;
    private HttpServer _httpServer;
    private Object _connection;
    private HashMap _codingParams;
    private ArrayList _codings;

    private boolean _statsOn;
    private long _tmpTime;
    private long _openTime;
    private long _reqTime;
    private int _requests;
    private Object _object;
    
    /* ------------------------------------------------------------ */
    /** Constructor.
     * @param listener The listener that created this connection.
     * @param remoteAddr The address of the remote end or null.
     * @param in InputStream to read request(s) from.
     * @param out OutputputStream to write response(s) to.
     * @param connection The underlying connection object, most likely
     * a socket. This is not used by HttpConnection other than to make
     * it available via getConnection().
     */
    public HttpConnection(HttpListener listener,
                          InetAddress remoteAddr,
                          InputStream in,
                          OutputStream out,
                          Object connection)
    {
        Code.debug("new HttpConnection: ",connection);
        _listener=listener;
        _remoteInetAddress=remoteAddr;
        int bufferSize=listener==null?4096:listener.getBufferSize();
        int reserveSize=listener==null?512:listener.getBufferReserve();
        _inputStream=new ChunkableInputStream(in,bufferSize);
        _outputStream=new ChunkableOutputStream(out,bufferSize,reserveSize);
        _outputStream.addObserver(this);
        _outputSetup=false;
        if (_listener!=null)
            _httpServer=_listener.getHttpServer();
        _connection=connection;
        
        _statsOn=_httpServer!=null && _httpServer.getStatsOn();
        if (_statsOn)
        {
            _openTime=System.currentTimeMillis();
            _httpServer.statsOpenConnection();
        }
        _reqTime=0;
        _requests=0;
        
        _request = new HttpRequest(this);
        _response = new HttpResponse(this);
    }

    /* ------------------------------------------------------------ */
    /** Get the ThreadLocal HttpConnection.
     * The ThreadLocal HttpConnection is set by the handle() method.
     * @return HttpConnection for this thread.
     */
    static HttpConnection getHttpConnection()
    {
        return (HttpConnection)__threadConnection.get();
    }
    
    /* ------------------------------------------------------------ */
    /** Get the Remote address.
     * @return the remote address
     */
    public InetAddress getRemoteInetAddress()
    {
        return _remoteInetAddress;
    }

    /* ------------------------------------------------------------ */
    /** Get the Remote address.
     * @return the remote host name
     */
    public String getRemoteAddr()
    {
        if (_remoteAddr==null)
        {
            if (_remoteInetAddress==null)
                return "127.0.0.1";
            _remoteAddr=_remoteInetAddress.getHostAddress();
        }
        return _remoteAddr;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the Remote address.
     * @return the remote host name
     */
    public String getRemoteHost()
    {
        if (_remoteHost==null)
        {
            if (_remoteInetAddress==null)
                return "localhost";
            _remoteHost=_remoteInetAddress.getHostName();
        }
        return _remoteHost;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the connections InputStream.
     * @return the connections InputStream
     */
    public ChunkableInputStream getInputStream()
    {
        return _inputStream;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the connections OutputStream.
     * @return the connections OutputStream
     */
    public ChunkableOutputStream getOutputStream()
    {
        return _outputStream;
    }

    /* ------------------------------------------------------------ */
    /** Get the underlying connection object.
     * This opaque object, most likely a socket. This is not used by
     * HttpConnection other than to make it available via getConnection().
     * @return Connection abject
     */
    public Object getConnection()
    {
        return _connection;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the request.
     * @return the request
     */
    public HttpRequest getRequest()
    {
        return _request;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the response.
     * @return the response
     */
    public HttpResponse getResponse()
    {
        return _response;
    }

    /* ------------------------------------------------------------ */
    /** Force the connection to not be persistent.
     */
    public void forceClose()
    {
        _persistent=false;
        _close=true;
    }
    
    /* ------------------------------------------------------------ */
    /** Close the connection.
     * This method calls close on the input and output streams and
     * interrupts any thread in the handle method.
     * may be specialized to close sockets etc.
     * @exception IOException 
     */
    public void close()
        throws IOException
    {
        try{
            _outputStream.close();
            _inputStream.close();
        }
        finally
        {
            if (_handlingThread!=null && Thread.currentThread()!=_handlingThread)
                _handlingThread.interrupt();
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Get the connections listener. 
     * @return HttpListener that created this Connection.
     */
    public HttpListener getListener()
    {
        return _listener;
    }

    /* ------------------------------------------------------------ */
    /** Get the listeners HttpServer .
     * Conveniance method equivalent to getListener().getHttpServer().
     * @return HttpServer.
     */
    public HttpServer getHttpServer()
    {
        return _httpServer;
    }

    /* ------------------------------------------------------------ */
    /** Get the listeners Default scheme. 
     * Conveniance method equivalent to getListener().getDefaultProtocol().
     * @return HttpServer.
     */
    public String getDefaultScheme()
    {
        return _listener.getDefaultScheme();
    }
    
    /* ------------------------------------------------------------ */
    /** Get the listeners HttpServer .
     * Conveniance method equivalent to getListener().getHost().
     * @return HttpServer.
     */
    public String getServerName()
    {
        return _listener.getHost();
    }
    
    /* ------------------------------------------------------------ */
    /** Get the listeners Port .
     * Conveniance method equivalent to getListener().getPort().
     * @return HttpServer.
     */
    public int getServerPort()
    {
        return _listener.getPort();
    }

    /* ------------------------------------------------------------ */
    /** Get associated object.
     * Used by a particular HttpListener implementation to associate
     * private datastructures with the connection.
     * @return An object associated with the connecton by setObject.
     */
    public Object getObject()
    {
        return _object;
    }
    
    /* ------------------------------------------------------------ */
    /** Set associated object.
     * Used by a particular HttpListener implementation to associate
     * private datastructures with the connection.
     * @param o An object associated with the connecton.
     */
    public void setObject(Object o)
    {
        _object=o;
    }

    /* ------------------------------------------------------------ */
    /* Verify HTTP/1.0 request
     * @exception HttpException problem with the request. 
     * @exception IOException problem with the connection.
     */
    private void verifyHTTP_1_0()
        throws HttpException, IOException
    {     
        // Set content length
        int content_length=
            _request.getIntField(HttpFields.__ContentLength);
        if (content_length>=0)
            _inputStream.setContentLength(content_length);
        else if (content_length<0)
        {
            // XXX - can't do this check because IE does this after
            // a redirect.
            // Can't have content without a content length
            // String content_type=_request.getField(HttpFields.__ContentType);
            // if (content_type!=null && content_type.length()>0)
            //     throw new HttpException(_response.__411_Length_Required);
            _inputStream.setContentLength(0);
        }

        // dont support persistent connections in HTTP/1.0
        _persistent=_keepAlive;
    }
    
    /* ------------------------------------------------------------ */
    /* Verify HTTP/1.1 request
     * @exception HttpException problem with the request. 
     * @exception IOException problem with the connection.
     */
    private void verifyHTTP_1_1()
        throws HttpException, IOException
    {        
        // Check Host Field exists
        String host=_request.getField(HttpFields.__Host);
        if (host==null || host.length()==0)
            throw new HttpException(_response.__400_Bad_Request);
        
        // check and enable requests transfer encodings.
        Enumeration transfer_coding=_request.getFieldValues(HttpFields.__TransferEncoding,
                                                            HttpFields.__separators);
        boolean input_encodings=false;
        if (transfer_coding!=null)
        {
            if (_codings==null)
                _codings=new ArrayList(2);
            else
            {
                _codings.clear();
                if (_codingParams!=null)
                    _codingParams.clear();
            }
            
            while(transfer_coding.hasMoreElements())
                _codings.add(transfer_coding.nextElement());

            for(int i=_codings.size();i-->0;)
            {        
                String value=_codings.get(i).toString();
                if (_codingParams==null && value.indexOf(';')>0)
                    _codingParams=new HashMap(7);
                String coding=HttpFields.valueParameters(value,_codingParams);
                
                if (HttpFields.__Identity.equalsIgnoreCase(coding))
                    continue;
                
                input_encodings=true;
                
                if (HttpFields.__Chunked.equalsIgnoreCase(coding))
                {
                    // chunking must be last and have no parameters
                    if (i+1<_codings.size() || _codingParams!=null&&_codingParams.size()>0)
                        throw new HttpException(HttpResponse.__400_Bad_Request);
                    _inputStream.setChunking();
                }
                else
                {
                    getHttpServer().getHttpEncoding()
                        .enableEncoding(_inputStream,coding,_codingParams);
                }
            }
        }
        
        // Check input content length can be determined
        int content_length=_request.getIntField(HttpFields.__ContentLength);
        String content_type=_request.getField(HttpFields.__ContentType);
        if (input_encodings)
        {
            // Must include chunked
            if (!_inputStream.isChunking())
                throw new HttpException(_response.__400_Bad_Request);
        }
        else
        {
            // If we have a content length, use it
            if (content_length>=0)
                _inputStream.setContentLength(content_length);
            // else if we have no content
            else if (content_type==null || content_type.length()==0)
                _inputStream.setContentLength(0);
            // else we need a content length
            else
            {
                // XXX - can't do this check as IE stuff up on
                // a redirect.
                // throw new HttpException(_response.__411_Length_Required);
                _inputStream.setContentLength(0);
            }
        }

        // Handle Continue Expectations
        String expect=_request.getField(HttpFields.__Expect);
        if (expect!=null && expect.length()>0)
        {
            if (StringUtil.asciiToLowerCase(expect)
                .equals(HttpFields.__ExpectContinue))
            {
                // Send continue if no body available yet.
                if (_inputStream.available()<=0)
                {
                    _outputStream.getRawStream()
                        .write(_response.__Continue);
                    _outputStream.getRawStream()
                        .flush();
                }
            }
            else
                throw new HttpException(_response.__417_Expectation_Failed);
        }
        else if (_inputStream.available()<=0 &&
                 (_request.__PUT.equals(_request.getMethod()) ||
                  _request.__POST.equals(_request.getMethod())))
        {
            // Send continue for RFC 2068 exception
            _outputStream.getRawStream()
                .write(_response.__Continue);
            _outputStream.getRawStream()
                .flush();
        }            
             
        // Persistent unless requested otherwise
        _persistent=!_close;

    }
    

    /* ------------------------------------------------------------ */
    /** Output Notifications.
     * Trigger header and/or filters from output stream observations.
     * Also finalizes method of indicating response content length.
     * Called as a result of the connection subscribing for notifications
     * to the ChunkableOutputStream.
     * @see ChunkableOutputStream
     * @param out The output stream observed.
     * @param action The action.
     */
    public void outputNotify(ChunkableOutputStream out, int action, Object ignoredData)
        throws IOException
    {
        if (_response==null)
            return;

        switch(action)
        {
          case OutputObserver.__FIRST_WRITE:
              if (!_outputSetup)
                  setupOutputStream();
              break;
              
          case OutputObserver.__RESET_BUFFER:
              _outputSetup=false;
              break;
              
          case OutputObserver.__COMMITING:
              if (_response.getState()==HttpMessage.__MSG_EDITABLE)
                  _response.commitHeader();
              break;
              
          case OutputObserver.__CLOSING:
              break;
              
          case OutputObserver.__CLOSED:
              break;
        }
    }

    /* ------------------------------------------------------------ */
    /** Setup the reponse output stream.
     * Use the current state of the request and response, to set tranfer
     * parameters such as chunking and content length.
     */
    protected void setupOutputStream()
        throws IOException
    {
        if (_outputSetup)
            return;
        _outputSetup=true;
        
        // Determine how to limit content length and
        // enable output transfer encodings 
        Enumeration transfer_coding=_response.getFieldValues(HttpFields.__TransferEncoding,
                                                             HttpFields.__separators);
        if (transfer_coding==null || !transfer_coding.hasMoreElements())
        {
            switch(_dotVersion)
            {
              case 1:
                  {
                      // if forced or (not closed and no length)
                      if (_listener.getHttpServer().isChunkingForced() ||
                          (!HttpFields.__Close.equals(_response.getField(HttpFields.__Connection)))&&
                          (_response.getField(HttpFields.__ContentLength)==null))
                      {
                          // Chunk it!
                          _response.removeField(HttpFields.__ContentLength);
                          _response.setField(HttpFields.__TransferEncoding,
                                         HttpFields.__Chunked);
                          _outputStream.setChunking();
                      }
                      break;
                  }
              case 0:
                  {
                      // If we dont have a content length (except 304 replies), 
		      // or we have been requested to close
		      // then we can't be persistent 
                      if (!_keepAlive || !_persistent ||
                          HttpResponse.__304_Not_Modified!=_response.getStatus() &&
                          _response.getField(HttpFields.__ContentLength)==null ||
                          HttpFields.__Close.equals(_response.getField(HttpFields.__Connection)))
                      {
                          _persistent=false;
                          if (_keepAlive)
                              _response.setField(HttpFields.__Connection,
                                                 HttpFields.__Close);
                          _keepAlive=false;
                      }
                      else if (_keepAlive)
                          _response.setField(HttpFields.__Connection,
                                             HttpFields.__KeepAlive);
                      break;
                  }
              default:
                  _keepAlive=false;
                  _persistent=false;
            }
        }
        else if (_dotVersion<1)
        {
            // Error for transfer encoding to be set in HTTP/1.0
            _response.removeField(HttpFields.__TransferEncoding);
            throw new HttpException(_response.__501_Not_Implemented,
                                    "Transfer-Encoding not supported in HTTP/1.0");
        }
        else
        {
            // Use transfer encodings to determine length
            _response.removeField(HttpFields.__ContentLength);
            
            if (_codings==null)
                _codings=new ArrayList(2);
            else
            {
                _codings.clear();
                if (_codingParams!=null)
                    _codingParams.clear();
            }

            // Handle transfer encoding
            while(transfer_coding.hasMoreElements())
                _codings.add(transfer_coding.nextElement());
            for(int i=_codings.size();i-->0;)
            {        
                String value=_codings.get(i).toString();
                if (_codingParams==null && value.indexOf(';')>0)
                    _codingParams=new HashMap(7);
                String coding=HttpFields.valueParameters(value,_codingParams);
                
                if (HttpFields.__Identity.equalsIgnoreCase(coding))
                    continue;                

                // Ignore identity coding
                if (HttpFields.__Identity.equalsIgnoreCase(coding))
                    continue;
                
                // Handle Chunking
                if (HttpFields.__Chunked.equalsIgnoreCase(coding))
                {
                    // chunking must be last and have no parameters
                    if (i+1<_codings.size() || _codingParams!=null&&_codingParams.size()>0)
                        throw new HttpException(_response.__400_Bad_Request,
                                                "Missing or incorrect chunked transfer-encoding");
                    _outputStream.setChunking();
                }
                else
                {
                    // Check against any TE field
                    List te = _request.getAcceptableTransferCodings();
                    if (te==null || !te.contains(coding))
                        throw new HttpException(_response.__501_Not_Implemented,
                                                "User agent does not accept "+
                                                coding+
                                                " transfer-encoding");
                    // Set coding
                    getHttpServer().getHttpEncoding()
                        .enableEncoding(_outputStream,coding,_codingParams);
                }
            }
        }

        // Nobble the OutputStream for HEAD requests
        if (_request.__HEAD.equals(_request.getMethod()))
            _outputStream.nullOutput();
    }

    
    /* ------------------------------------------------------------ */
    protected void commitResponse()
        throws IOException
    {            
        _outputSetup=true;
        
        // Handler forced close, listener stopped or no idle threads left.
        _close=HttpFields.__Close.equals(_response.getField(HttpFields.__Connection));
        if (!_close && (!_listener.isStarted()||_listener.isOutOfResources()))
        {
            _close=true;
            _response.setField(HttpFields.__Connection,
                               HttpFields.__Close);
        }
        if (_close)
            _persistent=false;
        
        // if we have no content or encoding, and no content length
        int status = _response.getStatus();
        if (status!=HttpResponse.__304_Not_Modified &&
            status!=HttpResponse.__204_No_Content &&
            !_outputStream.isWritten() &&
            !_response.containsField(HttpFields.__ContentLength) &&
            !_response.containsField(HttpFields.__TransferEncoding))
        {
            if(_persistent)
            {
                switch (_dotVersion)
                {
                  case 0:
                      {
                          _close=true;
                          _persistent=false;
                          _response.setField(HttpFields.__Connection,
                                             HttpFields.__Close);
                      }
                      break;
                  case 1:
                      {
                          // force chunking on.
                          _response.setField(HttpFields.__TransferEncoding,
                                             HttpFields.__Chunked);
                          _outputStream.setChunking();
                      }
                      break;
                      
                  default:
                      _close=true;
                      _response.setField(HttpFields.__Connection,
                                         HttpFields.__Close);
                      break;
                }
            }
            else
            {
                _close=true;
                _response.setField(HttpFields.__Connection,
                                   HttpFields.__Close);
            }
        }
    }

    
    /* ------------------------------------------------------------ */
    /* Exception reporting policy method.
     * @param e the Throwable to report.
     */
    private void exception(Throwable e)
    {
	//// ROC PINPOINT MIKECHEN EMK BEGIN ////
	if( ROCConfig.ENABLE_PINPOINT &&
	    ROCConfig.ENABLE_PINPOINT_TRACING_HTTP ) {
	    try {
		java.util.Map PP_originInfo = new java.util.HashMap( roc.pinpoint.tracing.java.EnvironmentDetails.GetDetails() );
		// PP_originInfo.put( "name", PP_name );
		roc.pinpoint.tracing.RequestInfo PP_reqInfo = roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo();
		PP_reqInfo.incrementSeqNum();
		
		java.util.Map PP_rawDetails = new java.util.HashMap();
		StackTraceElement[] PP_ste = e.getStackTrace();
		java.util.List PP_stacktrace = new java.util.ArrayList( PP_ste.length );
		for( int PP_i=0; PP_i < PP_ste.length; PP_i++ ) {
		    // todo, later, we might want a more structure storage 
		    //       of the details in PP_ste[ PP_i ], rather than
		    //       just a toString() dump.
		    PP_stacktrace.add( PP_ste[ PP_i ].toString() );
		}
		PP_rawDetails.put( "exception", e.toString() );
		PP_rawDetails.put( "stacktrace", PP_stacktrace );
		
		java.util.Map PP_attributes = new HashMap();
		PP_attributes.put( "observationLocation",
				   "org.mortbay.http.HttpConnection" );
		
		roc.pinpoint.tracing.Observation PP_obs =
		    new roc.pinpoint.tracing.Observation( roc.pinpoint.tracing.Observation.EVENT_ERROR,
							  PP_reqInfo,
							  PP_originInfo,
							  PP_rawDetails,
							  PP_attributes );
		roc.pinpoint.tracing.GlobalObservationPublisher.Send( PP_obs );
	    }
	    catch (Exception ex) {
		ex.printStackTrace();
	    }
	}

	//// ROC PINPOINT MIKECHEN EMK END   ////

	try{
	    _persistent=false;
	    boolean gotIOException = false;
            int error_code=HttpResponse.__500_Internal_Server_Error;
            
            if (e instanceof HttpException)
            {
                error_code=((HttpException)e).getCode();
                
                if (_request==null)
                    Code.warning(e.toString());
                else
                    Code.warning(_request.getRequestLine()+" "+e.toString());
                Code.debug(e);
            }
            else
            {    
                _request.setAttribute("javax.servlet.error.exception_type",e.getClass());
                _request.setAttribute("javax.servlet.error.exception",e);

                if (e instanceof IOException)
                {
                    // Assume browser closed connection
                    gotIOException = true;
                    if (Code.verbose())
                        Code.debug(e);
                    else if (Code.debug())
                        Code.debug(e.toString());
                }
                else 
                {
                    if (_request==null)
                        Code.warning(e);
                    else
                        Code.warning(_request.getRequestLine(),e);
                }
            }
            
	    if (_response != null && !_response.isCommitted())
	    {
		_response.reset();
		_response.removeField(HttpFields.__TransferEncoding);
		_response.setField(HttpFields.__Connection,HttpFields.__Close);
		_response.sendError(error_code);

                // probabluy not browser so be more verbose
		if (gotIOException)
                {
                    if (_request==null)
                        Code.warning(e);
                    else
                        Code.warning(_request.getRequestLine(),e);
                }
	    }
	}
        catch(Exception ex)
        {
            Code.ignore(ex);
        }
    }

    
    /* ------------------------------------------------------------ */
    /** Service a Request.
     * This implementation passes the request and response to the
     * service method of the HttpServer for this connections listener.
     * If no HttpServer has been associated, the 503 is returned.
     * This method may be specialized to implement other ways of
     * servicing a request.
     * @param request The request
     * @param response The response
     * @return The HttpContext that completed handling of the request or null.
     * @exception HttpException 
     * @exception IOException 
     */
    protected HttpContext service(HttpRequest request, HttpResponse response)
        throws HttpException, IOException
    {
        if (_httpServer==null)
                throw new HttpException(response.__503_Service_Unavailable);
        return _httpServer.service(request,response);
    }
    
    /* ------------------------------------------------------------ */
    /** Handle the connection.
     * Once the connection has been created, this method is called
     * to handle one or more requests that may be received on the
     * connection.  The method only returns once all requests have been
     * handled, an error has been returned to the requestor or the
     * connection has been closed.
     * The handleNext() is called in a loop until it returns false.
     */
    public final void handle()
    {
        try
        {
            associateThread();
            while(_listener.isStarted() && handleNext())
                recycle();
        }
        finally
        {
            disassociateThread();
            destroy();
        }
    }
    
    /* ------------------------------------------------------------ */
    protected void associateThread()
    {
        __threadConnection.set(this);
        _handlingThread=Thread.currentThread();
    }
    
    /* ------------------------------------------------------------ */
    protected void disassociateThread()
    {
        _handlingThread=null;
        __threadConnection.set(null);
    }

    
    /* ------------------------------------------------------------ */
    protected void readRequest()
        throws IOException
    {
        Code.debug("readRequest() ...");
        _request.readHeader((LineInput)((ChunkableInputStream)_inputStream)
                            .getRawStream());
    }
    
    /* ------------------------------------------------------------ */
    /** Handle next request off the connection.
     * The service(request,response) method is called by handle to
     * service each request received on the connection.
     * If the thread is a PoolThread, the thread is set as inactive
     * when waiting for a request. 
     * <P>
     * The Connection is set as a ThreadLocal of the calling thread and is
     * available via the getHttpConnection() method.
     * @return true if the connection is still open and may provide
     * more requests.
     */
    public boolean handleNext()
    {
        HttpContext context=null;


	//// ROC PINPOINT MIKECHEN EMK BEGIN ////

	String PP_name = null;
	java.util.Map PP_originInfo = null;
	java.util.Map PP_attributes = null;

	if( ROCConfig.ENABLE_PINPOINT &&
	    ROCConfig.ENABLE_PINPOINT_TRACING_HTTP ) {
	    // 1. insert the Request ID in the HTTP header
	    //    as well as the current thread
	
	    roc.pinpoint.tracing.RequestInfo PP_reqInfo = new roc.pinpoint.tracing.RequestInfo();
	    PP_attributes = new HashMap();
	    PP_attributes.put( "observationLocation",
			       "org.mortbay.http.HttpConnection" );
	    
	    roc.pinpoint.tracing.ThreadedRequestTracer.setRequestInfo( PP_reqInfo );
	    _request.setField("TRACER_REQUEST_ID", PP_reqInfo.getRequestId() );
	    _request.setField("TRACER_REQUEST_SEQ", "0");  // emk: what's the point of this? seq is always zero here...
	}	
	
	//// ROC PINPOINT MIKECHEN EMK END   ////

        try
        {   
            // Assume the connection is not persistent,
            // unless told otherwise.
            _persistent=false;
            _close=false;
            _keepAlive=false;
            _outputSetup=false;
            _dotVersion=0;

            // Read requests
            readRequest();
            _listener.customizeRequest(this,_request);
            if (_request.getState()!=HttpMessage.__MSG_RECEIVED)
                throw new HttpException(_response.__400_Bad_Request);
            

	    PP_name = _request.getURI().toString();

	    //// ROC PINPOINT MIKECHEN EMK BEGIN ////
	    if( ROCConfig.ENABLE_PINPOINT &&
		ROCConfig.ENABLE_PINPOINT_TRACING_HTTP ) {
		if (!PP_name.endsWith(".gif") && !PP_name.endsWith(".jpg")) {
		    try {
			PP_originInfo = new java.util.HashMap( roc.pinpoint.tracing.java.EnvironmentDetails.GetDetails() );
			PP_originInfo.put( "name", PP_name );
			
			String requestclassifier = null;
			int index = PP_name.indexOf( "?" );
			if( index == -1 )
			    requestclassifier = PP_name;
			else
			    requestclassifier = PP_name.substring( 0, index );
			PP_originInfo.put( "requestclassifier", 
					   requestclassifier );
			roc.pinpoint.tracing.RequestInfo PP_reqInfo = roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo();
			PP_reqInfo.incrementSeqNum();
			PP_attributes.put( "stage", "METHODCALLBEGIN" );
			
			roc.pinpoint.tracing.Observation PP_obs =
			    new roc.pinpoint.tracing.Observation( roc.pinpoint.tracing.Observation.EVENT_COMPONENT_USE,
								  PP_reqInfo,
								  PP_originInfo,
								  null,
								  PP_attributes );
			roc.pinpoint.tracing.GlobalObservationPublisher.Send( PP_obs );
		    }
		    catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    }
	    //// ROC PINPOINT MIKECHEN EMK END   ////


            // We have a valid request!
            statsRequestStart();
            if (Code.debug())
            {
                _response.setField("Jetty-Request",
                                   _request.getRequestLine());
                Code.debug("REQUEST:\n",_request);
            }
            
            // Pick response version
            _version=_request.getVersion();
            _dotVersion=_request.getDotVersion();
            
            if (_dotVersion>1)
            {
                _version=HttpMessage.__HTTP_1_1;
                _dotVersion=1;
            }
            
            // Common fields on the response
            _response.setVersion(HttpMessage.__HTTP_1_1);
            _response.setField(HttpFields.__Date,_request.getTimeStampStr());
            _response.setField(HttpFields.__Server,Version.__VersionDetail);
            _response.setField(HttpFields.__ServletEngine,Version.__ServletEngine);
            
            // Handle Connection header field
            Enumeration connectionValues =
                _request.getFieldValues(HttpFields.__Connection,
                                        HttpFields.__separators);
            if (connectionValues!=null)
            {
                while (connectionValues.hasMoreElements())
                {
                    String token=connectionValues.nextElement().toString();
                    // handle close token
                    if (token.equalsIgnoreCase(HttpFields.__Close))
                    {
                        _close=true;
                        _response.setField(HttpFields.__Connection,
                                           HttpFields.__Close);
                    }
                    else if (token.equalsIgnoreCase(HttpFields.__KeepAlive) &&
                             _dotVersion==0)
                        _keepAlive=true;
                    
                    // Remove headers for HTTP/1.0 requests
                    if (_dotVersion==0)
                        _request.forceRemoveField(token);
                }
            }
            
            // Handle version specifics
            if (_dotVersion==1)
                verifyHTTP_1_1();
            else if (_dotVersion==0)
                verifyHTTP_1_0();
            else if (_dotVersion!=-1)
                throw new HttpException(_response.__505_HTTP_Version_Not_Supported);
            
            if (Code.verbose(99))
                Code.debug("IN is "+
                           (_inputStream.isChunking()
                            ?"chunked":"not chunked")+
                           " Content-Length="+
                           _inputStream.getContentLength());
            
            // service the request
            if (!_request.isHandled())
                context=service(_request,_response);
        }
        catch(HttpException e) {exception(e);}
        catch (IOException e)
        {
            if (_request.getState()!=HttpMessage.__MSG_RECEIVED)
            {
                if (Code.debug())
                {
                    if (Code.verbose()) Code.debug(e);
                    else Code.debug(e.toString());
                }
                _response.destroy();
                _response=null;
            }
            else
                exception(e);
        }
        catch (Exception e)     {exception(e);}
        catch (Error e)         {exception(e);}
        finally
        {
            int bytes_written=0;
            int content_length = _response==null
                ?-1:_response.getIntField(HttpFields.__ContentLength);
                
            // Complete the request
            if (_persistent)
            {                    
                try{
                    // Read remaining input
                    while(_inputStream.skip(4096)>0 || _inputStream.read()>=0);
                }
                catch(IOException e)
                {
                    if (_inputStream.getContentLength()>0)
                        _inputStream.setContentLength(0);
                    _persistent=false;
                    Code.ignore(e);
                    exception(new HttpException(_response.__400_Bad_Request,
                                                "Missing Content"));
                }
                    
                // Check for no more content
                if (_inputStream.getContentLength()>0)
                {
                    _inputStream.setContentLength(0);
                    _persistent=false;
                    exception (new HttpException(_response.__400_Bad_Request,
                                                 "Missing Content"));
                }
                
                // Commit the response
                try{
                    _outputStream.flush(true);
                    _response.commit();
                    bytes_written=_outputStream.getBytesWritten();
                    _outputStream.resetStream();
                    _outputStream.addObserver(this);
                        _inputStream.resetStream();
                }
                catch(IOException e) {exception(e);}
            }
            else if (_response!=null) // There was a request
            {
                // half hearted attempt to eat any remaining input
                try{
                    if (_inputStream.getContentLength()>0)
                        while(_inputStream.skip(4096)>0 || _inputStream.read()>=0);
                    _inputStream.resetStream();
                }
                catch(IOException e){Code.ignore(e);}
                
                // commit non persistent
                try{
                    _outputStream.flush();
                    _response.commit();
                    bytes_written=_outputStream.getBytesWritten();
                    _outputStream.close();
                    _outputStream.resetStream();
                }
                catch(IOException e) {exception(e);}
            }
            
            // Check response length
            if (_response!=null)
            {
                Code.debug("RESPONSE:\n",_response);
                if (_persistent &&
                    content_length>=0 && bytes_written>0 && content_length!=bytes_written)
                {
                    Code.warning("Invalid length: Content-Length="+content_length+
                                 " written="+bytes_written+
                                 " for "+_request.getRequestURL());
                    _persistent=false;
                    try{_outputStream.close();}
                    catch(IOException e) {Code.warning(e);}
                }    
            }
            
            // stats & logging
            statsRequestEnd();       
            if (context!=null)
                context.log(_request,_response,bytes_written);


	    //// ROC PINPOINT MIKECHEN EMK BEGIN ////
	    
	    if( ROCConfig.ENABLE_PINPOINT &&
		ROCConfig.ENABLE_PINPOINT_TRACING_HTTP ) {
		//// 1. if it's a valid request, then we send the "methodend message"
		if (_request != null && _request.getURI() != null) {
		    if (!PP_name.endsWith(".gif") && !PP_name.endsWith( ".jpg" )) {
			try {
			    roc.pinpoint.tracing.RequestInfo PP_reqInfo = roc.pinpoint.tracing.ThreadedRequestTracer.getRequestInfo();
			    PP_reqInfo.incrementSeqNum();
			    PP_attributes.put( "stage", "METHODCALLEND" );
			    roc.pinpoint.tracing.Observation PP_obs =
				new roc.pinpoint.tracing.Observation( roc.pinpoint.tracing.Observation.EVENT_COMPONENT_USE,
								      PP_reqInfo,
								      PP_originInfo,
								      null,
								      PP_attributes );
			    roc.pinpoint.tracing.GlobalObservationPublisher.Send( PP_obs );
			}
			catch (Exception e) {
			    e.printStackTrace();
			}
		    }
		}
	    }
	    //// ROC PINPOINT MIKECHEN EMK END   ////

        }
        
        return _persistent;
    }

    /* ------------------------------------------------------------ */
    protected void statsRequestStart()
    {
        if (_statsOn)
        {
            if (_reqTime>0)
                statsRequestEnd();
            _requests++;
            _tmpTime=_request.getTimeStamp();
            _reqTime=_tmpTime;
            _httpServer.statsGotRequest();
        }
    }

    /* ------------------------------------------------------------ */
    protected void statsRequestEnd()
    {
        if (_statsOn && _reqTime>0)
        {
            _httpServer.statsEndRequest(System.currentTimeMillis()-_reqTime,
                                        (_response!=null));
            _reqTime=0;
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Recycle the connection.
     * called by handle when handleNext returns true.
     */
    protected void recycle()
    {
        _listener.persistConnection(this);
        if (_request!=null)
            _request.recycle(this);
        if (_response!=null)
            _response.recycle(this);
    }
    
    /* ------------------------------------------------------------ */
    /** Destroy the connection.
     * called by handle when handleNext returns false.
     */
    protected void destroy()
    {
        try{close();}
        catch (IOException e){Code.ignore(e);}
        catch (Exception e){Code.warning(e);}
        
        // Destroy request and response
        if (_request!=null)
            _request.destroy();
        if (_response!=null)
            _response.destroy();
        if (_inputStream!=null)
            _inputStream.destroy();
        if (_outputStream!=null)
            _outputStream.destroy();
        _inputStream=null;
        _outputStream=null;
        _request=null;
        _response=null;
        _handlingThread=null;
        
        if (_statsOn)
        {
            _tmpTime=System.currentTimeMillis();
            if (_reqTime>0)
                _httpServer.statsEndRequest(_tmpTime-_reqTime,false);
            _httpServer.statsCloseConnection(_tmpTime-_openTime,_requests);
        }
    }
}

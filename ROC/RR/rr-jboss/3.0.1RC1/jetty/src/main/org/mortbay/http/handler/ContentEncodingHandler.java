// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ContentEncodingHandler.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.http.handler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.mortbay.http.ChunkableInputStream;
import org.mortbay.http.ChunkableOutputStream;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpMessage;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.HttpEncoding;
import org.mortbay.http.HttpContext;
import org.mortbay.http.OutputObserver;
import org.mortbay.http.PathMap;
import org.mortbay.util.Code;

/* ------------------------------------------------------------ */
/** Content Encoding Handler.
 * This handler can perform decoding or encoding of the content for
 * requests and responses.
 *
 * Encodings supported are provided by an instance of the HttpEncoding
 * class registered with the HttpServer instance. The default
 * implementation understands identity, gzip and deflate.
 *
 * If an incoming request has a known content-encoding, then a input
 * filter stream is inserted to decode the content and the
 * content-ecoding header is removed so that subsequent filters or
 * servlets will not attempt to decode again.
 *
 * If a request indicates that it will accept a known encoding, then
 * an OutputObserver is attached to the responses output stream.  If
 * at the time it is committing there is content within the size
 * limits, then an output filter is attached and a content-encoding
 * header inserted.    If a content-encoding header already exists,
 * nothing is done as it is assume that a filter or serlvet has
 * already performed the encoding.
 *
 * If path specification are added to the handler, then it is only
 * triggered on requests with matching paths.
 *
 * It is far more efficient to use this handler than compression
 * filters within webapplications.
 * 
 * @version $Id: ContentEncodingHandler.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
 * @author Greg Wilkins (gregw)
 */
public class ContentEncodingHandler
    extends AbstractHttpHandler
    implements OutputObserver
{
    private HttpEncoding _httpEncoding;
    private int _minimumLength=512;
    private PathMap _pathMap;
    
    /* ------------------------------------------------------------ */
    public void start()
        throws Exception
    {
        super.start();
        _httpEncoding = getHttpContext().getHttpServer().getHttpEncoding();
    }
    
    /* ------------------------------------------------------------ */
    public void setMinimumLength(int l)
    {
        _minimumLength=l;
    }
    
    /* ------------------------------------------------------------ */
    public int getMinimumLength()
    {
        return _minimumLength;
    }

    /* ------------------------------------------------------------ */
    /** Add a PathSpecification.
     * Restrict the actions of this handler to matching paths.
     * @param pathSpec 
     */
    public void addPathSpec(String pathSpec)
    {
        if (_pathMap==null)
            _pathMap=new PathMap();
        _pathMap.put(pathSpec,pathSpec);
    }
    
    /* ------------------------------------------------------------ */
    public void handle(String pathInContext,
                       String pathParams,
                       HttpRequest request,
                       HttpResponse response)
        throws HttpException, IOException
    {
        // check path
        if (_pathMap!=null && _pathMap.getMatch(pathInContext)==null)
            return;

        // Handle request encoding
        String encoding=request.getField(HttpFields.__ContentEncoding);
        if (encoding!=null && _httpEncoding.knownEncoding(encoding))
        {
            Map encodingParams=null;
            if (encoding.indexOf(";")>0)
            {
                encodingParams=new HashMap(3);
                encoding=HttpFields.valueParameters(encoding,encodingParams);
            }
            
            _httpEncoding.enableEncoding((ChunkableInputStream)request.getInputStream(),
                                         encoding,encodingParams);
            request.setState(HttpMessage.__MSG_EDITABLE);
            request.removeField(HttpFields.__ContentEncoding);
            request.setState(HttpMessage.__MSG_RECEIVED);
        }
        
        // Handle response encoding.
        List list =
            HttpFields.qualityList(request.getFieldValues(HttpFields.__AcceptEncoding,
                                                          HttpFields.__separators));
        for (int i=0;i<list.size();i++)
        {
            encoding = HttpFields.valueParameters((String)list.get(i),null);
            
            if (_httpEncoding.knownEncoding(encoding))
            {
                // We can handle this encoding, so observe for content.
                ChunkableOutputStream out = (ChunkableOutputStream)response.getOutputStream();
                out.addObserver(this,response);
                response.setAttribute("Encoding",encoding);
                break;
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Output Notification Method.
     * The COMMITING event notification is used to test and trigger
     * output encoding.
     * @param out The output stream.
     * @param event The notified event.
     * @param data The associated response object.
     */
    public void outputNotify(ChunkableOutputStream out, int event, Object data)
    {
        switch (event)
        {
          case OutputObserver.__FIRST_WRITE:
              HttpResponse response = (HttpResponse)data;
              String encoding=(String)response.getAttribute("Encoding");
              if (encoding==null)
                  return;

              // check the known length
              int l=response.getContentLength();
              if (l>=0 && l<_minimumLength)
              {
                  if (Code.debug())
                      Code.debug("Abort encoding due to size: "+l);
                  return;
              }

              // Is it already encoded?
              if (response.getField(HttpFields.__ContentEncoding)!=null)
              {
                  Code.debug("Already encoded!");
                  return;
              }

              // Initialize encoding!
              try
              {
                  Code.debug("Enable: ",encoding);
                  _httpEncoding.enableEncoding(out,encoding,null);
                  response.setField(HttpFields.__ContentEncoding,encoding);
              }
              catch(Exception e)
              {
                  Code.ignore(e);
                  break;
              }
              response.removeField(HttpFields.__ContentLength);
              break;

          default:
        }
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return "ContentEncodingHandler>"+_minimumLength+
            (_pathMap==null?"[]":(_pathMap.keySet().toString()));
    }
}





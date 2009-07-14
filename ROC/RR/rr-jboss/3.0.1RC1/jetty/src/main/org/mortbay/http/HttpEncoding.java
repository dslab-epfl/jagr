// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: HttpEncoding.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.http;

import java.io.Serializable;
import java.util.Map;
import org.mortbay.util.Code;

/* ------------------------------------------------------------ */
/** HTTP content and transfer encodings.
 * This class or derivations of it, are used by the HttpServer to
 * apply transer and content encodings to requests and responses.
 *
 * This implementation handles the identity, gzip and deflate
 * encodings. The class can be specialized to handle more encodings
 * and an instance registered with HttpServer.
 *
 * @see HttpServer
 * @version 1.0 Wed Mar  6 2002
 * @author Greg Wilkins (gregw)
 */
public class HttpEncoding implements Serializable
{
    /* ------------------------------------------------------------ */
    public boolean knownEncoding(String coding)
    {
        return "gzip".equalsIgnoreCase(coding) ||
            "deflate".equalsIgnoreCase(coding) ||
            "identity".equalsIgnoreCase(coding);
    }
    
    /* ------------------------------------------------------------ */
    /** Enable encodings on InputStream.
     * @param in 
     * @param coding Coding enumeration
     * @exception HttpException 
     */
    public void enableEncoding(ChunkableInputStream in,
                               String coding,
                               Map parameters)
        throws HttpException
    {
        try
        {
            if ("gzip".equalsIgnoreCase(coding))
            {
                in.setFilterStream(new java.util.zip.GZIPInputStream(in.getFilterStream()));
            }
            else if ("deflate".equalsIgnoreCase(coding))
            {
                in.setFilterStream(new java.util.zip.InflaterInputStream(in.getFilterStream()));
            }
            else if (!"identity".equalsIgnoreCase(coding))
                throw new HttpException(HttpResponse.__501_Not_Implemented,
                                        "Unknown encoding "+coding);   
        }
        catch (HttpException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            Code.warning(e);
            throw new HttpException(HttpResponse.__500_Internal_Server_Error);
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Enable a encoding on a ChunkableOutputStream.
     * @param out
     * @param coding Coding name 
     * @param parameters Coding parameters or null
     * @exception HttpException 
     */
    public void enableEncoding(ChunkableOutputStream out,
                               String coding,
                               Map parameters)
        throws HttpException
    {
        try
        {
            if ("gzip".equalsIgnoreCase(coding))
            {
                out.setFilterStream(new java.util.zip.GZIPOutputStream(out.getFilterStream()));
            }
            else if ("deflate".equalsIgnoreCase(coding))
            {
                out.setFilterStream(new java.util.zip.DeflaterOutputStream(out.getFilterStream()));
            }
            else if (!"identity".equalsIgnoreCase(coding))
                throw new HttpException(HttpResponse.__501_Not_Implemented,
                                        "Unknown encoding "+coding);
        }
        catch (HttpException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            Code.warning(e);
            throw new HttpException(HttpResponse.__500_Internal_Server_Error);
        }
    }
}

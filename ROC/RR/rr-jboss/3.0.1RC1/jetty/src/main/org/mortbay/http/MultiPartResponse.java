// ========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: MultiPartResponse.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ------------------------------------------------------------------------

package org.mortbay.http;

import java.io.IOException;
import java.io.OutputStream;

/* ================================================================ */
/** Handle a multipart MIME response.
 *
 * @version $Id: MultiPartResponse.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
 * @author Greg Wilkins
 * @author Jim Crossley
*/
public class MultiPartResponse
{
    private static final byte[] __CRLF="\015\012".getBytes();
    private static final byte[] __DASHDASH="--".getBytes();
    
    /* ------------------------------------------------------------ */
    private String boundary =
        "org.mortbay.http.MultiPartResponse.boundary."+
        Long.toString(System.currentTimeMillis(),36);
    
    private byte[] boundaryBytes = boundary.getBytes();

    /* ------------------------------------------------------------ */
    public String getBoundary()
    {
        return boundary;
    }
    
    /* ------------------------------------------------------------ */    
    /** PrintWriter to write content too.
     */
    private OutputStream out = null; 
    public OutputStream getOut() {return out;}

    /* ------------------------------------------------------------ */
    private boolean inPart=false;
    
    /* ------------------------------------------------------------ */
    public MultiPartResponse(OutputStream out)
         throws IOException
    {
        this.out=out;
        inPart=false;
    }
    
    /* ------------------------------------------------------------ */
    /** MultiPartResponse constructor.
     */
    public MultiPartResponse(HttpResponse response)
         throws IOException
    {
        response.setField(HttpFields.__ContentType,"multipart/mixed;boundary="+boundary);
        out=response.getOutputStream();
        inPart=false;
    }    

    /* ------------------------------------------------------------ */
    /** Start creation of the next Content.
     */
    public void startPart(String contentType)
         throws IOException
    {
        if (inPart)
            out.write(__CRLF);
        inPart=true;
        out.write(__DASHDASH);
        out.write(boundaryBytes);
        out.write(__CRLF);
        out.write(("Content-type: "+contentType).getBytes());
        out.write(__CRLF);
        out.write(__CRLF);
    }
    
    /* ------------------------------------------------------------ */
    /** Start creation of the next Content.
     */
    public void startPart(String contentType, String[] headers)
         throws IOException
    {
        if (inPart)
            out.write(__CRLF);
        inPart=true;
        out.write(__DASHDASH);
        out.write(boundaryBytes);
        out.write(__CRLF);
        out.write(("Content-type: "+contentType).getBytes());
        out.write(__CRLF);
        for (int i=0;headers!=null && i<headers.length;i++)
        {
            out.write(headers[i].getBytes());
            out.write(__CRLF);
        }
        out.write(__CRLF);
    }
        
    /* ------------------------------------------------------------ */
    /** End the current part.
     * @param lastPart True if this is the last part
     * @exception IOException IOException
     */
    public void close()
         throws IOException
    {
        if (inPart)
            out.write(__CRLF);
        out.write(__DASHDASH);
        out.write(boundaryBytes);
        out.write(__DASHDASH);
        out.write(__CRLF);
        inPart=false;
    }
    
};





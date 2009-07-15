// ========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: MultiPartResponse.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ------------------------------------------------------------------------

package org.mortbay.servlet;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;



/* ================================================================ */
/** Handle a multipart MIME response.
 *
 *
 * @version $Id: MultiPartResponse.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
 * @author Greg Wilkins
 * @author Jim Crossley
*/
public class MultiPartResponse extends org.mortbay.http.MultiPartResponse
{
    /* ------------------------------------------------------------ */
    /** MultiPartResponse constructor.
     * @param response The ServletResponse to which this multipart
     *                 response will be sent.
     */
    public MultiPartResponse(HttpServletResponse response)
         throws IOException
    {
        super(response.getOutputStream());
        response.setContentType("multipart/mixed;boundary="+getBoundary());
    }
    
};





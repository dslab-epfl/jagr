// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: NotFoundServlet.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ---------------------------------------------------------------------------

package org.mortbay.servlet;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ------------------------------------------------------------ */
/** Not Found Servlet.
 * Utility servlet to protect a URI by always responding with 404.
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class NotFoundServlet extends HttpServlet
{
    /* ------------------------------------------------------------ */
    public void doPost(HttpServletRequest req, HttpServletResponse res) 
        throws ServletException, IOException
    {
        res.sendError(404);
    }
    
    /* ------------------------------------------------------------ */
    public void doGet(HttpServletRequest req, HttpServletResponse res) 
        throws ServletException, IOException
    {
        res.sendError(404);
    }
}

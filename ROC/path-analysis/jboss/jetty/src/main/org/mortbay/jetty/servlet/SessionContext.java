// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: SessionContext.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ---------------------------------------------------------------------------

package org.mortbay.jetty.servlet;
import java.util.Collections;
import java.util.Enumeration;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/* ------------------------------------------------------------ */
/** 
 * Null returning implementation of HttpSessionContext
 * @version $Id: SessionContext.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Greg Wilkins (gregw)
 */
public class SessionContext implements HttpSessionContext
{
    /* ------------------------------------------------------------ */
    public static final HttpSessionContext NULL_IMPL = new SessionContext();

    /* ------------------------------------------------------------ */
    private SessionContext(){}
    
    /* ------------------------------------------------------------ */
    /**
     * @deprecated From HttpSessionContext
     */
    public Enumeration getIds()
    {
        return Collections.enumeration(Collections.EMPTY_LIST);
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated From HttpSessionContext
     */
    public HttpSession getSession(String id)
    {
        return null;
    }
}

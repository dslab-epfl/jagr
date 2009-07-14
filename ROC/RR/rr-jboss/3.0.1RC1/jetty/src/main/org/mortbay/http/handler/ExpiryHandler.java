// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ExpiryHandler.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.http.handler;

import java.io.IOException;
import org.mortbay.util.Code;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import java.util.Date;

/* ------------------------------------------------------------ */
/**
 * Handler that allows the default Expiry of all content to be set.
 * 
 * @version $Id: ExpiryHandler.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
 * @author Brett Sealey
 */
public class ExpiryHandler extends AbstractHttpHandler
{
    /**
     * The default expiry time in seconds
     */
    private long _ttl=-1;

    /**
     * Set the default expiry time in seconds.
     *
     * @param ttl The default time to live in seconds. If negative (the
     * default) then all content will be set to expire 01Jan1970 by default.
     */
    public void setTimeToLive(long ttl) {
	_ttl=ttl;
    }
    
    /* ------------------------------------------------------------ */
    public void handle(String pathInContext,
                       String pathParams,
                       HttpRequest request,
                       HttpResponse response)
        throws HttpException, IOException
    {
        Code.debug("ExpiryHandler.handle()");
	String expires;
	if (_ttl<0)
	    expires=HttpFields.__01Jan1970;
	else
	    expires=HttpFields.__dateSend
		.format(new
		    Date(System.currentTimeMillis()+1000L*_ttl));
	response.setField(HttpFields.__Expires,expires);
    }
}

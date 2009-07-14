// ========================================================================
// Copyright (c) 2000 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: RequestLog.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.http;

import org.mortbay.util.LifeCycle;
import java.io.Serializable;

/* ------------------------------------------------------------ */
/** Abstract HTTP Request Log format
 * @version $Id: RequestLog.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Tony Thompson
 * @author Greg Wilkins
 */
public interface RequestLog
    extends LifeCycle,
            Serializable
{
    public void log(HttpRequest request,
                    HttpResponse response,
                    int responseLength);
}


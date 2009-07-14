// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: OutputObserver.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.http;

import java.io.IOException;

/* ------------------------------------------------------------ */
/** Observer output events.
 *
 * @see ChunkableOutputStream
 * @version $Id: OutputObserver.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Greg Wilkins (gregw)
 */
public interface OutputObserver
{
    public final static int
        __FIRST_WRITE=0,
        __RESET_BUFFER=1,
        __COMMITING=2,
        __CLOSING=4,
        __CLOSED=5;
    
    /* ------------------------------------------------------------ */
    /** Notify an output action.
     * @param out The OutputStream that caused the event
     * @param action The action taken
     * @param data Data associated with the event.
     */
    void outputNotify(ChunkableOutputStream out, int action, Object data)
        throws IOException;
}

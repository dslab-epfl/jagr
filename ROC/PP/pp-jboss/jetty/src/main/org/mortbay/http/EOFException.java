// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: EOFException.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.http;
import java.io.IOException;


/* ------------------------------------------------------------ */
/** Exception for EOF detected. 
 *
 * @version $$
 * @author Greg Wilkins (gregw)
 */
public class EOFException extends IOException
{
    private IOException _ex;

    public IOException getTargetException()
    {
        return _ex;
    }
    
    
    public EOFException()
    {}
    
    public EOFException(IOException ex)
    {
        _ex=ex;
    }

    public String toString()
    {
        return "EOFException("+
            (_ex==null?"":(_ex.toString()))+
            ")";
    }
}

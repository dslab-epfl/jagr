// ========================================================================
// Copyright (c) 1997 MortBay Consulting, Sydney
// $Id: CodeException.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.util;

/* ------------------------------------------------------------ */
/** Code Exception.
 * 
 * Thrown by Code.assert or Code.fail
 * @see Code
 * @version  $Id: CodeException.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
 * @author Greg Wilkins
 */
public class CodeException extends RuntimeException
{
    /* ------------------------------------------------------------ */
    /** Default constructor. 
     */
    public CodeException()
    {
        super();
    }

    public CodeException(String msg)
    {
        super(msg);
    }    
}


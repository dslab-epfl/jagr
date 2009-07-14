// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Tag.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ---------------------------------------------------------------------------

package org.mortbay.html;
import java.io.IOException;
import java.io.Writer;

/* -------------------------------------------------------------------- */
/** HTML Tag Element.
 * A Tag element is of the generic form &lt;TAG attributes... &gt;
 * @see  org.mortbay.html.Element
 */
public class Tag extends Element
{
    /* ---------------------------------------------------------------- */
    protected String tag;

    /* ---------------------------------------------------------------- */
    public Tag(String tag)
    {
        this.tag=tag;
    }
    
    /* ---------------------------------------------------------------- */
    public void write(Writer out)
         throws IOException
    {
        out.write('<'+tag+attributes()+'>');
    }
}


// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: Comment.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.html;
import java.io.IOException;
import java.io.Writer;


/* ------------------------------------------------------------ */
/** HTML Comment.
 * @version $Id: Comment.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
 * @author Greg Wilkins (gregw)
 */
public class Comment extends Composite
{
    /* ----------------------------------------------------------------- */
    public void write(Writer out)
         throws IOException
    {
        out.write("<!--\n");
        super.write(out);
        out.write("\n-->");
    }
};

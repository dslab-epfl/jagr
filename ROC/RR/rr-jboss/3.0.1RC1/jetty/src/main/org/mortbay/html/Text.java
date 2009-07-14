// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Text.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ---------------------------------------------------------------------------

package org.mortbay.html;
import java.util.Vector;

/* -------------------------------------------------------------------- */
/** A simple block of straight text.
 * @deprecated all Composites now take Strings direct.
 */
public class Text extends Composite
{
    /* ----------------------------------------------------------------- */
    public Text()
    {}

    /* ----------------------------------------------------------------- */
    public Text(String s)
    {
        add(s);
    }

    /* ----------------------------------------------------------------- */
    public Text(String[] s)
    {
        add(s);
    }

    /* ----------------------------------------------------------------- */
    public Text add(String[] s)
    {
        for (int i=0;i<s.length;i++)
            add(s[i]);
        return this;
    }

    /* ----------------------------------------------------------------- */
    public Text add(Vector v)
    {
        for (int i=0;i<v.size();i++)
            add(v.elementAt(i));
        return this;
    }
}

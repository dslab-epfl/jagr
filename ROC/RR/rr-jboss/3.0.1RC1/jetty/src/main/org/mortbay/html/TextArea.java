// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: TextArea.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ---------------------------------------------------------------------------

package org.mortbay.html;

/* -------------------------------------------------------------------- */
/** A Text Area within a form.
 * <p> The text in the textarea is handled by the super class, Text
 * @see org.mortbay.html.Text
 */
public class TextArea extends Block
{
    /* ----------------------------------------------------------------- */
    /** @param name The name of the TextArea within the form */
    public TextArea(String name)
    {
        super("textarea");
        attribute("name",name);
    }

    /* ----------------------------------------------------------------- */
    /** @param name The name of the TextArea within the form
     * @param s The string in the text area */
    public TextArea(String name, String s)
    {
        this(name);
        add(s);
    }

    /* ----------------------------------------------------------------- */
    public TextArea setSize(int cols,int lines)
    {
        attribute("rows",lines);
        attribute("cols",cols);
        return this;
    }
}


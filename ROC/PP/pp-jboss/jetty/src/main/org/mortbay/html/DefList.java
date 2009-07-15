// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: DefList.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ---------------------------------------------------------------------------

package org.mortbay.html;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

// =======================================================================
public class DefList extends Element
{

    // ------------------------------------------------------------
    public DefList()
    {
        terms = new Vector();
        defs = new Vector();
    }

    // ------------------------------------------------------------
    public void add(Element term, Element def)
    {
        terms.addElement(term);
        defs.addElement(def);
    }

    // ------------------------------------------------------------
    public void write(Writer out)
         throws IOException
    {
        out.write("<dl"+attributes()+">");

        if (terms.size() != defs.size())
            throw new Error("mismatched Vector sizes");

        for (int i=0; i <terms.size() ; i++)
        {
            out.write("<dt>");
            ((Element)terms.elementAt(i)).write(out);
            out.write("</dt><dd>");
            ((Element)defs.elementAt(i)).write(out);
            out.write("</dd>");
        }

        out.write("</dl>");
    }

    // ------------------------------------------------------------
    private Vector terms;
    private Vector defs;
}


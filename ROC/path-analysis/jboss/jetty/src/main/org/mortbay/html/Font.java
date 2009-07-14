// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Font.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ---------------------------------------------------------------------------

package org.mortbay.html;


/* -------------------------------------------------------------------- */
/** HTML Font Block.
 * Each Element added to the List (which is a Composite) is treated
 * as a new List Item.
 * @see  org.mortbay.html.Block
 */
public class Font extends Block
{    
    /* ----------------------------------------------------------------- */
    public Font()
    {
        super("font");
    }
    
    /* ----------------------------------------------------------------- */
    public Font(int size)
    {
        this();
        size(size);
    }
    
    /* ----------------------------------------------------------------- */
    public Font(int size, boolean relativeSize)
    {
        this();
        size(((relativeSize && size>=0)?"+":"")+size);
    }
    
    /* ----------------------------------------------------------------- */
    public Font(int size,String attributes)
    {
        this();
        size(size);
        this.attribute(attributes);
    }
    
    /* ----------------------------------------------------------------- */
    public Font(String attributes)
    {
        super("font",attributes);
    }
    
    /* ----------------------------------------------------------------- */
    public Font face(String face)
    {
        attribute("face",face);
        return this;
    }
    
}




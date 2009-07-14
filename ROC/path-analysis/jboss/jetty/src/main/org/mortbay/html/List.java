// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: List.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ---------------------------------------------------------------------------

package org.mortbay.html;


/* -------------------------------------------------------------------- */
/** HTML List Block.
 * Each Element added to the List (which is a Composite) is treated
 * as a new List Item.
 * @see  org.mortbay.html.Block
 */
public class List extends Block
{
    /* ----------------------------------------------------------------- */
    public static final String Unordered="ul";
    public static final String Ordered="ol";
    public static final String Menu="menu";
    public static final String Directory="dir";
    
    /* ----------------------------------------------------------------- */
    public List(String type)
    {
        super(type);
    }   
    
    /* ----------------------------------------------------------------- */
    /** 
     * @param o The item
     * @return This List.
     */
    public Composite add(Object o)
    {
        super.add("<li>");
        super.add(o);
        super.add("</li>");
        return this;
    }
    
    /* ----------------------------------------------------------------- */
    /** 
     * @return The new Item composite
     */
    public Composite newItem()
    {
        super.add("<li>");
        Composite composite=new Composite();
        super.add(composite);
	super.add("</li>");
        return composite;
    }

    
}







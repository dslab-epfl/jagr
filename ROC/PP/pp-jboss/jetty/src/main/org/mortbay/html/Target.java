// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Target.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ---------------------------------------------------------------------------

package org.mortbay.html;


/* -------------------------------------------------------------------- */
/** HTML Link Target.
 * This is a HTML reference (not a CSS Link).
 * @see StyleLink
 */
public class Target extends Block
{

    /* ----------------------------------------------------------------- */
    /** Construct Link.
     * @param target The target name 
     */
    public Target(String target)
    {
        super("a");
        attribute("name",target);
    }

    /* ----------------------------------------------------------------- */
    /** Construct Link.
     * @param target The target name 
     * @param link Link Element
     */
    public Target(String target,Object link)
    {
        this(target);
        add(link);
    }
}

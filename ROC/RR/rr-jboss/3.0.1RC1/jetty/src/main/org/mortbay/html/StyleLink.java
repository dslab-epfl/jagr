// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: StyleLink.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.html;


/* ------------------------------------------------------------ */
/** CSS Style LINK.
 *
 * @version $Id: StyleLink.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
 * @author Greg Wilkins (gregw)
 */
public class StyleLink extends Tag
{
    public final static String
        REL="rel",
        HREF="href",
        TYPE=Style.TYPE,
        MEDIA=Style.MEDIA;
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param href The URL of the style sheet
     */
    public StyleLink(String href)
    {
        super("link");
        attribute(REL,Style.StyleSheet);
        attribute(HREF,href);
        attribute(TYPE,Style.text_css);
    }
    
    /* ------------------------------------------------------------ */
    /** Full Constructor. 
     * @param rel Style Relationship, default StyleSheet if null.
     * @param href The URL of the style sheet
     * @param type The type, default text/css if null
     * @param media The media, not specified if null
     */
    public StyleLink(String rel, String href, String type, String media)
    {
        super("link");
        attribute(REL,rel==null?Style.StyleSheet:rel);
        attribute(HREF,href);
        attribute(TYPE,type==null?Style.text_css:type);
        if (media!=null)
            attribute(MEDIA,media);
    }
    
};









// ===========================================================================
// Copyright (c) 2001 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: ByteArrayOutputStream2.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ---------------------------------------------------------------------------

package org.mortbay.util;
import java.io.ByteArrayOutputStream;

/* ------------------------------------------------------------ */
/** ByteArrayOutputStream with public internals

 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class ByteArrayOutputStream2 extends ByteArrayOutputStream
{
    public ByteArrayOutputStream2(){super();}
    public ByteArrayOutputStream2(int size){super(size);}
    public byte[] getBuf(){return buf;}
    public int getCount(){return count;}
}

// ===========================================================================
// Copyright (c) 2002 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: TypeUtil.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ---------------------------------------------------------------------------

package org.mortbay.util;

/* ------------------------------------------------------------ */
/** TYPE Utilities
 * Provides a cache for basic Types.  The cache size can be controlled with
 * the "org.mortbay.util.TypeUtil.IntegerCacheSize" property.
 * @since Jetty 4.1
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class TypeUtil
{
    private static int intCacheSize=
        Integer.getInteger("org.mortbay.util.TypeUtil.IntegerCacheSize",600).intValue();
    private static Integer[] integerCache = new Integer[intCacheSize];
    private static String[] integerStrCache = new String[intCacheSize];
    private static Integer minusOne = new Integer(-1);
    
    /* ------------------------------------------------------------ */
    /** Convert int to Integer using cache. 
     */
    public static Integer newInteger(int i)
    {
        if (i>=0 && i<intCacheSize)
        {
            if (integerCache[i]==null)
                integerCache[i]=new Integer(i);
            return integerCache[i];
        }
        else if (i==-1)
            return minusOne;
        return new Integer(i);
    }

    
    /* ------------------------------------------------------------ */
    /** Convert int to String using cache. 
     */
    public static String toString(int i)
    {
        if (i>=0 && i<intCacheSize)
        {
            if (integerStrCache[i]==null)
                integerStrCache[i]=Integer.toString(i);
            return integerStrCache[i];
        }
        else if (i==-1)
            return "-1";
        return Integer.toString(i);
    }


    /* ------------------------------------------------------------ */
    /** Parse an int from a substring.
     * Negative numbers are not handled.
     * @param s String
     * @param offset Offset within string
     * @param length Length of integer or -1 for remainder of string
     * @param base base of the integer
     * @exception NumberFormatException 
     */
    public static int parseInt(String s, int offset, int length, int base)
        throws NumberFormatException
    {
        int value=0;

        if (length<0)
            length=s.length()-offset;

        for (int i=0;i<length;i++)
        {
            char c=s.charAt(offset+i);
            
            int digit=c-'0';
            if (digit<0 || digit>=base || digit>=10)
            {
                digit=10+c-'A';
                if (digit<10 || digit>=base)
                    digit=10+c-'a';
            }
            if (digit<0 || digit>=base)
                throw new NumberFormatException(s.substring(offset,offset+length));
            value=value*base+digit;
        }
        return value;
    }

    /* ------------------------------------------------------------ */
    public static byte[] parseBytes(String s, int base)
    {
        byte[] bytes=new byte[s.length()/2];
        for (int i=0;i<s.length();i+=2)
            bytes[i/2]=(byte)TypeUtil.parseInt(s,i,2,base);
        return bytes;
    }

    /* ------------------------------------------------------------ */
    public static String toString(byte[] bytes, int base)
    {
        StringBuffer buf = new StringBuffer();
        for (int i=0;i<bytes.length;i++)
        {
            int bi=0xff&bytes[i];
            int c='0'+(bi/base)%base;
            if (c>'9')
                c= 'a'+(c-'0'-10);
            buf.append((char)c);
            c='0'+bi%base;
            if (c>'9')
                c= 'a'+(c-'0'-10);
            buf.append((char)c);
        }
        return buf.toString();
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param b An ASCII encoded character 0-9 a-f A-F
     * @return The byte value of the character 0-16.
     */
    public static byte convertHexDigit( byte b )
    {
        if ((b >= '0') && (b <= '9')) return (byte)(b - '0');
        if ((b >= 'a') && (b <= 'f')) return (byte)(b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F')) return (byte)(b - 'A' + 10);
        return 0;
    }

    /* ------------------------------------------------------------ */
    public static String toHexString(byte[] b)
    {   
        StringBuffer buf = new StringBuffer();
        for (int i=0;i<b.length;i++)
        {
            int bi=0xff&b[i];
            int c='0'+(bi/16)%16;
            if (c>'9')
                c= 'A'+(c-'0'-10);
            buf.append((char)c);
            c='0'+bi%16;
            if (c>'9')
                c= 'a'+(c-'0'-10);
            buf.append((char)c);
        }
        return buf.toString();
    }
    
    /* ------------------------------------------------------------ */
    public static String toHexString(byte[] b,int offset,int length)
    {   
        StringBuffer buf = new StringBuffer();
        for (int i=offset;i<offset+length;i++)
        {
            int bi=0xff&b[i];
            int c='0'+(bi/16)%16;
            if (c>'9')
                c= 'A'+(c-'0'-10);
            buf.append((char)c);
            c='0'+bi%16;
            if (c>'9')
                c= 'a'+(c-'0'-10);
            buf.append((char)c);
        }
        return buf.toString();
    }
    
    /* ------------------------------------------------------------ */
    public static byte[] fromHexString(String s)
    {   
        if (s.length()%2!=0)
            throw new IllegalArgumentException(s);
        byte[] array = new byte[s.length()/2];
        for (int i=0;i<array.length;i++)
        {
            int b = Integer.parseInt(s.substring(i*2,i*2+2),16);
            array[i]=(byte)(0xff&b);
        }    
        return array;
    }
    
}

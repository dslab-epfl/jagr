// =========================================================================== 
// $Id: UrlEncoded.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
package org.mortbay.util;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

/* ------------------------------------------------------------ */
/** Handles coding of MIME  "x-www-form-urlencoded".
 * This class handles the encoding and decoding for either
 * the query string of a URL or the content of a POST HTTP request.
 *
 * <p><h4>Notes</h4>
 * The hashtable either contains String single values, vectors
 * of String or arrays of Strings.
 *
 * This class is only partially synchronised.  In particular, simple
 * get operations are not protected from concurrent updates.
 *
 * @see java.net.URLEncoder
 * @version $Id: UrlEncoded.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Greg Wilkins (gregw)
 */
public class UrlEncoded extends MultiMap
{
    /* ----------------------------------------------------------------- */
    public UrlEncoded(UrlEncoded url)
    {
        super(url);
    }
    
    /* ----------------------------------------------------------------- */
    public UrlEncoded()
    {
        super(6);
    }
    
    /* ----------------------------------------------------------------- */
    public UrlEncoded(String s)
    {
        super(6);
        decode(s,StringUtil.__ISO_8859_1);
    }
    
    /* ----------------------------------------------------------------- */
    public UrlEncoded(String s, String charset)
    {
        super(6);
        decode(s,charset);
    }
    
    /* ----------------------------------------------------------------- */
    public void decode(String query)
    {
        decodeTo(query,this,StringUtil.__ISO_8859_1);
    }
    
    /* ----------------------------------------------------------------- */
    public void decode(String query,String charset)
    {
        decodeTo(query,this,charset);
    }
    
    /* -------------------------------------------------------------- */
    /** Encode Hashtable with % encoding.
     */
    public String encode()
    {
        return encode(StringUtil.__ISO_8859_1,false);
    }
    
    /* -------------------------------------------------------------- */
    /** Encode Hashtable with % encoding.
     */
    public String encode(String charset)
    {
        return encode(charset,false);
    }
    
    /* -------------------------------------------------------------- */
    /** Encode Hashtable with % encoding.
     * @param equalsForNullValue if True, then an '=' is always used, even
     * for parameters without a value. e.g. "blah?a=&b=&c=".
     */
    public synchronized String encode(String charset, boolean equalsForNullValue)
    {
        if (charset==null)
            charset=StringUtil.__ISO_8859_1;
        
        StringBuffer result = new StringBuffer(128);
        synchronized(result)
        {
            Iterator iter = entrySet().iterator();
            while(iter.hasNext())
            {
                Map.Entry entry = (Map.Entry)iter.next();
                
                String key = entry.getKey().toString();
                LazyList list = (LazyList)entry.getValue();
                int s=LazyList.size(list);
                
                if (s==0)
                {
                    result.append(URLEncoder.encode(key));
                    if(equalsForNullValue)
                        result.append('=');
                }
                else
                {
                    for (int i=0;i<s;i++)
                    {
                        if (i>0)
                            result.append('&');
                        Object val=list.get(i);
                        result.append(encodeString(key,charset));

                        if (val!=null)
                        {
                            String str=val.toString();
                            if (str.length()>0)
                            {
                                result.append('=');
                                result.append(encodeString(str,charset));
                            }
                            else if (equalsForNullValue)
                                result.append('=');
                        }
                        else if (equalsForNullValue)
                            result.append('=');
                    }
                }
                if (iter.hasNext())
                    result.append('&');
            }
            return result.toString();
        }
    }

    /* -------------------------------------------------------------- */
    /* Decoded parameters to Map.
     * @param content the string containing the encoded parameters
     * @param url The dictionary to add the parameters to
     */
    public static void decodeTo(String content,MultiMap map)
    {
        decodeTo(content,map,StringUtil.__ISO_8859_1);
    }
    
    
    /* -------------------------------------------------------------- */
    /* Decoded parameters to Map.
     * @param content the string containing the encoded parameters
     * @param url The dictionary to add the parameters to
     */
    public static void decodeTo(String content,MultiMap map,String charset)
    {
        if (charset==null)
            charset=StringUtil.__ISO_8859_1;
        synchronized(map)
        {
            String token;
            String name;
            String value;

            StringTokenizer tokenizer =
                new StringTokenizer(content, "&", false);

            while ((tokenizer.hasMoreTokens()))
            {
                token = tokenizer.nextToken();
                
                // breaking it at the "=" sign
                int i = token.indexOf('=');
                if (i<0)
                {
                    name=decodeString(token,charset);
                    value="";
                }
                else
                {
                    name=decodeString(token.substring(0,i++),
                                      charset);
                    if (i>=token.length())
                        value="";
                    else
                        value = decodeString(token.substring(i),
                                             charset);
                }

                // Add value to the map
                if (name.length() > 0)
                    map.add(name,value);
            }
        }
    }
    
    /* -------------------------------------------------------------- */
    /** Decode String with % encoding.
     * This method makes the assumption that the majority of calls
     * will need no decoding and uses the 8859 encoding.
     */
    public static String decodeString(String encoded)
    {
        return decodeString(encoded,StringUtil.__ISO_8859_1);
    }
    
    /* -------------------------------------------------------------- */
    /** Decode String with % encoding.
     * This method makes the assumption that the majority of calls
     * will need no decoding.
     */
    public static String decodeString(String encoded,String charset)
    {
        if (charset==null)
            charset=StringUtil.__ISO_8859_1;
        int len=encoded.length();
        byte[] bytes=null;
        int n=0;
        StringBuffer buf=null;
        
        for (int i=0;i<len;i++)
        {
            char c = encoded.charAt(i);
            if (c<0||c>0xff)
                throw new IllegalArgumentException("Not decoded");
            
            if (c=='+')
            {
                if (buf==null)
                {
                    buf=new StringBuffer(len);
                    for (int j=0;j<i;j++)
                        buf.append(encoded.charAt(j));
                }
                if (n>0)
                {
                    try {buf.append(new String(bytes,0,n,charset));}
                    catch(UnsupportedEncodingException e)
                    {buf.append(new String(bytes,0,n));}
                    n=0;
                }        
                buf.append(' ');
            }
            else if (c=='%' && (i+2)<len)
            {
                byte b;
                char cn = encoded.charAt(i+1);
                if (cn>='a' && cn<='z')
                    b=(byte)(10+cn-'a');
                else if (cn>='A' && cn<='Z')
                    b=(byte)(10+cn-'A');
                else
                    b=(byte)(cn-'0');
                cn = encoded.charAt(i+2);
                if (cn>='a' && cn<='z')
                    b=(byte)(b*16+10+cn-'a');
                else if (cn>='A' && cn<='Z')
                    b=(byte)(b*16+10+cn-'A');
                else
                    b=(byte)(b*16+cn-'0');
                
                if (buf==null)
                {
                    buf=new StringBuffer(len);
                    for (int j=0;j<i;j++)
                        buf.append(encoded.charAt(j));
                }
                i+=2;
                if (bytes==null)
                    bytes=new byte[len];
                bytes[n++]=b;
            }
            else if (buf!=null)
            {
                if (n>0)
                {
                    try {buf.append(new String(bytes,0,n,charset));}
                    catch(UnsupportedEncodingException e)
                    {buf.append(new String(bytes,0,n));}
                    n=0;
                }                
                buf.append(c);
            }
        }

        if (buf==null)
            return encoded;

        if (n>0)
        {
            try {buf.append(new String(bytes,0,n,charset));}
            catch(UnsupportedEncodingException e)
            {buf.append(new String(bytes,0,n));}
        }

        return buf.toString();
    }
    
    /* ------------------------------------------------------------ */
    /** Perform URL encoding.
     * Assumes 8859 charset
     * @param string 
     * @return encoded string.
     */
    public static String encodeString(String string)
    {
        return encodeString(string,StringUtil.__ISO_8859_1);
    }
    
    /* ------------------------------------------------------------ */
    /** Perform URL encoding.
     * @param string 
     * @return encoded string.
     */
    public static String encodeString(String string,String charset)
    {
        if (charset==null)
            charset=StringUtil.__ISO_8859_1;
        byte[] bytes=null;
        try
        {
            bytes=string.getBytes(charset);
        }
        catch(UnsupportedEncodingException e)
        {
            Code.warning(e);
            bytes=string.getBytes();
        }
        
        int len=bytes.length;
        byte[] encoded= new byte[bytes.length*3];
        int n=0;
        boolean noEncode=true;
        
        for (int i=0;i<len;i++)
        {
            byte b = bytes[i];
            
            if (b==' ')
            {
                noEncode=false;
                encoded[n++]=(byte)'+';
            }
            else if (b>='a' && b<='z' ||
                     b>='A' && b<='Z' ||
                     b>='0' && b<='9')
            {
                encoded[n++]=b;
            }
            else
            {
                noEncode=false;
                encoded[n++]=(byte)'%';
                byte nibble= (byte) ((b&0xf0)>>4);
                if (nibble>=10)
                    encoded[n++]=(byte)('A'+nibble-10);
                else
                    encoded[n++]=(byte)('0'+nibble);
                nibble= (byte) (b&0xf);
                if (nibble>=10)
                    encoded[n++]=(byte)('A'+nibble-10);
                else
                    encoded[n++]=(byte)('0'+nibble);
            }
        }

        if (noEncode)
            return string;
        
        try
        {    
            return new String(encoded,0,n,charset);
        }
        catch(UnsupportedEncodingException e)
        {
            Code.warning(e);
            return new String(encoded,0,n);
        }
    }


    /* ------------------------------------------------------------ */
    /** 
     */
    public Object clone()
    {
        return new UrlEncoded(this);
    }
}

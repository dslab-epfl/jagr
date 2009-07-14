// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: AJP13Packet.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.http.ajp;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;
import org.mortbay.util.Code;
import org.mortbay.util.StringUtil;
import org.mortbay.util.ByteArrayISO8859Writer;


/* ------------------------------------------------------------ */
/**
 *
 * @version 1.0 Thu Jun 13 2002
 * @author Greg Wilkins (gregw)
 */
public class AJP13Packet
{
    /* ------------------------------------------------------------ */
    public static final int __HDR_SIZE=4;

    public static final byte
        __FORWARD_REQUEST=2,
        __SHUTDOWN=7,
        __SEND_BODY_CHUNK=3,
        __SEND_HEADERS=4,
        __END_RESPONSE=5,
        __GET_BODY_CHUNK=6;
    
    public static final String[] __method=
    {
        "ERROR",
        "OPTIONS",
        "GET",
        "HEAD",
        "POST",
        "PUT",
        "DELETE",
        "TRACE",
        "PROPFIND",
        "PROPPATCH",
        "MKCOL",
        "COPY",
        "MOVE",
        "LOCK",
        "UNLOCK",
        "ACL",
        "REPORT",
        "VERSION-CONTROL",
        "CHECKIN",
        "CHECKOUT",
        "UNCHECKOUT",
        "SEARCH"
    };
    
    public static final String[] __header=
    {
        "ERROR",
        "accept",
        "accept-charset",
        "accept-encoding",
        "accept-language",
        "authorization",
        "connection",
        "content-type",
        "content-length",
        "cookie",
        "cookie2",
        "host",
        "pragma",
        "referer",
        "user-agent"
    };
    
    private static final HashMap __headerMap = new HashMap();
    static
    {
        for (int  i=1;i<__header.length;i++)
            __headerMap.put(__header[i],new Integer(0xA000+i));
    }
    
    
    /* ------------------------------------------------------------ */
    private byte[] _buf;
    private int _bytes;
    private int _pos;
    private ByteArrayISO8859Writer _byteWriter;
    
    /* ------------------------------------------------------------ */
    public AJP13Packet(int size)
    {
        _buf=new byte[size];
    }

    /* ------------------------------------------------------------ */
    public void reset()
    {
        _bytes=0;
        _pos=0;
    }

    /* ------------------------------------------------------------ */
    public void resetData()
    {
        _bytes=__HDR_SIZE;
        _pos=0;
    }

    /* ------------------------------------------------------------ */
    public int getMark()
    {
        return _bytes;
    }
    
    /* ------------------------------------------------------------ */
    public int getBufferSize()
    {
        return _buf.length;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @return Bytes of data remaining 
     */
    public int unconsumedData()
    {
        return _bytes-_pos;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return Bytes of capacity remaining 
     */
    public int unconsumedCapacity()
    {
        return _buf.length-_bytes;
    }
    
    /* ------------------------------------------------------------ */
    public void read(InputStream in)
        throws IOException
    {
        _bytes=0;
        _pos=0;

        // read header
        do
        {
            int l=in.read(_buf,_bytes,__HDR_SIZE-_bytes);
            if (l<0)
                throw new IOException("EOF");
            _bytes+=l;
        }
        while (_bytes<__HDR_SIZE);

        // decode header
        int magic=getInt();
        if (magic!=0x1234)
            throw new IOException("Bad JSP13 rcv packet:"+magic+" "+this);
        int len=getInt();

        // read packet
        do
        {
            int l=in.read(_buf,_bytes,len+__HDR_SIZE-_bytes);
            if (l<0)
                throw new IOException("EOF");
            _bytes+=l;
        }
        while (_bytes<len);
        
        if (Code.verbose(99))
            Code.debug("AJP13 rcv: "+this.toString(64));

	// System.err.println(Thread.currentThread()+" AJP13 rcv>>>> "+this.toString(32));
    }

    /* ------------------------------------------------------------ */
    public void write(OutputStream out)
        throws IOException
    {
        if (Code.verbose(99))
            Code.debug("AJP13 snd: "+this.toString(64));
	// System.err.println(Thread.currentThread()+" AJP13 snd<<<< "+this.toString(32));
        out.write(_buf,0,_bytes);
    }
    
    /* ------------------------------------------------------------ */
    public byte getByte()
    {
        return _buf[_pos++];
    }
    
    /* ------------------------------------------------------------ */
    public int getBytes(byte[] buf,int offset,int length)
    {
        if (length>unconsumedData())
            length=unconsumedData();
        System.arraycopy(_buf,_pos,buf,offset,length);
        _pos+=length;
        return length;
    }
    
    /* ------------------------------------------------------------ */
    public boolean getBoolean()
    {
        return _buf[_pos++]!=0;
    }
    
    /* ------------------------------------------------------------ */
    public int getInt()
    {
	int i =  _buf[_pos++] & 0xFF;
        i=(i<<8)+(_buf[_pos++] & 0xFF);
        return i;
    }
    
    /* ------------------------------------------------------------ */
    public String getString()
    {
        int len=getInt();
        if (len==0xFFFF)
            return null;
        try
        {
            String s=new String(_buf,_pos,len,StringUtil.__ISO_8859_1);
            _pos+=len+1;
            return s;
        }
        catch (UnsupportedEncodingException e)
        {
            Code.fail(e);
            return null;
        }
    }
    
    /* ------------------------------------------------------------ */
    public String getMethod()
    {
        return __method[getByte()];
    }
    
    /* ------------------------------------------------------------ */
    public String getHeader()
    {
        if ((0xFF&_buf[_pos])==0xA0)
        {
            _pos++;
            return __header[_buf[_pos++]];
        }
        return getString();
    }
    
    /* ------------------------------------------------------------ */
    public void addByte(byte b)
    {
        _buf[_bytes++]=b;
    }
    
    /* ------------------------------------------------------------ */
    public int addBytes(byte[] buf,int offset,int length)
    {
        if (length>unconsumedCapacity())
            length=unconsumedCapacity();
        System.arraycopy(buf,offset,_buf,_bytes,length);
        _bytes+=length;
        return length;
    }
    
    /* ------------------------------------------------------------ */
    public void addBoolean(boolean b)
    {
        _buf[_bytes++]=(byte)(b?1:0);
    }
    
    /* ------------------------------------------------------------ */
    public void addInt(int i)
    {
        _buf[_bytes++]=(byte)((i>>8) & 0xFF);
        _buf[_bytes++]=(byte)(i & 0xFF);
    }
    
    /* ------------------------------------------------------------ */
    public void setInt(int mark, int i)
    {
        _buf[mark]=(byte)((i>>8) & 0xFF);
        _buf[mark+1]=(byte)(i & 0xFF);
    }
    
    /* ------------------------------------------------------------ */
    public void addString(String s)
        throws IOException
    {
        if (s==null)
        {
            addInt(0xFFFF);
            return;
        }

        if (_byteWriter==null)
            _byteWriter=new ByteArrayISO8859Writer(_buf);
        
        int p=_bytes+2;
        _byteWriter.setLength(p);
        _byteWriter.write(s);
        int l=_byteWriter.length()-p;

        addInt(l);
        _bytes+=l;
        _buf[_bytes++]=(byte)0;
    }

    /* ------------------------------------------------------------ */
    public void addHeader(String s)
        throws IOException
    {
        Integer h = (Integer)__headerMap.get(s);
        if (h!=null)
            addInt(h.intValue());
        else
            addString(s);
    }
    
    /* ------------------------------------------------------------ */
    public int getDataSize()
    {
        return _bytes-__HDR_SIZE;
    }
    
    /* ------------------------------------------------------------ */
    public void setDataSize()
    {
        int s = _bytes-__HDR_SIZE;
        _buf[2]=(byte)((s>>8) & 0xFF);
        _buf[3]=(byte)(s & 0xFF);
        if (_buf[4]==__SEND_BODY_CHUNK)
        {
            s=s-3;
            _buf[5]=(byte)((s>>8) & 0xFF);
            _buf[6]=(byte)(s & 0xFF);
        }
    }
    
    /* ------------------------------------------------------------ */
    public String toString()
    {
        return toString(-1);
    }
    
    /* ------------------------------------------------------------ */
    public String toString(int max)
    {
        StringBuffer b=new StringBuffer();
        StringBuffer a=new StringBuffer();

        b.append(_bytes);
        b.append('/');
        b.append(_buf.length);
        b.append('[');
        b.append(_pos);
        b.append("]: ");

        switch(_buf[__HDR_SIZE])
        {
          case __FORWARD_REQUEST: b.append("FORWARD_REQUEST:");break;
          case __SHUTDOWN: b.append("SHUTDOWN:");break;
          case __SEND_BODY_CHUNK: b.append("SEND_BODY_CHUNK:");break;
          case __SEND_HEADERS: b.append("SEND_HEADERS:");break;
          case __END_RESPONSE: b.append("END_RESPONSE:");break;
          case __GET_BODY_CHUNK: b.append("GET_BODY_CHUNK:");break;
        }
        b.append("\n");
        
        for (int i=0;i<_bytes;i++)
        {
            char c=(char)((int)_buf[i]&0xFF);
            if (c<16)
                b.append('0');
            b.append(Integer.toString(c,16));
            
            if (Character.isLetterOrDigit(c))
                a.append(c);
            else
                a.append('.');
            
            if (i%32==31 || i==(_bytes-1))
            {
                b.append(" : ");
                b.append(a.toString());
                a.setLength(0);
                b.append("\n");
                if (max>0 && max<i)
                    break;
            }
            else
                b.append(",");
        }   
        
        return b.toString();
    }    
}

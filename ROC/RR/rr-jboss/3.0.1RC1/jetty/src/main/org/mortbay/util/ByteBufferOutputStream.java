// ===========================================================================
// Copyright (c) 2001 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: ByteBufferOutputStream.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ---------------------------------------------------------------------------

package org.mortbay.util;
import java.io.IOException;
import java.io.OutputStream;

/* ------------------------------------------------------------ */
/** ByteBuffer OutputStream.
 * This stream is similar to the java.io.ByteArrayOutputStream,
 * except that it maintains a reserve of bytes at the start of the
 * buffer and allows efficient prepending of data.
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class ByteBufferOutputStream extends OutputStream
{
    private byte[] _buf;
    private int _start;
    private int _end;
    private int _reserve;
    private boolean _resized;
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public ByteBufferOutputStream(){this(4096,512);}
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param capacity Buffer capacity
     */
    public ByteBufferOutputStream(int capacity)
    {
        this(capacity,512);
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param capacity Buffer capacity.
     * @param fullAt The size of the buffer.
     * @param reserve The reserve of byte for prepending
     */
    public ByteBufferOutputStream(int capacity,int reserve)
    {
        _buf=ByteArrayPool.getByteArray(capacity);
        _reserve=reserve;
        _start=reserve;
        _end=reserve;
    }

    /* ------------------------------------------------------------ */
    public int size()
    {
        return _end-_start;
    }
    
    /* ------------------------------------------------------------ */
    public int getCapacity()
    {
        return _buf.length-_start;
    }
    
    /* ------------------------------------------------------------ */
    public int getSpareCapacity()
    {
        return _buf.length-_end;
    }
    
    /* ------------------------------------------------------------ */
    public boolean canFit(int n)
    {
        return _end+n<_buf.length;
    }
    
    /* ------------------------------------------------------------ */
    public void writeTo(OutputStream out)
        throws IOException
    {
        out.write(_buf,_start,_end-_start);
    }
    

    /* ------------------------------------------------------------ */
    public void write(int b)
    {
        ensureCapacity(1);
        _buf[_end++]=(byte)b;
    }
    
    /* ------------------------------------------------------------ */
    public void write(byte[] b)
    {
        ensureCapacity(b.length);
        System.arraycopy(b,0,_buf,_end,b.length);
        _end+=b.length;
    }
    
    /* ------------------------------------------------------------ */
    public void write(byte[] b,int offset, int length)
    {
        ensureCapacity(length);
        System.arraycopy(b,offset,_buf,_end,length);
        _end+=length;
    }
    
    /* ------------------------------------------------------------ */
    /** Write byte to start of the buffer.
     * @param b 
     */
    public void prewrite(int b)
    {
        ensureReserve(1);
        _buf[--_start]=(byte)b;
    }
    
    /* ------------------------------------------------------------ */
    /** Write byte array to start of the buffer.
     * @param b 
     */
    public void prewrite(byte[] b)
    {
        ensureReserve(b.length);
        System.arraycopy(b,0,_buf,_start-b.length,b.length);
        _start-=b.length;
    }
    
    /* ------------------------------------------------------------ */
    /** Write byte range to start of the buffer.
     * @param b 
     * @param offset 
     * @param length 
     */
    public void prewrite(byte[] b,int offset, int length)
    {
        ensureReserve(length);
        System.arraycopy(b,offset,_buf,_start-length,length);
        _start-=length;
    }

    /* ------------------------------------------------------------ */
    public void flush()
    {}
    
    /* ------------------------------------------------------------ */
    public void reset()
    {
        _end=_reserve;
        _start=_reserve;
    }
    
    /* ------------------------------------------------------------ */
    public void reset(int reserve)
    {
        _reserve=reserve;
        _end=_reserve;
        _start=_reserve;
    }

    /* ------------------------------------------------------------ */
    public void close()
    {}
    
    /* ------------------------------------------------------------ */
    public void destroy()
    {
        if (!_resized)
            ByteArrayPool.returnByteArray(_buf);
    }

    /* ------------------------------------------------------------ */
    public void ensureReserve(int n)
    {
        if (n>_start)
        {
            if (Code.debug())Code.debug("Reserve: "+n+">"+_start);
            if ((_end+n)<_buf.length)
            {
                if (Code.debug())Code.debug("Shift reserve: "+_end+"+"+n+"<"+_buf.length);
                System.arraycopy(_buf,_start,_buf,n,_end-_start);
                _end=_end+n-_start;
                _start=n;
            }
            else
            {    
                if (Code.debug())Code.debug("New reserve: "+_end+"+"+n+">="+_buf.length);
                byte[] buf = new byte[_buf.length+n-_start];
                System.arraycopy(_buf,_start,buf,n,_end-_start);
                _end=n+_end-_start;
                _start=n;
                _buf=buf;
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    public void ensureCapacity(int n)
    {
        if ((_end+n)>_buf.length)
        {
            int bl = ((_end+n+4095)/4096)*4096;
            byte[] buf = new byte[bl];
            if (Code.debug())Code.debug("New buf for ensure: "+_end+"+"+n+">"+_buf.length+" --> "+buf.length);
            System.arraycopy(_buf,_start,buf,_start,_end-_start);
            if (!_resized)
                ByteArrayPool.returnByteArray(_buf);
            _buf=buf;
            _resized=true;
        }
    }
}
    
    

// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: AJP13InputStream.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.http.ajp;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class AJP13InputStream extends InputStream
{   
    /* ------------------------------------------------------------ */
    private AJP13Packet _packet;
    private AJP13Packet _getBodyChunk;
    private InputStream _in;
    private OutputStream _out;
    private boolean _gotFirst=false;
    private boolean _closed;
    
    /* ------------------------------------------------------------ */
    AJP13InputStream(InputStream in, OutputStream out, int bufferSize)
    {
        _in=in;
        _out=out;
        _packet=new AJP13Packet(bufferSize);
        _getBodyChunk=new AJP13Packet(8);
        _getBodyChunk.addByte((byte)'A');
        _getBodyChunk.addByte((byte)'B');
        _getBodyChunk.addInt(3);
        _getBodyChunk.addByte(AJP13Packet.__GET_BODY_CHUNK);
        _getBodyChunk.addInt(bufferSize);
    }

    /* ------------------------------------------------------------ */
    public void recycle()
    {
        _gotFirst=false;
        _closed=false;
        _packet.reset();
    }
    

    /* ------------------------------------------------------------ */
    public int available()
        throws IOException
    {
        if (_closed)
            return 0;
        if (_packet.unconsumedData()==0)
            fillPacket();
        return _packet.unconsumedData();
    }

    /* ------------------------------------------------------------ */
    public void close()
        throws IOException
    {
        _closed=true;
    }

    /* ------------------------------------------------------------ */
    public void mark(int readLimit)
    {}

    /* ------------------------------------------------------------ */
    public boolean markSupported()
    {
        return false;
    }

    /* ------------------------------------------------------------ */
    public void reset()
        throws IOException
    {
        throw new IOException("reset() not supported");
    }

    /* ------------------------------------------------------------ */
    public int read()
        throws IOException
    {
        if (_closed)
            return -1;
        
        if (_packet.unconsumedData()<=0)
        {
            fillPacket();
            if (_packet.unconsumedData()<=0)
            {
                _closed=true;
                return -1;
            }
        }
        return _packet.getByte();
    }

    /* ------------------------------------------------------------ */
    public int read(byte[] b, int off, int len)
        throws IOException
    {
        if (_closed)
            return -1;
        
        if (_packet.unconsumedData()==0)
        {
            fillPacket();
            if (_packet.unconsumedData()==0)
            {
                _closed=true;
                return -1;
            }
        }
        
        return _packet.getBytes(b,off,len);
    }

    /* ------------------------------------------------------------ */
    /** 
     * @return The next packet from the stream. The packet is recycled and is
     * only valid until the next call to nextPacket or read().
     * @exception IOException 
     */
    public AJP13Packet nextPacket()
        throws IOException
    {
        _packet.read(_in);
        return _packet;
    }
    
    /* ------------------------------------------------------------ */
    private void fillPacket()
        throws IOException
    {
        if (_closed)
            return;
        
        if (_gotFirst)
            _getBodyChunk.write(_out);
        _gotFirst=true;

        // read packet
        _packet.read(_in);

        if (_packet.unconsumedData()<=0)
            _closed=true;
        else if(_packet.getInt()>_packet.getBufferSize())
            throw new IOException("AJP Protocol error");
    }
    
    
    
    /* ------------------------------------------------------------ */
    public long skip(long n)
        throws IOException
    {
        if (_closed)
            return -1;
        
        for (int i=0;i<n;i++)
            if (read()<0)
                return i==0?-1:i;
        return n;
    }
}

// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: AJP13OutputStream.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.http.ajp;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.mortbay.util.Code;


public class AJP13OutputStream extends OutputStream
{
    private AJP13Packet _packet;
    private OutputStream _out;
    private byte[] _byte = {(byte)0};
    private boolean _closed;

    /* ------------------------------------------------------------ */
    AJP13OutputStream(OutputStream out,int bufferSize)
    {
        _out=out;
        _packet=new AJP13Packet(bufferSize);
        _packet.addByte((byte)'A');
        _packet.addByte((byte)'B');
        _packet.addInt(0);
        _packet.addByte(AJP13Packet.__SEND_BODY_CHUNK);
        _packet.addInt(0);
    }

    /* ------------------------------------------------------------ */
    public void recycle()
    {
        _packet.resetData();
        _packet.addByte(AJP13Packet.__SEND_BODY_CHUNK);
        _packet.addInt(0);
        _closed=false;
    }
    
    /* ------------------------------------------------------------ */
    public void write(AJP13Packet packet)
        throws IOException
    {
        packet.write(_out);
    }
    
    /* ------------------------------------------------------------ */
    public void write(int b) throws IOException
    {
        if (_closed)
            return;
        _byte[0]=(byte)(b & 0xFF);
        write(_byte,0,1);
    }
    
    /* ------------------------------------------------------------ */
    public void write(byte b[]) throws IOException
    {
        if (_closed)
            return;
        write(b,0,b.length);
    }
    
    /* ------------------------------------------------------------ */
    public void write(byte b[], int off, int len) throws IOException
    {
        if (_closed)
            return;

        int l=_packet.addBytes(b,off,len);
        while(l<len)
        {
            len-=l;
            off+=l;
            _packet.setDataSize();
            _packet.write(_out);
            _packet.resetData();
            _packet.addByte(AJP13Packet.__SEND_BODY_CHUNK);
            _packet.addInt(0);
            l=_packet.addBytes(b,off,len);
        }   
    }
    
    /* ------------------------------------------------------------ */
    public void flush()
        throws IOException
    {
        if (_closed)
            return;
        _packet.setDataSize();
        if (_packet.getDataSize()>3)
        {
            _packet.write(_out);
            _packet.resetData();
            _packet.addByte(AJP13Packet.__SEND_BODY_CHUNK);
            _packet.addInt(0);
        }
    }
    
    /* ------------------------------------------------------------ */
    public void close()
        throws IOException
    {
        if (!_closed)
            flush();
        _closed=true;
    }

    /* ------------------------------------------------------------ */
    public void end(boolean persistent)
        throws IOException
    {
        _packet.resetData();
        _packet.addByte(AJP13Packet.__END_RESPONSE);
        _packet.addBoolean(persistent);
        _packet.setDataSize();
        _packet.write(_out);
        
        _packet.resetData();
        _packet.addByte(AJP13Packet.__SEND_BODY_CHUNK);
        _packet.addInt(0);
    }
    
}

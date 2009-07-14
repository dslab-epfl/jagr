// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: ChunkableOutputStream.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ---------------------------------------------------------------------------

package org.mortbay.http;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import org.mortbay.util.ByteArrayISO8859Writer;
import org.mortbay.util.ByteBufferOutputStream;
import org.mortbay.util.Code;
import org.mortbay.util.IO;

/* ---------------------------------------------------------------- */
/** HTTP Chunkable OutputStream.
 * Acts as a BufferedOutputStream until setChunking(true) is called.
 * Once chunking is enabled, the raw stream is chunk encoded as per RFC2616.
 *
 * Implements the following HTTP and Servlet features: <UL>
 * <LI>Filters for content and transfer encodings.
 * <LI>Allows output to be reset if not committed (buffer never flushed).
 * <LI>Notification of significant output events for filter triggering,
 *     header flushing, etc.
 * </UL>
 *
 * This class is not synchronized and should be synchronized
 * explicitly if an instance is used by multiple threads.
 *
 * @version $Id: ChunkableOutputStream.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Greg Wilkins
*/
public class ChunkableOutputStream extends FilterOutputStream
{
    /* ------------------------------------------------------------ */
    final static String
        __CRLF      = "\015\012";
    final static byte[]
        __CRLF_B    = {(byte)'\015',(byte)'\012'};
    final static byte[]
        __CHUNK_EOF_B ={(byte)'0',(byte)'\015',(byte)'\012'};

    final static int __BUFFER_SIZE=4096;
    final static int __FIRST_RESERVE=512;
    final static int __CHUNK_RESERVE=8;
    
    public final static Class[] __filterArg = {java.io.OutputStream.class};
    
    /* ------------------------------------------------------------ */
    OutputStream _realOut;
    ByteBufferOutputStream _buffer;
    boolean _chunking;
    HttpFields _trailer;
    boolean _committed;
    boolean _written;
    ArrayList _observers;
    ByteArrayISO8859Writer _rawWriter;
    boolean _nulled=false;
    int _bytes;
    int _headerReserve;
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param outputStream The outputStream to buffer or chunk to.
     */
    public ChunkableOutputStream(OutputStream outputStream)
    {
        this (outputStream,__BUFFER_SIZE,__FIRST_RESERVE);
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param outputStream The outputStream to buffer or chunk to.
     */
    public ChunkableOutputStream(OutputStream outputStream, int bufferSize)
    {
        this (outputStream,bufferSize,__FIRST_RESERVE);
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param outputStream The outputStream to buffer or chunk to.
     */
    public ChunkableOutputStream(OutputStream outputStream,
                                 int bufferSize,
                                 int headerReserve)
    {
        super(new ByteBufferOutputStream(bufferSize,headerReserve));
        _buffer=(ByteBufferOutputStream)out;
        _realOut=outputStream;
        _committed=false;
        _written=false;
        _headerReserve=headerReserve;
    }

    /* ------------------------------------------------------------ */
    /** Get the raw stream.
     * A stream without filters or chunking is returned.
     * @return Raw OutputStream.
     */
    public OutputStream getRawStream()
    {
        return _realOut;
    }
    
    /* ------------------------------------------------------------ */
    /** Get Writer for the raw stream.
     * A writer without filters or chunking is returned, which uses
     * the 8859-1 encoding. The converted bytes from this writer will be
     * writen to the rawStream when writeRawWriter() is called.
     * These methods allow Character encoded data to be mixed with
     * raw data on the same stream without excessive buffering or flushes.
     * @return Raw Writer
     */
    public Writer getRawWriter()
    {
        if (_rawWriter==null)
            _rawWriter=new ByteArrayISO8859Writer(2048);
        return _rawWriter;
    }
    
    /* ------------------------------------------------------------ */
    /** Get Filter OutputStream.
     * Get the current top of the OutputStream filter stack
     * @return OutputStream.
     */
    public OutputStream getFilterStream()
    {
        return out;
    }
    
    /* ------------------------------------------------------------ */
    /** Set Filter OutputStream.
     * Set output filter stream, which should be constructed to wrap
     * the stream returned from get FilterStream.
     */
    public void setFilterStream(OutputStream filter)
    {
        out=filter;
    }
    
    /* ------------------------------------------------------------ */
    /** Has any data been written to the stream.
     * @return True if write has been called.
     */
    public boolean isWritten()
    {
        return _written;
    }
    
    /* ------------------------------------------------------------ */
    /** Has any data been sent from this stream.
     * @return True if buffer has been flushed to destination.
     */
    public boolean isCommitted()
    {
        return _committed;
    }
        
    /* ------------------------------------------------------------ */
    /** Get the output buffer capacity.
     * @return Buffer capacity in bytes.
     */
    public int getBufferCapacity()
    {
        return _buffer.getCapacity();
    }
    
    /* ------------------------------------------------------------ */
    /** Set the output buffer capacity.
     * Note that this is the minimal buffer capacity and that installed
     * filters may perform their own buffering and are likely to change
     * the size of the output.
     * @param capacity Minimum buffer capacity in bytes
     * @exception IllegalStateException If output has been written.
     */
    public void setBufferCapacity(int capacity)
        throws IllegalStateException
    {
        if (capacity<=getBufferCapacity())
            return;
        
        if (_buffer.size()>0)
            throw new IllegalStateException("Buffer is not empty");
        if (_committed)
            throw new IllegalStateException("Output committed");
        if (out!=_buffer)
            throw new IllegalStateException("Filter(s) installed");

        _buffer.ensureCapacity(capacity+_headerReserve);
    }

    /* ------------------------------------------------------------ */
    public int getBytesWritten()
    {
        return _bytes;
    }
    
    /* ------------------------------------------------------------ */
    /** Reset Buffered output.
     * If no data has been committed, the buffer output is discarded and
     * the filters may be reinitialized.
     * @exception IllegalStateException
     * @exception Problem with observer notification.
     */
    public void resetBuffer()
        throws IllegalStateException
    {
        if (_committed)
            throw new IllegalStateException("Output committed");

        // Shutdown filters without observation
        ArrayList save_observers=_observers;
        _observers=null;
        try
        {
            out.flush();
            out.close();
        }
        catch(Exception e)
        {
            Code.ignore(e);
        }
        finally
        {
            _observers=save_observers;
        }

        // discard current buffer and set it to output
        _buffer.reset(_headerReserve);
	_bytes=0;
        out=_buffer;
        _written=false;
        _committed=false;
        try
        {
            notify(OutputObserver.__RESET_BUFFER);
        }
        catch(IOException e)
        {
            Code.ignore(e);
        }
    }

    /* ------------------------------------------------------------ */
    /** Add an Output Observer.
     * Output Observers get notified of significant events on the
     * output stream. Observers are called in the reverse order they
     * were added.
     * They are removed when the stream is closed.
     * @param observer The observer. 
     */
    public void addObserver(OutputObserver observer)
    {
        if (_observers==null)
            _observers=new ArrayList(4);
        _observers.add(observer);
        _observers.add(null);
    }
    
    /* ------------------------------------------------------------ */
    /** Add an Output Observer.
     * Output Observers get notified of significant events on the
     * output stream. Observers are called in the reverse order they
     * were added.
     * They are removed when the stream is closed.
     * @param observer The observer. 
     * @param data Data to be passed wit notify calls. 
     */
    public void addObserver(OutputObserver observer, Object data)
    {
        if (_observers==null)
            _observers=new ArrayList(4);
        _observers.add(observer);
        _observers.add(data);
    }
    
    
    /* ------------------------------------------------------------ */
    /** Null the output.
     * All output written is discarded until the stream is reset. Used
     * for HEAD requests.
     */
    public void nullOutput()
        throws IOException
    {
        _nulled=true;
    }
    
    /* ------------------------------------------------------------ */
    /** is the output Nulled?
     */
    public boolean isNullOutput()
        throws IOException
    {
        return _nulled;
    }
    
    /* ------------------------------------------------------------ */
    /** Set chunking mode.
     */
    public void setChunking()
    {
        _chunking=true;
    }
    
    /* ------------------------------------------------------------ */
    /** Reset the stream.
     * Turn disable all filters.
     * @exception IllegalStateException The stream cannot be
     * reset if chunking is enabled.
     */
    public void resetStream()
        throws IOException, IllegalStateException
    {
        if (isChunking())
            throw new IllegalStateException("Chunking");
        
        _trailer=null;
        _committed=false;
        _written=false;
        _buffer.reset(_headerReserve);
        
        out=_buffer;    
        _nulled=false;
        _bytes=0;

        if (_rawWriter!=null)
            _rawWriter.reset();

        if (_observers!=null)
            _observers.clear();
    }

    /* ------------------------------------------------------------ */
    public void destroy()
    {
        if (_buffer!=null)
            _buffer.destroy();
    }
    
    /* ------------------------------------------------------------ */
    /** Get chunking mode 
     */
    public boolean isChunking()
    {
        return _chunking;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the trailer to send with a chunked close.
     * @param trailer 
     */
    public void setTrailer(HttpFields trailer)
    {
        if (!isChunking())
            throw new IllegalStateException("Not Chunking");
        _trailer=trailer;
    }
    
    /* ------------------------------------------------------------ */
    public void write(int b) throws IOException
    {
	if (out==null)
	    throw new IOException("closed");

        if (!_written)
        {
            _written=true;
            notify(OutputObserver.__FIRST_WRITE);
        }
        
        if (!_buffer.canFit(_chunking?__CHUNK_RESERVE:1))
            flush();
        
        _bytes++;
        out.write(b);
    }

    /* ------------------------------------------------------------ */
    public void write(byte b[]) throws IOException
    {
        write(b,0,b.length);
    }

    /* ------------------------------------------------------------ */
    public void write(byte b[], int off, int len) throws IOException
    {        
	if (out==null)
	    throw new IOException("closed");

        if (!_written)
        {
            _written=true;
            notify(OutputObserver.__FIRST_WRITE);
        }

        int capacity=_buffer.getSpareCapacity();
        if (_chunking)
            capacity=capacity-__CHUNK_RESERVE;

        while (len>capacity)
        {
            _bytes+=capacity;
            out.write(b,off,capacity);
            off+=capacity;
            len-=capacity;
            flush(false);
            capacity=_buffer.getSpareCapacity();
            if (_chunking)
                capacity=capacity-__CHUNK_RESERVE;
        }
        
        _bytes+=len;
        out.write(b,off,len);
    }

    /* ------------------------------------------------------------ */
    public void flush() throws IOException
    {
        flush(false);
    }

    /* ------------------------------------------------------------ */
    public void commit()
        throws IOException
    {
        _committed=true;
        notify(OutputObserver.__COMMITING);
    }
    
    /* ------------------------------------------------------------ */
    /** Flush.
     * @param complete If true, filters are closed, chunking ended and
     * trailers written. 
     * @exception IOException 
     */
    public void flush(boolean complete)
        throws IOException
    {        
        // Flush filters
        if (out!=null)
        {
            out.flush();
            if (complete)
            {
                if (out!=_buffer)
                    out.close();
                out=null;
            }
        }
        
        // Save non-raw size
        int size=_buffer.size();
        
        // Do we need to commit?
        if (!_committed && (size>0 || (_rawWriter!=null && _rawWriter.length()>0)))
        {
            commit();            
            if (out!=null)
                out.flush();
            size=_buffer.size();
        }

        try
        {
            if (_nulled)
            {
                // Just write the contents of the rawWriter
                if (_rawWriter!=null)
                    _rawWriter.writeTo(_realOut);
            }
            else
            {
                // Handle chunking
                if (_chunking)
                {
                    Writer writer=getRawWriter();
                    if (size>0)
                    {
                        writer.write(Integer.toString(size,16));
                        writer.write(__CRLF);
                        writer.flush();
                        _buffer.write(__CRLF_B);
                    }

                    if (complete)
                    {
                        _buffer.write(__CHUNK_EOF_B);
                        if (_trailer==null)
                            _buffer.write(__CRLF_B);
                    }
                }
                
                // Pre write the raw writer to the buffer
                if (_rawWriter!=null && _rawWriter.length()>0)
                    _buffer.prewrite(_rawWriter.getBuf(),0,_rawWriter.length());
                
                
                // Handle any trailers
                if (_trailer!=null && complete)
                {
                    Writer writer=getRawWriter();
                    _rawWriter.reset();
                    _trailer.write(writer);
                    _rawWriter.writeTo(_buffer);
                }
                
                // Write the buffer
                if (_buffer.size()>0)
                {
                    _buffer.writeTo(_realOut);
                    _buffer.reset(_chunking?__CHUNK_RESERVE:0);
                }
            }
            _realOut.flush();
        }
        finally
        {
            if (_rawWriter!=null)
                _rawWriter.reset();

            if (complete)
            {
                _buffer.reset(_headerReserve);
                _chunking=false;
            }
            else
                _buffer.reset();
        }
    }

    /* ------------------------------------------------------------ */
    /** Close the stream.
     * @exception IOException 
     */
    public void close()
        throws IOException
    {
        // Are we already closed?
        if (out==null)
            return;

        // Close
        try {
            notify(OutputObserver.__CLOSING);
            flush(true);
            out=null;
            notify(OutputObserver.__CLOSED);
        }
        catch (IOException e)
        {
            Code.ignore(e);
        }
    }

    /* ------------------------------------------------------------ */
    /* Notify observers of action.
     * @see OutputObserver
     * @param action the action.
     */
    private void notify(int action)
        throws IOException
    {
        if (_observers!=null)
            for (int i=_observers.size();i-->0;)
            {
                Object data=_observers.get(i--);
                ((OutputObserver)_observers.get(i)).outputNotify(this,action,data);
            }
    }

    /* ------------------------------------------------------------ */
    public void write(InputStream in, int len)
        throws IOException
    {
        IO.copy(in,this,len);
    }
}

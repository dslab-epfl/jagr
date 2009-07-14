// ========================================================================
// Copyright (c) 2000 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: NCSARequestLog.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.http;

import org.mortbay.util.Code;
import java.util.TimeZone;
import org.mortbay.util.RolloverFileOutputStream;
import org.mortbay.util.DateCache;
import org.mortbay.util.ByteArrayISO8859Writer;
import org.mortbay.util.StringUtil;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;


/* ------------------------------------------------------------ */
/** NCSA HTTP Request Log.
 * NCSA common or NCSA extended (combined) request log.
 * @version $Id: NCSARequestLog.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Tony Thompson
 * @author Greg Wilkins
 */
public class NCSARequestLog implements RequestLog
{
    private String _filename;
    private boolean _extended;
    private boolean _append;
    private boolean _buffered;
    private int _retainDays;
    private boolean _closeOut;
    private String _logDateFormat="dd/MMM/yyyy:HH:mm:ss ZZZ";
    private Locale _logLocale=Locale.US;
    private String _logTimeZone=TimeZone.getDefault().getID();
    
    private transient OutputStream _out;
    private transient OutputStream _fileOut;
    private transient DateCache _logDateCache;
    private transient ByteArrayISO8859Writer _buf;
    
    /* ------------------------------------------------------------ */
    /** Constructor.
     * @exception IOException
     */
    public NCSARequestLog()
        throws IOException
    {
        this(null);
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param filename Filename, which can be in
     * rolloverFileOutputStream format
     * @see org.mortbay.util.RolloverFileOutputStream
     * @exception IOException 
     */
    public NCSARequestLog(String filename)
        throws IOException
    {
        _extended=true;
        _append=true;
        _retainDays=31;
        setFilename(filename);
        _buffered=(filename!=null);
    }

    /* ------------------------------------------------------------ */
    public void setFilename(String filename)
    {
        if (filename!=null)
        {
            filename=filename.trim();
            if (filename.length()==0)
                filename=null;
        }
        _filename=filename;        
    }

    /* ------------------------------------------------------------ */
    public String getFilename()
    {
        return _filename;
    }

    /* ------------------------------------------------------------ */
    public boolean isBuffered()
    {
        return _buffered;
    }
    
    /* ------------------------------------------------------------ */
    public void setBuffered(boolean buffered)
    {
        _buffered = buffered;
    }
    
    /* ------------------------------------------------------------ */
    public String getDatedFilename()
    {
        if (_fileOut instanceof RolloverFileOutputStream)
            return ((RolloverFileOutputStream)_fileOut).getDatedFilename();
        return null;
    }
    
    /* ------------------------------------------------------------ */
    public void setLogDateFormat(String format)
    {
        _logDateFormat=format;
    }

    /* ------------------------------------------------------------ */
    public String getLogDateFormat()
    {
        return _logDateFormat;
    }
    
    /* ------------------------------------------------------------ */
    public void setLogTimeZone(String tz)
    {
        _logTimeZone=tz;
    }

    /* ------------------------------------------------------------ */
    public String getLogTimeZone()
    {
        return _logTimeZone;
    }
    
    /* ------------------------------------------------------------ */
    public int getRetainDays()
    {
        return _retainDays;
    }

    /* ------------------------------------------------------------ */
    public void setRetainDays(int retainDays)
    {
        _retainDays = retainDays;
    }

    /* ------------------------------------------------------------ */
    public boolean isExtended()
    {
        return _extended;
    }

    /* ------------------------------------------------------------ */
    public void setExtended(boolean e)
    {
        _extended=e;
    }
    
    /* ------------------------------------------------------------ */
    public boolean isAppend()
    {
        return _append;
    }

    /* ------------------------------------------------------------ */
    public void setAppend(boolean a)
    {
        _append=a;
    }
    
    /* ------------------------------------------------------------ */
    public void start()
        throws Exception
    {
        _buf=new ByteArrayISO8859Writer();
        _logDateCache=new DateCache(_logDateFormat,_logLocale);
        _logDateCache.setTimeZoneID(_logTimeZone);
        
        if (_filename != null)
        {
            _fileOut=new RolloverFileOutputStream(_filename,_append,_retainDays);
            _closeOut=true;
        }
        else
            _fileOut=System.err;

        if (_buffered)
            _out=new BufferedOutputStream(_fileOut);
        else
            _out=_fileOut;
    }

    /* ------------------------------------------------------------ */
    public boolean isStarted()
    {
        return _fileOut!=null;
    }
    
    /* ------------------------------------------------------------ */
    public void stop()
    {
        if (_out!=null && _closeOut)
            try{_out.close();}catch(IOException e){Code.ignore(e);}
        _out=null;
        _fileOut=null;
        _closeOut=false;
        _buf=null;
        _logDateCache=null;
    }
    
    /* ------------------------------------------------------------ */
    public void log(HttpRequest request,
                    HttpResponse response,
                    int responseLength)
    {
        try{
            synchronized(_buf.getLock())
            {
                if (_fileOut==null)
                    return;
                
                _buf.write(request.getRemoteAddr());
                _buf.write(" - ");
                String user = request.getAuthUser();
                _buf.write((user==null)?"-":user);
                _buf.write(" [");
                _buf.write(_logDateCache.format(request.getTimeStamp()));
                _buf.write("] \"");
                request.writeRequestLine(_buf);
                _buf.write("\" ");
                int status=response.getStatus();    
                _buf.write('0'+((status/100)%10));
                _buf.write('0'+((status/10)%10));
                _buf.write('0'+(status%10));
                if (responseLength>=0)
                {
                    _buf.write(' ');
                    if (responseLength>99999)
                        _buf.write(Integer.toString(responseLength));
                    else
                    {
                        if (responseLength>9999) 
                            _buf.write('0'+((responseLength/10000)%10));
                        if (responseLength>999) 
                            _buf.write('0'+((responseLength/1000)%10));
                        if (responseLength>99) 
                            _buf.write('0'+((responseLength/100)%10));
                        if (responseLength>9) 
                            _buf.write('0'+((responseLength/10)%10));
                        _buf.write('0'+(responseLength%10));
                    }
                    _buf.write(' ');
                }
                else
                    _buf.write(" - ");
                
                if (_extended)
                {
                    String referer = request.getField(HttpFields.__Referer);
                    if(referer==null)
                        _buf.write("\"-\" ");
                    else
                    {
                        _buf.write('"');
                        _buf.write(referer);
                        _buf.write("\" ");
                    }
                    
                    String agent = request.getField(HttpFields.__UserAgent);
                    if(agent==null)
                    _buf.write("\"-\"");
                    else
                    {
                        _buf.write('"');
                        _buf.write(agent);
                        _buf.write('"');
                    }
                }
                _buf.write(StringUtil.__LINE_SEPARATOR);
                _buf.flush();
                _buf.writeTo(_out);
                _buf.reset();
            }
        }
        catch(IOException e)
        {
            Code.warning(e);
        }
    }
}


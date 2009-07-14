package org.jboss.test.jrmp.ejb;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

import org.apache.log4j.Category;

/** A custom server socket that uses the GZIPInputStream and GZIPOutputStream
streams for compression.

@see java.net.ServerSocket
@see java.util.zip.GZIPInputStream
@see java.util.zip.GZIPOutputStream

@author  Scott.Stark@jboss.org
@version $Revision: 1.1.1.1 $
*/
class CompressionServerSocket extends ServerSocket
{
   static Category log = Category.getInstance(CompressionServerSocket.class);

    private boolean closed;

    public CompressionServerSocket(int port) throws IOException 
    {
        super(port);
        log.debug("ctor, port="+port);
    }

    public Socket accept() throws IOException
    {
        Socket s = new CompressionSocket();
        implAccept(s);
        return s;
    }

    public int getLocalPort()
    {
        if( closed == true )
            return -1;
        return super.getLocalPort();
    }

    public void close() throws IOException
    {
        closed = true;
        super.close();
        log.debug("close");
    }
}

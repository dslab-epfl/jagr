/* Portions Copyright  (c) 2002 The Board of Trustees of The Leland Stanford
 * Junior University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

// based on code by Matt Welsh, modified to remove Ninja/iSpace dependencies
// Emre Kiciman, emrek@cs.stanford.edu
// see original copyright below.

// iSpace, by Matt Welsh (mdw@cs.berkeley.edu)
// See http://www.cs.berkeley.edu/~mdw/proj/ninja for details
// (c) 1998 by Matt Welsh and Regents of the University of California

package swig.httpd;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Thread which opens up a server socket on the given port and
 * dispatches URLs to the HttpServiceLocal passed to it.
 * @see HttpServiceLocal
 */
public class HttpServerThread extends Thread {

    private ServerSocket servsock;
    private HttpServiceLocal service;
    int port;

    /**
     * Creates an HttpServerThread with the given HttpServiceLocal as the
     * dispatchee.
     */
    public HttpServerThread(HttpServiceLocal theservice) throws IOException {
        service = theservice;
        this.port = 8080;
        servsock = new ServerSocket(port);
    }

    /**
     * Creates an HttpServerThread with the given HttpServiceLocal as the
     * dispatchee, and the given (already-created) ServerSocket to listen
     * on.
     */
    public HttpServerThread(HttpServiceLocal theservice, ServerSocket sock) {
        service = theservice;
        servsock = sock;
    }

    /**
     * Creates an HttpServerThread with the given HttpServiceLocal as the
     * dispatchee, and the given port to listen on.
     */
    public HttpServerThread(HttpServiceLocal theservice, int port)
        throws IOException {
        service = theservice;
        if (port == 0) {
            port = 8080;
        }
        else if (port == -1) {
            port = 0;
        }
        servsock = new ServerSocket(port);
        this.port = servsock.getLocalPort();
    }

    private void relisten() {
        HttpServerThread newthread = new HttpServerThread(service, servsock);
        newthread.start();
    }

    public void run() {
        Socket sock;

        try {
            sock = servsock.accept();
            // System.out.println("HttpServerThread: Connection from "+sock.getInetAddress().getHostName());
            relisten();
        }
        catch (Exception e) {
            System.err.println("HttpServerThread: Can't accept on servsock.");
            return;
        }

        try {
            sock.setTcpNoDelay(true);
            sock.setSoLinger(true, 10000);
        }
        catch (Exception e) {
            // Just show a warning
            System.out.println(
                "HttpServerThread: WARNING - can't "
                    + "set socket options, continuing anyway.");
        }

        try {
            InputStream is = sock.getInputStream();
            DataInputStream dis = new DataInputStream(is);
            //LineNumberReader lnr = new LineNumberReader(new InputStreamReader(is));
            BufferedOutputStream bos =
                new BufferedOutputStream(sock.getOutputStream());

            // todo: java.io.DataInputStream.readLine() has been depecrated
            String s = dis.readLine();
            StringTokenizer st = new StringTokenizer(s);

            String req, theURL, version;
            try {
                req = st.nextToken();
                theURL = st.nextToken();
                if (st.hasMoreTokens()) {
                    version = st.nextToken();
                }
                else {
                    version = null;
                }
            }
            catch (NoSuchElementException e) {
                // Give up on it
                try {
                    sock.close();
                }
                catch (Exception e1) {
                    // Do nothing
                }
                return;
            }

            /*
              if (version != null) {
                boolean done = false;
              
              while (! done) {
                s = dis.readLine();
            st = new StringTokenizer (s);
            
            if (st.countTokens() == 0)
            done = true;
              }
            }
            */

            String clientHostname = (sock.getInetAddress()).getHostName();
            if (clientHostname == null) {
                clientHostname = new String("");
            }

            if (req.equalsIgnoreCase("get")) {
                service.getURL(theURL, version, clientHostname, bos, is);
                bos.flush();
            }
            else if (req.equalsIgnoreCase("post")) {
                service.postURL(theURL, version, clientHostname, bos, is);
                bos.flush();
            }
            else {
                System.out.println(
                    "HttpServerThread: got request other than GET or POST");
                PrintWriter ps = new PrintWriter(bos);
                ps.println(
                    "<html>"
                        + "<title>swig.httpd HttpServerThread "
                        + "Error</title> HttpServerThread "
                        + "couldn't parse your request:"
                        + " <b>"
                        + s
                        + "</b><p>Sorry mate!</html>");
                ps.flush();
            }

        }
        catch (Exception e) {
            System.out.println(
                "HttpServerThread: Can't reply: " + e.getMessage());
        }

        try {
            sock.close();
        }
        catch (Exception e) {
            // Do nothing
        }
    }

}

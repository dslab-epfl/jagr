/* Portions Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * Implementation of an HTTP server
 * Other services that implement HttpExportedService,
 * they can be invoked via GET/POST URLs in the /service/... namespace.
 * @see HttpExportedService
 */
public class HttpService implements HttpServiceLocal {

    /** data members **/

    protected HashMap services;

    private HttpServerThread thread;
    private String httpdatadir;
    private String hostname;
    private int portnum = 0;
    private static int DISPATCH_GET = 0, DISPATCH_POST = 1, DISPATCH_HEAD = 2;

    /** constructors **/

    public HttpService(String httpdatadir, int portnum, String hostname) {

        services = new HashMap();

        this.httpdatadir = httpdatadir;
        this.portnum = portnum;
        this.hostname = hostname;

        if (httpdatadir == null) {
            System.err.println(
                "HttpService: No datadir specified; "
                    + "page loading disabled.");
        }

        try {
            thread = new HttpServerThread(this, portnum);
            this.portnum = thread.port;
            thread.start();
        }
        catch (Exception e) {
            System.err.println(
                "HttpService: Can't start HttpServerThread:" + e.getMessage());
        }
    }

    /** public methods **/

    public void registerService(
        String servicename,
        HttpExportedService service) {
        services.put(servicename, service);
    }

    public HttpExportedService getService(String servicename) {
        return (HttpExportedService) services.get(servicename);
    }

    public Set listServices() {
        return services.keySet();
    }

    public void unregisterService(String servicename) {
        services.get(servicename);
    }

    public void destroy() {
        // todo: thread.stop has been depecrated
        thread.stop();
    }

    /**
     * Return the port number on which this HTTP server is listening.
     */
    public int getPortNumber() throws java.rmi.RemoteException {
        return portnum;
    }

    /**
     * Return the hostname on which this HTTP server is listening.
     */
    public String getHostname() throws java.rmi.RemoteException {
        return hostname;
    }

    private void sendPage(String theURL, OutputStream os)
        throws FileNotFoundException, IOException {

        if (httpdatadir == null)
            throw new FileNotFoundException("No httpdatadir");

        File f = new File(httpdatadir + theURL);

        if (f.isDirectory()) {
            if (theURL.endsWith("/")) {
                f = new File(httpdatadir + theURL + "index.html");
            }
            else {
                f = new File(httpdatadir + theURL + "/index.html");
            }
        }

        BufferedInputStream is =
            new BufferedInputStream(new FileInputStream(f));

        // XXX mdw: Looks like Netscape 4.04 has a bug in HTTP/0.9 responses to
        // HTTP/1.x requests... for now we assume 1.x only.
        PrintWriter pw = new PrintWriter(os);
        pw.println("HTTP/1.1 200 OK");

        // XXX mdw: How to do this correctly?
        if (theURL.endsWith(".gif")) {
            pw.println("Content-Type: image/gif");
        }
        else if (theURL.endsWith(".jpg")) {
            pw.println("Content-Type: image/jpeg");
        }
        else if (theURL.endsWith(".htm")) {
            pw.println("Content-Type: text/html");
        }
        else if (theURL.endsWith(".html")) {
            pw.println("Content-Type: text/html");
        }
        else if (theURL.endsWith(".java")) {
            pw.println("Content-Type: text/plain");
        }
        else if (theURL.endsWith(".txt")) {
            pw.println("Content-Type: text/plain");
        }
        pw.println("");
        pw.flush();

        int ch;
        while ((ch = is.read()) != -1) {
            os.write(ch);
        }
        os.flush();
        is.close();
    }

    private void dispatchService(
        String theURL,
        String clientHostname,
        OutputStream os,
        InputStream is,
        int dispatchType)
        throws NotBoundException, IOException {

        // Got a URL for a name in "/service" - see if there's a class by this
        // name, and if so, pass it the URL through the GET interface.

        int si = theURL.indexOf("/service/");
        int se = si + "/service/".length();
        int sslash = theURL.indexOf("/", se);
        int squestion = theURL.indexOf("?", se);
        int sn;

        if (sslash == -1) {
            if (squestion == -1)
                sn = theURL.length();
            else
                sn = squestion;
        }
        else {
            if (squestion == -1)
                sn = sslash;
            else
                sn = ((sslash < squestion) ? (sslash) : (squestion));
        }

        // sn now points to the ?, /, or end of string
        String servicename = theURL.substring(se, sn);
        String servicedata = theURL.substring(sn, theURL.length());

        HttpExportedService serv =
            (HttpExportedService) services.get(servicename);

        if (serv == null) {
            throw new NotBoundException(
                "HttpService: dispatchService: "
                    + servicename
                    + " not bound in local service loader.");
        }

        if (dispatchType == DISPATCH_POST) {
            serv.postURL(theURL, servicedata, clientHostname, os, is);
        }
        else if (dispatchType == DISPATCH_GET) {
            serv.getURL(theURL, servicedata, clientHostname, os, is);
            // Clean up any unread header info 
            if (is.available() > 0) {
                System.out.println(
                    "WARNING: HttpExportedService did "
                        + "not read"
                        + " request data from client\n"
                        + " - cleaning up in HttpService "
                        + "(& discarding the rest of the "
                        + "request)");
                while (is.available() > 0 && is.read() != -1) {
                }
            }
            is.close();
        }
        else {
            //HEserv.headURL(theURL, servicedata, clientHostname, os, is);
        }

    }

    // Zap ".." in URL before appending to datadir
    private String squashURL(String theURL) throws EmptyStackException {
        String sURL;
        if (theURL.indexOf("..") != -1) {
            Stack ds = new Stack();
            StringTokenizer st = new StringTokenizer(theURL, "/");
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                if (s.equals("..")) {
                    if (!ds.empty())
                        ds.pop();
                }
                else {
                    ds.push(s);
                }
            }
            if (ds.empty()) {
                return "/";
            }
            else {
                sURL = (String) ds.pop();
                while (!ds.empty()) {
                    sURL = (String) ds.pop() + "/" + sURL;
                }
                sURL = "/" + sURL;
                return sURL;
            }
        }
        else {
            return theURL;
        }
    }

    public void getURL(
        String theURL,
        String version,
        String clientHostname,
        OutputStream os,
        InputStream is) {

        // XXX mdw: What's the right way to generate an HTML interface here?
        // --> For now, consider HttpService just a front-end to iSpaceServices
        // that it knows about - it's composing them!

        try {

            String sURL = squashURL(theURL);
            if (theURL.startsWith("/service")) {
                dispatchService(sURL, clientHostname, os, is, DISPATCH_GET);
            }
            else {
                if (version != null) {
                    // Have to read up until next blank line
                    LineNumberReader lin =
                        new LineNumberReader(new InputStreamReader(is));
                    try {
                        while (lin.readLine().length() > 0) {
                            // Do nothing
                        }
                    }
                    catch (Exception e) {
                    }
                }
                sendPage(sURL, os);
            }
        }
        catch (Exception e) {
            PrintWriter ps = new PrintWriter(os);
            ps.println(
                "<HTML><HEAD><TITLE>Reply from Swig HTTPD "
                    + "HttpService</TITLE></HEAD>");
            ps.println("<BODY>");
            ps.println(
                "Response from <b>Swig HTTPD</b> for request <b>get "
                    + theURL
                    + "</b>:");
            ps.println(
                "<p><p>Can't load that URL for some reason "
                    + "(though I tried).");
            ps.println("<tt>" + e.getMessage() + "</tt>");
            ps.println("<HTML>");
            ps.flush();
        }
    }

    public void postURL(
        String theURL,
        String version,
        String clientHostname,
        OutputStream os,
        InputStream is) {
        try {
            String sURL = squashURL(theURL);

            if (theURL.startsWith("/service/")) {
                dispatchService(sURL, clientHostname, os, is, DISPATCH_POST);
            }
            else {
                PrintWriter ps = new PrintWriter(os);
                ps.println(
                    "<HTML><HEAD><TITLE>Reply from swig.httpd "
                        + "HttpService</TITLE></HEAD>");
                ps.println("<BODY>");
                ps.println(
                    "Response from <b>swig.htpd</b> for request "
                        + "<b>get "
                        + theURL
                        + "</b>:");
                ps.println(
                    "<p><p>That URL needs to start with "
                        + "<tt>/service/</tt>, sorry...");
                ps.println("<HTML>");
                ps.flush();
            }
        }
        catch (Exception e) {
            //System.out.println("HttpService: Exception: POST request for URL "+theURL+":" + e.getMessage() );
            // XXX mdw: For some reason the following doesn't work (I think it
            // has to do with it being a response to a POST rather than GET)...
            PrintWriter ps = new PrintWriter(os);
            ps.println(
                "<HTML><HEAD><TITLE>Reply from swig.httpd "
                    + "HttpService</TITLE></HEAD>");
            ps.println("<BODY>");
            ps.println(
                "Response from <b>swig.httpd</b> for request "
                    + "<b>get "
                    + theURL
                    + "</b>:");
            ps.println(
                "<p><p>Can't load that URL for some reason "
                    + "(though I tried).");
            ps.println("<tt>" + e.getMessage() + "</tt>");
            ps.println("<HTML>");
            ps.flush();
        }
    }

    public void headURL(
        String theURL,
        String version,
        String clientHostname,
        OutputStream os,
        InputStream is) {
        try {
            String sURL = squashURL(theURL);
            if (theURL.startsWith("/service/")) {
                dispatchService(sURL, clientHostname, os, is, DISPATCH_HEAD);
            }
            else {
                PrintWriter ps = new PrintWriter(os);
                ps.println(
                    "<HTML><HEAD><TITLE>Reply from swig.httpd "
                        + "HttpService</TITLE></HEAD>");
                ps.println("<BODY>");
                ps.println(
                    "Response from <b>swig.httpd</b> for "
                        + "request <b>get "
                        + theURL
                        + "</b>:");
                ps.println(
                    "<p><p>That URL needs to start with "
                        + "<tt>/service/</tt>, sorry..");
                ps.println("<HTML>");
                ps.flush();
            }
        }
        catch (Exception e) {
            System.out.println(
                "HttpService: Exception: POST request "
                    + "for URL "
                    + theURL
                    + ":"
                    + e.getMessage());
            // XXX mdw: For some reason the following doesn't work (I think it
            // has to do with it being a response to a POST rather than GET)...
            PrintWriter ps = new PrintWriter(os);
            ps.println(
                "<HTML><HEAD><TITLE>Reply from swig.httpd "
                    + "HttpService</TITLE></HEAD>");
            ps.println("<BODY>");
            ps.println(
                "Response from <b>swig.httpd</b> for request <b>get "
                    + theURL
                    + "</b>:");
            ps.println(
                "<p><p>Can't load that URL for some reason "
                    + "(though I tried).");
            ps.println("<tt>" + e.getMessage() + "</tt>");
            ps.println("<HTML>");
            ps.flush();
        }
    }

}

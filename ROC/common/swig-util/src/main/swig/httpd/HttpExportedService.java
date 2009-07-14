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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface that classes can implement if they wish to hook into
 * the URL namespace of an HttpService on this httpd
 *
 * Basically, if a class implements HttpExportedService, and is registered
 * with HttpService, its
 * getURL and postURL methods will be invoked when a URL of the form
 * <br><tt>/service/someservicename/....?...</tt><br>
 * is accessed. <tt>someservicename</tt> must be the fully-qualified name
 * of the service as registered in the <b>iSpace</b> loader.
 *
 * For example, if the service <tt>mdw.testcode.TestService</tt> is loaded,
 * the URLs
 * <tt>/service/mdw.testcode.TestService/...</tt>
 * and
 * <tt>/service/mdw.testcode.TestService?...</tt>
 * will cause the getURL and postURL methods of that service to be
 * invoked.
 */
public interface HttpExportedService {

    /**
     * Method invoked on this service when a GET URL is received.
     * @param theURL The entire URL used to trigger this request.
     * @param urlData The URL after the servicename component (e.g.,  starting
     * at the first <tt>/</tt> or <tt>?</tt> after the service name).
     * @param clientHostname The hostname of the client connecting, or the
     *  empty string if not known.
     * @param os OutputStream used to send a reply to the client.
     * @param is InputStream used to read the rest of the HTTP request (e.g.,
     * the HTTP request header)
     */
    void getURL(
        String theURL,
        String urlData,
        String clientHostname,
        OutputStream os,
        InputStream is);

    /**
     * Method invoked on this service when a HEAD URL is received.
     * @param theURL The entire URL used to trigger this request.
     * @param URLdata The URL after the servicename component (e.g.,
     *  starting at the first <tt>/</tt> or <tt>?</tt> after the service name).
     * @param clientHostname The hostname of the client connecting, or the
     *  empty string if not known.
     * @param os OutputStream used to send a reply to the client.
     * @param is InputStream used to read the rest of the HTTP request (e.g.,
     * the  HTTP request header)
     */
    //public void headURL(String theURL, String URLdata, String clientHostname,
    // OutputStream os, InputStream is);

    /**
     * Method invoked on this service when a POST URL is received.
     * @param theURL The entire URL used to trigger this request.
     * @param urlData The URL after the servicename component (e.g.,  starting
     * at the first <tt>/</tt> or <tt>?</tt> after the service name).
     * @param clientHostname The hostname of the client connecting, or the
     *  empty string if not known.
     * @param os OutputStream used to send a reply to the client.
     * @param is InputStream used to read the rest of the HTTP request (e.g.,
     * the HTTP request header and the POST data itself).
     */
     void postURL(
        String theURL,
        String urlData,
        String clientHostname,
        OutputStream os,
        InputStream is);

}

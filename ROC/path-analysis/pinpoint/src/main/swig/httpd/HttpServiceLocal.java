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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for "clients" of an HttpServerThread - when an HttpServerThread
 * is created, a class implementing HttpServiceLocal is passed to it,
 * and all HTTP requests to that thread are dispatched for it.
 * This is different than an HttpExportedService, which only hooks into
 * URLs beginning with <tt>/service/</tt>.
 * 
 * @see HttpService
 * @see ProxyService
 * @see HttpServerThread
 * @see HttpExportedService
 */
interface HttpServiceLocal {

    /**
     * Method invoked when a GET URL is received on the corresponding
     * HttpServerThread.
     */
    public void getURL(
        String theURL,
        String version,
        String clientHostname,
        OutputStream os,
        InputStream is);

    /**
     * Method invoked when a HEAD URL is received on the corresponding
     * HttpServerThread.
     */
    public void headURL(
        String theURL,
        String version,
        String clientHostname,
        OutputStream os,
        InputStream is);

    /**
     * Method invoked when a POST URL is received on the corresponding
     * HttpServerThread.
     */
    public void postURL(
        String theURL,
        String version,
        String clientHostname,
        OutputStream os,
        InputStream is);
}

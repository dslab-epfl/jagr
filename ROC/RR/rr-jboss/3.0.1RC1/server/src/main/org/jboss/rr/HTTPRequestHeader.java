//
// $Id: HTTPRequestHeader.java,v 1.1 2003/03/17 11:44:15 steveyz Exp $
//

package org.jboss.RR;

/**
 * @(#)HTTPRequestHeader.java	April 3, 1998
 *
 * I MAKE NO WARRANTIES ABOUT THE SUITABILITY OF THIS SOFTWARE, EITHER
 * EXPRESS OR IMPLIED AND SHALL NOT BE LIABLE FOR ANY DAMAGES THIS
 * SOFTWARE MAY BRING TO YOUR SYSTEM. USE IT AT YOUR OWN RISK.
 *
 * Author : Steve Yeong-Ching Hsueh
 */

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * This class contains common functions for parsing HTTP request headers
 * and has member elements for storing header values.
 */
class HTTPRequestHeader {

    public String Method  = null;
    public String URI     = null;
    public String Version = null;
    public String primeheader = "";;
    public Hashtable headerfields = new Hashtable();
    private boolean MalFormedHeader = false;
    private int     content_length  = 0;

    /**
     * constructor
     */
    HTTPRequestHeader() {

    }

    /**
     * constructor
     */
    HTTPRequestHeader(String in) {
        if(!parseHeader(in)) MalFormedHeader = true;
    }

    /**
     * get first line of the request header
     */
    public String getPrimeHeader() {
        return primeheader;
    }

    /**
     * get header value by name
     */
    public String getHeader(String name) {
        return (String) headerfields.get(name);
    }

    /**
     * get request method
     */
    public String getMethod() {
        return Method;
    }

    /**
     * get request URI
     */
    public String getURI() {
        return URI;
    }

    /**
     * get HTTP version
     */
    public String getVersion() {
        return Version;
    }

    /**
     * get content length
     */
    public int getContentLength() {
        return content_length;
    }

    /**
     * get all headers in a Hashtable
     */
    public Hashtable getHeaderFields() {
        return headerfields;
    }

    /**
     * parse HTTP request headers
     */
    public boolean parseHeader(String input) {
        StringTokenizer st;
        String token, name, value, delimiter;
        int pos;


        if(input == null || input.equals("")) {
            return MalFormedHeader = true;
        }


        MalFormedHeader = false;

        if(input.endsWith("\r\n")) delimiter = "\r\n";
        else delimiter = "\n";

        // read the first line to get method, URI, and version
        if((pos = input.indexOf(delimiter)) < 0) {
            MalFormedHeader = true;
            return false;
        }

        primeheader = input.substring(0, pos);
        // System.out.println(primeheader);
        st = new StringTokenizer(primeheader, " ");
        for(int i=0; st.hasMoreTokens(); i++) {
            switch( i ) {
                case 0: Method = st.nextToken();
                    break;
                case 1: URI = st.nextToken();
                    break;
                case 2: Version = st.nextToken();
                    break;
                default:
                    MalFormedHeader = true;
                    return false;
            }
        }

        if( Method == null || URI == null || Version == null ) {
            MalFormedHeader = true;
            return false;
        }


        // remaining header fields
        st = new StringTokenizer(input.substring(pos), delimiter);
        while( st.hasMoreTokens() ) {
            token = st.nextToken();
            if((pos = token.indexOf(": ")) < 0) {
                MalFormedHeader = true;
                return false;
            }
            name  = token.substring(0, pos);
            value = token.substring(pos +2);
            // System.out.println(name + "<=>" + value);
            if(name.equalsIgnoreCase("Content-Length")) {
                content_length = Integer.parseInt(value);
            }
            headerfields.put(name, value);
        }

        return true;
    }

    /**
     * see if this header is ok.
     */
    public boolean isMalFormedHeader() {
        return MalFormedHeader;
    }
}


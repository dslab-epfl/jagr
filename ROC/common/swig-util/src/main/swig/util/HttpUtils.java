/*
 * Modification: This class moved from ninja.util package to swig.util
 *   package for convenience.  Emre Kiciman <emrek@cs.stanford.edu>
 */
package swig.util;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * (Hopefully) useful utilities for HTTP-aware services.
 * @author Ian Goldberg and Mike Chen
 */
public final class HttpUtils {
    private HttpUtils() {
    }

    private static int ctoh(char c) {
        if ((c >= '0') && (c <= '9')) {
            return (c - '0');
        }

        if ((c >= 'A') && (c <= 'F')) {
            return (c - 'A' + 10);
        }

        if ((c >= 'a') && (c <= 'f')) {
            return (c - 'a' + 10);
        }

        return 0;
    }

    private static String demunge(String data, int offset, int len) {
        // Optimistically assume the result will be the same length as
        // the input
        StringBuffer res = new StringBuffer(len);
        int s;

        for (s = offset; s < (offset + len); ++s) {
            char c = data.charAt(s);

            if ((c == '%') && ((s + 2) < (offset + len))) {
                res.append(
                    (char) ((ctoh(data.charAt(s + 1)) << 4)
                        | (ctoh(data.charAt(s + 2)))));
                s += 2;
            }
            else if (c == '+') {
                res.append(' ');
            }
            else {
                res.append(c);
            }
        }

        return res.toString();
    }

    public static Hashtable parseURLEnc(String data, int offset, int len) {
        return parseURLEnc(data, offset, len, false);
    }

    /**
     * Parse a URL-encoded String into a hash table of key/value
     * pairs, each of which is a String
     *
     * @param data a URL-encoded string containing a substring of the form
     *             "name1=value1&name2=value2" etc.
     * @param offset the offset of the beginning of the substring
     * @param len the length of the substring
     * @param supportduplicatekeys to support duplicate keys, we return a
                                   hashtable mapping from keys to Lists of
                                   values
     * @return a Hashtable, with keys being the Strings name1, name2, etc.
     *         and values the Strings value1, value2, etc.  If duplicate
               key support is requested, the values are lists of values
     */
    public static Hashtable parseURLEnc(
        String data,
        int offset,
        int len,
        boolean supportduplicatekeys) {
        Hashtable h = new Hashtable();
        int ampoff;
        int eqoff;
        int curoff = offset;

        while (curoff < (offset + len)) {
            // Find the next =
            eqoff = data.indexOf('=', curoff);

            if (eqoff == -1) {
                // There was no =; pretend it was "query=whatever" and quit
                String value = demunge(data, curoff, (offset + len) - curoff);
                h.put("query", value);
                curoff = offset + len;

                break;
            }

            String key = demunge(data, curoff, eqoff - curoff);
            curoff = eqoff + 1;

            // Fing the next &
            ampoff = data.indexOf('&', curoff);

            if (ampoff == -1) {
                ampoff = offset + len;
            }

            String value = demunge(data, curoff, ampoff - curoff);

            if (supportduplicatekeys) {
                List l = (List) h.get(key);

                if (l == null) {
                    l = new LinkedList();
                    h.put(key, l);
                }

                l.add(value);
            }
            else {
                h.put(key, value);
            }

            curoff = ampoff + 1;
        }

        return h;
    }

    public static Hashtable getHttpHeaders(LineNumberReader in)
        throws IOException {
        String line;
        Hashtable headers = new Hashtable();

        while ((line = in.readLine()) != null) {
            //println(line);
            if (line.trim().length() == 0) {
                break;
            }

            StringTokenizer parser = new StringTokenizer(line, ": ");
            headers.put(parser.nextToken().toLowerCase(), parser.nextToken());
        }

        return headers;
    }

    public static Hashtable getGetArgs(String urlData) {
        return parseURLEnc(urlData, 0, urlData.length());
    }

    /**
     * Parse the args for a POST request.  The input should be the start of the Http content.
     * This method should be called right after getHttpHeaders();
     *
     */
    public static Hashtable getPostArgs(
        Hashtable httpHeaders,
        LineNumberReader in)
        throws IOException {
        //// a hack to solve an IE bug where the POST data isn't terminated correctly.
        //// we therefore have to use the content-length header to figure out how
        //// much data we need to read in.
        String line = null;
        String lengthStr;

        if ((lengthStr = (String) httpHeaders.get("content-length")) != null) {
            int length = Integer.parseInt(lengthStr);
            System.out.println("getPostArgs: content-length=" + length);
            char[] buf = new char[length];
            int offset = 0;
            int size = 0;

            /* original code: what is this trying to do???? -jrvb */
            /* while ((size = in.read(buf, offset, length)) != -1) {
              line = new String(buf);
              //println(line);
              offset += size;
              if (offset == length)
            break;
            } */
            while ((offset < length)
                && ((size = in.read(buf, offset, length - offset)) != -1)) {
                offset += size;
            }

            line = new String(buf);
        }
        else {
            line = in.readLine();
        }

        return swig.util.HttpUtils.parseURLEnc(line, 0, line.length());
    }

    public static void sendHttpHeadersOK(PrintWriter ps, boolean noCache)
        throws IOException {
        ps.println("HTTP/1.1 200");
        ps.println("Server: SWIG/Httpd (http://swig.stanford.edu)");
        ps.println("Date: " + new Date());
        ps.println("Content-Type: text/html");

        //// cache control headers
        if (noCache) {
            ps.println("Cache-Control: no-cache"); // HTTP/1.1
            ps.println("Pragma: no-cache"); // HTTP/1.0
        }

        ps.print("\r\n");
    }

    public static void sendHttpHeadersOKNoContent(
        PrintWriter ps,
        boolean noCache)
        throws IOException {
        ps.println("HTTP/1.1 200");
        ps.println("Server: SWIG/Httpd (http://swig.stanford.edu)");
        ps.println("Date: " + new Date());

        //// cache control headers
        if (noCache) {
            ps.println("Cache-Control: no-cache"); // HTTP/1.1
            ps.println("Pragma: no-cache"); // HTTP/1.0
        }
    }

    public static void setContentType(PrintWriter ps, String type)
        throws IOException {
        ps.println("Content-Type: " + type);
        ps.print("\r\n");
    }
}
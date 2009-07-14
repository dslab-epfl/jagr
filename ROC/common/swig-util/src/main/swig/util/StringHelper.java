/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * $Id: StringHelper.java,v 1.1 2003/04/22 20:09:37 emrek Exp $
 *
 * $Log: StringHelper.java,v $
 * Revision 1.1  2003/04/22 20:09:37  emrek
 * * populated swig-util directory ROC/common/swig-util
 *
 * Revision 1.1.1.1  2003/03/07 08:12:40  emrek
 * * first checkin of PP to the new ROC/PP/ subdir after reorg
 *
 * Revision 1.2  2002/12/28 12:27:30  emrek
 * no functional changes, just formatting and general cleanup. also did some javadoc'ing of roc.pinpoint.** classes.
 *
 * Revision 1.1  2002/12/17 15:27:43  emrek
 * first commit of new pinpoint tracing and analysis framework
 *
 * Revision 1.3  2002/08/19 06:49:43  emrek
 * Added copyright information to source files
 *
 * Revision 1.2  2002/08/15 22:08:07  emrek
 * formatting changes (only) because of new editor
 *
 * Revision 1.1.1.1  2002/07/17 09:07:53  emrek
 *
 *
 * Revision 1.1.1.1  2001/10/17 00:53:42  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.5  2001/03/07 01:23:52  emrek
 * added JoinStrings( String[] ) -> List
 *
 * Revision 1.4  2000/10/08 04:07:40  emrek
 * adding FileFinder, to support java-classpath style searching for files
 *
 * Revision 1.3  2000/09/18 03:52:36  emrek
 * added working APC code.  Also refined path descriptions and Holders, and fixed a number of bugs all over the place.
 *
 * Revision 1.2  2000/09/11 21:21:27  emrek
 * *** empty log message ***
 *
 * Revision 1.1  2000/09/09 21:11:17  emrek
 * *** empty log message ***
 *
 *
 */
public class StringHelper {
    protected static String ReplaceHelper(
        String src,
        String search,
        String replacewith) {
        int idx = src.indexOf(search);
        int len = search.length();

        if (idx == -1) {
            return null;
        }

        StringBuffer t = new StringBuffer(src.length() + replacewith.length());
        t.append(src.substring(0, idx));
        t.append(replacewith);
        t.append(src.substring(idx + len));

        return t.toString();
    }

    public static String Replace(
        String src,
        String search,
        String replacewith) {
        String ret = ReplaceHelper(src, search, replacewith);

        if (ret == null) {
            ret = src;
        }

        return ret;
    }

    public static String ReplaceAll(
        String src,
        String search,
        String replacewith) {
        String ret = src;
        String t = null;

        while (true) {
            t = ReplaceHelper(ret, search, replacewith);

            if (t != null) {
                ret = t;
            }
            else {
                break;
            }
        }

        return ret;
    }

    public static String loadString(URL url) throws IOException {
        return loadString(url.openStream());
    }

    public static String loadString(InputStream is) throws IOException {
        StringWriter sw = new StringWriter();

        byte[] buf = new byte[1024];
        int c = 0;

        do {
            c = is.read(buf);

            if (c > 0) {
                sw.write(new String(buf, 0, c));
            }
        }
        while (c != -1);

        sw.close();

        return sw.getBuffer().toString();
    }

    public static int CountOccurrences(String s, String substring) {
        String tmp = s;
        int idx = 0;
        int count = 0;

        while (true) {
            idx = tmp.indexOf(substring);

            if (idx >= 0) {
                tmp = s.substring(idx + substring.length());
                count++;
            }
            else {
                break;
            }
        }

        return count;
    }

    public static List SeparateStrings(String s, char sep) {
        LinkedList l = new LinkedList();

        int curridx = 0;
        int idx = s.indexOf(sep);

        while (idx != -1) {
            if (idx > curridx) {
                l.add(s.substring(curridx, idx));
            }

            curridx = idx + 1;
            idx = s.indexOf(sep, curridx);
        }

        if (curridx < s.length()) {
            l.add(s.substring(curridx));
        }

        return l;
    }

    public static String JoinStrings(List s, char sep) {
        String ret = "";
        Iterator iter = s.iterator();

        while (iter.hasNext()) {
            ret += (String) iter.next();

            if (iter.hasNext()) {
                ret += sep;
            }
        }

        return ret;
    }
}
/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * $Id: FileFinder.java,v 1.1 2003/04/22 20:09:37 emrek Exp $
 *
 * $Log: FileFinder.java,v $
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
 * Revision 1.1.1.1  2002/07/17 09:07:51  emrek
 *
 *
 * Revision 1.1.1.1  2001/10/17 00:53:42  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.2  2001/06/21 20:31:20  emrek
 * added a "priority" option to Debug Messages, and updated swig.util classes
 * that use debug messages, as well as the HTTP Debug interface.
 *
 * Revision 1.1  2000/10/08 04:07:40  emrek
 * adding FileFinder, to support java-classpath style searching for files
 *
 *
 */
public class FileFinder {
    public static final char DEFAULT_SEP = ';';
    protected char sep;
    protected List searchpaths;

    public FileFinder() {
        this(new LinkedList());
    }

    public FileFinder(List searchpaths) {
        this.searchpaths = searchpaths;
    }

    public FileFinder(String searchpath, char sep) {
        this(StringHelper.SeparateStrings(searchpath, sep));
        this.sep = sep;
    }

    public FileFinder(String searchpath) {
        this(searchpath, DEFAULT_SEP);
    }

    public void addPath(String path) {
        Debug.Assert(path.indexOf(sep) == -1);
        searchpaths.add(path);
    }

    public List getPaths() {
        return searchpaths;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("");
        Iterator iter = searchpaths.iterator();

        while (iter.hasNext()) {
            sb.append((String) iter.next());

            if (iter.hasNext()) {
                sb.append(sep);
            }
        }

        return sb.toString();
    }

    public void clearPaths() {
        searchpaths = new LinkedList();
    }

    public void removePath(String p) {
        searchpaths.remove(p);
    }

    public void setSeparator(char sep) {
        this.sep = sep;
    }

    public char getSeparator() {
        return sep;
    }

    protected FoundFile findURLFile(String file, String path)
        throws IOException {
        Debug.Print(
            "filefinder",
            Debug.LOW_PRIORITY,
            "URLFile: path=" + path + "; file=" + file);

        URL u = new URL(path + file);

        URLConnection conn = u.openConnection();

        if (conn instanceof HttpURLConnection) {
            if (((HttpURLConnection) conn).getResponseCode()
                != HttpURLConnection.HTTP_OK) {
                return null;
            }
        }

        FoundFile ret = new FoundFile();
        ret.inputstream = conn.getInputStream();
        ret.path = path;
        ret.file = file;

        return ret;
    }

    protected FoundFile findLocalFile(String file, String path)
        throws IOException {
        File f = new File(path + file);

        FoundFile ret = new FoundFile();
        ret.inputstream = new FileInputStream(f);
        ret.path = path;
        ret.file = file;

        return ret;
    }

    protected FoundFile findFileHelper(String file, String path) {
        FoundFile ret = null;

        try {
            ret = findURLFile(file, path);
        }
        catch (IOException e) {
        }

        if (ret == null) {
            try {
                ret = findLocalFile(file, path);
            }
            catch (IOException e) {
            }
        }

        return ret;
    }

    public FoundFile findFile(String file) {
        FoundFile ret = null;

        Iterator iter = searchpaths.iterator();

        while ((iter.hasNext()) && (ret == null)) {
            ret = findFileHelper(file, (String) iter.next());
        }

        return ret;
    }

    public FoundFile findFile(String file, String path) {
        FoundFile ret = null;

        ret = findFileHelper(file, path);

        if (ret == null) {
            ret = findFile(file);
        }

        return ret;
    }

    public class FoundFile {
        public InputStream inputstream;
        public String path;
        public String file;
    }
}
/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * loads a list of files from a config file
 *
 * $Id: FileList.java,v 1.1 2003/04/22 20:09:37 emrek Exp $
 *
 * $Log: FileList.java,v $
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
 * Revision 1.1.1.1  2001/10/17 00:53:41  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.1  2000/09/03 22:40:35  emrek
 * first complete checkin of brick, kernel, and admin code.
 *
 * Revision 1.1  2000/06/09 21:44:01  emrek
 * - removed some extra debug printlns
 * - added support for loading operators/connectors from jar files
 *
 *
 */
public class FileList {
    public static String[] LoadList(LineNumberReader lr) throws IOException {
        String line = null;
        ArrayList list = new ArrayList();

        do {
            line = lr.readLine();

            if ((line != null)
                && (!line.startsWith("#"))
                && (line.trim().length() > 0)) {
                list.add(line);
            }
        }
        while (line != null);

        return (String[]) list.toArray(new String[list.size()]);
    }

    public static String[] LoadList(InputStream is) throws IOException {
        return LoadList(new LineNumberReader(new InputStreamReader(is)));
    }

    public static String[] LoadList(File f) throws IOException {
        return LoadList(new LineNumberReader(new FileReader(f)));
    }

    public static String[] LoadList(URL url) throws IOException {
        return LoadList(url.openStream());
    }
}
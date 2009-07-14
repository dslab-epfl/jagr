/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 *
 * $Id: ByteHelper.java,v 1.1 2003/04/22 20:09:37 emrek Exp $
 *
 * $Log: ByteHelper.java,v $
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
 * Revision 1.1.1.1  2002/07/17 09:07:47  emrek
 *
 *
 * Revision 1.1.1.1  2001/10/17 00:53:42  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.2  2001/05/02 05:55:03  emrek
 * added MSOffice transformations, and made paths a little more
 * Windows-Friendly by adding some batch files to set up appropriate
 * environment variables.
 * Also, did some general clean up, and removed some stale files
 *
 * Revision 1.1  2000/11/28 02:18:01  emrek
 * *** empty log message ***
 *
 *
 */
public class ByteHelper {
    public static byte[] LoadByteArray(File f) throws IOException {
        return LoadByteArray(new FileInputStream(f));
    }

    public static byte[] LoadByteArray(URL url) throws IOException {
        return LoadByteArray(url.openStream());
    }

    public static byte[] LoadByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buf = new byte[4096];
        int c = 0;
        int t = 0;

        do {
            c = is.read(buf);

            if (c > 0) {
                os.write(buf, 0, c);
                t += c;
            }
        }
        while (c != -1);

        /*
        System.err.println( "ByteHelper: loaded " + t + " bytes from file" );
        System.err.println( "ByteHelper: the stream says we've got " + 
           os.size() + " bytes from file" );
        */
        is.close();
        os.flush();
        os.close();

        return os.toByteArray();
    }

    public static void SaveByteArray(byte[] data, File f) throws IOException {
        FileOutputStream os = new FileOutputStream(f);
        os.write(data);
        os.flush();
        os.close();
    }
}
/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

/**
 * This class contains helper functions for munging text into HTML and URIs
 *
 *
 * $Id: HtmlHelper.java,v 1.2 2002/12/28 12:27:30 emrek Exp $
 *
 * $Log: HtmlHelper.java,v $
 * Revision 1.2  2002/12/28 12:27:30  emrek
 * no functional changes, just formatting and general cleanup. also did some javadoc'ing of roc.pinpoint.** classes.
 *
 * Revision 1.1  2002/12/17 15:27:43  emrek
 * first commit of new pinpoint tracing and analysis framework
 *
 * Revision 1.4  2002/08/23 00:25:03  emrek
 * removed from println's that were annoying
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
 * Revision 1.1.1.1  2001/10/17 00:53:47  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.1  2001/09/23 21:53:21  emrek
 * added helper classes
 *
 *
 *
 */
public class HtmlHelper {
    static String[] encodingtable;

    static {
        encodingtable = new String[256];

        for (int i = 0; i < 256; i++) {
            encodingtable[i] = '%' + Integer.toHexString(i);
        }

        for (int i = '0'; i <= '9'; i++) {
            char[] cc = {(char) i };
            encodingtable[i] = new String(cc);
        }

        for (int i = 'A'; i <= 'Z'; i++) {
            char[] cc = {(char) i };
            encodingtable[i] = new String(cc);
        }

        for (int i = 'a'; i <= 'z'; i++) {
            char[] cc = {(char) i };
            encodingtable[i] = new String(cc);
        }

        //        for(int i = 0; i < 256; i++) {
        //            System.out.println(encodingtable[i]);
        //        }
    }

    /**
     * encodes text as 'application/x-www-form-urlencoded' text.
     * See http://www.w3.org/TR/REC-html40/interact/forms.html#form-content-type
     * use this for encoding text being used for a GET/POST request 
     */
    public static String URLEncodeText(String data) {
        int len = data.length();
        StringBuffer ret = new StringBuffer(len);
        int i;

        for (i = 0; i < len; i++) {
            char c = data.charAt(i);
            ret.append(encodingtable[c]);
        }

        return ret.toString();
    }
}
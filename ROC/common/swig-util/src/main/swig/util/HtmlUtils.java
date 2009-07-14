/* 
 * Modification: This class moved from ninja.util package to swig.util
 *   package for convenience.  Emre Kiciman <emrek@cs.stanford.edu>
 *
 *
 * Author: Mike Chen <mikechen@cs.berkeley.edu>
 * Inception Date: April 16th, 1999
 *
 * This software is copyrighted by Mike Chen and the Regents of
 * the University of California.  The following terms apply to all
 * files associated with the software unless explicitly disclaimed in
 * individual files.
 * 
 * The authors hereby grant permission to use this software without
 * fee or royalty for any non-commercial purpose.  The authors also
 * grant permission to redistribute this software, provided this
 * copyright and a copy of this license (for reference) are retained
 * in all distributed copies.
 *
 * For commercial use of this software, contact the authors.
 * 
 * IN NO EVENT SHALL THE AUTHORS OR DISTRIBUTORS BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE, ITS DOCUMENTATION, OR ANY
 * DERIVATIVES THEREOF, EVEN IF THE AUTHORS HAVE BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * THE AUTHORS AND DISTRIBUTORS SPECIFICALLY DISCLAIM ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT.  THIS SOFTWARE
 * IS PROVIDED ON AN "AS IS" BASIS, AND THE AUTHORS AND DISTRIBUTORS HAVE
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS.
 */

//==============================================================================
//===   HtmlUtils.java   ==============================================
package swig.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

/**
 * 
 * Some Html munging utilities.
 *
 * <PRE>
 * Revisions:  0.1.0  04-16-1999
 *                    Created class. 
 * </PRE>
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A> 
 *        (<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @since   1.1.6
 * @version Version 0.1.0, 04-16-1999
 */
public final class HtmlUtils {
    //===========================================================================
    //===   CONSTANTS   =========================================================
    private static final String CLASS_NAME = "HtmlUtils";
    private static final String VERSION = "v0.1";

    //===   CONSTANTS   =========================================================
    //===========================================================================
    //===========================================================================
    //===   UTIL METHODS    =====================================================

    /**
     * Preserves text formatting by converting newlines and linebreaks into <BR>.
     *
     */
    public static String preserveTextFormat(String text) {
        int length = text.length();
        int begin = 0;
        StringBuffer buf = new StringBuffer(length);

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            if ((c == 10) || (c == 13)) {
                buf.append(text.substring(begin, i));
                buf.append("<BR>");
                begin = i;
            }
        }

        if (begin != length) {
            buf.append(text.substring(begin, length));
        }

        return buf.toString();
    }

    // simple implemtation, but slower
    public static String preserveTextFormatSlow(String text) {
        int length = text.length();
        StringBuffer buf = new StringBuffer(length);

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            if ((c == 10) || (c == 13)) {
                buf.append("<BR>\n");
            }
            else {
                buf.append(c);
            }
        }

        return buf.toString();
    }

    /**
     * Convert chars that don't displayed correctly in browsers into proper Html.
     * (e.g. "<" into "&lt;")
     */
    public static String sanitizeHtml(String text) {
        int length = text.length();
        StringBuffer buf = new StringBuffer(length);
        int begin = 0;

        for (int i = 0; i < length; i++) {

            switch (text.charAt(i)) {
                case '<' :

                    if (i != 0) {
                        buf.append(text.substring(begin, i));
                    }

                    buf.append("&lt;");
                    begin = i + 1;

                    break;
                case '>' :

                    if (i != 0) {
                        buf.append(text.substring(begin, i));
                    }

                    buf.append("&gt;");
                    begin = i + 1;

                    break;
            }
        }

        if (begin != length) {
            buf.append(text.substring(begin, length));
        }

        return buf.toString();
    }

    public static String sanitizeHtmlSlow(String text) {
        int length = text.length();
        StringBuffer buf = new StringBuffer(length);

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            if (c == '<') {
                buf.append("&lt;");
            }
            else if (c == '>') {
                buf.append("&gt;");
            }
            else {
                buf.append(c);
            }
        }

        return buf.toString();
    }

    public static String includeFile(String filename) throws IOException {
        File file;
        FileInputStream in;
        System.out.println("reading file " + filename);

        try {
            file = new File(filename);
            in = new FileInputStream(file);
        }
        catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();

            return ("can't find file: " + filename);
        }

        int length = (int) file.length();
        byte[] buf = new byte[length];
        int offset = 0;
        int remainingLength = length;
        int bytesRead = 0;

        while (remainingLength != 0) {
            bytesRead = in.read(buf, offset, remainingLength);
            offset += bytesRead;
            remainingLength -= bytesRead;
        }

        return new String(buf);
    }

    public static String includeFileSlow(String filename) throws IOException {
        File file;
        BufferedReader in;
        System.out.println("reading file " + filename);

        try {
            file = new File(filename);
            in = new BufferedReader(new FileReader(file));
        }
        catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();

            return ("can't find file: " + filename);
        }

        StringBuffer buf = new StringBuffer((int) file.length());
        String line;

        while ((line = in.readLine()) != null) {
            buf.append(line);
            buf.append("\n");
        }

        return buf.toString();
    }

    //===   UTIL METHODS    =====================================================
    //===========================================================================
    //===========================================================================
    //===   MAIN    =============================================================
    public static void main(String[] args) {
        //// self-testing
        System.out.println(
            "\nWelcome to "
                + CLASS_NAME
                + "/"
                + VERSION
                + " <mikechen@cs.berkeley.edu>");
        System.out.println(new Date());
        System.out.println("\n");

        /* 
        if (args.length != 1) {
          System.err.println("\nusage: java " + CLASS_NAME + " seed");
          return;
        }
        */

        //// get a large text file (37KB)
        String text = "";
        long begin = 0;
        long end = 0;

        try {
            begin = System.currentTimeMillis();
            text =
                includeFile("/project/cs/ninja/a/home/mikechen/rfc/rfc2068-http1.1.txt");
            end = System.currentTimeMillis();
            System.out.println(
                "fast includeFile(), time: "
                    + (end - begin)
                    + " length: "
                    + text.length());

            begin = System.currentTimeMillis();
            text =
                includeFileSlow("/project/cs/ninja/a/home/mikechen/rfc/rfc2068-http1.1.txt");
            end = System.currentTimeMillis();
            System.out.println(
                "slow includeFile(), time: "
                    + (end - begin)
                    + " length: "
                    + text.length());

            //text = includeFile("/project/cs/ninja/a/home/mikechen/World/tmp/good.html");
        }
        catch (Exception e) {
            e.printStackTrace();

            return;
        }

        String result;

        begin = System.currentTimeMillis();
        result = preserveTextFormat(text);
        end = System.currentTimeMillis();
        System.out.println(
            "fast preserveTextFormat(), time: "
                + (end - begin)
                + " length: "
                + result.length());

        begin = System.currentTimeMillis();
        result = preserveTextFormatSlow(text);
        end = System.currentTimeMillis();
        System.out.println(
            "slow preserveTextFormat(), time: "
                + (end - begin)
                + " length: "
                + result.length());

        begin = System.currentTimeMillis();
        result = sanitizeHtml(text);
        end = System.currentTimeMillis();
        System.out.println(
            "fast sanitizeHtml(), time: "
                + (end - begin)
                + " length: "
                + result.length());

        //System.out.println(result);
        begin = System.currentTimeMillis();
        result = sanitizeHtmlSlow(text);
        end = System.currentTimeMillis();
        System.out.println(
            "slow sanitizeHtml(), time: "
                + (end - begin)
                + " length: "
                + result.length());

        //System.out.println(result);
        //// result: on a RedHat Linux PII-400 box

        /*
           fast includeFile(), time: 374 length: 378114
           slow includeFile(), time: 801 length: 378114
           fast preserveTextFormat(), time: 568 length: 414414
           slow preserveTextFormat(), time: 1056 length: 414414
           fast sanitizeHtml(), time: 366 length: 378549
           slow sanitizeHtml(), time: 998 length: 378549
        */
    }

    //===   MAIN    =============================================================
    //===========================================================================
} // of Class HtmlUtils
//===   HtmlUtils.java   =============================================
//=============================================================================

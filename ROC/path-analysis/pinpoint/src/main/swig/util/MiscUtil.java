/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * This class contains a set of static method utilities.
 *
 * @author <a href="http://www.cs.stanford.edu/~ach">
 *        Andy Huang</a> - ach@cs.stanford.edu 
 **/
public class MiscUtil {
    /**
     * Executes the given command line on the host machine.  
     **/
    public static void execCommand(String commandLine) throws IOException {
        Runtime r = Runtime.getRuntime();
        Process p = r.exec(commandLine);

        InputStream is;
        is = p.getInputStream();

        int c;

        while ((c = is.read()) != -1) {
            System.out.write(c);
        }
    }

    /**
     * Returns a time-stamp in format: MM-DD-YY_HH-MM-SS-MS
     **/
    public static String getTimeStamp() {
        GregorianCalendar time = new GregorianCalendar();

        String year = (new Integer(time.get(Calendar.YEAR))).toString();

        int monthInt = time.get(Calendar.MONTH) + 1;
        String month = String.valueOf(monthInt);

        if (monthInt < 10) {
            month = "0" + month;
        }

        int dateInt = time.get(Calendar.DATE);
        String date = String.valueOf(dateInt);

        if (dateInt < 10) {
            date = "0" + date;
        }

        int hourInt = time.get(Calendar.HOUR_OF_DAY);
        String hour = String.valueOf(hourInt);

        if (hourInt < 10) {
            hour = "0" + hour;
        }

        int minInt = time.get(Calendar.MINUTE);
        String min = String.valueOf(minInt);

        if (minInt < 10) {
            min = "0" + min;
        }

        int secInt = time.get(Calendar.SECOND);
        String sec = String.valueOf(secInt);

        if (secInt < 10) {
            sec = "0" + sec;
        }

        int msInt = time.get(Calendar.MILLISECOND);
        String ms = String.valueOf(msInt);

        if (msInt < 100) {
            ms = "0" + ms;

            if (msInt < 10) {
                ms = "0" + ms;
            }
        }

        return year
            + "-"
            + month
            + "-"
            + date
            + "_"
            + hour
            + "-"
            + min
            + "-"
            + sec
            + "-"
            + ms;
    }
}
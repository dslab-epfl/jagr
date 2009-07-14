/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * $Id: ArgHelper.java,v 1.1 2003/04/22 20:09:37 emrek Exp $
 *
 * $Log: ArgHelper.java,v $
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
 * Revision 1.4  2002/08/19 06:49:43  emrek
 * Added copyright information to source files
 *
 * Revision 1.3  2002/08/15 22:08:07  emrek
 * formatting changes (only) because of new editor
 *
 * Revision 1.2  2002/07/23 01:41:40  emrek
 * *** empty log message ***
 *
 * Revision 1.1.1.1  2002/07/17 09:07:47  emrek
 *
 *
 * Revision 1.1.1.1  2001/10/17 00:53:42  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.1  2001/02/27 09:46:50  emrek
 * new helper class for parsing arguments to programs
 *
 *
 */
public class ArgHelper {
    public static final String EXTRA_ARGS = "__EXTRA_ARGS";
    public static final String LONG_MARKER = "--";
    public static final String SHORT_MARKER = "-";

    /** 
     * prints the usage of this program
     * 
     * cmd is the name of the program to run
     * description is a description of what the program does
     * options is a list of command-line options
     * extra determines whether arguments other than options can be given
     *   at the end of the command
     *
     */
    public static String UsageInfo(
        String cmd,
        String description,
        List options,
        boolean extra) {
        String usage = "Usage: " + cmd;
        String ops = "";

        if ((options != null) && (options.size() > 0)) {
            usage += " [options]";

            Iterator iter = new ArrayList(options).iterator();

            while (iter.hasNext()) {
                ArgOption op = (ArgOption) iter.next();
                String s = "\t";

                if ((op.shortname != null) && (op.longname != null)) {
                    s
                        += (SHORT_MARKER
                            + op.shortname
                            + ", "
                            + LONG_MARKER
                            + op.longname);
                }
                else if (op.longname != null) {
                    s += (LONG_MARKER + op.longname);
                }
                else if (op.shortname != null) {
                    s += (SHORT_MARKER + op.shortname);
                }
                else {
                    Debug.AssertNotReached();
                }

                if (op.requiredArg) {
                    s += " X";
                }
                else if (op.optionalArg) {
                    s += " [X]";
                }

                s += "\t";

                if (op.description != null) {
                    s += op.description;
                }

                s += "\n";
                ops += s;
            }
        }

        if (extra) {
            usage += "...";
        }

        usage += "\n";

        if (description != null) {
            usage += ("\n" + description);
        }

        if (options != null) {
            usage += ("\n" + ops);
        }

        return usage;
    }

    /**
     * parses command line arguments.  Arguments can have the form
     * 
     *
     *
     */
    public static HashMap ParseArgs(
        String[] argv,
        List options,
        boolean extra) {
        HashMap opmap = new HashMap();
        HashMap ret = new HashMap();

        {
            Iterator iter = options.iterator();

            while (iter.hasNext()) {
                ArgOption op = (ArgOption) iter.next();

                if (op.longname != null) {
                    opmap.put(op.longname, op);
                }

                if (op.shortname != null) {
                    opmap.put(op.shortname, op);
                }

                if (op.defaultvalue != null) {
                    if (op.isInt) {
                        ret.put(op.longname, new Integer(op.defaultvalue));
                    }
                    else {
                        ret.put(op.longname, op.defaultvalue);
                    }
                }
            }
        }

        int i;

        for (i = 0; i < argv.length; i++) {
            String s = argv[i];
            ArgOption op = null;

            if (s.startsWith(LONG_MARKER)) {
                String longname = s.substring(LONG_MARKER.length());
                op = (ArgOption) opmap.get(longname);
                Debug.Assert((op != null) && (op.longname.equals(longname)));
            }
            else if (s.startsWith(SHORT_MARKER)) {
                String shortname = s.substring(SHORT_MARKER.length());
                op = (ArgOption) opmap.get(shortname);
                Debug.Assert((op != null) && (op.shortname.equals(shortname)));
            }
            else {
                // we must be done parsing options, go on to extra arguments
                break;
            }

            Object arg = null;

            if (((op.requiredArg) || (op.optionalArg))
                && (i + 1 < argv.length)) {
                if (!(argv[i + 1].startsWith(LONG_MARKER))
                    && !(argv[i + 1].startsWith(SHORT_MARKER))) {
                    arg = argv[i + 1];
                    i++;

                    if (op.isInt) {
                        arg = new Integer((String) arg);
                    }
                }
            }

            if ((op.requiredArg) && (arg == null)) {
                Debug.AssertNotReached();
            }

            ret.put(op.longname, arg);
        }

        if (extra) {
            // parse extra arguments
            String[] x = new String[argv.length - i];
            int j = 0;

            for (; i < argv.length; i++, j++) {
                x[j] = argv[i];
            }

            ret.put(EXTRA_ARGS, x);
        }
        else {
            Debug.Assert(i == argv.length);
        }

        return ret;
    }
}
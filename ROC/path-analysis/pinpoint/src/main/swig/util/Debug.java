/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;

/*
 * $Id: Debug.java,v 1.2 2002/12/28 12:27:30 emrek Exp $
 *
 * $Log: Debug.java,v $
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
 * Revision 1.1.1.1  2002/07/17 09:07:50  emrek
 *
 *
 * Revision 1.1.1.1  2001/10/17 00:53:41  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.8  2001/06/21 20:31:20  emrek
 * added a "priority" option to Debug Messages, and updated swig.util classes
 * that use debug messages, as well as the HTTP Debug interface.
 *
 * Revision 1.7  2001/02/24 09:38:30  emrek
 * changed ParseDebugArgs to return array of unparsed arguments
 *
 * Revision 1.6  2001/02/07 21:41:57  ach
 * *** empty log message ***
 *
 * Revision 1.5  2000/10/17 12:18:34  emrek
 * added support for deepcopying hashsets,
 * added a couple more debug commands (printing exceptions, warnings, etc)
 *
 * Revision 1.4  2000/10/08 04:07:40  emrek
 * adding FileFinder, to support java-classpath style searching for files
 *
 * Revision 1.3  2000/02/12 00:59:54  ach
 * added the methods Enter and Exit
 *
 * Revision 1.1  2000/01/28 21:43:06  emrek
 * moved code from emrek/ to swig/
 *
 * Revision 1.2  2000/01/20 00:28:39  emrek
 * the path package continues to grow...
 *
 * Revision 1.1  1999/12/25 07:53:35  emrek
 * bare-bones checkin
 */

/**
 * The Debug package provides a selective printf debug mechansim.
 * Debug flags are specified at the command-line in the following way:
 *
 * <p><code>java MyApp -d=flag1,flag2,...</code></p><p>
 *
 * When the program is run like this all messages specified in the
 * application of the form <code>Debug.Print("flag1", [mesg])</code>,
 * <code>Debug.Enter("flag2", [mesg])</code>, and
 * <code>Debug.Exit("flag2", [mesg])</code> are printed out.
 **/
public class Debug {
    public static int HIGH_PRIORITY = 3;
    public static int MED_PRIORITY = 2;
    public static int LOW_PRIORITY = 1;
    static Hashtable args = new Hashtable();
    static boolean allflags = false;
    static int minpriority;
    static String tabs = "";
    public static DebugPrinter debugprinter = new DebugPrinter();

    /** 
     * Assertion checking. Throws Runtime exception with 'msg' text if
     * assertion fails
     */
    public static void Assert(boolean b, String msg) {
        if (!b) {
            throw new RuntimeException("Assert Failure: " + msg);
        }
    }

    /**
     * Assertion checking. Throws Runtime exception if assertion fails
     */
    public static void Assert(boolean b) {
        Assert(b, "");
    }

    /**
     * throws an assertion if this method is ever called
     */
    public static void AssertNotReached() {
        Assert(false);
    }

    /**
     * throws an assertion if this method is ever called
     */
    public static void AssertNotReached(String msg) {
        Assert(false, msg);
    }

    /**
     * Prints a warning message to the std err
     */
    public static void Warning(String flag, String s) {
        debugprinter.print(flag, MED_PRIORITY, "WARNING(" + flag + "): " + s);
    }

    /**
     * Prints a warning message to the std err
     */
    public static void Warning(String flag, Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        PrintThrowable(pw, t);
        pw.flush();
        String s = sw.toString();
        debugprinter.print(flag, MED_PRIORITY, "WARNING(" + flag + "): " + s);
    }

    /**
     * parse debug flags from command-line arguments.  Recognizes flags
     * of the type '--debug="flag1,flag2,..."' and '-d="flag1,..."'
     * Debug Flags are used to decide whether certain debugging code
     * is run. e.g., whether messages are printed or not.
     */
    public static String[] ParseDebugArgs(String[] argv) {
        String flag;
        ArrayList l = new ArrayList();

        for (int i = 0; i < argv.length; i++) {
            flag = null;

            if (argv[i].startsWith("-d=")) {
                flag = argv[i].substring("-d=".length());
            }
            else if (argv[i].startsWith("--debug=")) {
                flag = argv[i].substring("--debug=".length());
            }
            else {
                l.add(argv[i]);
            }

            if (flag != null) {
                while (flag.indexOf(",") != -1) {
                    String subflag = flag.substring(0, flag.indexOf(","));
                    subflag = subflag.trim();
                    AddFlag(subflag);
                    flag = flag.substring(flag.indexOf(",") + 1);
                }

                flag = flag.trim();
                AddFlag(flag);
            }
        }

        String[] ret = (String[]) l.toArray(new String[l.size()]);

        return ret;
    }

    public static void SetMinPriority(int minpriority) {
        Debug.minpriority = minpriority;
    }

    public static int GetMinPriority() {
        return minpriority;
    }

    /**
     * add a debug flag
     */
    public static void AddFlag(String flag) {
        args.put(flag, flag);
        Print(flag, "Debug flag " + flag + " added");
    }

    /**
     * All debugging flags will be turned on
     */
    public static void AddAllFlags() {
        allflags = true;
        Print("", "All debug flags turned on");
    }

    /**
     * All debugging flags will be turned off
     */
    public static void RemoveAllFlags() {
        Print("", "All debug flags turned off");
        allflags = false;
        args.clear();
    }

    /**
     * remove a debug flag
     */
    public static void RemoveFlag(String flag) {
        Print(flag, "Debug flag " + flag + " removed");
        args.remove(flag);
    }

    /**
     * returns true if a flag exists
     */
    public static boolean FlagExists(String flag) {
        return args.containsKey(flag) || allflags;
    }

    public static void Print(String flag, String msg) {
        Print(flag, HIGH_PRIORITY, msg);
    }

    /**
     * prints a message if the debug flag is set
     */
    public static void Print(String flag, int priority, String msg) {
        if (FlagExists(flag)) {
            debugprinter.print(
                flag,
                priority,
                tabs + "Debug(" + priority + "," + flag + "): " + msg);
        }
    }

    protected static void PrintThrowable(PrintWriter w, Throwable t) {
        if (t == null) {
            return;
        }

        if (t instanceof NestedException) {
            PrintThrowable(w, ((NestedException) t).getDetail());
        }

        t.printStackTrace(w);
    }

    public static void Print(String flag, Throwable t) {
        Print(flag, HIGH_PRIORITY, t);
    }

    /**
     * prints an exception onto the Debug Log
     */
    public static void Print(String flag, int priority, Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        PrintThrowable(pw, t);
        pw.flush();
        String s = sw.toString();
        Print(flag, priority, s);
    }

    /**
     * This method should be called upon entering a method. The result
     * is that the methodname parameter is printed out and all
     * subsequent calls to Print are indented. Note that there should
     * be a matching "Exit" call to remove the indentation.
     */
    public static void Enter(String flag, String methodname) {
        if (FlagExists(flag) && FlagExists("m")) {
            debugprinter.print(
                flag,
                LOW_PRIORITY,
                tabs + ">> Entering " + methodname);
            tabs = "   " + tabs;
        }
    }

    /**
     * This method should be called upon exiting a method. The result
     * is that the methodname parameter is printed out and the
     * indention caused by the matching "Enter" call is removed.
     */
    public static void Exit(String flag, String methodname) {
        if (FlagExists(flag) && FlagExists("m")) {
            tabs = tabs.substring(3);
            debugprinter.print(
                flag,
                LOW_PRIORITY,
                tabs + "<< Exiting " + methodname + "\n");
        }
    }
}
/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

/**
 *
 * $Id: DebugPrinter.java,v 1.2 2002/12/28 12:27:30 emrek Exp $
 *
 * $Log: DebugPrinter.java,v $
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
 * Revision 1.1.1.1  2001/10/17 00:53:42  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.2  2001/06/21 20:31:20  emrek
 * added a "priority" option to Debug Messages, and updated swig.util classes
 * that use debug messages, as well as the HTTP Debug interface.
 *
 * Revision 1.1  2000/10/08 04:09:09  emrek
 * separate Debug.java from debug message printing; added Html interface to Debug messages
 *
 *
 */
public class DebugPrinter {
    public void print(String flag, int priority, String msg) {
        System.err.println(msg);
    }
}
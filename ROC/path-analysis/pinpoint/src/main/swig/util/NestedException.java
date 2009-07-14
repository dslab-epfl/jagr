/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

/**
 *
 * $Id: NestedException.java,v 1.2 2002/12/28 12:27:30 emrek Exp $
 *
 * $Log: NestedException.java,v $
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
 * Revision 1.1.1.1  2001/10/17 00:53:41  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.1  2000/01/28 21:43:06  emrek
 * moved code from emrek/ to swig/
 *
 * Revision 1.1  2000/01/16 18:12:55  emrek
 * beginnings of path package
 *
 *
 */
public class NestedException extends Exception {
    public Throwable detail;

    public NestedException() {
    }

    public NestedException(String s) {
        super(s);
    }

    public NestedException(String s, Throwable t) {
        super(s + "; nested exception is: \n\t" + t.toString());
        detail = t;
    }

    public Throwable getDetail() {
        return detail;
    }
}
/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import swig.httpd.HttpExportedService;

/**
 *
 * $Id: DebugHttpService.java,v 1.3 2002/12/28 12:27:30 emrek Exp $
 *
 * $Log: DebugHttpService.java,v $
 * Revision 1.3  2002/12/28 12:27:30  emrek
 * no functional changes, just formatting and general cleanup. also did some javadoc'ing of roc.pinpoint.** classes.
 *
 * Revision 1.2  2002/12/23 09:22:59  emrek
 * *** empty log message ***
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
 * Revision 1.1.1.1  2001/10/17 00:53:48  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 *
 * This code is based on swig.util.ispace.DebugHttpService, but with some
 * small porting to the swig.httpd.* package (instead of Ninja iSpace Httpd)
 *
 */
public class DebugHttpService implements HttpExportedService {
    List allmsgs;
    HashMap msgs;

    public DebugHttpService() {
        allmsgs = new LinkedList();
        msgs = new HashMap();

        Debug.debugprinter = new DebugHttpPrinter();
        Debug.AddAllFlags();
        Debug.SetMinPriority(Debug.LOW_PRIORITY);
    }

    public synchronized void addMsg(String flag, int priority, String msg) {
        List l = (List) msgs.get(flag);

        if (l == null) {
            l = new LinkedList();
            msgs.put(flag, l);
        }

        Msg m = new Msg();
        m.priority = priority;
        m.msg = msg;
        m.flag = flag;
        l.add(m);

        allmsgs.add(m);
    }

    StringBuffer listToStringBuffer(List l, int minpriority) {
        StringBuffer ret = new StringBuffer();

        Iterator iter = l.iterator();

        while (iter.hasNext()) {
            Msg m = (Msg) iter.next();

            if (m.priority >= minpriority) {
                ret.append(m.msg);
                ret.append("\n");
            }
        }

        return ret;
    }

    public synchronized String getMsgs(String flag, int minpriority) {
        List l = (List) msgs.get(flag);

        if (l == null) {
            return "no msgs";
        }
        else {
            return listToStringBuffer(l, minpriority).toString();
        }
    }

    public synchronized String getMsgs(List flags, int minpriority) {
        StringBuffer ret = new StringBuffer();

        Iterator iter = allmsgs.iterator();

        while (iter.hasNext()) {
            Msg m = (Msg) iter.next();

            if (flags.contains(m.flag) && (m.priority >= minpriority)) {
                ret.append(m.msg);
                ret.append("\n");
            }
        }

        return ret.toString();
    }

    public synchronized String getAllMsgs(int priority) {
        return listToStringBuffer(allmsgs, priority).toString();
    }

    public synchronized void clearMsgs(String flag) {
        List l = (List) msgs.get(flag);

        if (l != null) {
            msgs.put(flag, null);
        }
    }

    public synchronized void clearAllMsgs() {
        msgs.clear();
        allmsgs = new LinkedList();
    }

    public static final String relurl = "/service/debug";

    protected void sendMenu(PrintWriter pw, String flag, int priority) {
        pw.println("<P>");

        // the view some msgs buttons
        pw.println(
            "<FORM action=\""
                + relurl
                + "\" method=get name=c>"
                + "Filter by flags: <input type=text "
                + "name=flag value=\""
                + flag
                + "\" >"
                + "<input type=hidden value=view name=cmd>"
                + "&nbsp|&nbsp;");
        pw.println(
            "Filter by priority: "
                + "<input type=radio "
                + ((priority == 3) ? "checked" : "")
                + " value=3 name=priority>High;"
                + "<input type=radio "
                + ((priority == 2) ? "checked" : "")
                + " value=2 name=priority>Med;"
                + "<input type=radio "
                + ((priority == 1) ? "checked" : "")
                + " value=1 name=priority>Low&nbsp;&nbsp;");
        pw.println("<input type=submit value=\"Show msgs\">");
        pw.println("</FORM>");

        pw.println("<BR>");

        // the clear all msgs button
        pw.println(
            "<FORM action=\""
                + relurl
                + "\" method=get name=a>"
                + "<input type=hidden name=cmd value=clear>"
                + "<input type=submit value=\"Clear all msgs\"></FORM>");

        pw.println("</P>");
    }

    protected void sendPageHeader(PrintWriter pw, String flag, int priority) {
        pw.println("<HTML>\n<HEAD><TITLE>swig.util.Debug</TITLE></HEAD>\n");
        pw.println("<BODY><H1>swig.util.Debug</H1><P>");
        sendMenu(pw, flag, priority);
        pw.println("<P><HR><P>");
    }

    protected void sendPageFooter(PrintWriter pw, String flag, int priority) {
        pw.println("<P><HR><P>");
        sendMenu(pw, flag, priority);
        pw.println("<P><HR><P>");
        pw.println(
            "Emre Kiciman: <A HREF=\"mailto:emrek@cs.stanford.edu\">emrek@cs.stanford.edu</A>");
        pw.println("</BODY></HTML>");
    }

    protected void sendFrontPage(PrintWriter pw) {
        sendPageHeader(pw, "", Debug.HIGH_PRIORITY);
        sendPageFooter(pw, "", Debug.HIGH_PRIORITY);
    }

    protected void sendMsgPage(PrintWriter pw, String flag, int priority) {
        sendPageHeader(pw, flag, priority);

        String m = null;

        List f = StringHelper.SeparateStrings(flag, ',');

        if (f.size() > 1) {
            m = HtmlUtils.sanitizeHtml(getMsgs(f, priority));
        }
        else {
            m = HtmlUtils.sanitizeHtml(getMsgs(flag, priority));
        }

        pw.println("<pre>");
        pw.println(m);
        pw.println("</pre>");

        sendPageFooter(pw, flag, priority);
    }

    protected void sendAllMsgsPage(PrintWriter pw, int priority) {
        sendPageHeader(pw, "", priority);
        String m = HtmlUtils.sanitizeHtml(getAllMsgs(priority));
        pw.println("<pre>");
        pw.println(m);
        pw.println("</pre>");
        sendPageFooter(pw, "", priority);
    }

    int getPriority(Map args) {
        String p = (String) args.get("priority");

        try {
            return Integer.parseInt(p);
        }
        catch (Exception e) {
            return Debug.HIGH_PRIORITY;
        }
    }

    protected void httpRespond(Map args, Map httpHeaders, PrintWriter pw)
        throws IOException {
        HttpUtils.sendHttpHeadersOK(pw, true);

        String c = (String) args.get("cmd");

        if (c == null) {
            c = "";
        }

        if (c.equals("clear")) {
            clearAllMsgs();
            sendFrontPage(pw);
        }
        else if (c.equals("view")) {
            String f = (String) args.get("flag");

            if ((f == null) || (f.length() == 0)) {
                sendAllMsgsPage(pw, getPriority(args));
            }
            else {
                sendMsgPage(pw, f, getPriority(args));
            }
        }
        else {
            sendFrontPage(pw);
        }

        pw.flush();
    }

    public void getURL(
        String theURL,
        String URLdata,
        String clientHostname,
        OutputStream os,
        InputStream is) {
        try {
            LineNumberReader r =
                new LineNumberReader(new InputStreamReader(is));
            Map headers = HttpUtils.getHttpHeaders(r);

            Map args = HttpUtils.parseURLEnc(URLdata, 1, URLdata.length() - 1);

            httpRespond(
                args,
                headers,
                new PrintWriter(new OutputStreamWriter(os)));
        }
        catch (Exception e) {
            System.out.println(
                "swig.util.DebugHttpService trapped exception=" + e);
            e.printStackTrace();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
            sendFrontPage(pw);
            pw.flush();
        }
    }

    public void postURL(
        String theURL,
        String URLdata,
        String clientHostname,
        OutputStream os,
        InputStream is) {
        // do nothing
    }

    class DebugHttpPrinter extends DebugPrinter {
        public void print(String flag, int priority, String msg) {
            addMsg(flag, priority, msg);
        }
    }

    class Msg {
        String flag;
        int priority;
        String msg;
    }
}

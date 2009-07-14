
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: EmulatorServlet.java,v 1.1.1.1 2002/11/16 05:35:30 emrek Exp $
 *
 * 1/4/2001 - Hogstrom - Added code to ensure serialized access to transaction counter.
 *            $MRH-001
 */
package com.sun.ecperf.supplier.emulator;


//Import statements
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;

import org.w3c.dom.Document;

import org.xml.sax.*;

import org.w3c.dom.*;

import org.apache.xerces.parsers.*;
import org.apache.xerces.dom.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.supplier.helper.*;


/**
 *  This is the HTTP servlet for the Supplier Emulator.
 *  It is external to the SUT, and must operate in a web server
 *  that can handle Secure Sockets (Eventually!!).
 *
 *  The doPost method is implemented so that Purchase Orders can
 *  be uploaded to the Emulator.
 *
 * @author Damian Guy
 */
public class EmulatorServlet extends HttpServlet {

    /* Debug reference is static so a change in the log
     * target will affect logging for all instances of the
     * servlet. Luckily, debug only accesses some of it's
     * instance variables and reading it only, so race
     * conditions are not an issue and performance should
     * not be impacted.
     */
    private static Debug  debug;
    protected boolean  debugging;
    private static LogManager logMgr;
    private static int txCounter;
    private static int sleepTime = 60000;

    private Map deliveryConfig;

    /**
     * Method init
     *
     *
     */
    public void init() {

        deliveryConfig       = new HashMap();

        ServletConfig config = getServletConfig();

        deliveryConfig.put("supplierHost", config.getInitParameter(
            "supplier.host"));
        deliveryConfig.put("supplierPort", new Integer(
            config.getInitParameter("supplier.port")));
        String supplierServlet = config.getInitParameter("supplier.servlet");

        // A new env PREFIX is added which will include a "/" at the 
        // start which should be removed. RFE 4491953
        if(supplierServlet.startsWith("/"))
            supplierServlet = supplierServlet.substring(1);

        deliveryConfig.put("supplierServlet", supplierServlet);

        deliveryConfig.put("deliveryDTD", config.getInitParameter(
            "deliveryDTD.location"));

        /* Make sure we initialize these only once */

        if (debug == null) {
            int debugLevel =
                Integer.parseInt(config.getInitParameter("debuglevel"));

            if (debugLevel > 0) {
                debug = new DebugPrint(debugLevel, this);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }
        }

        deliveryConfig.put("debugging", new Boolean(debugging));
        deliveryConfig.put("debug", debug);

        int maxPoolSize = Integer.MAX_VALUE;   // Practically unlimited.
        String maxPoolSizeString = config.getInitParameter(
                                   "scheduler.maxWorkerPoolSize");
        if (maxPoolSizeString != null) {
            try {
                maxPoolSize = Integer.parseInt(maxPoolSizeString);
            } catch (NumberFormatException e) {
                debug.println(0,
                "scheduler.maxWorkerPoolSize not an integer, set to default");
            }
        }

        int threadTimeOut = 120 * 1000;     // 120 seconds
        String threadTimeOutString = config.getInitParameter(
                                     "scheduler.threadTimeOut");
        if (threadTimeOutString != null) {
            try {
                threadTimeOut = Integer.parseInt(threadTimeOutString) * 1000;
            } catch (NumberFormatException e) {
                debug.println(0,
                "scheduler.threadTimeOut not an integer, set to default");
            }
        }

        deliveryConfig.put("scheduler", new Scheduler(maxPoolSize,
            threadTimeOut, debugging, debug));

        int retryInterval = 5 * 1000;     // 5 seconds
        String retryIntervalString = config.getInitParameter(
                                     "delivery.retryInterval");
        if (retryIntervalString != null) {
            try {
                retryInterval = Integer.parseInt(retryIntervalString) * 1000;
            } catch (NumberFormatException e) {
                debug.println(0,
                "delivery.retryInterval not an integer, set to default");
            }
        }

        deliveryConfig.put("retryInterval", new Integer(retryInterval));


        int maxRetries = 10;     // 5 seconds
        String maxRetriesString = config.getInitParameter(
                                     "delivery.maxRetries");
        if (maxRetriesString != null) {
            try {
                maxRetries = Integer.parseInt(maxRetriesString);
            } catch (NumberFormatException e) {
                debug.println(0,
                "delivery.maxRetries not an integer, set to default");
            }
        }

        deliveryConfig.put("maxRetries", new Integer(maxRetries));

        if (logMgr == null)
            logMgr = new LogManager(2, debug, config);

        if (debugging)
            debug.println(3, "Initialized");
    }

    /**
     * This servlet's get call takes the parameters
     * cmd=getlog, cmd=switchlog, sleeptime=<millis>
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException {

        String okMsg = "200 OK\n";
	String errorMsg = "400 Error\n";
	String responseString = "";
        boolean responded = false;
        ServletOutputStream responseStream = null;

        try {
            responseStream = response.getOutputStream();
        } catch (IOException e) {
            getServletConfig().getServletContext().log(
                               "Error getting response OutputStream", e);
            throw new ServletException(e);
        }

        int paramCount = 0;

        for (Enumeration enum = request.getParameterNames();
             enum.hasMoreElements(); ) {
            String paramName = (String) enum.nextElement();
            ++paramCount;

            String[] paramValues = request.getParameterValues(paramName);

            for (int i = 0; i < paramValues.length; i++)

                if ("cmd".equals(paramName)) {
                    if ("switchlog".equals(paramValues[i]))
                        try {
                            logMgr.switchLog();
                            txCounter = 0;
                            responseString = "";
                        } catch (IOException e) {
                            responseString = errorMsg + '\n'
                                             + e.getMessage();
                        }
                    else if ("getlog".equals(paramValues[i]))
                        try {
                            logMgr.writeLog(responseStream);
                            responded = true;
                        } catch (IOException e) {
                            responseString = errorMsg + '\n'
                                             + e.getMessage();
                        }
                    else if ("getcount".equals(paramValues[i]))
                        try {
                            responseStream.println("TxCount = " + String.valueOf(txCounter) + " ; ");
                            responseString = "";
                        } catch (IOException e) {
                            responseString = errorMsg + '\n'
                                             + e.getMessage();
                        }
                    else
                        responseString = errorMsg + '\n' +
                                         "Unrecognized command: " + 
                                         paramValues[i];
                } else if ("sleeptime".equals(paramName))
                    try {
                        int tm = Integer.parseInt(paramValues[i]);
                        if (tm < 0)
                            responseString = errorMsg +
                                             "\n sleep < 0 invalid!";
                        else {
                            sleepTime = tm;
                            responseString = "";
                        }
                    } catch (NumberFormatException e) {
                        responseString = errorMsg + '\n'
                                         + e.getMessage();
                    }
                else
                    responseString = errorMsg + '\n' +
                                     "Unrecognized command: " + 
                                     paramName;
        }

        try {
            if (paramCount == 0) {
                response.setContentType("text/html");
                responseStream.println("<html><head><title>EmulatorServlet " +
                    "Test Page</title></head><body bgcolor=#ffffff><center>" +
                    "<font size=+2>Emulator Servlet seems to work OK</font><br>" +
                    "<font size=+2>ECPERF_HOST : " + deliveryConfig.get(
                    "supplierHost") + "</font><br>" +
                    "<font size=+2>ECPERF_PORT : " + deliveryConfig.get(
                    "supplierPort") + "</font><br>" +
                    "<font size=+2>Servlet URL : " + deliveryConfig.get(
                    "supplierServlet") + "</font><br><br>" +
                    "<font size=+2>Number of Transactions : " +
                    String.valueOf(txCounter) + "</font><br>" +
                    "<font size=+1>Servlet invoked without command specified" +
                    "</font></center></body></html>");
            } else if (responseString.length() > 0) {
                if (responded)
                    responseStream.println("");
                responseStream.print(responseString);
            } else
                if (!responded)
                    responseStream.print(okMsg);

                responseStream.close();

        } catch (IOException e) {
            debug.printStackTrace(e);
        }
    }

    /**
     * doPost: responds to the POST command.
     * Receives Purchase Order in XML, parses and process
     * the Purchase Order.
     * @param request
     * @param response
     * @exception ServletException
     * @exception IOException
     */
    public void doPost(
            HttpServletRequest request, HttpServletResponse response)
                throws ServletException {

        if (debugging)
            debug.println(3, "doPost");

        // $MRH-001 - used static debug object to serialize access to static
        //            transaction Counter.
        synchronized(debug) {
          txCounter++;
        }

        PrintWriter   out    = null;
        String        order  = request.getParameter("xml");
        MyDOMParser   parser = new MyDOMParser();
        PurchaseOrder po     = null;
        boolean       ok     = false;

        try {
            out = response.getWriter();
        } catch (IOException e) {
            getServletConfig().getServletContext().log(
                               "Error getting response Writer", e);
            throw new ServletException(e);
        }

        try {
            Document xmlDoc =
                parser.parse(new InputSource(new StringReader(order)));

            po = new PurchaseOrder(xmlDoc, deliveryConfig);

	    String okmsg = "200 OK\n";
            // Using println and so length should inlude \n
	    response.setContentLength(okmsg.length());
            out.print(okmsg);
            out.close();
            if (debugging)
                debug.println(3, "Response sent OK");

            ok = true;
        } catch (Exception e) {

            /** Probably could use better response code! **/
            if (debugging)
                debug.println(1, e.getMessage());
            debug.printStackTrace(e);
	    String errmsg = "409 Conflict invalid XML format\n";
	    response.setContentLength(errmsg.length());
            out.print(errmsg);
            if (debugging)
                debug.println(3, "Response sent Error 409");
            out.close();
        }

        try {
            if(ok)
                /* Process the PO and schedule the delivery */
                po.processPO();
        } catch (Exception e) {
            if (debugging)
                debug.println(1, e.getMessage());
            debug.printStackTrace(e);

            throw new ServletException(e);
        }
    }
}


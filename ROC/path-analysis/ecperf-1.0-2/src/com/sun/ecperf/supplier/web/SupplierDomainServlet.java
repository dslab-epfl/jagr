/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: SupplierDomainServlet.java,v 1.1.1.1 2002/11/16 05:35:31 emrek Exp $
 * 1/4/2001 - Hogstrom - Added code to ensure serialized access to transaction counter.
 *            $MRH-001
 *
 */

package com.sun.ecperf.supplier.web;


//Import statements
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.rmi.*;
import javax.ejb.*;
import javax.naming.*;
import java.rmi.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.dom.*;
import com.sun.ecperf.supplier.receiverses.ejb.*;
import com.sun.ecperf.supplier.helper.*;
import com.sun.ecperf.common.*;

/**
 * This is the Servlet in the Supplier Domain that the
 * Emulator contacts to deliver orders.
 *
 * @author Damian Guy
 */
public class SupplierDomainServlet extends HttpServlet {

	private static Debug debug;
        protected boolean debugging;
        private static LogManager logMgr;
        private static int txCounter;
	private ReceiverSesHome receiverHome;


	/**
	 * get Receiver home.
	 */ 
	public void init() throws ServletException {
		 
		Context context = null;
		try {
			context = new InitialContext();
		}
		catch (Exception e) {
			System.err.println("Cannot create Initial Context!");
			e.printStackTrace(System.err);
			throw new ServletException(e);
		}

                if (debug == null) {
		    int debugLevel = 0;
		    try {
                        debugLevel = ((Integer) context.lookup(
                                     "java:comp/env/debuglevel")).intValue();
                        if (debugLevel > 0) {
                            debug = new DebugPrint(debugLevel, this);
                            debugging = true;
                        } else {
                            debug = new Debug();
                            debugging = false;
                        }
		    }
                    catch (Exception e) { }
		    debug = debugLevel > 0 ? new DebugPrint(debugLevel,
                                         this) : new Debug();
                }

                if (logMgr == null)
                    logMgr = new LogManager(2, debug, getServletConfig());

		if (debugging)
                    debug.println(3, "init");

		try {
			receiverHome = (ReceiverSesHome)javax.rmi
                                .PortableRemoteObject.narrow(context.lookup(
                                "java:comp/env/ejb/ReceiverSes"),
                                ReceiverSesHome.class);
		} catch(NamingException ne) {
			if (debugging)
                            debug.println(1, "Supp Init() : " + ne.getMessage());
			ne.printStackTrace(System.err);
			throw new ServletException(ne);
		}
	}
		
    /**
     * This servlet's get call takes the parameters
     * cmd=getlog, cmd=switchlog, sleeptime=<millis>
     * cmd=getcount to get the # Tx 
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
                            // Reset the counter
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
                            responseStream.println("TxCount = " + String.valueOf(txCounter)+ " ; ");
                            responseString = "";
                        } catch (IOException e) {
                            responseString = errorMsg + '\n'
                                             + e.getMessage();
                        }
                    else
                        responseString = errorMsg + '\n' +
                                         "Unrecognized command: " + 
                                         paramValues[i];
                } else
                    responseString = errorMsg + '\n' +
                                     "Unrecognized command: " + 
                                     paramName;
        }

        try {
            if (paramCount == 0) {
                response.setContentType("text/html");
                responseStream.println("<html><head><title>DeliveryServlet " +
                    "Test Page</title></head><body bgcolor=#ffffff><center>" +
                    "<font size=+2>Servlet seems to work OK</font><br>" +
                    "<font size=+2>Number of Transactions : " + String.valueOf(txCounter) + " </font><br>" +
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
	 * POST method - parse XML and deliver goods to the supplier domain
	 */
	public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
                throws ServletException, IOException {

		if (debugging)
                    debug.println(3, "doPost");
                // Increment TX count
                
                // $MRH-001 used static debug object to synchronize access to txCounter.
                synchronized(debug) {
                  txCounter++;
                }

                String delivery = request.getParameter("xml");

		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();	
		
		MyDOMParser parser = new MyDOMParser();

		Document xmlDoc = null;
		boolean ok = true;

		try{
			xmlDoc = parser.parse(new InputSource(
					new StringReader(delivery)));

		} catch(Exception pe) {
                        ok = false;
			if (debugging)
                            debug.println(2, pe.getMessage());
			debug.printStackTrace(pe);
                        sendError("409 Conflict invalid XML format",
                                  response, out);
		}

		if (ok) {
		    try {
			int ponum = Integer.parseInt(getData(
				xmlDoc.getElementsByTagName(
				XmlTags.PONUMBER).item(0)));

			NodeList polines = xmlDoc.getElementsByTagName(
				XmlTags.POLINE);
			ReceiverSes receiver = receiverHome.create();

			for(int i=0;i<polines.getLength(); i++) {
			    DeliveryInfo delInfo = getLineData(
				polines.item(i), ponum);
			    int retries = 10;
			    for (int j = 0; j < retries; j++) {
				try {
				    receiver.deliverPO(delInfo);
				    break;
				} catch (RemoteException re) {
				    if (j == retries - 1) {
					re.printStackTrace(System.err);
                                        sendError("409 Processing Error",
                                                  response, out);
                                        ok = false;
				    }
				    else {
					if (debugging)
                                            debug.println(2,
					        "Remote exception, most likely " +
    					        "optimistic miss. Retrying " +
					        (j + 1));
				    }
				} catch (NotReadyException e) {
                                    if (debugging)
                                        debug.println(2, "Not ready: " +
                                                      e.getMessage());
                                    sendError("502 RETRY", response, out);
                                    ok = false;
                                }
			    }
			}
                        receiver.remove();
                    } catch (Exception e) {
			if (debugging)
                            debug.println(1, e.getMessage());
			e.printStackTrace(System.err);
                        sendError("409 " + e.getMessage(), response, out);
                        ok = false;
			throw new ServletException(e);
		    }
		}
                if (ok) {
                    String okmsg = "200 OK\n";
                    response.setContentLength(okmsg.length());
                    out.print(okmsg);
                    out.close();
                    if (debugging)
                        debug.println(3, "Response sent OK");
                }
	}

        public void sendError(String errmsg, HttpServletResponse response,
                              PrintWriter responseWriter) {
            errmsg += '\n';
            response.setContentLength(errmsg.length());
            responseWriter.print(errmsg);		
            responseWriter.close();
            if (debugging)
                debug.println(3, "Response sent: " + errmsg);
        }

	private DeliveryInfo getLineData(Node line, int ponum) {
                NodeList children = line.getChildNodes();

		int lnum  = Integer.parseInt(getData(children.item(0)));
		String part = getData(children.item(1));
		int qty = Integer.parseInt(getData(children.item(2)));

		return new DeliveryInfo(ponum,lnum,part,qty);
	}


	private String getData(Node node) {
		
		if(node.hasChildNodes()) {

			Node dataNode = node.getChildNodes().item(0);
			return dataNode.getNodeValue().trim();
		}
		return null;
	}

}

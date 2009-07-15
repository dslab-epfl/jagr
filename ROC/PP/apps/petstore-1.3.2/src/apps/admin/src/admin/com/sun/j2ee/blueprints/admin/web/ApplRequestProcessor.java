/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 * OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of
 * any nuclear facility.
 */

package com.sun.j2ee.blueprints.admin.web;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Date;
import java.util.StringTokenizer;
import com.sun.j2ee.blueprints.admin.web.AdminRequestBD;
import com.sun.j2ee.blueprints.opc.admin.ejb.OrdersTO;
import com.sun.j2ee.blueprints.opc.admin.ejb.OrderDetails;
import com.sun.j2ee.blueprints.xmldocuments.OrderApproval;
import com.sun.j2ee.blueprints.xmldocuments.ChangedOrder;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This servlet serves requests from the rich client
 */
public class ApplRequestProcessor extends HttpServlet {

    String replyHeader = "<?xml version=\"1.0\" standalone=\"yes\"?>\n" +
        "<Response>\n";

    // Servlet classes start here

    public void init() {}

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws java.io.IOException, javax.servlet.ServletException {
        doPost(req, resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws java.io.IOException, javax.servlet.ServletException {

        HttpSession session = req.getSession(false);
        ServletOutputStream out = resp.getOutputStream();

        // Check session; if null then there is no authentication; send error
        // If session is not null then the client has been authorised by
        // form based login mechanism
        if(session == null) {
            out.println(replyHeader +
                        "<Error>Session Timed Out; Please exit and " +
                        "login as admin from the login page</Error>\n" +
                        "</Response>\n");
        } else {
            String sId = session.getId();
            resp.setContentType("text/xml");
            BufferedReader inp = req.getReader();
            StringBuffer strbuf = new StringBuffer("");
            while(true) {
                String str = inp.readLine();
                if(str == null)
                    break;
                strbuf.append(str+"\n");
            }
            inp.close();
            out.println(processRequest(strbuf.toString()));
        }
        out.flush();
        out.close();
    }

    /**
     * This method processes the incoming XML doc and returns the response
     * @param String  the incoming XML request
     * @returns Stirng  the response in XML format
     */
    String processRequest(String xmlString) {
        try {
            InputSource source =
                new InputSource(new ByteArrayInputStream(xmlString.
                                                         getBytes()));
            DocumentBuilder db =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Element root =
                db.parse(source).getDocumentElement();
            String reqType = getValue(root.getElementsByTagName("Type")
                                      .item(0));
            if (reqType.equals("GETORDERS")) {
                return getOrders(root);
            }
            else if (reqType.equals("UPDATESTATUS")) {
                return updateOrders(root);
            }
            else if (reqType.equals("REVENUE") || reqType.equals("ORDERS")) {
                return getChartInfo(root, reqType);
            }
            else {
                return replyHeader +
                    "<Error>Unable to process request : " +
                    "Unknown request type - " + reqType +
                    "</Error>\n</Response>\n";
            }
        }
        catch (ParserConfigurationException pe) {
            return replyHeader +
                "<Error>Exception while processing :  " + pe.getMessage() +
                ". Please try again</Error>\n</Response>\n";
        }
        catch (SAXException se) {
            return replyHeader +
                "<Error>Exception while processing :  " + se.getMessage() +
                ". Please try again</Error>\n</Response>\n";
        }
        catch (IOException ie) {
            return replyHeader +
                "<Error>Exception while processing :  " + ie.getMessage() +
                ". Please try again</Error>\n</Response>\n";
        }
    }

    String getOrders(Element root) {
        try {
            AdminRequestBD bd = new AdminRequestBD();
            NodeList nl = root.getElementsByTagName("Status");
            String status = getValue(nl.item(0));
            OrdersTO orders = bd.getOrdersByStatus(status);
            if (orders == null) {
                return null;
            }

            StringBuffer responseBuffer = new StringBuffer(replyHeader);
            responseBuffer.append("<Type>GETORDERS</Type>\n");
            responseBuffer.append("<Status>" + status + "</Status>\n");
            responseBuffer.append("<TotalCount>" + orders.size() +
                                  "</TotalCount>\n" );
            for (Iterator it = orders.iterator(); it.hasNext(); ) {
                OrderDetails o = (OrderDetails) it.next();
                responseBuffer.append("<Order>\n");
                responseBuffer.append("<OrderId>" + o.getOrderId()
                                      + "</OrderId>\n");
                responseBuffer.append("<UserId>" + o.getUserId()
                                      + "</UserId>\n");
                responseBuffer.append("<OrderDate>" + o.getOrderDate()
                                      + "</OrderDate>\n");
                responseBuffer.append("<OrderAmount>" + o.getOrderValue()
                                      + "</OrderAmount>\n");
                responseBuffer.append("<OrderStatus>" + o.getOrderStatus()
                                      + "</OrderStatus>\n");
                responseBuffer.append("</Order>\n");
            }
            responseBuffer.append("</Response>\n");
            return responseBuffer.toString();
        }
        catch (AdminBDException e) {
            e.printStackTrace();
            return null;
        }
    }

    String updateOrders(Element root) {

        // a better solution would be using a style sheet to convert from
        // one schema to the other

        OrderApproval oa = new OrderApproval();

        NodeList nl = root.getElementsByTagName("Order");
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);

            // Assumes values of OrderStatus are the same for both schemas
            String orderId =
                getValue((Element) e.getElementsByTagName("OrderId").item(0));
            String orderStatus =
                getValue((Element) e.getElementsByTagName("OrderStatus").item(0));
            if (orderId != null && orderStatus != null) {
                oa.addOrder(new ChangedOrder(orderId, orderStatus));
            }
        }

        try {
            AdminRequestBD bd = new AdminRequestBD();
            bd.updateOrders(oa);
            return replyHeader + "<Type>UPDATEORDERS</Type>\n"
                + "<Status>SUCCESS</Status>\n"
                + "</Response>\n";
        } catch (AdminBDException e) {
            return replyHeader
                + "<Error>Exception while processing :  " + e.getMessage()
                + ". Please try again</Error>\n</Response>\n";
        }
    }

    String getChartInfo(Element root, String request) {
        try {

            Map chartInfo = null;
            String category = null;
            String start = getValue(root.getElementsByTagName("Start").item(0));
            String end = getValue(root.getElementsByTagName("End").item(0));

            AdminRequestBD bd = new AdminRequestBD();

            category =
                getValue(root.getElementsByTagName("ReqCategory").item(0));

            chartInfo = bd.getChartInfo(request,
                                        getProperDate(start),
                                        getProperDate(end),
                                        category);

            StringBuffer responseBuffer = new StringBuffer(replyHeader);
            String xmlElement = (category == null) ? "Category" : "Item";
            responseBuffer.append("<Type>" + request + "</Type>\n");
            responseBuffer.append("<Start>" + start + "</Start>\n");
            responseBuffer.append("<End>" + end + "</End>\n");
            responseBuffer.append(((category == null) ?
                                   ("<ReqCategory/>\n") :
                                   ("<ReqCategory>" + category + "</ReqCategory>\n")));
            if (request.equals("REVENUE")) {
                float totalAmount = 0f;
                for (Iterator it = chartInfo.entrySet().iterator();
                     it.hasNext(); ) {
                    Map.Entry e = (Map.Entry) it.next();
                    float amount = ((Float) e.getValue()).floatValue();
                    totalAmount += amount;
                    responseBuffer.append("<" + xmlElement + " name=\""
                                          + e.getKey() + "\">");
                    responseBuffer.append(amount);
                    responseBuffer.append("</" + xmlElement + ">\n");
                }
                responseBuffer.append("<TotalSales>");
                responseBuffer.append(totalAmount);
                responseBuffer.append("</TotalSales>\n");
            }
            // ORDERS
            else {
                int totalQuantity = 0;
                for (Iterator it = chartInfo.entrySet().iterator();
                     it.hasNext(); ) {
                    Map.Entry e = (Map.Entry) it.next();
                    int quantity = ((Integer) e.getValue()).intValue();
                    totalQuantity += quantity;
                    responseBuffer.append("<" + xmlElement + " name=\""
                                          + e.getKey() + "\">");
                    responseBuffer.append(quantity);
                    responseBuffer.append("</" + xmlElement + ">\n");
                }
                responseBuffer.append("<TotalSales>");
                responseBuffer.append(totalQuantity);
                responseBuffer.append("</TotalSales>\n");
            }
            responseBuffer.append("</Response>\n");
            return responseBuffer.toString();
        }
        catch (AdminBDException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method gets the value of a give node
     * @param n <Code>Node</Code>
     * @returns String that represents value of given Node
     */
    String getValue(Node n) {
        Node child = n.getFirstChild();
        if (child == null)
            return null;
        return child.getNodeValue();
    }

    Date getProperDate(String inpDate) {
        StringTokenizer tokens = new StringTokenizer(inpDate, "/");
        Integer month = new Integer((String)tokens.nextElement());
        Integer day = new Integer((String)tokens.nextElement());
        Integer year = new Integer((String)tokens.nextElement());
        return(new Date(year.intValue()-1900, month.intValue()-1,
                        day.intValue()));
    }
}

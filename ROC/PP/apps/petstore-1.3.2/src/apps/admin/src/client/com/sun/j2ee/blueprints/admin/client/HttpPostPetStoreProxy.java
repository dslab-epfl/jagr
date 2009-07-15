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
package com.sun.j2ee.blueprints.admin.client;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;

public class HttpPostPetStoreProxy implements PetStoreProxy {

    private static final String requestType =
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>\n";

    private URL url = null;
    private DocumentBuilder documentBuilder = null;
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public HttpPostPetStoreProxy() {
    }

    public void setup(String host, String port, String sessionID) {
        // TBD assert host, port
        try {
            url = new URL("http://" + host + ":" + port
                + "/admin/ApplRequestProcessor;jsessionid=" + sessionID);
        } catch(MalformedURLException e) {
            e.printStackTrace();
            // TBD deal with exception - rethrow to main()
        }

        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {
            e.printStackTrace();
            // TBD deal with exception - rethrow to main()
        }
    }

    private Document doHttpPost(String request) {
        InputStream ist = null;
        OutputStream ost = null;
        HttpURLConnection uc = null;

        try {
            /* Initialize the HttpURLConnection */

            uc = (HttpURLConnection)(url.openConnection());
            uc.setRequestMethod("POST");
            uc.setDoOutput(true);
            uc.setUseCaches(false);
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            uc.setRequestProperty("JSESSIONID", "HAHAHAHA");
            uc.connect();

            /* Send the post request */

            ost = uc.getOutputStream();
            DataOutputStream out = new DataOutputStream(ost);
            out.writeBytes(request);
            out.flush();
            out.close();
            ost = null;

            /* Read and print the response */

            ist = uc.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(ist));
            StringBuffer sb = new StringBuffer();
            char[] c = new char[1];
            while (in.read(c, 0, 1) == 1) {
                sb.append(c[0]);
            }

            InputSource src = new InputSource(new StringReader(sb.toString()));
            return documentBuilder.parse(src);
        } catch (Exception e) {
            if (uc != null) {
                uc.disconnect();
            }
            return null;
        } finally { // Carefully close the input and output streams
            if (ost != null) {
                try {
                    ost.close();
                }
                catch (IOException ignore) {
                }
            }
            if (ist != null) {
                try {
                    ist.close();
                }
                catch (IOException ignore) {
                }
            }
        }
    }

    private String genRequest(StringBuffer body) {
        StringBuffer sb = new StringBuffer(requestType);
        sb.append("<Request>\n");
        sb.append(body.toString());
        sb.append("</Request>\n");
        return sb.toString();
    }

    /**
     * Return the value of the first child of this node.  For example, given
     * a node like "<foo>bar</foo>", return "bar".
     */
    private String getBody(Node node) {
        try {
            return node.getFirstChild().getNodeValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Return the body of the first element with the specified tag.  We
     * assume that the element has one text node child.  If anything goes
     * wrong, return null.
     */
    private String getBody(Document doc, String tag) {
        return getBody(doc.getElementsByTagName(tag).item(0));
    }


    /**
     * Return the date value of the first child of this node.  See the Document
     * overload of this method for more information.
     */
    private Date getDate(Node node) {
        try {
            String s = getBody(node);
            return dateFormat.parse(s, new ParsePosition(0));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the Date value of the first element with the specified tag.
     * The body of the element is assumed to be a date in the ISO 8601 format
     * used by the ICalendar server.  For example: "20010331T000000Z", is interpreted
     * as "Fri Mar 30 16:00:00 PST 2001".  Note that the calendar start/end
     * dates are assumed to be in the GMT time zone.
     */
    private Date getDate(Document doc, String tag) {
        return getDate(doc.getElementsByTagName(tag).item(0));
    }


    /**
     * Return the integer value of the first child of this node.
     */
    private int getInt(Node node) {
        try {
            String s = getBody(node);
            return Integer.parseInt(s);
        }
        catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Return the float value of the first child of this node.
     */
    private float getFloat(Node node) {
        String s = null;
        try {
            s = getBody(node);
            return Float.parseFloat(s);
        }
        catch (Exception e) {
            e.printStackTrace();
            return -1.0f;
        }
    }


    /**
     * Return the body of the first element with the specified tag as
     * an integer.  We assume that the element has one text node child.
     * If anything goes wrong, return null.
     */
    private int getInt(Document doc, String tag) {
        return getInt(doc.getElementsByTagName(tag).item(0));
    }


    /**
     * Return the string value of the specified attribute.  For example
     * if node represents <FOO key="bar"/> then return "bar".
     */
    private String getAttribute(Node node, String key) {
        NamedNodeMap nodeMap = node.getAttributes();
        if (nodeMap != null) {
            Node item = nodeMap.getNamedItem(key);
            return (item != null) ? item.getNodeValue() : null;
        }
        else {
            return null;
        }
    }

    private String parseOrderStatus(String s) {
        if (s.equals("PENDING")) {
            return PetStoreProxy.Order.PENDING;
        }
        else if (s.equals("APPROVED")) {
            return PetStoreProxy.Order.APPROVED;
        }
        else if (s.equals("DENIED")) {
            return PetStoreProxy.Order.DENIED;
        }
        else if (s.equals("COMPLETED")) {
            return PetStoreProxy.Order.COMPLETED;
        }
        else {
            return null;
        }
    }

    private PetStoreProxy.Order parseOrder(Node orderNode) {
        String id = null;
        String userId = null;
        Date date = null;
        float amount = -1.0f;
        String status = null;
        NodeList children = orderNode.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String name = child.getNodeName();
            if (name.equals("OrderId")) {
                id = getBody(child);
            }
            else if (name.equals("UserId")) {
                userId = getBody(child);
            }
            else if (name.equals("OrderDate")) {
                date = getDate(child);
            }
            else if (name.equals("OrderAmount")) {
                amount = getFloat(child);
            }
            else if (name.equals("OrderStatus")) {
                status = parseOrderStatus(getBody(child));
            }
        }
        return new PetStoreProxy.Order(id, userId, date, amount, status);
    }


    public PetStoreProxy.Order[] getOrders(String status) {
        StringBuffer sb = new StringBuffer("<Type>GETORDERS</Type>");
        sb.append("<Status>");
        sb.append(status);
        sb.append("</Status>");
        Document doc = doHttpPost(genRequest(sb));
        NodeList orderNodes = doc.getElementsByTagName("Order");
        ArrayList orders = new ArrayList(orderNodes.getLength());
        for(int i = 0; i < orderNodes.getLength(); i++) {
            PetStoreProxy.Order order = parseOrder(orderNodes.item(i));
            if (order != null) {
                orders.add(order);
            }
        }
        return (PetStoreProxy.Order[])(orders.toArray(new PetStoreProxy.Order[orders.size()]));
    }


    /**
     * Append "<tag>value</tag>" to the StringBuffer if value is non-null,
     * "<tag/>" otherwise.
     */
    private void appendElement(StringBuffer sb, String tag, String value) {
        if (value != null) {
            sb.append("<");
            sb.append(tag);
            sb.append(">");
            sb.append(value);
            sb.append("</");
            sb.append(tag);
            sb.append(">\n");
        }
        else {
            sb.append("<");
            sb.append(tag);
            sb.append("/>");
        }
    }


    private String genSalesRequest(String type, Date start, Date end, String category) {
        // check start/end non null, etc.
        // check trimmed category isn't zero length
        StringBuffer sb = new StringBuffer();
        appendElement(sb, "Type", type);
        appendElement(sb, "Start", dateFormat.format(start));
        appendElement(sb, "End", dateFormat.format(end));
        appendElement(sb, "ReqCategory", category);
        return genRequest(sb);
    }


    public PetStoreProxy.Sales[] getRevenue(Date start, Date end, String category) {
        String request = genSalesRequest("REVENUE", start, end, category);
        Document doc = doHttpPost(request);
        String tag = (category == null) ? "Category" : "Item";
        NodeList salesNodes = doc.getElementsByTagName(tag);
        ArrayList sales = new ArrayList(salesNodes.getLength());
        for(int i = 0; i < salesNodes.getLength(); i++) {
            Node salesNode = salesNodes.item(i);
            String key = getAttribute(salesNode, "name");
            float revenue = getFloat(salesNode);
            if ((key != null) && (revenue >= 0.0)) {
                sales.add(new PetStoreProxy.Sales(key, revenue));
            }
        }
        return (PetStoreProxy.Sales[])(sales.toArray(new PetStoreProxy.Sales[sales.size()]));
    }

    public PetStoreProxy.Sales[] getOrders(Date start, Date end, String category) {
        String request = genSalesRequest("ORDERS", start, end, category);
        Document doc = doHttpPost(request);
        String tag = (category == null) ? "Category" : "Item";
        NodeList salesNodes = doc.getElementsByTagName(tag);
        ArrayList sales = new ArrayList(salesNodes.getLength());
        for(int i = 0; i < salesNodes.getLength(); i++) {
            Node salesNode = salesNodes.item(i);
            String key = getAttribute(salesNode, "name");
            int nOrders = getInt(salesNode);
            if ((key != null) && (nOrders >= 0)) {
                sales.add(new PetStoreProxy.Sales(key, nOrders));
            }
        }
        return (PetStoreProxy.Sales[])(sales.toArray(new PetStoreProxy.Sales[sales.size()]));
    }


    public void updateStatus(PetStoreProxy.Order[] orders, String status) {
        StringBuffer sb = new StringBuffer();
        appendElement(sb, "Type", "UPDATESTATUS");
        appendElement(sb, "TotalCount", Integer.toString(orders.length));
        for(int i = 0; i < orders.length; i++) {
            sb.append("<Order>");
            appendElement(sb, "OrderId", orders[i].getId());
            appendElement(sb, "OrderStatus", status);
            sb.append("</Order>");
        }
        Document doc = doHttpPost(genRequest(sb));
        if (!getBody(doc, "Status").equals("SUCCESS")) {
            // throw an exception
        }
    }

    public void updateStatus(PetStoreProxy.Order order, String status) {
        updateStatus(new PetStoreProxy.Order[]{order}, status);
    }

    /*
    public static void main(String[] args) throws Exception
    {
        String host = (args.length >= 1) ? args[0] : "localhost";
        String port = (args.length >= 2) ? args[1] : "8000";

        PetStoreHttpPostBD server = new PetStoreHttpPostBD(host, port);

        String statusValues[] = {PetStoreProxy.Order.PENDING, PetStoreProxy.Order.APPROVED};
        for(int i = 0; i < statusValues.length; i++) {
            System.out.println("Status = " + statusValues[i]);
            PetStoreProxy.Order[] orders = server.getOrders(statusValues[i]);
            for(int j = 0; j < orders.length; j++) {
                System.out.println(orders[j]);
            }
        }

        PetStoreProxy.Order[] orders = server.getOrders(PetStoreProxy.Order.PENDING);
        server.updateStatus(orders, PetStoreProxy.Order.DENIED);

        Date startDate = new Date("01/12/2001");
        Date endDate = new Date("01/14/2001");
        String categories[] = new String[]{null, "CATS", "BIRDS", "REPTILES", "DOGS"};

        for(int i = 0; i < categories.length; i++) {
            PetStoreProxy.Sales[] sales = server.getRevenue(startDate, endDate, categories[i]);
            System.out.println("Category = " + categories[i]);
            for(int j = 0; j < sales.length; j++) {
                System.out.println(sales[j]);
            }
        }

        for(int i = 0; i < categories.length; i++) {
            PetStoreProxy.Sales[] sales = server.getOrders(startDate, endDate, categories[i]);
            System.out.println("Category = " + categories[i]);
            for(int j = 0; j < sales.length; j++) {
                System.out.println(sales[j]);
            }
        }
    }
    */
}

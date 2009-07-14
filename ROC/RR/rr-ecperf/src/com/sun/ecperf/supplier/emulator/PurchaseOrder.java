
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: PurchaseOrder.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.supplier.emulator;


//Import statements
import java.util.*;

import java.io.*;

import org.w3c.dom.*;

import org.apache.xerces.dom.*;

import com.sun.ecperf.supplier.helper.*;

import com.sun.ecperf.common.*;


/**
 * Each instance of this class represents
 * a Purchase Order. This class is responsible
 * for parsing of XML and creating POLine Objects.
 *
 * @author Damian Guy
 */
public class PurchaseOrder {

    ArrayList lines;
    String    poNumber;
    String    siteID;
    String    numLines;
    String    supplierHost;
    String    supplierServlet;
    int       supplierPort;
    String    deliveryDTD;
    int       maxRetries;
    int       retryInterval;

    boolean   debugging;
    Debug     debug;
    Scheduler scheduler;

    /**
     * create Purchase Order
     * @param xmlDoc - XML Document containing the Purchase Order
     */
    public PurchaseOrder(Document xmlDoc, Map config) {

        this.scheduler       = (Scheduler) config.get("scheduler");
        this.maxRetries      = ((Integer) config.get("maxRetries"))
                               .intValue();
        this.retryInterval   = ((Integer) config.get("retryInterval"))
                               .intValue();
        this.debugging       = ((Boolean) config.get("debugging"))
                               .booleanValue();
        this.debug           = (Debug) config.get("debug");
        this.supplierHost    = (String) config.get("supplierHost");
        this.supplierServlet = (String) config.get("supplierServlet");
        this.supplierPort    = ((Integer) config.get("supplierPort"))
                               .intValue();
        this.deliveryDTD     = (String) config.get("deliveryDTD");
        poNumber             =
            getData(xmlDoc.getElementsByTagName(XmlTags.PONUMBER).item(0));
        siteID              =
            getData(xmlDoc.getElementsByTagName(XmlTags.SITE).item(0));
        numLines             =
            getData(xmlDoc.getElementsByTagName(XmlTags.NUMLINES).item(0));

        NodeList polines = xmlDoc.getElementsByTagName(XmlTags.POLINE);

        lines = new ArrayList();

        for (int i = 0; i < polines.getLength(); i++) {
            getLineData(polines.item(i));
        }
    }

    private String getData(Node node) {

        if (node.hasChildNodes()) {
            Node dataNode = node.getChildNodes().item(0);

            return dataNode.getNodeValue().trim();
        }

        return null;
    }

    private void getLineData(Node line) {

        String   data[];
        NodeList children = line.getChildNodes();

        data = new String[5];

        for (int i = 0; i < data.length; i++) {
            data[i] = getData(children.item(i));
        }

        insert(new POLine(poNumber, siteID, data[0], data[1], data[2],
                          data[3], Integer.parseInt(data[4])));
    }

    /**
     * Inserts  POLines into  ArrayList in ascending order
     * based on lead time.
     */
    private void insert(POLine current) {

        int     j     = 0;
        boolean found = false;

        while ((j <= lines.size()) && (found == false)) {
            Vector v = null;

            if (j == lines.size()) {
                v = new Vector();

                v.addElement(current);
                lines.add(j, v);

                found = true;
            } else {
                v = (Vector) lines.get(j);

                POLine line = (POLine) v.firstElement();

                if (line.getLeadTime() == current.getLeadTime()) {
                    v.addElement(current);

                    found = true;
                } else if (line.getLeadTime() > current.getLeadTime()) {
                    v = new Vector();

                    v.addElement(current);
                    lines.add(j, v);

                    found = true;
                }
                // Otherwise continue searching.
            }

            j++;
        }
    }

    /**
     * processPO - Creates XML to deliver the POLines.
     *
     */
    public void processPO() {

        long refTime = System.currentTimeMillis();

        for (int i = 0; i < lines.size(); i++) {
            Vector v = (Vector) lines.get(i);

            // Calculates lead time per delivery.
	    long partsLeadTime = (long) ((POLine) v.firstElement())
                                                   .getLeadTime();
	    long deliveryTime = partsLeadTime * 5000l + refTime;

            scheduler.schedule(deliveryTime, new DeliveryOrder(v));
        }
    }

    class DeliveryOrder implements Runnable {

        Vector poLineVector;
        String xml = null;
        int retries = 0;

        public DeliveryOrder(Vector poLineVector) {
            this.poLineVector = poLineVector;
        }

        public void run() {
            if (xml == null)
                processPOLine();
            deliverGoods();
        }
        public void processPOLine() {
			
            StringBuffer xml = new StringBuffer(XmlTags.XMLVERSION);
            xml.append(XmlTags.DELIVERYDOC);
            xml.append("\"");
            xml.append(deliveryDTD);
            xml.append("\">");
            xml.append(XmlTags.DELIVERYSTART);
            xml.append(XmlTags.PONUMBERSTART);
            xml.append(poNumber);
            xml.append(XmlTags.PONUMBEREND);
            xml.append(XmlTags.NUMLINESSTART);
            xml.append(poLineVector.size());
            xml.append(XmlTags.NUMLINESEND);
    
            for (int i = 0; i < poLineVector.size(); i++) {
                POLine current = (POLine) poLineVector.elementAt(i);
                xml.append(current.getXml());
            }

            xml.append(XmlTags.DELIVERYEND);
            this.xml = xml.toString();
        }

        private void deliverGoods() {
    
            NonSecureXmlCommand comm = new NonSecureXmlCommand(supplierHost,
                                           xml, supplierServlet, supplierPort);
            try {
                comm.execute();
            } catch (NotReadyException e) {
                if (retries < maxRetries) {
                    if (debugging)
                        debug.println(2, "Not ready, rescheduling...");
                    ++retries;
                    scheduler.schedule(System.currentTimeMillis() +
                                       retryInterval, this);
                } else {
                    debug.println(0, "Giving up after " + maxRetries 
                                  + " delivery trials.");
                }
            } catch (ECperfException e) {
                if (debugging)
                    debug.println(1, e.getMessage());
                debug.printStackTrace(e);
            } catch (IOException e) {
                if (debugging)
                    debug.println(1, e.getMessage());
                debug.printStackTrace(e);
            }
        }
    }
}

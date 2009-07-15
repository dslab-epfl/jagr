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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Date;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

import com.sun.j2ee.blueprints.opc.admin.ejb.OPCAdminFacadeHome;
import com.sun.j2ee.blueprints.opc.admin.ejb.OPCAdminFacade;
import com.sun.j2ee.blueprints.opc.admin.ejb.OrdersTO;
import com.sun.j2ee.blueprints.opc.admin.ejb.OPCAdminFacadeException;
import com.sun.j2ee.blueprints.asyncsender.util.JNDINames;
import com.sun.j2ee.blueprints.asyncsender.ejb.AsyncSenderLocalHome;
import com.sun.j2ee.blueprints.asyncsender.ejb.AsyncSender;
import com.sun.j2ee.blueprints.xmldocuments.OrderApproval;
import com.sun.j2ee.blueprints.xmldocuments.ChangedOrder;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import com.sun.j2ee.blueprints.servicelocator.web.ServiceLocator;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;

public class AdminRequestBD {

    private String OPC_ADMIN_NAME = "java:comp/env/ejb/OPCAdminFacadeRemote";
    private OPCAdminFacade opcAdminEJB = null;

    public AdminRequestBD() throws AdminBDException {
        try {
            OPCAdminFacadeHome home = (OPCAdminFacadeHome) ServiceLocator.getInstance().getRemoteHome(OPC_ADMIN_NAME, OPCAdminFacadeHome.class);
            opcAdminEJB = home.create();
        } catch (ServiceLocatorException sle) {
            sle.printStackTrace();
            throw new AdminBDException(sle.getMessage());
        } catch (CreateException ce) {
            ce.printStackTrace();
            throw new AdminBDException(ce.getMessage());
        } catch (RemoteException re) {
            re.printStackTrace();
            throw new AdminBDException(re.getMessage());
        }
    }

    /**
     * This method returns the orders of given status
     * @param status  The requested status
     */
    public OrdersTO getOrdersByStatus(String status)
        throws AdminBDException {

        try {
            return opcAdminEJB.getOrdersByStatus(status);
        } catch (RemoteException re) {
            re.printStackTrace();
            throw new AdminBDException(re.getMessage());
        } catch (OPCAdminFacadeException oafee) {
            oafee.printStackTrace();
            throw new AdminBDException(oafee.getMessage());
        }
    }

    public void updateOrders(OrderApproval oa) throws AdminBDException {
        try {
            AsyncSenderLocalHome home = (AsyncSenderLocalHome)
                ServiceLocator.getInstance().getLocalHome(JNDINames.ASYNCSENDER_LOCAL_EJB_HOME);
            AsyncSender sender= home.create();
            sender.sendAMessage(oa.toXML());
        } catch (ServiceLocatorException sle) {
            sle.printStackTrace();
            throw new AdminBDException(sle.getMessage());
        } catch (XMLDocumentException xde) {
            xde.printStackTrace();
            throw new AdminBDException(xde.getMessage());
        }  catch (CreateException ce) {
            throw new AdminBDException(ce.getMessage());
        }
    }

    /**
     * This method gets chart details for the rich client
     * @param request  REVENUE or ORDER
     * @param start    start date in mm/dd/yyyy format
     * @param end      end date in mm/dd/yyyy format
     * @param category the requested category
     * @returns String  An xml doc that has the chart details for given dates
     *                  an xml document indicating error in case of failures
     */
    public Map getChartInfo(String request, Date start, Date end,
                            String category)
        throws AdminBDException {

        try {
            return opcAdminEJB.getChartInfo(request, start, end, category);
        } catch (RemoteException re) {
            re.printStackTrace();
            throw new AdminBDException(re.getMessage());
        } catch (OPCAdminFacadeException oafee) {
            oafee.printStackTrace();
            throw new AdminBDException(oafee.getMessage());
        }
    }
}

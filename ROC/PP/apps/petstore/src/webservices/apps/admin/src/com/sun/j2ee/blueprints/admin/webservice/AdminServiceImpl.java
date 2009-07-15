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

package com.sun.j2ee.blueprints.admin.webservice;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import com.sun.j2ee.blueprints.xmldocuments.OrderApproval;
import com.sun.j2ee.blueprints.xmldocuments.ChangedOrder;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import com.sun.j2ee.blueprints.admin.web.AdminRequestBD;
import com.sun.j2ee.blueprints.admin.web.AdminBDException;
import com.sun.j2ee.blueprints.opc.admin.ejb.OrderDetails;

public class AdminServiceImpl implements AdminService {

    private static AdminRequestBD adminBD = null;
    private static AdminRequestBD getAdminBD() throws AdminBDException {
        if (adminBD == null) {
            adminBD = new AdminRequestBD();
        }
        return adminBD;
    }

    public AdminService.OrdersTO getOrdersByStatus(String status) {
        try {
            return convert(getAdminBD().getOrdersByStatus(status));
        } catch (AdminBDException ex) {
            // TBD handle properly..
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }

    private AdminService.OrdersTO convert(com.sun.j2ee.blueprints.opc.admin.ejb.OrdersTO orders) {
        AdminService.Order[] orderArray = new AdminService.Order[orders.size()];
        int i = 0;
        for (Iterator it = orders.iterator(); it.hasNext(); ++i) {
            orderArray[i] = convert((OrderDetails) it.next());
        }
        AdminService.OrdersTO to = new AdminService.OrdersTO();
        to.setOrders(orderArray);
        return to;
    }

    private AdminService.Order convert(OrderDetails od) {
        return new AdminService.Order(od.getOrderId(), od.getUserId(), od.getOrderDate(),
        od.getOrderValue(), od.getOrderStatus());
    }

    public void update(AdminService.OrderApprovalTO to) {
        OrderApproval oa = new OrderApproval();
        String[] orderIds = to.getOrderIds();
        String[] statuses = to.getStatuses();
        for (int i = 0; i < orderIds.length; ++i) {
            oa.addOrder(new ChangedOrder(orderIds[i], statuses[i]));
        }
        try {
            getAdminBD().updateOrders(oa);
        } catch (AdminBDException e) {
            // TBD: need better exception handling
            e.printStackTrace();
        }
    }

    public void update(String orderId, String status) {
        OrderApproval oa = new OrderApproval();
        oa.addOrder(new ChangedOrder(orderId, status));
        try {
            getAdminBD().updateOrders(oa);
        } catch (AdminBDException e) {
            // TBD: need better exception handling
            e.printStackTrace();
        }
    }

    public Map getChartInfo(String request, Date start, Date end, String category) {
        try {
            AdminRequestBD bd = new AdminRequestBD();
            return bd.getChartInfo(request, start, end, category);
        } catch (AdminBDException ex) {
            // TBD handle properly..
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }
}

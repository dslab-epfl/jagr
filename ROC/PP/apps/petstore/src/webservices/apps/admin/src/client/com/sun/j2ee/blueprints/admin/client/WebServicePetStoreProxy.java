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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import javax.xml.rpc.Stub;
import com.sun.j2ee.blueprints.admin.webservice.adminclient.AdminService;
import com.sun.j2ee.blueprints.admin.webservice.adminclient.AdminServiceOrder;
import com.sun.j2ee.blueprints.admin.webservice.adminclient.AdminServiceOrdersTO;
import com.sun.j2ee.blueprints.admin.webservice.adminclient.AdminServiceOrderApprovalTO;
import com.sun.j2ee.blueprints.admin.webservice.adminclient.AdminWebService_Impl;

public class WebServicePetStoreProxy implements PetStoreProxy {

    private AdminService adminws = null;
    private String server = null;
    private String port = null;
    private String endpoint = null;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public WebServicePetStoreProxy() { }

    public void setup(String server, String port, String endpoint) {
        this.server = server;
        this.port = port;
        this.endpoint = endpoint;
    }

    private AdminService getAdminService() {
        if (adminws == null) {
            String serviceURL = "http://" + server + ":" + port + endpoint;
            Stub stub = (Stub) (new AdminWebService_Impl().getAdminServicePort());
            stub._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, serviceURL);
            adminws = (AdminService) stub;
        }
        return adminws;
    }

    public PetStoreProxy.Order[] getOrders(String status) {
        PetStoreProxy.Order[] orders = null;
        try {
            AdminServiceOrdersTO to = getAdminService().getOrdersByStatus(status);
            if (to == null) {
                // TBD: handle properly
            } else {
                orders = convert(to.getOrders());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return orders;
    }

    private PetStoreProxy.Order[] convert(AdminServiceOrder[] orders) {
        PetStoreProxy.Order[] converted = new PetStoreProxy.Order[orders.length];
        for (int i = 0; i < orders.length; ++i) {
            AdminServiceOrder o = orders[i];
            converted[i] = new PetStoreProxy.Order(o.getOrderId(), o.getUserId(),
            parseDate(o.getOrderDate()), o.getOrderValue(), o.getOrderStatus());
        }
        return converted;
    }

    private Date parseDate(String s) {
        try {
            return dateFormat.parse(s);
        } catch (ParseException pe) {
            // TBD: Just hard-code some date for now
            pe.printStackTrace();
            return new Date();
        }
    }

    // TBD: check for correctness
    private Calendar convert(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal;
    }

    public PetStoreProxy.Sales[] getRevenue(Date start, Date end, String category) {
        try {
            Map chartInfo = getAdminService().getChartInfo("REVENUE", convert(start), convert(end), category);
            Collection keys = chartInfo.keySet();
            ArrayList sales = new ArrayList(keys.size());
            for (Iterator it = keys.iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                float revenue = ((Float) chartInfo.get(key)).floatValue();
                if (revenue >= 0.0) {
                    PetStoreProxy.Sales sale = new PetStoreProxy.Sales(key, revenue);
                    sales.add(sale);
                }
            }
            return (PetStoreProxy.Sales[])(sales.toArray(new PetStoreProxy.Sales[sales.size()]));
        } catch (RemoteException re) {
            re.printStackTrace();
            return null;
        }
    }

    public PetStoreProxy.Sales[] getOrders(Date start, Date end, String category) {
        try {
            Map chartInfo = getAdminService().getChartInfo("ORDERS", convert(start), convert(end), category);
            Collection keys = chartInfo.keySet();
            ArrayList sales = new ArrayList(keys.size());
            for (Iterator it = keys.iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                int nOrders = ((Integer) chartInfo.get(key)).intValue();
                if (nOrders >= 0) {
                    PetStoreProxy.Sales sale = new PetStoreProxy.Sales(key, nOrders);
                    sales.add(sale);
                }
            }
            return (PetStoreProxy.Sales[])(sales.toArray(new PetStoreProxy.Sales[sales.size()]));
        } catch (RemoteException re) {
            re.printStackTrace();
            return null;
        }
    }

    public void updateStatus(PetStoreProxy.Order[] orders, String status) {
        try {
            String[] orderIds = new String[orders.length];
            String[] statuses = new String[orders.length];
            for (int i = 0; i < orders.length; ++i) {
                orderIds[i] = orders[i].getId();
                statuses[i] = status;
            }
            AdminServiceOrderApprovalTO to = new AdminServiceOrderApprovalTO();
            to.setOrderIds(orderIds);
            to.setStatuses(statuses);
            getAdminService().update(to);
        } catch (RemoteException re) {
            // TBD: need better handling
            re.printStackTrace();
        }
    }

    public void updateStatus(PetStoreProxy.Order order, String status) {
        try {
            getAdminService().update2(order.getId(), status);
        } catch (RemoteException re) {
            // TBD: need better handling
            re.printStackTrace();
        }
    }
}

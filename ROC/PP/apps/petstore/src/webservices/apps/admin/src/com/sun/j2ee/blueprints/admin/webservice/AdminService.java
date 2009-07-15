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

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.Map;
import java.util.Date;
//import com.sun.j2ee.blueprints.xmldocuments.OrderApproval;

public interface AdminService extends Remote {

    public static class Order implements java.io.Serializable {
        private String orderId = null;
        private String userId = null;
        private String orderDate = null;
        private float orderValue = 0;
        private String orderStatus = null;

        public Order() { }

        public Order(String oid, String uid, String date, float value, String stat) {
            orderId = oid;
            userId = uid;
            orderDate = date;
            orderValue = value;
            orderStatus = stat;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getOrderDate() {
            return orderDate;
        }

        public void setOrderDate(String orderDate) {
            this.orderDate = orderDate;
        }

        public float getOrderValue() {
            return orderValue;
        }

        public void setOrderValue(float orderValue) {
            this.orderValue = orderValue;
        }

        public String getOrderStatus() {
            return orderStatus;
        }

        public void setOrderStatus(String orderStatus) {
            this.orderStatus = orderStatus;
        }
    }

    public static class OrdersTO implements Serializable {
        private AdminService.Order[] orders = null;

        public OrdersTO() { }

        public int size() {
            return orders == null ? 0 : orders.length;
        }

        public AdminService.Order[] getOrders() {
            return orders;
        }

        public void setOrders(AdminService.Order[] orders) {
            this.orders = orders;
        }
    }

    public static class OrderApprovalTO implements Serializable {
        private String[] orderIds = null;
        private String[] statuses = null;

        public OrderApprovalTO() { }

        public int size() {
            return orderIds == null ? 0 : orderIds.length;
        }

        public String[] getOrderIds() {
            return orderIds;
        }

        public void setOrderIds(String[] orderIds) {
            this.orderIds = orderIds;
        }

        public String[] getStatuses() {
            return statuses;
        }

        public void setStatuses(String[] statuses) {
            this.statuses = statuses;
        }
    }

    /**
     * @return a data transfer object consisting of orders which match the status as requested
     */
    public OrdersTO getOrdersByStatus(String status) throws RemoteException;

    /**
     * Updates the status of the orders contained in the TO as specified for them in the TO
     */
    public void update(OrderApprovalTO oa) throws RemoteException;

    /**
     * Updates the status of the specified order to the specified status
     */
    public void update(String orderId, String status) throws RemoteException;

    /**
     * @param request the type of statistics requested
     * @param start only orders placed after the start date are used for the calculation
     * @param end only orders placed before the end date are used for the calculation
     * @param category the category of the products for which the statistics are requested
     * @return the sales statistics
     */
    public Map getChartInfo(String request, Date start, Date end, String category) throws RemoteException;
}

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

/**
 * This interface represents a delegate for interacting with the petstore server
 */
public interface PetStoreProxy {

    public static class Order {
        public final static String PENDING = "PENDING";
        public final static String DENIED = "DENIED";
        public final static String APPROVED = "APPROVED";
        public final static String COMPLETED = "COMPLETED";

        private String id;
        private String userId;
        private Date date;
        private float amount;
        private String status;

        public Order(String id, String userId, Date date, float amount, String status) {
            checkStatus(status);
            this.id = id;
            this.userId = userId;
            this.date = date;
            this.amount = amount;
            this.status = status;
        }
        public String getId() {
            return id;
        }
        public String getUserId() {
            return userId;
        }
        public Date getDate() {
            return date;
        }
        public float getAmount() {
            return amount;
        }
        public String getStatus() {
            return status;
        }

        void checkStatus(String status) {
            if ((status != Order.PENDING) &&
                (status != Order.APPROVED) &&
                (status != Order.DENIED) &&
                (status != Order.COMPLETED)) {
                // throw an illegal arg exception
            }
        }

        private void appendProperty(StringBuffer sb, String name, Object value) {
            if (value != null) {
                sb.append(" ");
                sb.append(name);
                sb.append("=");
                if (value instanceof String) {
                    sb.append("\"");
                }
                sb.append(value.toString());
                if (value instanceof String) {
                    sb.append("\"");
                }
            }
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            sb.append(this.getClass().getName());
            appendProperty(sb, "id", getId());
            appendProperty(sb, "userId", getUserId());
            appendProperty(sb, "date", getDate());
            appendProperty(sb, "status", getStatus());
            appendProperty(sb, "amount", new Float(getAmount()));
            sb.append("]");
            return sb.toString();
        }
    }

    public static class Sales {
        private final String key;
        private final float revenue;
        private final int orders;

        public Sales(String key, float revenue) {
            this.key = key;
            this.revenue = revenue;
            this.orders = -1;
        }

        public Sales(String key,  int orders) {
            this.key = key;
            this.revenue = -1.0f;
            this.orders = orders;
        }

        public String getKey() {
            return key;
        }
        public float getRevenue() {
            return revenue;
        }
        public float getOrders() {
            return orders;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            sb.append(this.getClass().getName());
            sb.append(" key=");
            sb.append(getKey());
            if (getOrders() != -1) {
                sb.append(" orders=");
                sb.append(getOrders());
            } else {
                sb.append(" revenue=");
                sb.append(getRevenue());
            }
            sb.append("]");
            return sb.toString();
        }
    }

    /**
     * Prepares connection to the petstore running at the specified host, port, and endpoint.
     * This method must be called before any operations are invoked on the server.
     * @param endpoint this is a proxy class specific property which typically gives more precise
     * mechanism to locate the petstore admin service.
     */
    public void setup(String host, String port, String endpoint);

    public Order[] getOrders(String status);

    public Sales[] getRevenue(Date start, Date end, String category);

    public Sales[] getOrders(Date start, Date end, String category);

    public void updateStatus(Order[] orders, String status);

    public void updateStatus(Order order, String status);
}

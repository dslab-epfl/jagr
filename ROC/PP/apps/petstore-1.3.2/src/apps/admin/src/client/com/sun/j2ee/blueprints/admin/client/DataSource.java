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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.util.*;

/**
 * Center point for all data send to and from the server.  This class supplies
 * views needed by the client app.
 * TODO: If no network connection is available this class will buffer the data
 * until a network connection is available.
 *
 * @author Joshua Outwater
 */

public class DataSource {
    private JFrame parent;
    private PetStoreProxy server;
    private PetStoreProxy.Sales barChartSales[];
    private PetStoreProxy.Sales pieChartSales[];
    private PetStoreProxy.Order orders[];

    /**
     * Columns used for the table headers.  The actual strings are retrieved
     * from the petstore properties file.
     */
    private String columns[] = {
        PetStoreAdminClient.getString("OrdersTable.id"),
        PetStoreAdminClient.getString("OrdersTable.userId"),
        PetStoreAdminClient.getString("OrdersTable.date"),
        PetStoreAdminClient.getString("OrdersTable.amount"),
        PetStoreAdminClient.getString("OrdersTable.status") };

        private OrdersViewTableModel ordersViewTableModel;
        private OrdersApproveTableModel ordersApproveTableModel;
        private PieChartModel pieChartModel;
        private BarChartModel barChartModel;

        private SwingPropertyChangeSupport pcs = new
        SwingPropertyChangeSupport(this);

        /** Property identifying that the orders data has changed. */
        public static final String ORDER_DATA_CHANGED = "ORDER_DATA_CHANGED";

        /** Property identifying that the pie chart data has changed. */
        public static final String PIE_CHART_DATA_CHANGED =
        "PIE_CHART_DATA_CHANGED";

        /** Property identifying that the bar chart data has changed. */
        public static final String BAR_CHART_DATA_CHANGED =
        "BAR_CHART_DATA_CHANGED";

        /** Property used to inform listeners to enable their actions. */
        public static final String ENABLE_ACTIONS = "ENABLE_ACTIONS";

        /** Property used to inform listeners to disable their actions. */
        public static final String DISABLE_ACTIONS = "DISABLE_ACTIONS";

        public DataSource(JFrame parent, String petStoreProxyClassName,
        String hostname, String port, String endpoint) {
            try {
                Class proxyClass = Class.forName(petStoreProxyClassName);
                server = (PetStoreProxy) proxyClass.newInstance();
                server.setup(hostname, port, endpoint);
                this.parent = parent;
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            } catch (InstantiationException ie) {
                ie.printStackTrace();
            } catch (IllegalAccessException iae) {
                iae.printStackTrace();
            }
        }

        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            pcs.addPropertyChangeListener(pcl);
        }

        public void addPropertyChangeListener(String property,
        PropertyChangeListener pcl) {
            pcs.addPropertyChangeListener(property, pcl);
        }

        /**
         * Retrieve only the order data that will be viewed or modified by the
         * OrdersViewPanel and OrdersApprovePanel.
         */
        private PetStoreProxy.Order[] getServerOrderData() {
            final Vector v = new Vector();
            v.addAll(Arrays.asList(
            server.getOrders(PetStoreProxy.Order.PENDING)));
            v.addAll(Arrays.asList(
            server.getOrders(PetStoreProxy.Order.APPROVED)));
            v.addAll(Arrays.asList(
            server.getOrders(PetStoreProxy.Order.DENIED)));
            v.addAll(Arrays.asList(
            server.getOrders(PetStoreProxy.Order.COMPLETED)));
            PetStoreProxy.Order[] newOrders = new PetStoreProxy.Order[v.size()];
            v.toArray(newOrders);
            return newOrders;
        }

        private PetStoreProxy.Sales[] getServerPieChartData() {
            return server.getRevenue(pieChartModel.getStartDate(),
            pieChartModel.getEndDate(), pieChartModel.getViewMode());
        }

        private PetStoreProxy.Sales[] getServerBarChartData() {
            return server.getOrders(barChartModel.getStartDate(),
            barChartModel.getEndDate(), barChartModel.getViewMode());
        }

        /**
         * Called when there was an error communicating with the server.  An error
         * is displayed to the user and the application will then exit.
         */
        private void fatalServerError(String message) {
            JOptionPane.showMessageDialog(parent, message,
            PetStoreAdminClient.getString("ServerErrorDialog.title"),
            JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        public TableModel getOrdersViewTableModel() {
            if (ordersViewTableModel == null) {
                ordersViewTableModel = new OrdersViewTableModel();
            }
            return ordersViewTableModel;
        }

        public OrdersApproveTableModel getOrdersApproveTableModel() {
            if (ordersApproveTableModel == null) {
                ordersApproveTableModel = new OrdersApproveTableModel();
            }
            return ordersApproveTableModel;
        }

        public PieChartModel getPieChartModel() {
            if (pieChartModel == null) {
                pieChartModel = new PieChartModel();
            }
            return pieChartModel;
        }

        public BarChartModel getBarChartModel() {
            if (barChartModel == null) {
                barChartModel = new BarChartModel();
            }
            return barChartModel;
        }

        public RefreshAction getRefreshAction() {
            return new RefreshAction();
        }

        /**
         * Action that will refresh the data in the application by retrieving it
         * from the server.  All table models will be updated and any listeners
         * will be notified of the change.
         */
        public class RefreshAction extends ServerAction {
            private PetStoreProxy.Sales newBarChartSales[];
            private PetStoreProxy.Sales newPieChartSales[];
            private PetStoreProxy.Order newOrders[];

            public RefreshAction() {
                super(PetStoreAdminClient.getString("RefreshAction.name"));
                putValue(SHORT_DESCRIPTION, PetStoreAdminClient.getString(
                "RefreshAction.tooltip"));
                putValue(LONG_DESCRIPTION, PetStoreAdminClient.getString(
                "RefreshAction.description"));
                putValue(SMALL_ICON, new ImageIcon(
                getClass().getResource("/resources/Refresh24.gif")));
            }

            public void actionPerformed(final ActionEvent action) {
                boolean isUncommitted = false;

                // Check if there is any uncommitted data.
                for (int i = 0; i < ordersApproveTableModel.getRowCount(); i++) {
                    if (!PetStoreProxy.Order.PENDING.equals(
                    ordersApproveTableModel.getValueAt(i, 4))) {
                        isUncommitted = true;
                        break;
                    }
                }

                int result = JOptionPane.OK_OPTION;
                // Warn the user if they have any uncommitted data.
                if (isUncommitted) {
                    result = JOptionPane.showConfirmDialog(parent,
                    PetStoreAdminClient.getString(
                    "RefreshWarningDialog.message"),
                    PetStoreAdminClient.getString(
                    "RefreshWarningDialog.title"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                }

                if (result == JOptionPane.OK_OPTION) {
                    // Notify listener to disable actions.
                    pcs.firePropertyChange(DISABLE_ACTIONS, null, null);

                    StatusBar.getInstance().setMessage(
                    PetStoreAdminClient.getString("Status.retrieve"));

                    super.actionPerformed(action);
                }
            }

            protected Object request(ActionEvent e) {
                newOrders = getServerOrderData();
                newPieChartSales = getServerPieChartData();
                newBarChartSales = getServerBarChartData();
                return null;
            }

            protected void response(Object requestValue) {
                StatusBar.getInstance().setMessage(null);

                orders = newOrders;
                pieChartSales = newPieChartSales;
                barChartSales = newBarChartSales;

                // Update all the data models.
                if (ordersViewTableModel != null) {
                    ordersViewTableModel.updateModel();
                }
                if (ordersApproveTableModel != null) {
                    ordersApproveTableModel.updateModel();
                }
                if (pieChartModel != null) {
                    pieChartModel.updateModel();
                }
                if (barChartModel != null) {
                    barChartModel.updateModel();
                }
                // Notify listener to enable actions.
                pcs.firePropertyChange(ENABLE_ACTIONS, null, null);
            }

            protected void handleException(Exception e) {
                e.printStackTrace();
                fatalServerError(e.getMessage());
            }
        }

        /**
         * This class is used to view orders with the status of APPROVED, COMPLETED
         * and DENIED.  No editing of data is allowed with this model.
         */
        protected class OrdersViewTableModel extends DefaultTableModel {
            protected int[] indexMapping;
            protected String[] statusList;
            protected SimpleDateFormat dateFormat =
            new SimpleDateFormat("MM/dd/yyyy");

            public OrdersViewTableModel() {
                super();
                setColumnIdentifiers(columns);
                statusList = new String[] {PetStoreProxy.Order.APPROVED, PetStoreProxy.Order.COMPLETED, PetStoreProxy.Order.DENIED};
            }

            protected void updateModel() {
                if (orders != null) {
                    int count = 0;
                    String status;
                    for (int i = 0; i < orders.length; i++) {
                        // We only want to look at approved, completed or denied
                        // orders.
                        status = orders[i].getStatus();
                        for (int j = 0; j < statusList.length; j++) {
                            if (statusList[j].equals(status)) {
                                count++;
                            }
                        }
                    }
                    indexMapping = new int[count];
                    count = 0;
                    for (int i = 0; i < orders.length; i++) {
                        status = orders[i].getStatus();
                        for (int j = 0; j < statusList.length; j++) {
                            if (statusList[j].equals(status)) {
                                indexMapping[count] = i;
                                count++;
                            }
                        }
                    }
                }
                // Notify listeners that the orders data has changed.
                pcs.firePropertyChange(ORDER_DATA_CHANGED, null, null);
            }

            public Object getValueAt(int row, int column) {
                if (orders == null) {
                    return null;
                }
                PetStoreProxy.Order order = orders[indexMapping[row]];
                Object result = null;
                switch (column) {
                    case 0:
                        try {
                            result = new Integer(order.getId());
                        } catch (NumberFormatException e) {
                            result = new Integer(-1);
                        }
                        break;
                    case 1:
                        result = order.getUserId();
                        break;
                    case 2:
                        result = order.getDate();
                        break;
                    case 3:
                        result = new Float(order.getAmount());
                        break;
                    case 4:
                        result = order.getStatus();
                        break;
                    default:
                        System.err.println("This shouldn't happen: (" + column
                        + ").  Defaulting to null value");
                }
                return result;
            }

            public Class getColumnClass(int column) {
                Class result = Object.class;
                switch (column) {
                    case 0:
                        result = Integer.class;
                        break;
                    case 1:
                        result = String.class;
                        break;
                    case 2:
                        result = Date.class;
                        break;
                    case 3:
                        result = Float.class;
                        break;
                    case 4:
                        result = String.class;
                        break;
                    default:
                        System.err.println("This shouldn't happen: (" + column
                        + ").  Defaulting to Object.class");
                }
                return result;
            }

            public int getRowCount() {
                if (indexMapping != null) {
                    return indexMapping.length;
                }
                return 0;
            }

            /**
             * Do not allow setting of data in view mode.
             */
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        }

        /**
         * This class is used to modify orders with the status of PENDING.  The new
         * status of the orders can either be APPROVED or DENIED.  These changes
         * will then be send back to the server, or cached until a network
         * connection is available.
         */
        protected class OrdersApproveTableModel extends OrdersViewTableModel {
            public OrdersApproveTableModel() {
                super();
                statusList = new String[] { PetStoreProxy.Order.PENDING };
            }

            public void commit() {
                Vector approvedRows = new Vector();
                Vector deniedRows = new Vector();
                for (int i = 0; i < getRowCount(); i++) {
                    if (PetStoreProxy.Order.APPROVED.equals(
                    (String)getValueAt(i, 4))) {
                        approvedRows.add(new Integer(i));
                    } else if (PetStoreProxy.Order.DENIED.equals(
                    (String)getValueAt(i, 4))) {
                        deniedRows.add(new Integer(i));
                    }
                }

                // Return if we don't have anything to update.
                if (approvedRows.size() == 0 &&
                deniedRows.size() == 0) {
                    return;
                }

                final PetStoreProxy.Order[] approvedOrders =
                new PetStoreProxy.Order[approvedRows.size()];
                int row;
                for (int i = 0; i < approvedRows.size(); i++) {
                    row = ((Integer)approvedRows.elementAt(i)).intValue();
                    approvedOrders[i] = orders[indexMapping[row]];
                }

                final PetStoreProxy.Order[] deniedOrders =
                new PetStoreProxy.Order[deniedRows.size()];
                for (int i = 0; i < deniedRows.size(); i++) {
                    row = ((Integer)deniedRows.elementAt(i)).intValue();
                    deniedOrders[i] = orders[indexMapping[row]];
                }

                ServerAction action = new ServerAction() {
                    public void actionPerformed(final ActionEvent action) {
                        // Notify listener to disable actions.
                        pcs.firePropertyChange(DISABLE_ACTIONS, null, null);

                        StatusBar.getInstance().setMessage(
                        PetStoreAdminClient.getString("Status.update"));
                        super.actionPerformed(action);
                    }

                    protected Object request(ActionEvent e) {
                        if (approvedOrders.length > 0) {
                            server.updateStatus(approvedOrders,
                            PetStoreProxy.Order.APPROVED);
                        }
                        if (deniedOrders.length > 0) {
                            server.updateStatus(deniedOrders,
                            PetStoreProxy.Order.DENIED);
                        }
                        return getServerOrderData();
                    }

                    protected void response(Object requestValue) {
                        orders = (PetStoreProxy.Order[])requestValue;

                        if (ordersViewTableModel != null) {
                            ordersViewTableModel.updateModel();
                        }
                        if (ordersApproveTableModel != null) {
                            ordersApproveTableModel.updateModel();
                        }
                        StatusBar.getInstance().setMessage(null);
                        // Notify listener to enable actions.
                        pcs.firePropertyChange(ENABLE_ACTIONS, null, null);
                    }

                    protected void handleException(Exception e) {
                        e.printStackTrace();
                        fatalServerError(e.getMessage());
                    }
                };
                action.actionPerformed(null);
            }

            public boolean isCellEditable(int row, int column) {
                // Only the status column is editable.
                boolean result = false;
                if (column == 4) {
                    result = true;
                }
                return result;
            }

            public void setValueAt(Object value, final int row, int column) {
                final PetStoreProxy.Order order = orders[indexMapping[row]];
                final String val = (String)value;

                // Update the order manually since we don't want to refresh
                // all the data.
                orders[indexMapping[row]] = new PetStoreProxy.Order(
                order.getId(), order.getUserId(), order.getDate(),
                order.getAmount(), val);
                fireTableRowsUpdated(row, row);
            }
        }

        public abstract class ChartModel {
            /** The default start date is 1/12/2001 for the demo. TODO In the
             * released application this should default to the present day. */
            private Date startDate = new Date("1/1/2001");

            /** The default end date is the present day. */
            private Date endDate = new Date("12/31/2002");

            /** The default view mode is the top level view. */
            private String viewMode = null;

            protected String keys[];
            protected PetStoreProxy.Sales[] sales;

            public ChartModel() {
            }

            public String[] getKeys() {
                if (sales == null) {
                    return null;
                }
                if (keys == null) {
                    keys = new String[sales.length];
                    for (int i = 0; i < keys.length; i++) {
                        keys[i] = sales[i].getKey();
                    }
                }
                return keys;
            }

            /**
             * Returns the revenue associated with a particular category.
             */
            public float getOrders(String category) {
                if (sales == null) {
                    return 0f;
                }

                float result = 0f;
                for (int i = 0; i < sales.length; i++) {
                    if (sales[i].getKey().equals(category)) {
                        result = sales[i].getOrders();
                    }
                }
                return result;
            }

            /**
             * Returns the revenue associated with a particular category.
             */
            public float getRevenue(String category) {
                if (sales == null) {
                    return 0f;
                }

                float result = 0f;
                for (int i = 0; i < sales.length; i++) {
                    if (sales[i].getKey().equals(category)) {
                        result = sales[i].getRevenue();
                    }
                }
                return result;
            }

            /**
             * Returns the current category we are using for the view mode.  If the
             * returned value is null we are using the top level view.
             */
            public String getViewMode() {
                return viewMode;
            }

            public Date getStartDate() {
                return (Date)startDate.clone();
            }

            public Date getEndDate() {
                return (Date)endDate.clone();
            }

            public void setDates(Date startDate, Date endDate) {
                this.startDate = startDate;
                this.endDate = endDate;

                new RefreshChartAction(this).actionPerformed(null);
            }

            private class RefreshChartAction extends ServerAction {
                private ChartModel chartModel;

                public RefreshChartAction(ChartModel chartModel) {
                    this.chartModel = chartModel;
                }

                public void actionPerformed(final ActionEvent action) {
                    // Notify listener to disable actions.
                    pcs.firePropertyChange(DISABLE_ACTIONS, null, null);

                    StatusBar.getInstance().setMessage(
                    PetStoreAdminClient.getString("Status.retrieve"));
                    super.actionPerformed(action);
                }

                protected Object request(ActionEvent e) {
                    return getServerChartData();
                }

                protected void response(Object requestValue) {
                    if (chartModel instanceof PieChartModel) {
                        pieChartSales = (PetStoreProxy.Sales[])requestValue;
                    } else if (chartModel instanceof BarChartModel) {
                        barChartSales = (PetStoreProxy.Sales[])requestValue;
                    }
                    updateModel();
                    StatusBar.getInstance().setMessage(null);
                    // Notify listener to enable actions.
                    pcs.firePropertyChange(ENABLE_ACTIONS, null, null);
                }

                protected void handleException(Exception e) {
                    e.printStackTrace();
                    fatalServerError(e.getMessage());
                }
            }

            protected abstract PetStoreProxy.Sales[] getServerChartData();
            protected abstract void updateModel();
        }

        public class PieChartModel extends ChartModel {
            public PieChartModel() {
                super();
            }

            protected PetStoreProxy.Sales[] getServerChartData() {
                return getServerPieChartData();
            }

            protected void updateModel() {
                // Update the reference to the sales array.
                sales = pieChartSales;

                // Reset the keys to null.
                keys = null;

                // Notify listeners that the pie chart data has changed.
                pcs.firePropertyChange(PIE_CHART_DATA_CHANGED, null, null);
            }
        }

        public class BarChartModel extends ChartModel {
            public BarChartModel() {
                super();
            }

            protected PetStoreProxy.Sales[] getServerChartData() {
                return getServerBarChartData();
            }

            protected void updateModel() {
                // Update the reference to the sales array.
                sales = barChartSales;

                // Reset the keys to null.
                keys = null;

                // Notify listeners that the bar chart data has changed.
                pcs.firePropertyChange(BAR_CHART_DATA_CHANGED, null, null);
            }
        }
}


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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.border.SoftBevelBorder;
import javax.swing.table.*;
import java.util.Vector;

/**
 * This panel allows the user to change the status of pending orders.
 * Via a table the user will be able to change the pending orders to either
 * approved or denied.
 *
 * @author Joshua Outwater
 */
public class OrdersApprovePanel extends JPanel implements
        PropertyChangeListener {
    private JTable orderTable;
    private DataSource.OrdersApproveTableModel tableModel;
    private TableSorter sorter;
    private DefaultTableCellRenderer statusRenderer;
    private DefaultTableCellRenderer headerRenderer;
    private JComboBox cellOptions = new JComboBox();
    private JButton approveButton;
    private JButton denyButton;
    private JButton commitButton;

    /**
     * Creates an instance of this class using the specified table model for
     * the data.
     *
     * @param tableModel The table model to retrieve data from.
     */
    public OrdersApprovePanel(DataSource.OrdersApproveTableModel tableModel) {
        this.tableModel = tableModel;

        createUI();
    }

    private void createUI() {
        // These are the different status types that the user may select from
        // when updating an order.
        cellOptions.addItem(PetStoreProxy.Order.PENDING);
        cellOptions.addItem(PetStoreProxy.Order.APPROVED);
        cellOptions.addItem(PetStoreProxy.Order.DENIED);

        setLayout(new BorderLayout());
        setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
        add(createTable(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createButtonPanel() {
        // Create a button panel that allows the user to do mass approve/deny
        // operations.
        JPanel panel = new JPanel(new FlowLayout());
        approveButton =
            new JButton(PetStoreAdminClient.getString("ApproveButton.label"));
        approveButton.setMnemonic(
            PetStoreAdminClient.getMnemonic("ApproveButton.mnemonic"));
        approveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] rows = orderTable.getSelectedRows();
                for (int i = 0; i < rows.length; i++) {
                    sorter.setValueAt(PetStoreProxy.Order.APPROVED,
                        rows[i], 4);
                    orderTable.repaint();
                }
            }
        });
        panel.add(approveButton);

        denyButton =
            new JButton(PetStoreAdminClient.getString("DenyButton.label"));
        denyButton.setMnemonic(
            PetStoreAdminClient.getMnemonic("DenyButton.mnemonic"));
        denyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] rows = orderTable.getSelectedRows();
                for (int i = 0; i < rows.length; i++) {
                    sorter.setValueAt(PetStoreProxy.Order.DENIED, rows[i], 4);
                    orderTable.repaint();
                }
            }
        });
        panel.add(denyButton);

        commitButton =
            new JButton(PetStoreAdminClient.getString("CommitButton.label"));
        commitButton.setMnemonic(
            PetStoreAdminClient.getMnemonic("CommitButton.mnemonic"));
        commitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tableModel.commit();
            }
        });
        panel.add(commitButton);

        return panel;
    }

    private JComponent createTable() {
        sorter = new TableSorter(tableModel);
        orderTable = new JTable(sorter);
        sorter.addMouseListenerToHeaderInTable(orderTable);
        orderTable.getTableHeader().setReorderingAllowed(false);
        orderTable.setRowSelectionAllowed(true);
        orderTable.setBorder(BorderFactory.createEtchedBorder());

        TableColumn statusColumn = orderTable.getColumn(
            PetStoreAdminClient.getString("OrdersTable.status"));
        statusColumn.setCellEditor(new DefaultCellEditor(cellOptions));

        // Set the cell renderer for the table.
        statusRenderer = new DefaultTableCellRenderer() {
            public void setValue(Object value) {
                String str = (String)value;
                if (PetStoreProxy.Order.APPROVED.equals(str)) {
                    setBackground(Color.green);
                    setText(str);
                } else if (PetStoreProxy.Order.DENIED.equals(str)) {
                    setBackground(Color.red);
                    setText(str);
                } else if (PetStoreProxy.Order.PENDING.equals(str)) {
                    setBackground(Color.yellow);
                    setText(str);
                } else {
                    super.setValue(str);
                }
            }
        };
        statusColumn.setCellRenderer(statusRenderer);

        // Set the row height of the table so the combo box cell renderer fits
        // nicely.
        orderTable.setRowHeight(cellOptions.getPreferredSize().height);

        return new JScrollPane(orderTable);
    }

    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();

        if (DataSource.ORDER_DATA_CHANGED.equals(property)) {
            sorter.tableChanged(null);

            // Reset the cell editor and renderer when the table is changed.
            TableColumn statusColumn = orderTable.getColumn(
                PetStoreAdminClient.getString("OrdersTable.status"));
            statusColumn.setCellEditor(new DefaultCellEditor(cellOptions));
            statusColumn.setCellRenderer(statusRenderer);
        } else if (DataSource.DISABLE_ACTIONS.equals(property)) {
            approveButton.setEnabled(false);
            denyButton.setEnabled(false);
            commitButton.setEnabled(false);
        } else if (DataSource.ENABLE_ACTIONS.equals(property)) {
            approveButton.setEnabled(true);
            denyButton.setEnabled(true);
            commitButton.setEnabled(true);
        }
    }
}


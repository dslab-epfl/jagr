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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.border.SoftBevelBorder;
import javax.swing.table.*;
import java.util.*;

/**
 * Used to display all the orders (completed/approved/denied).  The user can
 * not chagne the status of the orders via this display.
 *
 * @author Joshua Outwater
 */
public class OrdersViewPanel extends JPanel implements PropertyChangeListener {
    private JTable orderTable;
    private TableModel tableModel;
    private TableSorter sorter;

    public OrdersViewPanel(TableModel tableModel) {
        this.tableModel = tableModel;
        createUI();
    }

    private void createUI() {
        setLayout(new BorderLayout());
        setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
        add(createTable(), BorderLayout.CENTER);
    }

    private JComponent createTable() {
        sorter = new TableSorter(tableModel);
        orderTable = new JTable(sorter);
        sorter.addMouseListenerToHeaderInTable(orderTable);
        orderTable.getTableHeader().setReorderingAllowed(false);
        orderTable.setRowSelectionAllowed(true);
        orderTable.setBorder(BorderFactory.createEtchedBorder());
        return new JScrollPane(orderTable);
    }

    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if (DataSource.ORDER_DATA_CHANGED.equals(property)) {
            sorter.tableChanged(null);
        }
    }
}


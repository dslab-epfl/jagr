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
import java.util.*;

/**
 * Administration client that allows users to modify order information and also
 * view sales information.  Data is transferred using a client-server model.
 * TODO: If no network connection is currently available the data will be
 * cached locally until a network connection is available.
 *
 * @author Joshua Outwater
 */
public class PetStoreAdminClient extends JFrame
        implements PropertyChangeListener {
    private static ResourceBundle bundle;
    private DataSource dataSource;
    private JTabbedPane ordersTabbedPane;
    private JTabbedPane salesTabbedPane;
    private JToggleButton ordersToggleButton;
    private JToggleButton salesToggleButton;
    private MouseHandler mouseHandler = new MouseHandler();
    private About aboutDialog = null;
    private static final String[] developerNames = {
        "Joshua Outwater", "Hans Muller", "Vijay Ramachandran",
        "Shannon Hickey", "Jeff Dinkins", "Mark Davidson", "Rene Schmidt",
        "Norbert Lindenberg"};

    /* Actions used for the menu bar and toolbar. */
    private AbstractAction exitAction = new ExitAction();
    private AbstractItemAction ordersAction = new OrdersAction();
    private AbstractItemAction salesAction = new SalesAction();
    private AbstractAction aboutAction = new AboutAction();
    private DataSource.RefreshAction refreshAction;

    /**
     * Create a new administration client with the specified server
     * information.
     *
     * @param hostname Hostname
     * @param port Port on the host where the j2ee server is running.
     * @param sessionID Unique ID for this session.
     */
    public PetStoreAdminClient(String petStoreProxyClassName, String hostname, String port, String endpoint) {
        super(getString("PetStore.title"));
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set up the data connection.
        dataSource = new DataSource(this, petStoreProxyClassName, hostname, port, endpoint);

        createUI();
        setVisible(true);

        // Force a refresh so we get some initial data.
        refreshAction.actionPerformed(null);
    }

    private void about() {
        if (aboutDialog == null) {
            aboutDialog = new About(getString("About.title"),
                getString("About.message"), developerNames, this, true);
        }
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setVisible(true);
    }

    private void createUI() {
        getContentPane().setLayout(new BorderLayout());

        // Create the sales orders pane.
        OrdersViewPanel ordersViewPanel = new OrdersViewPanel(
            dataSource.getOrdersViewTableModel());
        OrdersApprovePanel ordersApprovePanel = new OrdersApprovePanel(
            dataSource.getOrdersApproveTableModel());
        ordersTabbedPane = new JTabbedPane();
        ordersTabbedPane.add(getString("OrdersViewPanel.title"),
            ordersViewPanel);
        ordersTabbedPane.add(getString("OrdersApprovePanel.title"),
            ordersApprovePanel);

        // Create the sales tabbed pane.
        PieChartPanel pieChartPanel =
            new PieChartPanel(dataSource.getPieChartModel());
        BarChartPanel barChartPanel =
            new BarChartPanel(dataSource.getBarChartModel());
        salesTabbedPane = new JTabbedPane();
        salesTabbedPane.add(getString("PieChart.title"), pieChartPanel);
        salesTabbedPane.add(getString("BarChart.title"), barChartPanel);

        setJMenuBar(createMenuBar());
        getContentPane().add(createToolBar(), BorderLayout.NORTH);
        getContentPane().add(ordersTabbedPane, BorderLayout.CENTER);
        getContentPane().add(StatusBar.getInstance(), BorderLayout.SOUTH);


        // Install listeners for the events this class is interested in.
        dataSource.addPropertyChangeListener(DataSource.ENABLE_ACTIONS, this);
        dataSource.addPropertyChangeListener(DataSource.DISABLE_ACTIONS, this);

        // Install listeners for the orders view panel.
        dataSource.addPropertyChangeListener(DataSource.ORDER_DATA_CHANGED,
            ordersViewPanel);

        // Install listeners for the orders approve panel.
        dataSource.addPropertyChangeListener(DataSource.ORDER_DATA_CHANGED,
            ordersApprovePanel);
        dataSource.addPropertyChangeListener(DataSource.ENABLE_ACTIONS,
            ordersApprovePanel);
        dataSource.addPropertyChangeListener(DataSource.DISABLE_ACTIONS,
            ordersApprovePanel);

        // Install the listeners for the pie chart panel.
        dataSource.addPropertyChangeListener(DataSource.PIE_CHART_DATA_CHANGED,
            pieChartPanel);
        dataSource.addPropertyChangeListener(DataSource.ENABLE_ACTIONS,
            pieChartPanel);
        dataSource.addPropertyChangeListener(DataSource.DISABLE_ACTIONS,
            pieChartPanel);

        // Install the listeners for the bar chart panel.
        dataSource.addPropertyChangeListener(DataSource.BAR_CHART_DATA_CHANGED,
            barChartPanel);
        dataSource.addPropertyChangeListener(DataSource.ENABLE_ACTIONS,
            barChartPanel);
        dataSource.addPropertyChangeListener(DataSource.DISABLE_ACTIONS,
            barChartPanel);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu(getString("FileAction.name"));
        menu.setMnemonic(getMnemonic("FileAction.mnemonic"));
        JMenuItem menuItem = new JMenuItem(exitAction);
        menuItem.setMnemonic(getMnemonic("ExitAction.mnemonic"));
        menuItem.addMouseListener(mouseHandler);
        menu.add(menuItem);
        menuBar.add(menu);

        menu = new JMenu(getString("ViewAction.name"));
        menu.setMnemonic(getMnemonic("ViewAction.mnemonic"));

        ButtonGroup bg = new ButtonGroup();
        menuItem = new JRadioButtonMenuItem(ordersAction);
        menuItem.setMnemonic(getMnemonic("OrdersAction.mnemonic"));
        menuItem.setToolTipText(
            (String)ordersAction.getValue(Action.SHORT_DESCRIPTION));
        menuItem.addMouseListener(mouseHandler);
        menuItem.addItemListener(ordersAction);
        ordersAction.addPropertyChangeListener(
            new ToggleActionPropertyChangeListener(menuItem));
        menuItem.setSelected(true);
        bg.add(menuItem);
        menu.add(menuItem);

        menuItem = new JRadioButtonMenuItem(salesAction);
        menuItem.setMnemonic(getMnemonic("SalesAction.mnemonic"));
        menuItem.addMouseListener(mouseHandler);
        menuItem.addItemListener(salesAction);
        salesAction.addPropertyChangeListener(
            new ToggleActionPropertyChangeListener(menuItem));
        menu.add(menuItem);
        bg.add(menuItem);
        menuBar.add(menu);

        return menuBar;
    }

    private JToolBar createToolBar() {
        refreshAction = dataSource.getRefreshAction();

        JToolBar toolBar = new JToolBar();

        ordersToggleButton = new JToggleButton(ordersAction);
        ordersToggleButton.setToolTipText(
            (String)ordersAction.getValue(Action.SHORT_DESCRIPTION));
        ordersToggleButton.setText(null);
        ordersToggleButton.addMouseListener(mouseHandler);
        ordersToggleButton.addItemListener(ordersAction);
        ordersAction.addPropertyChangeListener(
            new ToggleActionPropertyChangeListener(ordersToggleButton));
        toolBar.add(ordersToggleButton);

        salesToggleButton = new JToggleButton(salesAction);
        salesToggleButton.setToolTipText(
            (String)salesAction.getValue(Action.SHORT_DESCRIPTION));
        salesToggleButton.setText(null);
        salesToggleButton.addMouseListener(mouseHandler);
        salesToggleButton.addItemListener(salesAction);
        salesAction.addPropertyChangeListener(
            new ToggleActionPropertyChangeListener(salesToggleButton));
        toolBar.add(salesToggleButton);

        toolBar.addSeparator();

        ButtonGroup group = new ButtonGroup();
        group.add(ordersToggleButton);
        group.add(salesToggleButton);
        ordersToggleButton.setSelected(true);

        JButton button = toolBar.add(refreshAction);
        button.setToolTipText(
            (String)refreshAction.getValue(Action.SHORT_DESCRIPTION));
        button.addMouseListener(mouseHandler);

        button = toolBar.add(aboutAction);
        button.setToolTipText(
            (String)aboutAction.getValue(Action.SHORT_DESCRIPTION));
        button.addMouseListener(mouseHandler);

        return toolBar;
    }

    private static ResourceBundle getResourceBundle() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle("resources.petstore");
        }
        return bundle;
    }

    /**
     * Retrieve a localized string from our resource bundle.
     *
     * @param key Key for the string we want to retrieve.
     * @return Localized string.
     */
    public static String getString(String key) {
        String result = null;
        try {
            result = getResourceBundle().getString(key);
        } catch (MissingResourceException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Retrieve a localized mnemonic character from our resource bundle.
     *
     * @param key Key for the mnemonic character we want to retrieve.
     * @return Localized mnemonic character.
     */
    public static char getMnemonic(String key) {
        return (getString(key)).charAt(0);
    }

    /**
     * Retrieve an integer from our resource bundle.
     *
     * @param key Key for the integer we want to retrieve.
     * @return Integer value.
     */
    public static int getInteger(String key) {
        int result = -1;
        try {
            result = Integer.parseInt(getString(key));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();

        if (DataSource.DISABLE_ACTIONS.equals(property)) {
            refreshAction.setEnabled(false);
            aboutAction.setEnabled(false);
            exitAction.setEnabled(false);
        } else if (DataSource.ENABLE_ACTIONS.equals(property)) {
            refreshAction.setEnabled(true);
            aboutAction.setEnabled(true);
            exitAction.setEnabled(true);
        }
    }

    private class MouseHandler extends MouseAdapter {
        public MouseHandler() {
        }

        public void mouseEntered(MouseEvent evt) {
            if (evt.getSource() instanceof AbstractButton) {
                AbstractButton button = (AbstractButton)evt.getSource();
                Action action = button.getAction();

                if (action != null) {
                    // Set the status bar message.
                    StatusBar.getInstance().setMessage(
                        (String)action.getValue(Action.LONG_DESCRIPTION));
                }
            }
        }

        public void mouseExited(MouseEvent e) {
            StatusBar.getInstance().setMessage(null);
        }
    }

    protected class ExitAction extends AbstractAction {
        public ExitAction() {
            super(getString("ExitAction.name"));
            putValue(SHORT_DESCRIPTION, getString("ExitAction.tooltip"));
            putValue(LONG_DESCRIPTION, getString("ExitAction.description"));
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    protected class OrdersAction extends AbstractItemAction {
        public OrdersAction() {
            super(getString("OrdersAction.name"));
            putValue(SHORT_DESCRIPTION, getString("OrdersAction.tooltip"));
            putValue(LONG_DESCRIPTION, getString("OrdersAction.description"));
            putValue(SMALL_ICON, new ImageIcon(
                getClass().getResource("/resources/orders.gif")));
        }

        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // Remove sales panel.
                    if (salesTabbedPane.isShowing()) {
                        getContentPane().remove(salesTabbedPane);
                    }
                    // Add tabbed pane.
                    if (!ordersTabbedPane.isShowing()) {
                        getContentPane().add(ordersTabbedPane,
                            BorderLayout.CENTER);
                    }
                    validate();
                    repaint();
                }
            });
        }

        public void itemStateChanged(ItemEvent evt)  {
            boolean show;

            if (evt.getStateChange() == ItemEvent.SELECTED) {
                show = true;
            } else {
                show = false;
            }

            // Update all objects that share this item
            setSelected(show);
        }
    }

    protected class SalesAction extends AbstractItemAction {
        public SalesAction() {
            super(getString("SalesAction.name"));
            putValue(SHORT_DESCRIPTION, getString("SalesAction.tooltip"));
            putValue(LONG_DESCRIPTION, getString("SalesAction.description"));
            putValue(SMALL_ICON, new ImageIcon(
                getClass().getResource("/resources/sales.gif")));
        }

        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //Remove tabbed pane.
                    if (ordersTabbedPane.isShowing()) {
                        getContentPane().remove(ordersTabbedPane);
                    }
                    // Add sales panel.
                    if (!salesTabbedPane.isShowing()) {
                        getContentPane().add(salesTabbedPane,
                            BorderLayout.CENTER);
                    }
                    validate();
                    repaint();
                }
            });
        }

        public void itemStateChanged(ItemEvent evt)  {
            boolean show;

            if (evt.getStateChange() == ItemEvent.SELECTED) {
                show = true;
            } else {
                show = false;
            }

            // Update all objects that share this item
            setSelected(show);
        }
    }

    protected class AboutAction extends AbstractAction {
        public AboutAction() {
            super(getString("AboutAction.name"));
            putValue(SHORT_DESCRIPTION, getString("AboutAction.tooltip"));
            putValue(LONG_DESCRIPTION, getString("AboutAction.description"));
            putValue(SMALL_ICON, new ImageIcon(
                getClass().getResource("/resources/About24.gif")));
        }

        public void actionPerformed(ActionEvent e) {
            about();
        }
    }

    public static void main(String args[]) {
        if (args.length != 4) {
            System.err.println("Usage: java PetStoreAdminClient <class name for petstore proxy> <hostname> " +
                "<port> <session id>");
            System.exit(1);
        }
        new PetStoreAdminClient(args[0], args[1], args[2], args[3]);
    }
}


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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.SoftBevelBorder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Panel that displays a pie chart defined by the given data model.
 * TODO: Refactor panel code to support both chart types.
 *
 * @author Joshua Outwater
 */
public class PieChartPanel extends JPanel implements PropertyChangeListener {
    private PieChart pieChart;
    private DataSource.PieChartModel pieChartModel;
    private JTextField startDateTxtField;
    private JTextField endDateTxtField;
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private DecimalFormat df;
    private JButton submitButton;

    public PieChartPanel(DataSource.PieChartModel pieChartModel) {
        // Create and set up the decimal format to format floating point
        // numbers to 2 decimal precision.
        df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        this.pieChartModel = pieChartModel;
        createUI();
    }

    private void createUI() {
        setLayout(new BorderLayout());
        setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel(PetStoreAdminClient.getString(
            "PieChart.description") + " "
            + PetStoreAdminClient.getString("Chart.from")));

        startDateTxtField = new JTextField(dateFormat.format(
            pieChartModel.getStartDate()));
        panel.add(startDateTxtField);
        panel.add(new JLabel(PetStoreAdminClient.getString("Chart.to")));

        endDateTxtField = new JTextField(dateFormat.format(
            pieChartModel.getEndDate()));
        panel.add(endDateTxtField);
        add(panel, BorderLayout.NORTH);

        submitButton =
            new JButton(PetStoreAdminClient.getString("SubmitButton.text"));
        panel.add(submitButton);
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateModelDates();
            }
        });

        pieChart = new PieChart(pieChartModel);
        add(pieChart, BorderLayout.CENTER);
    }

    private void updateModelDates() {
        Date startDate = null;
        Date endDate = null;

        // Make sure that the dates entered in the text field are valid.  If
        // they are not prompt the user to fix them.
        try {
            startDate =
                dateFormat.parse(startDateTxtField.getText());
            endDate =
                dateFormat.parse(endDateTxtField.getText());
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this,
                PetStoreAdminClient.getString("DateFormatErrorDialog.message"),
                PetStoreAdminClient.getString("DateFormatErrorDialog.title"),
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Setting the dates will automatically retrieve the data from the
        // server.
        pieChartModel.setDates(startDate, endDate);
    }

    /**
     * Catch property changes for the pie chart data model and force a
     * repaint.
     */
    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();

        if (DataSource.DISABLE_ACTIONS.equals(property)) {
            submitButton.setEnabled(false);
        } else if (DataSource.ENABLE_ACTIONS.equals(property)) {
            submitButton.setEnabled(true);
        } else if (DataSource.PIE_CHART_DATA_CHANGED.equals(property)) {
            pieChart.repaint();
        }
    }

    /**
     * Displays a pie chart of total sales (in $s) of a category divided into
     * sales (in $s) per item of that category in a given period of days.
     */
    public class PieChart extends Chart {

        private Arc2D.Float[] arcs;
        private String[] keys;
        private JToolTip toolTip = new JToolTip();
        private float[] percentages;
        private int CHART_INDENT =
            PetStoreAdminClient.getInteger("PieChart.indent");

        /**
         * Create an instance of the PieChart class.
         *
         * @param pieChartModel Model to retrieve data from.
         * @param width Width of the pie chart.
         * @param height Height of the pie chart.
         */
        public PieChart(DataSource.PieChartModel pieChartModel) {
            super(pieChartModel);
        }

        public JToolTip createToolTip() {
            toolTip.setComponent(this);
            return toolTip;
        }

        protected void renderLegend(Graphics2D g2) {
            FontMetrics fm = g2.getFontMetrics();
            int boxWH = fm.getHeight();
            int y = 10;
            Color oldColor = g2.getColor();
            for (int i = 0; i < keys.length; i++) {
                // Draw a box.
                g2.setColor(colorList[i % keys.length]);
                g2.fillRect(10, y, boxWH, boxWH);
                // Draw the string.
                g2.setColor(Color.black);
                g2.drawString(keys[i], 10 + boxWH + 2,
                    y + boxWH - fm.getDescent());
                y += boxWH + 2;
            }
            g2.setColor(oldColor);
        }

        public void renderChart(Graphics2D g2) {
            keys = chartModel.getKeys();
            if (keys == null) {
                return;
            }

            calculateTotals();
            percentages = new float[keys.length];

            for (int i = 0; i < keys.length; i++) {
                percentages[i] =
                    chartModel.getRevenue(keys[i]) / totalRevenue * 100f;
            }

            int width = getWidth() - CHART_INDENT;
            int height = getHeight() - CHART_INDENT;
            float pieWH = (float)width;
            float startX = 20f;
            float startY = 20f;

            // Keep the perspective ratio on the pie chart 1:1
            if (width < height) {
                pieWH = (float)width;
                startY += (float)((height - width) / 2);
            } else if (height < width) {
                pieWH = (float)height;
                startX += (float)((width - height) / 2);
            }

            float angle = 0f;
            Color oldColor;
            arcs = new Arc2D.Float[percentages.length];
            for (int i = 0; i < percentages.length; i++) {
                // Convert percentage to coordinate system
                // (percent * 360 / 100).
                float extent = percentages[i] * 360f / 100f;
                arcs[i] = new Arc2D.Float(startX, startY, pieWH, pieWH,
                        angle, extent, Arc2D.PIE);
                angle += extent;

                oldColor = g2.getColor();
                g2.setColor(colorList[i % percentages.length]);
                g2.fill(arcs[i]);
                g2.setColor(Color.black);
                g2.draw(arcs[i]);
                g2.setColor(oldColor);
            }
            renderLegend(g2);
        }

        public boolean contains(int x, int y) {
            boolean result = false;
            if (arcs == null) {
                return result;
            }

            for (int i = 0; i < arcs.length; i++) {
                if (arcs[i].contains(x, y)) {
                    setToolTipText(keys[i] + " ("
                        + df.format(percentages[i]) + "%)");
                    result = true;
                }
            }
            return result;
        }

        public void setToolTipText(String tip) {
            toolTip.setTipText(tip);
            super.setToolTipText(tip);
        }

        public Point getToolTipLocation(MouseEvent e) {
            return new Point(e.getX(), e.getY() - toolTip.getHeight());
        }

        public String toString() {
            return "Pie Chart";
        }
    }
}


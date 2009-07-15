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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.SoftBevelBorder;

/**
 * Panel that displays a bar chart defined by the given data model.
 * TODO: Refactor panel code to support both chart types.
 *
 * @author Joshua Outwater
 */
public class BarChartPanel extends JPanel implements PropertyChangeListener {
    private BarChart barChart;
    private DataSource.BarChartModel barChartModel;
    private JTextField startDateTxtField;
    private JTextField endDateTxtField;
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private JButton submitButton;

    public BarChartPanel(DataSource.BarChartModel barChartModel) {
        this.barChartModel = barChartModel;
        createUI();
    }

    private void createUI() {
        setLayout(new BorderLayout());
        setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel(PetStoreAdminClient.getString(
            "BarChart.description") + " "
            + PetStoreAdminClient.getString("Chart.from")));
        startDateTxtField = new JTextField(dateFormat.format(
            barChartModel.getStartDate()));
        panel.add(startDateTxtField);
        panel.add(new JLabel(PetStoreAdminClient.getString("Chart.to")));
        endDateTxtField = new JTextField(dateFormat.format(
            barChartModel.getEndDate()));
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


        barChart = new BarChart(barChartModel);
        add(barChart, BorderLayout.CENTER);
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
        barChartModel.setDates(startDate, endDate);
    }

    /**
     * Catch property changes for the bar chart data model and force a
     * repaint.
     */
    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();

        if (DataSource.DISABLE_ACTIONS.equals(property)) {
            submitButton.setEnabled(false);
        } else if (DataSource.ENABLE_ACTIONS.equals(property)) {
            submitButton.setEnabled(true);
        } else if (DataSource.BAR_CHART_DATA_CHANGED.equals(property)) {
            barChart.repaint();
        }
    }

    /**
     * Displays a bar chart of the toal sales (in #s) divided into sales (in #s)
     * per category in a given period of days.
     */
    public class BarChart extends Chart {
        /** # of pixels to indent for the left and right of the bar chart. */
        private int INSET_LEFT;
        /** # of pixels to indent for the top and bottom of the bar chart. */
        private int INSET_TOP;
        /** # of tics to display on the y-axis for the range of values. */
        private final int NUM_TICS =
            PetStoreAdminClient.getInteger("BarChart.numTics");
        private final int TIC_LEN =
            PetStoreAdminClient.getInteger("BarChart.ticLen");
        private final int EMPTY_SPACE =
            PetStoreAdminClient.getInteger("BarChart.emptySpace");
        private float maxVal = 0f;
        private float barWidth = 0f;
        private float valInterval = 0f;
        private JToolTip toolTip = new JToolTip();
        private Rectangle[] bars;
        private String[] keys;

        /**
         * Creates a new bar chart defined by the model passed in.
         *
         * @param barChartModel The model to retrieve data from.
         * @param width Width of the chart in pixels.
         * @param height Height of the chart in pixels.
         */
        public BarChart(DataSource.BarChartModel barChartModel) {
            super(barChartModel);
        }

        /**
         * Calculates all the constants needed to draw the bar chart.
         */
        private void determineChartValues(Graphics2D g2) {
            // Already checked for null in caller renderChart.
            maxVal = 0f;
            for (int i = 0; i < keys.length; i++) {
                if (chartModel.getOrders(keys[i]) > maxVal) {
                    maxVal = chartModel.getOrders(keys[i]);
                }
            }
            valInterval = maxVal / (NUM_TICS - 1);

            float val = 0f;
            FontMetrics fm = g2.getFontMetrics();

            // Find the largest label on the y-axis so we can determine the
            // amount to indent the left and right sides.
            int maxStrLen = 0;
            int currStrLen = 0;
            for (int i = 0; i < keys.length; i++) {
                currStrLen = fm.stringWidth(Float.toString(val));
                if (currStrLen > maxStrLen) {
                    maxStrLen = currStrLen;
                }
                val += valInterval;
            }

            // Find the max height of the font being used to determine the
            // amount to indent the top and bottom of the chart.
            int maxStrHeight = fm.getHeight();
            INSET_LEFT = maxStrLen + (2 * EMPTY_SPACE) + TIC_LEN;
            INSET_TOP = maxStrHeight + (2 * EMPTY_SPACE) + TIC_LEN;

            barWidth = ((float)getWidth() - 2 * INSET_LEFT)
                / (float)keys.length;
        }

        protected void drawAxes(Graphics2D g2) {
            // Already checked for null in caller renderChart.
            Color oldColor = g2.getColor();
            int height = getHeight();
            int width = getWidth();
            float val = 0f;
            FontMetrics fm = g2.getFontMetrics();
            int maxStrHeight = fm.getHeight();

            g2.setColor(Color.black);

            // Draw y-axis.
            g2.drawLine(INSET_LEFT, INSET_TOP, INSET_LEFT, height - INSET_TOP);

            // Draw x-axis.
            g2.drawLine(INSET_LEFT, height - INSET_TOP,
                width - INSET_LEFT, height - INSET_TOP);

            // Draw tics and labels on x-axis.
            int strLen = 0;
            int x = INSET_LEFT + (int)(barWidth / 2);
            for (int i = 0; i < keys.length; i++) {
                g2.drawLine(x, height - INSET_TOP,
                    x, height - INSET_TOP + TIC_LEN);
                strLen = fm.stringWidth(keys[i]);
                g2.drawString(keys[i], x - strLen / 2,
                    height - INSET_TOP + (fm.getMaxAscent()) + EMPTY_SPACE
                    + TIC_LEN);
                x += (int)barWidth;
            }

            //Draw tics and labels on y-axis.
            int y = height - INSET_TOP;
            x = INSET_LEFT;
            int ticInterval = (height - (2 * INSET_TOP)) / (NUM_TICS - 1);
            String str;
            for (int i = 0; i < NUM_TICS - 1; i++) {
                g2.drawLine(INSET_LEFT, y, INSET_LEFT - TIC_LEN, y);
                strLen = fm.stringWidth(str = Float.toString(val));
                g2.drawString(str, x - strLen - EMPTY_SPACE - TIC_LEN,
                    y + (maxStrHeight / 2));
                y -= ticInterval;
                val += valInterval;

            }
            // The last tic is drawn from the known axis point due to round-off
            // errors in the ticInterval that may have accumulated.  This
            // doesn't look great...  A fix for this is to spread the error by
            // finding the center tic first and solve the rest by divide and
            // conquer.
            g2.drawLine(INSET_LEFT, INSET_TOP,
                INSET_LEFT - TIC_LEN, INSET_TOP);
            strLen = fm.stringWidth(str = Float.toString(val));
            g2.drawString(str, x - strLen - EMPTY_SPACE - TIC_LEN,
                y + (maxStrHeight / 2));

            g2.setColor(oldColor);
        }

        public void renderChart(Graphics2D g2) {
            keys = chartModel.getKeys();
            if (keys == null) {
                // TODO: Draw a message saying we have no data.
                return;
            }

            calculateTotals();
            bars = new Rectangle[keys.length];
            determineChartValues(g2);

            float x = INSET_LEFT + 1;
            float y = getHeight() - INSET_TOP;
            float maxHeight = getHeight() - (2 * INSET_TOP);
            Color oldColor;
            for (int i = 0; i < keys.length; i++) {
                float barHeight = (chartModel.getOrders(keys[i]) * maxHeight)
                    / maxVal;
                oldColor = g2.getColor();
                g2.setColor(colorList[i % keys.length]);
                bars[i] = new Rectangle((int)x, (int)(maxHeight - barHeight
                    + INSET_TOP), (int)barWidth, (int)barHeight);
                g2.fill3DRect(bars[i].x, bars[i].y, bars[i].width,
                    bars[i].height, true);
                x += barWidth;
                g2.setColor(oldColor);
            }
            drawAxes(g2);
        }

        public JToolTip createToolTip() {
            toolTip.setComponent(this);
            return toolTip;
        }

        public boolean contains(int x, int y) {
            boolean result = false;
            if (bars == null) {
                return result;
            }

            for (int i = 0; i < bars.length; i++) {
                if (bars[i].contains(x, y)) {
                    setToolTipText(((int)chartModel.getOrders(keys[i]))
                        + " orders");
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
            return "Bar Chart";
        }
    }
}


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
import java.awt.image.*;
import javax.swing.*;

/**
 * Abstract class used for defining various charts in this application.
 *
 * @author Joshua Outwater
 */
public abstract class Chart extends JPanel {
    /**
     * List of colors that can be used by the various charts.
     */
    public static final Color colorList[] = { new Color(170, 170, 178),
        new Color(170, 139, 232), new Color(229, 223, 129),
        new Color(136, 184, 224), new Color(142, 232, 158) };
    protected float totalRevenue = 0f;
    protected int totalOrders = 0;
    protected DataSource.ChartModel chartModel;

    /**
     * Creates a new instance of the Chart class with the specified data model.
     *
     * @param chartModel The model to use for retrieving data.
     * @param width Width of the chart.
     * @param height Height of the chart.
     */
    public Chart(DataSource.ChartModel chartModel) {
        setBackground(new Color(196, 194, 195));
        this.chartModel = chartModel;
    }

    protected abstract void renderChart(Graphics2D g2);

    protected void calculateTotals() {
        String keys[] = chartModel.getKeys();
        if (keys == null) {
            return;
        }

        totalRevenue = 0f;
        totalOrders = 0;

        for (int i = 0; i < keys.length; i++) {
            totalRevenue += chartModel.getRevenue(keys[i]);
            totalOrders += chartModel.getOrders(keys[i]);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!(g instanceof Graphics2D)) {
            return;
        }

        Graphics2D g2 = (Graphics2D)g;
        renderChart(g2);
    }
}


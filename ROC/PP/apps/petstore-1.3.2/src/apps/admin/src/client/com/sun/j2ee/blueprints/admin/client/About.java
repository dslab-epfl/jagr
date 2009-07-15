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

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * About dialog that displays a short string of info
 * and a scrolling list of names...with a waving Duke!
 *
 * @author Shannon Hickey
 */
public class About extends JDialog {

    private AboutPanel panel;

    public About(String title, String display, String[] names, Frame owner,
            boolean modal) {
        super(owner, title, modal);

        panel = new AboutPanel(names);
        panel.setLayout(new BorderLayout());
        panel.add(new TranLabel(display), BorderLayout.NORTH);

        setContentPane(panel);
        setSize(320, 150);
        setResizable(false);
    }

    public void setVisible(boolean b) {
        if (b) {
            panel.start();
        } else {
            panel.stop();
        }
        super.setVisible(b);
    }

}


class AboutPanel extends JPanel implements ActionListener {

    private static final ImageIcon[] icons = new ImageIcon[10];
    static {
        for (int i = 0; i < icons.length; i++) {
            icons[i] = new ImageIcon(About.class.getResource("/resources/duke"
                    + i + ".gif"));
        }
    }

    private static final Color bgColorA = Color.white;
    private static final Color bgColorB = new Color(120, 120, 255);
    private static final Font nameFont = new Font("Dialog", Font.BOLD, 25);

    private static final RenderingHints hints =
        new RenderingHints(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

    private int iconIndex = icons.length;

    private String[] names;

    private Timer timer = new Timer(80, this);

    private boolean fading = false;
    private int nameIndex = 0;
    private int nameY = -10;
    private float fade = 1.0f;

    private int cachedWidth = 0;
    private int cachedHeight = 0;

    private GradientPaint bgPaint = null;

    public AboutPanel(String[] names) {
        this.names = names;
        setOpaque(true);
        setBorder(BorderFactory.createLoweredBevelBorder());
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g.create();

        int xloc;
        int yloc;

        int width = getWidth();
        int height = getHeight();
        if (cachedWidth != width || cachedHeight != height || bgPaint == null) {
            cachedWidth = width;
            cachedHeight = height;
            bgPaint = new GradientPaint(0, 0, bgColorA, cachedWidth,
                cachedHeight, bgColorB);
        }

        g2d.setRenderingHints(hints);

        g2d.setPaint(bgPaint);
        g2d.fillRect(0, 0, cachedWidth, cachedHeight);

        ImageIcon icon = icons[iconIndex >= icons.length ? 0 : iconIndex];
        xloc = getInsets().left;
        yloc = cachedHeight - getInsets().bottom - 6 - icon.getIconHeight();
        icon.paintIcon(this, g2d, xloc, yloc);

        if (fading) {
            g2d.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, fade));
        } else if (nameY >= cachedHeight - getInsets().bottom - 22) {
            fading = true;
            iconIndex = 0;
        }

        Rectangle rect = nameFont.getStringBounds(names[nameIndex],
            g2d.getFontRenderContext()).getBounds();
        g2d.setFont(nameFont);
        xloc = xloc + icon.getIconWidth() + 5;
        Paint paint = new GradientPaint(xloc, nameY, Color.red, xloc
            + rect.width, nameY, Color.blue);
        g2d.setPaint(paint);
        g2d.drawString(names[nameIndex], xloc, nameY);

        g2d.dispose();
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public void actionPerformed(ActionEvent ae) {
        if (fading || iconIndex <= icons.length) {
            iconIndex++;
        }

        if (fading) {
            fade = fade - 0.05f;
            if (fade <= 0.0f) {
                nameIndex = (nameIndex + 1) % names.length;
                nameY = -10;
                fading = false;
                fade = 1.0f;
            }
        } else {
            nameY += 2;
        }

        repaint();
    }
}


class TranLabel extends JLabel {

    private static final RenderingHints hints =
        new RenderingHints(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

    private static final AlphaComposite composite =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);

    private static final Color bgColor = Color.white;

    public TranLabel(String display) {
        super();
        setHorizontalAlignment(SwingConstants.CENTER);
        setText("<html><font color=black face=\"Dialog\">" + display
            + "</font></html>");
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g.create();


        g2d.setRenderingHints(hints);

        g2d.setComposite(composite);

        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        super.paintComponent(g2d);

        g2d.dispose();
    }
}


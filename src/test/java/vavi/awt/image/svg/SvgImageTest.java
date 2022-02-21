/*
 * Copyright (c) 2017 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.svg;

import java.awt.Dimension;
import java.awt.Graphics;
import java.io.FileInputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;


/**
 * SvgImageTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 Nov 5, 2017 umjammer initial version <br>
 */
@Disabled
public class SvgImageTest {

    @Test
    public void test() {
        fail("Not yet implemented");
    }

    /**
     * @param args Usage: java svgImage <source file>
     */
    public static void main(String args[]) throws Exception {
        String filename = args[0];

        final SvgImage svg = new SvgImage(new FileInputStream(filename));

        JFrame frame = new JFrame(filename);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add("Center", new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(svg.getImage(), 0, 0, this);
            }
            public Dimension getPreferredSize() {
                return svg.getSize();
            }
        });
        frame.pack();
        frame.setVisible(true);
    }
}

/* */

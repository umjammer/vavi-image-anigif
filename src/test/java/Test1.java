/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 * Test1. Batik SVG
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2009/06/21 nsano initial version <br>
 */
public class Test1 {

    /** */
    public static void main(String[] args) throws Exception {
        final BufferedImage image = ImageIO.read(new FileInputStream(args[0]));
System.err.println(image);
        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(image, 0, 0, this);
            }
            public Dimension getPreferredSize() {
                return new Dimension(image.getWidth(), image.getHeight());
            }
        };
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}

/* */

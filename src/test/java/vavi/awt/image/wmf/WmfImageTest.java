/*
 * Copyright (c) 2017 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.wmf;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.FileInputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.fail;


/**
 * WmfImageTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 Nov 5, 2017 umjammer initial version <br>
 */
@Ignore
public class WmfImageTest {

    @Test
    public void test() {
        fail("Not yet implemented");
    }

    //----

    /** */
    public static void main(String[] args) throws Exception {
        String file = args[0];
System.err.println("file: " + file);

        final WmfImage wmf = new WmfImage(new FileInputStream(file)/*, 640, 480*/);

        final JFrame frame = new JFrame();
        frame.setTitle(file);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(wmf.getImage(), 0, 0, this);
            }
            public Dimension getPreferredSize() {
                return wmf.getSize();
            }
        });
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                wmf.setSize(frame.getSize());
                frame.repaint();
            }
        });
        frame.pack();
        frame.setVisible(true);
    }
}

/* */

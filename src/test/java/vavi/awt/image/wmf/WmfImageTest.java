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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import vavi.awt.ImageComponent;
import vavi.swing.JImageComponent;

import static org.junit.jupiter.api.Assertions.fail;


/**
 * WmfImageTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 Nov 5, 2017 umjammer initial version <br>
 */
public class WmfImageTest {

    @Test
    @Disabled
    public void test() {
        fail("Not yet implemented");
    }

    //----

    /** */
    public static void main(String[] args) throws Exception {
//        String file = args[0];
//System.err.println("file: " + file);
//        InputStream is = Files.newInputStream(Paths.get(file));

        InputStream is = WmfImageTest.class.getResourceAsStream("/test.wmf");
        final WmfImage wmf = new WmfImage(is/*, 640, 480*/);

        final JFrame frame = new JFrame();
        frame.setTitle("WMF");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageComponent ic = new ImageComponent();
        ic.setImage(wmf.getImage());
        ic.setPreferredSize(new Dimension(wmf.getImage().getWidth(null), wmf.getImage().getHeight(null)));
        frame.getContentPane().add(ic);
        frame.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                ic.repaint();
            }
        });
        frame.pack();
        frame.setVisible(true);
    }
}

/* */

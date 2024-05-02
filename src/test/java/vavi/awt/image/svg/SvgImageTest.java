/*
 * Copyright (c) 2017 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.svg;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JFrame;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import vavi.swing.JImageComponent;
import vavi.util.Debug;


/**
 * SvgImageTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 Nov 5, 2017 umjammer initial version <br>
 */
public class SvgImageTest {

    @Test
    @Disabled("wip")
    public void test() throws IOException {
        SvgImage svg = new SvgImage(SvgImageTest.class.getResourceAsStream("/tiger.svg"));
Debug.println(svg.getImage());
    }

    /**
     * @param args Usage: java svgImage <source file>
     */
    public static void main(String[] args) throws Exception {
        String filename = args[0];

        SvgImage svg = new SvgImage(Files.newInputStream(Paths.get(filename)));

        JFrame frame = new JFrame(filename);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JImageComponent ic = new JImageComponent();
        ic.setImage(svg.getImage());
        ic.setPreferredSize(new Dimension(svg.getImage().getWidth(), svg.getImage().getHeight()));
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

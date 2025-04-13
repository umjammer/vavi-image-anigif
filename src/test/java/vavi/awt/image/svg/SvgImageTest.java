/*
 * Copyright (c) 2017 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.svg;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.swing.JImageComponent;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * SvgImageTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 Nov 5, 2017 umjammer initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class SvgImageTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "svg")
    String svg = "src/test/resources/tiger.svg";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    @Disabled("wip")
    public void test() throws Exception {
        SvgImage image = new SvgImage(Files.newInputStream(Path.of(svg)));
Debug.println(image.getImage());
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    public void test4() throws Exception {
        SvgImage image = new SvgImage(Files.newInputStream(Path.of(svg)));
        show(image.getImage());
    }

    /** */
    static void show(BufferedImage image) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        JFrame frame = new JFrame("SVG");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                cdl.countDown();
            }
        });
        JImageComponent ic = new JImageComponent();
        ic.setImage(image);
        ic.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        frame.getContentPane().add(ic);
        frame.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                ic.repaint();
            }
        });
        frame.pack();
        frame.setVisible(true);
        cdl.await();
    }

    /**
     * @param args 0: svg file
     */
    public static void main(String[] args) throws Exception {
//        String filename = args[0];
        SvgImageTest app = new SvgImageTest();
        app.setup();

        SvgImage svg = new SvgImage(Files.newInputStream(Paths.get(app.svg)));

        show(svg.getImage());
    }
}

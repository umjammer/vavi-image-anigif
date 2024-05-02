/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.svg;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFrame;

import org.junit.jupiter.api.Test;
import vavi.swing.JImageComponent;
import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * SvgImageReaderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/09/26 nsano initial version <br>
 */
class SvgImageReaderTest {

    @Test
    void test1() throws Exception {
        BufferedImage image = ImageIO.read(SvgImageReaderTest.class.getResource("/tiger.svg"));
        assertNotNull(image);
    }

    @Test
    void test2() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("svg").next();
        ImageReadParam param = reader.getDefaultReadParam();
        param.setSourceRenderSize(new Dimension(2000, 2000));
        // #createImageInputStream() doesn't accept url
        ImageInputStream iis = ImageIO.createImageInputStream(SvgImageReaderTest.class.getResourceAsStream("/tiger.svg"));
        assert iis != null : "is resource class correct?";
        reader.setInput(iis, true);
        BufferedImage image = reader.read(0, param);
        assertNotNull(image);
    }

    @Test
    void test3() throws Exception {
        System.setProperty("vavi.imageio.svg.BatikSvgImageReadParam.size", "123x456");
        BufferedImage image = ImageIO.read(SvgImageReaderTest.class.getResource("/tiger.svg"));
        assertEquals(123, image.getWidth());
        assertEquals(456, image.getHeight());
    }

    /**
     * @param args Usage: java svgImage <source file>
     */
    public static void main(String[] args) throws Exception {
//        String filename = args[0];

        ImageReader reader = ImageIO.getImageReadersByFormatName("svg").next();
        ImageReadParam param = reader.getDefaultReadParam();
        param.setSourceRenderSize(new Dimension(2000, 2000));
        // #createImageInputStream() doesn't accept url
        ImageInputStream iis = ImageIO.createImageInputStream(new URL("https://glyphwiki.org/glyph/u2b81b.svg").openStream());
//        ImageInputStream iis = ImageIO.createImageInputStream(SvgImageReaderTest.class.getResourceAsStream("/tiger.svg"));
        assert iis != null : "is resource class correct?";
        reader.setInput(iis, true);
        BufferedImage image = reader.read(0, param);
Debug.println("svg: " + image.getWidth() + "x" + image.getHeight());
        JFrame frame = new JFrame("SVG");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JImageComponent ic = new JImageComponent();
        ic.setImage(image);
        ic.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        frame.getContentPane().add(ic);
        frame.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                ic.repaint();
                Debug.println(ic.getSize());
            }
        });
        frame.pack();
        frame.setVisible(true);
    }
}
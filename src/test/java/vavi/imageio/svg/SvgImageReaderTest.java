/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.svg;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFrame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import org.apache.poi.util.IOUtils;
import vavi.imageio.IIOUtil;
import vavi.swing.JImageComponent;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * SvgImageReaderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/09/26 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class SvgImageReaderTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "svg")
    String svg = "src/test/resources/tiger.svg";

    @Property(name = "glyph")
    String glyph = "https://glyphwiki.org/glyph/u2b81b.svg";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    void test1() throws Exception {
        BufferedImage image = ImageIO.read(Path.of(svg).toFile());
        assertNotNull(image);
    }

    @Test
    void test2() throws Exception {
        ImageReader reader = IIOUtil.getImageReader("svg", "vavi.imageio.svg.BatikSvgImageReader");
        ImageReadParam param = reader.getDefaultReadParam();
        param.setSourceRenderSize(new Dimension(1200, 1200));
        // #createImageInputStream() doesn't accept url
        ImageInputStream iis = ImageIO.createImageInputStream(Path.of(svg).toFile());
        assert iis != null : "is resource class correct?";
        reader.setInput(iis, true);
        BufferedImage image = reader.read(0, param);
        assertNotNull(image);
    }

    @Test
    void test3() throws Exception {
        IIOUtil.setOrder(ImageReaderSpi.class, "vavi.imageio.svg.BatikSvgImageReaderSpi", "vavi.imageio.svg.SvgImageReaderSpi");
        System.setProperty("vavi.imageio.svg.BatikSvgImageReadParam.size", "123x456");
Debug.println(svg);
        BufferedImage image = ImageIO.read(Path.of(svg).toFile());
        assertEquals(123, image.getWidth());
        assertEquals(456, image.getHeight());
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test4() throws Exception {
        ImageReader reader = IIOUtil.getImageReader("svg", "vavi.imageio.svg.SvgImageReader");
        ImageReadParam param = reader.getDefaultReadParam();
        param.setSourceRenderSize(new Dimension(1200, 1200));
        // #createImageInputStream() doesn't accept url
        ImageInputStream iis = ImageIO.createImageInputStream(Path.of(svg).toFile());
        assert iis != null : svg + ", is the file correct?";
        reader.setInput(iis, true);
        BufferedImage image = reader.read(0, param);
        show(image);
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
     * @param args Usage: java svgImage <source file>
     */
    public static void main(String[] args) throws Exception {
//        String filename = args[0];
        SvgImageReaderTest app = new SvgImageReaderTest();
        app.setup();

//        ImageReader reader = IIOUtil.getImageReader("svg", "vavi.imageio.svg.BatikSvgImageReader");
        ImageReader reader = IIOUtil.getImageReader("svg", "vavi.imageio.svg.SvgImageReader");
        ImageReadParam param = reader.getDefaultReadParam();
        param.setSourceRenderSize(new Dimension(1200, 1200));
        // #createImageInputStream() doesn't accept url
        ImageInputStream iis = ImageIO.createImageInputStream(new URL(app.glyph).openStream());
//        ImageInputStream iis = ImageIO.createImageInputStream(SvgImageReaderTest.class.getResourceAsStream("/tiger.svg"));
        assert iis != null : "is resource class correct?";
        reader.setInput(iis, true);
        BufferedImage image = reader.read(0, param);
Debug.println("svg: " + image.getWidth() + "x" + image.getHeight());

        show(image);
    }
}
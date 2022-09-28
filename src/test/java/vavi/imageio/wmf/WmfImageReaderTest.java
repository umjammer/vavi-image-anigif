/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.wmf;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFrame;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.wmf.tosvg.WMFTranscoder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sun.rmi.rmic.IndentingWriter;
import vavi.io.InputEngine;
import vavi.io.InputEngineOutputStream;
import vavi.swing.JImageComponent;
import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * WmfImageReaderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/09/26 nsano initial version <br>
 */
class WmfImageReaderTest {

    @Test
    @Disabled("wip")
    void test0() throws Exception {
        InputStream is = WmfImageReaderTest.class.getResourceAsStream("/test.wmf");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        TranscoderInput input = new TranscoderInput(is);
        TranscoderOutput output = new TranscoderOutput(new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8)));
        WMFTranscoder transcoder = new WMFTranscoder();
        transcoder.addTranscodingHint(WMFTranscoder.KEY_WIDTH, 400f);
        transcoder.addTranscodingHint(WMFTranscoder.KEY_WIDTH, 400f);
        transcoder.transcode(input, output);

        BufferedImage[] imageA = new BufferedImage[1];
        ImageTranscoder imageTranscoder = new ImageTranscoder() {
            @Override public BufferedImage createImage(int width, int height) {
                return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }
            @Override public void writeImage(BufferedImage image, TranscoderOutput output) throws TranscoderException {
                imageA[0] = image;
            }
        };
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
Debug.println(baos.toString());
        TranscoderInput input2 = new TranscoderInput(bais);
        imageTranscoder.transcode(input2, null);
Debug.println(imageA[0]);
    }

    @Test
    @Disabled("wip")
    void test1() throws Exception {
        BufferedImage image = ImageIO.read(WmfImageReaderTest.class.getResource("/test.wmf"));
        assertNotNull(image);
    }

    @Test
    @Disabled("wip")
    void test2() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("wmf").next();
        ImageReadParam param = reader.getDefaultReadParam();
        param.setSourceRenderSize(new Dimension(2000, 2000));
        // #createImageInputStream() doesn't accept url
        ImageInputStream iis = ImageIO.createImageInputStream(WmfImageReaderTest.class.getResourceAsStream("/test.wmf"));
        assert iis != null : "is resource class correct?";
        reader.setInput(iis, true);
        BufferedImage image = reader.read(0, param);
        assertNotNull(image);
    }

    /**
     * @param args Usage: java svgImage <source file>
     */
    public static void main(String[] args) throws Exception {
//        String filename = args[0];

        ImageReader reader = ImageIO.getImageReadersByFormatName("wmf").next();
        ImageReadParam param = reader.getDefaultReadParam();
        param.setSourceRenderSize(new Dimension(2000, 2000));
        // #createImageInputStream() doesn't accept url
        ImageInputStream iis = ImageIO.createImageInputStream(WmfImageReaderTest.class.getResourceAsStream("/test.wmf"));
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
            }
        });
        frame.pack();
        frame.setVisible(true);
    }
}
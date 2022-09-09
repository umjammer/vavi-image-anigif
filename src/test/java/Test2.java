/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;


/**
 * Test2. Batik SVG direct
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2009/06/21 nsano initial version <br>
 */
public class Test2 {

    /** */
    static class BufferedImageTranscoder extends ImageTranscoder {
        private BufferedImage image;

        public BufferedImage createImage(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        public void writeImage(BufferedImage image, TranscoderOutput output) throws TranscoderException {
            // ignore output parameter
            this.image = image;
        }

        public BufferedImage getImage() {
            return image;
        }
    }

    /** */
    public static void main(String[] args) throws Exception {
        BufferedImageTranscoder trans = new BufferedImageTranscoder();
        TranscoderInput input = new TranscoderInput(Files.newInputStream(Paths.get(args[0])));
        trans.transcode(input, null);
        final BufferedImage image = trans.getImage();
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

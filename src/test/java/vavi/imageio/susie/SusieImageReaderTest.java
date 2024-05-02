/*
 * Copyright (c) ${YEAR} by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.susie;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;


/**
 * SusieImageReaderTest. (susie spi)
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070619 nsano initial version <br>
 */
public class SusieImageReaderTest {

    /** */
    public static void main(String[] args) throws Exception {
        new SusieImageReaderTest(args);
    }

    private Image bgImage;

    /** */
    SusieImageReaderTest(String[] args) throws IOException {
        JFrame frame = new JFrame("SPIConnector FaceDetectionTest");
        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
System.err.println("paint: " + bgImage);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, this);
                }
            }
        };
        panel.setPreferredSize(new Dimension(640, 480));
        frame.setContentPane(panel);
        frame.pack();
        Insets insets = frame.getInsets();
        frame.setBounds(0, 0, 640 + insets.left + insets.right, 480 + insets.top + insets.bottom);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("wmf");
        ImageReader reader = null;
        while (irs.hasNext()) {
            ImageReader ir = irs.next();
System.err.println("writer D: " + ir.getOriginatingProvider().getDescription(Locale.getDefault()));
System.err.println("writer C: " + ir.getClass().getName());
            if (ir.getClass().getName().equals("vavi.imageio.susie.SusieImageReader")) {
                reader = ir;
                break;
            }
        }
System.err.println("reader: " + reader.getClass().getName());

        reader.setInput(new File(args[0]));
        bgImage = reader.read(0);
System.err.println("image: " + bgImage);
        frame.setVisible(true);
    }
}

/* */
